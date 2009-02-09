package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.NisPlusServersOption;

/**
 * <p>Title: DhcpNisPlusServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisPlusServersOption extends BaseServerAddressesOption
{
    private NisPlusServersOption nisPlusServersOption;

    public DhcpNisPlusServersOption()
    {
        super();
        nisPlusServersOption = NisPlusServersOption.Factory.newInstance();
    }
    public DhcpNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        super();
        if (nisPlusServersOption != null)
            this.nisPlusServersOption = nisPlusServersOption;
        else
            this.nisPlusServersOption = NisPlusServersOption.Factory.newInstance();
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
    
    @Override
    public List<String> getServerIpAddresses()
    {
        return Arrays.asList(nisPlusServersOption.getServerIpAddressesArray());
    }
    
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		nisPlusServersOption.addServerIpAddresses(serverIpAddress);
	}
}
