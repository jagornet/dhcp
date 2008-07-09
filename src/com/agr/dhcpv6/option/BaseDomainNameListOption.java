package com.agr.dhcpv6.option;

import java.io.IOException;
import java.util.List;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Title: BaseDomainNameListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseDomainNameListOption extends BaseDhcpOption
{
	private static Logger log = LoggerFactory.getLogger(BaseDomainNameListOption.class);

    public abstract List<String> getDomainNames();
    
    public IoBuffer encode() throws IOException
    {
    	IoBuffer iobuf = super.encodeCodeAndLength();
        List<String> domainNames = this.getDomainNames();
        if (domainNames != null) {
            for (String domain : domainNames) {
                BaseDomainNameOption.encodeDomainName(iobuf, domain);
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
                String domain = BaseDomainNameOption.decodeDomainName(iobuf, eof);
                this.addDomainName(domain);
            }
        }
    }

    public int getLength()
    {
        int len = 0;
        List<String> domainNames = this.getDomainNames();
        if (domainNames != null) {
            for (String domain : domainNames) {
                len += BaseDomainNameOption.getDomainNameLength(domain);
            }
        }
        return len;
    }

    public void addDomainName(String domain)
    {
        if (domain != null) {
            this.getDomainNames().add(domain);
        }
    }

    public String toString()
    {
        if (this == null)
            return null;
        
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        List<String> domainNames = this.getDomainNames();
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
