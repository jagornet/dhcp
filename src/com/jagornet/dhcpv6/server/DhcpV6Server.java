package com.jagornet.dhcpv6.server;

import java.io.PrintWriter;
import java.util.Arrays;

import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;
import com.jagornet.dhcpv6.dto.DhcpV6ServerConfigDTO;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.mina.MinaDhcpServer;
import com.jagornet.dhcpv6.util.DhcpConstants;

public class DhcpV6Server
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6Server.class);

    protected Options options;
    protected CommandLineParser parser;
    protected HelpFormatter formatter;

    public static String DEFAULT_CONFIG_FILENAME = DhcpConstants.DHCPV6_HOME != null ? 
    	DhcpConstants.DHCPV6_HOME + "/conf/" + "dhcpv6server.xml" : "dhcpv6server.xml";

    protected static MapperIF mapper = 
    	new DozerBeanMapper(Arrays.asList("com/jagornet/dhcpv6/dto/dozermap.xml"));
    
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
    }
    
    protected void start() throws Exception
    {        
        if (supportMulticast) {
        	MulticastDhcpServer multicastServer = 
        		new MulticastDhcpServer(configFilename, portNumber);
        	multicastServer.start();
        }
        else {
        	// my original channel-based server, which is 
        	// now superseded by the MinaDhcpSever
        	// DhcpSever server = new DhcpServer(configFilename, portNumber);
        	MinaDhcpServer minaServer = 
        		new MinaDhcpServer(configFilename, portNumber);
        	minaServer.start();
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
            server.start();
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
     * @return DhcpV6ServerConfigDTO
     * @throws Exception
     */
    public static DhcpV6ServerConfigDTO loadConfigDTO(String filename) throws Exception
    {
        DhcpV6ServerConfig config = DhcpV6Server.loadConfig(filename);
        if (config != null) {
            return convertToDTO(config);
        }
        return null;
    }
    
    public static DhcpV6ServerConfig loadConfig(String filename) throws Exception
    {
        return DhcpServerConfiguration.loadConfig(filename);
    }
    
    /**
     * Static method used by the GUI to save the config file
     * @param dto DhcpV6ServerConfigDTO to save
     * @param filename
     * @throws Exception
     */
    public static void saveConfigDTO(DhcpV6ServerConfigDTO dto, String filename) throws Exception
    {
        DhcpV6ServerConfig config = convertFromDTO(dto);
        DhcpV6Server.saveConfig(config, filename);
    }
    
    public static void saveConfig(DhcpV6ServerConfig config, String filename) throws Exception
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
    

    /**
     * Convert the XML-based DhcpV6ServerConfig domain object to
     * its equivalent Data Transfer Object (DTO) version.
     * This implementation uses Dozer (dozer.sourceforge.net)
     * to perform the conversion via reflection.
     * 
     * @param config    DhcpV6ServerConfig domain object to convert
     * @return          DhcpV6ServerConfigDTO object populated
     *                  with the contents of the domain object
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
     * Convert the DhcpV6ServerConfigDTO data-tranfer object to
     * its equivalent domain version.
     * This implementation uses Dozer (dozer.sourceforge.net)
     * to perform the conversion via reflection.
     * 
     * @param config    DhcpV6ServerConfigDTO data-transfer object to convert
     * @return          DhcpV6ServerConfig object populated
     *                  with the contents of the data-transfer object
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
}
