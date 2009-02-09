package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpClientIdOption extends BaseOpaqueDataOption
{
    private ClientIdOption clientIdOption;
    
    public DhcpClientIdOption()
    {
        super();
        clientIdOption = ClientIdOption.Factory.newInstance();
    }
    public DhcpClientIdOption(ClientIdOption clientIdOption)
    {
        super();
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
        else
            this.clientIdOption = ClientIdOption.Factory.newInstance();
    }

    public ClientIdOption getClientIdOption()
    {
        return clientIdOption;
    }

    public void setClientIdOption(ClientIdOption clientIdOption)
    {
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
    }
    
    public int getCode()
    {
        return clientIdOption.getCode();
    }
    
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.clientIdOption;
    }
}
