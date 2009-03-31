package com.jagornet.dhcpv6.server.config;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.Policy;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

public class TestDhcpServerConfiguration extends TestCase
{
	public void testSaveAndLoadConfig() throws Exception
	{
		DhcpV6ServerConfig config = DhcpV6ServerConfigDocument.DhcpV6ServerConfig.Factory.newInstance();
		Policy policy = Policy.Factory.newInstance();
		policy.setName("sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = PoliciesType.Factory.newInstance();
		policies.setPolicyArray(new Policy[] { policy });
		config.setPolicies(policies);
		DhcpServerConfiguration.saveConfig(config, "test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		config = DhcpServerConfiguration.loadConfig("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		assertNotNull(config);
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
	}
	
    public void testLinkMap() throws Exception
    {
        DhcpServerConfiguration.init("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigLinkTest1.xml");
        
        DhcpV6ServerConfig config = DhcpServerConfiguration.getConfig();
        assertNotNull(config);
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
        assertNotNull(config.getServerIdOption());
		assertEquals(DhcpConstants.OPTION_SERVERID, config.getServerIdOption().getCode());
        assertEquals("abcdef0123456789", Util.toHexString(config.getServerIdOption().getOpaqueData().getHexValue()));
        assertNotNull(config.getOptions().getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getOptions().getDnsServersOption().getCode());
        assertEquals(3, config.getOptions().getDnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", config.getOptions().getDnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", config.getOptions().getDnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", config.getOptions().getDnsServersOption().getIpAddressList().get(2));
        assertNotNull(config.getOptions().getDomainSearchListOption());
        assertEquals(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST, config.getOptions().getDomainSearchListOption().getCode());
        assertEquals(3, config.getOptions().getDomainSearchListOption().getDomainNameList().size());
        assertEquals("foo.com.", config.getOptions().getDomainSearchListOption().getDomainNameList().get(0));
        assertEquals("bar.com.", config.getOptions().getDomainSearchListOption().getDomainNameList().get(1));
        assertEquals("yuk.com.", config.getOptions().getDomainSearchListOption().getDomainNameList().get(2));
        
        TreeMap<Subnet, Link> linkMap = DhcpServerConfiguration.getLinkMap();
        assertNotNull(linkMap);
        assertEquals(5, linkMap.size());
        Subnet searchAddr = new Subnet("2001:DB8:3:1:DEB:DEB:DEB:1", 128);
        // there are two subnets greater than our search address
        SortedMap<Subnet, Link> subMap = linkMap.tailMap(searchAddr);
        assertNotNull(subMap);
        assertEquals(2, subMap.size());

        // there are three subnets less than our search address
        subMap = linkMap.headMap(searchAddr);
        assertNotNull(subMap);
        assertEquals(3, subMap.size());
        // the last subnet from the head list is the one we want
        assertEquals(InetAddress.getByName("2001:DB8:3::"), 
                     subMap.lastKey().getSubnetAddress());
    }
}
