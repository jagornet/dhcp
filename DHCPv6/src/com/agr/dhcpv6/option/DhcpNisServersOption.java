package com.agr.dhcpv6.option;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.NisServersOption;

/**
 * <p>Title: DhcpNisServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisServersOption extends BaseServerAddressesOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpNisServersOption.class);

    private NisServersOption nisServersOption;

    public DhcpNisServersOption()
    {
        super();
        nisServersOption = new NisServersOption();
    }
    public DhcpNisServersOption(NisServersOption nisServersOption)
    {
        super();
        if (nisServersOption != null)
            this.nisServersOption = nisServersOption;
        else
            this.nisServersOption = new NisServersOption();
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

    public String getName()
    {
        return nisServersOption.getName();
    }
    
    public List<String> getServerIpAddresses()
    {
        // get a reference to the live list in the XML object
        return nisServersOption.getServerIpAddresses();
    }
}
