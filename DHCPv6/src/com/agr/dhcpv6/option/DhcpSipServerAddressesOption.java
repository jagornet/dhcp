package com.agr.dhcpv6.option;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static Logger log = LoggerFactory.getLogger(DhcpSipServerAddressesOption.class);

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

    public int getCode()
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
