package com.agr.dhcpv6.option;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.SntpServersOption;

/**
 * <p>Title: DhcpSntpServersOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSntpServersOption extends BaseServerAddressesOption
{
    private static Log log = LogFactory.getLog(DhcpSntpServersOption.class);

    private SntpServersOption sntpServersOption;

    public DhcpSntpServersOption()
    {
        super();
        sntpServersOption = new SntpServersOption();
    }
    public DhcpSntpServersOption(SntpServersOption sntpServersOption)
    {
        super();
        if (sntpServersOption != null)
            this.sntpServersOption = sntpServersOption;
        else
            this.sntpServersOption = new SntpServersOption();
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

    public short getCode()
    {
        return sntpServersOption.getCode();
    }

    public String getName()
    {
        return sntpServersOption.getName();
    }
    
    public List<String> getServerIpAddresses()
    {
        // get a reference to the live list in the XML object
        return sntpServersOption.getServerIpAddresses();
    }
}
