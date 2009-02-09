package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.SipServerAddressesOption;

/**
 * <p>Title: DhcpSipServerAddressesOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSipServerAddressesOption extends BaseServerAddressesOption
{
    private SipServerAddressesOption sipServerAddressesOption;

    public DhcpSipServerAddressesOption()
    {
        super();
        sipServerAddressesOption = SipServerAddressesOption.Factory.newInstance();
    }
    public DhcpSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        super();
        if (sipServerAddressesOption != null)
            this.sipServerAddressesOption = sipServerAddressesOption;
        else
            this.sipServerAddressesOption = SipServerAddressesOption.Factory.newInstance();
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
    
    @Override
    public List<String> getServerIpAddresses()
    {
        return Arrays.asList(sipServerAddressesOption.getServerIpAddressesArray());
    }
    
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		sipServerAddressesOption.addServerIpAddresses(serverIpAddress);
	}
}
