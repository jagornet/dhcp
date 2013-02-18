/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6Server.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.jmx.HierarchyDynamicMBean;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcpv6.Version;
import com.jagornet.dhcpv6.db.DbSchemaManager;
import com.jagornet.dhcpv6.db.IaManager;
import com.jagornet.dhcpv6.option.DhcpOptionFactory;
import com.jagornet.dhcpv6.option.v4.DhcpV4OptionFactory;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.netty.NettyDhcpServer;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

/**
 * The main DhcpV6Server class.
 */
public class DhcpV6Server
{
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DhcpV6Server.class);

    /** The command line options. */
    protected Options options;
    
    /** The command line parser. */
    protected CommandLineParser parser;
    
    /** The help formatter. */
    protected HelpFormatter formatter;
    
    /** The default config filename. */
    public static String DEFAULT_CONFIG_FILENAME = DhcpConstants.DHCPV6_HOME != null ? 
    	(DhcpConstants.DHCPV6_HOME + "/conf/dhcpv6server.xml") : "conf/dhcpv6server.xml";
	
    /** The configuration filename. */
    protected String configFilename = DEFAULT_CONFIG_FILENAME;

    /** The application context filename. */
    public static String APP_CONTEXT_FILENAME = "com/jagornet/dhcpv6/context.xml";
    public static String APP_CONTEXT_V1SCHEMA_FILENAME = "com/jagornet/dhcpv6/context_v1schema.xml";
    public static String APP_CONTEXT_V2SCHEMA_FILENAME = "com/jagornet/dhcpv6/context_v2schema.xml";    
    
    /** The server port number. */
    protected int portNumber = DhcpConstants.SERVER_PORT;
    
    /** The multicast network interfaces. */
    protected List<NetworkInterface> mcastNetIfs = null;
    
    /** The unicast IP addresses. */
    protected List<InetAddress> ucastAddrs = null;
    
    protected NetworkInterface v4BcastNetIf = null;
    protected List<InetAddress> v4UcastAddrs = null;
    protected int v4PortNumber = DhcpConstants.V4_SERVER_PORT;
    
    protected DhcpServerConfiguration serverConfig = null;
    protected ApplicationContext context = null;
    
    /**
     * Instantiates the DHCPv6 server.
     * 
     * @param args the command line argument array
     */
    public DhcpV6Server(String[] args)
    {
        options = new Options();
        parser = new BasicParser();
        setupOptions();

        if(!parseOptions(args)) {
        	System.err.println("Invalid command line options: " + Arrays.toString(args));
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
//            formatter.printHelp(cliName, options);
            PrintWriter stderr = new PrintWriter(System.err, true);	// auto-flush=true
            formatter.printHelp(stderr, 80, cliName + " [options]", 
            				    Version.getVersion(), options, 2, 2, null);
            System.exit(0);
        }        
    }
    
    /**
     * Start the DHCPv6 server.  If multicast network interfaces have
     * been supplied on startup, then start a NetDhcpServer thread
     * on each of those interfaces.  Start one NioDhcpServer thread
     * which will listen on all IPv6 interfaces on the local host.
     * 
     * @throws Exception the exception
     */
    protected void start() throws Exception
    {
    	log.info("Starting Jagornet DHCPv6 Server");
    	log.info(Version.getVersion());
    	int cores = Runtime.getRuntime().availableProcessors();
    	log.info("Number of available core processors: " + cores);

    	Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	  log.info("Stopping Jagornet DHCPv6 Server");
                  System.out.println("Stopping Jagornet DHCPv6 Server: " + new Date());
                }
            });
    	
    	log.info("Initializing option factories...");
    	DhcpOptionFactory.init();
    	DhcpV4OptionFactory.init();
    	log.info("Option factories initialized.");
    	
        DhcpServerConfiguration.configFilename = configFilename;
        serverConfig = DhcpServerConfiguration.getInstance();
        if (serverConfig == null) {
        	throw new IllegalStateException("Failed to initialize server configuration file: " +
        			configFilename);
        }

		int schemaVersion = DhcpServerPolicies.globalPolicyAsInt(Property.DHCP_DATABASE_SCHEMA_VERSION);

        String appContext[] = null;
        if (schemaVersion <= 1) {
        	appContext = new String[] { APP_CONTEXT_FILENAME, APP_CONTEXT_V1SCHEMA_FILENAME };
        }
        else {
        	appContext = new String[] { APP_CONTEXT_FILENAME, APP_CONTEXT_V2SCHEMA_FILENAME };
        }
        log.info("Loading application context: " + Arrays.toString(appContext));
		context = new ClassPathXmlApplicationContext(appContext);
		if (context == null) {
			throw new IllegalStateException("Failed to initialize application context: " +
        			appContext);
		}
		log.info("Application context loaded.");
		
		DataSource dataSource = (DataSource) context.getBean("dataSource");
		if (dataSource == null) {
			throw new IllegalStateException("Failed to initialize DataSource");
		}
		
		boolean schemaCreated = DbSchemaManager.validateSchema(dataSource, schemaVersion);
		if (schemaCreated) {
			log.info("Database schema created.");
		}
		
		log.info("Loading managers from context...");
		
		NaAddrBindingManager naAddrBindingMgr = 
			(NaAddrBindingManager) context.getBean("naAddrBindingManager");
		if (naAddrBindingMgr != null) {
			try {
				log.info("Initializing NA Address Binding Manager");
				naAddrBindingMgr.init();
				serverConfig.setNaAddrBindingMgr(naAddrBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize NA Address Binding Manager", ex);
			}
		}
		else {
			log.warn("No NA Address Binding Manager available");
		}
		
		TaAddrBindingManager taAddrBindingMgr = 
			(TaAddrBindingManager) context.getBean("taAddrBindingManager");
		if (taAddrBindingMgr != null) {
			try {
				log.info("Initializing TA Address Binding Manager");
				taAddrBindingMgr.init();
				serverConfig.setTaAddrBindingMgr(taAddrBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize TA Address Binding Manager", ex);
			}
		}
		else {
			log.warn("No TA Address Binding Manager available");
		}
		
		PrefixBindingManager prefixBindingMgr = 
			(PrefixBindingManager) context.getBean("prefixBindingManager");
		if (prefixBindingMgr != null) {
			try {
				log.info("Initializing Prefix Binding Manager");
				prefixBindingMgr.init();
				serverConfig.setPrefixBindingMgr(prefixBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize Prefix Binding Manager", ex);
			}
		}
		else {
			log.warn("No Prefix Binding Manager available");
		}
		
		V4AddrBindingManager v4AddrBindingMgr = 
			(V4AddrBindingManager) context.getBean("v4AddrBindingManager");
		if (v4AddrBindingMgr != null) {
			try {
				log.info("Initializing V4 Address Binding Manager");
				v4AddrBindingMgr.init();
				serverConfig.setV4AddrBindingMgr(v4AddrBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize V4 Address Binding Manager", ex);
			}
		}
		else {
			log.warn("No V4 Address Binding Manager available");
		}
        
		IaManager iaMgr;
		if (schemaVersion <= 1) {
			iaMgr = (IaManager) context.getBean("iaManager");
		}
		else {
			iaMgr = (IaManager) context.getBean("leaseManager");
		}
		
		if (iaMgr == null) {
			log.warn("No IA Manager available");
		}
		else {
			serverConfig.setIaMgr(iaMgr);
		}
		
		log.info("Managers loaded.");
		
        registerLog4jInJmx();

        String msg = null;
        
        // by default, all IPv6 addresses are selected for unicast
        if (ucastAddrs == null) {
        	ucastAddrs = getAllIPv6Addrs();
        }
        msg = "DHCPv6 Unicast addresses: " + Arrays.toString(ucastAddrs.toArray());
        System.out.println(msg);
        log.info(msg);
        
        // for now, the mcast interfaces MUST be listed at
        // startup to get the mcast behavior at all... but
        // we COULD default to use all IPv6 interfaces 
        if (mcastNetIfs != null) {
//        	msg = "DHCPv6 Multicast interfaces: " + Arrays.toString(mcastNetIfs.toArray());
        	StringBuilder sb = new StringBuilder();
        	sb.append("DHCPv6 Multicast interfaces: [");
        	for (NetworkInterface mcastNetIf : mcastNetIfs) {
				sb.append(mcastNetIf.getName());
				sb.append(", ");
			}
        	sb.setLength(sb.length()-2);	// remove last ", "
        	sb.append("]");
        	msg = sb.toString();
        	System.out.println(msg);
        	log.info(msg);
        }
        else {
        	msg = "DHCPv6 Multicast interfaces: none";
        	System.out.println(msg);
        	log.info(msg);
        }
        
        msg = "DHCPv6 Port number: " + portNumber;
        System.out.println(msg);
        log.info(msg);

        
        // by default, all IPv4 addresses are selected for unicast
        if (v4UcastAddrs == null) {
        	v4UcastAddrs = getAllIPv4Addrs();
        }
        msg = "DHCPv4 Unicast addresses: " + Arrays.toString(v4UcastAddrs.toArray());
        System.out.println(msg);
        log.info(msg);
        
        if (v4BcastNetIf != null) {
        	msg = "DHCPv4 Broadcast Interface: " + v4BcastNetIf.getName();
        	System.out.println(msg);
        	log.info(msg);
        }
        else {
        	msg = "DHCPv4 Broadcast interface: none";
        	System.out.println(msg);
        	log.info(msg);
        }
        
        msg = "DHCPv4 Port number: " + v4PortNumber;
        System.out.println(msg);
        log.info(msg);
        
    	NettyDhcpServer nettyServer = new NettyDhcpServer(ucastAddrs, mcastNetIfs, portNumber, 
    														v4UcastAddrs, v4BcastNetIf, v4PortNumber);
    	nettyServer.start();
    }
    
	/**
	 * Setup command line options.
	 */
    @SuppressWarnings("static-access")
	private void setupOptions()
    {
        Option configFileOption =
        	OptionBuilder.withLongOpt("configfile")
        	.withArgName("filename")
        	.withDescription("Configuration file (default = $DHCPV6_HOME/conf/dhcpv6server.xml).")
        	.hasArg()
        	.create("c");
        options.addOption(configFileOption);
        
        Option portOption =
        	OptionBuilder.withLongOpt("port")
        	.withArgName("portnum")
        	.withDescription("DHCPv6 Port number (default = 547).")
        	.hasArg()
        	.create("p");
        options.addOption(portOption);

        Option mcastOption =
        	OptionBuilder.withLongOpt("mcast")
        	.withArgName("interfaces")
        	.withDescription("DHCPv6 Multicast support (default = none). " +
        			"Use this option without arguments to instruct the server to bind to all " +
        			"multicast-enabled IPv6 interfaces on the host. Optionally, use arguments " +
        			"to list specific interfaces, separated by spaces.")
        	.hasOptionalArgs()
        	.create("m");
        options.addOption(mcastOption);

        Option ucastOption =
        	OptionBuilder.withLongOpt("ucast")
        	.withArgName("addresses")
        	.withDescription("DHCPv6 Unicast addresses (default = all IPv6 addresses). " +
        			"Use this option to instruct the server to bind to a specific list " +
        			"of global IPv6 addresses, separated by spaces. These addresses " +
        			"should be configured on one or more DHCPv6 relay agents connected " +
        			"to DHCPv6 client links.")
        	.hasOptionalArgs()
        	.create("u");        				 
        options.addOption(ucastOption);
        
        Option v4BcastOption = 
        	OptionBuilder.withLongOpt("v4bcast")
        	.withArgName("interface")
        	.withDescription("DHCPv4 broadcast support (default = none). " +
        			"Use this option to specify the interface for the server to " +
        			"receive and send broadcast DHCPv4 packets. Only one interface " +
        			"may be specified. All other interfaces on the host will only " +
        			"receive and send unicast traffic.  The default IPv4 address on " +
        			"the specified interface will be used for determining the " +
        			"DHCPv4 client link within the server configuration file.")
        	.hasArg()
        	.create("4b");
        options.addOption(v4BcastOption);

        Option v4UcastOption =
        	OptionBuilder.withLongOpt("v4ucast")
        	.withArgName("addresses")
        	.withDescription("DHCPv4 Unicast addresses (default = all IPv4 addresses). " +
        			"Use this option to instruct the server to bind to a specific list " +
        			"of IPv4 addresses, separated by spaces. These addresses " +
        			"should be configured on one or more DHCPv4 relay agents connected " +
        			"to DHCPv4 client links.")
        	.hasOptionalArgs()
        	.create("4u");        				 
        options.addOption(v4UcastOption);
        
        Option v4PortOption =
        	OptionBuilder.withLongOpt("v4port")
        	.withArgName("v4portnum")
        	.withDescription("DHCPv4 Port number (default = 67).")
        	.hasArg()
        	.create("4p");
        options.addOption(v4PortOption);

        Option testConfigFileOption =
        	OptionBuilder.withLongOpt("test-configfile")
        	.withArgName("filename")
        	.withDescription("Test configuration file, then exit.")
        	.hasArg()
        	.create("tc");
        options.addOption(testConfigFileOption);

        Option listIfOption = new Option("li", "list-interfaces", false, 
        		"Show detailed host interface list, then exit.");
        options.addOption(listIfOption);

        Option versionOption = new Option("v", "version", false, 
        		"Show version information, then exit.");
        options.addOption(versionOption);
        
        Option helpOption = new Option("?", "help", false, "Show this help page.");        
        options.addOption(helpOption);
    }

    /**
     * Parses the command line options.
     * 
     * @param args the command line argument array
     * 
     * @return true, if all arguments were successfully parsed
     */
    protected boolean parseOptions(String[] args)
    {
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("?")) {
                return false;
            }
            if (cmd.hasOption("c")) {
                configFilename = cmd.getOptionValue("c");
            }
            if (cmd.hasOption("p")) {
            	String p = cmd.getOptionValue("p");
            	try {
            		portNumber = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		portNumber = DhcpConstants.SERVER_PORT;
            		System.err.println("Invalid port number: '" + p +
            							"' using default: " + portNumber +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("m")) {
            	String[] ifnames = cmd.getOptionValues("m");
            	if ((ifnames == null) || (ifnames.length < 1)) {
            		ifnames = new String[] { "*" };
            	}
        		mcastNetIfs = getIPv6NetIfs(ifnames);
        		if ((mcastNetIfs == null) || mcastNetIfs.isEmpty()) {
        			return false;
        		}
            }
            if (cmd.hasOption("u")) {
            	String[] addrs = cmd.getOptionValues("u");
            	if ((addrs == null) || (addrs.length < 1)) {
            		addrs = new String[] { "*" };
            	}
        		ucastAddrs = getIpAddrs(addrs);
        		if ((ucastAddrs == null) || ucastAddrs.isEmpty()) {
        			return false;
        		}
            }
            if (cmd.hasOption("4b")) {
            	String v4if = cmd.getOptionValue("4b");
        		v4BcastNetIf = getIPv4NetIf(v4if);
        		if (v4BcastNetIf == null) {
        			return false;
        		}
            }
            if (cmd.hasOption("4u")) {
            	String[] addrs = cmd.getOptionValues("4u");
            	if ((addrs == null) || (addrs.length < 1)) {
            		addrs = new String[] { "*" };
            	}
        		v4UcastAddrs = getV4IpAddrs(addrs);
        		if ((v4UcastAddrs == null) || v4UcastAddrs.isEmpty()) {
        			return false;
        		}
            }            
            if (cmd.hasOption("4p")) {
            	String p = cmd.getOptionValue("4p");
            	try {
            		v4PortNumber = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		v4PortNumber = DhcpConstants.V4_SERVER_PORT;
            		System.err.println("Invalid port number: '" + p +
            							"' using default: " + v4PortNumber +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("v")) {
            	System.err.println(Version.getVersion());
            	System.exit(0);
            }
            if (cmd.hasOption("tc")) {
            	try {
            		String filename = cmd.getOptionValue("tc");
            		System.err.println("Parsing server configuration file: " + filename);
            		DhcpV6ServerConfig config = DhcpServerConfiguration.parseConfig(filename);
            		if (config != null) {
            			System.err.println("OK: " + filename + " is a valid DHCPv6 server configuration file.");
            		}
            	}
            	catch (Exception ex) {
            		System.err.println("ERROR: " + ex);
            	}
            	System.exit(0);
            }
            if (cmd.hasOption("li")) {
    			Enumeration<NetworkInterface> netIfs = NetworkInterface.getNetworkInterfaces();
    			if (netIfs != null) {
    				while (netIfs.hasMoreElements()) {
    					NetworkInterface ni = netIfs.nextElement();
    					System.err.println(ni);
    				}
    			}
            	System.exit(0);
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
        } catch (SocketException se) {
			System.err.println("Network interface socket failure: " + se);
			return false;
		} catch (UnknownHostException he) {
			System.err.println("IP Address failure: " + he);
		}
        
        return true;
    }

	/**
	 * Gets the IPv6 network interfaces for the supplied interface names.
	 * 
	 * @param ifnames the interface names to locate NetworkInterfaces by
	 * 
	 * @return the list of NetworkInterfaces that are up, support multicast,
	 * and have at least one IPv6 address configured
	 * 
	 * @throws SocketException the socket exception
	 */
	private List<NetworkInterface> getIPv6NetIfs(String[] ifnames) throws SocketException 
	{
		List<NetworkInterface> netIfs = new ArrayList<NetworkInterface>();
		for (String ifname : ifnames) {
			if (ifname.equals("*")) {
				return getAllIPv6NetIfs();
			}
			NetworkInterface netIf = NetworkInterface.getByName(ifname);
			if (netIf == null) {
				// if not found by name, see if the name is actually an address
				try {
					InetAddress ipaddr = InetAddress.getByName(ifname);
					netIf = NetworkInterface.getByInetAddress(ipaddr);
				}
				catch (UnknownHostException ex) {
					log.warn("Unknown interface: " + ifname + ": " + ex);
				}
			}
			if (netIf != null) {
				if (netIf.isUp()) {
		        	// for multicast, the loopback interface is excluded
		        	if (netIf.supportsMulticast() && !netIf.isLoopback()) {
						boolean isV6 = false;
						List<InterfaceAddress> ifAddrs =
							netIf.getInterfaceAddresses();
						for (InterfaceAddress ifAddr : ifAddrs) {
							if (ifAddr.getAddress() instanceof Inet6Address) {
								netIfs.add(netIf);
								isV6 = true;
								break;
							}
						}
						if (!isV6) {
							System.err.println("Interface is not configured for IPv6: " +
												netIf);
							return null;
						}
					}
					else {
						System.err.println("Interface does not support multicast: " +
										   netIf);
						return null;
					}
				}
				else {
					System.err.println("Interface is not up: " +
										netIf);
					return null;
				}
			}
			else {
				System.err.println("Interface not found or inactive: " + ifname);
				return null;
			}
		}
		return netIfs;
	}

	/**
	 * Gets all IPv6 network interfaces on the local host.
	 * 
	 * @return the list NetworkInterfaces
	 */
	private List<NetworkInterface> getAllIPv6NetIfs() throws SocketException
	{
		List<NetworkInterface> netIfs = new ArrayList<NetworkInterface>();
        Enumeration<NetworkInterface> localInterfaces =
        	NetworkInterface.getNetworkInterfaces();
        if (localInterfaces != null) {
	        while (localInterfaces.hasMoreElements()) {
	        	NetworkInterface netIf = localInterfaces.nextElement();
	        	// for multicast, the loopback interface is excluded
	        	if (netIf.supportsMulticast() && !netIf.isLoopback()) {
	            	Enumeration<InetAddress> ifAddrs = netIf.getInetAddresses();
	            	while (ifAddrs.hasMoreElements()) {
	            		InetAddress ip = ifAddrs.nextElement();
	            		if (ip instanceof Inet6Address) {
	            			netIfs.add(netIf);
	            			break;	// out to next interface
	            		}
	            	}
	        	}
	        }
        }
        else {
        	log.error("No network interfaces found!");
        }
        return netIfs;
	}
	
	private List<InetAddress> getIpAddrs(String[] addrs) throws UnknownHostException
	{
		List<InetAddress> ipAddrs = new ArrayList<InetAddress>();
		for (String addr : addrs) {
			if (addr.equals("*")) {
				return getAllIPv6Addrs();
			}
			InetAddress ipAddr = InetAddress.getByName(addr);
			// allow only IPv6 addresses?
			ipAddrs.add(ipAddr);
		}
		return ipAddrs;
	}
	
	static List<InetAddress> allIPv6Addrs;
	public static List<InetAddress> getAllIPv6Addrs()
	{
		if (allIPv6Addrs == null) {
			allIPv6Addrs = new ArrayList<InetAddress>();
			try {
		        Enumeration<NetworkInterface> localInterfaces =
		        	NetworkInterface.getNetworkInterfaces();
		        if (localInterfaces != null) {
			        while (localInterfaces.hasMoreElements()) {
			        	NetworkInterface netIf = localInterfaces.nextElement();
		            	Enumeration<InetAddress> ifAddrs = netIf.getInetAddresses();
		            	while (ifAddrs.hasMoreElements()) {
		            		InetAddress ip = ifAddrs.nextElement();
		            		if (ip instanceof Inet6Address) {
		            			allIPv6Addrs.add(ip);
		            		}
		            	}
			        }
		        }
		        else {
		        	log.error("No network interfaces found!");
		        }
			}
			catch (IOException ex) {
				log.error("Failed to get IPv6 addresses: " + ex);
			}
		}
        return allIPv6Addrs;
	}
	
	private NetworkInterface getIPv4NetIf(String ifname) throws SocketException 
	{
		NetworkInterface netIf = NetworkInterface.getByName(ifname);
		if (netIf == null) {
			// if not found by name, see if the name is actually an address
			try {
				InetAddress ipaddr = InetAddress.getByName(ifname);
				netIf = NetworkInterface.getByInetAddress(ipaddr);
			}
			catch (UnknownHostException ex) {
				log.warn("Unknown interface: " + ifname + ": " + ex);
			}
		}
		if (netIf != null) {
			if (netIf.isUp()) {
	        	// the loopback interface is excluded
	        	if (!netIf.isLoopback()) {
					boolean isV4 = false;
					List<InterfaceAddress> ifAddrs =
						netIf.getInterfaceAddresses();
					for (InterfaceAddress ifAddr : ifAddrs) {
						if (ifAddr.getAddress() instanceof Inet4Address) {
							isV4 = true;
							break;
						}
					}
					if (!isV4) {
						System.err.println("Interface is not configured for IPv4: " +
											netIf);
						return null;
					}
				}
				else {
					System.err.println("Interface is loopback: " +
									   netIf);
					return null;
				}
			}
			else {
				System.err.println("Interface is not up: " +
									netIf);
				return null;
			}
		}
		else {
			System.err.println("Interface not found or inactive: " + ifname);
			return null;
		}
		return netIf;
	}
	
	private List<InetAddress> getV4IpAddrs(String[] addrs) throws UnknownHostException
	{
		List<InetAddress> ipAddrs = new ArrayList<InetAddress>();
		for (String addr : addrs) {
			if (addr.equals("*")) {
				return getAllIPv4Addrs();
			}
			InetAddress ipAddr = InetAddress.getByName(addr);
			// allow only IPv4 addresses?
			ipAddrs.add(ipAddr);
		}
		return ipAddrs;
	}
	
	static List<InetAddress> allIPv4Addrs;
	public static List<InetAddress> getAllIPv4Addrs()
	{
		if (allIPv4Addrs == null) {
			allIPv4Addrs = new ArrayList<InetAddress>();
			try {
		        Enumeration<NetworkInterface> localInterfaces =
		        	NetworkInterface.getNetworkInterfaces();
		        if (localInterfaces != null) {
			        while (localInterfaces.hasMoreElements()) {
			        	NetworkInterface netIf = localInterfaces.nextElement();
		            	Enumeration<InetAddress> ifAddrs = netIf.getInetAddresses();
		            	while (ifAddrs.hasMoreElements()) {
		            		InetAddress ip = ifAddrs.nextElement();
		            		if (ip instanceof Inet4Address) {
		            			allIPv4Addrs.add(ip);
		            		}
		            	}
			        }
		        }
		        else {
		        	log.error("No network interfaces found!");
		        }
			}
			catch (IOException ex) {
				log.error("Failed to get IPv4 addresses: " + ex);
			}
		}
        return allIPv4Addrs;
	}
	
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        try {
            DhcpV6Server server = new DhcpV6Server(args);
            System.out.println("Starting Jagornet DHCPv6 Server: " + new Date());
            System.out.println(Version.getVersion());
            server.start();
        }
        catch (Exception ex) {
            System.err.println("DhcpV6Server ABORT!");
            ex.printStackTrace();
            System.exit(1);
        }
	}
    
    /**
     * Load server configuration.  For use by the GUI.
     * 
     * @param filename the configuration filename
     * 
     * @return DhcpV6ServerConfig XML document object
     * 
     * @throws DhcpServerConfigException, XmlException, IOException
     */
    public static DhcpV6ServerConfig loadConfig(String filename) 
    		throws DhcpServerConfigException, XmlException, IOException
    {
        return DhcpServerConfiguration.loadConfig(filename);
    }
    
    /**
     * Save server configuration.  For use by the GUI.
     * 
     * @param config DhcpV6ServerConfig XML document object
     * @param filename the configuration filename
     * 
     * @throws IOException the exception
     */
    public static void saveConfig(DhcpV6ServerConfig config, String filename) throws IOException
    {
        DhcpServerConfiguration.saveConfig(config, filename);
    }

    /**
     * Static method to get the server configuration.  For use by the GUI.
     * 
     * @return the DhcpV6ServerConfig XML document object
     */
    public static DhcpV6ServerConfig getDhcpServerConfig()
    {
        return DhcpServerConfiguration.getInstance().getDhcpV6ServerConfig();
    }
        
    /**
     * Register Log4J in JMX to allow dynamic configuration
     * of server logging using JMX client (e.g. jconsole).
     */
    @SuppressWarnings("unchecked")
    public static void registerLog4jInJmx()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();  
        try {
            // Create and Register the top level Log4J MBean
            HierarchyDynamicMBean hdm = new HierarchyDynamicMBean();
            ObjectName mbo = new ObjectName("log4j:hiearchy=default");
            mbs.registerMBean(hdm, mbo);
    
            // Add the root logger to the Hierarchy MBean
            org.apache.log4j.Logger rootLogger =
            	org.apache.log4j.Logger.getRootLogger();
            hdm.addLoggerMBean(rootLogger.getName());
    
            // Get each logger from the Log4J Repository and add it to
            // the Hierarchy MBean created above.
            LoggerRepository r = LogManager.getLoggerRepository();
            Enumeration<Logger> loggers = r.getCurrentLoggers();
            if (loggers != null) {
                while (loggers.hasMoreElements()) {
                	org.apache.log4j.Logger logger = 
                		(org.apache.log4j.Logger) loggers.nextElement();
                    hdm.addLoggerMBean(logger.getName());
                }
            }
        }
        catch (Exception ex) {
            log.error("Failure registering Log4J in JMX: " + ex);
        }
    }
}
