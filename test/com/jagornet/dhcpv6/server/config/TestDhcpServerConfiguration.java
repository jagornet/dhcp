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
package com.jagornet.dhcpv6.server.config;

import java.net.InetAddress;
import java.util.SortedMap;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.Policy;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

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
		DhcpV6ServerConfig config = DhcpV6ServerConfigDocument.DhcpV6ServerConfig.Factory.newInstance();
		
		ServerIdOption serverId = ServerIdOption.Factory.newInstance();
		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
		serverId.setOpaqueData(duid);
		config.setServerIdOption(serverId);
		
		Policy policy = Policy.Factory.newInstance();
		policy.setName("sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = PoliciesType.Factory.newInstance();
		policies.setPolicyArray(new Policy[] { policy });
		config.setPolicies(policies);
		
		DhcpServerConfiguration.saveConfig(config, "test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		
		config = DhcpServerConfiguration.loadConfig("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		assertNotNull(config);
		assertNotNull(config.getServerIdOption());
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
        
        DhcpV6ServerConfig config = serverConfig.getDhcpV6ServerConfig();
        assertNotNull(config);
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
        assertNotNull(config.getServerIdOption());
		assertEquals(DhcpConstants.OPTION_SERVERID, config.getServerIdOption().getCode());
        assertEquals("abcdef0123456789", Util.toHexString(config.getServerIdOption().getOpaqueData().getHexValue()));
        assertNotNull(config.getMsgConfigOptions().getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getMsgConfigOptions().getDnsServersOption().getCode());
        assertEquals(3, config.getMsgConfigOptions().getDnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", config.getMsgConfigOptions().getDnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", config.getMsgConfigOptions().getDnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", config.getMsgConfigOptions().getDnsServersOption().getIpAddressList().get(2));
        assertNotNull(config.getMsgConfigOptions().getDomainSearchListOption());
        assertEquals(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST, config.getMsgConfigOptions().getDomainSearchListOption().getCode());
        assertEquals(3, config.getMsgConfigOptions().getDomainSearchListOption().getDomainNameList().size());
        assertEquals("foo.com.", config.getMsgConfigOptions().getDomainSearchListOption().getDomainNameList().get(0));
        assertEquals("bar.com.", config.getMsgConfigOptions().getDomainSearchListOption().getDomainNameList().get(1));
        assertEquals("yuk.com.", config.getMsgConfigOptions().getDomainSearchListOption().getDomainNameList().get(2));
        
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
