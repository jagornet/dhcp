package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.InfoRefreshTimeOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.util.Util;

/**
 * <p>Title: DhcpInfoRefreshTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpInfoRefreshTimeOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpInfoRefreshTimeOption.class);
    
    private InfoRefreshTimeOption infoRefreshTimeOption;
    
    public DhcpInfoRefreshTimeOption()
    {
        super();
        infoRefreshTimeOption = new InfoRefreshTimeOption();
    }
    public DhcpInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        super();
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
        else
            this.infoRefreshTimeOption = new InfoRefreshTimeOption();
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
        iobuf.putInt((int)infoRefreshTimeOption.getValue());
        return (IoBuffer)iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf); 
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		infoRefreshTimeOption.setValue(iobuf.getUnsignedInt());
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
                    if (infoRefreshTimeOption.getValue() == Long.parseLong(ascii)) {
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
                    if (infoRefreshTimeOption.getValue() == hexLong) {
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

    public String getName()
    {
        return infoRefreshTimeOption.getName();
    }
   
    public String toString()
    {
        StringBuilder sb = new StringBuilder(infoRefreshTimeOption.getName());
        sb.append(": ");
        sb.append(infoRefreshTimeOption.getValue());
        return sb.toString();
    }    
}
