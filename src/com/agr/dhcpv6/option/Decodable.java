package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <p>Title: Decodable </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface Decodable
{

    public abstract void decode(ByteBuffer bb) throws IOException;

}
