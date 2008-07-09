package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: BaseOpaqueDataOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseOpaqueDataOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseOpaqueDataOption.class);

    public abstract OpaqueData getOpaqueData();
    
    public int getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(this.getOpaqueData());
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        OpaqueDataUtil.encodeDataOnly(iobuf, this.getOpaqueData());
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf); 
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            while (iobuf.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decodeDataOnly(iobuf, len);
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
