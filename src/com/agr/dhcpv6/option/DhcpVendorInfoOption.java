package com.agr.dhcpv6.option;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.Option;
import com.agr.dhcpv6.server.config.xml.VendorInfoOption;

/**
 * <p>Title: DhcpVendorClassOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpVendorInfoOption extends BaseDhcpOption
{
    private static Log log = LogFactory.getLog(DhcpVendorInfoOption.class);

    private VendorInfoOption vendorInfoOption;

    public DhcpVendorInfoOption()
    {
        super();
        vendorInfoOption = new VendorInfoOption();
    }
    public DhcpVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        super();
        if (vendorInfoOption != null)
            this.vendorInfoOption = vendorInfoOption;
        else
            this.vendorInfoOption = new VendorInfoOption();
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
     * @see com.agr.dhcpv6.option.Encodable#encode()
     */
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)vendorInfoOption.getEnterpriseNumber());
        List<Option> subopts = vendorInfoOption.getSuboptions();
        if (subopts != null) {
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
     * @see com.agr.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                vendorInfoOption.setEnterpriseNumber(iobuf.getUnsignedInt());
                while (iobuf.position() < eof) {
                    Option subopt = new Option();
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
        List<Option> subopts = vendorInfoOption.getSuboptions();
        if (subopts != null) {
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
        vendorInfoOption.getSuboptions().add(subopt);
    }

    public int getCode()
    {
        return vendorInfoOption.getCode();
    }

    public String getName()
    {
        return vendorInfoOption.getName();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(vendorInfoOption.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        sb.append(vendorInfoOption.getEnterpriseNumber());
        sb.append(" ");
        List<Option> suboptions = vendorInfoOption.getSuboptions();
        if (suboptions != null) {
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
