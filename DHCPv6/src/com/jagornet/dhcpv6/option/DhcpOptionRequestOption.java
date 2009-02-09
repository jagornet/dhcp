package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.OptionRequestOption;

/**
 * <p>Title: DhcpOptionRequestOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpOptionRequestOption extends BaseDhcpOption
{
    private OptionRequestOption optionRequestOption;

    public DhcpOptionRequestOption()
    {
        super();
        this.optionRequestOption = OptionRequestOption.Factory.newInstance();
    }
    public DhcpOptionRequestOption(OptionRequestOption optionRequestOption)
    {
        super();
        if (optionRequestOption != null)
            this.optionRequestOption = optionRequestOption;
        else
            this.optionRequestOption = OptionRequestOption.Factory.newInstance();
    }

    public OptionRequestOption getOptionRequestOption()
    {
        return optionRequestOption;
    }

    public void setOptionRequestOption(OptionRequestOption optionRequestOption)
    {
        if (optionRequestOption != null)
            this.optionRequestOption = optionRequestOption;
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        int[] oros = optionRequestOption.getRequestedOptionCodesArray();
        if ((oros != null) && (oros.length > 0)) {
            for (int oro : oros) {
                iobuf.putShort((short)oro);
            }
        }
        return iobuf.flip();
    }
    
    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            for (int i=0; i<len/2; i++) {
                if (iobuf.hasRemaining()) {
                    this.addRequestedOptionCode(iobuf.getUnsignedShort());
                }
            }
        }
    }

    public int getLength()
    {
        int len = 0;
        int[] oros = optionRequestOption.getRequestedOptionCodesArray();
        if ((oros != null) && (oros.length > 0)) {
            len = oros.length * 2;
        }
        return len;
    }

    public void addRequestedOptionCode(Integer code)
    {
        if (code != null) {
        	optionRequestOption.addRequestedOptionCodes(code);
        }
    }

    public int getCode()
    {
        return optionRequestOption.getCode();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        int[] oros = optionRequestOption.getRequestedOptionCodesArray();
        if ((oros != null)  && (oros.length > 0)) {
            for (Integer oro : oros) {
                sb.append(oro);
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
