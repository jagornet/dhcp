package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.DnsServersOption;

/**
 * <p>Title: DhcpDnsServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpDnsServersOption extends BaseServerAddressesOption
{
    private DnsServersOption dnsServersOption;

    public DhcpDnsServersOption()
    {
        super();
        dnsServersOption = DnsServersOption.Factory.newInstance();
    }
    public DhcpDnsServersOption(DnsServersOption dnsServersOption)
    {
        super();
        if (dnsServersOption != null)
            this.dnsServersOption = dnsServersOption;
        else
            this.dnsServersOption = DnsServersOption.Factory.newInstance();
    }

    public DnsServersOption getDnsServersOption()
    {
        return dnsServersOption;
    }

    public void setDnsServersOption(DnsServersOption dnsServersOption)
    {
        if (dnsServersOption != null)
            this.dnsServersOption = dnsServersOption;
    }

    public int getCode()
    {
        return dnsServersOption.getCode();
    }
    
    @Override
    public List<String> getServerIpAddresses()
    {
        return Arrays.asList(dnsServersOption.getServerIpAddressesArray());
    }
    
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		dnsServersOption.addServerIpAddresses(serverIpAddress);
	}
}
