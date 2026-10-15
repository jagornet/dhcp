/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.request.ddns;

/**
 * Enumeration of supported DDNS identification record types.
 * <ul>
 *   <li>{@link #DHCID} - RFC 4701 standard DHCID resource record (Type 49)</li>
 *   <li>{@link #TXT} - Legacy ISC DHCP interim style TXT resource record (Type 16)</li>
 * </ul>
 * 
 * @author A. Gregory Rabil
 */
public enum DdnsRecordType {
	DHCID,
	TXT;

	public static DdnsRecordType fromString(String val) {
		if (val != null) {
			if (val.trim().equalsIgnoreCase("txt")) {
				return TXT;
			}
		}
		return DHCID;
	}
}
