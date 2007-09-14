package com.agr.dhcpv6.option;

import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: DhcpComparableOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface DhcpComparableOption
{

    public abstract boolean matches(OptionExpression expression);

}
