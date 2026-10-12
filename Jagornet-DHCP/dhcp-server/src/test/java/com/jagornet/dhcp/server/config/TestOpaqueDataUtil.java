/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

import java.util.Arrays;

import junit.framework.TestCase;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.OpaqueDataUtil;

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

	/** toBaseOpaqueData with null returns null. */
	public void testToBaseOpaqueDataNull() {
		assertNull(OpaqueDataUtil.toBaseOpaqueData(null));
	}

	/** toString with null returns null. */
	public void testToStringNull() {
		assertNull(OpaqueDataUtil.toString(null));
	}
	
}
