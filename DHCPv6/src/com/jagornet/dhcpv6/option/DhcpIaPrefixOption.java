/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpIaPrefixOption.java is part of DHCPv6.
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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.base.BaseDhcpOption;
import com.jagornet.dhcpv6.option.base.BaseIpAddressOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.ConfigOptionsType;
import com.jagornet.dhcpv6.xml.IaPrefixOption;

/**
 * The Class DhcpIaPrefixOption.
 */
public class DhcpIaPrefixOption extends BaseDhcpOption
{	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpIaPrefixOption.class);
	
	/** The ia addr prefix, which contains any configured options for the ia prefix */
	private IaPrefixOption iaPrefixOption;
    
	/** The dhcp options inside this ia prefix option */
	protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();

	public DhcpIaPrefixOption()
	{
		this(null);
	}
	
	public DhcpIaPrefixOption(IaPrefixOption iaPrefixOption)
	{
		if (iaPrefixOption != null)
			this.iaPrefixOption = iaPrefixOption;
		else
			this.iaPrefixOption = IaPrefixOption.Factory.newInstance();
	}

	public IaPrefixOption getIaPrefixOption()
	{
		return iaPrefixOption;
	}
	
	public void setIaPrefixOption(IaPrefixOption iaPrefixOption)
	{
		if (iaPrefixOption != null)
			this.iaPrefixOption = iaPrefixOption;
	}

	public Map<Integer, DhcpOption> getDhcpOptionMap() {
		return dhcpOptions;
	}

	public void setDhcpOptionMap(Map<Integer, DhcpOption> dhcpOptions) {
		this.dhcpOptions = dhcpOptions;
	}

	public void putAllDhcpOptions(Map<Integer, DhcpOption> dhcpOptions) {
		this.dhcpOptions.putAll(dhcpOptions);
	}
	
	public InetAddress getInetAddress()
	{
		InetAddress inetAddr = null;
		if (iaPrefixOption != null) {
			try {
				inetAddr = InetAddress.getByName(iaPrefixOption.getIpv6Prefix());
			}
			catch (UnknownHostException ex) {
				log.error("Invalid IP address: " + iaPrefixOption.getIpv6Prefix());
			}
		}
		return inetAddr;
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return iaPrefixOption.getCode();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 4 + 4 + 1 + 16;	// iaid + preferred + valid + prefix_len + prefix_addr
        // encode the configured options, so get the length of configured options
        ConfigOptionsType configOptions = iaPrefixOption.getConfigOptions();
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
        buf.putInt((int)iaPrefixOption.getPreferredLifetime());
        buf.putInt((int)iaPrefixOption.getValidLifetime());
        buf.put((byte)iaPrefixOption.getPrefixLength());
        String ipPrefix = iaPrefixOption.getIpv6Prefix();
        if (ipPrefix != null) {
            InetAddress inet6Prefix = Inet6Address.getByName(ipPrefix);
            buf.put(inet6Prefix.getAddress());
            // encode the configured options
	        ConfigOptionsType configOptions = iaPrefixOption.getConfigOptions();
	        if (configOptions != null) {
	        	DhcpConfigOptions dhcpConfigOptions = new DhcpConfigOptions(configOptions);
	        	if (dhcpConfigOptions != null) {
	        		ByteBuffer _buf = dhcpConfigOptions.encode();
	        		if (_buf != null) {
	        			buf.put(_buf);
	        		}
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
                log.debug("IA_PREFIX option reports length=" + len +
                          ":  bytes remaining in buffer=" + buf.remaining());
            int eof = buf.position() + len;
            if (buf.position() < eof) {
        		iaPrefixOption.setPreferredLifetime(Util.getUnsignedInt(buf));
            	if (buf.position() < eof) {
            		iaPrefixOption.setValidLifetime(Util.getUnsignedInt(buf));
	            	if (buf.position() < eof) {
	                	iaPrefixOption.setPrefixLength(Util.getUnsignedByte(buf));
	                	if (buf.position() < eof) {
	    	            	iaPrefixOption.setIpv6Prefix(BaseIpAddressOption.decodeIpAddress(buf));
	                		if (buf.position() < eof) {
	                			decodeOptions(buf);
	                		}
	                	}
	            	}
            	}
            }
        }
    }
    
    /**
     * Decode any options sent by the client inside this IA_PREFIX.  Note that this
     * should actually be a rare occurrence, in that such a client would have to
     * have requested specific IA_PREFIXs inside an IA_NA/IA_TA, _and_ must have
     * requested some specific options for any such IA_PREFIXs.  RFC 3315 does not
     * specify if a client can actually provide any options with requested IA_PREFIXs,
     * but it does not say that the client cannot do so, and the IA_PREFIX option
     * definition itself supports sub-options, thus we check for any when decoding.
     * 
     * @param buf	ByteBuffer positioned at the start of the options in the packet
     * @return	a Map of DhcpOptions keyed by the option code
     * @throws IOException
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
                dhcpOptions.put(option.getCode(), option);
            }
            else {
                break;  // no more options, or one is malformed, so we're done
            }
        }
    }

}
