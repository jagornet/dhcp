package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * <p>Title: Encodable </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface Encodable
{

    public abstract IoBuffer encode() throws IOException;

}
