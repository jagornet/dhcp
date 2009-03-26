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
import com.jagornet.dhcpv6.dto.FilterDTO;
import com.jagornet.dhcpv6.dto.FilterExpressionDTO;
import com.jagornet.dhcpv6.dto.FilterExpressionsDTO;
import com.jagornet.dhcpv6.dto.FiltersDTO;
import com.jagornet.dhcpv6.dto.OptionsDTO;
import com.jagornet.dhcpv6.dto.PoliciesDTO;
import com.jagornet.dhcpv6.dto.PolicyDTO;
import com.jagornet.dhcpv6.dto.option.DnsServersOptionDTO;
import com.jagornet.dhcpv6.dto.option.PreferenceOptionDTO;
import com.jagornet.dhcpv6.dto.option.base.OpaqueDataDTO;
import com.jagornet.dhcpv6.dto.option.base.OpaqueDataOptionDTO;
import com.jagornet.dhcpv6.dto.option.base.OptionExpressionDTO;
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
        assertEquals("abcdef0123456789", Util.toHexString(config.getServerIdOption().getOpaqueData().getHexValue()));
        assertNotNull(config.getOptions().getDnsServersOption());
        assertEquals(3, config.getOptions().getDnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", config.getOptions().getDnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", config.getOptions().getDnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", config.getOptions().getDnsServersOption().getIpAddressList().get(2));
        assertNotNull(config.getOptions().getDomainSearchListOption());
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
        assertEquals(1, dto.getPolicies().getPolicyList().size());
        assertEquals("sendRequestedOptionsOnly", dto.getPolicies().getPolicyList().get(0).getName());
        assertEquals("true", dto.getPolicies().getPolicyList().get(0).getValue());
        assertEquals("abcdef0123456789", Util.toHexString(dto.getServerIdOption().getOpaqueData().getHexValue()));
        assertNotNull(dto.getOptions());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, dto.getOptions().getDnsServersOption().getCode().intValue());
        assertEquals(3, dto.getOptions().getDnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", dto.getOptions().getDnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", dto.getOptions().getDnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", dto.getOptions().getDnsServersOption().getIpAddressList().get(2));
        assertNotNull(dto.getOptions().getDomainSearchListOption());
        assertEquals(3, dto.getOptions().getDomainSearchListOption().getDomainNameList().size());
        assertEquals("foo.com.", dto.getOptions().getDomainSearchListOption().getDomainNameList().get(0));
        assertEquals("bar.com.", dto.getOptions().getDomainSearchListOption().getDomainNameList().get(1));
        assertEquals("yuk.com.", dto.getOptions().getDomainSearchListOption().getDomainNameList().get(2));
    }
    
    public void testFromDTO()
    {
    	DhcpV6ServerConfigDTO dto = new DhcpV6ServerConfigDTO();
    	PolicyDTO policyDTO = new PolicyDTO();
    	policyDTO.setName("sendRequestedOptionsOnly");
    	policyDTO.setValue("true");
    	List<PolicyDTO> policyList = new ArrayList<PolicyDTO>();
    	policyList.add(policyDTO);
    	PoliciesDTO policies = new PoliciesDTO();
    	policies.setPolicyList(policyList);
    	dto.setPolicies(policies);
    	OptionsDTO optionsDTO = new OptionsDTO();
    	PreferenceOptionDTO preferenceDTO = new PreferenceOptionDTO();
    	preferenceDTO.setValue((short)10);
    	optionsDTO.setPreferenceOption(preferenceDTO);
    	DnsServersOptionDTO dnsServersDTO = new DnsServersOptionDTO();
    	dnsServersDTO.setIpAddressList(Arrays.asList("3ffe::0001", "3ffe::0002", "3ffe::0003"));
    	optionsDTO.setDnsServersOption(dnsServersDTO);
    	dto.setOptions(optionsDTO);
    	List<FilterDTO> filtersList = new ArrayList<FilterDTO>();
    	FilterDTO filterDTO = new FilterDTO();
    	List<FilterExpressionDTO> filterExpressionList = new ArrayList<FilterExpressionDTO>();
    	FilterExpressionDTO filterExpr = new FilterExpressionDTO();
    	OptionExpressionDTO opExpr = new OptionExpressionDTO();
    	opExpr.setCode(DhcpConstants.OPTION_CLIENTID);
    	OpaqueDataOptionDTO opaqueOption = new OpaqueDataOptionDTO();
    	// set the code on the option itself in addition to the expression
    	opaqueOption.setCode(DhcpConstants.OPTION_CLIENTID);
    	//opaqueOption.setAsciiValue("foobar");
    	OpaqueDataDTO opaque = new OpaqueDataDTO();
    	opaque.setAsciiValue("foobar");
    	opaqueOption.setOpaqueData(opaque);
    	opExpr.setOpaqueDataOption(opaqueOption);
    	opExpr.setOperator("equals");
    	filterExpr.setOptionExpression(opExpr);  	
    	filterExpressionList.add(filterExpr);
    	FilterExpressionsDTO filterExprs = new FilterExpressionsDTO();
    	filterExprs.setFilterExpressionList(filterExpressionList);
    	filterDTO.setFilterExpressions(filterExprs);
    	DnsServersOptionDTO filterDnsServersDTO = new DnsServersOptionDTO();
    	filterDnsServersDTO.setIpAddressList(Arrays.asList("4ffe::0001", "4ffe::0002", "4ffe::0003"));
    	OptionsDTO filtersDTO = new OptionsDTO();
    	filtersDTO.setDnsServersOption(filterDnsServersDTO);
    	filterDTO.setOptions(filtersDTO);
    	filtersList.add(filterDTO);
    	FiltersDTO filters = new FiltersDTO();
    	filters.setFilterList(filtersList);
    	dto.setFilters(filters);
    	
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DhcpV6ServerConfig config = (DhcpV6ServerConfig) mapper.map(dto, DhcpV6ServerConfig.class);
        
        assertNotNull(config);
		assertNotNull(config.getPolicies().getPolicyList());
		assertEquals(1, config.getPolicies().getPolicyList().size());
		assertEquals("sendRequestedOptionsOnly", config.getPolicies().getPolicyList().get(0).getName());
		assertEquals("true", config.getPolicies().getPolicyList().get(0).getValue());
        assertNotNull(config.getOptions().getPreferenceOption());
        assertEquals(10, config.getOptions().getPreferenceOption().getUnsignedByte());
        assertNotNull(config.getOptions().getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getOptions().getDnsServersOption().getCode());
        assertEquals(3, config.getOptions().getDnsServersOption().getIpAddressList().size());
        assertEquals("3ffe::0001", config.getOptions().getDnsServersOption().getIpAddressList().get(0));
        assertEquals("3ffe::0002", config.getOptions().getDnsServersOption().getIpAddressList().get(1));
        assertEquals("3ffe::0003", config.getOptions().getDnsServersOption().getIpAddressList().get(2));
        assertNotNull(config.getFilters().getFilterList());
        assertEquals(1, config.getFilters().getFilterList().size());
        assertEquals(1, config.getFilters().getFilterList().get(0).getFilterExpressions().getFilterExpressionList().size());
        assertNotNull(config.getFilters().getFilterList().get(0).getFilterExpressions().getFilterExpressionList().get(0).getOptionExpression());
        assertEquals(DhcpConstants.OPTION_CLIENTID,
        		config.getFilters().getFilterList().get(0).getFilterExpressions().getFilterExpressionList().get(0).getOptionExpression().getCode());
        assertEquals("equals",
   			 	config.getFilters().getFilterList().get(0).getFilterExpressions().getFilterExpressionList().get(0).getOptionExpression().getOperator().toString());
        assertEquals("foobar",
   			 	config.getFilters().getFilterList().get(0).getFilterExpressions().getFilterExpressionList().get(0).getOptionExpression().getOpaqueDataOption().getOpaqueData().getAsciiValue());
        assertNotNull(config.getFilters().getFilterList().get(0).getOptions().getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getFilters().getFilterList().get(0).getOptions().getDnsServersOption().getCode());
        assertEquals(3, config.getFilters().getFilterList().get(0).getOptions().getDnsServersOption().getIpAddressList().size());
        assertEquals("4ffe::0001", config.getFilters().getFilterList().get(0).getOptions().getDnsServersOption().getIpAddressList().get(0));
        assertEquals("4ffe::0002", config.getFilters().getFilterList().get(0).getOptions().getDnsServersOption().getIpAddressList().get(1));
        assertEquals("4ffe::0003", config.getFilters().getFilterList().get(0).getOptions().getDnsServersOption().getIpAddressList().get(2));
    }
}
