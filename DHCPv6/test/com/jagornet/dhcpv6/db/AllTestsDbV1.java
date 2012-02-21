package com.jagornet.dhcpv6.db;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsDbV1 {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsDbV1.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestJdbcIaManager.class);
		suite.addTestSuite(TestJdbcIdentityAssocDAO.class);
		suite.addTestSuite(TestJdbcIaAddressDAO.class);
		//$JUnit-END$
		return suite;
	}

}
