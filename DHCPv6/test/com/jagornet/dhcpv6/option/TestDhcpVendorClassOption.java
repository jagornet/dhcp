/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpVendorClassOption.java is part of DHCPv6.
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

import java.nio.ByteBuffer;
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.option.base.BaseOpaqueData;
import com.jagornet.dhcpv6.option.v6.DhcpV6VendorClassOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcp.xml.ClientClassExpression;
import com.jagornet.dhcp.xml.Operator;

/**
 * The Class TestDhcpVendorClassOption.
 */
public class TestDhcpVendorClassOption extends TestCase
{
	protected DhcpV6VendorClassOption dvco;
	
	public TestDhcpVendorClassOption()
	{
		dvco = new DhcpV6VendorClassOption();
		dvco.setEnterpriseNumber(12345);
        dvco.addOpaqueData("VendorClass 1");   // 15 (len=2 bytes, data=11 bytes)
        dvco.addOpaqueData("VendorClass 2");   // 15 (len=2 bytes, data=11 bytes)		
	}
	
    /**
     * Test encode.
     * 
     * @throws Exception the exception
     */
    public void testEncode() throws Exception
    {
        ByteBuffer bb = dvco.encode();
        assertNotNull(bb);
        assertEquals(38, bb.capacity());    // +4 (code=2 bytes, len=2 bytes)
        assertEquals(38, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_VENDOR_CLASS, bb.getShort());
        assertEquals((short)34, bb.getShort());   // length
        assertEquals(12345, bb.getInt());
        assertEquals((short)13, bb.getShort());
    }

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecode() throws Exception
    {
        // just 28 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(36);
        bb.putShort((short)34);     // length of option
        bb.putInt(12345);
        bb.putShort((short)13);     // length of "VendorClass 1"
        bb.put("VendorClass 1".getBytes());
        bb.putShort((short)13);
        bb.put("VendorClass 2".getBytes());
        bb.flip();
        DhcpV6VendorClassOption _dvco = new DhcpV6VendorClassOption();
        _dvco.decode(bb);
        assertEquals(12345, _dvco.getEnterpriseNumber());
        List<BaseOpaqueData> VendorClasses = _dvco.getOpaqueDataList();
        assertNotNull(VendorClasses);
        assertEquals(2, VendorClasses.size());
        assertEquals("VendorClass 1", VendorClasses.get(0).getAscii());
        assertEquals("VendorClass 2", VendorClasses.get(1).getAscii());
    }
    
    public void testMatches() throws Exception
    {
        ClientClassExpression expression = ClientClassExpression.Factory.newInstance();
        assertFalse(dvco.matches((DhcpV6VendorClassOption)expression.getV6VendorClassOption(), Operator.EQUALS));
        DhcpV6VendorClassOption _dvco = new DhcpV6VendorClassOption(expression.getV6VendorClassOption());
        assertFalse(dvco.matches(_dvco, Operator.EQUALS));
        _dvco.setEnterpriseNumber(12345);
        assertFalse(dvco.matches(_dvco, Operator.EQUALS));
        _dvco.addOpaqueData("VendorClass 1");
        _dvco.addOpaqueData("VendorClass 2");
        assertTrue(dvco.matches(_dvco, Operator.EQUALS));
        _dvco.setEnterpriseNumber(999);
        assertFalse(dvco.matches(_dvco, Operator.EQUALS));
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        System.out.println(dvco);
    }
}
