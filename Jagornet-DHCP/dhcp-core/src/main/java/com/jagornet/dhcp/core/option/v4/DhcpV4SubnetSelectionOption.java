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
