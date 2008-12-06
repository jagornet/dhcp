package com.agr.dhcpv6.server;

import java.io.PrintWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.agr.dhcpv6.server.config.DhcpServerConfiguration;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig;
import com.agr.dhcpv6.server.mina.MinaDhcpServer;
import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpV6Server
{
    protected Options options;
    protected CommandLineParser parser;
    protected HelpFormatter formatter;

    public static String DEFAULT_CONFIG_FILENAME = DhcpConstants.DHCPV6_HOME != null ? 
    	DhcpConstants.DHCPV6_HOME + "/conf/" + "dhcpv6server.xml" : "dhcpv6server.xml";
    
    protected String configFilename = DEFAULT_CONFIG_FILENAME;
    
    protected int portNumber = DhcpConstants.SERVER_PORT;
    
    protected boolean supportMulticast = false;


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
        
        if (supportMulticast) {
        	MulticastDhcpServer multicastServer = 
        		new MulticastDhcpServer(configFilename, portNumber);
        }
        else {
        	// my original channel-based server, which is 
        	// now superseded by the MinaDhcpSever
        	// DhcpSever server = new DhcpServer(configFilename, portNumber);
        	MinaDhcpServer minaServer = 
        		new MinaDhcpServer(configFilename, portNumber);
        }
    }
    
	private void setupOptions()
    {
        Option configFileOption = new Option("c", "configfile", true,
                                             "Configuration File [$DHCPV6_HOME/conf/dhcpv6server.xml]");
        options.addOption(configFileOption);
        
        Option portOption = new Option("p", "port", true,
        							  "Port Number [547]");
        options.addOption(portOption);
        
        Option multicastOption = new Option("m", "multicast", false,
        									"Multicast [false]");
        options.addOption(multicastOption);
        
        Option helpOption = new Option("?", "help", false, "Show this help page.");
        
        options.addOption(helpOption);
    }

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
            	supportMulticast = true;
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
        }
        return true;
    }

    public static void main(String[] args)
    {
        try {
            System.out.println("Starting DhcpV6Server");
            DhcpV6Server server = new DhcpV6Server(args);
        }
        catch (Exception ex) {
            System.err.println("DhcpV6Server ABORT!");
            ex.printStackTrace();
            System.exit(1);
        }
	}
    
    /**
     * Static method used by the GUI to load the config file
     * @param filename
     * @return
     * @throws Exception
     */
    public static DhcpV6ServerConfig loadConfig(String filename)
    // TODO: change to throws IOException, JAXBException...
    // right now, just throw Exception so as to allow GUI to
    // use this method without requiring JAXB libs
            throws Exception
    {
        return DhcpServerConfiguration.loadConfig(filename);
    }
    
    /**
     * Static method used by the GUI to save the config file
     * @param config
     * @param filename
     * @throws Exception
     */
    public static void saveConfig(DhcpV6ServerConfig config, String filename)
        throws Exception    // see comment for loadConfig
    {
        DhcpServerConfiguration.saveConfig(config, filename);
    }

    /**
     * Static method used by the GUI to get the config
     * @return
     */
    public static DhcpV6ServerConfig getDhcpServerConfig()
    {
        return DhcpServerConfiguration.getConfig();
    }
}
