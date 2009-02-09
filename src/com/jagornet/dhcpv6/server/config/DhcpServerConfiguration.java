package com.jagornet.dhcpv6.server.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig.Links;
import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.server.DhcpV6Server;
import com.jagornet.dhcpv6.util.Subnet;

public class DhcpServerConfiguration
{
	private static Logger log = LoggerFactory.getLogger(DhcpServerConfiguration.class);

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
                    initServerId();
                    initLinkMap();
                }
            }
    }
    
    public static void initServerId() throws Exception
    {
    	ServerIdOption serverId = CONFIG.getServerIdOption();
    	if ( (serverId == null) || 
    		 ( ((serverId.getAsciiValue() == null) || (serverId.getAsciiValue().length() <= 0)) &&
    		   ((serverId.getHexValue() == null) || (serverId.getHexValue().length <= 0)) ) ) {
    		OpaqueData opaque = OpaqueDataUtil.generateDUID_LLT();
    		if (opaque == null) {
    			throw new IllegalStateException("Failed to create ServerID");
    		}
    		if (serverId == null) {
    			serverId = ServerIdOption.Factory.newInstance();
    		}
    		serverId.setHexValue(opaque.getHexValue());
    		CONFIG.setServerIdOption(serverId);
    		saveConfig(CONFIG, DhcpV6Server.DEFAULT_CONFIG_FILENAME);
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
        	List<Links> links = Arrays.asList(CONFIG.getLinksArray());
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
    
    public static DhcpV6ServerConfig loadConfig(String filename) throws Exception
    {
    	DhcpV6ServerConfig config = null;
    	FileInputStream fis = null;
    	try {
	        log.info("Loading server configuration: " + filename);
	        fis = new FileInputStream(filename);
	        long start = System.currentTimeMillis();
	        config = DhcpV6ServerConfigDocument.Factory.parse(fis).getDhcpV6ServerConfig();
	        log.info("Server configuration loaded in " + (System.currentTimeMillis()-start) + "ms");
    	}
    	finally {
    		if (fis != null) {
    			try { fis.close(); } catch (IOException ex) { }
    		}
    	}
    	return config;
    }
    
    public static void saveConfig(DhcpV6ServerConfig config, String filename) throws Exception
    {
    	FileOutputStream fos = null;
    	try {
	        log.info("Saving server configuration: " + filename);
	        fos = new FileOutputStream(filename);
	        long start = System.currentTimeMillis();			
	        DhcpV6ServerConfigDocument doc = DhcpV6ServerConfigDocument.Factory.newInstance();
	        doc.setDhcpV6ServerConfig(config);
	        doc.save(fos, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4));
	        log.info("Server configuration saved in " + (System.currentTimeMillis()-start) + "ms");
    	}
    	finally {
    		if (fos != null) {
    			try { fos.close(); } catch (IOException ex) { }
    		}
    	}
    }
}
