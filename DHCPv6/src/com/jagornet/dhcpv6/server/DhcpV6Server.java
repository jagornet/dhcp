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
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.netty.NettyDhcpServer;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;
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
    public static String appContextFilename = "com/jagornet/dhcpv6/context.xml";
    
    /** The server port number. */
    protected int portNumber = DhcpConstants.SERVER_PORT;
    
    /** The multicast network interfaces. */
    protected List<NetworkInterface> mcastNetIfs = null;
    
    /** The multicast server thread. */
    protected Thread mcastThread;
    
    /** The unicast IP addresses. */
    protected List<InetAddress> ucastAddrs = null;
    
    /** The unicast server thread. */
    protected Thread ucastThread;
    
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

    	Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	  log.info("Stopping Jagornet DHCPv6 Server");
                  System.out.println("Stopping Jagornet DHCPv6 Server: " + new Date());
                }
            });
    	
        DhcpServerConfiguration.configFilename = configFilename;
        serverConfig = DhcpServerConfiguration.getInstance();
        if (serverConfig == null) {
        	throw new IllegalStateException("Failed to initialize server configuration file: " +
        			configFilename);
        }
        
        log.info("Loading application context: " + appContextFilename);
		context = new ClassPathXmlApplicationContext(appContextFilename);
		if (context == null) {
			throw new IllegalStateException("Failed to initialize application context: " +
        			appContextFilename);
		}
		log.info("Application context loaded.");
		
		DataSource dataSource = (DataSource) context.getBean("dataSource");
		if (dataSource == null) {
			throw new IllegalStateException("Failed to initialize DataSource");
		}
		
		DbSchemaManager.validateSchema(dataSource);
		
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
        
		IaManager iaMgr = (IaManager) context.getBean("iaManager");
		if (iaMgr == null) {
			log.warn("No IA Manager available");
		}
		else {
			serverConfig.setIaMgr(iaMgr);
		}
		
		log.info("Managers loaded.");
		
        registerLog4jInJmx();

        String msg = "Port number: " + portNumber;
        System.out.println(msg);
        log.info(msg);
        
        // for now, the mcast interfaces MUST be listed at
        // startup to get the mcast behavior at all... but
        // we COULD default to use all IPv6 interfaces 
        if (mcastNetIfs != null) {
        	msg = "Multicast interfaces: " + Arrays.toString(mcastNetIfs.toArray());
        	System.out.println(msg);
        	log.info(msg);
        }
        else {
        	msg = "Multicast interfaces: none";
        	System.out.println(msg);
        	log.info(msg);
        }
        
        // by default, all IPv6 addresses are selected for unicast
        if (ucastAddrs == null) {
        	ucastAddrs = getAllIPv6Addrs();
        }
        msg = "Unicast addresses: " + Arrays.toString(ucastAddrs.toArray());
        System.out.println(msg);
        log.info(msg);
        
    	NettyDhcpServer nettyServer = new NettyDhcpServer(ucastAddrs, mcastNetIfs, portNumber);
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
        	.withDescription("Port Number (default = 547).")
        	.hasArg()
        	.create("p");
        options.addOption(portOption);

        Option mcastOption =
        	OptionBuilder.withLongOpt("mcast")
        	.withArgName("interfaces")
        	.withDescription("Multicast support (default = no multicast). " +
        			"Optionally list specific interfaces,\n" +
        			"leave empty to select all IPv6 interfaces.")
        	.hasOptionalArgs()
        	.create("m");
        				 
        options.addOption(mcastOption);

        Option ucastOption =
        	OptionBuilder.withLongOpt("ucast")
        	.withArgName("interfaces")
        	.withDescription("Unicast support (default = wildcard address). " +
        			"Optionally list specific addresses to bind to.")
        	.hasOptionalArgs()
        	.create("u");
        				 
        options.addOption(ucastOption);
        
        Option testConfigFileOption =
        	OptionBuilder.withLongOpt("test-configfile")
        	.withArgName("filename")
        	.withDescription("Test configuration file, then exit.")
        	.hasArg()
        	.create("tc");
        options.addOption(testConfigFileOption);

        Option listIfOption = new Option("li", "list-interfaces", false, 
        		"Show detailed interface list, then exit.");
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
												netIf.getDisplayName());
							return null;
						}
					}
					else {
						System.err.println("Interface does not support multicast: " +
										   netIf.getDisplayName());
						return null;
					}
				}
				else {
					System.err.println("Interface is not up: " +
										netIf.getDisplayName());
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
	
	private List<InetAddress> getAllIPv6Addrs()
	{
		List<InetAddress> ipAddrs = new ArrayList<InetAddress>();
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
	            			ipAddrs.add(ip);
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
        return ipAddrs;
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
