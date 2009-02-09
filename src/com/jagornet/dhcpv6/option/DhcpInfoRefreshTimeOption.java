package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.InfoRefreshTimeOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.util.Util;

/**
 * <p>Title: DhcpInfoRefreshTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpInfoRefreshTimeOption extends BaseDhcpOption implements DhcpComparableOption
{
    private InfoRefreshTimeOption infoRefreshTimeOption;
    
    public DhcpInfoRefreshTimeOption()
    {
        super();
        infoRefreshTimeOption = InfoRefreshTimeOption.Factory.newInstance();
    }
    public DhcpInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        super();
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
        else
            this.infoRefreshTimeOption = InfoRefreshTimeOption.Factory.newInstance();
    }

    public InfoRefreshTimeOption getInfoRefreshTimeOption()
    {
        return infoRefreshTimeOption;
    }

    public void setInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
    }

    public int getLength()
    {
        return 4;   // always four bytes (int)
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)infoRefreshTimeOption.getLongValue());
        return (IoBuffer)iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf); 
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		infoRefreshTimeOption.setLongValue(iobuf.getUnsignedInt());
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        OpaqueData opaque = expression.getData();
        if (opaque != null) {
            String ascii = opaque.getAsciiValue();
            if (ascii != null) {
                try {
                	// need a long to handle unsigned int
                    if (infoRefreshTimeOption.getLongValue() == Long.parseLong(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 4) ) {
                	long hexLong = Long.valueOf(Util.toHexString(hex), 16);
                    if (infoRefreshTimeOption.getLongValue() == hexLong) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getCode()
    {
        return infoRefreshTimeOption.getCode();
    }
   
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(infoRefreshTimeOption.getLongValue());
        return sb.toString();
    }    
}
