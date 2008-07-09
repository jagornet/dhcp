package com.agr.dhcpv6.option;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.DomainSearchListOption;

/**
 * <p>Title: DhcpDomainSearchListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpDomainSearchListOption extends BaseDomainNameListOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpDomainSearchListOption.class);

    private DomainSearchListOption domainSearchListOption;

    public DhcpDomainSearchListOption()
    {
        super();
        domainSearchListOption = new DomainSearchListOption();
    }
    public DhcpDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        super();
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
        else
            this.domainSearchListOption = new DomainSearchListOption();
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

    public String getName()
    {
        return domainSearchListOption.getName();
    }

    public List<String> getDomainNames()
    {
        return domainSearchListOption.getDomainNames();
    }
}
