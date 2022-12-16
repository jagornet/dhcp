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
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.generic.GenericIpAddressListOption;
import com.jagornet.dhcp.core.option.generic.GenericIpAddressOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4SubnetMaskOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6DnsServersOption;
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

/**
 * The Class TestDhcpServerConfiguration.
 */
public class TestDhcpServerConfiguration extends TestCase
{
	
	public void testSaveAndLoadXmlConfig() throws Exception
	{
		DhcpServerConfig config = new DhcpServerConfig();
		
		V6ServerIdOption serverId = new V6ServerIdOption();
		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
		serverId.setOpaqueData(duid);
		config.setV6ServerIdOption(serverId);
		
		Policy policy = new Policy();
		policy.setName("dhcp.sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = new PoliciesType();
		policies.getPolicyList().add(policy);
		config.setPolicies(policies);
		
		DhcpServerConfiguration.saveConfig(config, "file:src/test/resources/dhcpserver-test-config-save.xml");
		
		config = DhcpServerConfiguration.loadConfig("file:src/test/resources/dhcpserver-test-config-save.xml");
		assertNotNull(config);
		assertNotNull(config.getV6ServerIdOption());
		assertTrue(Arrays.equals(serverId.getOpaqueData().getHexValue(), 
				config.getV6ServerIdOption().getOpaqueData().getHexValue()));
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("dhcp.sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
	}
	
	public void testSaveAndLoadJsonConfig() throws Exception
	{
		DhcpServerConfig config = new DhcpServerConfig();
		
		V6ServerIdOption serverId = new V6ServerIdOption();
		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
		serverId.setOpaqueData(duid);
		config.setV6ServerIdOption(serverId);
		
		Policy policy = new Policy();
		policy.setName("dhcp.sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = new PoliciesType();
		policies.getPolicyList().add(policy);
		config.setPolicies(policies);
		
		DhcpServerConfiguration.saveConfig(config, "file:src/test/resources/dhcpserver-test-config-save.json");
		
		config = DhcpServerConfiguration.loadConfig("file:src/test/resources/dhcpserver-test-config-save.json");
		assertNotNull(config);
		assertNotNull(config.getV6ServerIdOption());
		assertTrue(Arrays.equals(serverId.getOpaqueData().getHexValue(), 
				config.getV6ServerIdOption().getOpaqueData().getHexValue()));
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("dhcp.sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
	}
	
	public void testSaveAndLoadYamlConfig() throws Exception
	{
		DhcpServerConfig config = new DhcpServerConfig();
		
		V6ServerIdOption serverId = new V6ServerIdOption();
		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
		serverId.setOpaqueData(duid);
		config.setV6ServerIdOption(serverId);
		
		Policy policy = new Policy();
		policy.setName("dhcp.sendRequestedOptionsOnly");
		policy.setValue("true");
		PoliciesType policies = new PoliciesType();
		policies.getPolicyList().add(policy);
		config.setPolicies(policies);
		
		DhcpServerConfiguration.saveConfig(config, "file:src/test/resources/dhcpserver-test-config-save.yaml");
		
		config = DhcpServerConfiguration.loadConfig("file:src/test/resources/dhcpserver-test-config-save.yaml");
		assertNotNull(config);
		assertNotNull(config.getV6ServerIdOption());
		assertTrue(Arrays.equals(serverId.getOpaqueData().getHexValue(), 
				config.getV6ServerIdOption().getOpaqueData().getHexValue()));
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("dhcp.sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
	}

	
    /**
     * Test link map.
     * 
     * @throws Exception the exception
     */
    public void testLinkMap() throws Exception
    {
    	String configFilename = "file:src/test/resources/dhcpserver-test-link.xml";
        DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();
        serverConfig.init(configFilename);
        assertNotNull(serverConfig);
		assertNotNull(serverConfig.getGlobalPolicies());
		assertNotNull(serverConfig.getGlobalPolicies().getPolicyList());
		assertEquals(1, serverConfig.getGlobalPolicies().getPolicyList().size());
		assertEquals("dhcp.sendRequestedOptionsOnly", serverConfig.getGlobalPolicies().getPolicyList().get(0).getName());
		assertEquals("true", serverConfig.getGlobalPolicies().getPolicyList().get(0).getValue());
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
    
    public void testGlobalOptionDefs() throws Exception {
    	String configFilename = "file:src/test/resources/dhcpserver-test-optiondefs.xml";
        DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();
        serverConfig.init(configFilename);
        assertNotNull(serverConfig);
        InetSocketAddress localAddress = new InetSocketAddress(11111);
        InetSocketAddress remoteAddress = new InetSocketAddress(22222);
        DhcpV4Message v4RequestMsg = new DhcpV4Message(localAddress, remoteAddress);
        Map<Integer, DhcpOption> v4map = serverConfig.effectiveV4AddrOptions(v4RequestMsg);
        assertNotNull(v4map);
        assertEquals(3, v4map.size());
        DhcpOption v4SubnetMaskOption = v4map.get(1);
        assertNotNull(v4SubnetMaskOption);
        assertTrue(v4SubnetMaskOption instanceof DhcpV4SubnetMaskOption);
        DhcpOption v4OtherOption184 = v4map.get(184);
        assertNotNull(v4OtherOption184);
        assertTrue(v4OtherOption184 instanceof GenericIpAddressOption);
        assertEquals("1.2.3.4", ((GenericIpAddressOption)v4OtherOption184).getIpAddress());
        DhcpOption v4OtherOption185 = v4map.get(185);
        assertNotNull(v4OtherOption185);
        assertTrue(v4OtherOption185 instanceof GenericIpAddressListOption);
        List<String> option185ips = ((GenericIpAddressListOption)v4OtherOption185).getIpAddressList();
        assertNotNull(option185ips);
        assertEquals(2, option185ips.size());
        assertEquals("1.1.1.1", option185ips.get(0));
        assertEquals("2.2.2.2", option185ips.get(1));
        
        DhcpV6Message v6RequestMsg = new DhcpV6Message(localAddress, remoteAddress);
        Map<Integer, DhcpOption> v6map = serverConfig.effectiveMsgOptions(v6RequestMsg);
        assertNotNull(v6map);
        assertEquals(3, v6map.size());
        DhcpOption v6DnsServersOption = v6map.get(23);
        assertNotNull(v6DnsServersOption);
        assertTrue(v6DnsServersOption instanceof DhcpV6DnsServersOption);
        DhcpOption v6OtherOption186 = v6map.get(186);
        assertNotNull(v6OtherOption186);
        assertTrue(v6OtherOption186 instanceof GenericIpAddressOption);
        assertEquals("fd00::0006", ((GenericIpAddressOption)v6OtherOption186).getIpAddress());
        DhcpOption v6OtherOption187 = v6map.get(187);
        assertNotNull(v6OtherOption187);
        assertTrue(v6OtherOption187 instanceof GenericIpAddressListOption);
        List<String> option187ips = ((GenericIpAddressListOption)v6OtherOption187).getIpAddressList();
        assertNotNull(option187ips);
        assertEquals(2, option187ips.size());
        assertEquals("fd00::0001", option187ips.get(0));
        assertEquals("fd00::0002", option187ips.get(1));
    }
}
