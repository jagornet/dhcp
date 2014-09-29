package com.jagornet.dhcp.server.request;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsRequest {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsRequest.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestDhcpV6ReleaseProcessor.class);
		suite.addTestSuite(TestDhcpV6MessageHandler.class);
		suite.addTestSuite(TestDhcpV6SolicitProcessor.class);
		suite.addTestSuite(TestDhcpV6RenewProcessor.class);
		suite.addTestSuite(TestDhcpV6RebindProcessor.class);
		suite.addTestSuite(TestDhcpV6DeclineProcessor.class);
		suite.addTestSuite(TestDhcpV6RequestProcessor.class);
		suite.addTestSuite(TestDhcpV6ConfirmProcessor.class);
		//$JUnit-END$
		return suite;
	}

}
