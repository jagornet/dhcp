package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;

import com.agr.dhcpv6.server.config.xml.PreferenceOption;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpPreferenceOption extends BaseDhcpOption
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

    public int getLength()
    {
        return 1;   // always one bytes
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.put((byte)preferenceOption.getValue());
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            preferenceOption.setValue(iobuf.getUnsigned());
        }
    }

    public int getCode()
    {
        return preferenceOption.getCode();
    }

    public String getName()
    {
        return preferenceOption.getName();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(preferenceOption.getName());
        sb.append(": ");
        sb.append(preferenceOption.getValue());
        return sb.toString();
    }
    
}
