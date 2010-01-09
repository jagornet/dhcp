/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpGeoconfCivicOption.java is part of DHCPv6.
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
import java.util.List;

import com.jagornet.dhcpv6.option.base.BaseDhcpOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.CivicAddressElement;
import com.jagornet.dhcpv6.xml.GeoconfCivicOption;

/**
 * <p>Title: DhcpGeoconfCivicOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpGeoconfCivicOption extends BaseDhcpOption
{
    /** The geoconf civic option. */
    private GeoconfCivicOption geoconfCivicOption;
    
    /**
     * Instantiates a new dhcp geoconf civic option.
     */
    public DhcpGeoconfCivicOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp geoconf civic option.
     * 
     * @param geoconfCivicOption the geoconf civic option
     */
    public DhcpGeoconfCivicOption(GeoconfCivicOption geoconfCivicOption)
    {
        super();
        if (geoconfCivicOption != null)
            this.geoconfCivicOption = geoconfCivicOption;
        else
            this.geoconfCivicOption = GeoconfCivicOption.Factory.newInstance();
    }

    /**
     * Gets the geoconf civic option.
     * 
     * @return the geoconf civic option
     */
    public GeoconfCivicOption getGeoconfCivicOption()
    {
        return geoconfCivicOption;
    }

    /**
     * Sets the geoconf civic option.
     * 
     * @param geoconfCivicOption the new geoconf civic option
     */
    public void setGeoconfCivicOption(GeoconfCivicOption geoconfCivicOption)
    {
        if (geoconfCivicOption != null)
            this.geoconfCivicOption = geoconfCivicOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
    	int len = 3;	// size of what(1) + country code(2)
    	List<CivicAddressElement> civicAddrs = geoconfCivicOption.getCivicAddressElementList();
    	if ((civicAddrs != null) && !civicAddrs.isEmpty()) {
    		for (CivicAddressElement civicAddr : civicAddrs) {
				len += 2;	// CAtype byte + CAlength byte
				String caVal = civicAddr.getCaValue();
				if (caVal != null)
					len += caVal.length();
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
        buf.put((byte)geoconfCivicOption.getWhat());
        String country = geoconfCivicOption.getCountryCode();
        if (country != null) {
        	buf.put(country.getBytes());
        }
        else {
        	//TODO: throw exception?
        	buf.put("XX".getBytes());
        }
        List<CivicAddressElement> civicAddrs = geoconfCivicOption.getCivicAddressElementList();
    	if ((civicAddrs != null) && !civicAddrs.isEmpty()) {
    		for (CivicAddressElement civicAddr : civicAddrs) {
    			buf.put((byte)civicAddr.getCaType());
    			String caVal = civicAddr.getCaValue();
    			if (caVal != null) {
    				buf.put((byte)caVal.length());
    				buf.put(caVal.getBytes());
    			}
    			else {
    				buf.put((byte)0);
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
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	geoconfCivicOption.setWhat(Util.getUnsignedByte(buf));
                if (buf.position() < eof) {
                	byte[] country = new byte[2];
                	buf.get(country);
                	geoconfCivicOption.setCountryCode(new String(country));
                	while (buf.position() < eof) {
                		CivicAddressElement civicAddr = geoconfCivicOption.addNewCivicAddressElement();
                		civicAddr.setCaType(Util.getUnsignedByte(buf));
                		short caLen = Util.getUnsignedByte(buf);
                		if (caLen > 0) {
                			byte[] caVal = new byte[caLen];
                			buf.get(caVal);
                			civicAddr.setCaValue(new String(caVal));
                		}
                	}
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return geoconfCivicOption.getCode();
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
        sb.append(geoconfCivicOption.toString());
        return sb.toString();
    }
    
}
