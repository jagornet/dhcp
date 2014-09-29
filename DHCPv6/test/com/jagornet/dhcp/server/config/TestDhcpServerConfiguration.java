/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpServerConfiguration.java is part of DHCPv6.
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
package com.jagornet.dhcp.server.config;

import java.net.InetAddress;
import java.util.SortedMap;

import junit.framework.TestCase;

import com.jagornet.dhcp.option.OpaqueDataUtil;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.DhcpServerConfigDocument;
import com.jagornet.dhcp.xml.DhcpServerConfigDocument.DhcpServerConfig;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.PoliciesType;
import com.jagornet.dhcp.xml.Policy;
import com.jagornet.dhcp.xml.V6ServerIdOption;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpServerConfiguration.
 */
public class TestDhcpServerConfiguration extends TestCase
{
	
	/**
	 * Test save and load config.
	 * 
	 * @throws Exception the exception
	 */
	public void testSaveAndLoadConfig() throws Exception
	{
		DhcpServerConfig config = DhcpServerConfigDocument.DhcpServerConfig.Factory.newInstance();
		
		V6ServerIdOption serverId = V6ServerIdOption.Factory.newInstance();
		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
		serverId.setOpaqueData(duid);
		config.setV6ServerIdOption(serverId);
		
		Policy policy = Policy.Factory.newInstance();
		policy.setName("sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = PoliciesType.Factory.newInstance();
		policies.setPolicyArray(new Policy[] { policy });
		config.setPolicies(policies);
		
		DhcpServerConfiguration.saveConfig(config, "test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		
		config = DhcpServerConfiguration.loadConfig("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		assertNotNull(config);
		assertNotNull(config.getV6ServerIdOption());
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
	}
	
    /**
     * Test link map.
     * 
     * @throws Exception the exception
     */
    public void testLinkMap() throws Exception
    {
    	String configFilename = "test/com/jagornet/dhcpv6/server/config/dhcpServerConfigLinkTest1.xml";
    	DhcpServerConfiguration.configFilename = configFilename;
        DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();
        
        DhcpServerConfig config = serverConfig.getDhcpServerConfig();
        assertNotNull(config);
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
        assertNotNull(config.getV6ServerIdOption());
		assertEquals(DhcpConstants.OPTION_SERVERID, config.getV6ServerIdOption().getCode());
        assertEquals("abcdef0123456789", Util.toHexString(config.getV6ServerIdOption().getOpaqueData().getHexValue()));
        assertNotNull(config.getV6MsgConfigOptions().getV6DnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getV6MsgConfigOptions().getV6DnsServersOption().getCode());
        assertEquals(3, config.getV6MsgConfigOptions().getV6DnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", config.getV6MsgConfigOptions().getV6DnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", config.getV6MsgConfigOptions().getV6DnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", config.getV6MsgConfigOptions().getV6DnsServersOption().getIpAddressList().get(2));
        assertNotNull(config.getV6MsgConfigOptions().getV6DomainSearchListOption());
        assertEquals(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST, config.getV6MsgConfigOptions().getV6DomainSearchListOption().getCode());
        assertEquals(3, config.getV6MsgConfigOptions().getV6DomainSearchListOption().getDomainNameList().size());
        assertEquals("foo.com.", config.getV6MsgConfigOptions().getV6DomainSearchListOption().getDomainNameList().get(0));
        assertEquals("bar.com.", config.getV6MsgConfigOptions().getV6DomainSearchListOption().getDomainNameList().get(1));
        assertEquals("yuk.com.", config.getV6MsgConfigOptions().getV6DomainSearchListOption().getDomainNameList().get(2));
        
        SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
        assertNotNull(linkMap);
        assertEquals(5, linkMap.size());
        Subnet searchAddr = new Subnet("2001:DB8:3:1:DEB:DEB:DEB:1", 128);
        // there are two subnets greater than our search address
        SortedMap<Subnet, DhcpLink> subMap = linkMap.tailMap(searchAddr);
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
