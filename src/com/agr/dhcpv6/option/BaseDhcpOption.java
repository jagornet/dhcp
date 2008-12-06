package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDhcpOption implements DhcpOption
{
	private static Logger log = LoggerFactory.getLogger(BaseDhcpOption.class);

    /**
     * Encode the DHCP option code and length fields of any DHCP option
     * @return the IoBuffer containing the encoded code and length fields
     * @throws IOException
     */
    public IoBuffer encodeCodeAndLength() throws IOException
	{
		IoBuffer iobuf = IoBuffer.allocate(2 + 2 + getLength());
		iobuf.putShort((short)getCode());
		iobuf.putShort((short)getLength());
		return iobuf;
	}

    /**
     * Decode the DHCP option length field of any DHCP option.  Because we have a
     * DhcpOptionFactory to build the option based on the code, then the code is already
     * decoded, so this method is invoked by the concrete class to decode the length
     * @param iobuf the IoBuffer containing the data to be decoded
     * @return the length of the option, or zero if there is no data for the option
     * @throws IOException
     */
	public int decodeLength(IoBuffer iobuf) throws IOException
	{
        if ((iobuf != null) && iobuf.hasRemaining()) {
            // already have the code, so length is next
            int len = iobuf.getUnsignedShort();
            if (log.isDebugEnabled())
                log.debug(getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + iobuf.remaining());
            return len;
        }
        return 0;
	}

}
