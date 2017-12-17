/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpServerConfiguration.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.config;

import java.net.InetAddress;
import java.util.SortedMap;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.Policy;
import com.jagornet.dhcp.server.config.xml.V6ConfigOptionsType;
import com.jagornet.dhcp.server.config.xml.V6ServerIdOption;

import junit.framework.TestCase;

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
		DhcpServerConfig config = new DhcpServerConfig();
		
		V6ServerIdOption serverId = new V6ServerIdOption();
		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
		serverId.setOpaqueData(duid);
		config.setV6ServerIdOption(serverId);
		
		Policy policy = new Policy();
		policy.setName("sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = new PoliciesType();
		policies.getPolicies().add(policy);
		config.setPolicies(policies);
		
		DhcpServerConfiguration.saveConfig(config, "src/test/java/com/jagornet/dhcp/server/config/dhcpServerConfigTestSave.xml");
		
		config = DhcpServerConfiguration.loadConfig("src/test/java/com/jagornet/dhcp/server/config/dhcpServerConfigTestSave.xml");
		assertNotNull(config);
		assertNotNull(config.getV6ServerIdOption());
		assertNotNull(config.getPolicies().getPolicies());
		assertEquals(1, config.getPolicies().getPolicies().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicies().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicies().get(0).getValue());
	}
	
    /**
     * Test link map.
     * 
     * @throws Exception the exception
     */
    public void testLinkMap() throws Exception
    {
    	String configFilename = "src/test/java/com/jagornet/dhcp/server/config/dhcpServerConfigLinkTest1.xml";
        DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();
        serverConfig.init(configFilename);
        assertNotNull(serverConfig);
		assertNotNull(serverConfig.getGlobalPolicies());
		assertNotNull(serverConfig.getGlobalPolicies().getPolicies());
		assertEquals(1, serverConfig.getGlobalPolicies().getPolicies().size());
		assertEquals("sendRequestedOptionsOnly", serverConfig.getGlobalPolicies().getPolicies().get(0).getName());
		assertEquals("true", serverConfig.getGlobalPolicies().getPolicies().get(0).getValue());
        assertNotNull(serverConfig.getDhcpV6ServerIdOption());
		assertEquals(DhcpConstants.V6OPTION_SERVERID, serverConfig.getDhcpV6ServerIdOption().getCode());
        assertEquals("abcdef0123456789", Util.toHexString(serverConfig.getDhcpV6ServerIdOption().getOpaqueData().getHex()));
        assertNotNull(serverConfig.getGlobalV6MsgConfigOptions().getV6ConfigOptions().getV6DnsServersOption());
        assertEquals(DhcpConstants.V6OPTION_DNS_SERVERS, serverConfig.getGlobalV6MsgConfigOptions().getV6ConfigOptions().getV6DnsServersOption().getCode());
        DhcpV6ConfigOptions globalV6MsgConfigOptions = serverConfig.getGlobalV6MsgConfigOptions();
        assertNotNull(globalV6MsgConfigOptions);
        V6ConfigOptionsType v6MsgConfigOptions = globalV6MsgConfigOptions.getV6ConfigOptions();
        assertEquals(3, v6MsgConfigOptions.getV6DnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", v6MsgConfigOptions.getV6DnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", v6MsgConfigOptions.getV6DnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", v6MsgConfigOptions.getV6DnsServersOption().getIpAddressList().get(2));
        assertNotNull(v6MsgConfigOptions.getV6DomainSearchListOption());
        assertEquals(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST, v6MsgConfigOptions.getV6DomainSearchListOption().getCode());
        assertEquals(3, v6MsgConfigOptions.getV6DomainSearchListOption().getDomainNameList().size());
        assertEquals("foo.com.", v6MsgConfigOptions.getV6DomainSearchListOption().getDomainNameList().get(0));
        assertEquals("bar.com.", v6MsgConfigOptions.getV6DomainSearchListOption().getDomainNameList().get(1));
        assertEquals("yuk.com.", v6MsgConfigOptions.getV6DomainSearchListOption().getDomainNameList().get(2));
        
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
