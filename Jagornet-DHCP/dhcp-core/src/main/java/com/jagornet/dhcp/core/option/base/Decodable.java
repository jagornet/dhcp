/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The Interface Decodable
 * 
 * @author A. Gregory Rabil
 */
public interface Decodable
{
    /**
     * Decode a DHCP option.
     * 
     * @param buf the ByteBuffer to decode.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void decode(ByteBuffer buf) throws IOException;
}
