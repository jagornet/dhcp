/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JagornetDhcpServer.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server;

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
import javax.xml.bind.JAXBException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.db.DbSchemaManager;
import com.jagornet.dhcp.server.db.IaManager;
import com.jagornet.dhcp.server.netty.NettyDhcpServer;
import com.jagornet.dhcp.server.request.binding.BaseBindingManager;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcp.server.rest.JerseyRestServer;

import io.netty.channel.Channel;

/**
 * The main DhcpV6Server class.
 */
public class JagornetDhcpServer
{
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(JagornetDhcpServer.class);

	/** The INSTANCE. */
	private static JagornetDhcpServer INSTANCE;

    /** The command line options. */
    protected Options options;
    
    /** The command line parser. */
    protected CommandLineParser parser;
    
    /** The help formatter. */
    protected HelpFormatter formatter;
    
    public static String JAGORNET_DHCP_SERVER = "Jagornet DHCP Server";
    
    /** The default config filename. */
    public static String DEFAULT_CONFIG_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME != null ? 
    	(DhcpConstants.JAGORNET_DHCP_HOME + "/config/dhcpserver.xml") : "config/dhcpserver.xml";
	
    /** The configuration filename. */
    protected String configFilename = DEFAULT_CONFIG_FILENAME;

    /** The application context filename. */
    public static String APP_CONTEXT_FILENAME = "context.xml";
    
    public static String ALL = "all";
    public static String NONE = "none";
    
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
    
    /** HTTPS Unicast address */
    protected InetAddress httpsAddr = null;
    protected int httpsPortNumber = JerseyRestServer.HTTPS_SERVER_PORT;
    
    protected DhcpServerConfiguration serverConfig = null;
    protected ApplicationContext context = null;
    
    protected JerseyRestServer jerseyServer = null;
    
    public static synchronized JagornetDhcpServer getInstance() throws Exception
    {
    	if (INSTANCE == null) {
    		try {
    			INSTANCE = new JagornetDhcpServer();
    		}
    		catch (Exception ex) {
    			log.error("Failed to initialize JagornetDhcpServer", ex);
    			throw ex;
    		}
    	}
    	return INSTANCE;
    }
    
    private JagornetDhcpServer() {
    	
    }
    
