package com.agr.dhcpv6.option;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.SipServerDomainNamesOption;

/**
 * <p>Title: DhcpSipServerDomainNamesOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSipServerDomainNamesOption extends BaseDomainNameListOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpSipServerDomainNamesOption.class);

    private SipServerDomainNamesOption sipServerDomainNamesOption;

    public DhcpSipServerDomainNamesOption()
    {
        super();
        sipServerDomainNamesOption = new SipServerDomainNamesOption();
    }
    public DhcpSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        super();
        if (sipServerDomainNamesOption != null)
            this.sipServerDomainNamesOption = sipServerDomainNamesOption;
        else
            this.sipServerDomainNamesOption = new SipServerDomainNamesOption();
    }

    public SipServerDomainNamesOption getSipServerDomainNamesOption()
    {
        return sipServerDomainNamesOption;
    }

    public void setSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        if (sipServerDomainNamesOption != null)
            this.sipServerDomainNamesOption = sipServerDomainNamesOption;
    }

    public int getCode()
    {
        return sipServerDomainNamesOption.getCode();
    }

    public String getName()
    {
        return sipServerDomainNamesOption.getName();
    }

    public List<String> getDomainNames()
    {
        return sipServerDomainNamesOption.getDomainNames();
    }
}
