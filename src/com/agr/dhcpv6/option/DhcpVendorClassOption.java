package com.agr.dhcpv6.option;

import java.io.IOException;
import java.util.List;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DhcpVendorClassOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpVendorClassOption.class);

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

    public int getLength()
    {
        int len = 4;  // size of enterprise number (int)
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClasses();
        if (vendorClasses != null) {
            for (OpaqueData opaque : vendorClasses) {
                len += 2;   // opaque data length
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.agr.dhcpv6.option.Encodable#encode()
     */
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)vendorClassOption.getEnterpriseNumber());
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClasses();
        if (vendorClasses != null) {
            for (OpaqueData opaque : vendorClasses) {
                OpaqueDataUtil.encode(iobuf, opaque);
            }
        }
        return iobuf.flip();        
    }

    /* (non-Javadoc)
     * @see com.agr.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
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

    public int getCode()
    {
        return vendorClassOption.getCode();
    }

    public String getName()
    {
        return vendorClassOption.getName();
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
