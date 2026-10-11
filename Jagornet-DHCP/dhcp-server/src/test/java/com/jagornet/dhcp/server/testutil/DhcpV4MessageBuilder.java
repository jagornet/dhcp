/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.testutil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.DhcpUnknownOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ClientIdOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4HostnameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ParamRequestOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4VendorClassOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

/**
 * Fluent builder for constructing {@link DhcpV4Message} instances for testing.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4MessageBuilder {

	private static final Random RANDOM = new Random();

	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	private short op = DhcpConstants.V4_OP_REQUEST;
	private short htype = 1; // Ethernet
	private short hlen = 6;
	private short hops = 0;
	private long transactionId = RANDOM.nextInt(Integer.MAX_VALUE);
	private int secs = 0;
	private int flags = 0;
	private InetAddress ciAddr = DhcpConstants.ZEROADDR_V4;
	private InetAddress yiAddr = DhcpConstants.ZEROADDR_V4;
	private InetAddress siAddr = DhcpConstants.ZEROADDR_V4;
	private InetAddress giAddr = DhcpConstants.ZEROADDR_V4;
	private byte[] chAddr = new byte[] { (byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55 };
	private String sName;
	private String file;
	private boolean unicast = false;

	private Short messageType;
	private final java.util.Map<Integer, DhcpOption> options = new java.util.HashMap<>();

	public DhcpV4MessageBuilder() {
		try {
			this.localAddress = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 67);
			this.remoteAddress = new InetSocketAddress(InetAddress.getByName("255.255.255.255"), 68);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public static DhcpV4MessageBuilder discover() {
		return new DhcpV4MessageBuilder().withMessageType((short) DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
	}

	public static DhcpV4MessageBuilder request() {
		return new DhcpV4MessageBuilder().withMessageType((short) DhcpConstants.V4MESSAGE_TYPE_REQUEST);
	}

	public static DhcpV4MessageBuilder release() {
		return new DhcpV4MessageBuilder().withMessageType((short) DhcpConstants.V4MESSAGE_TYPE_RELEASE);
	}

	public static DhcpV4MessageBuilder inform() {
		return new DhcpV4MessageBuilder().withMessageType((short) DhcpConstants.V4MESSAGE_TYPE_INFORM);
	}

	public static DhcpV4MessageBuilder decline() {
		return new DhcpV4MessageBuilder().withMessageType((short) DhcpConstants.V4MESSAGE_TYPE_DECLINE);
	}

	public static DhcpV4MessageBuilder ofType(short msgType) {
		return new DhcpV4MessageBuilder().withMessageType(msgType);
	}

	public DhcpV4MessageBuilder withMessageType(short msgType) {
		this.messageType = msgType;
		return this;
	}

	public DhcpV4MessageBuilder withOp(short op) {
		this.op = op;
		return this;
	}

	public DhcpV4MessageBuilder withTransactionId(long transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public DhcpV4MessageBuilder withHops(short hops) {
		this.hops = hops;
		return this;
	}

	public DhcpV4MessageBuilder withMac(String mac) {
		this.chAddr = Util.fromHexString(DhcpV4MessageBuilder.cleanHex(mac));
		this.hlen = (short) chAddr.length;
		return this;
	}

	public DhcpV4MessageBuilder withMac(byte[] mac) {
		this.chAddr = mac;
		this.hlen = (short) mac.length;
		return this;
	}

	public DhcpV4MessageBuilder withCiAddr(String ip) {
		try {
			this.ciAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid ciAddr IP: " + ip, e);
		}
		return this;
	}

	public DhcpV4MessageBuilder withCiAddr(InetAddress ip) {
		this.ciAddr = ip;
		return this;
	}

	public DhcpV4MessageBuilder withYiAddr(String ip) {
		try {
			this.yiAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid yiAddr IP: " + ip, e);
		}
		return this;
	}

	public DhcpV4MessageBuilder withYiAddr(InetAddress ip) {
		this.yiAddr = ip;
		return this;
	}

	public DhcpV4MessageBuilder withSiAddr(String ip) {
		try {
			this.siAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid siAddr IP: " + ip, e);
		}
		return this;
	}

	public DhcpV4MessageBuilder withSiAddr(InetAddress ip) {
		this.siAddr = ip;
		return this;
	}

	public DhcpV4MessageBuilder withGiAddr(String ip) {
		try {
			this.giAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid giAddr IP: " + ip, e);
		}
		return this;
	}

	public DhcpV4MessageBuilder withGiAddr(InetAddress ip) {
		this.giAddr = ip;
		return this;
	}

	public DhcpV4MessageBuilder withLocalAddress(InetSocketAddress addr) {
		this.localAddress = addr;
		return this;
	}

	public DhcpV4MessageBuilder withLocalAddress(String ip, int port) {
		try {
			this.localAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid localAddress IP: " + ip, e);
		}
		return this;
	}

	public DhcpV4MessageBuilder withRemoteAddress(InetSocketAddress addr) {
		this.remoteAddress = addr;
		return this;
	}

	public DhcpV4MessageBuilder withRemoteAddress(String ip, int port) {
		try {
			this.remoteAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid remoteAddress IP: " + ip, e);
		}
		return this;
	}

	public DhcpV4MessageBuilder withUnicast(boolean unicast) {
		this.unicast = unicast;
		return this;
	}

	public DhcpV4MessageBuilder withBroadcast(boolean broadcast) {
		if (broadcast) {
			this.flags |= 0x8000;
		} else {
			this.flags &= ~0x8000;
		}
		return this;
	}

	public DhcpV4MessageBuilder withOption(DhcpOption option) {
		if (option != null) {
			this.options.put(option.getCode(), option);
		}
		return this;
	}

	public DhcpV4MessageBuilder withVendorClass(String vendorClass) {
		DhcpV4VendorClassOption option = new DhcpV4VendorClassOption();
		option.setOpaqueData(new BaseOpaqueData(vendorClass));
		return withOption(option);
	}

	public DhcpV4MessageBuilder withVendorClassHex(String hex) {
		DhcpV4VendorClassOption option = new DhcpV4VendorClassOption();
		option.setOpaqueData(new BaseOpaqueData(Util.fromHexString(cleanHex(hex))));
		return withOption(option);
	}

	public DhcpV4MessageBuilder withClientId(String clientId) {
		DhcpV4ClientIdOption option = new DhcpV4ClientIdOption();
		option.setOpaqueData(new BaseOpaqueData(clientId));
		return withOption(option);
	}

	public DhcpV4MessageBuilder withClientIdHex(String hex) {
		DhcpV4ClientIdOption option = new DhcpV4ClientIdOption();
		option.setOpaqueData(new BaseOpaqueData(Util.fromHexString(cleanHex(hex))));
		return withOption(option);
	}

	public DhcpV4MessageBuilder withRequestedIp(String ip) {
		try {
			DhcpV4RequestedIpAddressOption option = new DhcpV4RequestedIpAddressOption();
			option.setIpAddress(ip);
			return withOption(option);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid requested IP: " + ip, e);
		}
	}

	public DhcpV4MessageBuilder withClientFqdn(String fqdn, boolean serverUpdate, boolean noUpdate) {
		com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption option = 
				new com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption(fqdn);
		option.setUpdateABit(serverUpdate);
		option.setNoUpdateBit(noUpdate);
		option.setEncodingBit(true);
		return withOption(option);
	}

	public DhcpV4MessageBuilder withHostname(String hostname) {
		com.jagornet.dhcp.core.option.v4.DhcpV4HostnameOption option = 
				new com.jagornet.dhcp.core.option.v4.DhcpV4HostnameOption();
		option.setString(hostname);
		return withOption(option);
	}

	public DhcpV4MessageBuilder withServerId(String ip) {
		try {
			DhcpV4ServerIdOption option = new DhcpV4ServerIdOption();
			option.setIpAddress(ip);
			return withOption(option);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid serverId IP: " + ip, e);
		}
	}

	public DhcpV4MessageBuilder withHostName(String hostName) {
		DhcpV4HostnameOption option = new DhcpV4HostnameOption();
		option.setString(hostName);
		return withOption(option);
	}

	public DhcpV4MessageBuilder withRapidCommit() {
		DhcpUnknownOption option = new DhcpUnknownOption();
		option.setCode(DhcpConstants.V4OPTION_RAPID_COMMIT);
		option.setV4(true);
		return withOption(option);
	}

	public DhcpV4MessageBuilder withRequestedOptions(int... codes) {
		DhcpV4ParamRequestOption option = new DhcpV4ParamRequestOption();
		for (int code : codes) {
			option.addUnsignedByte((short) code);
		}
		return withOption(option);
	}

	public DhcpV4Message build() {
		DhcpV4Message msg = new DhcpV4Message(localAddress, remoteAddress);
		msg.setOp(op);
		msg.setHtype(htype);
		msg.setHlen(hlen);
		msg.setHops(hops);
		msg.setTransactionId(transactionId);
		msg.setSecs(secs);
		msg.setFlags(flags);
		msg.setCiAddr(ciAddr);
		msg.setYiAddr(yiAddr);
		msg.setSiAddr(siAddr);
		msg.setGiAddr(giAddr);
		msg.setChAddr(chAddr);
		msg.setsName(sName);
		msg.setFile(file);
		msg.setUnicast(unicast);

		if (messageType != null) {
			msg.setMessageType(messageType);
		}
		for (DhcpOption opt : options.values()) {
			msg.putDhcpOption(opt);
		}
		return msg;
	}

	private static String cleanHex(String hex) {
		if (hex == null) return "";
		return hex.replace(":", "").replace("-", "").replace(".", "").trim();
	}
}
