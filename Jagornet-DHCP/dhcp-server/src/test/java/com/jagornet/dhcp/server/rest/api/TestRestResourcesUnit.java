/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Base64;
import java.util.Date;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.jagornet.dhcp.server.JagornetDhcpServer;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.Policy;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.ha.HaBackupFSM;
import com.jagornet.dhcp.server.ha.HaPrimaryFSM;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;

/**
 * Comprehensive Unit Test Suite for JAX-RS REST API Resources, Services,
 * and Authentication / Authorization Filters.
 * 
 * @author A. Gregory Rabil
 */
public class TestRestResourcesUnit {

	private DhcpConfigTestContext context;
	private InMemoryIaManager iaMgr;
	private File tempConfigFile;

	private static final String SAMPLE_CONFIG_XML = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n"
			+
			"  <v4ServerIdOption><ipAddress>192.168.1.1</ipAddress></v4ServerIdOption>\n" +
			"  <policies>\n" +
			"    <policyList><name>v4.defaultLeasetime</name><value>7200</value></policyList>\n" +
			"    <policyList><name>rest.api.username</name><value>jagornet</value></policyList>\n" +
			"    <policyList><name>rest.api.password</name><value>jagornet</value></policyList>\n" +
			"    <policyList><name>ha.peerUsername</name><value>hapeer</value></policyList>\n" +
			"    <policyList><name>ha.peerPassword</name><value>jagornet</value></policyList>\n" +
			"  </policies>\n" +
			"  <links>\n" +
			"    <linkList>\n" +
			"      <name>CorporateLink</name>\n" +
			"      <address>192.168.1.0/24</address>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList><range>192.168.1.100-192.168.1.199</range></poolList>\n" +
			"      </v4AddrPools>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

	@Before
	public void setUp() throws Exception {
		context = new DhcpConfigTestContext();
		context.initXml(SAMPLE_CONFIG_XML);

		iaMgr = new InMemoryIaManager();
		DhcpServerConfiguration.getInstance().setIaMgr(iaMgr);

		tempConfigFile = File.createTempFile("jagornet-rest-test-config", ".xml");
		tempConfigFile.deleteOnExit();

		// Configure serverConfigFilename so saveConfig persists cleanly
		Field filenameField = DhcpServerConfiguration.class.getDeclaredField("serverConfigFilename");
		filenameField.setAccessible(true);
		filenameField.set(null, tempConfigFile.getAbsolutePath());

		// Save initial config to temp file
		DhcpServerConfiguration.saveConfig(DhcpServerConfiguration.getInstance().getJaxbServerConfig(),
				tempConfigFile.getAbsolutePath());
	}

	@After
	public void tearDown() throws Exception {
		JagornetDhcpServer.haPrimaryFSM = null;
		JagornetDhcpServer.haBackupFSM = null;
		if (context != null) {
			context.close();
		}
		if (tempConfigFile != null && tempConfigFile.exists()) {
			tempConfigFile.delete();
		}
	}

	// =========================================================================
	// 1. AuthenticationResource Tests
	// =========================================================================

	@Test
	public void testAuthenticationResourceFormSuccess() {
		AuthenticationResource resource = new AuthenticationResource();
		Response response = resource.authenticateUser("jagornet", "jagornet");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String token = (String) response.getEntity();
		assertNotNull(token);
		assertFalse(token.isEmpty());
	}

