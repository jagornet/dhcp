package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.UserClassOption;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpUserClassOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpUserClassOption.class);

    private UserClassOption userClassOption;

    public DhcpUserClassOption()
    {
        super();
        userClassOption = new UserClassOption();
    }
    public DhcpUserClassOption(UserClassOption userClassOption)
    {
        super();
        if (userClassOption != null)
            this.userClassOption = userClassOption;
        else
            this.userClassOption = new UserClassOption();
    }

    public UserClassOption getUserClassOption()
    {
        return userClassOption;
    }

    public void setUserClassOption(UserClassOption userClassOption)
    {
        if (userClassOption != null)
            this.userClassOption = userClassOption;
    }

    public short getCode()
    {
        return userClassOption.getCode();
    }

    public short getLength()
    {
        short len = 0;
        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                OpaqueDataUtil.encode(bb, opaque);
            }
        }
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        // already have the code, so length is next
        short len = bb.getShort();
        if (log.isDebugEnabled())
            log.debug(userClassOption.getName() + " reports length=" + len +
                      ":  bytes remaining in buffer=" + bb.remaining());
        short eof = (short)(bb.position() + len);
        while (bb.position() < eof) {
            OpaqueData opaque = OpaqueDataUtil.decode(bb);
            this.addUserClass(opaque);
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                if (OpaqueDataUtil.matches(expression, opaque)) {
                    // if one of the user classes matches the
                    // expression, then consider it a match
                    return true;
                }
            }
        }
        return false;
    }

    public void addUserClass(String userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = new OpaqueData();
            opaque.setAsciiValue(userclass);
            this.addUserClass(opaque);
        }
    }

    public void addUserClass(byte[] userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = new OpaqueData();
            opaque.setHexValue(userclass);
            this.addUserClass(opaque);
        }
    }
    
    public void addUserClass(OpaqueData opaque)
    {
        userClassOption.getUserClasses().add(opaque);
    }

    public String toString()
    {
        if (userClassOption == null)
            return null;
        
        StringBuilder sb = new StringBuilder(userClassOption.getName());
        sb.append(": ");
        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
