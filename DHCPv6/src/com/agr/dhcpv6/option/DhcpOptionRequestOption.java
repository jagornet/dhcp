package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.OptionRequestOption;

/**
 * <p>Title: DhcpOptionRequestOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpOptionRequestOption implements DhcpOption
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

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        List<Short> oros = optionRequestOption.getRequestedOptionCodes();
        if (oros != null) {
            for (Short oro : oros) {
                bb.putShort(oro);
            }
        }
        return (ByteBuffer)bb.flip();
    }

    public short getCode()
    {
        return optionRequestOption.getCode();
    }
    
    public void decode(ByteBuffer bb) throws IOException
    {
        // already have the code, so length is next
        short len = bb.getShort();
        if (log.isDebugEnabled())
            log.debug(optionRequestOption.getName() + " reports length=" + len +
                      ":  bytes remaining in buffer=" + bb.remaining());
        for (int i=0; i<len/2; i++) {
            if (bb.hasRemaining()) {
                this.addRequestedOptionCode(bb.getShort());
            }
        }
    }

    public short getLength()
    {
        short len = 0;
        List<Short> oros = optionRequestOption.getRequestedOptionCodes();
        if (oros != null) {
            len = (short)(oros.size() * 2);
        }
        return len;
    }

    public void addRequestedOptionCode(Short code)
    {
        if (code != null) {
            optionRequestOption.getRequestedOptionCodes().add(code);
        }
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(optionRequestOption.getName());
        sb.append(": ");
        List<Short> oros = optionRequestOption.getRequestedOptionCodes();
        if (oros != null) {
            for (Short oro : oros) {
                sb.append(oro);
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
