package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.InterfaceIdOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

/**
 * <p>Title: DhcpInterfaceIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpInterfaceIdOption extends BaseOpaqueDataOption
{
    private InterfaceIdOption interfaceIdOption;
    
    public DhcpInterfaceIdOption()
    {
        super();
        interfaceIdOption = InterfaceIdOption.Factory.newInstance();
    }
    public DhcpInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        super();
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
        else
            this.interfaceIdOption = InterfaceIdOption.Factory.newInstance();
    }

    public InterfaceIdOption getInterfaceIdOption()
    {
        return interfaceIdOption;
    }

    public void setInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
    }
    
    public int getCode()
    {
        return interfaceIdOption.getCode();
    }
    
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.interfaceIdOption;
    }
}
