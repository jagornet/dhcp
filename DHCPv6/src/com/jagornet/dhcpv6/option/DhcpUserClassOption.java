package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.UserClassOption;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpUserClassOption extends BaseDhcpOption implements DhcpComparableOption
{
    private UserClassOption userClassOption;

    public DhcpUserClassOption()
    {
        super();
        userClassOption = UserClassOption.Factory.newInstance();
    }
    public DhcpUserClassOption(UserClassOption userClassOption)
    {
        super();
        if (userClassOption != null)
            this.userClassOption = userClassOption;
        else
            this.userClassOption = UserClassOption.Factory.newInstance();
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

    public int getLength()
    {
        int len = 0;
        OpaqueData[] userClasses = userClassOption.getUserClassesArray();
        if ((userClasses != null) && (userClasses.length > 0)) {
            for (OpaqueData opaque : userClasses) {
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        OpaqueData[] userClasses = userClassOption.getUserClassesArray();
        if ((userClasses != null) && (userClasses.length > 0)) {
            for (OpaqueData opaque : userClasses) {
                OpaqueDataUtil.encode(iobuf, opaque);
            }
        }
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		int eof = iobuf.position() + len;
            while (iobuf.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decode(iobuf);
                this.addUserClass(opaque);
            }
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        OpaqueData[] userClasses = userClassOption.getUserClassesArray();
        if ((userClasses != null) && (userClasses.length > 0)) {
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
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setAsciiValue(userclass);
            this.addUserClass(opaque);
        }
    }

    public void addUserClass(byte[] userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setHexValue(userclass);
            this.addUserClass(opaque);
        }
    }
    
    public void addUserClass(OpaqueData opaque)
    {
        userClassOption.addNewUserClasses().set(opaque);
    }

    public int getCode()
    {
        return userClassOption.getCode();
    }
    
    public String toString()
    {
        if (userClassOption == null)
            return null;
        
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        OpaqueData[] userClasses = userClassOption.getUserClassesArray();
        if ((userClasses != null) && (userClasses.length > 0)) {
            for (OpaqueData opaque : userClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
