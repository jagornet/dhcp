package com.jagornet.dhcpv6.option;

import java.util.Arrays;
import java.util.List;

import com.jagornet.dhcpv6.xml.DomainSearchListOption;

/**
 * <p>Title: DhcpDomainSearchListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpDomainSearchListOption extends BaseDomainNameListOption
{
    private DomainSearchListOption domainSearchListOption;

    public DhcpDomainSearchListOption()
    {
        super();
        domainSearchListOption = DomainSearchListOption.Factory.newInstance();
    }
    public DhcpDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        super();
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
        else
            this.domainSearchListOption = DomainSearchListOption.Factory.newInstance();
    }

    public DomainSearchListOption getDomainSearchListOption()
    {
        return domainSearchListOption;
    }

    public void setDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
    }

    public int getCode()
    {
        return domainSearchListOption.getCode();
    }

    @Override
    public List<String> getDomainNames()
    {
    	return Arrays.asList(domainSearchListOption.getDomainNamesArray());
    }
    
	@Override
	public void addDomainName(String domainName)
	{
		domainSearchListOption.addDomainNames(domainName);
	}
}
