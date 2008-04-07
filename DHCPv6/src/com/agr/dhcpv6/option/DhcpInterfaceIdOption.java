package com.agr.dhcpv6.option;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.InterfaceIdOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;

/**
 * <p>Title: DhcpInterfaceIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpInterfaceIdOption extends BaseOpaqueDataOption
{
    private static Log log = LogFactory.getLog(DhcpInterfaceIdOption.class);
    
    private InterfaceIdOption interfaceIdOption;
    
    public DhcpInterfaceIdOption()
    {
        super();
        interfaceIdOption = new InterfaceIdOption();
    }
    public DhcpInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        super();
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
        else
            this.interfaceIdOption = new InterfaceIdOption();
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

    public String getName()
    {
        return interfaceIdOption.getName();
    }
    
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.interfaceIdOption;
    }
}
