/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.hostname;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.Test;

/**
 * Unit tests for HostnameGenerator.
 * 
 * @author A. Gregory Rabil
 */
public class TestHostnameGenerator {

	@Test
	public void testDefaultTemplateV4() throws Exception {
		InetAddress ip = InetAddress.getByName("192.168.1.100");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		String generated = HostnameGenerator.generate(null, ip, mac, null, null);
		assertEquals("host-192-168-1-100", generated);
	}

	@Test
	public void testMacTemplate() throws Exception {
		InetAddress ip = InetAddress.getByName("10.0.0.1");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		String generated = HostnameGenerator.generate("device-${mac}", ip, mac, null, null);
		assertEquals("device-001122334455", generated);

		String generatedDashed = HostnameGenerator.generate("dev-${mac.dashed}", ip, mac, null, null);
		assertEquals("dev-00-11-22-33-44-55", generatedDashed);
	}

	@Test
	public void testFilterNameTemplate() throws Exception {
		InetAddress ip = InetAddress.getByName("172.16.0.5");
		byte[] mac = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };

		String generated = HostnameGenerator.generate("${filter.name}-${ip.dashed}", ip, mac, null, "GuestWifi");
		assertEquals("guestwifi-172-16-0-5", generated);
	}

	@Test
	public void testHexIpTemplate() throws Exception {
		InetAddress ip = InetAddress.getByName("192.168.1.1");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		String generated = HostnameGenerator.generate("node-${ip.hex}", ip, mac, null, null);
		assertEquals("node-c0a80101", generated);
	}

	@Test
	public void testV6Template() throws Exception {
		InetAddress ip = InetAddress.getByName("2001:db8::100");
		byte[] duid = new byte[] { 0x00, 0x01, 0x00, 0x01, (byte) 0xaa, (byte) 0xbb };

		String generated = HostnameGenerator.generate("v6-${ip.dashed}", ip, null, duid, null);
		String expected = "v6-" + ip.getHostAddress().replace(':', '-');
		assertEquals(expected, generated);
	}
}
