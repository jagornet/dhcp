/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.ha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RoutersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.JagornetDhcpServer;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.DhcpV4DiscoverProcessor;
import com.jagornet.dhcp.server.request.DhcpV4RequestProcessor;
import com.jagornet.dhcp.server.request.DhcpV6RequestProcessor;
import com.jagornet.dhcp.server.request.DhcpV6SolicitProcessor;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManagerImpl;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManagerImpl;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusService;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;
import com.jagornet.dhcp.server.testutil.TestHaClient;

/**
 * Comprehensive High Availability (HA) State Synchronization and Failover Test Suite.
 * Validates:
 * <ul>
 *   <li>Backup and Primary Finite State Machine transitions (init, polling, running, failover)</li>
 *   <li>Peer health polling, failure count accumulation, and failover trigger</li>
 *   <li>Health recovery and transition back to standby polling</li>
 *   <li>Link synchronization across HA peers</li>
 *   <li>Dynamic lease replication (Sync and Async modes)</li>
 *   <li>Request processor active gatekeeping (traffic dropping in standby vs serving on failover)</li>
 *   <li>End-to-end lease replication during dynamic DORA and SARR lifecycles</li>
 * </ul>
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpHighAvailability {

	private static final String CONFIG_XML =
		"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
		"  <v4ServerIdOption>\n" +
		"    <ipAddress>10.10.1.1</ipAddress>\n" +
		"  </v4ServerIdOption>\n" +
		"  <v6ServerIdOption>\n" +
		"    <opaqueData>\n" +
		"      <hexValue>00010001aabbccdd001122334455</hexValue>\n" +
		"    </opaqueData>\n" +
		"  </v6ServerIdOption>\n" +
		"  <policies>\n" +
		"    <policyList>\n" +
		"      <name>v4.defaultLeasetime</name>\n" +
		"      <value>86400</value>\n" +
		"    </policyList>\n" +
		"    <policyList>\n" +
		"      <name>v6.validLifetime</name>\n" +
		"      <value>86400</value>\n" +
		"    </policyList>\n" +
		"    <policyList>\n" +
		"      <name>v6.preferredLifetime</name>\n" +
		"      <value>43200</value>\n" +
		"    </policyList>\n" +
		"  </policies>\n" +
		"  <links>\n" +
		"    <linkList>\n" +
		"      <name>CorporateV4Link</name>\n" +
		"      <address>10.10.1.0/24</address>\n" +
		"      <v4ConfigOptions>\n" +
		"        <v4DomainNameOption>\n" +
		"          <domainName>corp.local</domainName>\n" +
		"        </v4DomainNameOption>\n" +
		"        <v4RoutersOption>\n" +
		"          <ipAddressList>10.10.1.1</ipAddressList>\n" +
		"        </v4RoutersOption>\n" +
		"      </v4ConfigOptions>\n" +
		"      <v4AddrPools>\n" +
		"        <poolList>\n" +
		"          <range>10.10.1.100-10.10.1.199</range>\n" +
		"        </poolList>\n" +
		"      </v4AddrPools>\n" +
		"    </linkList>\n" +
		"    <linkList>\n" +
		"      <name>CorporateV6Link</name>\n" +
		"      <address>2001:db8:1::/64</address>\n" +
		"      <v6MsgConfigOptions>\n" +
		"        <v6DnsServersOption>\n" +
		"          <ipAddressList>2001:db8:1::53</ipAddressList>\n" +
		"        </v6DnsServersOption>\n" +
		"      </v6MsgConfigOptions>\n" +
		"      <v6NaAddrPools>\n" +
		"        <poolList>\n" +
		"          <range>2001:db8:1::100-2001:db8:1::199</range>\n" +
		"        </poolList>\n" +
		"      </v6NaAddrPools>\n" +
		"    </linkList>\n" +
		"  </links>\n" +
		"</dhc:dhcpServerConfig>";

	private DhcpConfigTestContext context;
	private InMemoryIaManager iaManager;
	private V4AddrBindingManagerImpl v4Mgr;
	private V6NaAddrBindingManagerImpl v6Mgr;

	private TestHaClient testHaClient;
	private File tempHaDb;
	private HaBackupFSM activeBackupFSM;
	private HaPrimaryFSM activePrimaryFSM;

	private final InetAddress clientLinkV4;
	private final InetAddress clientLinkV6;

	public TestDhcpHighAvailability() throws Exception {
		clientLinkV4 = InetAddress.getByName("10.10.1.1");
		clientLinkV6 = InetAddress.getByName("2001:db8:1::1");
	}

	@Before
	public void setUp() throws Exception {
		context = new DhcpConfigTestContext();
		DhcpServerConfig serverConfig = context.initXml(CONFIG_XML);
		assertNotNull(serverConfig);

		iaManager = new InMemoryIaManager();

		v4Mgr = new V4AddrBindingManagerImpl();
		v4Mgr.setIaMgr(iaManager);
		v4Mgr.init();

		v6Mgr = new V6NaAddrBindingManagerImpl();
		v6Mgr.setIaMgr(iaManager);
		v6Mgr.init();

		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.setIaMgr(iaManager);
		config.setV4AddrBindingMgr(v4Mgr);
		config.setV6NaAddrBindingMgr(v6Mgr);

		tempHaDb = File.createTempFile("jagornet-ha-state-test", ".db");
		tempHaDb.delete();
		DhcpServerPolicies.setProperty(Property.HA_DATABASE_FILE, tempHaDb.getAbsolutePath());

		testHaClient = new TestHaClient();
	}

	@After
	public void tearDown() {
		if (activeBackupFSM != null) {
			try {
				activeBackupFSM.stopPollingTask();
			} catch (Exception ignored) {
			}
			shutdownBackupExecutor(activeBackupFSM);
			activeBackupFSM = null;
		}
		activePrimaryFSM = null;

		JagornetDhcpServer.haBackupFSM = null;
		JagornetDhcpServer.haPrimaryFSM = null;

		if (v4Mgr != null) {
			v4Mgr.close();
		}
		if (v6Mgr != null) {
			v6Mgr.close();
		}
		if (context != null) {
			context.close();
		}
		if (tempHaDb != null && tempHaDb.exists()) {
			tempHaDb.delete();
		}
	}

	private void shutdownBackupExecutor(HaBackupFSM fsm) {
		try {
			Field f = HaBackupFSM.class.getDeclaredField("pollingTaskExecutor");
			f.setAccessible(true);
			ExecutorService executor = (ExecutorService) f.get(fsm);
			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
			}
		} catch (Exception ignored) {
		}
	}

	private void injectHaStateDbManager(Object fsmTarget, HaStateDbManager dbManager) throws Exception {
		Field f = fsmTarget.getClass().getDeclaredField("haStateDbManager");
		f.setAccessible(true);
		f.set(fsmTarget, dbManager);
	}

	private void injectDhcpLinks(Object fsmTarget) throws Exception {
		Field f = fsmTarget.getClass().getDeclaredField("dhcpLinks");
		f.setAccessible(true);
		f.set(fsmTarget, DhcpServerConfiguration.getInstance().getLinkMap().values());
	}

	private void injectDhcpLeasesService(HaPrimaryFSM fsm, DhcpLeasesService leasesService) throws Exception {
		Field f = HaPrimaryFSM.class.getDeclaredField("dhcpLeasesService");
		f.setAccessible(true);
		f.set(fsm, leasesService);
	}

	private DhcpLink findLink(String name) {
		for (DhcpLink link : DhcpServerConfiguration.getInstance().getLinkMap().values()) {
			if (link.getLink() != null && name.equals(link.getLink().getName())) {
				return link;
			}
		}
		return null;
	}

	// ==================== HaBackupFSM Tests ====================

	@Test
	public void testBackupFsmStartupWithPrimaryAvailable() throws Exception {
		testHaClient.setPrimaryHaState(HaPrimaryFSM.State.PRIMARY_RUNNING);
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);

		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, dbManager);
		injectDhcpLinks(activeBackupFSM);

		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_INIT);
		activeBackupFSM.run();

		assertEquals("Backup should transition to BACKUP_POLLING when primary is reachable",
				HaBackupFSM.State.BACKUP_POLLING, activeBackupFSM.getState());
		assertEquals(HaPrimaryFSM.State.PRIMARY_RUNNING, activeBackupFSM.getPrimaryState());

		// Verify links were set to OK after link sync
		for (DhcpLink link : DhcpServerConfiguration.getInstance().getLinkMap().values()) {
			assertEquals("Configured links should be OK after backup link sync completes",
					DhcpLink.State.OK, link.getState());
		}
	}

	@Test
	public void testBackupFsmStartupWithPrimaryDown() throws Exception {
		testHaClient.setPrimaryHaState(null);
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);

		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, dbManager);
		injectDhcpLinks(activeBackupFSM);

		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_INIT);
		activeBackupFSM.run();

		assertEquals("Backup should immediately enter BACKUP_POLLING when primary is initially down",
				HaBackupFSM.State.BACKUP_POLLING, activeBackupFSM.getState());
		assertNull(activeBackupFSM.getPrimaryState());
	}

	@Test
	public void testBackupFsmFailoverTakeoverOnConsecutiveFailures() throws Exception {
		DhcpServerPolicies.setProperty(Property.HA_POLL_REPLY_FAILURE_COUNT, "3");
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);

		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, dbManager);

		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_POLLING);

		HaBackupFSM.PollingTask pollingTask = activeBackupFSM.new PollingTask();

		// Primary stops responding
		testHaClient.setStatus(null);

		// Tick 1
		pollingTask.run();
		assertEquals("First failure should not trigger failover",
				HaBackupFSM.State.BACKUP_POLLING, activeBackupFSM.getState());

		// Tick 2
		pollingTask.run();
		assertEquals("Second failure should not trigger failover",
				HaBackupFSM.State.BACKUP_POLLING, activeBackupFSM.getState());

		// Tick 3: Threshold reached (3 >= 3)
		pollingTask.run();
		assertEquals("Third consecutive failure must transition backup to BACKUP_RUNNING (failover takeover)",
				HaBackupFSM.State.BACKUP_RUNNING, activeBackupFSM.getState());
	}

	@Test
	public void testBackupFsmRecoveryWhenPrimaryReturnsOk() throws Exception {
		DhcpServerPolicies.setProperty(Property.HA_POLL_REPLY_FAILURE_COUNT, "2");
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);

		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, dbManager);

		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_POLLING);
		HaBackupFSM.PollingTask pollingTask = activeBackupFSM.new PollingTask();

		// Simulate 2 failures -> failover
		testHaClient.setStatus(null);
		pollingTask.run();
		pollingTask.run();
		assertEquals(HaBackupFSM.State.BACKUP_RUNNING, activeBackupFSM.getState());

		// Primary recovers
		testHaClient.setStatus(DhcpServerStatusService.STATUS_OK);
		pollingTask.run();

		assertEquals("When primary status returns OK, backup should stand down back to BACKUP_POLLING",
				HaBackupFSM.State.BACKUP_POLLING, activeBackupFSM.getState());
	}

	// ==================== HaPrimaryFSM Tests ====================

	@Test
	public void testPrimaryFsmStartupWithBackupDown() throws Exception {
		testHaClient.setBackupHaState(null);
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, false);

		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		injectDhcpLinks(activePrimaryFSM);

		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_INIT);
		activePrimaryFSM.run();

		assertEquals("Primary should transition directly to PRIMARY_RUNNING when backup is down",
				HaPrimaryFSM.State.PRIMARY_RUNNING, activePrimaryFSM.getState());
		assertNull(activePrimaryFSM.getBackupState());
	}

	@Test
	public void testPrimaryFsmStartupWithBackupAvailableAndLinkSync() throws Exception {
		testHaClient.setBackupHaState(HaBackupFSM.State.BACKUP_POLLING);
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, false);

		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		injectDhcpLinks(activePrimaryFSM);

		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_INIT);
		activePrimaryFSM.run();

		assertEquals("Primary should transition to PRIMARY_RUNNING after link sync",
				HaPrimaryFSM.State.PRIMARY_RUNNING, activePrimaryFSM.getState());
		assertEquals(HaBackupFSM.State.BACKUP_POLLING, activePrimaryFSM.getBackupState());

		for (DhcpLink link : DhcpServerConfiguration.getInstance().getLinkMap().values()) {
			assertEquals("Links should be OK after link sync completes",
					DhcpLink.State.OK, link.getState());
		}
	}

	@Test
	public void testPrimaryFsmBindingReplicationSyncMode() throws Exception {
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, false);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		injectDhcpLeasesService(activePrimaryFSM, new DhcpLeasesService());

		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_RUNNING);
		activePrimaryFSM.setBackupState(HaBackupFSM.State.BACKUP_POLLING);

		// Build test binding
		DhcpLink link = findLink("CorporateV4Link");
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(new byte[] { 0x01, 0x02, 0x03 });
		ia.setIatype((byte) 0);
		ia.setIaid(1L);
		ia.setState(IaAddress.LEASED);

		IaAddress addr = new IaAddress();
		addr.setIpAddress(InetAddress.getByName("10.10.1.155"));
		addr.setState(IaAddress.LEASED);
		addr.setHaPeerState(IaAddress.UNKNOWN);
		addr.setStartTime(new Date());
		addr.setPreferredEndTime(new Date(System.currentTimeMillis() + 86400000));
		addr.setValidEndTime(new Date(System.currentTimeMillis() + 86400000));

		ia.setIaAddresses(Collections.singletonList(addr));

		Binding binding = new Binding(ia, link);

		Map<Integer, DhcpOption> optionMap = new HashMap<>();
		DhcpV4RoutersOption routerOption = new DhcpV4RoutersOption(Collections.singletonList("10.10.1.1"));
		optionMap.put(DhcpConstants.V4OPTION_ROUTERS, routerOption);

		activePrimaryFSM.updateBindings(Collections.singletonList(binding), optionMap);

		assertEquals("Primary should call updateDhcpLease on HaClient",
				1, testHaClient.getUpdateLeaseCallCount());
		List<DhcpLease> received = testHaClient.getReceivedLeaseUpdates();
		assertEquals(1, received.size());
		assertEquals(InetAddress.getByName("10.10.1.155"), received.get(0).getIpAddress());
		assertEquals(IaAddress.LEASED, received.get(0).getState());
	}

	@Test
	public void testPrimaryFsmBindingReplicationAsyncMode() throws Exception {
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, true);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		injectDhcpLeasesService(activePrimaryFSM, new DhcpLeasesService());

		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_RUNNING);
		activePrimaryFSM.setBackupState(HaBackupFSM.State.BACKUP_POLLING);

		DhcpLink link = findLink("CorporateV4Link");
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(new byte[] { 0x05, 0x06 });
		ia.setIatype((byte) 0);
		ia.setIaid(2L);
		ia.setState(IaAddress.LEASED);

		IaAddress addr = new IaAddress();
		addr.setIpAddress(InetAddress.getByName("10.10.1.156"));
		addr.setState(IaAddress.LEASED);
		addr.setStartTime(new Date());
		addr.setPreferredEndTime(new Date(System.currentTimeMillis() + 86400000));
		addr.setValidEndTime(new Date(System.currentTimeMillis() + 86400000));

		ia.setIaAddresses(Collections.singletonList(addr));

		Binding binding = new Binding(ia, link);

		activePrimaryFSM.updateBindings(Collections.singletonList(binding), Collections.emptyMap());

		assertEquals("Primary in async mode should invoke updateDhcpLeaseAsync",
				1, testHaClient.getReceivedAsyncLeaseUpdates().size());
		assertEquals(InetAddress.getByName("10.10.1.156"),
				testHaClient.getReceivedAsyncLeaseUpdates().get(0).getIpAddress());
	}

	@Test
	public void testPrimaryFsmSkipsReplicationWhenBackupUnavailable() throws Exception {
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, false);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		injectDhcpLeasesService(activePrimaryFSM, new DhcpLeasesService());

		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_RUNNING);
		activePrimaryFSM.setBackupState(null); // backup is down

		DhcpLink link = findLink("CorporateV4Link");
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(new byte[] { 0x09 });
		ia.setIatype((byte) 0);
		ia.setIaid(3L);
		Binding binding = new Binding(ia, link);

		activePrimaryFSM.updateBindings(Collections.singletonList(binding), Collections.emptyMap());

		assertEquals("Primary should skip sending lease updates when backup peer is null/down",
				0, testHaClient.getUpdateLeaseCallCount());
	}

	// ==================== DhcpServerStatusService Tests ====================

	@Test
	public void testStatusServicePeerStateInteraction() throws Exception {
		DhcpServerStatusService statusService = new DhcpServerStatusService();
		assertEquals(DhcpServerStatusService.STATUS_OK, statusService.getStatus());

		// Test when running as Primary
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, false);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_RUNNING);
		JagornetDhcpServer.haPrimaryFSM = activePrimaryFSM;

		assertEquals("PRIMARY_RUNNING", statusService.getHaState());

		// Backup peer queries primary HA state
		String stateReply = statusService.haPeerGetHaState();
		assertEquals("PRIMARY_RUNNING", stateReply);
		assertEquals("Primary should mark backup as BACKUP_POLLING when backup queries its state",
				HaBackupFSM.State.BACKUP_POLLING, activePrimaryFSM.getBackupState());

		// Test SYNCING_TO_PEER notification
		statusService.setHaState(DhcpServerStatusService.SYNCING_TO_PEER);
		assertEquals(HaBackupFSM.State.BACKUP_SYNCING_FROM_PRIMARY, activePrimaryFSM.getBackupState());

		// Switch role to Backup
		JagornetDhcpServer.haPrimaryFSM = null;
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);
		HaStateDbManager backupDbMgr = new HaStateDbManager();
		backupDbMgr.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, backupDbMgr);
		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_RUNNING);
		JagornetDhcpServer.haBackupFSM = activeBackupFSM;

		assertEquals("BACKUP_RUNNING", statusService.getHaState());

		// Primary queries backup -> backup should cease and desist to BACKUP_POLLING
		String backupReply = statusService.haPeerGetHaState();
		assertEquals("BACKUP_POLLING", backupReply);
		assertEquals(HaBackupFSM.State.BACKUP_POLLING, activeBackupFSM.getState());
		assertEquals(HaPrimaryFSM.State.PRIMARY_RUNNING, activeBackupFSM.getPrimaryState());
	}

	// ==================== End-to-End Request Gatekeeping Tests ====================

	@Test
	public void testDhcpV4BackupGatekeepingStandbyVsFailover() throws Exception {
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, dbManager);

		JagornetDhcpServer.haBackupFSM = activeBackupFSM;

		// 1. In STANDBY (BACKUP_POLLING) -> message must be DROPPED
		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_POLLING);

		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:aa:bb:cc:dd:ee")
				.build();

		DhcpV4DiscoverProcessor discoverProcessor = new DhcpV4DiscoverProcessor(discoverMsg, clientLinkV4);
		assertFalse("preProcess must return false when backup is in BACKUP_POLLING",
				discoverProcessor.preProcess());
		DhcpV4Message droppedReply = discoverProcessor.processMessage();
		assertNull("DHCPv4 Discover must be dropped when backup is in standby", droppedReply);

		// 2. In FAILOVER (BACKUP_RUNNING) -> message must be SERVED
		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_RUNNING);

		DhcpV4DiscoverProcessor activeDiscover = new DhcpV4DiscoverProcessor(discoverMsg, clientLinkV4);
		assertTrue("preProcess must return true when backup transitions to BACKUP_RUNNING",
				activeDiscover.preProcess());
		DhcpV4Message offerMsg = activeDiscover.processMessage();
		assertNotNull("DHCPv4 Offer must be produced when backup is BACKUP_RUNNING", offerMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, offerMsg.getMessageType());

		// Selecting Request
		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac("00:aa:bb:cc:dd:ee")
				.withTransactionId(offerMsg.getTransactionId())
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.build();

		DhcpV4RequestProcessor requestProcessor = new DhcpV4RequestProcessor(requestMsg, clientLinkV4);
		DhcpV4Message ackMsg = requestProcessor.processMessage();
		assertNotNull("DHCPv4 ACK must be produced when backup is BACKUP_RUNNING", ackMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, ackMsg.getMessageType());
		assertEquals(offerMsg.getYiAddr(), ackMsg.getYiAddr());
	}

	@Test
	public void testDhcpV6BackupGatekeepingStandbyVsFailover() throws Exception {
		activeBackupFSM = new HaBackupFSM("192.168.1.10", 8080, testHaClient);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
		injectHaStateDbManager(activeBackupFSM, dbManager);

		JagornetDhcpServer.haBackupFSM = activeBackupFSM;

		String clientIdHex = "00010001aabbccdd112233445566";

		// 1. In STANDBY (BACKUP_POLLING) -> message must be DROPPED
		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_POLLING);

		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientId(clientIdHex)
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.build();

		DhcpV6SolicitProcessor solicitProcessor = new DhcpV6SolicitProcessor(solicitMsg, clientLinkV6);
		assertFalse("DHCPv6 preProcess must return false when backup is in BACKUP_POLLING",
				solicitProcessor.preProcess());
		DhcpV6Message droppedReply = solicitProcessor.processMessage();
		assertNull("DHCPv6 Solicit must be dropped when backup is in standby", droppedReply);

		// 2. In FAILOVER (BACKUP_RUNNING) -> message must be SERVED
		activeBackupFSM.setState(HaBackupFSM.State.BACKUP_RUNNING);

		DhcpV6SolicitProcessor activeSolicit = new DhcpV6SolicitProcessor(solicitMsg, clientLinkV6);
		assertTrue("DHCPv6 preProcess must return true when backup transitions to BACKUP_RUNNING",
				activeSolicit.preProcess());
		DhcpV6Message advMsg = activeSolicit.processMessage();
		assertNotNull("DHCPv6 Advertise must be produced when backup is BACKUP_RUNNING", advMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_ADVERTISE, advMsg.getMessageType());

		DhcpV6IaNaOption advIaNa = advMsg.getIaNaOptions().get(0);
		assertNotNull(advIaNa);
		assertFalse(advIaNa.getIaAddrOptions().isEmpty());
		DhcpV6IaAddrOption leasedV6 = advIaNa.getIaAddrOptions().get(0);

		// Request
		DhcpV6ServerIdOption serverId = advMsg.getDhcpServerIdOption();
		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientId(clientIdHex)
				.withServerId(serverId.getOpaqueData().getHex())
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.withIaAddr(leasedV6.getIpAddress())
				.build();

		DhcpV6RequestProcessor requestProcessor = new DhcpV6RequestProcessor(requestMsg, clientLinkV6);
		DhcpV6Message replyMsg = requestProcessor.processMessage();
		assertNotNull("DHCPv6 Reply must be produced when backup is BACKUP_RUNNING", replyMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
	}

	@Test
	public void testDhcpLinkStateGatekeepingDuringSync() throws Exception {
		DhcpLink v4Link = findLink("CorporateV4Link");
		DhcpLink v6Link = findLink("CorporateV6Link");

		// Set links to NOT_SYNCED (simulating link sync in progress)
		v4Link.setState(DhcpLink.State.NOT_SYNCED);
		v6Link.setState(DhcpLink.State.NOT_SYNCED);

		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:11:11:11:11")
				.build();
		DhcpV4DiscoverProcessor v4Proc = new DhcpV4DiscoverProcessor(discoverMsg, clientLinkV4);
		assertFalse("DHCPv4 preProcess must fail when link is NOT_SYNCED", v4Proc.preProcess());
		assertNull("DHCPv4 Discover must be dropped when link is NOT_SYNCED", v4Proc.processMessage());

		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientId("0001000111111111111111111111")
				.withIaNa(1)
				.withRemoteAddress("2001:db8:1::100", 546)
				.build();
		DhcpV6SolicitProcessor v6Proc = new DhcpV6SolicitProcessor(solicitMsg, clientLinkV6);
		assertFalse("DHCPv6 preProcess must fail when link is NOT_SYNCED", v6Proc.preProcess());
		assertNull("DHCPv6 Solicit must be dropped when link is NOT_SYNCED", v6Proc.processMessage());

		// Restore links to OK
		v4Link.setState(DhcpLink.State.OK);
		v6Link.setState(DhcpLink.State.OK);

		assertNotNull("DHCPv4 Discover should succeed once link is OK",
				new DhcpV4DiscoverProcessor(discoverMsg, clientLinkV4).processMessage());
		assertNotNull("DHCPv6 Solicit should succeed once link is OK",
				new DhcpV6SolicitProcessor(solicitMsg, clientLinkV6).processMessage());
	}

	@Test
	public void testPrimaryEndToEndDynamicReplicationOnDoraAndSarr() throws Exception {
		activePrimaryFSM = new HaPrimaryFSM("192.168.1.20", 8080, testHaClient, false);
		HaStateDbManager dbManager = new HaStateDbManager();
		dbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
		injectHaStateDbManager(activePrimaryFSM, dbManager);
		injectDhcpLeasesService(activePrimaryFSM, new DhcpLeasesService());

		activePrimaryFSM.setState(HaPrimaryFSM.State.PRIMARY_RUNNING);
		activePrimaryFSM.setBackupState(HaBackupFSM.State.BACKUP_POLLING);
		JagornetDhcpServer.haPrimaryFSM = activePrimaryFSM;

		// 1. Complete DHCPv4 DORA
		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:33:33:33:33:33")
				.build();
		DhcpV4Message offer = new DhcpV4DiscoverProcessor(discover, clientLinkV4).processMessage();
		assertNotNull(offer);

		DhcpV4Message v4Request = DhcpV4MessageBuilder.request()
				.withMac("00:33:33:33:33:33")
				.withTransactionId(offer.getTransactionId())
				.withRequestedIp(offer.getYiAddr().getHostAddress())
				.withServerId(offer.getDhcpV4ServerIdOption().getIpAddress())
				.build();
		DhcpV4Message ack = new DhcpV4RequestProcessor(v4Request, clientLinkV4).processMessage();
		assertNotNull(ack);

		// Verify DHCPv4 lease was replicated to backup peer
		List<DhcpLease> replicated = testHaClient.getReceivedLeaseUpdates();
		assertTrue("DHCPv4 lease for " + offer.getYiAddr() + " must be replicated to backup via HaClient",
				replicated.stream().anyMatch(l -> l.getIpAddress().equals(offer.getYiAddr())));

		// 2. Complete DHCPv6 SARR
		String clientIdHex = "0001000144444444444444444444";
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId(clientIdHex)
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.build();
		DhcpV6Message adv = new DhcpV6SolicitProcessor(solicit, clientLinkV6).processMessage();
		assertNotNull(adv);

		DhcpV6IaAddrOption leasedV6 = adv.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		DhcpV6ServerIdOption serverId = adv.getDhcpServerIdOption();

		DhcpV6Message v6Request = DhcpV6MessageBuilder.request()
				.withClientId(clientIdHex)
				.withServerId(serverId.getOpaqueData().getHex())
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.withIaAddr(leasedV6.getIpAddress())
				.build();
		DhcpV6Message reply = new DhcpV6RequestProcessor(v6Request, clientLinkV6).processMessage();
		assertNotNull(reply);

		// Verify DHCPv6 lease was replicated to backup peer
		assertTrue("DHCPv6 lease for " + leasedV6.getIpAddress() + " must be replicated to backup via HaClient",
				testHaClient.getReceivedLeaseUpdates().stream()
						.anyMatch(l -> l.getIpAddress().getHostAddress().equals(leasedV6.getIpAddress())));
	}
}
