/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import jakarta.xml.bind.JAXBException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinksType;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.Policy;
import com.jagornet.dhcp.server.config.xml.V4AddressPool;
import com.jagornet.dhcp.server.config.xml.V4AddressPoolsType;
import com.jagornet.dhcp.server.config.xml.V4ConfigOptionsType;
import com.jagornet.dhcp.server.config.xml.V4DomainNameOption;
import com.jagornet.dhcp.server.config.xml.V4DomainServersOption;
import com.jagornet.dhcp.server.config.xml.V4RoutersOption;
import com.jagornet.dhcp.server.config.xml.V4ServerIdOption;
import com.jagornet.dhcp.server.config.xml.V4SubnetMaskOption;
import com.jagornet.dhcp.server.config.xml.V6AddressPool;
import com.jagornet.dhcp.server.config.xml.V6AddressPoolsType;
import com.jagornet.dhcp.server.config.xml.V6ConfigOptionsType;
import com.jagornet.dhcp.server.config.xml.V6DnsServersOption;
import com.jagornet.dhcp.server.config.xml.V6DomainSearchListOption;
import com.jagornet.dhcp.server.config.xml.V6ServerIdOption;

public class GenerateTestConfig {

	protected Options options = new Options();
	protected CommandLineParser parser = new BasicParser();
	protected HelpFormatter formatter = new HelpFormatter();
	protected String filename;
	protected NetworkInterface networkInterface;
	protected InetAddress ipv4Address;
	protected InetAddress ipv6Address;
	protected String protocolVersion;
	protected boolean valid = false;

	public GenerateTestConfig(String[] args, boolean exitOnError) {
		setupOptions();

		if (!parseOptions(args)) {
			formatter = new HelpFormatter();
			String cliName = this.getClass().getName();
			formatter.printHelp(cliName, options);
			if (exitOnError) {
				System.exit(0);
			}
		} else {
			valid = true;
		}
	}

	public GenerateTestConfig(String[] args) {
		this(args, true);
	}

	public Options getOptions() {
		return options;
	}

	private void setupOptions() {
		Option filenameOption = new Option("f", "filename", true,
				"Output configuration filename [dhcpserver-test-config.xml");
		options.addOption(filenameOption);

		Option interfaceOption = new Option("i", "interface", true,
				"Network interface [default]");
		options.addOption(interfaceOption);

		Option versionOption = new Option("v", "protocol version", true,
				"Protocol Version (4|6|both)[both]");
		options.addOption(versionOption);

		Option helpOption = new Option("h", "help", false, "Show this help page.");
		options.addOption(helpOption);
		Option qhelpOption = new Option("?", false, "Show this help page.");
		options.addOption(qhelpOption);
	}

	private boolean parseOptions(String[] args) {
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("h") || cmd.hasOption("?") || cmd.hasOption("help")) {
				return false;
			}
			if (cmd.hasOption("f")) {
				filename = cmd.getOptionValue("f");
			} else {
				filename = "dhcpserver-test-config.xml";
			}
			String netIfName = null;
			if (cmd.hasOption("i")) {
				netIfName = cmd.getOptionValue("i");
				networkInterface = NetworkInterface.getByName(netIfName);
			} else {
				netIfName = "default";
				networkInterface = NetworkInterface.getNetworkInterfaces().nextElement();
			}

			if (networkInterface == null) {
				System.err.println("Network interface '" + netIfName + "' not found");
				return false;
			}

			String protoVersion = null;
			if (cmd.hasOption("v")) {
				protoVersion = cmd.getOptionValue("v");
			}
			if ((protoVersion == null) || ((!protoVersion.equalsIgnoreCase("both") &&
					!protoVersion.equals("4") && !protoVersion.equals("6")))) {
				protocolVersion = "both";
			} else {
				protocolVersion = protoVersion;
			}

			if ((protocolVersion.equals("4") || protocolVersion.equals("both"))) {
				Enumeration<InetAddress> ipAddrs = networkInterface.getInetAddresses();
				if (ipAddrs != null) {
					while (ipAddrs.hasMoreElements()) {
						InetAddress ipAddr = ipAddrs.nextElement();
						if ((ipAddr instanceof Inet4Address) &&
								!ipAddr.isLinkLocalAddress() &&
								!ipAddr.isLoopbackAddress()) {
							ipv4Address = ipAddr;
							break;
						}
					}
				}
				if (ipv4Address == null) {
					System.err.println("No IPv4 address found for interface: " + networkInterface.getName());
					return false;
				}
			}

