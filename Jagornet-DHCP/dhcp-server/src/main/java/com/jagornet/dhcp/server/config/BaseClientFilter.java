/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * Base abstract class for file-based client identifier filters (e.g. MAC address or DUID).
 * Provides file path resolution, comment stripping, file parsing, set lookups,
 * and hot-reloading when files are modified on disk.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseClientFilter {

	private static Logger log = LoggerFactory.getLogger(BaseClientFilter.class);

	/** Check file modification timestamps at most every 2 seconds */
	protected static final long RELOAD_CHECK_INTERVAL_MS = 2000;

	protected final Object reloadLock = new Object();

	protected volatile Set<String> excludedTokens = Collections.emptySet();
	protected volatile Set<String> includedTokens = Collections.emptySet();

	protected File excludedFile;
	protected File includedFile;

	protected long excludedFileLastModified = -1;
	protected long includedFileLastModified = -1;

	protected long lastCheckTime = 0;

	protected final String filterType;

	/**
	 * Constructor.
	 * 
	 * @param filterType descriptive name for logging (e.g., "MAC" or "DUID")
	 */
	public BaseClientFilter(String filterType) {
		this.filterType = filterType;
	}

	/**
	 * Initialize filter from policies. Subclasses bind specific policy properties.
	 */
	public abstract void init();

	/**
	 * Normalize a raw string token into a canonical uppercase hex format.
	 *
	 * @param token raw token string
	 * @return canonical hex representation or null if invalid
	 */
	public abstract String normalize(String token);

	/**
	 * Resolves a file path: if absolute, returns it directly;
	 * otherwise resolves relative to $JAGORNET_DHCP_HOME/config.
	 *
	 * @param filename the configured file name or path
	 * @return resolved File object, or null if filename is empty
	 */
	public static File resolveConfigFile(String filename) {
		if ((filename == null) || filename.trim().isEmpty()) {
			return null;
		}
		File file = new File(filename.trim());
		if (file.isAbsolute()) {
			return file;
		}
		// Primary resolution: relative to $JAGORNET_DHCP_HOME/config
		String configDir = DhcpConstants.JAGORNET_DHCP_HOME + File.separator + "config";
		File targetFile = new File(configDir, filename.trim());
		if (targetFile.exists()) {
			return targetFile;
		}
		// Fallback for tests or working directory execution
		if (file.exists()) {
			return file;
		}
		return targetFile;
	}

	/**
	 * Strips line or inline comments starting with #, //, or ;
	 *
	 * @param text the line text
	 * @return text with comment stripped
	 */
	public static String stripComment(String text) {
		if (text == null) {
			return "";
		}
		int hashIdx = text.indexOf('#');
		int slashSlashIdx = text.indexOf("//");
		int semiIdx = text.indexOf(';');

		int cutIdx = -1;
		if (hashIdx >= 0) cutIdx = hashIdx;
		if ((slashSlashIdx >= 0) && ((cutIdx == -1) || (slashSlashIdx < cutIdx))) cutIdx = slashSlashIdx;
		if ((semiIdx >= 0) && ((cutIdx == -1) || (semiIdx < cutIdx))) cutIdx = semiIdx;

		if (cutIdx >= 0) {
			return text.substring(0, cutIdx);
		}
		return text;
	}

	/**
	 * Reads tokens from a file using normalize(). Lines starting with #, //, or ; are comments.
	 * Inline comments following #, //, or ; are stripped.
	 *
	 * @param file the file to read
	 * @return set of normalized tokens
	 */
	public Set<String> loadTokensFromFile(File file) {
		Set<String> tokens = new HashSet<>();
		if ((file == null) || !file.exists()) {
			return tokens;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				lineNum++;
				line = stripComment(line).trim();
				if (line.isEmpty()) {
					continue;
				}

				String normalized = normalize(line);
				if (normalized != null) {
					tokens.add(normalized);
				} else {
					log.warn("Invalid " + filterType + " in " + file.getName() + " line " + lineNum + ": '" + line + "'");
				}
			}
		} catch (IOException ex) {
			log.error("Failed to read " + filterType + " file: " + file.getPath(), ex);
		}

		return tokens;
	}

	/**
	 * Checks if the underlying files have been modified and reloads if necessary.
	 */
	public void checkAndReloadIfModified() {
		long now = System.currentTimeMillis();
		if ((now - lastCheckTime) > RELOAD_CHECK_INTERVAL_MS) {
			lastCheckTime = now;
			boolean needExcludedReload = false;
			boolean needIncludedReload = false;

			if (excludedFile != null) {
				long mod = excludedFile.exists() ? excludedFile.lastModified() : -1;
				if (mod != excludedFileLastModified) {
					needExcludedReload = true;
				}
			}

			if (includedFile != null) {
				long mod = includedFile.exists() ? includedFile.lastModified() : -1;
				if (mod != includedFileLastModified) {
					needIncludedReload = true;
				}
			}

			if (needExcludedReload || needIncludedReload) {
				synchronized (reloadLock) {
					if (needExcludedReload) {
						reloadExcluded();
					}
					if (needIncludedReload) {
						reloadIncluded();
					}
				}
			}
		}
	}

	/**
	 * Reload all lists.
	 */
	public void reload() {
		synchronized (reloadLock) {
			loadAll();
			lastCheckTime = System.currentTimeMillis();
		}
	}

	protected void loadAll() {
		reloadExcluded();
		reloadIncluded();
	}

	protected void reloadExcluded() {
		Set<String> newExcluded = new HashSet<>();

		if (excludedFile != null) {
			if (excludedFile.exists() && excludedFile.canRead()) {
				Set<String> fileTokens = loadTokensFromFile(excludedFile);
				newExcluded.addAll(fileTokens);
				excludedFileLastModified = excludedFile.lastModified();
				log.info("Loaded " + fileTokens.size() + " excluded " + filterType + "s from " + excludedFile.getPath());
			} else {
				log.warn("Excluded " + filterType + " file not found or not readable: " + excludedFile.getPath());
				excludedFileLastModified = -1;
			}
		}

		this.excludedTokens = Collections.unmodifiableSet(newExcluded);
	}

	protected void reloadIncluded() {
		Set<String> newIncluded = new HashSet<>();

		if (includedFile != null) {
			if (includedFile.exists() && includedFile.canRead()) {
				Set<String> fileTokens = loadTokensFromFile(includedFile);
				newIncluded.addAll(fileTokens);
				includedFileLastModified = includedFile.lastModified();
				log.info("Loaded " + fileTokens.size() + " included " + filterType + "s from " + includedFile.getPath());
			} else {
				log.warn("Included " + filterType + " file not found or not readable: " + includedFile.getPath());
				includedFileLastModified = -1;
			}
		}

		this.includedTokens = Collections.unmodifiableSet(newIncluded);
	}

	/**
	 * Core evaluation against excluded and included sets.
	 *
	 * @param normalizedToken canonical uppercase token
	 * @return true if allowed; false if excluded or not included
	 */
	protected boolean isTokenAllowed(String normalizedToken) {
		Set<String> excluded = excludedTokens;
		if ((excluded != null) && !excluded.isEmpty() && excluded.contains(normalizedToken)) {
			return false;
		}

		Set<String> included = includedTokens;
		if ((included != null) && !included.isEmpty()) {
			if (!included.contains(normalizedToken)) {
				return false;
			}
		}

		return true;
	}

	public Set<String> getExcludedTokens() {
		return excludedTokens;
	}

	public Set<String> getIncludedTokens() {
		return includedTokens;
	}

	public File getExcludedFile() {
		return excludedFile;
	}

	public void setExcludedFile(File excludedFile) {
		this.excludedFile = excludedFile;
	}

	public File getIncludedFile() {
		return includedFile;
	}

	public void setIncludedFile(File includedFile) {
		this.includedFile = includedFile;
	}
}