	@Test
	public void testAuthenticationResourceFormInvalidPassword() {
		AuthenticationResource resource = new AuthenticationResource();
		Response response = resource.authenticateUser("jagornet", "wrongpassword");
		assertNotNull(response);
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	public void testAuthenticationResourceFormInvalidUser() {
		AuthenticationResource resource = new AuthenticationResource();
		Response response = resource.authenticateUser("intruder", "jagornet");
		assertNotNull(response);
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	public void testAuthenticationResourceJsonSuccess() {
		AuthenticationResource resource = new AuthenticationResource();
		Credentials creds = new Credentials();
		creds.setUsername("jagornet");
		creds.setPassword("jagornet");
		Response response = resource.authenticateUser(creds);
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String token = (String) response.getEntity();
		assertNotNull(token);
	}

	@Test
	public void testAuthenticationResourceJsonFailure() {
		AuthenticationResource resource = new AuthenticationResource();
		Credentials creds = new Credentials();
		creds.setUsername("jagornet");
		creds.setPassword("badpass");
		Response response = resource.authenticateUser(creds);
		assertNotNull(response);
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	// =========================================================================
	// 2. AuthenticationFilter & AuthFilterSecurityContext Tests
	// =========================================================================

	@Test
	public void testAuthenticationFilterMissingHeaderAbortsUnauthorized() throws Exception {
		AuthenticationFilter filter = new AuthenticationFilter();
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

		filter.filter(requestContext);

		ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(responseCaptor.capture());
		Response aborted = responseCaptor.getValue();
		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), aborted.getStatus());
		assertTrue(aborted.getHeaderString(HttpHeaders.WWW_AUTHENTICATE).contains("Basic"));
	}

	@Test
	public void testAuthenticationFilterNonBasicHeaderAbortsUnauthorized() throws Exception {
		AuthenticationFilter filter = new AuthenticationFilter();
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer some-token");

		filter.filter(requestContext);

		ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(responseCaptor.capture());
		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseCaptor.getValue().getStatus());
	}

