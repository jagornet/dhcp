package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.VendorClassOption;

/**
 * <p>Title: DhcpVendorClassOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpVendorClassOption extends BaseDhcpOption implements DhcpComparableOption
{
    private VendorClassOption vendorClassOption;

    public DhcpVendorClassOption()
    {
        super();
        vendorClassOption = VendorClassOption.Factory.newInstance();
    }
    public DhcpVendorClassOption(VendorClassOption vendorClassOption)
    {
        super();
        if (vendorClassOption != null)
            this.vendorClassOption = vendorClassOption;
        else
            this.vendorClassOption = VendorClassOption.Factory.newInstance();
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

    public int getLength()
    {
        int len = 4;  // size of enterprise number (int)
        OpaqueData[] vendorClasses = vendorClassOption.getVendorClassesArray();
        if ((vendorClasses != null) && (vendorClasses.length > 0)) {
            for (OpaqueData opaque : vendorClasses) {
                len += 2;   // opaque data length
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)vendorClassOption.getEnterpriseNumber());
        OpaqueData[] vendorClasses = vendorClassOption.getVendorClassesArray();
        if ((vendorClasses != null) && (vendorClasses.length > 0)) {
            for (OpaqueData opaque : vendorClasses) {
                OpaqueDataUtil.encode(iobuf, opaque);
            }
        }
        return iobuf.flip();        
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                vendorClassOption.setEnterpriseNumber(iobuf.getUnsignedInt());
                while (iobuf.position() < eof) {
                    OpaqueData opaque = OpaqueDataUtil.decode(iobuf);
                    this.addVendorClass(opaque);
                }
            }
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        OpaqueData[] vendorClasses = vendorClassOption.getVendorClassesArray();
        if ((vendorClasses != null) && (vendorClasses.length > 0)) {
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
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setAsciiValue(vendorclass);
            this.addVendorClass(opaque);
        }
    }

    public void addVendorClass(byte[] vendorclass)
    {
        if (vendorclass != null) {
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setHexValue(vendorclass);
            this.addVendorClass(opaque);
        }
    }

    public void addVendorClass(OpaqueData opaque)
    {
        vendorClassOption.addNewVendorClasses().set(opaque);        
    }

    public int getCode()
    {
        return vendorClassOption.getCode();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        sb.append(vendorClassOption.getEnterpriseNumber());
        sb.append(" ");
        OpaqueData[] vendorClasses = vendorClassOption.getVendorClassesArray();
        if ((vendorClasses != null) && (vendorClasses.length > 0)) {
            for (OpaqueData opaque : vendorClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
