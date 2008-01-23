package com.agr.dhcpv6.option;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.DomainSearchListOption;

/**
 * <p>Title: DhcpDomainSearchListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseDomainNameOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(BaseDomainNameOption.class);

    public abstract short getCode();
    public abstract String getName();
    public abstract String getDomainName();
    public abstract void setDomainName(String domainName);
    
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        String domainName = this.getDomainName();
        if (domainName != null) {
            encodeDomainName(bb, domainName);
        }
        return (ByteBuffer)bb.flip();
    }
    
    public static void encodeDomainName(ByteBuffer bb, String domain)
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
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(this.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            if (bb.position() < eof) {
                String domain = decodeDomainName(bb, eof);
                this.setDomainName(domain);
            }
        }
    }
    
    public static String decodeDomainName(ByteBuffer bb, short eof)
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

    public short getLength()
    {
        short len = 0;
        String domainName = this.getDomainName();
        if (domainName != null) {
            len = getDomainNameLength(domainName);
        }
        return len;
    }

    public static short getDomainNameLength(String domainName)
    {
        short len = 0;
        String[] labels = domainName.split("\\.");
        if (labels != null) {
            for (String label : labels) {
                // each label consists of a length byte and data
                len += 1 + label.length();
            }
            len += 1;   // one extra byte for the zero length terminator
        }
        return len;
    }

    public String toString()
    {
        if (this == null)
            return null;
        
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        String domainName = this.getDomainName();
        if (domainName != null) {
            sb.append(domainName);
        }            
        return sb.toString();
    }

}
