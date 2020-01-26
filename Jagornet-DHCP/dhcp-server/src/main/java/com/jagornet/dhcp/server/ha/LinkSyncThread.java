package com.jagornet.dhcp.server.ha;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesResource;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.rest.cli.JerseyRestClient;

public class LinkSyncThread implements Runnable {

	private static Logger log = LoggerFactory.getLogger(LinkSyncThread.class);

	// the DhcpLink to be synced
	private DhcpLink dhcpLink;
	// thread synchronization latch
	private CountDownLatch linkSyncLatch;
	// REST client for communicating to peer
	private JerseyRestClient restClient;
	// REST service for handling requests from peer
	private DhcpLeasesService dhcpLeasesService;
	
	public LinkSyncThread(DhcpLink dhcpLink,
							CountDownLatch linkSyncLatch,
							JerseyRestClient restClient,
							DhcpLeasesService dhcpLeasesService) {
		super();
		this.dhcpLink = dhcpLink;
		this.linkSyncLatch = linkSyncLatch;
		this.restClient = restClient;
		this.dhcpLeasesService = dhcpLeasesService;
	}
	
	@Override
	public void run() {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String linkStartIp = dhcpLink.getSubnet().getSubnetAddress().getHostAddress();
		paramMap.put(DhcpLeasesResource.QUERYPARAM_START, linkStartIp);
		String linkEndIp = dhcpLink.getSubnet().getEndAddress().getHostAddress();
		paramMap.put(DhcpLeasesResource.QUERYPARAM_END, linkEndIp);
		paramMap.put(DhcpLeasesResource.QUERYPARAM_HAUPDATE, Boolean.TRUE.toString());
		// set link syncing state now or in process method?
		dhcpLink.setState(DhcpLink.State.SYNCING);
		log.info("Starting lease sync for link: " + dhcpLink.getLinkAddress());
		Instant start = Instant.now();
		boolean syncOk = false;
//TODO: pick one... JSON or GSON?
		syncOk = processJsonStream(paramMap);
//		syncOk = processGsonStream(paramMap);
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
		InputStream stream = restClient.doGetStream(
				DhcpLeasesResource.PATH + DhcpLeasesResource.JSONLEASESTREAM,
				paramMap);
		try {
			if (stream != null) {
				StringBuilder sb = new StringBuilder();
				int c = stream.read();
				while (c != -1) {
					if ((char)c == '{') {
						sb.append((char)c);
						c = stream.read();
						while ((c != -1) && ((char)c != '}')) {
							sb.append((char)c);
							c = stream.read();
						}
						if ((char)c == '}') {
							sb.append((char)c);
							DhcpLease dhcpLease = DhcpLease.fromJson(sb.toString());
							// mark this lease as 'synced'
							dhcpLease.setHaPeerState(dhcpLease.getState());
							if (dhcpLeasesService.createOrUpdateDhcpLease(dhcpLease)) {
								// now tell the backup server we're in sync
								restClient.doPut(
										DhcpLeasesResource.buildPutPath(
												dhcpLease.getIpAddress().getHostAddress()),
										dhcpLease.toJson());

							}
						}
						sb.setLength(0);
					}
					else if ((char)c != '\n') {
						log.error("Expected '{' or '\n', but found '" + (char)c + "'");
						break;
					}
					c = stream.read();
				}
				return true;
			}
			else {
				return false;
			}
		} 
		catch (Exception ex) {
			log.error("Link sync failure: " + ex);
			return false;
		}
	}
	
	private boolean processGsonStream(Map<String, Object> paramMap) {
		Gson gson = new Gson();
		InputStream stream = restClient.doGetStream(
				DhcpLeasesResource.PATH + DhcpLeasesResource.GSONLEASESTREAM,
				paramMap);
		try {
	        JsonReader reader = new JsonReader(new InputStreamReader(stream));
	        reader.beginArray();
	        while (reader.hasNext()) {
	            DhcpLease dhcpLease = gson.fromJson(reader, DhcpLease.class);
				dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
	        }
	        reader.endArray();
	        reader.close();
	        return true;
		} 
		catch (Exception ex) {
			log.error("Link sync failure: " + ex);
			return false;
		}
	}
}	
