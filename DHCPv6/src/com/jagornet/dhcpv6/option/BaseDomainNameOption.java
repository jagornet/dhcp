/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDomainNameOption.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Title: DhcpDomainSearchListOption
 * Description: The abstract base class for domain name DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameOption extends BaseDhcpOption
{
    /**
     * Gets the domain name.
     * 
     * @return the domain name
     */
    public abstract String getDomainName();
    
    /**
     * Sets the domain name.
     * 
     * @param domainName the new domain name
     */
    public abstract void setDomainName(String domainName);
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        String domainName = this.getDomainName();
        if (domainName != null) {
            encodeDomainName(iobuf, domainName);
        }
        return iobuf.flip().buf();
    }
    
    /**
     * Encode domain name.
     * 
     * @param iobuf the iobuf
     * @param domain the domain
     */
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

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	IoBuffer iobuf = IoBuffer.wrap(buf);
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            String domain = decodeDomainName(iobuf, eof);
            this.setDomainName(domain);
        }
    }
    
    /**
     * Decode domain name.
     * 
     * @param iobuf the iobuf
     * @param eof the eof
     * 
     * @return the string
     */
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

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        String domainName = this.getDomainName();
        if (domainName != null) {
            len = getDomainNameLength(domainName);
        }
        return len;
    }

    /**
     * Gets the domain name length.
     * 
     * @param domainName the domain name
     * 
     * @return the domain name length
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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