			if ((protocolVersion.equals("6") || protocolVersion.equals("both"))) {
				Enumeration<InetAddress> ipAddrs = networkInterface.getInetAddresses();
				if (ipAddrs != null) {
					while (ipAddrs.hasMoreElements()) {
						InetAddress ipAddr = ipAddrs.nextElement();
						if ((ipAddr instanceof Inet6Address) &&
								!ipAddr.isLinkLocalAddress() &&
								!ipAddr.isLoopbackAddress()) {
							ipv6Address = ipAddr;
							break;
						}
					}
				}
				if (ipv6Address == null) {
					System.err.println("No IPv6 address found for interface: " + networkInterface.getName());
					return false;
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void generate() {
		if (!valid) {
			return;
		}
		try {
			DhcpServerConfig config = new DhcpServerConfig();

			PoliciesType policies = new PoliciesType();
			Policy policy = new Policy();
			policies.getPolicyList().add(policy);
			policy.setName("dhcp.ignoreSelfPackets");
			policy.setValue("false");
			config.setPolicies(policies);

			LinksType links = new LinksType();

			if (ipv4Address != null) {

				V4ServerIdOption v4ServerId = new V4ServerIdOption();
				String myIp = ipv4Address.getHostAddress();
				v4ServerId.setIpAddress(myIp);
				config.setV4ServerIdOption(v4ServerId);

				Link v4Link = new Link();
				links.getLinkList().add(v4Link);
				v4Link.setName("Test IPv4 Client Link");
				// assume the client is on a /24 size IPv4 subnet
				int p = myIp.lastIndexOf('.');
				String myNet = myIp.substring(0, p + 1);
				v4Link.setAddress(myNet.concat("0/24"));
				V4ConfigOptionsType v4Options = new V4ConfigOptionsType();
				v4Link.setV4ConfigOptions(v4Options);
				V4SubnetMaskOption smo = new V4SubnetMaskOption();
				smo.setIpAddress("255.255.255.0");
				v4Options.setV4SubnetMaskOption(smo);
				// assume the router is at .1
				V4RoutersOption ro = new V4RoutersOption();
				ro.getIpAddressList().add(myNet.concat("1"));
				v4Options.setV4RoutersOption(ro);
				V4DomainServersOption dso4 = new V4DomainServersOption();
				dso4.getIpAddressList().add("10.0.0.1");
				v4Options.setV4DomainServersOption(dso4);
				V4DomainNameOption dno4 = new V4DomainNameOption();
				dno4.setDomainName("test.jagornet.com.");
				v4Options.setV4DomainNameOption(dno4);

				V4AddressPoolsType v4Pools = new V4AddressPoolsType();
				v4Link.setV4AddrPools(v4Pools);
				// create a pool of 50 addresses at the end of subnet
				V4AddressPool v4pool = new V4AddressPool();
				v4pool.setRange(myNet + "100-" + myNet + "199");
				v4Pools.getPoolList().add(v4pool);
			}

			if (ipv6Address != null) {

				V6ServerIdOption serverId = new V6ServerIdOption();
				serverId.setOpaqueData(OpaqueDataUtil.generateDUID_LLT());
				config.setV6ServerIdOption(serverId);

				Link v6Link = new Link();
				links.getLinkList().add(v6Link);
				v6Link.setName("Test IPv6 Client Link");
				String myIf = networkInterface.getName();
				v6Link.setInterface(myIf);
				V6ConfigOptionsType v6Options = new V6ConfigOptionsType();
				v6Link.setV6MsgConfigOptions(v6Options);
				V6DnsServersOption dso6 = new V6DnsServersOption();
				dso6.getIpAddressList().add("2001:db8:1::1");
				v6Options.setV6DnsServersOption(dso6);
				V6DomainSearchListOption dno6 = new V6DomainSearchListOption();
				dno6.getDomainNameList().add("test.jagornet.com.");
				v6Options.setV6DomainSearchListOption(dno6);

				V6AddressPoolsType v6Pools = new V6AddressPoolsType();
				v6Link.setV6NaAddrPools(v6Pools);
				V6AddressPool v6Pool = new V6AddressPool();
				v6Pool.setRange("2001:db8:1::/64");
				v6Pools.getPoolList().add(v6Pool);
			}

			config.setLinks(links);
			DhcpServerConfiguration.saveConfig(config, filename);

			System.out.println("Generated test configuration: " + filename);
			if (networkInterface != null) {
				System.out.println("  Interface: " + networkInterface.getName());
			}
			if (ipv4Address != null) {
				int p = ipv4Address.getHostAddress().lastIndexOf('.');
				String myNet = ipv4Address.getHostAddress().substring(0, p + 1);
				System.out.println("  IPv4 Link: " + myNet + "0/24 (server IP: " + ipv4Address.getHostAddress() +
						", pool: " + myNet + "100-" + myNet + "199)");
			}
			if (ipv6Address != null) {
				System.out.println("  IPv6 Link: 2001:db8:1::/64 (interface: " + networkInterface.getName() + ")");
			}

		} catch (Exception e) {
			System.err.println("Failed to generate test configuration: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		GenerateTestConfig testConfig = new GenerateTestConfig(args);
		testConfig.generate();

	}

}