    /**
     * Instantiates the Jagornet DHCP server.
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
    protected void start(String[] args) throws Exception
    {
    	log.info("Starting " + JAGORNET_DHCP_SERVER);
    	log.info(Version.getVersion());
    	log.info("Arguments: " + Arrays.toString(args));
    	int cores = Runtime.getRuntime().availableProcessors();
    	log.info("Number of available core processors: " + cores);

    	Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	  log.info("Stopping " + JAGORNET_DHCP_SERVER);
                  System.out.println("Stopping " + JAGORNET_DHCP_SERVER + ": " + new Date());
                }
            });
    	
        serverConfig = DhcpServerConfiguration.getInstance();
        serverConfig.init(configFilename);
        
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
        
        if (v4UcastAddrs != null) {
        	v4UcastAddrs = getFilteredIPv4Addrs(v4UcastAddrs);
	        msg = "DHCPv4 Unicast addresses: " + Arrays.toString(v4UcastAddrs.toArray());
	        System.out.println(msg);
	        log.info(msg);
        }
        else {
        	msg = "DHCPv4 Unicast addresses: none";
        	System.out.println(msg);
        	log.info(msg);
        }
        
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
        
        if ((v4UcastAddrs != null) || (v4BcastNetIf != null)) {
	        msg = "DHCPv4 Port number: " + v4PortNumber;
	        System.out.println(msg);
	        log.info(msg);
        }
        
        if (v6UcastAddrs != null) {
        	v6UcastAddrs = getFilteredIPv6Addrs(v6UcastAddrs);
            msg = "DHCPv6 Unicast addresses: " + Arrays.toString(v6UcastAddrs.toArray());
            System.out.println(msg);
            log.info(msg);
        }
        else {
        	msg = "DHCPv6 Unicast addresses: none";
        	System.out.println(msg);
        	log.info(msg);
        }
        
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
        
        if ((v6UcastAddrs != null) || (v6McastNetIfs != null)) {
	        msg = "DHCPv6 Port number: " + v6PortNumber;
	        System.out.println(msg);
	        log.info(msg);
        }
        
    	NettyDhcpServer nettyServer = new NettyDhcpServer(
    			v4UcastAddrs, v4BcastNetIf, v4PortNumber,
    			v6UcastAddrs, v6McastNetIfs, v6PortNumber);
    	
    	nettyServer.start();
    	    	
    	if (httpsAddr != null) {
	    	//HttpServer jerseyHttpServer = JerseyRestServer.startGrizzlyServer();
	    	Channel jerseyHttpServer = 
	    			JerseyRestServer.startNettyServer(httpsAddr, httpsPortNumber);
	    	if (jerseyHttpServer == null) {
	    		log.error("Failed to create Jersey JAX-RS Netty HTTPS Server");
	    	}
	    	else if (!jerseyHttpServer.isActive()) {
	    		log.warn("Jersey JAX-RS Netty HTTPS Server is not active");
	    	}
	    	if (serverConfig.getHaPrimaryFSM() != null) {
	    		serverConfig.getHaPrimaryFSM().init();
		    	log.info("HA Primary DHCP server startup with HTTPS complete");
	    	}
	    	else if (serverConfig.getHaBackupFSM() != null) {
	    		serverConfig.getHaBackupFSM().init();
		    	log.info("HA Backup DHCP server startup with HTTPS complete");
	    	}
	    	else {
	    		log.info("Standalone server startup with HTTPS complete");
	    	}
    	}
    	else if (serverConfig.isHA()) {
    		throw new DhcpServerConfigException("HTTPS is required for HA configuration");
    	}
    	else {
    		log.info("Standalone server startup complete");
    	}
    }
    
    public static String[] getAppContextFiles(String schemaType, int schemaVersion) throws Exception {

    	List<String> appContexts = new ArrayList<String>();

    	appContexts.addAll(DbSchemaManager.getDbContextFiles(schemaType, schemaVersion));
    	appContexts.add(APP_CONTEXT_FILENAME);
        
    	String[] ctxArray = new String[appContexts.size()];
    	return appContexts.toArray(ctxArray);
    }
    
    private void loadManagers() throws Exception {
		
		log.info("Loading managers from context...");
		
		//TODO: Check if binding manager init method can be
		//		done by Spring in the context.xml file?
		//		Maybe init is here for error handling at startup?
		
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
		
		V6NaAddrBindingManager v6NaAddrBindingMgr = 
			(V6NaAddrBindingManager) context.getBean("v6NaAddrBindingManager");
		if (v6NaAddrBindingMgr != null) {
			try {
				log.info("Initializing V6 NA Address Binding Manager");
				v6NaAddrBindingMgr.init();
				serverConfig.setV6NaAddrBindingMgr(v6NaAddrBindingMgr);
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
				serverConfig.setV6TaAddrBindingMgr(v6TaAddrBindingMgr);
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
				serverConfig.setV6PrefixBindingMgr(v6PrefixBindingMgr);
			}
			catch (Exception ex) {
				log.error("Failed initialize V6 Prefix Binding Manager", ex);
				throw ex;
			}
		}
		else {
			log.warn("No V6 Prefix Binding Manager available");
		}

    	Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
	          	  if (v4AddrBindingMgr != null) {
	        		  ((BaseBindingManager) v4AddrBindingMgr).close();
	        	  }
            	  if (v6NaAddrBindingMgr != null) {
            		  ((BaseBindingManager) v6NaAddrBindingMgr).close();
            	  }
            	  if (v6TaAddrBindingMgr != null) {
            		  ((BaseBindingManager) v6TaAddrBindingMgr).close();
            	  }
            	  if (v6PrefixBindingMgr != null) {
            		  ((BaseBindingManager) v6PrefixBindingMgr).close();
            	  }
                }
            });
        
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
        
        Option v4BcastOption = 
        	OptionBuilder.withLongOpt("v4bcast")
        	.withArgName("interface")
        	.withDescription("DHCPv4 broadcast interface (default = none). " +
        			"Use this option to specify the interface for the server to " +
        			"receive and send broadcast DHCPv4 packets. " + /* Only one interface " +
        			"may be specified. All other interfaces on the host will only " +
        			"receive and send unicast traffic. */ "The default IPv4 address on " +
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

