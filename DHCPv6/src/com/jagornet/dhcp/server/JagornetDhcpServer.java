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
package com.jagornet.dhcp.server;

import java.io.File;
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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.jmx.HierarchyDynamicMBean;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcp.Version;
import com.jagornet.dhcp.db.DbSchemaManager;
import com.jagornet.dhcp.db.IaManager;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.netty.NettyDhcpServer;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.DhcpServerConfigDocument.DhcpServerConfig;

/**
 * The main DhcpV6Server class.
 */
public class JagornetDhcpServer
{
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(JagornetDhcpServer.class);

    /** The command line options. */
    protected Options options;
    
    /** The command line parser. */
    protected CommandLineParser parser;
    
    /** The help formatter. */
    protected HelpFormatter formatter;
    
    /** The default config filename. */
    public static String DEFAULT_CONFIG_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME != null ? 
    	(DhcpConstants.JAGORNET_DHCP_HOME + "/conf/dhcpserver.xml") : "conf/dhcpserver.xml";
	
    /** The configuration filename. */
    protected String configFilename = DEFAULT_CONFIG_FILENAME;

    /** The application context filename. */
    public static String APP_CONTEXT_FILENAME = "com/jagornet/dhcp/context.xml";
    public static String APP_CONTEXT_JDBC_DATASOURCE_FILENAME = "com/jagornet/dhcp/context_jdbc_datasource.xml";
    public static String APP_CONTEXT_JDBC_DERBY_FILENAME = "com/jagornet/dhcp/context_jdbc_derby.xml";
    public static String APP_CONTEXT_JDBC_H2_FILENAME = "com/jagornet/dhcp/context_jdbc_h2.xml";
    public static String APP_CONTEXT_JDBC_SQLITE_FILENAME = "com/jagornet/dhcp/context_jdbc_sqlite.xml";
    public static String APP_CONTEXT_JDBC_FILENAME = "com/jagornet/dhcp/context_jdbc.xml";
    public static String APP_CONTEXT_JDBC_V1SCHEMA_FILENAME = "com/jagornet/dhcp/context_jdbc_v1schema.xml";
    public static String APP_CONTEXT_JDBC_V2SCHEMA_FILENAME = "com/jagornet/dhcp/context_jdbc_v2schema.xml";    
    public static String APP_CONTEXT_SQLITE_V2SCHEMA_FILENAME = "com/jagornet/dhcp/context_sqlite_v2schema.xml";    
    public static String APP_CONTEXT_MONGO_V2SCHEMA_FILENAME = "com/jagornet/dhcp/context_mongo_v2schema.xml";    
    
    /** DHCPv6 Multicast interfaces */
    protected List<NetworkInterface> v6McastNetIfs = null;
    /** DHCPv6 Unicast addresses */
    protected List<InetAddress> v6UcastAddrs = null;
    /** DHCPv6 Server port number */
    protected int v6PortNumber = DhcpConstants.V6_SERVER_PORT;
    
    /** DHCPv4 Broadcast interface */
    protected NetworkInterface v4BcastNetIf = null;
    /** DHCPv4 Unicast addresses */
    protected List<InetAddress> v4UcastAddrs = null;
    /** DHCPv4 Server port number */
    protected int v4PortNumber = DhcpConstants.V4_SERVER_PORT;
    
    protected DhcpServerConfiguration serverConfig = null;
    protected ApplicationContext context = null;
    
    /**
     * Instantiates the DHCPv6 server.
     * 
     * @param args the command line argument array
     */
    public JagornetDhcpServer(String[] args)
    {
        options = new Options();
        parser = new BasicParser();
        setupOptions();

        if(!parseOptions(args)) {
        	System.err.println("Invalid command line options: " + Arrays.toString(args));
        	showHelp();
            System.exit(0);
        }        
    }
    
