package com.jagornet.dhcp.server.request.binding;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsBinding {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsBinding.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(TestFreeList.suite());
		suite.addTestSuite(TestV6PrefixBindingPool.class);
		suite.addTestSuite(TestV6NaAddrBindingManager.class);
		//$JUnit-END$
		return suite;
	}

}
