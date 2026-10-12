/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.ha;

public class HaException extends Exception {
    
    public HaException(Exception ex) {
        super(ex);
    }

    public HaException(String msg) {
        super(msg);
    }

    public HaException(String msg, Exception ex) {
        super(msg, ex);
    }
}
