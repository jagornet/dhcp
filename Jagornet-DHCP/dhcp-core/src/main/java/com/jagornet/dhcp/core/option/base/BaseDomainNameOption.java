/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseDomainNameOption
 * Description: The abstract base class for domain name DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameOption extends BaseDhcpOption
{
	protected String domainName;
	
	public BaseDomainNameOption() {
		this(null);
	}
	
	public BaseDomainNameOption(String domainName) {
		super();
		this.domainName = domainName;
	}
		
    public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

    @Override
    public int getLength()
    {
        int len = 0;
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
        if (domainName != null) {
        	boolean fqdn = domainName.endsWith(".");
	        String[] labels = domainName.split("\\.");
	        if (labels != null) {
	            for (String label : labels) {
	                // each label consists of a length byte and opaqueData
	                len += 1 + label.length();
	            }
	        }
            if (fqdn) {
            	len += 1;   // one extra byte for the zero length terminator
            }
        }
        return len;
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if (domainName != null) {
            encodeDomainName(buf, domainName);
        }
        return (ByteBuffer) buf.flip();
    }
    
    /**
     * Encode domain name.
     * 
     * @param buf the buf
     * @param domain the domain
     */
    public static void encodeDomainName(ByteBuffer buf, String domain)
    {
    	if (domain != null) {
        	boolean fqdn = domain.endsWith(".");
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
	            	buf.put((byte)label.length());
	            	if (label.length() > 0) {
	            		buf.put(label.getBytes());
	            	}
	            }
	            if (fqdn) {
	            	buf.put((byte)0);    // terminate with zero-length "root" label
	            }
	        }
    	}
    }

    @Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            domainName = decodeDomainName(buf, eof);
        }
    }
    
    /**
     * Decode domain name.
     * 
     * @param buf the buf
     * @param eof the eof
     * 
     * @return the string
     */
    public static String decodeDomainName(ByteBuffer buf, int eof)
    {
        StringBuilder domain = new StringBuilder();
        while (buf.position() < eof) {
            short l = Util.getUnsignedByte(buf);  // length byte as short
            if (l == 0)
                break;      // terminating null "root" label
            byte[] b = new byte[l];
            buf.get(b);      // get next label
            domain.append(new String(b));
            if (buf.position() < eof)
            	domain.append('.');     // build the FQDN by appending labels
        }
        return domain.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": domainName=");
        sb.append(domainName);
        return sb.toString();
    }

}
