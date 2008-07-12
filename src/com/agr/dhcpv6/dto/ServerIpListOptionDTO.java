package com.agr.dhcpv6.dto;

import java.util.List;

public class ServerIpListOptionDTO extends BaseOptionDTO
{
    protected List<String> serverIpAddresses;

    public List<String> getServerIpAddresses()
    {
        return serverIpAddresses;
    }

    public void setServerIpAddresses(List<String> serverIpAddresses)
    {
        this.serverIpAddresses = serverIpAddresses;
    }
}
