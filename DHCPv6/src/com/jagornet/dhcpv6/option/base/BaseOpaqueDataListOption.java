/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataListOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OpaqueDataListOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * <p>Title: BaseOpaqueDataListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOpaqueDataListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseOpaqueDataListOption.class);
	
    /** The opaque opaqueData list option. */
    protected OpaqueDataListOptionType opaqueDataListOption;

    /**
     * Instantiates a new opaque opaqueData list option.
     */
    public BaseOpaqueDataListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new opaque opaqueData list option.
     * 
     * @param opaqueDataListOption the opaque opaqueData list option
     */
    public BaseOpaqueDataListOption(OpaqueDataListOptionType opaqueDataListOption)
    {
        super();
        if (opaqueDataListOption != null)
            this.opaqueDataListOption = opaqueDataListOption;
        else
            this.opaqueDataListOption = OpaqueDataListOptionType.Factory.newInstance();
    }

    /**
     * Gets the opaque opaqueData list option.
     * 
     * @return the opaque opaqueData list option
     */
    public OpaqueDataListOptionType getOpaqueDataListOptionType()
    {
        return opaqueDataListOption;
    }

    /**
     * Sets the opaque opaqueData list option.
     * 
     * @param opaqueDataListOption the new opaque opaqueData list option
     */
    public void setOpaqueDataListOptionType(OpaqueDataListOptionType opaqueDataListOption)
    {
        if (opaqueDataListOption != null)
            this.opaqueDataListOption = opaqueDataListOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        List<OpaqueData> opaqueList = opaqueDataListOption.getOpaqueDataList();
        if ((opaqueList != null) && !opaqueList.isEmpty()) {
            for (OpaqueData opaque : opaqueList) {
                len += OpaqueDataUtil.getLength(opaque);
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
        List<OpaqueData> opaqueList = opaqueDataListOption.getOpaqueDataList();
        if ((opaqueList != null) && !opaqueList.isEmpty()) {
            for (OpaqueData opaque : opaqueList) {
                OpaqueDataUtil.encode(buf, opaque);
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
            while (buf.position() < eof) {
				OpaqueData opaque = opaqueDataListOption.addNewOpaqueData();
                OpaqueDataUtil.decode(opaque, buf);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcpv6.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        if (opaqueDataListOption == null)
        	return false;

		List<OpaqueData> myOpaqueList = opaqueDataListOption.getOpaqueDataList();
		if (myOpaqueList == null)
			return false;

        // first see if we have a opaque opaqueData list option to compare to
        OpaqueDataListOptionType that = expression.getOpaqueDataListOption();
        if (that != null) {
        	List<OpaqueData> opaqueList = that.getOpaqueDataList();
        	if (opaqueList != null) {
        		Operator.Enum op = expression.getOperator();
        		if (op.equals(Operator.EQUALS)) {
	        		if (opaqueList.size() != myOpaqueList.size()) {
	        			return false;
	        		}
	        		for (int i=0; i<opaqueList.size(); i++) {
	        			OpaqueData opaque = opaqueList.get(i);
	        			OpaqueData myOpaque = myOpaqueList.get(i);
	        			if (!OpaqueDataUtil.equals(opaque, myOpaque)) {
	        				return false;
	        			}
	        		}
	        		return true;
        		}
        		else if (op.equals(Operator.CONTAINS)) {
        			if (opaqueList.size() > myOpaqueList.size()) {
        				return false;
        			}
        			for (int i=0; i<opaqueList.size(); i++) {
	        			OpaqueData opaque = opaqueList.get(i);
	        			boolean found = false;
        				for (int j=0; j<myOpaqueList.size(); i++) {
    	        			OpaqueData myOpaque = myOpaqueList.get(j);
    	        			if (OpaqueDataUtil.equals(opaque, myOpaque)) {
    	        				found = true;
    	        				break;
    	        			}        					
        				}
        				if (!found) {
        					return false;        					
        				}
        			}
        			return true;
        		}
        		else {
                	log.warn("Unsupported expression operator: " + op);
        		}
        	}
        }

        return false;
    }

    /**
     * Adds the opaque opaqueData.
     * 
     * @param opaqueString the opaque opaqueData string
     */
    public void addOpaqueData(String opaqueString)
    {
        if (opaqueString != null) {
            OpaqueData opaque = opaqueDataListOption.addNewOpaqueData();
            opaque.setAsciiValue(opaqueString);
        }
    }

    /**
     * Adds the opaque opaqueData.
     * 
     * @param opaqueData the opaque opaqueData
     */
    public void addOpaqueData(byte[] opaqueData)
    {
        if (opaqueData != null) {
            OpaqueData opaque = opaqueDataListOption.addNewOpaqueData();
            opaque.setHexValue(opaqueData);
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
        sb.append(opaqueDataListOption.toString());
        return sb.toString();
    }

}
