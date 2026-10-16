/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.jagornet.dhcp.server.hostname;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.jagornet.dhcp.server.hostname.HostnameScrubber.ScrubResult;

/**
 * Unit tests for HostnameScrubber.
 * 
 * @author A. Gregory Rabil
 */
public class TestHostnameScrubber {

	private List<String> defaultStripDomains = Arrays.asList("local", "lan", "home");

	@Test
	public void testValidCleanHostname() {
		ScrubResult result = HostnameScrubber.scrub("workstation-01", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertFalse(result.isModified());
		assertEquals("workstation-01", result.getScrubbedName());
	}

	@Test
	public void testValidCleanFqdn() {
		ScrubResult result = HostnameScrubber.scrub("node-1.example.com", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertFalse(result.isModified());
		assertEquals("node-1.example.com", result.getScrubbedName());
	}

	@Test
	public void testInvalidCharactersReplacedWithHyphen() {
		ScrubResult result = HostnameScrubber.scrub("John's MacBook Pro! #4", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertTrue(result.isModified());
		assertEquals("john-s-macbook-pro-4", result.getScrubbedName());
	}

	@Test
	public void testUnderscoreReplacementWhenDisallowed() {
		ScrubResult result = HostnameScrubber.scrub("my_test_server", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertTrue(result.isModified());
		assertEquals("my-test-server", result.getScrubbedName());
	}

	@Test
	public void testUnderscoreAllowedWhenConfigured() {
		ScrubResult result = HostnameScrubber.scrub("my_test_server", true, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertEquals("my_test_server", result.getScrubbedName());
	}

	@Test
	public void testStripLocalSuffix() {
		ScrubResult result = HostnameScrubber.scrub("laptop.local", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertTrue(result.isModified());
		assertEquals("laptop", result.getScrubbedName());
	}

	@Test
	public void testStripLanSuffix() {
		ScrubResult result = HostnameScrubber.scrub("printer01.lan", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertTrue(result.isModified());
		assertEquals("printer01", result.getScrubbedName());
	}

	@Test
	public void testCollapseHyphensAndTrimEdges() {
		ScrubResult result = HostnameScrubber.scrub("--alpha---beta--", false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertTrue(result.isModified());
		assertEquals("alpha-beta", result.getScrubbedName());
	}

	@Test
	public void testLabelExceedingMaxLengthTruncated() {
		String longLabel = "a".repeat(70);
		ScrubResult result = HostnameScrubber.scrub(longLabel, false, "-", true, defaultStripDomains);
		assertTrue(result.isValid());
		assertTrue(result.isModified());
		assertEquals(63, result.getScrubbedName().length());
	}

	@Test
	public void testEmptyOrAllInvalidCharactersFails() {
		ScrubResult empty = HostnameScrubber.scrub("", false, "-", true, defaultStripDomains);
		assertFalse(empty.isValid());

		ScrubResult symbols = HostnameScrubber.scrub("$$$###@@@", false, "-", true, defaultStripDomains);
		assertFalse(symbols.isValid());

		ScrubResult onlyHyphens = HostnameScrubber.scrub("----", false, "-", true, defaultStripDomains);
		assertFalse(onlyHyphens.isValid());
	}
}
