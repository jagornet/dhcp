package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <p>Title: Encodable </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface Encodable
{

    public abstract ByteBuffer encode() throws IOException;

}
