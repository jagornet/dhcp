/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.config;

import java.io.File;
import java.util.Set;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

/**
 * Filter for DHCPv6 client DUIDs (DHCP Unique Identifiers).
 * Supports excluded (blocklist) and included (allowlist) DUID lists.
 * DUID lists can be loaded from external files with hot-reloading when files are modified.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6DuidFilter extends BaseClientFilter {

	public DhcpV6DuidFilter() {
		super("DUID");
	}

	@Override
	public void init() {
		synchronized (reloadLock) {
			String excludedFilename = DhcpServerPolicies.globalPolicy(Property.V6_EXCLUDED_DUIDS_FILE);
			String includedFilename = DhcpServerPolicies.globalPolicy(Property.V6_INCLUDED_DUIDS_FILE);

			excludedFile = resolveConfigFile(excludedFilename);
			includedFile = resolveConfigFile(includedFilename);

			loadAll();
			lastCheckTime = System.currentTimeMillis();
		}
	}

	@Override
	public String normalize(String token) {
		return normalizeDuid(token);
	}

	/**
	 * Check if client DUID is allowed to receive DHCPv6 service.
	 * 
	 * @param duid the client DUID bytes
	 * @return true if client is allowed; false if client should be dropped/ignored
	 */
	public boolean isAllowed(byte[] duid) {
		checkAndReloadIfModified();

		Set<String> included = includedTokens;

		if ((duid == null) || (duid.length == 0)) {
			// If an allowlist is configured, a client without DUID cannot match
			if ((included != null) && !included.isEmpty()) {
				return false;
			}
			return true;
		}

		String duidStr = Util.toHexString(duid);
		if (duidStr == null) {
			if ((included != null) && !included.isEmpty()) {
				return false;
			}
			return true;
		}
		duidStr = duidStr.toUpperCase();

		return isTokenAllowed(duidStr);
	}

	/**
	 * Reads DUIDs from a file.
	 *
	 * @param file the file to read
	 * @return set of normalized uppercase DUID hex strings
	 */
	public static Set<String> loadDuidsFromFile(File file) {
		return new DhcpV6DuidFilter().loadTokensFromFile(file);
	}

	/**
	 * Normalizes a DUID string by stripping colons, hyphens, and dots.
	 * Returns an uppercase hexadecimal string, or null if invalid.
	 *
	 * @param duidStr raw DUID string
	 * @return normalized uppercase hex string or null
	 */
	public static String normalizeDuid(String duidStr) {
		if (duidStr == null) {
			return null;
		}
		String cleaned = duidStr.replaceAll("[^0-9a-fA-F]", "").toUpperCase();
		// DUID is between 1 and 128 octets (2 to 256 hex digits), even length
		if ((cleaned.length() >= 2) && (cleaned.length() % 2 == 0) && (cleaned.length() <= 256)) {
			return cleaned;
		}
		return null;
	}

	public Set<String> getExcludedDuids() {
		return getExcludedTokens();
	}

	public Set<String> getIncludedDuids() {
		return getIncludedTokens();
	}

	public File getExcludedDuidFile() {
		return getExcludedFile();
	}

	public void setExcludedDuidFile(File excludedDuidFile) {
		setExcludedFile(excludedDuidFile);
	}

	public File getIncludedDuidFile() {
		return getIncludedFile();
	}

	public void setIncludedDuidFile(File includedDuidFile) {
		setIncludedFile(includedDuidFile);
	}
}
