/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option;

import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;

/**
 * <p>Title: DhcpUnknownOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpUnknownOption extends BaseOpaqueDataOption
{
	
	/** The option code for this unknown option. */
	protected int code;
	
	/**
	 * Instantiates a new dhcp unknown option.
	 */
	public DhcpUnknownOption() {
		super();
	}

	public String getName() {
		if (name == null) {
			name = "Dhcp" + (isV4() ? "V4-" : "V6-") + "Option-" + this.getCode();
		}
		return name;
	}
}
