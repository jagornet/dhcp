package com.jagornet.dhcp.message;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsMessage {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsMessage.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestDhcpMessage.class);
		suite.addTestSuite(TestDhcpRelayMessage.class);
		//$JUnit-END$
		return suite;
	}

}
