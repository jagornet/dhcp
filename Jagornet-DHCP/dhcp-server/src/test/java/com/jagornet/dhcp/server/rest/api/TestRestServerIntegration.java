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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Base64;
import java.util.Date;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jagornet.dhcp.server.JagornetDhcpServer;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;

import io.netty.channel.Channel;

/**
 * End-to-End Integration Tests for Jagornet DHCP REST Management Server.
 * <p>
 * Boots an ephemeral Netty HTTP server hosting the JAX-RS REST API resources
 * and executes real socket-level HTTP requests using the Jersey client.
 * 
 * @author A. Gregory Rabil
 */
public class TestRestServerIntegration {

	private static final String SAMPLE_CONFIG_XML =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
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
			"        <poolList><range>192.168.1.100-192.168.1.200</range></poolList>\n" +
			"      </v4AddrPools>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

	private static Channel serverChannel;
	private static int port;
	private static URI baseUri;
	private static Client client;
	private static WebTarget target;
	private static File tempConfigFile;
	private static DhcpConfigTestContext context;
	private static InMemoryIaManager iaMgr;

	private static String basicAuthHeader(String user, String pass) {
		String credentials = user + ":" + pass;
		return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
	}

	private static int findFreePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		}
	}

	@BeforeClass
	public static void startServer() throws Exception {
		context = new DhcpConfigTestContext();
		context.initXml(SAMPLE_CONFIG_XML);

		iaMgr = new InMemoryIaManager();
		DhcpServerConfiguration.getInstance().setIaMgr(iaMgr);

		tempConfigFile = File.createTempFile("jagornet-rest-integration-config", ".xml");
		tempConfigFile.deleteOnExit();

		Field filenameField = DhcpServerConfiguration.class.getDeclaredField("serverConfigFilename");
		filenameField.setAccessible(true);
		filenameField.set(null, tempConfigFile.getAbsolutePath());

		DhcpServerConfiguration.saveConfig(DhcpServerConfiguration.getInstance().getJaxbServerConfig(),
				tempConfigFile.getAbsolutePath());

		port = findFreePort();
		baseUri = UriBuilder.fromUri("http://127.0.0.1/").port(port).build();

		ResourceConfig resourceConfig = new ResourceConfig()
				.register(AuthenticationResource.class)
				.register(DhcpLeasesResource.class)
				.register(DhcpLinksResource.class)
				.register(DhcpServerConfigResource.class)
				.register(DhcpServerStatusResource.class)
				.register(MetricsResource.class)
				.register(AuthenticationFilter.class)
				.register(ApplicationExceptionMapper.class)
				.register(new ObjectMapperContextResolver())
				.register(JacksonFeature.class);

		serverChannel = NettyHttpContainerProvider.createServer(baseUri, resourceConfig, false);

		client = ClientBuilder.newBuilder()
				.register(new ObjectMapperContextResolver())
				.register(JacksonFeature.class)
				.build();
		target = client.target(baseUri);
	}

	@AfterClass
	public static void stopServer() throws Exception {
		if (client != null) {
			client.close();
		}
		if (serverChannel != null) {
			serverChannel.close().syncUninterruptibly();
		}
		JagornetDhcpServer.haPrimaryFSM = null;
		JagornetDhcpServer.haBackupFSM = null;
		if (context != null) {
			context.close();
		}
		if (tempConfigFile != null && tempConfigFile.exists()) {
			tempConfigFile.delete();
		}
	}

	@Before
	public void clearDatabase() {
		if (iaMgr != null) {
			iaMgr.clear();
		}
	}

	// =========================================================================
	// 1. Authentication & Security Filter HTTP Tests
	// =========================================================================

	@Test
	public void testUnauthenticatedRequestReturns401WithWwwAuthenticateHeader() {
		Response response = target.path("dhcpserverstatus")
				.request(MediaType.TEXT_PLAIN)
				.get();

		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		String authHeader = response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE);
		assertNotNull(authHeader);
		assertTrue("Must challenge with Basic realm", authHeader.startsWith("Basic realm="));
	}

	@Test
	public void testMalformedAuthorizationHeaderReturns401() {
		Response response = target.path("dhcpserverstatus")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, "Basic %%%bad-base64%%%")
				.get();

		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	public void testAuthenticationEndpointFormCredentialsSuccess() {
		Form form = new Form();
		form.param("username", "jagornet");
		form.param("password", "jagornet");

		Response response = target.path("authentication")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String token = response.readEntity(String.class);
		assertNotNull(token);
		assertEquals("Token should be UUID formatted", 36, token.length());
	}

	@Test
	public void testAuthenticationEndpointFormCredentialsInvalidReturns403() {
		Form form = new Form();
		form.param("username", "baduser");
		form.param("password", "badpass");

		Response response = target.path("authentication")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test
	public void testAuthenticationEndpointJsonCredentialsSuccess() {
		Credentials credentials = new Credentials();
		credentials.setUsername("jagornet");
		credentials.setPassword("jagornet");

		Response response = target.path("authentication")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String token = response.readEntity(String.class);
		assertNotNull(token);
		assertEquals(36, token.length());
	}

	@Test
	public void testAuthenticationEndpointJsonCredentialsInvalidReturns403() {
		Credentials credentials = new Credentials();
		credentials.setUsername("wrong");
		credentials.setPassword("wrong");

		Response response = target.path("authentication")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	// =========================================================================
	// 2. DhcpServerStatusResource HTTP Tests
	// =========================================================================

	@Test
	public void testDhcpServerStatusEndpointWithValidAuth() {
		Response response = target.path("dhcpserverstatus")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String status = response.readEntity(String.class);
		assertEquals("all-systems-go", status);
	}

	@Test
	public void testDhcpServerStatusHaStateNotConfigured() {
		Response response = target.path("dhcpserverstatus").path("hastate")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String haState = response.readEntity(String.class);
		assertEquals("HA not configured", haState);
	}

	// =========================================================================
	// 3. DhcpServerConfigResource HTTP Tests
	// =========================================================================

	@Test
	public void testDhcpServerConfigEndpointGet() {
		Response response = target.path("dhcpserverconfig")
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String jsonConfig = response.readEntity(String.class);
		assertNotNull(jsonConfig);
		assertTrue("Config JSON should contain links", jsonConfig.contains("links"));
	}

	@Test
	public void testDhcpServerConfigGlobalPoliciesGet() {
		Response response = target.path("dhcpserverconfig").path("globalpolicies")
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String jsonPolicies = response.readEntity(String.class);
		assertNotNull(jsonPolicies);
		assertTrue(jsonPolicies.contains("rest.api.username"));
	}

	// =========================================================================
	// 4. DhcpLinksResource HTTP Tests
	// =========================================================================

	@Test
	public void testDhcpLinksEndpointSuccess() {
		Response response = target.path("dhcplinks").path("192.168.1.0-24")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String linkState = response.readEntity(String.class);
		assertEquals(DhcpLink.State.OK.toString(), linkState);
	}

	@Test
	public void testDhcpLinksEndpointNotFoundReturns500() {
		Response response = target.path("dhcplinks").path("10.99.99.0-24")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		String body = response.readEntity(String.class);
		assertTrue(body.contains("Link state not found"));
	}

	@Test
	public void testDhcpLinksEndpointInvalidFormatReturns500() {
		Response response = target.path("dhcplinks").path("invalid_format_link")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		String body = response.readEntity(String.class);
		assertTrue(body.contains("Subnet invalid"));
	}

	// =========================================================================
	// 5. DhcpLeasesResource HTTP CRUD Lifecycle Tests
	// =========================================================================

	@Test
	public void testDhcpLeasesFullCrudLifecycle() throws Exception {
		String testIp = "192.168.1.150";

		// 1. Initial GET: Expect 404 NOT FOUND
		Response getNotFound = target.path("dhcpleases").path(testIp)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getNotFound.getStatus());

		// 2. POST new lease
		DhcpLease lease = new DhcpLease();
		lease.setIpAddress(java.net.InetAddress.getByName(testIp));
		lease.setDuid("00010001aabbccddeeff".getBytes());
		lease.setIatype(IdentityAssoc.V4_TYPE);
		lease.setIaid(1001L);
		lease.setState(IaAddress.LEASED);
		lease.setStartTime(new Date());
		lease.setPreferredEndTime(new Date(System.currentTimeMillis() + 3600000));
		lease.setValidEndTime(new Date(System.currentTimeMillis() + 7200000));

		Response postResp = target.path("dhcpleases")
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.post(Entity.entity(lease, MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.OK.getStatusCode(), postResp.getStatus());
		DhcpLease createdLease = postResp.readEntity(DhcpLease.class);
		assertEquals(testIp, createdLease.getIpAddress().getHostAddress());

		// 3. GET newly created lease: Expect 200 OK
		Response getOk = target.path("dhcpleases").path(testIp)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), getOk.getStatus());
		DhcpLease fetchedLease = getOk.readEntity(DhcpLease.class);
		assertEquals(testIp, fetchedLease.getIpAddress().getHostAddress());
		assertNotNull(fetchedLease.getDuid());

		// 4. PUT update lease (change state to OFFERED)
		fetchedLease.setState(IaAddress.OFFERED);
		Response putResp = target.path("dhcpleases").path(testIp)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.put(Entity.entity(fetchedLease, MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.OK.getStatusCode(), putResp.getStatus());
		DhcpLease updatedLease = putResp.readEntity(DhcpLease.class);
		assertEquals(IaAddress.OFFERED, updatedLease.getState());

		// 5. GET Range Lease IPs
		Response ipsResp = target.path("dhcpleases").path("ips")
				.queryParam("start", "192.168.1.100")
				.queryParam("end", "192.168.1.200")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), ipsResp.getStatus());
		String ipList = ipsResp.readEntity(String.class);
		assertTrue("IP list should contain " + testIp, ipList.contains(testIp));

		// 6. DELETE lease
		Response delResp = target.path("dhcpleases").path(testIp)
				.request()
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.delete();

		assertEquals(Response.Status.OK.getStatusCode(), delResp.getStatus());

		// 7. GET after DELETE: Expect 404 NOT FOUND
		Response getDeleted = target.path("dhcpleases").path(testIp)
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getDeleted.getStatus());
	}

	@Test
	public void testDhcpLeasesPostWithHaUpdateFlag() throws Exception {
		String syncIp = "192.168.1.177";
		DhcpLease syncLease = new DhcpLease();
		syncLease.setIpAddress(java.net.InetAddress.getByName(syncIp));
		syncLease.setDuid("00010001hapeer112233".getBytes());
		syncLease.setIatype(IdentityAssoc.V4_TYPE);
		syncLease.setIaid(2002L);
		syncLease.setState(IaAddress.LEASED);
		syncLease.setStartTime(new Date());
		syncLease.setPreferredEndTime(new Date(System.currentTimeMillis() + 1800000));
		syncLease.setValidEndTime(new Date(System.currentTimeMillis() + 3600000));

		Response postResp = target.path("dhcpleases")
				.queryParam("haupdate", "true")
				.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.post(Entity.entity(syncLease, MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.OK.getStatusCode(), postResp.getStatus());
		DhcpLease created = postResp.readEntity(DhcpLease.class);
		assertEquals(IaAddress.LEASED, created.getHaPeerState());
	}

	// =========================================================================
	// 6. MetricsResource HTTP Tests
	// =========================================================================

	@Test
	public void testMetricsEndpointOverHttp() {
		Response response = target.path("metrics")
				.request(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.AUTHORIZATION, basicAuthHeader("jagornet", "jagornet"))
				.get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		String metrics = response.readEntity(String.class);
		assertNotNull(metrics);
		assertFalse(metrics.isEmpty());
	}
}
