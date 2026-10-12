/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.hostname;

/**
 * Result of resolving, scrubbing, or generating a client hostname.
 * 
 * @author A. Gregory Rabil
 */
public class HostnameResolution {

	private final String hostname;
	private final String fqdn;
	private final boolean modified;
	private final boolean generated;
	private final boolean valid;
	private final boolean sendOption12;

	public HostnameResolution(String hostname, String fqdn, boolean modified,
			boolean generated, boolean valid, boolean sendOption12) {
		this.hostname = hostname;
		this.fqdn = fqdn;
		this.modified = modified;
		this.generated = generated;
		this.valid = valid;
		this.sendOption12 = sendOption12;
	}

	public String getHostname() {
		return hostname;
	}

	public String getFqdn() {
		return fqdn;
	}

	public boolean isModified() {
		return modified;
	}

	public boolean isGenerated() {
		return generated;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isSendOption12() {
		return sendOption12;
	}

	@Override
	public String toString() {
		return "HostnameResolution [hostname=" + hostname + ", fqdn=" + fqdn
				+ ", modified=" + modified + ", generated=" + generated
				+ ", valid=" + valid + ", sendOption12=" + sendOption12 + "]";
	}
}
