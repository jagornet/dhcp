package com.agr.dhcpv6.option;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.NisPlusServersOption;

/**
 * <p>Title: DhcpNisPlusServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisPlusServersOption extends BaseServerAddressesOption
{
    private static Log log = LogFactory.getLog(DhcpNisPlusServersOption.class);

    private NisPlusServersOption nisPlusServersOption;

    public DhcpNisPlusServersOption()
    {
        super();
        nisPlusServersOption = new NisPlusServersOption();
    }
    public DhcpNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        super();
        if (nisPlusServersOption != null)
            this.nisPlusServersOption = nisPlusServersOption;
        else
            this.nisPlusServersOption = new NisPlusServersOption();
    }

    public NisPlusServersOption getNisPlusServersOption()
    {
        return nisPlusServersOption;
    }

    public void setNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        if (nisPlusServersOption != null)
            this.nisPlusServersOption = nisPlusServersOption;
    }

    public int getCode()
    {
        return nisPlusServersOption.getCode();
    }

    public String getName()
    {
        return nisPlusServersOption.getName();
    }
    
    public List<String> getServerIpAddresses()
    {
        // get a reference to the live list in the XML object
        return nisPlusServersOption.getServerIpAddresses();
    }
}
