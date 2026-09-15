/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.BaseDhcpV6Processor;

/**
 * Unit tests for DhcpV6DuidFilter and associated file-based DUID policies.
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpV6DuidFilter {

	private File tempExcludedFile;
	private File tempIncludedFile;

	@Before
	public void setUp() {
		DhcpServerPolicies.SERVER_PROPERTIES = new Properties(DhcpServerPolicies.DEFAULT_PROPERTIES);
	}

	@After
	public void tearDown() {
		if ((tempExcludedFile != null) && tempExcludedFile.exists()) {
			tempExcludedFile.delete();
		}
		if ((tempIncludedFile != null) && tempIncludedFile.exists()) {
			tempIncludedFile.delete();
		}
	}

	@Test
	public void testNormalizeDuid() {
		assertEquals("0001000128ABCDEF001122334455", 
				DhcpV6DuidFilter.normalizeDuid("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:55"));
		assertEquals("0001000128ABCDEF001122334455", 
				DhcpV6DuidFilter.normalizeDuid("00-01-00-01-28-ab-cd-ef-00-11-22-33-44-55"));
		assertEquals("0001000128ABCDEF001122334455", 
				DhcpV6DuidFilter.normalizeDuid("0001.0001.28ab.cdef.0011.2233.4455"));
		assertEquals("0001000128ABCDEF001122334455", 
				DhcpV6DuidFilter.normalizeDuid("0001000128abcdef001122334455"));
		assertNull(DhcpV6DuidFilter.normalizeDuid(null));
		assertNull(DhcpV6DuidFilter.normalizeDuid(""));
		assertNull(DhcpV6DuidFilter.normalizeDuid("not-a-duid"));
		assertNull(DhcpV6DuidFilter.normalizeDuid("1")); // odd length
	}

	@Test
	public void testLoadDuidsFromFile() throws IOException {
		tempExcludedFile = File.createTempFile("test_duids_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("# Comment line\n");
			writer.write("// Another comment style\n");
			writer.write("; Semicolon comment\n");
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:55\n");
			writer.write("  00-02-00-00-aa-bb-cc-dd-ee-ff   # inline comment\n");
			writer.write("0003.0001.1122.3344.5566 // cisco format inline\n");
			writer.write("\n"); // blank line
			writer.write("0004000102030405060708090A0B; semi inline comment\n");
		}

		Set<String> duids = DhcpV6DuidFilter.loadDuidsFromFile(tempExcludedFile);
		assertNotNull(duids);
		assertEquals(4, duids.size());
		assertTrue(duids.contains("0001000128ABCDEF001122334455"));
		assertTrue(duids.contains("00020000AABBCCDDEEFF"));
		assertTrue(duids.contains("00030001112233445566"));
		assertTrue(duids.contains("0004000102030405060708090A0B"));
	}

	@Test
	public void testDefaultFiltering() {
		DhcpV6DuidFilter filter = new DhcpV6DuidFilter();
		filter.init();

		byte[] duid = Util.fromHexString("0001000128ABCDEF001122334455");
		assertTrue(filter.isAllowed(duid));
		// When no allowlist is configured, null/empty DUID passes through to message validation
		assertTrue(filter.isAllowed(null));
		assertTrue(filter.isAllowed(new byte[0]));
	}

	@Test
	public void testExcludedDuidsFile() throws IOException {
		tempExcludedFile = File.createTempFile("excluded_duids_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("# Blocklist\n");
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:55\n");
			writer.write("00-02-00-00-aa-bb-cc-dd-ee-ff\n");
		}

		DhcpServerPolicies.setProperty(Property.V6_EXCLUDED_DUIDS_FILE, tempExcludedFile.getAbsolutePath());

		DhcpV6DuidFilter filter = new DhcpV6DuidFilter();
		filter.init();

		byte[] blocked1 = Util.fromHexString("0001000128ABCDEF001122334455");
		byte[] blocked2 = Util.fromHexString("00020000AABBCCDDEEFF");
		byte[] allowed = Util.fromHexString("0001000128ABCDEF001122334456");

		assertFalse(filter.isAllowed(blocked1));
		assertFalse(filter.isAllowed(blocked2));
		assertTrue(filter.isAllowed(allowed));
	}

	@Test
	public void testIncludedDuidsFile() throws IOException {
		tempIncludedFile = File.createTempFile("included_duids_", ".txt");
		try (FileWriter writer = new FileWriter(tempIncludedFile)) {
			writer.write("# Allowlist - only these DUIDs are permitted\n");
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:55\n");
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:56\n");
		}

		DhcpServerPolicies.setProperty(Property.V6_INCLUDED_DUIDS_FILE, tempIncludedFile.getAbsolutePath());

		DhcpV6DuidFilter filter = new DhcpV6DuidFilter();
		filter.init();

		byte[] allowed1 = Util.fromHexString("0001000128ABCDEF001122334455");
		byte[] allowed2 = Util.fromHexString("0001000128ABCDEF001122334456");
		byte[] disallowed = Util.fromHexString("0001000128ABCDEF001122334457");

		assertTrue(filter.isAllowed(allowed1));
		assertTrue(filter.isAllowed(allowed2));
		assertFalse(filter.isAllowed(disallowed));
		// Missing DUID is disallowed when allowlist is active
		assertFalse(filter.isAllowed(null));
		assertFalse(filter.isAllowed(new byte[0]));
	}

	@Test
	public void testCombinedIncludedAndExcluded() throws IOException {
		tempIncludedFile = File.createTempFile("included_", ".txt");
		try (FileWriter writer = new FileWriter(tempIncludedFile)) {
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:55\n");
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:56\n");
		}

		tempExcludedFile = File.createTempFile("excluded_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			// Explicitly block 0001000128abcdef001122334455 even though it's in included
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:55\n");
		}

		DhcpServerPolicies.setProperty(Property.V6_INCLUDED_DUIDS_FILE, tempIncludedFile.getAbsolutePath());
		DhcpServerPolicies.setProperty(Property.V6_EXCLUDED_DUIDS_FILE, tempExcludedFile.getAbsolutePath());

		DhcpV6DuidFilter filter = new DhcpV6DuidFilter();
		filter.init();

		byte[] duid55 = Util.fromHexString("0001000128ABCDEF001122334455");
		byte[] duid56 = Util.fromHexString("0001000128ABCDEF001122334456");
		byte[] duid57 = Util.fromHexString("0001000128ABCDEF001122334457");

		// duid55 is in both -> excluded takes precedence!
		assertFalse(filter.isAllowed(duid55));
		// duid56 is in included and not excluded -> allowed!
		assertTrue(filter.isAllowed(duid56));
		// duid57 is neither -> not allowed!
		assertFalse(filter.isAllowed(duid57));
	}

	@Test
	public void testHotReloading() throws Exception {
		tempExcludedFile = File.createTempFile("hot_reload_duid_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:01\n");
		}

		DhcpServerPolicies.setProperty(Property.V6_EXCLUDED_DUIDS_FILE, tempExcludedFile.getAbsolutePath());

		DhcpV6DuidFilter filter = new DhcpV6DuidFilter();
		filter.init();

		byte[] duid01 = Util.fromHexString("0001000128ABCDEF001122334401");
		byte[] duid02 = Util.fromHexString("0001000128ABCDEF001122334402");

		assertFalse(filter.isAllowed(duid01));
		assertTrue(filter.isAllowed(duid02));

		// Now modify the file to also exclude duid02
		Thread.sleep(100);
		try (FileWriter writer = new FileWriter(tempExcludedFile, true)) {
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:02\n");
		}
		tempExcludedFile.setLastModified(System.currentTimeMillis() + 5000);

		// Trigger reload
		filter.reload();

		assertFalse(filter.isAllowed(duid01));
		assertFalse(filter.isAllowed(duid02));
	}

	private class DummyV6Processor extends BaseDhcpV6Processor {
		public DummyV6Processor(DhcpV6Message requestMsg, InetAddress clientLinkAddress) {
			super(requestMsg, clientLinkAddress);
		}
		@Override
		public boolean process() {
			return true;
		}
		public boolean testIsIgnoredDuid(byte[] duid) {
			return isIgnoredDuid(duid);
		}
	}

	@Test
	public void testProcessorDuidFiltering() throws IOException {
		tempExcludedFile = File.createTempFile("proc_excluded_duid_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("00:01:00:01:28:ab:cd:ef:00:11:22:33:44:99\n");
		}

		DhcpServerPolicies.setProperty(Property.V6_EXCLUDED_DUIDS_FILE, tempExcludedFile.getAbsolutePath());
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.getDhcpV6DuidFilter().init();

		InetSocketAddress local = new InetSocketAddress("2001:db8::1", 547);
		InetSocketAddress remote = new InetSocketAddress("2001:db8::2", 546);
		DhcpV6Message req = new DhcpV6Message(local, remote);
		DummyV6Processor processor = new DummyV6Processor(req, local.getAddress());

		byte[] blockedDuid = Util.fromHexString("0001000128ABCDEF001122334499");
		byte[] allowedDuid = Util.fromHexString("0001000128ABCDEF001122334488");

		assertTrue(processor.testIsIgnoredDuid(blockedDuid));
		assertFalse(processor.testIsIgnoredDuid(allowedDuid));

		// Test with ClientIdOption on message
		DhcpV6ClientIdOption clientIdOption = new DhcpV6ClientIdOption();
		BaseOpaqueData opaque = new BaseOpaqueData();
		opaque.setHex(blockedDuid);
		clientIdOption.setOpaqueData(opaque);
		req.putDhcpOption(clientIdOption);

		// preProcess should reject message with blocked DUID
		assertFalse(processor.preProcess());
	}
}
