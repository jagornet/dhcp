/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.jagornet.dhcp.server.hostname;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates and scrubs hostnames and FQDNs for DNS compliance (RFC 952, RFC
 * 1123, RFC 1035).
 * 
 * @author A. Gregory Rabil
 */
public class HostnameScrubber {

	public static final int MAX_LABEL_LENGTH = 63;
	public static final int MAX_FQDN_LENGTH = 253;

	public static class ScrubResult {
		private final String scrubbedName;
		private final boolean modified;
		private final boolean valid;

		public ScrubResult(String scrubbedName, boolean modified, boolean valid) {
			this.scrubbedName = scrubbedName;
			this.modified = modified;
			this.valid = valid;
		}

		public String getScrubbedName() {
			return scrubbedName;
		}

		public boolean isModified() {
			return modified;
		}

		public boolean isValid() {
			return valid;
		}
	}

	/**
	 * Scrubs the given raw hostname or domain name according to DNS rules.
	 * 
	 * @param rawName          the raw client-supplied name
	 * @param allowUnderscores whether to allow underscores in labels
	 * @param replaceChar      the replacement string/character for invalid
	 *                         characters (e.g. "-")
	 * @param collapseHyphens  whether to collapse multiple consecutive hyphens into
	 *                         one
	 * @param stripDomains     list of domain suffixes to strip (e.g. "local",
	 *                         "lan", "home")
	 * @return ScrubResult containing cleaned name, modified flag, and validity
	 */
	public static ScrubResult scrub(String rawName, boolean allowUnderscores,
			String replaceChar, boolean collapseHyphens, List<String> stripDomains) {
		if (rawName == null || rawName.trim().isEmpty()) {
			return new ScrubResult("", false, false);
		}

		String original = rawName.trim();
		String working = original;

		// Strip trailing dot if present
		if (working.endsWith(".")) {
			working = working.substring(0, working.length() - 1);
		}

		// Strip disallowed domain suffixes (e.g. .local, .lan, .home)
		if (stripDomains != null) {
			for (String domain : stripDomains) {
				String suffix = "." + domain.trim().toLowerCase();
				if (working.toLowerCase().endsWith(suffix)) {
					working = working.substring(0, working.length() - suffix.length());
					break;
				}
			}
		}

		if (working.isEmpty()) {
			return new ScrubResult("", true, false);
		}

		if (replaceChar == null) {
			replaceChar = "-";
		}

		String[] labels = working.split("\\.");
		List<String> cleanLabels = new ArrayList<>();

		for (String label : labels) {
			String clean = cleanLabel(label, allowUnderscores, replaceChar, collapseHyphens);
			if (!clean.isEmpty()) {
				cleanLabels.add(clean);
			}
		}

		if (cleanLabels.isEmpty()) {
			return new ScrubResult("", true, false);
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cleanLabels.size(); i++) {
			if (i > 0) {
				sb.append(".");
			}
			sb.append(cleanLabels.get(i));
		}

		String result = sb.toString();
		if (result.length() > MAX_FQDN_LENGTH) {
			// Exceeds max FQDN length
			return new ScrubResult(result, true, false);
		}

		boolean modified = !result.equalsIgnoreCase(original)
				&& !result.equalsIgnoreCase(original.replaceAll("\\.+$", ""));
		return new ScrubResult(result, modified, true);
	}

	/**
	 * Cleans a single label.
	 */
	public static String cleanLabel(String label, boolean allowUnderscores,
			String replaceChar, boolean collapseHyphens) {
		if (label == null || label.isEmpty()) {
			return "";
		}

		String working = label.toLowerCase();

		// Replace invalid characters:
		// Allowed: [a-z0-9-] or [a-z0-9-_] if allowUnderscores
		String regex = allowUnderscores ? "[^a-z0-9_\\-]" : "[^a-z0-9\\-]";
		working = working.replaceAll(regex, replaceChar);

		// Collapse consecutive hyphens
		if (collapseHyphens && !replaceChar.isEmpty()) {
			working = working.replaceAll("-{2,}", "-");
			if (allowUnderscores) {
				working = working.replaceAll("_{2,}", "_");
			}
		}

		// Trim leading hyphens (and underscores)
		while (working.startsWith("-") || (allowUnderscores && working.startsWith("_"))) {
			working = working.substring(1);
		}

		// Trim trailing hyphens (and underscores)
		while (working.endsWith("-") || (allowUnderscores && working.endsWith("_"))) {
			working = working.substring(0, working.length() - 1);
		}

		// Enforce maximum label length (63 octets)
		if (working.length() > MAX_LABEL_LENGTH) {
			working = working.substring(0, MAX_LABEL_LENGTH);
			// Truncation might have left a trailing hyphen or underscore
			while (working.endsWith("-") || (allowUnderscores && working.endsWith("_"))) {
				working = working.substring(0, working.length() - 1);
			}
		}

		return working;
	}

	/**
	 * Parse comma-separated list of domains to strip.
	 */
	public static List<String> parseStripDomains(String domainsStr) {
		if (domainsStr == null || domainsStr.trim().isEmpty()) {
			return new ArrayList<>();
		}
		String[] split = domainsStr.split(",");
		List<String> list = new ArrayList<>();
		for (String s : split) {
			String trimmed = s.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return list;
	}
}
