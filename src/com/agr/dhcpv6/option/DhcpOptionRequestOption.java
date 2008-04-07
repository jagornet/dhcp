package com.agr.dhcpv6.option;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;

import com.agr.dhcpv6.server.config.xml.OptionRequestOption;

/**
 * <p>Title: DhcpOptionRequestOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpOptionRequestOption extends BaseDhcpOption
{
    private static Log log = LogFactory.getLog(DhcpOptionRequestOption.class);
    
    private OptionRequestOption optionRequestOption;

    public DhcpOptionRequestOption()
    {
        super();
        this.optionRequestOption = new OptionRequestOption();
    }
    public DhcpOptionRequestOption(OptionRequestOption optionRequestOption)
    {
        super();
        if (optionRequestOption != null)
            this.optionRequestOption = optionRequestOption;
        else
            this.optionRequestOption = new OptionRequestOption();
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
        List<Integer> oros = optionRequestOption.getRequestedOptionCodes();
        if (oros != null) {
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
        List<Integer> oros = optionRequestOption.getRequestedOptionCodes();
        if (oros != null) {
            len = oros.size() * 2;
        }
        return len;
    }

    public void addRequestedOptionCode(Integer code)
    {
        if (code != null) {
            optionRequestOption.getRequestedOptionCodes().add(code);
        }
    }

    public int getCode()
    {
        return optionRequestOption.getCode();
    }

    public String getName()
    {
        return optionRequestOption.getName();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(optionRequestOption.getName());
        sb.append(": ");
        List<Integer> oros = optionRequestOption.getRequestedOptionCodes();
        if (oros != null) {
            for (Integer oro : oros) {
                sb.append(oro);
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
