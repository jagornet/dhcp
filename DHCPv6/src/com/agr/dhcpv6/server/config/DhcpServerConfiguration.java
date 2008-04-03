package com.agr.dhcpv6.server.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig.Links;
import com.agr.dhcpv6.util.Subnet;

public class DhcpServerConfiguration
{
    private static Log log = LogFactory.getLog(DhcpServerConfiguration.class);

    private static volatile DhcpV6ServerConfig CONFIG;
    private static TreeMap<Subnet, Links> linkMap;
    
    // Private constructor suppresses generation of a (public) default constructor
    private DhcpServerConfiguration() {}

    public static void init(String filename) 
        throws Exception
    {
        if (CONFIG == null)
            synchronized(DhcpServerConfiguration.class) {
                if (CONFIG == null) {
                    CONFIG = loadConfig(filename);
                    initLinkMap();
                }
            }
    }
    
    public static void initLinkMap() throws Exception
    {
        linkMap = 
            new TreeMap<Subnet, Links>(new Comparator<Subnet>() {

                public int compare(Subnet s1, Subnet s2)
                {
                    BigInteger bi1 = new BigInteger(s1.getSubnetAddress().getAddress());
                    BigInteger bi2 = new BigInteger(s2.getSubnetAddress().getAddress());
                    if (bi1.equals(bi2)) {
                        // if we have two subnets with the same starting address
                        // then the "smaller" subnet is the one with the larger
                        // prefix length, which logically places the more specific
                        // subnet _before_ the less specific subnet in the map
                        // this allows us to work from "inside-out"?
                        if (s1.getPrefixLength() > s2.getPrefixLength())
                            return -1;
                        else if (s1.getPrefixLength() < s2.getPrefixLength())
                            return 1;
                        else
                            return 0;
                    }
                    else {
                        // subnet addresses are different, so return
                        // the standard compare for the address
                        return bi1.compareTo(bi2);
                    }
                }
                
            });
        
        try {
            List<Links> links = CONFIG.getLinks();
            if ((links != null) && !links.isEmpty()) {
                for (Links link : links) {
                    String addr = link.getAddress();
                    if (addr != null) {
                        String[] subnet = addr.split("/");
                        if ((subnet != null) && (subnet.length == 2)) {
                            Subnet s = new Subnet(subnet[0], subnet[1]);
                            linkMap.put(s, link);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            log.error("Failed to build linkMap: " + ex);
            throw ex;
        }
    }
    
    public static DhcpV6ServerConfig getConfig()
    {
        if (CONFIG == null) {
            throw new IllegalStateException("DhcpServerConfiguration not initialized");
        }
        return CONFIG;
    }
    
    public static TreeMap<Subnet, Links> getLinkMap()
    {
        if (CONFIG == null) {
            throw new IllegalStateException("DhcpServerConfiguration not initialized");
        }
        return linkMap;
    }

    public static Links findLinkForAddress(InetAddress inetAddr)
    {
        Links link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
            Subnet s = new Subnet(inetAddr, 128);
            SortedMap<Subnet, Links> subMap = linkMap.headMap(s);
            if ((subMap != null) && !subMap.isEmpty()) {
                s = subMap.lastKey();
                if (s.contains(inetAddr)) {
                    link = subMap.get(s);
                }
            }
        }
        return link;
    }
    
    public static DhcpV6ServerConfig loadConfig(String filename) 
        throws FileNotFoundException, JAXBException
    {
        log.info("Loading server configuration: " + filename);
        FileInputStream fis = new FileInputStream(filename);
        // the newInstance method accepts the package name
        // where the JAXB classes live
        log.debug("Creating JAXB context...");
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
        JAXBContext jc = JAXBContext.newInstance("com.agr.dhcpv6.server.config.xml");
        Marshaller m = jc.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        log.debug("Marshalling to XML...");
        m.marshal(config, fos);
    }
}
