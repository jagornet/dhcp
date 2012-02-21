package com.jagornet.dhcpv6.db;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsDbV2 {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsDbV2.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestJdbcIaManager.class);
		//$JUnit-END$
		return suite;
	}

}
