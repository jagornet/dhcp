package com.agr.dhcpv6.option;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.SipServerAddressesOption;

/**
 * <p>Title: DhcpSipServerAddressesOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSipServerAddressesOption extends BaseServerAddressesOption
{
    private static Log log = LogFactory.getLog(DhcpSipServerAddressesOption.class);

    private SipServerAddressesOption sipServerAddressesOption;

    public DhcpSipServerAddressesOption()
    {
        super();
        sipServerAddressesOption = new SipServerAddressesOption();
    }
    public DhcpSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        super();
        if (sipServerAddressesOption != null)
            this.sipServerAddressesOption = sipServerAddressesOption;
        else
            this.sipServerAddressesOption = new SipServerAddressesOption();
    }

    public SipServerAddressesOption getSipServerAddressesOption()
    {
        return sipServerAddressesOption;
    }

    public void setSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        if (sipServerAddressesOption != null)
            this.sipServerAddressesOption = sipServerAddressesOption;
    }

    public short getCode()
    {
        return sipServerAddressesOption.getCode();
    }

    public String getName()
    {
        return sipServerAddressesOption.getName();
    }
    
    public List<String> getServerIpAddresses()
    {
        // get a reference to the live list in the XML object
        return sipServerAddressesOption.getServerIpAddresses();
    }
}
