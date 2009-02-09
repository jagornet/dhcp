package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.OptionExpression;

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
