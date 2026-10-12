/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseIpAddressOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

public class DhcpV4SubnetSelectionOption extends BaseIpAddressOption {
    public DhcpV4SubnetSelectionOption() {
        super();
        this.code = DhcpConstants.V4OPTION_SUBNET_SELECTION;
        this.name = "Subnet-Selection";
        this.setV4(true);
    }
}
