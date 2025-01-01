package com.jagornet.dhcp.server.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.option.v4.DhcpV4DomainNameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4DomainServersOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4HostnameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RoutersOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4SubnetMaskOption;
import com.jagornet.dhcp.server.grpc.DhcpLeaseUpdate;

public class TestDhcpLeaseUtil {

	DhcpLease dhcpLease = null;
	
	@Before
	public void setUp() throws Exception {
		dhcpLease = new DhcpLease();
		dhcpLease.setIpAddress(InetAddress.getByName("10.11.12.13"));
		dhcpLease.setDuid(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, 
										(byte)0xde, (byte)0xbb, (byte)0x1e });
		dhcpLease.setIaid(1);
		dhcpLease.setIatype(IdentityAssoc.V4_TYPE);
		dhcpLease.setState(IdentityAssoc.LEASED);
		Date now = new Date();
		dhcpLease.setStartTime(now);
		dhcpLease.setPreferredEndTime(now);
		dhcpLease.setValidEndTime(now);
		com.jagornet.dhcp.core.option.base.DhcpOption subnetMaskOption = 
				new DhcpV4SubnetMaskOption("255.255.255.0");
		dhcpLease.addDhcpOption(subnetMaskOption);
		com.jagornet.dhcp.core.option.base.DhcpOption routersOption = 
				new DhcpV4RoutersOption(Arrays.asList("10.11.12.1"));
		dhcpLease.addDhcpOption(routersOption);
		com.jagornet.dhcp.core.option.base.DhcpOption dnsServersOption = 
				new DhcpV4DomainServersOption(Arrays.asList("10.11.12.10"));
		dhcpLease.addDhcpOption(dnsServersOption);
		com.jagornet.dhcp.core.option.base.DhcpOption hostnameOption = 
				new DhcpV4HostnameOption("hostname");
		dhcpLease.addDhcpOption(hostnameOption);
		com.jagornet.dhcp.core.option.base.DhcpOption domainOption = 
				new DhcpV4DomainNameOption("jagornet.com");
		dhcpLease.addDhcpOption(domainOption);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToFromJson() {
		try {			
			String json = DhcpLeaseUtil.dhcpLeaseToJson(dhcpLease);
			System.out.println(json);
			DhcpLease _dhcpLease = DhcpLeaseUtil.jsonToDhcpLease(json);
			assertEquals(dhcpLease, _dhcpLease);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testToFromGrpc() {
		try {
			DhcpLeaseUpdate grpc = DhcpLeaseUtil.dhcpLeaseToGrpc(dhcpLease);
			System.out.println(grpc);
			DhcpLease _dhcpLease = DhcpLeaseUtil.grpcToDhcpLease(grpc);
			assertEquals(dhcpLease, _dhcpLease);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
