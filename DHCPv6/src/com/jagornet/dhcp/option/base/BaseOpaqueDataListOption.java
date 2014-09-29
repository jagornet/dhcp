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
package com.jagornet.dhcp.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.option.DhcpComparableOption;
import com.jagornet.dhcp.option.OpaqueDataUtil;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataListOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseOpaqueDataListOption
 * Description: The abstract base class for opaque opaqueDataList DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOpaqueDataListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseOpaqueDataListOption.class);
	
    private List<BaseOpaqueData> opaqueDataList;

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
        if (opaqueDataListOption != null) {
        	List<OpaqueData> _opaqueDataList = opaqueDataListOption.getOpaqueDataList();
        	if (_opaqueDataList != null) {
        		for (OpaqueData opaqueData : _opaqueDataList) {
					addOpaqueData(new BaseOpaqueData(opaqueData));
				}
        	}
        }
    }

    public List<BaseOpaqueData> getOpaqueDataList() {
		return opaqueDataList;
	}

	public void setOpaqueDataList(List<BaseOpaqueData> opaqueDataList) {
		this.opaqueDataList = opaqueDataList;
	}
	
	public void addOpaqueData(BaseOpaqueData baseOpaque) {
		if (baseOpaque != null) {
			if (opaqueDataList == null) {
				opaqueDataList = new ArrayList<BaseOpaqueData>();
			}
			opaqueDataList.add(baseOpaque);
		}
	}

    public void addOpaqueData(String opaqueString) {
        if (opaqueString != null) {
        	BaseOpaqueData opaque = new BaseOpaqueData(opaqueString);
        	addOpaqueData(opaque);
        }
    }

    public void addOpaqueData(byte[] opaqueData)
    {
        if (opaqueData != null) {
        	BaseOpaqueData opaque = new BaseOpaqueData(opaqueData);
        	addOpaqueData(opaque);
        }
    }

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        if ((opaqueDataList != null) && !opaqueDataList.isEmpty()) {
            for (BaseOpaqueData opaque : opaqueDataList) {
                len += 2 + opaque.getLength();
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
        if ((opaqueDataList != null) && !opaqueDataList.isEmpty()) {
            for (BaseOpaqueData opaque : opaqueDataList) {
            	opaque.encodeLengthAndData(buf);
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
            	BaseOpaqueData opaque = new BaseOpaqueData();
            	opaque.decodeLengthAndData(buf);
                addOpaqueData(opaque);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcp.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        return matches(expression.getOpaqueDataListOption(), expression.getOperator());
    }

	public boolean matches(OpaqueDataListOptionType that, Operator.Enum op) 
	{
		if (that != null) {
        	List<OpaqueData> opaqueList = that.getOpaqueDataList();
        	if (opaqueList != null) {
        		if (op.equals(Operator.EQUALS)) {
	        		if (opaqueList.size() != opaqueDataList.size()) {
	        			return false;
	        		}
	        		for (int i=0; i<opaqueList.size(); i++) {
	        			BaseOpaqueData opaque = new BaseOpaqueData(opaqueList.get(i));
	        			BaseOpaqueData myOpaque = opaqueDataList.get(i);
	        			if (!OpaqueDataUtil.equals(opaque, myOpaque)) {
	        				return false;
	        			}
	        		}
	        		return true;
        		}
        		else if (op.equals(Operator.CONTAINS)) {
        			if (opaqueList.size() > opaqueDataList.size()) {
        				return false;
        			}
        			for (int i=0; i<opaqueList.size(); i++) {
	        			BaseOpaqueData opaque = new BaseOpaqueData(opaqueList.get(i));
	        			boolean found = false;
        				for (int j=0; j<opaqueDataList.size(); i++) {
    	        			BaseOpaqueData myOpaque = opaqueDataList.get(j);
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

	public boolean matches(BaseOpaqueDataListOption that, Operator.Enum op) 
	{
		if (opaqueDataList == null)
        	return false;
	
		if (that != null) {
        	List<BaseOpaqueData> opaqueList = that.getOpaqueDataList();
        	if (opaqueList != null) {
        		if (op.equals(Operator.EQUALS)) {
	        		if (opaqueList.size() != opaqueDataList.size()) {
	        			return false;
	        		}
	        		for (int i=0; i<opaqueList.size(); i++) {
	        			BaseOpaqueData opaque = opaqueList.get(i);
	        			BaseOpaqueData myOpaque = opaqueDataList.get(i);
	        			if (!OpaqueDataUtil.equals(opaque, myOpaque)) {
	        				return false;
	        			}
	        		}
	        		return true;
        		}
        		else if (op.equals(Operator.CONTAINS)) {
        			if (opaqueList.size() > opaqueDataList.size()) {
        				return false;
        			}
        			for (int i=0; i<opaqueList.size(); i++) {
	        			BaseOpaqueData opaque = opaqueList.get(i);
	        			boolean found = false;
        				for (int j=0; j<opaqueDataList.size(); i++) {
    	        			BaseOpaqueData myOpaque = opaqueDataList.get(j);
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": dataList=");
        if ((opaqueDataList != null) && !opaqueDataList.isEmpty()) {
        	for (BaseOpaqueData opaque : opaqueDataList) {
				sb.append(opaque.toString());
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
