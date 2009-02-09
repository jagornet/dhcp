package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.Option;
import com.jagornet.dhcpv6.xml.VendorInfoOption;

/**
 * <p>Title: DhcpVendorClassOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpVendorInfoOption extends BaseDhcpOption
{
    private VendorInfoOption vendorInfoOption;

    public DhcpVendorInfoOption()
    {
        super();
        vendorInfoOption = VendorInfoOption.Factory.newInstance();
    }
    public DhcpVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        super();
        if (vendorInfoOption != null)
            this.vendorInfoOption = vendorInfoOption;
        else
            this.vendorInfoOption = VendorInfoOption.Factory.newInstance();
    }

    public VendorInfoOption getVendorInfoOption()
    {
        return vendorInfoOption;
    }

    public void setVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        if (vendorInfoOption != null)
            this.vendorInfoOption = vendorInfoOption;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)vendorInfoOption.getEnterpriseNumber());
        Option[] subopts = vendorInfoOption.getSuboptionsArray();
        if ((subopts != null) && (subopts.length > 0)) {
            for (Option subopt : subopts) {
                iobuf.putShort((short)subopt.getCode());  // suboption code
                OpaqueData opaque = subopt.getData();
                if (opaque != null) {
                    OpaqueDataUtil.encode(iobuf, opaque);
                }
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
                vendorInfoOption.setEnterpriseNumber(iobuf.getUnsignedInt());
                while (iobuf.position() < eof) {
                    Option subopt = Option.Factory.newInstance();
                    subopt.setCode(iobuf.getUnsignedShort());
                    OpaqueData opaque = OpaqueDataUtil.decode(iobuf);
                    subopt.setData(opaque);
                    this.addVendorSubOption(subopt);
                }
            }
        }
    }

    public int getLength()
    {
        int len = 4;  // size of enterprise number (int)
        Option[] subopts = vendorInfoOption.getSuboptionsArray();
        if ((subopts != null) && (subopts.length > 0)) {
            for (Option subopt : subopts) {
                len += 2;   // suboption code
                OpaqueData opaque = subopt.getData();
                if (opaque != null) {
                    len += 2;   // opaque data len
                    len += OpaqueDataUtil.getLength(opaque);
                }
            }
        }
        return len;
    }

    public void addVendorSubOption(Option subopt)
    {
        vendorInfoOption.addNewSuboptions().set(subopt);
    }

    public int getCode()
    {
        return vendorInfoOption.getCode();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        sb.append(vendorInfoOption.getEnterpriseNumber());
        sb.append(" ");
        Option[] suboptions = vendorInfoOption.getSuboptionsArray();
        if ((suboptions != null) && (suboptions.length > 0)) {
            sb.append("suboptions: ");
            for (Option subopt : suboptions) {
                sb.append(subopt.getCode());
                sb.append("=");
                sb.append(OpaqueDataUtil.toString(subopt.getData()));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
