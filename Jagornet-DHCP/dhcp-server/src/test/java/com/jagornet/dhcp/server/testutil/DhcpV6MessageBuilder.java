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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6OptionRequestOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6RapidCommitOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6UserClassOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6VendorClassOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

/**
 * Fluent builder for constructing {@link DhcpV6Message} instances for testing.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6MessageBuilder {

	private static final Random RANDOM = new Random();

	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	private short messageType = DhcpConstants.V6MESSAGE_TYPE_SOLICIT;
	private int transactionId = RANDOM.nextInt(0x00FFFFFF); // 3-byte xid
	private boolean unicast = false;

	private final java.util.Map<Integer, DhcpOption> options = new java.util.HashMap<>();
	private final List<DhcpV6IaNaOption> iaNaOptions = new ArrayList<>();
	private final List<DhcpV6IaTaOption> iaTaOptions = new ArrayList<>();
	private final List<DhcpV6IaPdOption> iaPdOptions = new ArrayList<>();

	public DhcpV6MessageBuilder() {
		try {
			this.localAddress = new InetSocketAddress(InetAddress.getByName("::"), 547);
			this.remoteAddress = new InetSocketAddress(InetAddress.getByName("fe80::1"), 546);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public static DhcpV6MessageBuilder solicit() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_SOLICIT);
	}

	public static DhcpV6MessageBuilder request() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_REQUEST);
	}

	public static DhcpV6MessageBuilder confirm() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_CONFIRM);
	}

	public static DhcpV6MessageBuilder renew() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_RENEW);
	}

	public static DhcpV6MessageBuilder rebind() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_REBIND);
	}

	public static DhcpV6MessageBuilder release() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_RELEASE);
	}

	public static DhcpV6MessageBuilder decline() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_DECLINE);
	}

	public static DhcpV6MessageBuilder informationRequest() {
		return new DhcpV6MessageBuilder().withMessageType((short) DhcpConstants.V6MESSAGE_TYPE_INFO_REQUEST);
	}

	public static DhcpV6MessageBuilder ofType(short msgType) {
		return new DhcpV6MessageBuilder().withMessageType(msgType);
	}

	public DhcpV6MessageBuilder withMessageType(short msgType) {
		this.messageType = msgType;
		return this;
	}

	public DhcpV6MessageBuilder withTransactionId(int transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public DhcpV6MessageBuilder withLocalAddress(InetSocketAddress addr) {
		this.localAddress = addr;
		return this;
	}

	public DhcpV6MessageBuilder withLocalAddress(String ip, int port) {
		try {
			this.localAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid localAddress IP: " + ip, e);
		}
		return this;
	}

	public DhcpV6MessageBuilder withRemoteAddress(InetSocketAddress addr) {
		this.remoteAddress = addr;
		return this;
	}

	public DhcpV6MessageBuilder withRemoteAddress(String ip, int port) {
		try {
			this.remoteAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid remoteAddress IP: " + ip, e);
		}
		return this;
	}

	public DhcpV6MessageBuilder withUnicast(boolean unicast) {
		this.unicast = unicast;
		return this;
	}

	public DhcpV6MessageBuilder withOption(DhcpOption option) {
		if (option != null) {
			this.options.put(option.getCode(), option);
		}
		return this;
	}

	public DhcpV6MessageBuilder withDuid(String hexDuid) {
		return withClientId(hexDuid);
	}

	public DhcpV6MessageBuilder withClientId(String hexDuid) {
		byte[] duidBytes = Util.fromHexString(cleanHex(hexDuid));
		return withClientId(duidBytes);
	}

	public DhcpV6MessageBuilder withClientIdHex(String hexDuid) {
		return withClientId(hexDuid);
	}

	public DhcpV6MessageBuilder withClientId(byte[] duidBytes) {
		DhcpV6ClientIdOption option = new DhcpV6ClientIdOption();
		BaseOpaqueData opaque = new BaseOpaqueData();
		opaque.setHex(duidBytes);
		option.setOpaqueData(opaque);
		return withOption(option);
	}

	public DhcpV6MessageBuilder withServerId(String hexDuid) {
		byte[] duidBytes = Util.fromHexString(cleanHex(hexDuid));
		return withServerId(duidBytes);
	}

	public DhcpV6MessageBuilder withServerId(byte[] duidBytes) {
		DhcpV6ServerIdOption option = new DhcpV6ServerIdOption();
		BaseOpaqueData opaque = new BaseOpaqueData();
		opaque.setHex(duidBytes);
		option.setOpaqueData(opaque);
		return withOption(option);
	}

	public DhcpV6MessageBuilder withServerId(BaseOpaqueData opaque) {
		if (opaque != null) {
			return withServerId(opaque.getHex());
		}
		return this;
	}

	public DhcpV6MessageBuilder withServerId(DhcpV6ServerIdOption option) {
		return withOption(option);
	}

	public DhcpV6MessageBuilder withIaNa(long iaId) {
		return withIaNa(iaId, 0, 0);
	}

	public DhcpV6MessageBuilder withIaNa(long iaId, long t1, long t2) {
		DhcpV6IaNaOption iaNa = new DhcpV6IaNaOption(iaId, t1, t2);
		this.iaNaOptions.add(iaNa);
		return this;
	}

	public DhcpV6MessageBuilder withIaAddr(String ip) {
		DhcpV6IaAddrOption iaAddr = new DhcpV6IaAddrOption();
		iaAddr.setIpAddress(ip);
		if (this.iaNaOptions.isEmpty()) {
			withIaNa(1);
		}
		this.iaNaOptions.get(0).getIaAddrOptions().add(iaAddr);
		return this;
	}

	public DhcpV6MessageBuilder withIaNaWithAddr(long iaId, String ip) {
		withIaNa(iaId);
		return withIaAddr(ip);
	}

	public DhcpV6MessageBuilder withRapidCommit() {
		return withOption(new DhcpV6RapidCommitOption());
	}

	public DhcpV6MessageBuilder withIaTa(long iaId) {
		DhcpV6IaTaOption iaTa = new DhcpV6IaTaOption();
		iaTa.setIaId(iaId);
		this.iaTaOptions.add(iaTa);
		return this;
	}

	public DhcpV6MessageBuilder withIaPd(long iaId) {
		DhcpV6IaPdOption iaPd = new DhcpV6IaPdOption();
		iaPd.setIaId(iaId);
		this.iaPdOptions.add(iaPd);
		return this;
	}

	public DhcpV6MessageBuilder withUserClass(String userClass) {
		DhcpV6UserClassOption option = new DhcpV6UserClassOption();
		option.addOpaqueData(new BaseOpaqueData(userClass));
		return withOption(option);
	}

	public DhcpV6MessageBuilder withUserClassHex(String hex) {
		DhcpV6UserClassOption option = new DhcpV6UserClassOption();
		option.addOpaqueData(new BaseOpaqueData(Util.fromHexString(cleanHex(hex))));
		return withOption(option);
	}

	public DhcpV6MessageBuilder withVendorClass(long enterpriseNumber, String vendorData) {
		DhcpV6VendorClassOption option = new DhcpV6VendorClassOption();
		option.setEnterpriseNumber(enterpriseNumber);
		option.addOpaqueData(new BaseOpaqueData(vendorData));
		return withOption(option);
	}

	public DhcpV6MessageBuilder withVendorClassHex(long enterpriseNumber, String hex) {
		DhcpV6VendorClassOption option = new DhcpV6VendorClassOption();
		option.setEnterpriseNumber(enterpriseNumber);
		option.addOpaqueData(new BaseOpaqueData(Util.fromHexString(cleanHex(hex))));
		return withOption(option);
	}

	public DhcpV6MessageBuilder withClientFqdn(String fqdn, boolean serverUpdate, boolean noUpdate) {
		com.jagornet.dhcp.core.option.v6.DhcpV6ClientFqdnOption option = 
				new com.jagornet.dhcp.core.option.v6.DhcpV6ClientFqdnOption(fqdn);
		option.setUpdateAaaaBit(serverUpdate);
		option.setNoUpdateBit(noUpdate);
		return withOption(option);
	}

	public DhcpV6MessageBuilder withRequestedOptions(int... codes) {
		DhcpV6OptionRequestOption option = new DhcpV6OptionRequestOption();
		for (int code : codes) {
			option.addUnsignedShort(code);
		}
		return withOption(option);
	}

	public DhcpV6Message build() {
		DhcpV6Message msg = new DhcpV6Message(localAddress, remoteAddress);
		msg.setMessageType(messageType);
		msg.setTransactionId(transactionId);
		msg.setUnicast(unicast);

		for (DhcpOption opt : options.values()) {
			msg.putDhcpOption(opt);
		}
		for (DhcpV6IaNaOption iaNa : iaNaOptions) {
			msg.addIaNaOption(iaNa);
		}
		for (DhcpV6IaTaOption iaTa : iaTaOptions) {
			msg.getIaTaOptions().add(iaTa);
		}
		for (DhcpV6IaPdOption iaPd : iaPdOptions) {
			msg.getIaPdOptions().add(iaPd);
		}
		return msg;
	}

	private static String cleanHex(String hex) {
		if (hex == null) return "";
		return hex.replace(":", "").replace("-", "").replace(".", "").trim();
	}
}
