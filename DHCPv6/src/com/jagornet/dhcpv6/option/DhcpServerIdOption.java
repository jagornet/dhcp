package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.ServerIdOption;

/**
 * <p>Title: DhcpServerIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpServerIdOption extends BaseOpaqueDataOption
{
    private ServerIdOption serverIdOption;
    
    public DhcpServerIdOption()
    {
        super();
        serverIdOption = ServerIdOption.Factory.newInstance();
    }
    public DhcpServerIdOption(ServerIdOption serverIdOption)
    {
        super();
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
        else
            this.serverIdOption = ServerIdOption.Factory.newInstance();
    }

    public ServerIdOption getServerIdOption()
    {
        return serverIdOption;
    }

    public void setServerIdOption(ServerIdOption serverIdOption)
    {
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
    }

    public int getCode()
    {
        return serverIdOption.getCode();
    }
    
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.serverIdOption;
    }
}
