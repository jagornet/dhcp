/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpConfigOptions.java is part of DHCPv6.
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

import java.net.InetSocketAddress;
import java.util.Arrays;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.ConfigOptionsType;
import com.jagornet.dhcpv6.xml.PreferenceOption;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpConfigOptions.
 */
public class TestDhcpConfigOptions extends TestCase 
{
	
	/**
	 * Test set message options.
	 * 
	 * @throws Exception the exception
	 */
	public void testSetMessageOptions() throws Exception
	{
		// override the default, which is false
		DhcpServerPolicies.setProperty(Property.SEND_REQUESTED_OPTIONS_ONLY.key(), "true");
		
		ConfigOptionsType configOptions = ConfigOptionsType.Factory.newInstance();
		PreferenceOption preferenceOption = PreferenceOption.Factory.newInstance();
		preferenceOption.setUnsignedByte((short)10);
		
		configOptions.setPreferenceOption(preferenceOption);

		// wrap the ConfigOptions XML object inside a DhcpConfigOptions object
		DhcpConfigOptions dhcpConfigOptions = new DhcpConfigOptions(configOptions);
		
		InetSocketAddress local = new InetSocketAddress(0);
		InetSocketAddress remote = new InetSocketAddress(0);
		
		DhcpMessage dhcpMsg = new DhcpMessage(local, remote);
		dhcpMsg.setDhcpOptionMap(dhcpConfigOptions.getDhcpOptionMap());
		
		DhcpPreferenceOption dhcpPreferenceOpt = 
			(DhcpPreferenceOption) dhcpMsg.getDhcpOption(DhcpConstants.OPTION_PREFERENCE);
		// we won't find it the first time because sendRequestedOptionsOnly = true;
		assertNull(dhcpPreferenceOpt);
		
		// now set the requested option, and test again
		dhcpConfigOptions = 
			new DhcpConfigOptions(configOptions, Arrays.asList(DhcpConstants.OPTION_PREFERENCE));
		dhcpMsg.setDhcpOptionMap(dhcpConfigOptions.getDhcpOptionMap());
		dhcpPreferenceOpt = (DhcpPreferenceOption) dhcpMsg.getDhcpOption(DhcpConstants.OPTION_PREFERENCE);
		assertNotNull(dhcpPreferenceOpt);
		assertEquals((short)10, dhcpPreferenceOpt.getUnsignedByteOption().getUnsignedByte());
	}
}
