package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.PreferenceOption;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpPreferenceOption extends BaseDhcpOption
{
    private PreferenceOption preferenceOption;

    public DhcpPreferenceOption()
    {
        super();
        this.preferenceOption = PreferenceOption.Factory.newInstance();
    }
    
    public DhcpPreferenceOption(PreferenceOption preferenceOption)
    {
        super();
        if (preferenceOption != null)
            this.preferenceOption = preferenceOption;
        else
            this.preferenceOption = PreferenceOption.Factory.newInstance();
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
        iobuf.put((byte)preferenceOption.getShortValue());
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            preferenceOption.setShortValue(iobuf.getUnsigned());
        }
    }

    public int getCode()
    {
        return preferenceOption.getCode();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(preferenceOption.getShortValue());
        return sb.toString();
    }
    
}
