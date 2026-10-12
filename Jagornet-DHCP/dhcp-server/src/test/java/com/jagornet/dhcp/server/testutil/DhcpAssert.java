/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

/**
 * Fluent assertion helper for validating DHCPv4 and DHCPv6 messages and option maps in test cases.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpAssert {

	public static V4Assert assertThat(DhcpV4Message message) {
		return new V4Assert(message);
	}

	public static V6Assert assertThat(DhcpV6Message message) {
		return new V6Assert(message);
	}

	public static OptionMapAssert assertThat(Map<Integer, DhcpOption> options) {
		return new OptionMapAssert(options);
	}

	public static class OptionMapAssert {
		private final Map<Integer, DhcpOption> options;

		public OptionMapAssert(Map<Integer, DhcpOption> options) {
			this.options = options;
		}

		public OptionMapAssert isNotNull() {
			assertNotNull("Expected options map to not be null", options);
			return this;
		}

		public OptionMapAssert hasOption(int optionCode) {
			isNotNull();
			assertTrue("Expected option code " + optionCode + " to be present in map",
					options.containsKey(optionCode));
			return this;
		}

		public OptionMapAssert doesNotHaveOption(int optionCode) {
			if (options != null) {
				assertFalse("Expected option code " + optionCode + " to NOT be present in map",
						options.containsKey(optionCode));
			}
			return this;
		}

		public <T extends DhcpOption> T getOption(int optionCode, Class<T> expectedType) {
			isNotNull();
			DhcpOption opt = options.get(optionCode);
			assertNotNull("Option " + optionCode + " is missing from map", opt);
			assertTrue("Option " + optionCode + " is expected to be " + expectedType.getSimpleName() + 
					" but was " + opt.getClass().getSimpleName(), expectedType.isInstance(opt));
			return expectedType.cast(opt);
		}

		public DhcpOption getOption(int optionCode) {
			isNotNull();
			DhcpOption opt = options.get(optionCode);
			assertNotNull("Option " + optionCode + " is missing from map", opt);
			return opt;
		}
	}

	public static class V4Assert {
		private final DhcpV4Message message;

		public V4Assert(DhcpV4Message message) {
			this.message = message;
		}

		public V4Assert isNotNull() {
			assertNotNull("Expected DhcpV4Message to not be null", message);
			return this;
		}

		public V4Assert isOffer() {
			isNotNull();
			assertEquals("Expected DHCPv4 OFFER message type", 
					DhcpConstants.V4MESSAGE_TYPE_OFFER, message.getMessageType());
			return this;
		}

		public V4Assert isAck() {
			isNotNull();
			assertEquals("Expected DHCPv4 ACK message type", 
					DhcpConstants.V4MESSAGE_TYPE_ACK, message.getMessageType());
			return this;
		}

		public V4Assert isNak() {
			isNotNull();
			assertEquals("Expected DHCPv4 NAK message type", 
					DhcpConstants.V4MESSAGE_TYPE_NAK, message.getMessageType());
			return this;
		}

		public V4Assert hasMessageType(short type) {
			isNotNull();
			assertEquals("Expected message type: " + type, type, message.getMessageType());
			return this;
		}

		public V4Assert hasTransactionId(long expectedXid) {
			isNotNull();
			assertEquals("Transaction ID mismatch", expectedXid, message.getTransactionId());
			return this;
		}

		public V4Assert hasChAddr(String expectedMac) {
			isNotNull();
			byte[] expected = Util.fromHexString(cleanHex(expectedMac));
			return hasChAddr(expected);
		}

		public V4Assert hasChAddr(byte[] expectedMac) {
			isNotNull();
			assertTrue("Client MAC mismatch. Expected: " + Util.toHexString(expectedMac) + 
					", Got: " + Util.toHexString(message.getChAddr()),
					Arrays.equals(expectedMac, message.getChAddr()));
			return this;
		}

		public V4Assert hasYiAddr(String expectedIp) {
			isNotNull();
			try {
				InetAddress expected = InetAddress.getByName(expectedIp);
				assertEquals("Your IP (yiaddr) mismatch", expected, message.getYiAddr());
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("Invalid expected IP: " + expectedIp, e);
			}
			return this;
		}

		public V4Assert hasYiAddrInRange(String minIp, String maxIp) {
			isNotNull();
			assertNotNull("yiaddr is null", message.getYiAddr());
			assertIpInRange(message.getYiAddr(), minIp, maxIp);
			return this;
		}

		public V4Assert hasOption(int optionCode) {
			isNotNull();
			assertTrue("Expected option code " + optionCode + " to be present", 
					message.getDhcpOption(optionCode) != null);
			return this;
		}

		public V4Assert doesNotHaveOption(int optionCode) {
			isNotNull();
			assertFalse("Expected option code " + optionCode + " to NOT be present", 
					message.getDhcpOption(optionCode) != null);
			return this;
		}

		public DhcpOption getOption(int optionCode) {
			isNotNull();
			DhcpOption opt = message.getDhcpOption(optionCode);
			assertNotNull("Option code " + optionCode + " is missing", opt);
			return opt;
		}
	}

	public static class V6Assert {
		private final DhcpV6Message message;

		public V6Assert(DhcpV6Message message) {
			this.message = message;
		}

		public V6Assert isNotNull() {
			assertNotNull("Expected DhcpV6Message to not be null", message);
			return this;
		}

		public V6Assert isAdvertise() {
			isNotNull();
			assertEquals("Expected DHCPv6 ADVERTISE message type", 
					DhcpConstants.V6MESSAGE_TYPE_ADVERTISE, message.getMessageType());
			return this;
		}

		public V6Assert isReply() {
			isNotNull();
			assertEquals("Expected DHCPv6 REPLY message type", 
					DhcpConstants.V6MESSAGE_TYPE_REPLY, message.getMessageType());
			return this;
		}

		public V6Assert hasMessageType(short type) {
			isNotNull();
			assertEquals("Expected message type: " + type, type, message.getMessageType());
			return this;
		}

		public V6Assert hasTransactionId(int expectedXid) {
			isNotNull();
			assertEquals("Transaction ID mismatch", expectedXid, message.getTransactionId());
			return this;
		}

		public V6Assert hasOption(int optionCode) {
			isNotNull();
			assertTrue("Expected option code " + optionCode + " to be present", 
					message.hasOption(optionCode));
			return this;
		}

		public V6Assert doesNotHaveOption(int optionCode) {
			isNotNull();
			assertFalse("Expected option code " + optionCode + " to NOT be present", 
					message.hasOption(optionCode));
			return this;
		}

		public DhcpOption getOption(int optionCode) {
			isNotNull();
			DhcpOption opt = message.getDhcpOption(optionCode);
			assertNotNull("Option code " + optionCode + " is missing", opt);
			return opt;
		}

		public V6Assert hasIaNa() {
			isNotNull();
			List<DhcpV6IaNaOption> iaNas = message.getIaNaOptions();
			assertTrue("Expected at least one IA_NA option", (iaNas != null) && !iaNas.isEmpty());
			return this;
		}

		public V6Assert hasIaNaAddressInRange(String minIp, String maxIp) {
			isNotNull();
			hasIaNa();
			DhcpV6IaNaOption iaNa = message.getIaNaOptions().get(0);
			List<DhcpV6IaAddrOption> addrs = iaNa.getIaAddrOptions();
			assertTrue("Expected at least one IA_ADDR in IA_NA", (addrs != null) && !addrs.isEmpty());
			String actualAddr = addrs.get(0).getIpAddress();
			assertNotNull("IA_ADDR IP address is null", actualAddr);
			assertIpInRange(actualAddr, minIp, maxIp);
			return this;
		}
	}

	public static void assertIpInRange(String actualIp, String minIp, String maxIp) {
		try {
			assertIpInRange(InetAddress.getByName(actualIp), minIp, maxIp);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Failed to parse actual IP: " + actualIp, e);
		}
	}

	public static void assertIpInRange(InetAddress actual, String minIp, String maxIp) {
		try {
			BigInteger actualVal = new BigInteger(1, actual.getAddress());
			BigInteger minVal = new BigInteger(1, InetAddress.getByName(minIp).getAddress());
			BigInteger maxVal = new BigInteger(1, InetAddress.getByName(maxIp).getAddress());

			assertTrue("IP " + actual.getHostAddress() + " must be >= " + minIp, 
					actualVal.compareTo(minVal) >= 0);
			assertTrue("IP " + actual.getHostAddress() + " must be <= " + maxIp, 
					actualVal.compareTo(maxVal) <= 0);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Failed to parse IP range: " + minIp + " - " + maxIp, e);
		}
	}

	private static String cleanHex(String hex) {
		if (hex == null) return "";
		return hex.replace(":", "").replace("-", "").replace(".", "").trim();
	}
}
