/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6IaPdOption.java is part of DHCPv6.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.base.BaseDhcpOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcp.xml.V6IaPdOption;

/**
 * The Class DhcpV6IaPdOption.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6IaPdOption extends BaseDhcpOption
{		
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV6IaPdOption.class);
	
	protected long iaId;
	protected long t1;
	protected long t2;
    
	/** The dhcp options sent by the client inside this ia pd option, _NOT_ including any requested ia prefix options. */
	protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();
	
	/** The ia prefix options. */
	private List<DhcpV6IaPrefixOption> iaPrefixOptions = new ArrayList<DhcpV6IaPrefixOption>();

	/**
	 * Instantiates a new dhcp ia pd option.
	 */
	public DhcpV6IaPdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp ia pd option.
	 * 
	 * @param iaPdOption the ia pd option
	 */
	public DhcpV6IaPdOption(V6IaPdOption iaPdOption)
	{
		super();
		if (iaPdOption != null) {
			iaId = iaPdOption.getIaId();
			t1 = iaPdOption.getT1();
			t1 = iaPdOption.getT2();
		}
		setCode(DhcpConstants.OPTION_IA_PD);
	}

	public long getIaId() {
		return iaId;
	}

	public void setIaId(long iaId) {
		this.iaId = iaId;
	}

	public long getT1() {
		return t1;
	}

	public void setT1(long t1) {
		this.t1 = t1;
	}

	public long getT2() {
		return t2;
	}

	public void setT2(long t2) {
		this.t2 = t2;
	}

	/**
	 * Gets the dhcp option map.
	 * 
	 * @return the dhcp option map
	 */
	public Map<Integer, DhcpOption> getDhcpOptionMap() {
		return dhcpOptions;
	}

	/**
	 * Sets the dhcp option map.
	 * 
	 * @param dhcpOptions the dhcp options
	 */
	public void setDhcpOptionMap(Map<Integer, DhcpOption> dhcpOptions) {
		this.dhcpOptions = dhcpOptions;
	}

	/**
	 * Put all dhcp options.
	 * 
	 * @param dhcpOptions the dhcp options
	 */
	public void putAllDhcpOptions(Map<Integer, DhcpOption> dhcpOptions) {
		this.dhcpOptions.putAll(dhcpOptions);
	}
	
	/**
	 * Implement DhcpOptionable.
	 * 
	 * @param dhcpOption the dhcp option
	 */
	public void putDhcpOption(DhcpOption dhcpOption)
	{
		dhcpOptions.put(dhcpOption.getCode(), dhcpOption);
	}

	/**
	 * Gets the ia prefix options.
	 * 
	 * @return the ia prefix options
	 */
	public List<DhcpV6IaPrefixOption> getIaPrefixOptions() {
		return iaPrefixOptions;
	}

	/**
	 * Sets the ia prefix options.
	 * 
	 * @param iaPrefixOptions the new ia prefix options
	 */
	public void setIaPrefixOptions(List<DhcpV6IaPrefixOption> iaPrefixOptions) {
		this.iaPrefixOptions = iaPrefixOptions;
	}

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
    	return getDecodedLength();
    }

    /**
     * Gets the decoded length.
     * 
     * @return the decoded length
     */
    public int getDecodedLength()
    {
    	int len = 4 + 4 + 4;	// iaId + t1 + t2
    	if (iaPrefixOptions != null) {
    		for (DhcpV6IaPrefixOption iaPrefixOption : iaPrefixOptions) {
    			// code(short) + len(short) + data_len
				len += 4 + iaPrefixOption.getDecodedLength();
			}
    	}
    	if (dhcpOptions != null) {
    		for (DhcpOption dhcpOption : dhcpOptions.values()) {
    			// code(short) + len(short) + data_len
				len += 4 + dhcpOption.getLength();
			}
    	}
        return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putInt((int)iaId);
        buf.putInt((int)t1);
        buf.putInt((int)t2);

        if (iaPrefixOptions != null) {
        	for (DhcpV6IaPrefixOption iaPrefixOption : iaPrefixOptions) {
				ByteBuffer _buf = iaPrefixOption.encode();
				if (_buf != null) {
					buf.put(_buf);
				}
			}
        }
        if (dhcpOptions != null) {
        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
				ByteBuffer _buf = dhcpOption.encode();
				if (_buf != null) {
					buf.put(_buf);
				}
			}
        }
        return (ByteBuffer) buf.flip();

    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
        if ((buf != null) && buf.hasRemaining()) {
            // already have the code, so length is next
            int len = Util.getUnsignedShort(buf);
            if (log.isDebugEnabled())
                log.debug("IA_PD option reports length=" + len +
                          ":  bytes remaining in buffer=" + buf.remaining());
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	iaId = Util.getUnsignedInt(buf);
            	if (buf.position() < eof) {
            		t1 = Util.getUnsignedInt(buf);
            		if (buf.position() < eof) {
            			t2 = Util.getUnsignedInt(buf);
            			if (buf.position() < eof) {
            				decodeOptions(buf, eof);
            			}
            		}
            	}
            }
        }
    }
    
    /**
     * Decode any options sent by the client inside this IA_PD.  Mostly, we are
     * concerned with any IA_PREFIX options that the client may have included as
     * a hint to which address(es) it may want.  RFC 3315 does not specify if
     * a client can actually provide any options other than IA_PREFIX options in
     * inside the IA_PD, but it does not say that the client cannot do so, and
     * the IA_PD option definition supports any type of sub-options.
     * 
     * @param buf ByteBuffer positioned at the start of the options in the packet
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */    
    protected void decodeOptions(ByteBuffer buf, int eof) 
	    throws IOException
	{
		while (buf.position() < eof) {
		    int code = Util.getUnsignedShort(buf);
		    log.debug("Option code=" + code);
		    DhcpOption option = DhcpV6OptionFactory.getDhcpOption(code);
		    if (option != null) {
		        option.decode(buf);
		        if (option instanceof DhcpV6IaPrefixOption) {
		        	iaPrefixOptions.add((DhcpV6IaPrefixOption)option);
		        }
		        else {
		        	dhcpOptions.put(option.getCode(), option);
		        }
		    }
		    else {
		        break;  // no more options, or one is malformed, so we're done
		    }
		}
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": iaId=");
        sb.append(iaId);
        sb.append(" t1=");
        sb.append(t1);
        sb.append(" t2=");
        sb.append(t2);
        if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_DHCPOPTIONS");
        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
				sb.append(dhcpOption.toString());
			}
        }
        if ((iaPrefixOptions != null) && !iaPrefixOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_PREFIXES");
            sb.append(Util.LINE_SEPARATOR);
        	for (DhcpV6IaPrefixOption iaPrefixOption : iaPrefixOptions) {
				sb.append(iaPrefixOption.toString());
			}
        }
        return sb.toString();
    }
}
