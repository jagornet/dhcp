/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenerateTestConfig.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.config;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

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
    
    public GenerateTestConfig(String args[]) {
    	
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
            formatter.printHelp(cliName, options);
            System.exit(0);
        }
    	
    }
    
	private void setupOptions()
    {
		Option filenameOption = new Option("f", "filename", true, 
				"Output configuration filename [dhcpserver-test-config.xml");
		options.addOption(filenameOption);

		Option interfaceOption = new Option("i", "interface", true,
				"Network interface [default]");
		options.addOption(interfaceOption);

		Option versionOption = new Option("v", "protocol version", true,
				"Protocol Version (4|6|both)[both]");
		options.addOption(versionOption);
        
        Option helpOption = new Option("?", "help", false, "Show this help page.");
        
        options.addOption(helpOption);
    }

	private boolean parseOptions(String args[]) {
        try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("?")) {
			    return false;
			}
			if (cmd.hasOption("f")) {
				filename = cmd.getOptionValue("f");
			}
			else {
				filename = "dhcpserver-test-config.xml";
			}
			String netIfName = null;
			if (cmd.hasOption("i")) {
				netIfName = cmd.getOptionValue("i");
				networkInterface = NetworkInterface.getByName(netIfName);
			}
			else {
				netIfName = "default";
				networkInterface = NetworkInterface.getNetworkInterfaces().nextElement();
			}
			
			if (networkInterface == null) {
				System.err.println("Network interface '" + netIfName + "' not found");
			}
			
			String protoVersion = null;
			if (cmd.hasOption("v")) {
				protoVersion = cmd.getOptionValue("v");
			}
			if ((protoVersion == null) || ((!protoVersion.equalsIgnoreCase("both") &&
					!protoVersion.equals("4") && !protoVersion.equals("6")))) {
				protocolVersion = "both";
			}
			else {
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
		}
        catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void generate() {
		try {
			DhcpServerConfig config = new DhcpServerConfig();

			PoliciesType policies = new PoliciesType();
			Policy policy = new Policy();
			policies.getPolicies().add(policy);
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
				links.getLinks().add(v4Link);
				v4Link.setName("Test IPv4 Client Link");
				// assume the client is on a /24 size IPv4 subnet
				int p = myIp.lastIndexOf('.');
				String myNet = myIp.substring(0, p+1);
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
				dno4.setString("test.jagornet.com.");
				v4Options.setV4DomainNameOption(dno4);
				
				V4AddressPoolsType v4Pools = new V4AddressPoolsType();
				v4Link.setV4AddrPools(v4Pools);
				// create a pool of 50 addresses at the end of subnet
				V4AddressPool v4pool = new V4AddressPool();
				v4pool.setRange(myNet + "200-" + myNet + "250");
				v4Pools.getPool().add(v4pool);
			}
			
			if (ipv6Address != null) {
				
				V6ServerIdOption serverId = new V6ServerIdOption();
				serverId.setOpaqueData(OpaqueDataUtil.generateDUID_LLT());
				config.setV6ServerIdOption(serverId);

				Link v6Link = new Link();
				links.getLinks().add(v6Link);
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
				v6Pools.getPool().add(v6Pool);
			}
			
			config.setLinks(links);
			DhcpServerConfiguration.saveConfig(config, filename);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
