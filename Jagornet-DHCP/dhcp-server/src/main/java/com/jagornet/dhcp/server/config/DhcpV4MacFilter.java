/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.config;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

/**
 * Filter for DHCPv4 client MAC addresses (hardware addresses).
 * Supports excluded (blocklist) and included (allowlist) MAC address lists.
 * MAC address lists can be loaded from external files and/or policies,
 * with hot-reloading when files are modified.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4MacFilter extends BaseClientFilter {

	private static Logger log = LoggerFactory.getLogger(DhcpV4MacFilter.class);

	public DhcpV4MacFilter() {
		super("MAC");
	}

	@Override
	public void init() {
		synchronized (reloadLock) {
			String excludedFilename = DhcpServerPolicies.globalPolicy(Property.V4_EXCLUDED_MACS_FILE);
			String includedFilename = DhcpServerPolicies.globalPolicy(Property.V4_INCLUDED_MACS_FILE);

			excludedFile = resolveConfigFile(excludedFilename);
			includedFile = resolveConfigFile(includedFilename);

			loadAll();
			lastCheckTime = System.currentTimeMillis();
		}
	}

	@Override
	public String normalize(String token) {
		return normalizeMac(token);
	}

	@Override
	protected void reloadExcluded() {
		Set<String> newExcluded = new HashSet<>();

		// 1. Add legacy / inline ignored MACs (e.g. 000000000000, FFFFFFFFFFFF)
		String legacyIgnored = DhcpServerPolicies.globalPolicy(Property.V4_IGNORED_MACS);
		if ((legacyIgnored != null) && !legacyIgnored.trim().isEmpty()) {
			newExcluded.addAll(parseMacList(legacyIgnored));
		}

		// 2. Load from file if configured
		if (excludedFile != null) {
			if (excludedFile.exists() && excludedFile.canRead()) {
				Set<String> fileMacs = loadTokensFromFile(excludedFile);
				newExcluded.addAll(fileMacs);
				excludedFileLastModified = excludedFile.lastModified();
				log.info("Loaded " + fileMacs.size() + " excluded MAC addresses from " + excludedFile.getPath());
			} else {
				log.warn("Excluded MAC file not found or not readable: " + excludedFile.getPath());
				excludedFileLastModified = -1;
			}
		}

		this.excludedTokens = Collections.unmodifiableSet(newExcluded);
	}

	/**
	 * Check if client MAC address is allowed to receive DHCP service.
	 * 
	 * @param chAddr the client hardware address bytes
	 * @return true if client is allowed; false if client should be dropped/ignored
	 */
	public boolean isAllowed(byte[] chAddr) {
		if ((chAddr == null) || (chAddr.length == 0)) {
			return false;
		}

		checkAndReloadIfModified();

		String mac = Util.toHexString(chAddr);
		if (mac == null) {
			return false;
		}
		mac = mac.toUpperCase();

		return isTokenAllowed(mac);
	}

	/**
	 * Reads MAC addresses from a file.
	 *
	 * @param file the file to read
	 * @return set of normalized uppercase MAC addresses
	 */
	public static Set<String> loadMacsFromFile(File file) {
		return new DhcpV4MacFilter().loadTokensFromFile(file);
	}

	/**
	 * Parses a comma-separated list of MAC addresses.
	 *
	 * @param macListStr comma-separated MAC addresses
	 * @return set of normalized uppercase MAC addresses
	 */
	public static Set<String> parseMacList(String macListStr) {
		Set<String> macs = new HashSet<>();
		if (macListStr == null) {
			return macs;
		}
		String[] entries = macListStr.split(",");
		for (String entry : entries) {
			String trimmed = stripComment(entry).trim();
			if (!trimmed.isEmpty()) {
				String normalized = normalizeMac(trimmed);
				if (normalized != null) {
					macs.add(normalized);
				} else {
					log.warn("Invalid inline MAC address: '" + trimmed + "'");
				}
			}
		}
		return macs;
	}

	/**
	 * Normalizes a MAC address string by stripping colons, hyphens, and dots.
	 * Returns 12 uppercase hexadecimal characters, or null if invalid.
	 *
	 * @param macStr raw MAC address string
	 * @return normalized 12-character hex string or null
	 */
	public static String normalizeMac(String macStr) {
		if (macStr == null) {
			return null;
		}
		String cleaned = macStr.replaceAll("[^0-9a-fA-F]", "").toUpperCase();
		// Standard Ethernet MAC address is 6 bytes (12 hex digits)
		if (cleaned.length() == 12) {
			return cleaned;
		}
		// Also support variable-length hardware addresses if even number of hex digits >= 2
		if ((cleaned.length() >= 2) && (cleaned.length() % 2 == 0)) {
			return cleaned;
		}
		return null;
	}

	public Set<String> getExcludedMacs() {
		return getExcludedTokens();
	}

	public Set<String> getIncludedMacs() {
		return getIncludedTokens();
	}

	public File getExcludedMacFile() {
		return getExcludedFile();
	}

	public void setExcludedMacFile(File excludedMacFile) {
		setExcludedFile(excludedMacFile);
	}

	public File getIncludedMacFile() {
		return getIncludedFile();
	}

	public void setIncludedMacFile(File includedMacFile) {
		setIncludedFile(includedMacFile);
	}
}
