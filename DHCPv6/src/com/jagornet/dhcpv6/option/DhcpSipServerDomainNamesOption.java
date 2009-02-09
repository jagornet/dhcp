package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.SipServerDomainNamesOption;

/**
 * <p>Title: DhcpSipServerDomainNamesOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSipServerDomainNamesOption extends BaseDomainNameListOption
{
    private SipServerDomainNamesOption sipServerDomainNamesOption;

    public DhcpSipServerDomainNamesOption()
    {
        super();
        sipServerDomainNamesOption = SipServerDomainNamesOption.Factory.newInstance();
    }
    public DhcpSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        super();
        if (sipServerDomainNamesOption != null)
            this.sipServerDomainNamesOption = sipServerDomainNamesOption;
        else
            this.sipServerDomainNamesOption = SipServerDomainNamesOption.Factory.newInstance();
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

    @Override
    public List<String> getDomainNames()
    {
    	return Arrays.asList(sipServerDomainNamesOption.getDomainNamesArray());
    }
    
	@Override
	public void addDomainName(String domainName)
	{
		sipServerDomainNamesOption.addDomainNames(domainName);
	}
}
