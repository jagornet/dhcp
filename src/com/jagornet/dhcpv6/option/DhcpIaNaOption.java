/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpIaNaOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.option.base.DhcpOptionable;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.IaNaOption;

/**
 * The Class DhcpIaNaOption.
 */
public class DhcpIaNaOption extends BaseDhcpOption implements DhcpOptionable
{	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpIaNaOption.class);
	
	/** The ia na option */
	private IaNaOption iaNaOption;
    
	/** The dhcp options inside this ia na option,
	 *  _NOT_ including any ia addr options. */
	protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();
	
	/** The ia addr options  */
	private List<DhcpIaAddrOption> iaAddrOptions = new ArrayList<DhcpIaAddrOption>();

	public DhcpIaNaOption()
	{
		this(null);
	}
	
	public DhcpIaNaOption(IaNaOption iaNaOption)
	{
		if (iaNaOption != null)
			this.iaNaOption = iaNaOption;
		else
			this.iaNaOption = IaNaOption.Factory.newInstance();
	}
	
    public IaNaOption getIaNaOption() {
		return iaNaOption;
	}

	public void setIaNaOption(IaNaOption iaNaOption) {
		this.iaNaOption = iaNaOption;
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
	
	/**
	 * Implement DhcpOptionable.
	 */
	public void putDhcpOption(DhcpOption dhcpOption)
	{
		dhcpOptions.put(dhcpOption.getCode(), dhcpOption);
	}

	public List<DhcpIaAddrOption> getIaAddrOptions() {
		return iaAddrOptions;
	}

	public void setIaAddrOptions(List<DhcpIaAddrOption> iaAddrOptions) {
		this.iaAddrOptions = iaAddrOptions;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return iaNaOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
/*
    	int len = 4 + 4 + 4;	// iaId + t1 + t2
        // encode the configured options, so get the length of configured options
        ConfigOptionsType configOptions = iaNaOption.getIaConfigOptions();
        if (configOptions != null) {
        	DhcpConfigOptions dhcpConfigOptions = new DhcpConfigOptions(configOptions);
        	if (dhcpConfigOptions != null) {
        		len += dhcpConfigOptions.getLength();
        	}
        }
        // the configured IaAddr options are in a separate list, so count them up
        List<IaAddrOption> iaAddrs = iaNaOption.getIaAddrOptionList().getIaAddrOptionList();
        if (iaAddrs != null) {
        	for (IaAddrOption iaAddr : iaAddrs) {
            	DhcpIaAddrOption dhcpIaAddr = new DhcpIaAddrOption(iaAddr);
            	len += 2 + 2 + dhcpIaAddr.getLength();
			}
        }
        return len;
*/
    	return getDecodedLength();
    }

    public int getDecodedLength()
    {
    	int len = 4 + 4 + 4;	// iaId + t1 + t2
    	if (iaAddrOptions != null) {
    		for (DhcpIaAddrOption iaAddrOption : iaAddrOptions) {
    			// code(short) + len(short) + data_len
				len += 4 + iaAddrOption.getDecodedLength();
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
        buf.putInt((int)iaNaOption.getIaId());
        buf.putInt((int)iaNaOption.getT1());
        buf.putInt((int)iaNaOption.getT2());
/*
        // encode the ia addrs configured for this ia na
        List<IaAddrOption> iaAddrOptions = iaNaOption.getIaAddrOptionList().getIaAddrOptionList();
        if (iaAddrOptions != null) {
        	for (IaAddrOption iaAddrOption : iaAddrOptions) {
				DhcpIaAddrOption dhcpIaAddrOption = new DhcpIaAddrOption(iaAddrOption);
				if (dhcpIaAddrOption != null) {
					ByteBuffer _buf = dhcpIaAddrOption.encode();
					if (_buf != null) {
						buf.put(_buf);
					}
				}
			}
        }
        // encode the options configured for this ia na
        ConfigOptionsType configOptions = iaNaOption.getConfigOptions();
        if (configOptions != null) {
        	DhcpConfigOptions dhcpConfigOptions = new DhcpConfigOptions(configOptions);
        	if (dhcpConfigOptions != null) {
        		ByteBuffer _buf = dhcpConfigOptions.encode();
        		if (_buf != null) {
        			buf.put(_buf);
        		}
        	}
        }
*/
        if (iaAddrOptions != null) {
        	for (DhcpIaAddrOption iaAddrOption : iaAddrOptions) {
				ByteBuffer _buf = iaAddrOption.encode();
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
                log.debug("IA_NA option reports length=" + len +
                          ":  bytes remaining in buffer=" + buf.remaining());
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	iaNaOption.setIaId(Util.getUnsignedInt(buf));
            	if (buf.position() < eof) {
            		iaNaOption.setT1(Util.getUnsignedInt(buf));
            		if (buf.position() < eof) {
            			iaNaOption.setT2(Util.getUnsignedInt(buf));
            			if (buf.position() < eof) {
            				decodeOptions(buf, eof);
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
     * @param buf	ByteBuffer positioned at the start of the options in the packet
     * @throws IOException
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
		        if (option instanceof DhcpIaAddrOption) {
		        	iaAddrOptions.add((DhcpIaAddrOption)option);
		        }
		        else {
		        	putDhcpOption(option);
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
        sb.append(iaNaOption.toString());
        if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_DHCPOPTIONS");
        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
				sb.append(dhcpOption.toString());
			}
        }
        if ((iaAddrOptions != null) && !iaAddrOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_ADDRS");
            sb.append(Util.LINE_SEPARATOR);
        	for (DhcpIaAddrOption iaAddrOption : iaAddrOptions) {
				sb.append(iaAddrOption.toString());
			}
        }
        return sb.toString();
    }
}
