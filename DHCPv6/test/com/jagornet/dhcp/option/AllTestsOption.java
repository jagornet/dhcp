/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file AllTestsOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.option;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsOption {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsOption.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestDhcpPreferenceOption.class);
		suite.addTestSuite(TestDhcpDnsServersOption.class);
		suite.addTestSuite(TestDhcpStatusCodeOption.class);
		suite.addTestSuite(TestDhcpUserClassOption.class);
		suite.addTestSuite(TestDhcpDomainSearchListOption.class);
		suite.addTestSuite(TestOpaqueDataUtil.class);
		suite.addTestSuite(TestDhcpIaNaOption.class);
		//$JUnit-END$
		return suite;
	}

}
