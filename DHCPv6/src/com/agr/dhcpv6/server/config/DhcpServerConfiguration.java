package com.agr.dhcpv6.server.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig;

public class DhcpServerConfiguration
{
    private static Log log = LogFactory.getLog(DhcpServerConfiguration.class);

    private static volatile DhcpV6ServerConfig CONFIG;

    // Private constructor suppresses generation of a (public) default constructor
    private DhcpServerConfiguration() {}

    public static void init(String filename) 
        throws FileNotFoundException, JAXBException
    {
        if (CONFIG == null)
            synchronized(DhcpServerConfiguration.class) {
                if (CONFIG == null)
                    CONFIG = loadConfig(filename);
            }
    }
    
    public static DhcpV6ServerConfig getConfig()
    {
        if (CONFIG == null) {
            throw new IllegalStateException("DhcpServerConfiguration not initialized");
        }
        return CONFIG;
    }
        
    public static DhcpV6ServerConfig loadConfig(String filename) 
        throws FileNotFoundException, JAXBException
    {
        log.info("Loading server configuration: " + filename);
        FileInputStream fis = new FileInputStream(filename);
        // the newInstance method accepts the package name
        // where the JAXB classes live
        log.debug("Creating JAXB context...");
        //JAXBContext jc = JAXBContext.newInstance("ipv6test.xml");
//        JAXBContext jc = JAXBContext.newInstance("com.agr.dhcpv6.xml");
        JAXBContext jc = JAXBContext.newInstance("com.agr.dhcpv6.server.config.xml");
        Unmarshaller u = jc.createUnmarshaller();
        log.debug("Unmarshalling XML...");
        return (DhcpV6ServerConfig)u.unmarshal(fis);
    }
    
    public static void saveConfig(DhcpV6ServerConfig config, String filename) 
        throws FileNotFoundException, JAXBException
    {
        log.info("Saving server configuration: " + filename);
        FileOutputStream fos = new FileOutputStream(filename);
        // the newInstance method accepts the package name
        // where the JAXB classes live
        log.debug("Creating JAXB context...");
        //JAXBContext jc = JAXBContext.newInstance("ipv6test.xml");
//        JAXBContext jc = JAXBContext.newInstance("com.agr.dhcpv6.xml");
        JAXBContext jc = JAXBContext.newInstance("com.agr.dhcpv6.server.config.xml");
        Marshaller m = jc.createMarshaller();
        log.debug("Marshalling to XML...");
        m.marshal(config, fos);
    }
}
