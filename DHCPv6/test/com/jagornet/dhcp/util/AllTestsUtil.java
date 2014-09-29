package com.jagornet.dhcp.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsUtil {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsUtil.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestSubnet.class);
		suite.addTestSuite(TestLinkMap.class);
		//$JUnit-END$
		return suite;
	}

}
