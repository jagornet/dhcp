package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.ServerIdOption;

/**
 * <p>Title: DhcpServerIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpServerIdOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpServerIdOption.class);
    
    private ServerIdOption serverIdOption;
    
    public DhcpServerIdOption()
    {
        super();
        serverIdOption = new ServerIdOption();
    }
    public DhcpServerIdOption(ServerIdOption serverIdOption)
    {
        super();
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
        else
            this.serverIdOption = new ServerIdOption();
    }

    public ServerIdOption getServerIdOption()
    {
        return serverIdOption;
    }

    public void setServerIdOption(ServerIdOption serverIdOption)
    {
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
    }

    public short getCode()
    {
        return serverIdOption.getCode();
    }

    public short getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(serverIdOption);
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        OpaqueDataUtil.encodeDataOnly(bb, serverIdOption);
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(serverIdOption.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            while (bb.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decodeDataOnly(bb, len);
                if (opaque.getAsciiValue() != null) {
                    serverIdOption.setAsciiValue(opaque.getAsciiValue());
                }
                else {
                    serverIdOption.setHexValue(opaque.getHexValue());
                }
            }
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        return OpaqueDataUtil.matches(expression, serverIdOption);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(serverIdOption.getName());
        sb.append(": ");
        sb.append(OpaqueDataUtil.toString(serverIdOption));
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DhcpServerIdOption) {
            DhcpServerIdOption that = (DhcpServerIdOption) obj;
            return OpaqueDataUtil.equals(this.getServerIdOption(), 
                                         that.getServerIdOption());
        }
        return false;
    }
}
