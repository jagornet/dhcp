package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.ClientIdOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpClientIdOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpClientIdOption.class);
    
    private ClientIdOption clientIdOption;
    
    public DhcpClientIdOption()
    {
        super();
        clientIdOption = new ClientIdOption();
    }
    public DhcpClientIdOption(ClientIdOption clientIdOption)
    {
        super();
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
        else
            this.clientIdOption = new ClientIdOption();
    }

    public ClientIdOption getClientIdOption()
    {
        return clientIdOption;
    }

    public void setClientIdOption(ClientIdOption clientIdOption)
    {
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
    }
    
    public short getCode()
    {
        return clientIdOption.getCode();
    }

    public short getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(clientIdOption);
    }
    
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        OpaqueDataUtil.encodeDataOnly(bb, clientIdOption);
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        // already have the code, so length is next
        short len = bb.getShort();
        if (log.isDebugEnabled())
            log.debug(clientIdOption.getName() + " reports length=" + len +
                      ":  bytes remaining in buffer=" + bb.remaining());
        short eof = (short)(bb.position() + len);
        while (bb.position() < eof) {
            OpaqueData opaque = OpaqueDataUtil.decodeDataOnly(bb, len);
            if (opaque.getAsciiValue() != null) {
                clientIdOption.setAsciiValue(opaque.getAsciiValue());
            }
            else {
                clientIdOption.setHexValue(opaque.getHexValue());
            }
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        return OpaqueDataUtil.matches(expression, clientIdOption);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(clientIdOption.getName());
        sb.append(": ");
        sb.append(OpaqueDataUtil.toString(clientIdOption));
        return sb.toString();
    }
}
