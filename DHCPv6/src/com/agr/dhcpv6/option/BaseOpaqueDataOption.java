package com.agr.dhcpv6.option;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.ServerIdOption;

/**
 * <p>Title: BaseOpaqueDataOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseOpaqueDataOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(BaseOpaqueDataOption.class);

    public abstract short getCode();
    public abstract String getName();
    public abstract OpaqueData getOpaqueData();
    
    public short getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(this.getOpaqueData());
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        OpaqueDataUtil.encodeDataOnly(bb, this.getOpaqueData());
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(this.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            while (bb.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decodeDataOnly(bb, len);
                if (opaque.getAsciiValue() != null) {
                    this.getOpaqueData().setAsciiValue(opaque.getAsciiValue());
                }
                else {
                    this.getOpaqueData().setHexValue(opaque.getHexValue());
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

        return OpaqueDataUtil.matches(expression, this.getOpaqueData());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BaseOpaqueDataOption) {
            BaseOpaqueDataOption that = (BaseOpaqueDataOption) obj;
            return OpaqueDataUtil.equals(this.getOpaqueData(), 
                                         that.getOpaqueData());
        }
        return false;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        sb.append(OpaqueDataUtil.toString(this.getOpaqueData()));
        return sb.toString();
    }
}
