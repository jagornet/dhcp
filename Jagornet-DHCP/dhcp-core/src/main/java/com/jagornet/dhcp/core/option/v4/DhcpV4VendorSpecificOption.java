/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4VendorSpecificOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4VendorSpecificOption extends BaseOpaqueDataOption
{
	/**
	 * Instantiates a new dhcp vendor info option.
	 */
	public DhcpV4VendorSpecificOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp vendor info option.
	 * 
	 * @param v4VendorSpecificOption the vendor info option
	 */
	public DhcpV4VendorSpecificOption(BaseOpaqueData baseOpaqueData)
	{
		super(baseOpaqueData);
		setCode(DhcpConstants.V4OPTION_VENDOR_INFO);
		setV4(true);
	}
}
