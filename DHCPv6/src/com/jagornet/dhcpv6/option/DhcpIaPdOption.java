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
import com.jagornet.dhcpv6.xml.ConfigOptionsType;
import com.jagornet.dhcpv6.xml.IaPdOption;
import com.jagornet.dhcpv6.xml.IaPrefixOption;

// TODO: Auto-generated Javadoc
/**
 * The Class DhcpIaPdOption.
 */
public class DhcpIaPdOption extends BaseDhcpOption
{	
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpIaPdOption.class);
	
	/** The ia na option, which contains any configured options for the ia na. */
	private IaPdOption iaPdOption;
    
	/** The dhcp options sent by the client inside this ia na option, _NOT_ including any requested ia addr options. */
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
    	int len = 4 + 4 + 4;	// iaId + t1 + t2
        // encode the configured options, so get the length of configured options
        ConfigOptionsType configOptions = iaPdOption.getConfigOptions();
        if (configOptions != null) {
        	DhcpConfigOptions dhcpConfigOptions = new DhcpConfigOptions(configOptions);
        	if (dhcpConfigOptions != null) {
        		len += dhcpConfigOptions.getLength();
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
        // encode the ia addrs configured for this ia na
        List<IaPrefixOption> iaPrefixOptions = iaPdOption.getIaPrefixOptionList().getIaPrefixOptionList();
        if (iaPrefixOptions != null) {
        	for (IaPrefixOption iaPrefixOption : iaPrefixOptions) {
				DhcpIaPrefixOption dhcpIaPrefixOption = new DhcpIaPrefixOption(iaPrefixOption);
				if (dhcpIaPrefixOption != null) {
					ByteBuffer _buf = dhcpIaPrefixOption.encode();
					if (_buf != null) {
						buf.put(_buf);
					}
				}
			}
        }
        // encode the options configured for this ia na
        ConfigOptionsType configOptions = iaPdOption.getConfigOptions();
        if (configOptions != null) {
        	DhcpConfigOptions dhcpConfigOptions = new DhcpConfigOptions(configOptions);
        	if (dhcpConfigOptions != null) {
        		ByteBuffer _buf = dhcpConfigOptions.encode();
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
                log.debug("IA_NA option reports length=" + len +
                          ":  bytes remaining in buffer=" + buf.remaining());
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	iaPdOption.setIaId(Util.getUnsignedInt(buf));
            	if (buf.position() < eof) {
            		iaPdOption.setT1(Util.getUnsignedInt(buf));
            		if (buf.position() < eof) {
            			iaPdOption.setT2(Util.getUnsignedInt(buf));
            			if (buf.position() < eof) {
            				decodeOptions(buf);
            			}
            		}
            	}
            }
        }
    }
    
    /**
     * Decode any options sent by the client inside this IA_NA.  Mostly, we are
     * concerned with any IA_ADDR options that the client may have included as
     * a hint to which address(es) it may want.  RFC 3315 does not specify if
     * a client can actually provide any options other than IA_ADDR options in
     * inside the IA_NA, but it does not say that the client cannot do so, and
     * the IA_NA option definition supports any type of sub-options.
     * 
     * @param buf ByteBuffer positioned at the start of the options in the packet
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */    
    protected void decodeOptions(ByteBuffer buf) 
	    throws IOException
	{
		while (buf.hasRemaining()) {
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
}