        Option mcastOption =
        	OptionBuilder.withLongOpt("v6mcast")
        	.withArgName("interfaces")
        	.withDescription("DHCPv6 Multicast interfaces (default = none). " +
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
        
        Option portOption =
        	OptionBuilder.withLongOpt("v6port")
        	.withArgName("portnum")
        	.withDescription("DHCPv6 Port number (default = 547).")
        	.hasArg()
        	.create("6p");
        options.addOption(portOption);

        Option hAddrOption =
        	OptionBuilder.withLongOpt("haddr")
        	.withArgName("address")
        	.withDescription("HTTPS address (default = all IP addresses). " +
        			"Use this option to instruct the server to bind to a specific " +
        			"IP address for HTTPS communications. Set the value to 'none' " +
        			"(without quotes) to disable HTTPS for standalone server.")
        	.hasOptionalArgs()
        	.create("ha");        				 
        options.addOption(hAddrOption);
        
        Option hPortOption =
        	OptionBuilder.withLongOpt("hport")
        	.withArgName("portnum")
        	.withDescription("HTTPS Port number (default = " +
        					JerseyRestServer.HTTPS_SERVER_PORT + ").")
        	.hasArg()
        	.create("hp");
        options.addOption(hPortOption);

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
            
            v4BcastNetIf = null;	// default
            if (cmd.hasOption("4b")) {
            	String v4if = cmd.getOptionValue("4b");
        		v4BcastNetIf = getIPv4NetIf(v4if);
        		if (v4BcastNetIf == null) {
        			return false;
        		}
            }
            
    		v4UcastAddrs = getAllIPv4Addrs();	// default
            if (cmd.hasOption("4u")) {
            	String[] addrs = cmd.getOptionValues("4u");
            	if (addrs != null) {
            		if (addrs.length == 1) {
            			String addr = addrs[0];
            			if (addr.equalsIgnoreCase(NONE)) {
            				v4UcastAddrs = null;
            			}
            			if (addr.equals("*") || addr.equalsIgnoreCase(ALL)) {
            				v4UcastAddrs = getAllIPv4Addrs();
            			}
            		}
            		else {	            		
	            		v4UcastAddrs = getV4IpAddrs(addrs);
	            		if ((v4UcastAddrs == null) || v4UcastAddrs.isEmpty()) {
	            			return false;
	            		}
            		}
            	}
            	else {
            		v4UcastAddrs = getAllIPv4Addrs();
            	}
            }

            v4PortNumber = DhcpConstants.V4_SERVER_PORT;
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

            v6McastNetIfs = null;	// default
            if (cmd.hasOption("6m")) {
            	String[] ifnames = cmd.getOptionValues("6m");
            	if (ifnames != null) {
            		v6McastNetIfs = getIPv6NetIfs(ifnames);
            		if ((v6McastNetIfs == null) || v6McastNetIfs.isEmpty()) {
            			return false;
            		}
            	}
            	else {
            		v6McastNetIfs = getAllIPv6NetIfs();
            	}
        		
            }
            
            v6UcastAddrs = getAllIPv6Addrs();	// default
            if (cmd.hasOption("6u")) {
            	String[] addrs = cmd.getOptionValues("6u");
            	if (addrs != null) {
            		if (addrs.length == 1) {
            			String addr = addrs[0];
            			if (addr.equalsIgnoreCase(NONE)) {
            				v6UcastAddrs = null;
            			}
            			if (addr.equals("*") || addr.equalsIgnoreCase(ALL)) {
            				v6UcastAddrs = getAllIPv6Addrs();
            			}
            		}
            		else {
    	        		v6UcastAddrs = getV6IpAddrs(addrs);
    	        		if ((v6UcastAddrs == null) || v6UcastAddrs.isEmpty()) {
    	        			return false;
    	        		}
            		}
            	}
            	else {
            		v6UcastAddrs = getAllIPv6Addrs();
            	}
            }
            
    		v6PortNumber = DhcpConstants.V6_SERVER_PORT;
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
            
            httpsAddr = DhcpConstants.ZEROADDR_V4;
            if (cmd.hasOption("ha")) {
            	String addr = cmd.getOptionValue("ha", ALL);
        		httpsAddr = getHttpsAddr(addr);
            }            
            if (cmd.hasOption("hp")) {
            	String p = cmd.getOptionValue("hp");
            	try {
            		httpsPortNumber = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		httpsPortNumber = JerseyRestServer.HTTPS_SERVER_PORT;
            		System.err.println("Invalid HTTPS port number: '" + p +
            							"' using default: " + httpsPortNumber +
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
        } 
        catch (SocketException se) {
			System.err.println("Network interface socket failure: " + se);
			return false;
		} 
        catch (UnknownHostException he) {
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
	public static List<NetworkInterface> getIPv6NetIfs(String[] ifnames) throws SocketException 
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
	public static List<NetworkInterface> getAllIPv6NetIfs() throws SocketException
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
	
	public static List<InetAddress> getV6IpAddrs(String[] addrs) throws UnknownHostException
	{
		List<InetAddress> ipAddrs = new ArrayList<InetAddress>();
		for (String addr : addrs) {
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
		return getFilteredIPv6Addrs(getAllIPv6Addrs());
	}
	
	public static List<InetAddress> getFilteredIPv6Addrs(List<InetAddress> v6Addrs) {
		
    	boolean ignoreLoopback = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LOOPBACK);
    	boolean ignoreLinkLocal = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LINKLOCAL);
    	
		List<InetAddress> filteredV6Addrs = new ArrayList<InetAddress>();
		if (v6Addrs != null) {
			for (InetAddress ip : v6Addrs) {
        		if (ignoreLoopback && ip.isLoopbackAddress()) {
        			log.debug("Skipping loopback address: " + ip);
        			continue;
        		}
        		if (ignoreLinkLocal && ip.isLinkLocalAddress()) {
        			log.debug("Skipping link local address: " + ip);
        			continue;
        		}
        		filteredV6Addrs.add(ip);
			}
		}
		return filteredV6Addrs;
	}
	
	public static NetworkInterface getIPv4NetIf(String ifname) throws SocketException 
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
	
	public static List<InetAddress> getV4IpAddrs(String[] addrs) throws UnknownHostException
	{
		List<InetAddress> ipAddrs = new ArrayList<InetAddress>();
		for (String addr : addrs) {
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
		return getFilteredIPv4Addrs(getAllIPv4Addrs());
	}
	
	public static List<InetAddress> getFilteredIPv4Addrs(List<InetAddress> v4Addrs) {
		
    	boolean ignoreLoopback = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LOOPBACK);
    	boolean ignoreLinkLocal = 
    			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_LINKLOCAL);
    	
		List<InetAddress> filteredV4Addrs = new ArrayList<InetAddress>();
		if (v4Addrs != null) {
			for (InetAddress ip : v4Addrs) {
        		if (ignoreLoopback && ip.isLoopbackAddress()) {
        			log.debug("Skipping loopback address: " + ip);
        			continue;
        		}
        		if (ignoreLinkLocal && ip.isLinkLocalAddress()) {
        			log.debug("Skipping link local address: " + ip);
        			continue;
        		}
        		filteredV4Addrs.add(ip);
			}
		}
		return filteredV4Addrs;		
	}
	
	public static InetAddress getHttpsAddr(String addr) throws UnknownHostException {
		if (addr.equalsIgnoreCase(NONE)) {
			return null;
		}
		if (addr.equalsIgnoreCase(ALL)) {
			return DhcpConstants.ZEROADDR_V4;
		}
		// NOTE: Jersey NettyHttpContainerProvider does not support 
		// see: https://github.com/eclipse-ee4j/jersey/issues/4045
		//return InetAddress.getByName(addr);
		return DhcpConstants.ZEROADDR_V4;
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
            System.out.println("Starting " + JAGORNET_DHCP_SERVER + ": " + new Date());
            System.out.println(Version.getVersion());
            server.start(args);
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
    		throws DhcpServerConfigException, JAXBException, IOException
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
//        return DhcpServerConfiguration.getInstance().getDhcpServerConfig();
    	return null;
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

	public List<NetworkInterface> getV6McastNetIfs() {
		return v6McastNetIfs;
	}

	public void setV6McastNetIfs(List<NetworkInterface> v6McastNetIfs) {
		this.v6McastNetIfs = v6McastNetIfs;
	}
	
	public void addV6McastNetIf(NetworkInterface v6McastNetIf) {
		if (v6McastNetIfs == null) {
			v6McastNetIfs = new ArrayList<NetworkInterface>();
		}
		v6McastNetIfs.add(v6McastNetIf);
	}

	public List<InetAddress> getV6UcastAddrs() {
		return v6UcastAddrs;
	}

	public void setV6UcastAddrs(List<InetAddress> v6UcastAddrs) {
		this.v6UcastAddrs = v6UcastAddrs;
	}
	
	public void addV6UcastAddr(InetAddress v6UcastAddr) {
		if (v6UcastAddrs == null) {
			v6UcastAddrs = new ArrayList<InetAddress>();
		}
		v6UcastAddrs.add(v6UcastAddr);
	}

	public int getV6PortNumber() {
		return v6PortNumber;
	}

	public void setV6PortNumber(int v6PortNumber) {
		this.v6PortNumber = v6PortNumber;
	}

	public NetworkInterface getV4BcastNetIf() {
		return v4BcastNetIf;
	}

	public void setV4BcastNetIf(NetworkInterface v4BcastNetIf) {
		this.v4BcastNetIf = v4BcastNetIf;
	}

	public List<InetAddress> getV4UcastAddrs() {
		return v4UcastAddrs;
	}

	public void setV4UcastAddrs(List<InetAddress> v4UcastAddrs) {
		this.v4UcastAddrs = v4UcastAddrs;
	}
	
	public void addV4UcastAddr(InetAddress v4UcastAddr) {
		if (v4UcastAddrs == null) {
			v4UcastAddrs = new ArrayList<InetAddress>();
		}
		v4UcastAddrs.add(v4UcastAddr);
	}

	public int getV4PortNumber() {
		return v4PortNumber;
	}

	public void setV4PortNumber(int v4PortNumber) {
		this.v4PortNumber = v4PortNumber;
	}

	public DhcpServerConfiguration getServerConfig() {
		return serverConfig;
	}

	public void setServerConfig(DhcpServerConfiguration serverConfig) {
		this.serverConfig = serverConfig;
	}

}
