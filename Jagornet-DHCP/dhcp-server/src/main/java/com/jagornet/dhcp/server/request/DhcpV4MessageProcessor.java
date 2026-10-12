/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import com.jagornet.dhcp.core.message.DhcpV4Message;

/**
 * The Interface DhcpV4MessageProcessor.
 */
public interface DhcpV4MessageProcessor
{
	/**
	 * Process message.
	 * 
	 * @return the dhcp v4 message
	 */
	public DhcpV4Message processMessage();
}
