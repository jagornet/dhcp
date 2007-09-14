package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.VendorClassOption;

/**
 * <p>Title: DhcpVendorClassOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpVendorClassOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpVendorClassOption.class);

    private VendorClassOption vendorClassOption;

    public DhcpVendorClassOption()
    {
        super();
        vendorClassOption = new VendorClassOption();
    }
    public DhcpVendorClassOption(VendorClassOption vendorClassOption)
    {
        super();
        if (vendorClassOption != null)
            this.vendorClassOption = vendorClassOption;
        else
            this.vendorClassOption = new VendorClassOption();
    }

    public VendorClassOption getVendorClassOption()
    {
        return vendorClassOption;
    }

    public void setVendorClassOption(VendorClassOption vendorClassOption)
    {
        if (vendorClassOption != null)
            this.vendorClassOption = vendorClassOption;
    }

    public short getCode()
    {
        return vendorClassOption.getCode();
    }

    public short getLength()
    {
        short len = 4;  // size of enterprise number (int)
        List<OpaqueData> userClasses = vendorClassOption.getVendorClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                len += 2;   // opaque data length
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.agr.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.putInt(vendorClassOption.getEnterpriseNumber());
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClasses();
        if (vendorClasses != null) {
            for (OpaqueData opaque : vendorClasses) {
                OpaqueDataUtil.encode(bb, opaque);
            }
        }
        return (ByteBuffer)bb.flip();        
    }

    /* (non-Javadoc)
     * @see com.agr.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer bb) throws IOException
    {
        // already have the code, so length is next
        short len = bb.getShort();
        if (log.isDebugEnabled())
            log.debug(vendorClassOption.getName() + " reports length=" + len +
                    ":  bytes remaining in buffer=" + bb.remaining());
        short eof = (short)(bb.position() + len);
        if (bb.position() < eof) {
            vendorClassOption.setEnterpriseNumber(bb.getInt());
            while (bb.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decode(bb);
                this.addVendorClass(opaque);
            }
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClasses();
        if (vendorClasses != null) {
            for (OpaqueData opaque : vendorClasses) {
                if (OpaqueDataUtil.matches(expression, opaque)) {
                    // if one of the vendor classes matches the
                    // expression, then consider it a match
                    return true;
                }
            }
        }
        return false;
    }

    public void addVendorClass(String vendorclass)
    {
        if (vendorclass != null) {
            OpaqueData opaque = new OpaqueData();
            opaque.setAsciiValue(vendorclass);
            this.addVendorClass(opaque);
        }
    }

    public void addVendorClass(byte[] vendorclass)
    {
        if (vendorclass != null) {
            OpaqueData opaque = new OpaqueData();
            opaque.setHexValue(vendorclass);
            this.addVendorClass(opaque);
        }
    }

    public void addVendorClass(OpaqueData opaque)
    {
        vendorClassOption.getVendorClasses().add(opaque);        
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(vendorClassOption.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        sb.append(vendorClassOption.getEnterpriseNumber());
        sb.append(" ");
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClasses();
        if (vendorClasses != null) {
            for (OpaqueData opaque : vendorClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
