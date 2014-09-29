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
