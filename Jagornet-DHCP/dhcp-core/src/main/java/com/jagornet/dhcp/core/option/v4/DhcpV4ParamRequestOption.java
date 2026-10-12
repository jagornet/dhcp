/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseUnsignedByteListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4ParamRequestOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4ParamRequestOption extends BaseUnsignedByteListOption
{
	/**
	 * Instantiates a new dhcp v4 param request option.
	 */
	public DhcpV4ParamRequestOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 param request option.
	 * 
	 * @param optionRequestOption the v4 param request option
	 */
	public DhcpV4ParamRequestOption(List<Short> paramRequest)
	{
		super(paramRequest);
		setCode(DhcpConstants.V4OPTION_PARAM_REQUEST_LIST);
		setV4(true);
	}
}
