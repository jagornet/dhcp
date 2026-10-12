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
 * The Interface Encodable.
 * 
 * @author A. Gregory Rabil
 */
public interface Encodable
{
    /**
     * Encode a DHCP option.
     * 
     * @return the ByteBuffer containing the encoded DHCP option.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBuffer encode() throws IOException;
}
