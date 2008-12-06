package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * <p>Title: Decodable </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface Decodable
{

    public abstract void decode(IoBuffer iobuf) throws IOException;

}
