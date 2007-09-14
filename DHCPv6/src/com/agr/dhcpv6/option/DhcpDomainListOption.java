package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.DomainListOption;

/**
 * <p>Title: DhcpDomainListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpDomainListOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(DhcpDomainListOption.class);

    private DomainListOption domainListOption;

    public DhcpDomainListOption()
    {
        super();
        domainListOption = new DomainListOption();
    }
    public DhcpDomainListOption(DomainListOption domainListOption)
    {
        super();
        if (domainListOption != null)
            this.domainListOption = domainListOption;
        else
            this.domainListOption = new DomainListOption();
    }

    public DomainListOption getDomainListOption()
    {
        return domainListOption;
    }

    public void setDomainListOption(DomainListOption domainListOption)
    {
        if (domainListOption != null)
            this.domainListOption = domainListOption;
    }
    
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        List<String> domainNames = domainListOption.getDomainNames();
        if (domainNames != null) {
            for (String domain : domainNames) {
                encodeDomainName(bb, domain);
            }
        }
        return (ByteBuffer)bb.flip();
    }
    
    public void encodeDomainName(ByteBuffer bb, String domain)
    {
        // we split the human-readable string representing the
        // fully-qualified domain name along the dots, which
        // gives us the list of labels that make up the FQDN
        String[] labels = domain.split("\\.");
        if (labels != null) {
            for (String label : labels) {
                // domain names are encoded according to RFC1035 sec 3.1
                // a 'label' consists of a length byte (i.e. octet) with
                // the two high order bits set to zero (which means each
                // label is limited to 63 bytes) followed by length number
                // of bytes (i.e. octets) which make up the name
                bb.put((byte)label.length());
                bb.put(label.getBytes());
            }
            bb.put((byte)0);    // terminate with zero-length "root" label
        }
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        // already have the code, so length is next
        short len = bb.getShort();
        if (log.isDebugEnabled())
            log.debug(domainListOption.getName() + " reports length=" + len +
                      ":  bytes remaining in buffer=" + bb.remaining());
        short eof = (short)(bb.position() + len);
        while (bb.position() < eof) {
            String domain = decodeDomainName(bb, eof);
            this.addDomainName(domain);
        }
    }
    
    public String decodeDomainName(ByteBuffer bb, short eof)
    {
        StringBuilder domain = new StringBuilder();
        while (bb.position() < eof) {
            byte l = bb.get();  // length byte
            if (l == 0)
                break;      // terminating null "root" label
            byte b[] = new byte[l];
            bb.get(b);      // get next label
            domain.append(new String(b));
            domain.append(".");     // build the FQDN by appending labels
        }
        return domain.toString();
    }

    public short getCode()
    {
        return domainListOption.getCode();
    }

    public short getLength()
    {
        short len = 0;
        List<String> domainNames = domainListOption.getDomainNames();
        if (domainNames != null) {
            for (String domain : domainNames) {
                String[] labels = domain.split("\\.");
                if (labels != null) {
                    for (String label : labels) {
                        // each label consists of a length byte and data
                        len += 1 + label.length();
                    }
                    len += 1;   // one extra byte for the zero length terminator
                }
            }
        }
        return len;
    }

    public void addDomainName(String domain)
    {
        if (domain != null) {
            domainListOption.getDomainNames().add(domain);
        }
    }

    public String toString()
    {
        if (domainListOption == null)
            return null;
        
        StringBuilder sb = new StringBuilder(domainListOption.getName());
        sb.append(": ");
        List<String> domainNames = domainListOption.getDomainNames();
        if (domainNames != null) {
            for (String domain : domainNames) {
                // there is trailing null label domain as per RFC 1035 sec 3.1
                if (domain.length() > 0) {
                    sb.append(domain);
                    sb.append(",");
                }
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
