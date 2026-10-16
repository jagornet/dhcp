/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.jagornet.dhcp.server.request.ddns;

/**
 * Enumeration of supported DDNS identification record types.
 * <ul>
 * <li>{@link #DHCID} - RFC 4701 standard DHCID resource record (Type 49)</li>
 * <li>{@link #TXT} - Legacy ISC DHCP interim style TXT resource record (Type
 * 16)</li>
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
