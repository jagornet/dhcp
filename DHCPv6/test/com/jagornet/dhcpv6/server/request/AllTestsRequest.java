package com.jagornet.dhcpv6.server.request;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsRequest {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsRequest.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TestDhcpReleaseProcessor.class);
		suite.addTestSuite(TestDhcpMessageHandler.class);
		suite.addTestSuite(TestDhcpSolicitProcessor.class);
		suite.addTestSuite(TestDhcpRenewProcessor.class);
		suite.addTestSuite(TestDhcpRebindProcessor.class);
		suite.addTestSuite(TestDhcpDeclineProcessor.class);
		suite.addTestSuite(TestDhcpRequestProcessor.class);
		suite.addTestSuite(TestDhcpConfirmProcessor.class);
		//$JUnit-END$
		return suite;
	}

}
