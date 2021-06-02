package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagornet.dhcp.server.db.BaseTestCase;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.rest.api.JacksonObjectMapper;

public class TestBindingSerialization extends BaseTestCase {

	ObjectMapper objectMapper = new JacksonObjectMapper().getJsonObjectMapper();
	IdentityAssoc v4IA;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
//		initializeContext();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
//		closeContext();
	}
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
//		DhcpLink v4Link = config.findLinkForAddress(InetAddress.getByName("192.168.0.1"));
		
		v4IA = new IdentityAssoc();
		v4IA.setId(Long.valueOf(100));
		v4IA.setDuid(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e,
								(byte)0xde, (byte)0xbb, (byte)0x1e });
		v4IA.setIatype(IdentityAssoc.V4_TYPE);
		v4IA.setIaid(1);
		v4IA.setState(IdentityAssoc.LEASED);
		
		List<IaAddress> iaAddrs = new ArrayList<IaAddress>();
		IaAddress iaAddr = new IaAddress();
		iaAddr.setId(Long.valueOf(1000));
		iaAddr.setIdentityAssocId(v4IA.getId());
		iaAddr.setIpAddress(InetAddress.getByName("192.168.0.100"));
		Date now = new Date();
		iaAddr.setStartTime(now);
		Date expire = new Date(now.getTime() + 3600);
		iaAddr.setPreferredEndTime(expire);
		iaAddr.setValidEndTime(expire);
		iaAddr.setState(IaAddress.LEASED);
		iaAddrs.add(iaAddr);
		
		v4IA.setIaAddresses(iaAddrs);
	}
	
	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void test() throws Exception {
		String json = objectMapper.writeValueAsString(v4IA);
		System.out.println(json);
		IdentityAssoc ia = objectMapper.readValue(json, IdentityAssoc.class);
		Assert.assertEquals(v4IA, ia);
	}

}
