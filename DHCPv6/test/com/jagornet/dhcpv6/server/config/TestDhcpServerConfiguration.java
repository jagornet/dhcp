package com.jagornet.dhcpv6.server.config;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import com.jagornet.dhcpv6.dto.DhcpV6ServerConfigDTO;
import com.jagornet.dhcpv6.dto.DnsServersOptionDTO;
import com.jagornet.dhcpv6.dto.FilterDTO;
import com.jagornet.dhcpv6.dto.FilterExpressionDTO;
import com.jagornet.dhcpv6.dto.OptionExpressionDTO;
import com.jagornet.dhcpv6.dto.PolicyDTO;
import com.jagornet.dhcpv6.dto.PreferenceOptionDTO;
import com.jagornet.dhcpv6.dto.StandardOptionsDTO;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.Link;
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
		config.setPoliciesArray(new Policy[] { policy });
		DhcpServerConfiguration.saveConfig(config, "test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		config = DhcpServerConfiguration.loadConfig("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		assertNotNull(config);
		assertNotNull(config.getPoliciesArray());
		assertEquals(1, config.getPoliciesArray().length);
		assertEquals("sendRequestedOptionsOnly", config.getPoliciesArray(0).getName());
		assertEquals("true", config.getPoliciesArray(0).getValue());
	}
	
    public void testLinkMap() throws Exception
    {
        DhcpServerConfiguration.init("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigLinkTest1.xml");
        
        DhcpV6ServerConfig config = DhcpServerConfiguration.getConfig();
        assertNotNull(config);
		assertNotNull(config.getPoliciesArray());
		assertEquals(1, config.getPoliciesArray().length);
		assertEquals("sendRequestedOptionsOnly", config.getPoliciesArray(0).getName());
		assertEquals("true", config.getPoliciesArray(0).getValue());
        assertEquals("abcdef0123456789", Util.toHexString(config.getServerIdOption().getHexValue()));
        assertNotNull(config.getStandardOptions().getDnsServersOption());
        assertEquals(3, config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray().length);
        assertEquals("3ffe::0001", config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray(0));
        assertEquals("3ffe::0002", config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray(1));
        assertEquals("3ffe::0003", config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray(2));
        assertNotNull(config.getStandardOptions().getDomainSearchListOption());
        assertEquals(3, config.getStandardOptions().getDomainSearchListOption().getDomainNamesArray().length);
        assertEquals("foo.com.", config.getStandardOptions().getDomainSearchListOption().getDomainNamesArray(0));
        assertEquals("bar.com.", config.getStandardOptions().getDomainSearchListOption().getDomainNamesArray(1));
        assertEquals("yuk.com.", config.getStandardOptions().getDomainSearchListOption().getDomainNamesArray(2));
        
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
    
    public void testToDTO() throws Exception
    {
        DhcpServerConfiguration.init("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigLinkTest1.xml");
        
        DhcpV6ServerConfig config = DhcpServerConfiguration.getConfig();
        assertNotNull(config);

        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DhcpV6ServerConfigDTO dto = (DhcpV6ServerConfigDTO) mapper.map(config, DhcpV6ServerConfigDTO.class);
        
        assertNotNull(dto);
        assertNotNull(dto.getPolicies());
        assertEquals(1, dto.getPolicies().size());
        assertEquals("sendRequestedOptionsOnly", dto.getPolicies().get(0).getName());
        assertEquals("true", dto.getPolicies().get(0).getValue());
        assertEquals("abcdef0123456789", Util.toHexString(dto.getServerIdOption().getHexValue()));
        assertNotNull(dto.getStandardOptions());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, dto.getStandardOptions().getDnsServersOption().getCode().intValue());
        assertEquals(3, dto.getStandardOptions().getDnsServersOption().getServerIpAddresses().size());
        assertEquals("3ffe::0001", dto.getStandardOptions().getDnsServersOption().getServerIpAddresses().get(0));
        assertEquals("3ffe::0002", dto.getStandardOptions().getDnsServersOption().getServerIpAddresses().get(1));
        assertEquals("3ffe::0003", dto.getStandardOptions().getDnsServersOption().getServerIpAddresses().get(2));
        assertNotNull(dto.getStandardOptions().getDomainSearchListOption());
        assertEquals(3, dto.getStandardOptions().getDomainSearchListOption().getDomainNames().size());
        assertEquals("foo.com.", dto.getStandardOptions().getDomainSearchListOption().getDomainNames().get(0));
        assertEquals("bar.com.", dto.getStandardOptions().getDomainSearchListOption().getDomainNames().get(1));
        assertEquals("yuk.com.", dto.getStandardOptions().getDomainSearchListOption().getDomainNames().get(2));
    }
    
    public void testFromDTO()
    {
    	DhcpV6ServerConfigDTO dto = new DhcpV6ServerConfigDTO();
    	PolicyDTO policyDTO = new PolicyDTO();
    	policyDTO.setName("sendRequestedOptionsOnly");
    	policyDTO.setValue("true");
    	List<PolicyDTO> policies = new ArrayList<PolicyDTO>();
    	policies.add(policyDTO);
    	dto.setPolicies(policies);
    	StandardOptionsDTO standardsDTO = new StandardOptionsDTO();
    	PreferenceOptionDTO preferenceDTO = new PreferenceOptionDTO();
    	preferenceDTO.setValue((short)10);
    	standardsDTO.setPreferenceOption(preferenceDTO);
    	DnsServersOptionDTO dnsServersDTO = new DnsServersOptionDTO();
    	dnsServersDTO.setServerIpAddresses(Arrays.asList("3ffe::0001", "3ffe::0002", "3ffe::0003"));
    	standardsDTO.setDnsServersOption(dnsServersDTO);
    	dto.setStandardOptions(standardsDTO);
    	List<FilterDTO> filtersList = new ArrayList<FilterDTO>();
    	FilterDTO filterDTO = new FilterDTO();
    	List<FilterExpressionDTO> filterExpressions = new ArrayList<FilterExpressionDTO>();
    	FilterExpressionDTO filterExpr = new FilterExpressionDTO();
    	OptionExpressionDTO opExpr = new OptionExpressionDTO();
    	opExpr.setCode(DhcpConstants.OPTION_CLIENTID);
    	opExpr.setOperator("equals");
    	opExpr.setAsciiValue("foobar");
    	filterExpr.setOptionExpression(opExpr);
    	filterExpressions.add(filterExpr);
    	filterDTO.setFilterExpressions(filterExpressions);
    	DnsServersOptionDTO filterDnsServersDTO = new DnsServersOptionDTO();
    	filterDnsServersDTO.setServerIpAddresses(Arrays.asList("4ffe::0001", "4ffe::0002", "4ffe::0003"));
    	StandardOptionsDTO filterStandardsDTO = new StandardOptionsDTO();
    	filterStandardsDTO.setDnsServersOption(filterDnsServersDTO);
    	filterDTO.setStandardOptions(filterStandardsDTO);
    	filtersList.add(filterDTO);
    	dto.setFilters(filtersList);
    	
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DhcpV6ServerConfig config = (DhcpV6ServerConfig) 
                mapper.map(dto, DhcpV6ServerConfig.class);
        
        assertNotNull(config);
		assertNotNull(config.getPoliciesArray());
		assertEquals(1, config.getPoliciesArray().length);
		assertEquals("sendRequestedOptionsOnly", config.getPoliciesArray(0).getName());
		assertEquals("true", config.getPoliciesArray(0).getValue());
        assertNotNull(config.getStandardOptions().getPreferenceOption());
        assertEquals(10, config.getStandardOptions().getPreferenceOption().getShortValue());
        assertNotNull(config.getStandardOptions().getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getStandardOptions().getDnsServersOption().getCode());
        assertEquals(3, config.getStandardOptions().getDnsServersOption().sizeOfServerIpAddressesArray());
        assertEquals("3ffe::0001", config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray(0));
        assertEquals("3ffe::0002", config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray(1));
        assertEquals("3ffe::0003", config.getStandardOptions().getDnsServersOption().getServerIpAddressesArray(2));
        assertNotNull(config.getFiltersArray());
        assertEquals(1, config.getFiltersArray().length);
        assertEquals(1, config.getFiltersArray(0).getFilterExpressionsArray().length);
        assertNotNull(config.getFiltersArray(0).getFilterExpressionsArray(0).getOptionExpression());
        assertEquals(DhcpConstants.OPTION_CLIENTID,
        		config.getFiltersArray(0).getFilterExpressionsArray(0).getOptionExpression().getCode());
        assertEquals("equals",
   			 	config.getFiltersArray(0).getFilterExpressionsArray(0).getOptionExpression().getOperator().toString());
        assertEquals("foobar",
   			 	config.getFiltersArray(0).getFilterExpressionsArray(0).getOptionExpression().getData().getAsciiValue());
        assertNotNull(config.getFiltersArray(0).getStandardOptions().getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getFiltersArray(0).getStandardOptions().getDnsServersOption().getCode());
        assertEquals(3, config.getFiltersArray(0).getStandardOptions().getDnsServersOption().sizeOfServerIpAddressesArray());
        assertEquals("4ffe::0001", config.getFiltersArray(0).getStandardOptions().getDnsServersOption().getServerIpAddressesArray(0));
        assertEquals("4ffe::0002", config.getFiltersArray(0).getStandardOptions().getDnsServersOption().getServerIpAddressesArray(1));
        assertEquals("4ffe::0003", config.getFiltersArray(0).getStandardOptions().getDnsServersOption().getServerIpAddressesArray(2));
    }
}
