/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6GeoconfCivicOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.option.base.BaseDhcpOption;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.CivicAddressElement;
import com.jagornet.dhcp.xml.V6GeoconfCivicOption;

/**
 * <p>Title: DhcpV6GeoconfCivicOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6GeoconfCivicOption extends BaseDhcpOption
{
	private short what;
	private String countryCode;
	private List<CivicAddress> civicAddressList;
	
	class CivicAddress {
		short type;
		String value;
		public CivicAddress() {
			this(null);
		}
		public CivicAddress(CivicAddressElement caElement) {
			if (caElement != null) {
				type = caElement.getCaType();
				value = caElement.getCaValue();
			}
		}
	}
	
    /**
     * Instantiates a new dhcp geoconf civic option.
     */
    public DhcpV6GeoconfCivicOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp geoconf civic option.
     * 
     * @param geoconfCivicOption the geoconf civic option
     */
    public DhcpV6GeoconfCivicOption(V6GeoconfCivicOption geoconfCivicOption)
    {
        super();
        if (geoconfCivicOption != null) {
        	what = geoconfCivicOption.getWhat();
        	countryCode = geoconfCivicOption.getCountryCode();
        	List<CivicAddressElement> cas = geoconfCivicOption.getCivicAddressElementList();
        	if ((cas != null) && !cas.isEmpty()) {
        		for (CivicAddressElement civicAddressElement : cas) {
					addCivicAddress(new CivicAddress(civicAddressElement));
				}
        	}
        }
    }

    public short getWhat() {
		return what;
	}

	public void setWhat(short what) {
		this.what = what;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public List<CivicAddress> getCivicAddressList() {
		return civicAddressList;
	}

	public void setCivicAddressList(List<CivicAddress> civicAddressList) {
		this.civicAddressList = civicAddressList;
	}
	
	public void addCivicAddress(CivicAddress civicAddress) {
		if (civicAddressList == null) {
			civicAddressList = new ArrayList<CivicAddress>();
		}
		civicAddressList.add(civicAddress);
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
    	int len = 3;	// size of what(1) + country code(2)
    	if ((civicAddressList != null) && !civicAddressList.isEmpty()) {
    		for (CivicAddress civicAddr : civicAddressList) {
				len += 2;	// CAtype byte + CAlength byte
				String caVal = civicAddr.value;
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
        buf.put((byte)what);
        if (countryCode != null) {
        	buf.put(countryCode.getBytes());
        }
        else {
        	//TODO: throw exception?
        	buf.put("XX".getBytes());
        }
    	if ((civicAddressList != null) && !civicAddressList.isEmpty()) {
    		for (CivicAddress civicAddr : civicAddressList) {
    			buf.put((byte)civicAddr.type);
    			String caVal = civicAddr.value;
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
            	what = Util.getUnsignedByte(buf);
                if (buf.position() < eof) {
                	byte[] country = new byte[2];
                	buf.get(country);
                	countryCode = new String(country);
                	while (buf.position() < eof) {
                		CivicAddress civicAddr = new CivicAddress();
                		civicAddr.type = Util.getUnsignedByte(buf);
                		short caLen = Util.getUnsignedByte(buf);
                		if (caLen > 0) {
                			byte[] caVal = new byte[caLen];
                			buf.get(caVal);
                			civicAddr.value = new String(caVal);
                		}
                		addCivicAddress(civicAddr);
                	}
                }
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
        sb.append(": what=");
        sb.append(what);
        sb.append(" countryCode=");
        sb.append(countryCode);
    	if ((civicAddressList != null) && !civicAddressList.isEmpty()) {
    		sb.append(Util.LINE_SEPARATOR);
    		for (CivicAddress civicAddr : civicAddressList) {
    			sb.append("civicAddress: type=");
    			sb.append(civicAddr.type);
    			sb.append(" value=");
    			sb.append(civicAddr.value);
    		}
    	}
        return sb.toString();
    }
    
}
