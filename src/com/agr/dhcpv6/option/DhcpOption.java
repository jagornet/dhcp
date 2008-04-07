package com.agr.dhcpv6.option;


/**
 * <p>Title: DhcpOptionInterface </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface DhcpOption extends Encodable, Decodable
{
	// code is unsigned short - java int
    public abstract int getCode();

    public abstract String getName();
    
    // length is unsigned short - java int
    public abstract int getLength();
    
    public abstract String toString();
}
