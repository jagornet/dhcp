package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;

/**
 * <p>Title: DhcpDomainSearchListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseDomainNameOption extends BaseDhcpOption
{
    private static Log log = LogFactory.getLog(BaseDomainNameOption.class);

    public abstract String getDomainName();
    public abstract void setDomainName(String domainName);
    
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        String domainName = this.getDomainName();
        if (domainName != null) {
            encodeDomainName(iobuf, domainName);
        }
        return iobuf.flip();
    }
    
    public static void encodeDomainName(IoBuffer iobuf, String domain)
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
                iobuf.put((byte)label.length());
                iobuf.put(label.getBytes());
            }
            iobuf.put((byte)0);    // terminate with zero-length "root" label
        }
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            String domain = decodeDomainName(iobuf, eof);
            this.setDomainName(domain);
        }
    }
    
    public static String decodeDomainName(IoBuffer iobuf, int eof)
    {
        StringBuilder domain = new StringBuilder();
        while (iobuf.position() < eof) {
            short l = iobuf.getUnsigned();  // length byte as short
            if (l == 0)
                break;      // terminating null "root" label
            byte b[] = new byte[l];
            iobuf.get(b);      // get next label
            domain.append(new String(b));
            domain.append(".");     // build the FQDN by appending labels
        }
        return domain.toString();
    }

    public int getLength()
    {
        int len = 0;
        String domainName = this.getDomainName();
        if (domainName != null) {
            len = getDomainNameLength(domainName);
        }
        return len;
    }

    public static int getDomainNameLength(String domainName)
    {
        int len = 0;
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
