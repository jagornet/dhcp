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
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

/**
 * Unit tests for DhcpV4MacFilter and associated file-based MAC policies.
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpV4MacFilter {

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
	public void testNormalizeMac() {
		assertEquals("001122334455", DhcpV4MacFilter.normalizeMac("00:11:22:33:44:55"));
		assertEquals("001122334455", DhcpV4MacFilter.normalizeMac("00-11-22-33-44-55"));
		assertEquals("001122334455", DhcpV4MacFilter.normalizeMac("0011.2233.4455"));
		assertEquals("001122334455", DhcpV4MacFilter.normalizeMac("001122334455"));
		assertEquals("AABBCCDDEEFF", DhcpV4MacFilter.normalizeMac("aa:bb:cc:dd:ee:ff"));
		assertEquals("AABBCCDDEEFF", DhcpV4MacFilter.normalizeMac("AABB.CCDD.EEFF"));
		assertNull(DhcpV4MacFilter.normalizeMac(null));
		assertNull(DhcpV4MacFilter.normalizeMac(""));
		assertNull(DhcpV4MacFilter.normalizeMac("not-a-mac"));
		assertNull(DhcpV4MacFilter.normalizeMac("1")); // odd length
	}

	@Test
	public void testLoadMacsFromFile() throws IOException {
		tempExcludedFile = File.createTempFile("test_macs_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("# This is a comment\n");
			writer.write("// Another comment style\n");
			writer.write("; Semicolon comment\n");
			writer.write("00:11:22:33:44:55\n");
			writer.write("  aa-bb-cc-dd-ee-ff   # inline comment with spaces\n");
			writer.write("1122.3344.5566 // cisco format inline comment\n");
			writer.write("\n"); // blank line
			writer.write("1234567890AB; semi inline comment\n");
		}

		Set<String> macs = DhcpV4MacFilter.loadMacsFromFile(tempExcludedFile);
		assertNotNull(macs);
		assertEquals(4, macs.size());
		assertTrue(macs.contains("001122334455"));
		assertTrue(macs.contains("AABBCCDDEEFF"));
		assertTrue(macs.contains("112233445566"));
		assertTrue(macs.contains("1234567890AB"));
	}

	@Test
	public void testDefaultFiltering() {
		DhcpV4MacFilter filter = new DhcpV4MacFilter();
		filter.init();

		// Default legacy policy blocks 000000000000 and FFFFFFFFFFFF
		byte[] zeroMac = Util.fromHexString("000000000000");
		byte[] bcastMac = Util.fromHexString("FFFFFFFFFFFF");
		byte[] normalMac = Util.fromHexString("001122334455");

		assertFalse(filter.isAllowed(zeroMac));
		assertFalse(filter.isAllowed(bcastMac));
		assertFalse(filter.isAllowed(null));
		assertFalse(filter.isAllowed(new byte[0]));
		assertTrue(filter.isAllowed(normalMac));
	}

	@Test
	public void testExcludedMacsFile() throws IOException {
		tempExcludedFile = File.createTempFile("excluded_macs_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("# Blocklist\n");
			writer.write("00:11:22:33:44:55\n");
			writer.write("aa:bb:cc:dd:ee:ff\n");
		}

		DhcpServerPolicies.setProperty(Property.V4_EXCLUDED_MACS_FILE, tempExcludedFile.getAbsolutePath());

		DhcpV4MacFilter filter = new DhcpV4MacFilter();
		filter.init();

		byte[] blocked1 = Util.fromHexString("001122334455");
		byte[] blocked2 = Util.fromHexString("AABBCCDDEEFF");
		byte[] allowed = Util.fromHexString("001122334456");

		assertFalse(filter.isAllowed(blocked1));
		assertFalse(filter.isAllowed(blocked2));
		assertTrue(filter.isAllowed(allowed));
	}

	@Test
	public void testIncludedMacsFile() throws IOException {
		tempIncludedFile = File.createTempFile("included_macs_", ".txt");
		try (FileWriter writer = new FileWriter(tempIncludedFile)) {
			writer.write("# Allowlist - only these MACs are permitted\n");
			writer.write("00:11:22:33:44:55\n");
			writer.write("00:11:22:33:44:56\n");
		}

		DhcpServerPolicies.setProperty(Property.V4_INCLUDED_MACS_FILE, tempIncludedFile.getAbsolutePath());

		DhcpV4MacFilter filter = new DhcpV4MacFilter();
		filter.init();

		byte[] allowed1 = Util.fromHexString("001122334455");
		byte[] allowed2 = Util.fromHexString("001122334456");
		byte[] disallowed = Util.fromHexString("001122334457");

		assertTrue(filter.isAllowed(allowed1));
		assertTrue(filter.isAllowed(allowed2));
		assertFalse(filter.isAllowed(disallowed));
	}

	@Test
	public void testCombinedIncludedAndExcluded() throws IOException {
		tempIncludedFile = File.createTempFile("included_", ".txt");
		try (FileWriter writer = new FileWriter(tempIncludedFile)) {
			writer.write("00:11:22:33:44:55\n");
			writer.write("00:11:22:33:44:56\n");
		}

		tempExcludedFile = File.createTempFile("excluded_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			// Explicitly block 00:11:22:33:44:55 even though it's in included
			writer.write("00:11:22:33:44:55\n");
		}

		DhcpServerPolicies.setProperty(Property.V4_INCLUDED_MACS_FILE, tempIncludedFile.getAbsolutePath());
		DhcpServerPolicies.setProperty(Property.V4_EXCLUDED_MACS_FILE, tempExcludedFile.getAbsolutePath());

		DhcpV4MacFilter filter = new DhcpV4MacFilter();
		filter.init();

		byte[] mac55 = Util.fromHexString("001122334455");
		byte[] mac56 = Util.fromHexString("001122334456");
		byte[] mac57 = Util.fromHexString("001122334457");

		// mac55 is in both -> excluded takes precedence!
		assertFalse(filter.isAllowed(mac55));
		// mac56 is in included and not excluded -> allowed!
		assertTrue(filter.isAllowed(mac56));
		// mac57 is neither -> not allowed!
		assertFalse(filter.isAllowed(mac57));
	}

	@Test
	public void testHotReloading() throws Exception {
		tempExcludedFile = File.createTempFile("hot_reload_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("00:11:22:33:44:01\n");
		}

		DhcpServerPolicies.setProperty(Property.V4_EXCLUDED_MACS_FILE, tempExcludedFile.getAbsolutePath());

		DhcpV4MacFilter filter = new DhcpV4MacFilter();
		filter.init();

		byte[] mac01 = Util.fromHexString("001122334401");
		byte[] mac02 = Util.fromHexString("001122334402");

		assertFalse(filter.isAllowed(mac01));
		assertTrue(filter.isAllowed(mac02));

		// Now modify the file to also exclude mac02
		// Adjust last modified timestamp to ensure timestamp check detects change
		Thread.sleep(100);
		try (FileWriter writer = new FileWriter(tempExcludedFile, true)) {
			writer.write("00:11:22:33:44:02\n");
		}
		tempExcludedFile.setLastModified(System.currentTimeMillis() + 5000);

		// Trigger reload
		filter.reload();

		assertFalse(filter.isAllowed(mac01));
		assertFalse(filter.isAllowed(mac02));
	}

	private class DummyV4Processor extends com.jagornet.dhcp.server.request.BaseDhcpV4Processor {
		public DummyV4Processor(com.jagornet.dhcp.core.message.DhcpV4Message requestMsg, java.net.InetAddress clientLinkAddress) {
			super(requestMsg, clientLinkAddress);
		}
		@Override
		public boolean process() {
			return true;
		}
		public boolean testIsIgnoredMac(byte[] chAddr) {
			return isIgnoredMac(chAddr);
		}
	}

	@Test
	public void testProcessorMacFiltering() throws IOException {
		tempExcludedFile = File.createTempFile("proc_excluded_", ".txt");
		try (FileWriter writer = new FileWriter(tempExcludedFile)) {
			writer.write("00:11:22:33:44:99\n");
		}

		DhcpServerPolicies.setProperty(Property.V4_EXCLUDED_MACS_FILE, tempExcludedFile.getAbsolutePath());
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.getDhcpV4MacFilter().init();

		java.net.InetSocketAddress local = new java.net.InetSocketAddress("10.0.0.1", 67);
		java.net.InetSocketAddress remote = new java.net.InetSocketAddress("10.0.0.1", 68);
		com.jagornet.dhcp.core.message.DhcpV4Message req = new com.jagornet.dhcp.core.message.DhcpV4Message(local, remote);
		DummyV4Processor processor = new DummyV4Processor(req, local.getAddress());

		byte[] blockedMac = Util.fromHexString("001122334499");
		byte[] allowedMac = Util.fromHexString("001122334488");

		assertTrue(processor.testIsIgnoredMac(blockedMac));
		assertFalse(processor.testIsIgnoredMac(allowedMac));
	}
}
