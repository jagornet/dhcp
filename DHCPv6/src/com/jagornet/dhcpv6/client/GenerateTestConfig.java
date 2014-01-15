package com.jagornet.dhcpv6.client;

import java.io.IOException;
import java.net.Inet4Address;
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

import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.ConfigOptionsType;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinksType;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.Policy;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.V4AddressPoolsType;
import com.jagornet.dhcpv6.xml.V4ConfigOptionsType;
import com.jagornet.dhcpv6.xml.V4ServerIdOption;

public class GenerateTestConfig {

    protected Options options = new Options();
    protected CommandLineParser parser = new BasicParser();
    protected HelpFormatter formatter = new HelpFormatter();
    protected String filename;
    protected NetworkInterface networkInterface;
    protected InetAddress ipv4Address;
    
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
				"Output configuration filename [dhcpv6server-test-config.xml");
		options.addOption(filenameOption);

		Option interfaceOption = new Option("i", "interface", true,
				"Network interface [default]");
		options.addOption(interfaceOption);
        
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
				filename = "dhcpv6server-test-config.xml";
			}
			if (cmd.hasOption("i")) {
				networkInterface = NetworkInterface.getByName(cmd.getOptionValue("i"));
			}
			else {
				networkInterface = NetworkInterface.getNetworkInterfaces().nextElement();
			}
			
			Enumeration<InetAddress> ipAddrs = networkInterface.getInetAddresses();
			while (ipAddrs.hasMoreElements()) {
				InetAddress ipAddr = ipAddrs.nextElement();
				if ((ipAddr instanceof Inet4Address) && 
						!ipAddr.isLinkLocalAddress() &&
						!ipAddr.isLoopbackAddress()) {
					ipv4Address = ipAddr;
					return true;
				}
			}
			System.err.println("No IPv4 address found for interface: " + networkInterface.getName());
			return false;
		}
        catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void generate() {
		try {
			DhcpV6ServerConfig config = DhcpV6ServerConfig.Factory.newInstance();
			
			ServerIdOption serverId = ServerIdOption.Factory.newInstance();
			serverId.setOpaqueData(OpaqueDataUtil.generateDUID_LLT());
			config.setServerIdOption(serverId);
			
			V4ServerIdOption v4ServerId = V4ServerIdOption.Factory.newInstance();
			String myIp = ipv4Address.getHostAddress();
			v4ServerId.setIpAddress(myIp);
			config.setV4ServerIdOption(v4ServerId);

			PoliciesType policies = PoliciesType.Factory.newInstance();
			Policy policy = policies.addNewPolicy();
			policy.setName("dhcp.ignoreSelfPackets");
			policy.setValue("false");
			config.setPolicies(policies);
			
			LinksType links = LinksType.Factory.newInstance();
			
			Link v4Link = links.addNewLink();
			v4Link.setName("Test IPv4 Client Link");
			// assume the client is on a /24 size IPv4 subnet
			int p = myIp.lastIndexOf('.');
			String myNet = myIp.substring(0, p+1);
			v4Link.setAddress(myNet.concat("0/24"));
			V4ConfigOptionsType v4Options = v4Link.addNewV4ConfigOptions();
			v4Options.addNewV4SubnetMaskOption().setIpAddress("255.255.255.0");
			// assume the router is at .1
			v4Options.addNewV4RoutersOption().addIpAddress(myNet.concat("1"));
			v4Options.addNewV4DomainServersOption().addIpAddress("10.0.0.1");
			v4Options.addNewV4DomainNameOption().setString("jagornet.test.com");
			V4AddressPoolsType v4Pools = v4Link.addNewV4AddrPools();
			// create a pool of 50 addresses at the end of subnet
			v4Pools.addNewPool().setRange(myNet + "200-" + myNet + "250");
			
			Link v6Link = links.addNewLink();
			v6Link.setName("Test IPv6 Client Link");
			String myIf = networkInterface.getName();
			v6Link.setInterface(myIf);
			ConfigOptionsType v6Options = v6Link.addNewMsgConfigOptions();
			v6Options.addNewDnsServersOption().addIpAddress("2001:db8:1::1");
			v6Options.addNewDomainSearchListOption().addDomainName("jagornet.test.com");
			AddressPoolsType v6Pools = v6Link.addNewNaAddrPools();
			v6Pools.addNewPool().setRange("2001:db8:1::/64");
			
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
