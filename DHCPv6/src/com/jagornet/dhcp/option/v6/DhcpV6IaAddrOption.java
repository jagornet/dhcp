/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6IaAddrOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option.v6;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.option.base.BaseDhcpOption;
import com.jagornet.dhcp.option.base.BaseIpAddressOption;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.V6IaAddrOption;

/**
 * The Class DhcpV6IaAddrOption.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6IaAddrOption extends BaseDhcpOption
{		
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV6IaAddrOption.class);
	
	private String ipAddress;
	private long preferredLifetime;		// long for unsigned int
	private long validLifetime;			// long for unsigned int
    
	/** The dhcp options inside this ia addr option. */
	protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();

	/**
	 * Instantiates a new dhcp ia addr option.
	 */
	public DhcpV6IaAddrOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp ia addr option.
	 * 
	 * @param iaAddrOption the ia addr option
	 */
	public DhcpV6IaAddrOption(V6IaAddrOption iaAddrOption)
	{
		if (iaAddrOption != null) {
			this.ipAddress = iaAddrOption.getIpv6Address();
			this.preferredLifetime = iaAddrOption.getPreferredLifetime();
			this.validLifetime = iaAddrOption.getValidLifetime();
		}
		setCode(DhcpConstants.OPTION_IAADDR);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public long getPreferredLifetime() {
		return preferredLifetime;
	}

	public void setPreferredLifetime(long preferredLifetime) {
		this.preferredLifetime = preferredLifetime;
	}

	public long getValidLifetime() {
		return validLifetime;
	}

	public void setValidLifetime(long validLifetime) {
		this.validLifetime = validLifetime;
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
	 * Gets the inet address.
	 * 
	 * @return the inet address
	 */
	public InetAddress getInetAddress()
	{
		InetAddress inetAddr = null;
		if (ipAddress != null) {
			try {
				inetAddr = InetAddress.getByName(ipAddress);
			}
			catch (UnknownHostException ex) {
				log.error("Invalid IP address: " + ipAddress + ": " + ex);
			}
		}
		return inetAddr;
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
        int len = 24;	// ipAddr(16) + preferred(4) + valid(4)
    	if (dhcpOptions != null) {
    		for(DhcpOption dhcpOption : dhcpOptions.values()) {
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
        if (ipAddress != null) {
            InetAddress inet6Addr = Inet6Address.getByName(ipAddress);
            buf.put(inet6Addr.getAddress());
            buf.putInt((int)preferredLifetime);
            buf.putInt((int)validLifetime);
            // encode the configured options
            if (dhcpOptions != null) {
            	for (DhcpOption dhcpOption : dhcpOptions.values()) {
    				ByteBuffer _buf = dhcpOption.encode();
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
                log.debug("IA_ADDR option reports length=" + len +
                          ":  bytes remaining in buffer=" + buf.remaining());
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	ipAddress = BaseIpAddressOption.decodeIpAddress(buf);
            	if (buf.position() < eof) {
            		preferredLifetime = Util.getUnsignedInt(buf);
                	if (buf.position() < eof) {
                		validLifetime = Util.getUnsignedInt(buf);
                		if (buf.position() < eof) {
                			decodeOptions(buf, eof);
                		}
                	}
            	}
            }
        }
    }
    
    /**
     * Decode any options sent by the client inside this IA_ADDR.  Note that this
     * should actually be a rare occurrence, in that such a client would have to
     * have requested specific IA_ADDRs inside an IA_NA/IA_TA, _and_ must have
     * requested some specific options for any such IA_ADDRs.  RFC 3315 does not
     * specify if a client can actually provide any options with requested IA_ADDRs,
     * but it does not say that the client cannot do so, and the IA_ADDR option
     * definition itself supports sub-options, thus we check for any when decoding.
     * Options within an IA_ADDR may come from a client when renewing an IA_ADDR
     * which contained options originally provided by the server, and the client is
     * requesting that those same options be renewed along with the address(es).
     * 
     * @param buf ByteBuffer positioned at the start of the options in the packet
     * @param eof the eof
     * 
     * @return a Map of DhcpOptions keyed by the option code
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
                putDhcpOption(option);
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
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ipAddress=");
        sb.append(ipAddress);
        sb.append(" preferredLifetime=");
        sb.append(getPreferredLifetime());
        sb.append(" validLifetime=");
        sb.append(getValidLifetime());
        if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_ADDR_DHCPOPTIONS");
        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
				sb.append(dhcpOption.toString());
			}
        }
        return sb.toString();
    }

}
