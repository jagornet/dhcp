package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.InterfaceIdOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: DhcpInterfaceIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpInterfaceIdOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpInterfaceIdOption.class);
    
    private InterfaceIdOption interfaceIdOption;
    
    public DhcpInterfaceIdOption()
    {
        super();
        interfaceIdOption = new InterfaceIdOption();
    }
    public DhcpInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        super();
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
        else
            this.interfaceIdOption = new InterfaceIdOption();
    }

    public InterfaceIdOption getInterfaceIdOption()
    {
        return interfaceIdOption;
    }

    public void setInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
    }

    public short getCode()
    {
        return interfaceIdOption.getCode();
    }

    public short getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(interfaceIdOption);
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        OpaqueDataUtil.encodeDataOnly(bb, interfaceIdOption);
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(interfaceIdOption.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            while (bb.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decodeDataOnly(bb, len);
                if (opaque.getAsciiValue() != null) {
                    interfaceIdOption.setAsciiValue(opaque.getAsciiValue());
                }
                else {
                    interfaceIdOption.setHexValue(opaque.getHexValue());
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

        return OpaqueDataUtil.matches(expression, interfaceIdOption);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(interfaceIdOption.getName());
        sb.append(": ");
        sb.append(OpaqueDataUtil.toString(interfaceIdOption));
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DhcpInterfaceIdOption) {
            DhcpInterfaceIdOption that = (DhcpInterfaceIdOption) obj;
            return OpaqueDataUtil.equals(this.getInterfaceIdOption(), 
                                         that.getInterfaceIdOption());
        }
        return false;
    }
}
