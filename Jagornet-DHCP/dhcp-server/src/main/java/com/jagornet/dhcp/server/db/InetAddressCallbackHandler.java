/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.net.InetAddress;

public interface InetAddressCallbackHandler {

	public void processInetAddress(InetAddress inetAddr) throws IOException;
}
