/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestOpaqueDataUtil.java is part of DHCPv6.
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

import java.util.Arrays;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.option.base.BaseOpaqueData;
import com.jagornet.dhcpv6.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class TestOpaqueDataUtil.
 */
public class TestOpaqueDataUtil extends TestCase
{
	
	/**
	 * Test hex.
	 * 
	 * @throws Exception the exception
	 */
	public void testHex() throws Exception
	{
		String hex1 = "ABCDEF0123456789";
		String hex2 = "abcdef0123456789";
		byte[] b1 = Util.fromHexString(hex1);
		byte[] b2 = Util.fromHexString(hex2);
		assertTrue(Arrays.equals(b1, b2));
	}
	
	/**
	 * Test equals.
	 * 
	 * @throws Exception the exception
	 */
	public void testEquals() throws Exception
	{
		BaseOpaqueData od1 = new BaseOpaqueData();
		od1.setHex(Util.fromHexString("ABCDEF0123456789"));
		BaseOpaqueData od2 = new BaseOpaqueData();
		od2.setHex(Util.fromHexString("abcdef0123456789"));
		assertTrue(OpaqueDataUtil.equals(od1, od2));
	}
	
}
