/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpIaPdOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.IaPdOption;

/**
 * The Class DhcpIaPdOption.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpIaPdOption extends BaseDhcpOption
{	
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpIaPdOption.class);
	
	/** The ia pd option, which contains any configured options for the ia pd. */
	private IaPdOption iaPdOption;
    
	/** The dhcp options sent by the client inside this ia pd option, _NOT_ including any requested ia prefix options. */
	protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();
	
	/** The ia prefix options. */
	private List<DhcpIaPrefixOption> iaPrefixOptions = new ArrayList<DhcpIaPrefixOption>();

	/**
	 * Instantiates a new dhcp ia pd option.
	 */
	public DhcpIaPdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp ia pd option.
	 * 
	 * @param iaPdOption the ia pd option
	 */
	public DhcpIaPdOption(IaPdOption iaPdOption)
	{
		if (iaPdOption != null)
			this.iaPdOption = iaPdOption;
		else
			this.iaPdOption = IaPdOption.Factory.newInstance();
	}
	
    /**
     * Gets the ia pd option.
     * 
     * @return the ia pd option
     */
    public IaPdOption getIaPdOption() {
		return iaPdOption;
	}

	/**
	 * Sets the ia pd option.
	 * 
	 * @param iaPdOption the new ia pd option
	 */
	public void setIaPdOption(IaPdOption iaPdOption) {
		this.iaPdOption = iaPdOption;
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
	public List<DhcpIaPrefixOption> getIaPrefixOptions() {
		return iaPrefixOptions;
	}

	/**
	 * Sets the ia prefix options.
	 * 
	 * @param iaPrefixOptions the new ia prefix options
	 */
	public void setIaPrefixOptions(List<DhcpIaPrefixOption> iaPrefixOptions) {
		this.iaPrefixOptions = iaPrefixOptions;
	}

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return iaPdOption.getCode();
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
    		for (DhcpIaPrefixOption iaPrefixOption : iaPrefixOptions) {
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
        buf.putInt((int)iaPdOption.getIaId());
        buf.putInt((int)iaPdOption.getT1());
        buf.putInt((int)iaPdOption.getT2());

        if (iaPrefixOptions != null) {
        	for (DhcpIaPrefixOption iaPrefixOption : iaPrefixOptions) {
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
            	iaPdOption.setIaId(Util.getUnsignedInt(buf));
            	if (buf.position() < eof) {
            		iaPdOption.setT1(Util.getUnsignedInt(buf));
            		if (buf.position() < eof) {
            			iaPdOption.setT2(Util.getUnsignedInt(buf));
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
		    DhcpOption option = DhcpOptionFactory.getDhcpOption(code);
		    if (option != null) {
		        option.decode(buf);
		        if (option instanceof DhcpIaPrefixOption) {
		        	iaPrefixOptions.add((DhcpIaPrefixOption)option);
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
        sb.append(Util.LINE_SEPARATOR);
        // use XmlObject implementation
        sb.append(iaPdOption.toString());
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
        	for (DhcpIaPrefixOption iaPrefixOption : iaPrefixOptions) {
				sb.append(iaPrefixOption.toString());
			}
        }
        return sb.toString();
    }
}
