/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.ha;

import java.util.Date;

public class HaState {

	public String state;
	public Date timestamp;
	
	@Override
	public String toString() {
		return "Timestamp=" + timestamp + 
				" state=" + state;
	}
}
