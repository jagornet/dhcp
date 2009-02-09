package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.SntpServersOption;

/**
 * <p>Title: DhcpSntpServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSntpServersOption extends BaseServerAddressesOption
{
    private SntpServersOption sntpServersOption;

    public DhcpSntpServersOption()
    {
        super();
        sntpServersOption = SntpServersOption.Factory.newInstance();
    }
    public DhcpSntpServersOption(SntpServersOption sntpServersOption)
    {
        super();
        if (sntpServersOption != null)
            this.sntpServersOption = sntpServersOption;
        else
            this.sntpServersOption = SntpServersOption.Factory.newInstance();
    }

    public SntpServersOption getSntpServersOption()
    {
        return sntpServersOption;
    }

    public void setSntpServersOption(SntpServersOption sntpServersOption)
    {
        if (sntpServersOption != null)
            this.sntpServersOption = sntpServersOption;
    }

    public int getCode()
    {
        return sntpServersOption.getCode();
    }
    
    @Override
    public List<String> getServerIpAddresses()
    {
        return Arrays.asList(sntpServersOption.getServerIpAddressesArray());
    }
    
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		sntpServersOption.addServerIpAddresses(serverIpAddress);
	}
}
