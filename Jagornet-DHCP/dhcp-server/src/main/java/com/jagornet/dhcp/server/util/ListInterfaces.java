/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.util;

import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Command-line utility to inspect and display host network interfaces
 * as seen by the Java runtime (useful for -4b, -6m, gentestconfig -i,
 * and DHCPv6 link interface configuration, particularly on Windows).
 */
public class ListInterfaces {

	protected Options options = new Options();
	protected CommandLineParser parser = new BasicParser();
	protected HelpFormatter formatter = new HelpFormatter();
	protected boolean showAll = false;
	protected boolean valid = false;

	public ListInterfaces(String[] args, boolean exitOnError) {
		setupOptions();
		if (!parseOptions(args)) {
			formatter.printHelp("listinterfaces [options]", options);
			if (exitOnError) {
				System.exit(0);
			}
		} else {
			valid = true;
		}
	}

	public ListInterfaces(String[] args) {
		this(args, true);
	}

	public Options getOptions() {
		return options;
	}

	private void setupOptions() {
		Option allOption = new Option("a", "all", false,
				"Include inactive (DOWN) and loopback interfaces.");
		options.addOption(allOption);

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
			showAll = cmd.hasOption("a") || cmd.hasOption("all");
			return true;
		} catch (ParseException pe) {
			System.err.println("Command line option parsing failure: " + pe.getMessage());
			return false;
		}
	}

	public static void printInterfaces(boolean includeAll, PrintStream out) throws SocketException {
		Enumeration<NetworkInterface> netIfs = NetworkInterface.getNetworkInterfaces();
		if (netIfs == null) {
			out.println("No network interfaces found.");
			return;
		}

		List<NetworkInterface> matched = new ArrayList<>();
		List<NetworkInterface> all = new ArrayList<>();
		while (netIfs.hasMoreElements()) {
			NetworkInterface ni = netIfs.nextElement();
			all.add(ni);
			if (includeAll || (ni.isUp() && !ni.isLoopback())) {
				matched.add(ni);
			}
		}

		List<NetworkInterface> toPrint = matched;
		boolean fellBackToAll = false;
		if (toPrint.isEmpty() && !all.isEmpty()) {
			toPrint = all;
			fellBackToAll = true;
		}

		for (int idx = 0; idx < toPrint.size(); idx++) {
			NetworkInterface ni = toPrint.get(idx);
			if (idx > 0) {
				out.println();
			}
			formatInterface(ni, out);
		}

		if (!includeAll && !fellBackToAll && (matched.size() < all.size())) {
			int hidden = all.size() - matched.size();
			out.println();
			out.println("(" + hidden + " inactive/loopback interface(s) hidden; use -a / --all to show all)");
		}
	}

	private static void formatInterface(NetworkInterface ni, PrintStream out) throws SocketException {
		String name = ni.getName();
		String displayName = ni.getDisplayName();
		if (displayName != null && !displayName.isEmpty() && !displayName.equals(name)) {
			out.println(name + "  (" + displayName + ")");
		} else {
			out.println(name);
		}

		List<String> flags = new ArrayList<>();
		flags.add(ni.isUp() ? "UP" : "DOWN");
		if (ni.isLoopback()) {
			flags.add("LOOPBACK");
		}
		if (ni.supportsMulticast()) {
			flags.add("MULTICAST");
		}
		if (ni.isPointToPoint()) {
			flags.add("POINTOPOINT");
		}
		if (ni.isVirtual()) {
			flags.add("VIRTUAL");
		}

		StringBuilder meta = new StringBuilder();
		meta.append("  Status: ").append(String.join(", ", flags));
		int mtu = ni.getMTU();
		if (mtu > 0) {
			meta.append("  MTU: ").append(mtu);
		}
		byte[] mac = ni.getHardwareAddress();
		if (mac != null && mac.length > 0) {
			meta.append("  MAC: ").append(formatMac(mac));
		}
		out.println(meta.toString());

		List<String> v4List = new ArrayList<>();
		List<String> v6List = new ArrayList<>();
		List<InterfaceAddress> ifAddrs = ni.getInterfaceAddresses();
		if (ifAddrs != null) {
			for (InterfaceAddress ifAddr : ifAddrs) {
				InetAddress addr = ifAddr.getAddress();
				if (addr == null) {
					continue;
				}
				short prefix = ifAddr.getNetworkPrefixLength();
				String cidr = (prefix >= 0) ? (addr.getHostAddress() + "/" + prefix) : addr.getHostAddress();
				if (addr instanceof Inet4Address) {
					InetAddress bcast = ifAddr.getBroadcast();
					if (bcast != null) {
						cidr = cidr + " (broadcast " + bcast.getHostAddress() + ")";
					}
					v4List.add(cidr);
				} else if (addr instanceof Inet6Address) {
					v6List.add(cidr);
				}
			}
		}

		if (!v4List.isEmpty()) {
			out.println("  IPv4:   " + v4List.get(0));
			for (int i = 1; i < v4List.size(); i++) {
				out.println("          " + v4List.get(i));
			}
		}
		if (!v6List.isEmpty()) {
			out.println("  IPv6:   " + v6List.get(0));
			for (int i = 1; i < v6List.size(); i++) {
				out.println("          " + v6List.get(i));
			}
		}
	}

	private static String formatMac(byte[] mac) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			if (i > 0) {
				sb.append(':');
			}
			sb.append(String.format("%02x", mac[i]));
		}
		return sb.toString();
	}

	public void run() throws SocketException {
		if (!valid) {
			return;
		}
		printInterfaces(showAll, System.out);
	}

	public static void main(String[] args) {
		try {
			ListInterfaces li = new ListInterfaces(args);
			li.run();
		} catch (Exception ex) {
			System.err.println("Failed to list network interfaces: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
	}
}

