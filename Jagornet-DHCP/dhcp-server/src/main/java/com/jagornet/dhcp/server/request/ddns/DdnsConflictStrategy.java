/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.request.ddns;

/**
 * Enumeration of DDNS forward update conflict resolution strategies.
 * <ul>
 *   <li>{@link #STRICT} - RFC 4703 client ownership. The first registrant owns the FQDN;
 *       other dynamic clients cannot claim or overwrite it.</li>
 *   <li>{@link #REPLACE_DYNAMIC} - Dynamic client takeover with static host protection.
 *       Dynamic clients can displace other dynamic client entries (identified via a shared dynamic token),
 *       while strictly protecting static infrastructure records lacking the dynamic marker.</li>
 *   <li>{@link #UNCONDITIONAL} - Unconditional replacement without prerequisite checks.</li>
 * </ul>
 * 
 * @author A. Gregory Rabil
 */
public enum DdnsConflictStrategy {
	STRICT,
	REPLACE_DYNAMIC,
	UNCONDITIONAL;

	public static DdnsConflictStrategy fromString(String val) {
		if (val != null) {
			String normalized = val.trim().toLowerCase().replace('_', '-');
			if (normalized.equals("replace-dynamic")) {
				return REPLACE_DYNAMIC;
			}
			if (normalized.equals("unconditional")) {
				return UNCONDITIONAL;
			}
		}
		return STRICT;
	}
}
