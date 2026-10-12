/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.hostname;

import java.net.InetAddress;

import com.jagornet.dhcp.core.util.Util;

/**
 * Deterministic template-based hostname generator.
 * Supports tokens:
 * <ul>
 *   <li><code>${ip}</code> - Assigned IP address (e.g. 192.168.1.100)</li>
 *   <li><code>${ip.dashed}</code> - IP with dots/colons replaced with hyphens (e.g. 192-168-1-100)</li>
 *   <li><code>${ip.hex}</code> - IP in hexadecimal format</li>
 *   <li><code>${mac}</code> - MAC address in 12-char hex (e.g. 001122334455)</li>
 *   <li><code>${mac.dashed}</code> - MAC address with hyphens (e.g. 00-11-22-33-44-55)</li>
 *   <li><code>${duid}</code> - DUID in hex</li>
 *   <li><code>${filter.name}</code> - Matching filter name</li>
 * </ul>
 * 
 * @author A. Gregory Rabil
 */
public class HostnameGenerator {

	public static final String DEFAULT_TEMPLATE = "host-${ip.dashed}";

	/**
	 * Generates a hostname using the given template and client details.
	 * 
	 * @param template the template string (e.g. "host-${ip.dashed}")
	 * @param ip the assigned IP address (v4 or v6)
	 * @param chAddr client hardware address (MAC) or DUID bytes
	 * @param duid client DUID bytes (if distinct from chAddr)
	 * @param filterName name of matched filter, if any
	 * @return generated hostname string (unqualified)
	 */
	public static String generate(String template, InetAddress ip, byte[] chAddr,
			byte[] duid, String filterName) {
		if (template == null || template.trim().isEmpty()) {
			template = DEFAULT_TEMPLATE;
		}

		String result = template;

		if (ip != null) {
			String ipStr = ip.getHostAddress();
			// Strip zone index from IPv6 if present
			int pct = ipStr.indexOf('%');
			if (pct > 0) {
				ipStr = ipStr.substring(0, pct);
			}

			result = result.replace("${ip}", ipStr);

			// Replace both dots and colons for dashed format
			String ipDashed = ipStr.replace('.', '-').replace(':', '-');
			result = result.replace("${ip.dashed}", ipDashed);

			byte[] ipBytes = ip.getAddress();
			result = result.replace("${ip.hex}", Util.toHexString(ipBytes).toLowerCase());
		} else {
			result = result.replace("${ip}", "0-0-0-0");
			result = result.replace("${ip.dashed}", "0-0-0-0");
			result = result.replace("${ip.hex}", "00000000");
		}

		if (chAddr != null && chAddr.length > 0) {
			String macHex = Util.toHexString(chAddr).toLowerCase();
			result = result.replace("${mac}", macHex);

			StringBuilder macDashed = new StringBuilder();
			for (int i = 0; i < chAddr.length; i++) {
				if (i > 0) macDashed.append("-");
				macDashed.append(String.format("%02x", chAddr[i]));
			}
			result = result.replace("${mac.dashed}", macDashed.toString());
		} else {
			result = result.replace("${mac}", "000000000000");
			result = result.replace("${mac.dashed}", "00-00-00-00-00-00");
		}

		if (duid != null && duid.length > 0) {
			result = result.replace("${duid}", Util.toHexString(duid).toLowerCase());
		} else {
			result = result.replace("${duid}", "");
		}

		if (filterName != null && !filterName.trim().isEmpty()) {
			String cleanFilter = filterName.trim().toLowerCase().replaceAll("[^a-z0-9\\-]", "-");
			result = result.replace("${filter.name}", cleanFilter);
		} else {
			result = result.replace("${filter.name}", "client");
		}

		// Ensure the generated short hostname is scrubbed clean
		return HostnameScrubber.cleanLabel(result, false, "-", true);
	}
}
