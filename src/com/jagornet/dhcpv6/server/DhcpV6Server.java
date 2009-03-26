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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

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

import com.jagornet.dhcpv6.dto.DhcpV6ServerConfigDTO;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.multicast.MulticastDhcpServer;
import com.jagornet.dhcpv6.server.unicast.UnicastDhcpServer;
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
    	DhcpConstants.DHCPV6_HOME + "/conf/" + "dhcpv6server.xml" : "dhcpv6server.xml";

    /** The dozer bean mapper. */
    protected static MapperIF mapper = 
    	new DozerBeanMapper(Arrays.asList("com/jagornet/dhcpv6/dto/dozermap.xml"));
    
    /** The configuration filename. */
    protected String configFilename = DEFAULT_CONFIG_FILENAME;
    
    /** The server port number. */
    protected int portNumber = DhcpConstants.SERVER_PORT;
    
    /** The multicast network interfaces. */
    protected List<NetworkInterface> mcastNetIfs = null;
    
    /** The multicast server thread. */
    protected Thread mcastThread;
    
    /** The unicast server thread. */
    protected Thread ucastThread;

    /**
     * Instantiates the DHCPv6 server.
     * 
     * @param args the command line argument array
     * 
     * @throws Exception the exception
     */
    public DhcpV6Server(String[] args) throws Exception
    {
        options = new Options();
        parser = new BasicParser();
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
//            formatter.printHelp(cliName, options);
            PrintWriter stderr = new PrintWriter(System.err, true);	// auto-flush=true
            formatter.printHelp(stderr, 80, cliName, null, options, 2, 2, null);
            System.exit(0);
        }        
    }
    
    /**
     * Start the DHCPv6 server.  If multicast network interfaces have
     * been supplied on startup, then start a MulticastDhcpServer thread
     * on each of those interfaces.  Start one UnicastDhcpServer thread
     * which will listen on all IPv6 interfaces on the local host.
     * 
     * @throws Exception the exception
     */
    protected void start() throws Exception
    {        
        DhcpServerConfiguration.init(configFilename);

        registerLog4jInJmx();

        // for now, the mcast interfaces MUST be listed at
        // startup to get the mcast behavior at all... but
        // we COULD default to use all IPv6 interfaces 
        if (mcastNetIfs != null) {
	        MulticastDhcpServer mcastServer = 
	        	new MulticastDhcpServer(mcastNetIfs, portNumber);
	        mcastThread = new Thread(mcastServer, "mcast.server");
	        mcastThread.start();
        }
 
        UnicastDhcpServer ucastServer = 
        		new UnicastDhcpServer(configFilename, portNumber);
        ucastServer.start();
    }
    
	/**
	 * Setup command line options.
	 */
    @SuppressWarnings("static-access")
	private void setupOptions()
    {
        Option configFileOption = new Option("c", "configfile", true,
                                             "Configuration File [$DHCPV6_HOME/conf/dhcpv6server.xml]");
        options.addOption(configFileOption);
        
        Option portOption = new Option("p", "port", true,
        							  "Port Number [547]");
        options.addOption(portOption);

        Option mcastOption =
        	OptionBuilder.withLongOpt("mcast")
        	.withArgName("args")
        	.withDescription("Multicast interfaces [none]" +
        			" - list interface names separated by spaces," +
        			" or * for all IPv6 configured interfaces")
        	.hasArgs()
        	.create("m");
        				 
        options.addOption(mcastOption);
        
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
            							"' using default: " + portNumber);
            	}
            }
            if (cmd.hasOption("m")) {
            	String[] ifnames = cmd.getOptionValues("m");
            	if ((ifnames == null) || (ifnames.length < 1)) {
            		System.err.println("No multicast interfaces specified, -m option ignored");
            	}
            	else {
            		mcastNetIfs = getMcastNetIfs(ifnames);
            		if ((mcastNetIfs == null) || mcastNetIfs.isEmpty()) {
            			return false;
            		}
            	}
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
        } catch (SocketException se) {
			System.err.println("Network interface socket failure: " + se);
			return false;
		}
        return true;
    }

	/**
	 * Gets the multicast network interfaces for the supplied interface names.
	 * 
	 * @param ifnames the interface names to locate NetworkInterfaces by
	 * 
	 * @return the list of NetworkInterfaces that are up, support multicast,
	 * and have at least one IPv6 address configured
	 * 
	 * @throws SocketException the socket exception
	 */
	private List<NetworkInterface> getMcastNetIfs(String[] ifnames) throws SocketException 
	{
		List<NetworkInterface> netIfs = null;
		for (String ifname : ifnames) {
			if (ifname.equals("*")) {
				return getAllMcastV6NetIfs();
			}
			netIfs = new ArrayList<NetworkInterface>();
			NetworkInterface netIf = NetworkInterface.getByName(ifname);
			if (netIf != null) {
				if (netIf.isUp()) {
					if (netIf.supportsMulticast()) {
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
	 * Gets all multicast IPv6 network interfaces on the local host.
	 * 
	 * @return the list NetworkInterfaces
	 */
	private List<NetworkInterface> getAllMcastV6NetIfs()
	{
		List<NetworkInterface> netIfs = null;
		try {
	        Enumeration<NetworkInterface> localInterfaces =
	        	NetworkInterface.getNetworkInterfaces();
	        if (localInterfaces != null) {
	        	netIfs = new ArrayList<NetworkInterface>();
		        while (localInterfaces.hasMoreElements()) {
		        	NetworkInterface netIf = localInterfaces.nextElement();
		        	if (!netIf.isLoopback() && netIf.supportsMulticast()) {
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
		}
		catch (IOException ex) {
			log.error("Failed to get multicast IPv6 network interfaces: " + ex);
		}
        return netIfs;
	}
	
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        try {
            System.out.println("Starting DhcpV6Server");
            DhcpV6Server server = new DhcpV6Server(args);
            server.start();
        }
        catch (Exception ex) {
            System.err.println("DhcpV6Server ABORT!");
            ex.printStackTrace();
            System.exit(1);
        }
	}
    
    /**
     * Static method used by the GUI to load the config file.
     * 
     * @param filename the configuration filename
     * 
     * @return DhcpV6ServerConfigDTO opaqueData transfer object
     * 
     * @throws Exception the exception
     */
    public static DhcpV6ServerConfigDTO loadConfigDTO(String filename) throws Exception
    {
        DhcpV6ServerConfig config = DhcpV6Server.loadConfig(filename);
        if (config != null) {
            return convertToDTO(config);
        }
        return null;
    }
    
    /**
     * Load server configuration.
     * 
     * @param filename the configuration filename
     * 
     * @return DhcpV6ServerConfig domain object
     * 
     * @throws Exception the exception
     */
    public static DhcpV6ServerConfig loadConfig(String filename) throws Exception
    {
        return DhcpServerConfiguration.loadConfig(filename);
    }
    
    /**
     * Static method used by the GUI to save the configuration file.
     * 
     * @param dto DhcpV6ServerConfigDTO opaqueData transfer object
     * @param filename the configuration filename
     * 
     * @throws Exception the exception
     */
    public static void saveConfigDTO(DhcpV6ServerConfigDTO dto, String filename) throws Exception
    {
        DhcpV6ServerConfig config = convertFromDTO(dto);
        DhcpV6Server.saveConfig(config, filename);
    }
    
    /**
     * Save server configuration.
     * 
     * @param config DhcpV6ServerConfig domain object
     * @param filename the configuration filename
     * 
     * @throws Exception the exception
     */
    public static void saveConfig(DhcpV6ServerConfig config, String filename) throws Exception
    {
        DhcpServerConfiguration.saveConfig(config, filename);
    }

    /**
     * Static method used by the GUI to get the server configuration.
     * 
     * @return the DhcpV6ServerConfig domain object
     */
    public static DhcpV6ServerConfig getDhcpServerConfig()
    {
        return DhcpServerConfiguration.getConfig();
    }
    

    /**
     * Convert the XML-based DhcpV6ServerConfig domain object to
     * its equivalent Data Transfer Object (DTO) version.
     * This implementation uses Dozer (dozer.sourceforge.net)
     * to perform the conversion via reflection.
     * 
     * @param config    DhcpV6ServerConfig domain object to convert
     * 
     * @return          DhcpV6ServerConfigDTO object populated
     * with the contents of the domain object
     * 
     * @throws Exception the exception
     */
    public static DhcpV6ServerConfigDTO convertToDTO(DhcpV6ServerConfig config) throws Exception 
    {
        DhcpV6ServerConfigDTO dto = null;
        try {
            dto = (DhcpV6ServerConfigDTO) mapper.map(config, DhcpV6ServerConfigDTO.class);
        }
        catch (Exception ex) {
            log.error("Failure converting domain object to DTO:" + ex);
            throw ex;
        }
        return dto;
    }

    /**
     * Convert the DhcpV6ServerConfigDTO opaqueData-tranfer object to
     * its equivalent domain version.
     * This implementation uses Dozer (dozer.sourceforge.net)
     * to perform the conversion via reflection.
     * 
     * @param dto the dto
     * 
     * @return          DhcpV6ServerConfig object populated
     * with the contents of the opaqueData-transfer object
     * 
     * @throws Exception the exception
     */
    public static DhcpV6ServerConfig convertFromDTO(DhcpV6ServerConfigDTO dto) throws Exception 
    {
        DhcpV6ServerConfig config = null;
        try {
            config = (DhcpV6ServerConfig) mapper.map(dto, DhcpV6ServerConfig.class);
        }
        catch (Exception ex) {
            log.error("Failure converting DTO to domain object:" + ex);
            throw ex;
        }
        return config;
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
