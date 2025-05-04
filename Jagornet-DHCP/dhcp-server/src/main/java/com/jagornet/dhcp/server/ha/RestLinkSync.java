package com.jagornet.dhcp.server.ha;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesResource;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.rest.api.JacksonObjectMapper;
import com.jagornet.dhcp.server.rest.cli.JerseyRestClient;

public class RestLinkSync implements Runnable {

	private static Logger log = LoggerFactory.getLogger(RestLinkSync.class);

	// the DhcpLink to be synced
	private DhcpLink dhcpLink;
	// thread synchronization latch
	private CountDownLatch linkSyncLatch;
	// REST client for communicating to peer
	private JerseyRestClient restClient;
	// service for handling requests from peer
	private DhcpLeasesService dhcpLeasesService;
	private boolean unsyncedLeasesOnly;
	private ObjectMapper objectMapper;
	
	public RestLinkSync(DhcpLink dhcpLink,
							CountDownLatch linkSyncLatch,
							boolean unsyncedLeasesOnly,
							JerseyRestClient restClient) {

		this.dhcpLink = dhcpLink;
		this.linkSyncLatch = linkSyncLatch;
		this.unsyncedLeasesOnly = unsyncedLeasesOnly;
		this.restClient = restClient;

		dhcpLeasesService = new DhcpLeasesService();
		objectMapper = new JacksonObjectMapper().getJsonObjectMapper();
	}
	
	@Override
	public void run() {
		Map<String, Object> paramMap = new HashMap<>();
		String linkStartIp = dhcpLink.getSubnet().getSubnetAddress().getHostAddress();
		paramMap.put(DhcpLeasesResource.QUERYPARAM_START, linkStartIp);
		String linkEndIp = dhcpLink.getSubnet().getEndAddress().getHostAddress();
		paramMap.put(DhcpLeasesResource.QUERYPARAM_END, linkEndIp);
		if (unsyncedLeasesOnly) {
			log.debug("Requesting unsynced leases only");
			paramMap.put(DhcpLeasesResource.QUERYPARAM_HAUPDATE, 
							DhcpLeasesResource.QUERYPARAM_HAUPDATE_UNSYNCED);
		}
		else {
			log.debug("Requesting all leases");
			paramMap.put(DhcpLeasesResource.QUERYPARAM_HAUPDATE, 
							DhcpLeasesResource.QUERYPARAM_HAUPDATE_ALL);
		}
		// set link syncing state now or in process method?
		dhcpLink.setState(DhcpLink.State.SYNCING);
		log.info("Starting lease sync for link: " + dhcpLink.getLinkAddress());
		Instant start = Instant.now();
		boolean syncOk = false;
		syncOk = processJsonStream(paramMap);
		Instant finish = Instant.now();
	    long timeElapsed = Duration.between(start, finish).toMillis();
		if (syncOk) {
			dhcpLink.setState(DhcpLink.State.OK);
			log.info("Completed lease sync for link: " + dhcpLink.getLinkAddress() + 
					 " timeElapsed=" + timeElapsed + "ms");
		}
		else {
			log.error("Failed lease sync for link: " + dhcpLink.getLinkAddress() + 
					  " timeElapsed=" + timeElapsed + "ms");
		}
		linkSyncLatch.countDown();
	}
	
	private boolean processJsonStream(Map<String, Object> paramMap) {
		JsonFactory factory = objectMapper.getFactory();
		String endpoint = DhcpLeasesResource.PATH + DhcpLeasesResource.DHCPLEASESTREAM;
		try (InputStream stream = restClient.doGetStream(endpoint, paramMap);
			 JsonParser parser = factory.createParser(new InputStreamReader(stream))) {
			JsonToken token = parser.nextToken();
			if (JsonToken.START_ARRAY.equals(token)) {
				// Iterate through the objects of the array.
				while (JsonToken.START_OBJECT.equals(parser.nextToken())) {
					DhcpLease dhcpLease = parser.readValueAs(DhcpLease.class);
					// mark this lease as 'synced'
					dhcpLease.setHaPeerState(dhcpLease.getState());
					if (dhcpLeasesService.createOrUpdateDhcpLease(dhcpLease)) {
						// now tell the peer server we're in sync
//							restClient.doPutString(
//									DhcpLeasesResource.buildPutPath(
//											dhcpLease.getIpAddress().getHostAddress()),
//									DhcpLeaseJsonUtil.dhcpLeaseToJson(dhcpLease));
						restClient.doPutDhcpLease(
								DhcpLeasesResource.buildPutPath(
										dhcpLease.getIpAddress().getHostAddress()),
										dhcpLease);

					}
				}
			}
			else {
				log.error("Expected start_array token in JsonStream for link: " + 
							dhcpLink.getLinkAddress());
			}
			return true;
		} 
		catch (Exception ex) {
			log.error("Link sync failure", ex);
			return false;
		}
	}
}	