    public void showHelp() {
        formatter = new HelpFormatter();
        String cliName = this.getClass().getName();
//        formatter.printHelp(cliName, options);
        PrintWriter stderr = new PrintWriter(System.err, true);	// auto-flush=true
        formatter.printHelp(stderr, 80, cliName + " [options]", 
        				    Version.getVersion(), options, 2, 2, null);    	
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
    	log.info("Starting Jagornet DHCP Server");
    	log.info(Version.getVersion());
    	int cores = Runtime.getRuntime().availableProcessors();
    	log.info("Number of available core processors: " + cores);

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
        
        String schemaType = DhcpServerPolicies.globalPolicy(Property.DATABASE_SCHEMA_TYTPE);
    	int schemaVersion = DhcpServerPolicies.globalPolicyAsInt(Property.DATABASE_SCHEMA_VERSION);
        String[] appContext = getAppContextFiles(schemaType, schemaVersion);     
        log.info("Loading application context: " + Arrays.toString(appContext));
		context = new ClassPathXmlApplicationContext(appContext);
		if (context == null) {
			throw new IllegalStateException("Failed to initialize application context: " +
        			appContext);
		}
		log.info("Application context loaded.");
		
		loadManagers();
		
        registerLog4jInJmx();

        String msg = null;
        
        // by default, all non-loopback, non-linklocal,
        // IPv6 addresses are selected for unicast
        if (v6UcastAddrs == null) {
        	v6UcastAddrs = getFilteredIPv6Addrs();
        }
        msg = "DHCPv6 Unicast addresses: " + Arrays.toString(v6UcastAddrs.toArray());
        System.out.println(msg);
        log.info(msg);
        
        // for now, the mcast interfaces MUST be listed at
        // startup to get the mcast behavior at all... but
        // we COULD default to use all IPv6 interfaces 
        if (v6McastNetIfs != null) {
//        	msg = "DHCPv6 Multicast interfaces: " + Arrays.toString(mcastNetIfs.toArray());
        	StringBuilder sb = new StringBuilder();
        	sb.append("DHCPv6 Multicast interfaces: [");
        	for (NetworkInterface mcastNetIf : v6McastNetIfs) {
				sb.append(mcastNetIf.getName());
				sb.append(", ");
			}
        	sb.setLength(sb.length()-2);	// remove last ", "
        	sb.append(']');
        	msg = sb.toString();
        	System.out.println(msg);
        	log.info(msg);
        }
        else {
        	msg = "DHCPv6 Multicast interfaces: none";
        	System.out.println(msg);
        	log.info(msg);
        }
        
        msg = "DHCPv6 Port number: " + v6PortNumber;
        System.out.println(msg);
        log.info(msg);

        
        // by default, all non-loopback, non-linklocal,
        // IPv4 addresses are selected for unicast
        if (v4UcastAddrs == null) {
        	v4UcastAddrs = getFilteredIPv4Addrs();
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
        
    	NettyDhcpServer nettyServer = new NettyDhcpServer(v6UcastAddrs, v6McastNetIfs, v6PortNumber, 
    														v4UcastAddrs, v4BcastNetIf, v4PortNumber);
    	nettyServer.start();
    }
    
    public static String[] getAppContextFiles(String schemaType, int schemaVersion) throws Exception {

    	List<String> appContexts = new ArrayList<String>();

    	String dbDir = DbSchemaManager.DB_HOME;
    	
    	if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_JDBC_DERBY) ||
    			schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_JDBC_H2) ||
    			schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_JDBC_SQLITE)) {
            String jdbcContext = null;
        	if (schemaVersion == 1) {
        		jdbcContext = APP_CONTEXT_JDBC_V1SCHEMA_FILENAME;
        	}
        	else if (schemaVersion == 2) {
        		jdbcContext = APP_CONTEXT_JDBC_V2SCHEMA_FILENAME;
        	}
        	else {
        		throw new IllegalStateException("Unsupported schema version: " + schemaVersion);
        	}
        	
        	if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_JDBC_DERBY)) {
        		FileUtils.forceMkdir(new File(dbDir + "derby"));
            	appContexts.add(APP_CONTEXT_JDBC_DERBY_FILENAME);
            	appContexts.add(APP_CONTEXT_JDBC_DATASOURCE_FILENAME);
            	appContexts.add(jdbcContext);
            	appContexts.add(APP_CONTEXT_FILENAME);
            }
            else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_JDBC_H2)) {
        		FileUtils.forceMkdir(new File(dbDir + "h2"));
            	appContexts.add(APP_CONTEXT_JDBC_H2_FILENAME);
            	appContexts.add(APP_CONTEXT_JDBC_DATASOURCE_FILENAME);
            	appContexts.add(jdbcContext);
            	appContexts.add(APP_CONTEXT_FILENAME);
            }
            else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_JDBC_SQLITE)) {
        		FileUtils.forceMkdir(new File(dbDir + "sqlite"));
            	appContexts.add(APP_CONTEXT_JDBC_SQLITE_FILENAME);
            	appContexts.add(APP_CONTEXT_JDBC_DATASOURCE_FILENAME);
            	appContexts.add(jdbcContext);
            	appContexts.add(APP_CONTEXT_FILENAME);
            }
            else {
            	log.warn("Unknown JDBC data source, using jdbc.properties");
            	appContexts.add(APP_CONTEXT_JDBC_FILENAME);
            	appContexts.add(APP_CONTEXT_JDBC_DATASOURCE_FILENAME);
            	appContexts.add(jdbcContext);
            	appContexts.add(APP_CONTEXT_FILENAME);
            }
    	}
        else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_SQLITE)) {
    		FileUtils.forceMkdir(new File(dbDir + "sqlite"));
        	appContexts.add(APP_CONTEXT_SQLITE_V2SCHEMA_FILENAME);
        	appContexts.add(APP_CONTEXT_FILENAME);
        }
        else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_MONGO)) {
        	appContexts.add(APP_CONTEXT_MONGO_V2SCHEMA_FILENAME);
        	appContexts.add(APP_CONTEXT_FILENAME);
        }
        else {
        	throw new DhcpServerConfigException("Unsupported schema type: " + schemaType);
        }
        
    	String[] ctxArray = new String[appContexts.size()];
    	return appContexts.toArray(ctxArray);
    }
    
    private void loadManagers() throws Exception {
		
		log.info("Loading managers from context...");
		
		V6NaAddrBindingManager v6NaAddrBindingMgr = 
			(V6NaAddrBindingManager) context.getBean("v6NaAddrBindingManager");
		if (v6NaAddrBindingMgr != null) {
			try {
				log.info("Initializing V6 NA Address Binding Manager");
				v6NaAddrBindingMgr.init();
				serverConfig.setNaAddrBindingMgr(v6NaAddrBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize V6 NA Address Binding Manager", ex);
				throw ex;
			}
		}
		else {
			log.warn("No V6 NA Address Binding Manager available");
		}
		
		V6TaAddrBindingManager v6TaAddrBindingMgr = 
			(V6TaAddrBindingManager) context.getBean("v6TaAddrBindingManager");
		if (v6TaAddrBindingMgr != null) {
			try {
				log.info("Initializing V6 TA Address Binding Manager");
				v6TaAddrBindingMgr.init();
				serverConfig.setTaAddrBindingMgr(v6TaAddrBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize V6 TA Address Binding Manager", ex);
				throw ex;
			}
		}
		else {
			log.warn("No V6 TA Address Binding Manager available");
		}
		
		V6PrefixBindingManager v6PrefixBindingMgr = 
			(V6PrefixBindingManager) context.getBean("v6PrefixBindingManager");
		if (v6PrefixBindingMgr != null) {
			try {
				log.info("Initializing V6 Prefix Binding Manager");
				v6PrefixBindingMgr.init();
				serverConfig.setPrefixBindingMgr(v6PrefixBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize V6 Prefix Binding Manager", ex);
				throw ex;
			}
		}
		else {
			log.warn("No V6 Prefix Binding Manager available");
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
				throw ex;
			}
		}
		else {
			log.warn("No V4 Address Binding Manager available");
		}
        
		IaManager iaMgr = (IaManager) context.getBean("iaManager");		
		if (iaMgr != null) {
			serverConfig.setIaMgr(iaMgr);
		}
		else {
			log.warn("No IA Manager available");
		}
		
		log.info("Managers loaded.");
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
        	.withDescription("Configuration file (default = " + DEFAULT_CONFIG_FILENAME + ").")
        	.hasArg()
        	.create("c");
        options.addOption(configFileOption);
        
        Option portOption =
        	OptionBuilder.withLongOpt("v6port")
        	.withArgName("portnum")
        	.withDescription("DHCPv6 Port number (default = 547).")
        	.hasArg()
        	.create("6p");
        options.addOption(portOption);

        Option mcastOption =
        	OptionBuilder.withLongOpt("v6mcast")
        	.withArgName("interfaces")
        	.withDescription("DHCPv6 Multicast support (default = none). " +
        			"Use this option without arguments to instruct the server to bind to all " +
        			"multicast-enabled IPv6 interfaces on the host. Optionally, use arguments " +
        			"to list specific interfaces, separated by spaces.")
        	.hasOptionalArgs()
        	.create("6m");
        options.addOption(mcastOption);

        Option ucastOption =
        	OptionBuilder.withLongOpt("v6ucast")
        	.withArgName("addresses")
        	.withDescription("DHCPv6 Unicast addresses (default = all IPv6 addresses). " +
        			"Use this option to instruct the server to bind to a specific list " +
        			"of global IPv6 addresses, separated by spaces. These addresses " +
        			"should be configured on one or more DHCPv6 relay agents connected " +
        			"to DHCPv6 client links.")
        	.hasOptionalArgs()
        	.create("6u");        				 
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
        	.withArgName("portnum")
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
                showHelp();
                System.exit(0);
            }
            if (cmd.hasOption("c")) {
                configFilename = cmd.getOptionValue("c");
            }
            if (cmd.hasOption("6p")) {
            	String p = cmd.getOptionValue("6p");
            	try {
            		v6PortNumber = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		v6PortNumber = DhcpConstants.V6_SERVER_PORT;
            		System.err.println("Invalid port number: '" + p +
            							"' using default: " + v6PortNumber +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("6m")) {
            	String[] ifnames = cmd.getOptionValues("6m");
            	if ((ifnames == null) || (ifnames.length < 1)) {
            		ifnames = new String[] { "*" };
            	}
        		v6McastNetIfs = getIPv6NetIfs(ifnames);
        		if ((v6McastNetIfs == null) || v6McastNetIfs.isEmpty()) {
        			return false;
        		}
            }
            if (cmd.hasOption("6u")) {
            	String[] addrs = cmd.getOptionValues("6u");
            	if ((addrs == null) || (addrs.length < 1)) {
            		addrs = new String[] { "*" };
            	}
        		v6UcastAddrs = getIpAddrs(addrs);
        		if ((v6UcastAddrs == null) || v6UcastAddrs.isEmpty()) {
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
            		DhcpServerConfig config = DhcpServerConfiguration.parseConfig(filename);
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

	public static List<InetAddress> getFilteredIPv6Addrs() {
		
    	boolean ignoreLoopback = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LOOPBACK);
    	boolean ignoreLinkLocal = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LINKLOCAL);
    	
		List<InetAddress> myV6Addrs = new ArrayList<InetAddress>();
		List<InetAddress> allV6Addrs = getAllIPv6Addrs();
		if (allV6Addrs != null) {
			for (InetAddress ip : allV6Addrs) {
        		if (ignoreLoopback && ip.isLoopbackAddress()) {
        			log.debug("Skipping loopback address: " + ip);
        			continue;
        		}
        		if (ignoreLinkLocal && ip.isLinkLocalAddress()) {
        			log.debug("Skipping link local address: " + ip);
        			continue;
        		}
        		myV6Addrs.add(ip);
			}
		}
		return myV6Addrs;
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
	
	public static List<InetAddress> getFilteredIPv4Addrs() {
		
    	boolean ignoreLoopback = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LOOPBACK);
    	boolean ignoreLinkLocal = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LINKLOCAL);
    	
		List<InetAddress> myV4Addrs = new ArrayList<InetAddress>();
		List<InetAddress> allV4Addrs = getAllIPv4Addrs();
		if (allV4Addrs != null) {
			for (InetAddress ip : allV4Addrs) {
        		if (ignoreLoopback && ip.isLoopbackAddress()) {
        			log.debug("Skipping loopback address: " + ip);
        			continue;
        		}
        		if (ignoreLinkLocal && ip.isLinkLocalAddress()) {
        			log.debug("Skipping link local address: " + ip);
        			continue;
        		}
        		myV4Addrs.add(ip);
			}
		}
		return myV4Addrs;		
	}
	
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        try {
            JagornetDhcpServer server = new JagornetDhcpServer(args);
            System.out.println("Starting Jagornet DHCP Server: " + new Date());
            System.out.println(Version.getVersion());
            server.start();
        }
        catch (Exception ex) {
            System.err.println("DhcpServer ABORT!");
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
    public static DhcpServerConfig loadConfig(String filename) 
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
    public static void saveConfig(DhcpServerConfig config, String filename) throws IOException
    {
        DhcpServerConfiguration.saveConfig(config, filename);
    }

    /**
     * Static method to get the server configuration.  For use by the GUI.
     * 
     * @return the DhcpV6ServerConfig XML document object
     */
    public static DhcpServerConfig getDhcpServerConfig()
    {
        return DhcpServerConfiguration.getInstance().getDhcpServerConfig();
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
