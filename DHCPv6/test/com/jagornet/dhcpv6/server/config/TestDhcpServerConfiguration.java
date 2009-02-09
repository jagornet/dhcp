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

import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig.Links;
import com.jagornet.dhcpv6.dto.DhcpV6ServerConfigDTO;
import com.jagornet.dhcpv6.dto.DnsServersOptionDTO;
import com.jagornet.dhcpv6.dto.OptionExpressionDTO;
import com.jagornet.dhcpv6.dto.PreferenceOptionDTO;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;

public class TestDhcpServerConfiguration extends TestCase
{
	public void testSaveAndLoadConfig() throws Exception
	{
		DhcpV6ServerConfig config = DhcpV6ServerConfigDocument.DhcpV6ServerConfig.Factory.newInstance();
		config.setSendRequestedOptionsOnlyPolicy(true);
		DhcpServerConfiguration.saveConfig(config, "test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		config = DhcpServerConfiguration.loadConfig("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigTestSave.xml");
		assertNotNull(config);
		assertEquals(true, config.getSendRequestedOptionsOnlyPolicy());
	}
	
    public void testLinkMap() throws Exception
    {
        DhcpServerConfiguration.init("test/com/jagornet/dhcpv6/server/config/dhcpServerConfigLinkTest1.xml");
        
        DhcpV6ServerConfig config = DhcpServerConfiguration.getConfig();
        assertNotNull(config);
        assertEquals(true, config.getSendRequestedOptionsOnlyPolicy());
        assertEquals("abcdef0123456789", Util.toHexString(config.getServerIdOption().getHexValue()));
        assertNotNull(config.getDnsServersOption());
        assertEquals(3, config.getDnsServersOption().getServerIpAddressesArray().length);
        assertEquals("3ffe::0001", config.getDnsServersOption().getServerIpAddressesArray(0));
        assertEquals("3ffe::0002", config.getDnsServersOption().getServerIpAddressesArray(1));
        assertEquals("3ffe::0003", config.getDnsServersOption().getServerIpAddressesArray(2));
        assertNotNull(config.getDomainSearchListOption());
        assertEquals(3, config.getDomainSearchListOption().getDomainNamesArray().length);
        assertEquals("foo.com.", config.getDomainSearchListOption().getDomainNamesArray(0));
        assertEquals("bar.com.", config.getDomainSearchListOption().getDomainNamesArray(1));
        assertEquals("yuk.com.", config.getDomainSearchListOption().getDomainNamesArray(2));
        
        TreeMap<Subnet, Links> linkMap = DhcpServerConfiguration.getLinkMap();
        assertNotNull(linkMap);
        assertEquals(5, linkMap.size());
        Subnet searchAddr = new Subnet("2001:DB8:3:1:DEB:DEB:DEB:1", 128);
        // there are two subnets greater than our search address
        SortedMap<Subnet, Links> subMap = linkMap.tailMap(searchAddr);
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
        assertEquals(true, dto.getSendRequestedOptionsOnlyPolicy().booleanValue());
        assertEquals("abcdef0123456789", Util.toHexString(dto.getServerIdOption().getHexValue()));
        assertNotNull(dto.getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, dto.getDnsServersOption().getCode().intValue());
        assertEquals(3, dto.getDnsServersOption().getServerIpAddresses().size());
        assertEquals("3ffe::0001", dto.getDnsServersOption().getServerIpAddresses().get(0));
        assertEquals("3ffe::0002", dto.getDnsServersOption().getServerIpAddresses().get(1));
        assertEquals("3ffe::0003", dto.getDnsServersOption().getServerIpAddresses().get(2));
        assertNotNull(dto.getDomainSearchListOption());
        assertEquals(3, dto.getDomainSearchListOption().getDomainNames().size());
        assertEquals("foo.com.", dto.getDomainSearchListOption().getDomainNames().get(0));
        assertEquals("bar.com.", dto.getDomainSearchListOption().getDomainNames().get(1));
        assertEquals("yuk.com.", dto.getDomainSearchListOption().getDomainNames().get(2));
    }
    
    public void testFromDTO()
    {
    	DhcpV6ServerConfigDTO dto = new DhcpV6ServerConfigDTO();
    	dto.setSendRequestedOptionsOnlyPolicy(true);
    	PreferenceOptionDTO preferenceDTO = new PreferenceOptionDTO();
    	preferenceDTO.setValue((short)10);
    	dto.setPreferenceOption(preferenceDTO);
    	DnsServersOptionDTO dnsServersDTO = new DnsServersOptionDTO();
    	dnsServersDTO.setServerIpAddresses(Arrays.asList("3ffe::0001", "3ffe::0002", "3ffe::0003"));
    	dto.setDnsServersOption(dnsServersDTO);
    	List<DhcpV6ServerConfigDTO.Filters> filtersList = new ArrayList<DhcpV6ServerConfigDTO.Filters>();
    	DhcpV6ServerConfigDTO.Filters filtersDTO = new DhcpV6ServerConfigDTO.Filters();
    	List<OptionExpressionDTO> optionExpressions = new ArrayList<OptionExpressionDTO>();
    	OptionExpressionDTO opEx = new OptionExpressionDTO();
    	opEx.setCode(DhcpConstants.OPTION_CLIENTID);
    	opEx.setOperator("equals");
    	opEx.setAsciiValue("foobar");
    	optionExpressions.add(opEx);
    	filtersDTO.setOptionExpressions(optionExpressions);
    	DnsServersOptionDTO filterDnsServersDTO = new DnsServersOptionDTO();
    	filterDnsServersDTO.setServerIpAddresses(Arrays.asList("4ffe::0001", "4ffe::0002", "4ffe::0003"));
    	filtersDTO.setDnsServersOption(filterDnsServersDTO);
    	filtersList.add(filtersDTO);
    	dto.setFilters(filtersList);
    	
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DhcpV6ServerConfig config = (DhcpV6ServerConfig) 
                mapper.map(dto, DhcpV6ServerConfig.class);
        
        assertNotNull(config);
        assertEquals(true, config.getSendRequestedOptionsOnlyPolicy());
        assertNotNull(config.getPreferenceOption());
        assertEquals(10, config.getPreferenceOption().getShortValue());
        assertNotNull(config.getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getDnsServersOption().getCode());
        assertEquals(3, config.getDnsServersOption().sizeOfServerIpAddressesArray());
        assertEquals("3ffe::0001", config.getDnsServersOption().getServerIpAddressesArray(0));
        assertEquals("3ffe::0002", config.getDnsServersOption().getServerIpAddressesArray(1));
        assertEquals("3ffe::0003", config.getDnsServersOption().getServerIpAddressesArray(2));
        assertNotNull(config.getFiltersArray());
        assertEquals(1, config.getFiltersArray().length);
        assertNotNull(config.getFiltersArray(0).getOptionExpressionsArray());
        assertEquals(1, config.getFiltersArray(0).getOptionExpressionsArray().length);
        assertEquals(DhcpConstants.OPTION_CLIENTID,
        		config.getFiltersArray(0).getOptionExpressionsArray(0).getCode());
        assertEquals("equals",
   			 	config.getFiltersArray(0).getOptionExpressionsArray(0).getOperator().toString());
        assertEquals("foobar",
   			 	config.getFiltersArray(0).getOptionExpressionsArray(0).getData().getAsciiValue());
        assertNotNull(config.getFiltersArray(0).getDnsServersOption());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, config.getFiltersArray(0).getDnsServersOption().getCode());
        assertEquals(3, config.getFiltersArray(0).getDnsServersOption().sizeOfServerIpAddressesArray());
        assertEquals("4ffe::0001", config.getFiltersArray(0).getDnsServersOption().getServerIpAddressesArray(0));
        assertEquals("4ffe::0002", config.getFiltersArray(0).getDnsServersOption().getServerIpAddressesArray(1));
        assertEquals("4ffe::0003", config.getFiltersArray(0).getDnsServersOption().getServerIpAddressesArray(2));
    }
}