	@Test
	public void testAuthenticationFilterMalformedTokenAbortsUnauthorized() throws Exception {
		AuthenticationFilter filter = new AuthenticationFilter();
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		// Malformed base64 token will cause exception in decoder and trigger
		// abortWithUnauthorized
		String badAuth = "Basic %%%not-valid-base64%%%";
		when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(badAuth);

		filter.filter(requestContext);

		ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(responseCaptor.capture());
		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), responseCaptor.getValue().getStatus());
	}

	@Test
	public void testAuthenticationFilterInvalidCredentialsDeniesRoles() throws Exception {
		AuthenticationFilter filter = new AuthenticationFilter();
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		SecurityContext baseSecurityContext = mock(SecurityContext.class);
		when(requestContext.getSecurityContext()).thenReturn(baseSecurityContext);

		String badCredsAuth = "Basic " + Base64.getEncoder().encodeToString("baduser:badpass".getBytes());
		when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(badCredsAuth);

		filter.filter(requestContext);

		ArgumentCaptor<SecurityContext> secCaptor = ArgumentCaptor.forClass(SecurityContext.class);
		verify(requestContext).setSecurityContext(secCaptor.capture());
		SecurityContext authSec = secCaptor.getValue();
		assertNotNull(authSec);
		assertNull(authSec.getUserPrincipal().getName());
		try {
			authSec.isUserInRole(AuthenticationFilter.AuthRole.REST_API.toString());
			fail("Expected NullPointerException for null username in isUserInRole");
		} catch (NullPointerException expected) {
			// Expected because unauthenticated principal cannot evaluate role equality
		}
	}

	@Test
	public void testAuthenticationFilterValidRestApiUserSetsSecurityContext() throws Exception {
		AuthenticationFilter filter = new AuthenticationFilter();
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		SecurityContext baseSecurityContext = mock(SecurityContext.class);
		when(requestContext.getSecurityContext()).thenReturn(baseSecurityContext);

		String validAuth = "Basic " + Base64.getEncoder().encodeToString("jagornet:jagornet".getBytes());
		when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(validAuth);

		filter.filter(requestContext);

		ArgumentCaptor<SecurityContext> secCaptor = ArgumentCaptor.forClass(SecurityContext.class);
		verify(requestContext).setSecurityContext(secCaptor.capture());
		SecurityContext authSec = secCaptor.getValue();
		assertNotNull(authSec);
		assertEquals("jagornet", authSec.getUserPrincipal().getName());
		assertTrue("Must have REST_API role", authSec.isUserInRole(AuthenticationFilter.AuthRole.REST_API.toString()));
		assertFalse("Must not have HA_PEER role",
				authSec.isUserInRole(AuthenticationFilter.AuthRole.HA_PEER.toString()));
	}

	@Test
	public void testAuthenticationFilterValidHaPeerUserSetsSecurityContext() throws Exception {
		AuthenticationFilter filter = new AuthenticationFilter();
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		SecurityContext baseSecurityContext = mock(SecurityContext.class);
		when(requestContext.getSecurityContext()).thenReturn(baseSecurityContext);

		String validAuth = "Basic " + Base64.getEncoder().encodeToString("hapeer:jagornet".getBytes());
		when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(validAuth);

		filter.filter(requestContext);

		ArgumentCaptor<SecurityContext> secCaptor = ArgumentCaptor.forClass(SecurityContext.class);
		verify(requestContext).setSecurityContext(secCaptor.capture());
		SecurityContext authSec = secCaptor.getValue();
		assertNotNull(authSec);
		assertEquals("hapeer", authSec.getUserPrincipal().getName());
		assertTrue("Must have HA_PEER role", authSec.isUserInRole(AuthenticationFilter.AuthRole.HA_PEER.toString()));
		assertFalse("Must not have REST_API role",
				authSec.isUserInRole(AuthenticationFilter.AuthRole.REST_API.toString()));
	}

	// =========================================================================
	// 3. DhcpServerStatusResource & Service Tests
	// =========================================================================

	@Test
	public void testDhcpServerStatusResourceGetStatusRestApiRole() {
		DhcpServerStatusResource resource = new DhcpServerStatusResource();
		ContainerRequestContext context = mock(ContainerRequestContext.class);
		SecurityContext secContext = mock(SecurityContext.class);
		when(context.getSecurityContext()).thenReturn(secContext);
		when(secContext.isUserInRole("REST_API")).thenReturn(true);

		String status = resource.getStatus(context);
		assertEquals(DhcpServerStatusService.STATUS_OK, status);
	}

	@Test
	public void testDhcpServerStatusResourceGetStatusHaPeerRole() {
		DhcpServerStatusResource resource = new DhcpServerStatusResource();
		ContainerRequestContext context = mock(ContainerRequestContext.class);
		SecurityContext secContext = mock(SecurityContext.class);
		when(context.getSecurityContext()).thenReturn(secContext);
		when(secContext.isUserInRole("HA_PEER")).thenReturn(true);

		String status = resource.getStatus(context);
		assertEquals(DhcpServerStatusService.STATUS_OK, status);
	}

	@Test
	public void testDhcpServerStatusResourceGetStatusNoRoleReturnsNull() {
		DhcpServerStatusResource resource = new DhcpServerStatusResource();
		ContainerRequestContext context = mock(ContainerRequestContext.class);
		when(context.getSecurityContext()).thenReturn(null);

		assertNull(resource.getStatus(context));
	}

	@Test
	public void testDhcpServerStatusResourceGetHaStateNotConfigured() {
		DhcpServerStatusResource resource = new DhcpServerStatusResource();
		ContainerRequestContext context = mock(ContainerRequestContext.class);
		SecurityContext secContext = mock(SecurityContext.class);
		when(context.getSecurityContext()).thenReturn(secContext);
		when(secContext.isUserInRole("REST_API")).thenReturn(true);

		String state = resource.getHaState(context);
		assertEquals("HA not configured", state);
	}

	@Test
	public void testDhcpServerStatusResourceGetHaStatePrimaryActive() {
		HaPrimaryFSM primaryFSM = mock(HaPrimaryFSM.class);
		when(primaryFSM.getState()).thenReturn(HaPrimaryFSM.State.PRIMARY_RUNNING);
		JagornetDhcpServer.haPrimaryFSM = primaryFSM;

		DhcpServerStatusResource resource = new DhcpServerStatusResource();
		ContainerRequestContext context = mock(ContainerRequestContext.class);
		SecurityContext secContext = mock(SecurityContext.class);
		when(context.getSecurityContext()).thenReturn(secContext);
		when(secContext.isUserInRole("REST_API")).thenReturn(true);

		String state = resource.getHaState(context);
		assertTrue("State should reflect PRIMARY_RUNNING", state.contains("PRIMARY_RUNNING"));
	}

	@Test
	public void testDhcpServerStatusResourceGetHaStateBackupActive() {
		HaBackupFSM backupFSM = mock(HaBackupFSM.class);
		when(backupFSM.getState()).thenReturn(HaBackupFSM.State.BACKUP_RUNNING);
		JagornetDhcpServer.haBackupFSM = backupFSM;

		DhcpServerStatusResource resource = new DhcpServerStatusResource();
		ContainerRequestContext context = mock(ContainerRequestContext.class);
		SecurityContext secContext = mock(SecurityContext.class);
		when(context.getSecurityContext()).thenReturn(secContext);
		when(secContext.isUserInRole("REST_API")).thenReturn(true);

		String state = resource.getHaState(context);
		assertTrue("State should reflect BACKUP_RUNNING", state.contains("BACKUP_RUNNING"));
	}

	@Test
	public void testDhcpServerStatusServiceSetHaStateSyncingToPeer() {
		HaPrimaryFSM primaryFSM = mock(HaPrimaryFSM.class);
		JagornetDhcpServer.haPrimaryFSM = primaryFSM;

		DhcpServerStatusService service = new DhcpServerStatusService();
		service.setHaState(DhcpServerStatusService.SYNCING_TO_PEER);

		verify(primaryFSM).setBackupState(HaBackupFSM.State.BACKUP_SYNCING_FROM_PRIMARY);
	}

	// =========================================================================
	// 4. DhcpServerConfigResource & Service Tests
	// =========================================================================

	@Test
	public void testDhcpServerConfigResourceGetConfig() {
		DhcpServerConfigResource resource = new DhcpServerConfigResource();
		Response response = resource.getDhcpServerConfig();
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		DhcpServerConfig config = (DhcpServerConfig) response.getEntity();
		assertNotNull(config);
		assertEquals("192.168.1.1", config.getV4ServerIdOption().getIpAddress());
	}

	@Test
	public void testDhcpServerConfigResourceGetGlobalPolicies() {
		DhcpServerConfigResource resource = new DhcpServerConfigResource();
		Response response = resource.getGlobalPolicies();
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		PoliciesType policies = (PoliciesType) response.getEntity();
		assertNotNull(policies);
		assertFalse(policies.getPolicyList().isEmpty());
	}

	@Test
	public void testDhcpServerConfigResourcePutGlobalPolicies() {
		DhcpServerConfigResource resource = new DhcpServerConfigResource();
		PoliciesType newPolicies = new PoliciesType();
		Policy policy = new Policy();
		policy.setName("v4.defaultLeasetime");
		policy.setValue("14400");
		newPolicies.getPolicyList().add(policy);

		Response response = resource.putGlobalPolicies(newPolicies);
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		PoliciesType returned = (PoliciesType) response.getEntity();
		assertNotNull(returned);
		assertEquals("14400", returned.getPolicyList().get(0).getValue());
	}

	@Test
	public void testDhcpServerConfigResourcePutNullConfigReturnsError() {
		DhcpServerConfigResource resource = new DhcpServerConfigResource();
		Response response = resource.putDhcpServerConfig(null);
		assertNotNull(response);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDhcpServerConfigResourcePutNullGlobalPoliciesReturnsError() {
		DhcpServerConfigResource resource = new DhcpServerConfigResource();
		Response response = resource.putGlobalPolicies(null);
		assertNotNull(response);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	// =========================================================================
	// 5. DhcpLinksResource & Service Tests
	// =========================================================================

	@Test
	public void testDhcpLinksResourceGetLinkStateSuccess() {
		DhcpLinksResource resource = new DhcpLinksResource();
		// Configured link in SAMPLE_CONFIG_XML is 192.168.1.0/24 -> dash format
		// "192.168.1.0-24"
		Response response = resource.getLinkState("192.168.1.0-24");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals(DhcpLink.State.OK.toString(), response.getEntity());
	}

	@Test
	public void testDhcpLinksResourceGetLinkStateNotFound() {
		DhcpLinksResource resource = new DhcpLinksResource();
		Response response = resource.getLinkState("10.99.99.0-24");
		assertNotNull(response);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		assertTrue(((String) response.getEntity()).contains("Link state not found"));
	}

	@Test
	public void testDhcpLinksResourceGetLinkStateMalformedSubnet() {
		DhcpLinksResource resource = new DhcpLinksResource();
		Response response = resource.getLinkState("invalid_subnet_format");
		assertNotNull(response);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		assertTrue(((String) response.getEntity()).contains("Subnet invalid"));
	}

	// =========================================================================
	// 6. DhcpLeasesResource & Service Tests
	// =========================================================================

	private DhcpLease createSampleLease(String ipStr) throws Exception {
		DhcpLease lease = new DhcpLease();
		lease.setIpAddress(InetAddress.getByName(ipStr));
		lease.setDuid("001122334455".getBytes());
		lease.setIatype(IdentityAssoc.V4_TYPE);
		lease.setIaid(0);
		lease.setState(IaAddress.LEASED);
		lease.setHaPeerState(IaAddress.OFFERED);
		lease.setStartTime(new Date());
		lease.setPreferredEndTime(new Date(System.currentTimeMillis() + 3600000));
		lease.setValidEndTime(new Date(System.currentTimeMillis() + 7200000));
		return lease;
	}

	@Test
	public void testDhcpLeasesResourceGetExistingLease() throws Exception {
		DhcpLease lease = createSampleLease("192.168.1.150");
		iaMgr.insertDhcpLease(lease);

		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.getDhcpLease("192.168.1.150");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		DhcpLease fetched = (DhcpLease) response.getEntity();
		assertEquals("192.168.1.150", fetched.getIpAddress().getHostAddress());
	}

	@Test
	public void testDhcpLeasesResourceGetNonExistentLeaseReturns404() {
		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.getDhcpLease("192.168.1.199");
		assertNotNull(response);
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDhcpLeasesResourcePostNewLease() throws Exception {
		DhcpLeasesResource resource = new DhcpLeasesResource();
		DhcpLease newLease = createSampleLease("192.168.1.151");

		Response response = resource.postDhcpLease(newLease, "false");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		DhcpLease inDb = iaMgr.findDhcpLeaseForInetAddr(InetAddress.getByName("192.168.1.151"));
		assertNotNull(inDb);
		assertEquals(IaAddress.OFFERED, inDb.getHaPeerState());
	}

	@Test
	public void testDhcpLeasesResourcePostNewLeaseWithHaUpdateSync() throws Exception {
		DhcpLeasesResource resource = new DhcpLeasesResource();
		DhcpLease newLease = createSampleLease("192.168.1.152");

		Response response = resource.postDhcpLease(newLease, "true");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		DhcpLease inDb = iaMgr.findDhcpLeaseForInetAddr(InetAddress.getByName("192.168.1.152"));
		assertNotNull(inDb);
		assertEquals("With haupdate=true, haPeerState must match state",
				inDb.getState(), inDb.getHaPeerState());
	}

	@Test
	public void testDhcpLeasesResourcePostNullLeaseReturns400() {
		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.postDhcpLease(null, "false");
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDhcpLeasesResourcePutUpdateLease() throws Exception {
		DhcpLease initialLease = createSampleLease("192.168.1.153");
		iaMgr.insertDhcpLease(initialLease);

		DhcpLeasesResource resource = new DhcpLeasesResource();
		initialLease.setState(IaAddress.AVAILABLE);
		Response response = resource.putDhcpLease("192.168.1.153", initialLease, "false");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		DhcpLease updated = iaMgr.findDhcpLeaseForInetAddr(InetAddress.getByName("192.168.1.153"));
		assertNotNull(updated);
		assertEquals(IaAddress.AVAILABLE, updated.getState());
	}

	@Test
	public void testDhcpLeasesResourcePutNullLeaseReturns400() {
		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.putDhcpLease("192.168.1.153", null, "false");
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDhcpLeasesResourceDeleteExistingLease() throws Exception {
		DhcpLease lease = createSampleLease("192.168.1.154");
		iaMgr.insertDhcpLease(lease);

		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.deleteDhcpLease("192.168.1.154");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		assertNull(iaMgr.findDhcpLeaseForInetAddr(InetAddress.getByName("192.168.1.154")));
	}

	@Test
	public void testDhcpLeasesResourceDeleteNonExistentLeaseReturnsError() {
		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.deleteDhcpLease("192.168.1.199");
		assertNotNull(response);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDhcpLeasesResourceGetRangeLeaseIps() throws Exception {
		iaMgr.insertDhcpLease(createSampleLease("192.168.1.110"));
		iaMgr.insertDhcpLease(createSampleLease("192.168.1.120"));
		iaMgr.insertDhcpLease(createSampleLease("192.168.1.130"));

		DhcpLeasesResource resource = new DhcpLeasesResource();
		Response response = resource.getDhcpLeaseIps("192.168.1.100", "192.168.1.125");
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		String ipsText = (String) response.getEntity();
		assertNotNull(ipsText);
		assertTrue(ipsText.contains("192.168.1.110"));
		assertTrue(ipsText.contains("192.168.1.120"));
		assertFalse(ipsText.contains("192.168.1.130"));
	}

	@Test
	public void testDhcpLeasesPathHelpers() {
		assertEquals("dhcpleases", DhcpLeasesResource.buildPostPath("192.168.1.1"));
		assertEquals("dhcpleases/192.168.1.1", DhcpLeasesResource.buildPutPath("192.168.1.1"));
	}
}
