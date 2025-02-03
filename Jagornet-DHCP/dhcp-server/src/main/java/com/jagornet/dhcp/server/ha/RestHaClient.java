package com.jagornet.dhcp.server.ha;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.ws.rs.client.InvocationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesResource;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusResource;
import com.jagornet.dhcp.server.rest.cli.JerseyRestClient;
import com.jagornet.dhcp.server.util.MtlsConfig;

public class RestHaClient implements HaClient {

	private static Logger log = LoggerFactory.getLogger(RestHaClient.class);

	// REST client for communicating to backup
	private JerseyRestClient restClient;
	// service for handling requests from backup
	private DhcpLeasesService dhcpLeasesService;

    public RestHaClient(String haHost, int haPort, MtlsConfig mtlsConfig, String haUsername, String haPassword) throws HaException {

        try {
            restClient = new JerseyRestClient(haHost, haPort, mtlsConfig, haUsername, haPassword);
        }
        catch (Exception ex) {
            throw new HaException("Failed to initialize JerseyRestClient: " + ex);
        }
		
		// service implementation for processing leases synced from backup server
		dhcpLeasesService = new DhcpLeasesService();
    }

    @Override
    public String getStatus() {
        String status = restClient.doGetString(DhcpServerStatusResource.PATH);
        log.debug("getStatus=" + status);
        return status;
    }

    @Override
    public HaPrimaryFSM.State getPrimaryHaState() {
		String primaryHaState = 
				restClient.doGetString(DhcpServerStatusResource.PATH +
										DhcpServerStatusResource.HASTATE);
		log.debug("getPrimaryHaState=" + primaryHaState);
		if (primaryHaState == null) {
			return null;
		}
		return HaPrimaryFSM.State.valueOf(primaryHaState);
    }

    @Override
    public HaBackupFSM.State getBackupHaState() {
		String backupHaState = 
				restClient.doGetString(DhcpServerStatusResource.PATH +
										DhcpServerStatusResource.HASTATE);
        log.debug("getBackupHaState=" + backupHaState);
		if (backupHaState == null) {
			return null;
		}
		return HaBackupFSM.State.valueOf(backupHaState);
    }

    @Override
    public DhcpLease updateDhcpLease(DhcpLease dhcpLease) {
        // this is an HA update, so it will set the
        // haPeerState of the lease before updating
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(DhcpLeasesResource.QUERYPARAM_HAUPDATE, Boolean.TRUE.toString());
        // NOTE: relying on the PUT behavior with no
        // "format" query param, which defaults to JSON
        // PUT for create/update
        DhcpLease responseDhcpLease = restClient.doPutDhcpLease(
                DhcpLeasesResource.buildPutPath(
                        dhcpLease.getIpAddress().getHostAddress()), 
                        dhcpLease, queryParams);
        if (log.isDebugEnabled()) {
            log.debug("updateDhcpLease response: " + responseDhcpLease);
        }
        return responseDhcpLease;
    }

    @Override
    public void updateDhcpLeaseAsync(DhcpLease dhcpLease, DhcpLease expectedDhcpLease) {
        HaDhcpLeaseCallback callback = new HaDhcpLeaseCallback(dhcpLease, expectedDhcpLease);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(DhcpLeasesResource.QUERYPARAM_HAUPDATE, Boolean.TRUE.toString());
        Future<DhcpLease> future = restClient.doPutAsyncDhcpLease(
                DhcpLeasesResource.buildPutPath(
                        dhcpLease.getIpAddress().getHostAddress()),
                        dhcpLease, callback, queryParams);

        // TODO: Something with the future? Or is the callback sufficient?
        // return future;
    }
	
	public class HaDhcpLeaseCallback implements InvocationCallback<DhcpLease> {
		
		private DhcpLease dhcpLease;
		private DhcpLease expectedDhcpLease;
		
		public HaDhcpLeaseCallback(DhcpLease dhcpLease, DhcpLease expectedDhcpLease) {
			this.dhcpLease = dhcpLease;
			this.expectedDhcpLease = expectedDhcpLease;
		}

		@Override
		public void completed(DhcpLease responseDhcpLease) {
            if (log.isDebugEnabled()) {
			    log.debug("HA (async) DhcpLease update completed response: " + responseDhcpLease);
            }
            else if (log.isInfoEnabled()) {
                log.info("HA (async) DhcpLease update completed:"+
                        " IP=" + dhcpLease.getIpAddress().getHostAddress());
            }
			if (expectedDhcpLease.equals(responseDhcpLease)) {
				// if response matches what we sent, then success
				// so update the HA peer state of the lease as synced
				dhcpLease.setHaPeerState(dhcpLease.getState());
				if (!dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease)) {
					log.error("HA (async) peer state update failed");
				}
			}
			else {
                log.warn("HA (async) DhcpLease update does not match:" +
                        System.lineSeparator() +
                        "expected:" + expectedDhcpLease +
                        System.lineSeparator() +
                        "response:" + responseDhcpLease);
				// if the response doesn't match what we sent, then failure
				// so update the HA peer state of the lease as unknown
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//				dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//				dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
			}
			
		}

		@Override
		public void failed(Throwable t) {
			log.error("HA (async) DhcpLease update failed:" +
                      " IP=" + dhcpLease.getIpAddress().getHostAddress() + 
                      " error=" + t);
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//			dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//			dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
		}
	}
    
    @Override
    public Runnable buildLinkSyncThread(DhcpLink dhcpLink, 
                                        CountDownLatch linkSyncLatch, 
                                        boolean unsyncedLeasesOnly) {

        return new RestLinkSync(dhcpLink, linkSyncLatch, unsyncedLeasesOnly, restClient);
    }
}
