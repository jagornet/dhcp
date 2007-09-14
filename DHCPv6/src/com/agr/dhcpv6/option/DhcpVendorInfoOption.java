package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class DhcpVendorInfoOption implements DhcpOption
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
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.putInt(vendorInfoOption.getEnterpriseNumber());
        List<Option> subopts = vendorInfoOption.getSuboptions();
        if (subopts != null) {
            for (Option subopt : subopts) {
                bb.putShort(subopt.getCode());  // suboption code
                OpaqueData opaque = subopt.getData();
                if (opaque != null) {
                    OpaqueDataUtil.encode(bb, opaque);
                }
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
            log.debug(vendorInfoOption.getName() + " reports length=" + len +
                    ":  bytes remaining in buffer=" + bb.remaining());
        if (bb.remaining() > 3) {
            vendorInfoOption.setEnterpriseNumber(bb.getInt());
            while (bb.remaining() > 3) {
                Option subopt = new Option();
                subopt.setCode(bb.getShort());
                OpaqueData opaque = OpaqueDataUtil.decode(bb);
                subopt.setData(opaque);
                this.addVendorSubOption(subopt);
            }
        }
    }

    public short getCode()
    {
        return vendorInfoOption.getCode();
    }

    public short getLength()
    {
        short len = 4;  // size of enterprise number (int)
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
