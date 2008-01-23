package com.agr.dhcpv6.option;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.DnsServersOption;

/**
 * <p>Title: DhcpDnsServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpDnsServersOption extends BaseServerAddressesOption
{
    private static Log log = LogFactory.getLog(DhcpDnsServersOption.class);

    private DnsServersOption dnsServersOption;

    public DhcpDnsServersOption()
    {
        super();
        dnsServersOption = new DnsServersOption();
    }
    public DhcpDnsServersOption(DnsServersOption dnsServersOption)
    {
        super();
        if (dnsServersOption != null)
            this.dnsServersOption = dnsServersOption;
        else
            this.dnsServersOption = new DnsServersOption();
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

    public short getCode()
    {
        return dnsServersOption.getCode();
    }

    public String getName()
    {
        return dnsServersOption.getName();
    }
    
    public List<String> getServerIpAddresses()
    {
        // get a reference to the live list in the XML object
        return dnsServersOption.getServerIpAddresses();
    }
}
