package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.PreferenceOption;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpPreferenceOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(DhcpPreferenceOption.class);

    private PreferenceOption preferenceOption;

    public DhcpPreferenceOption()
    {
        super();
        this.preferenceOption = new PreferenceOption();
    }
    public DhcpPreferenceOption(PreferenceOption preferenceOption)
    {
        super();
        if (preferenceOption != null)
            this.preferenceOption = preferenceOption;
        else
            this.preferenceOption = new PreferenceOption();
    }

    public PreferenceOption getPreferenceOption()
    {
        return preferenceOption;
    }

    public void setPreferenceOption(PreferenceOption preferenceOption)
    {
        if (preferenceOption != null)
            this.preferenceOption = preferenceOption;
    }

    public short getCode()
    {
        return preferenceOption.getCode();
    }

    public short getLength()
    {
        return 1;   // always one bytes
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.put(preferenceOption.getValue());
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(preferenceOption.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            if (bb.remaining() > 0) {
                preferenceOption.setValue(bb.get());
            }
        }
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(preferenceOption.getName());
        sb.append(": ");
        sb.append(preferenceOption.getValue());
        return sb.toString();
    }
    
}
