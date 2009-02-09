package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.NisServersOption;

/**
 * <p>Title: DhcpNisServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisServersOption extends BaseServerAddressesOption
{
    private NisServersOption nisServersOption;

    public DhcpNisServersOption()
    {
        super();
        nisServersOption = NisServersOption.Factory.newInstance();
    }
    public DhcpNisServersOption(NisServersOption nisServersOption)
    {
        super();
        if (nisServersOption != null)
            this.nisServersOption = nisServersOption;
        else
            this.nisServersOption = NisServersOption.Factory.newInstance();
    }

    public NisServersOption getNisServersOption()
    {
        return nisServersOption;
    }

    public void setNisServersOption(NisServersOption nisServersOption)
    {
        if (nisServersOption != null)
            this.nisServersOption = nisServersOption;
    }

    public int getCode()
    {
        return nisServersOption.getCode();
    }
    
    @Override
    public List<String> getServerIpAddresses()
    {
        return Arrays.asList(nisServersOption.getServerIpAddressesArray());
    }
    
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		nisServersOption.addServerIpAddresses(serverIpAddress);
	}
}
