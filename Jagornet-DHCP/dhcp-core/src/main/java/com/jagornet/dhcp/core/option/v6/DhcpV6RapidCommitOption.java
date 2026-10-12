/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseEmptyOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6RapidCommitOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6RapidCommitOption extends BaseEmptyOption
{
	/**
	 * Instantiates a new dhcp rapid commit option.
	 */
	public DhcpV6RapidCommitOption()
	{
		super();
		setCode(DhcpConstants.V6OPTION_RAPID_COMMIT);
	}
}
