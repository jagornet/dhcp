package com.jagornet.dhcp.server.ha;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseUtil;
import com.jagornet.dhcp.server.grpc.DhcpLeaseUpdate;
import com.jagornet.dhcp.server.grpc.GrpcClient;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.util.MtlsConfig;

import io.grpc.stub.StreamObserver;

public class GrpcHaClient implements HaClient {

	private static Logger log = LoggerFactory.getLogger(GrpcHaClient.class);

	// gRPC client for communicating to backup
    private GrpcClient grpcClient;
	// service for handling requests from backup
	private DhcpLeasesService dhcpLeasesService;

    public GrpcHaClient(String haHost, int haPort, MtlsConfig mtlsConfig) throws HaException {

        try {
            grpcClient = new GrpcClient(haHost, haPort, mtlsConfig);
        }
        catch (Exception ex) {
            throw new HaException("Failed to initialize GrpcClient: " + ex);
        }
		
		// service implementation for processing leases synced from backup server
		dhcpLeasesService = new DhcpLeasesService();
    }

    @Override
    public String getStatus() {
        String status = grpcClient.getStatus();
        log.debug("getStatus=" + status);
        return status;
    }

    @Override
    public HaPrimaryFSM.State getPrimaryHaState() {
		String primaryHaState = grpcClient.getHaState();
		if (primaryHaState == null) {
			log.info("Null response from HA Primary server");
			return null;
		}
		log.info("HA Primary state: " + primaryHaState);
		return HaPrimaryFSM.State.valueOf(primaryHaState);
    }

    @Override
    public HaBackupFSM.State getBackupHaState() {
		String backupHaState = grpcClient.getHaState();
		if (backupHaState == null) {
			log.info("Null response from HA Backup server");
			return null;
		}
		log.info("HA Backup state: " + backupHaState);
		return HaBackupFSM.State.valueOf(backupHaState);
    }

    @Override
    public DhcpLease updateDhcpLease(DhcpLease dhcpLease) {
        DhcpLease responseDhcpLease = grpcClient.updateDhcpLease(dhcpLease);
        log.info("Binding update response: " + responseDhcpLease);
        return responseDhcpLease;
    }

    @Override
    public void updateDhcpLeaseAsync(DhcpLease dhcpLease, DhcpLease expectedDhcpLease) {
        HaDhcpLeaseUpdateObserver observer = new HaDhcpLeaseUpdateObserver(dhcpLease, expectedDhcpLease);
        grpcClient.updateDhcpLeaseAsync(expectedDhcpLease, observer);
    }
	
	public class HaDhcpLeaseUpdateObserver implements StreamObserver<DhcpLeaseUpdate> {
		
		private DhcpLease dhcpLease;
		private DhcpLease expectedDhcpLease;
		
		public HaDhcpLeaseUpdateObserver(DhcpLease dhcpLease, DhcpLease expectedDhcpLease) {
			this.dhcpLease = dhcpLease;
			this.expectedDhcpLease = expectedDhcpLease;
		}

        @Override
        public void onNext(DhcpLeaseUpdate value) {
			log.info("DhcpLease update onNext for: " + dhcpLease);
            DhcpLease responseDhcpLease = DhcpLeaseUtil.grpcToDhcpLease(value);
			if (expectedDhcpLease.equals(responseDhcpLease)) {
				// if response matches what we sent, then success
				// so update the HA peer state of the lease as synced
				dhcpLease.setHaPeerState(dhcpLease.getState());
				if (dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease)) {
					log.info("HA peer state updated successfully");
				}
				else {
					log.error("HA peer state update failed");
				}
			}
			else {
				log.warn("Response (async) does not match posted DhcpLease");
				// if the response doesn't match what we sent, then failure
				// so update the HA peer state of the lease as unknown
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//				dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//				dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
			}
        }

        @Override
        public void onError(Throwable t) {
			log.error("DhcpLease update onError for: " + dhcpLease + ": " + t);
        }

        @Override
        public void onCompleted() {
            // TODO Auto-generated method stub
            // throw new UnsupportedOperationException("Unimplemented method 'onCompleted'");
			log.debug("DhcpLease update onCompleted for: " + dhcpLease);
        }
	}
    
    @Override
    public Runnable buildLinkSyncThread(DhcpLink dhcpLink, 
                                                CountDownLatch linkSyncLatch, 
                                                boolean unsyncedLeasesOnly) {

        return new GrpcLinkSync(dhcpLink, linkSyncLatch, unsyncedLeasesOnly, grpcClient);
    }
}
