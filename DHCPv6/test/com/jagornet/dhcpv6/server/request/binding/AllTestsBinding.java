package com.jagornet.dhcpv6.server.request.binding;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsBinding {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsBinding.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(TestFreeList.suite());
		suite.addTestSuite(TestPrefixBindingPool.class);
		suite.addTestSuite(TestBindingManager.class);
		//$JUnit-END$
		return suite;
	}

}
