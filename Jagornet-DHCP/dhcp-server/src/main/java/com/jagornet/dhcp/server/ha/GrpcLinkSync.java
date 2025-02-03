package com.jagornet.dhcp.server.ha;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseUtil;
import com.jagornet.dhcp.server.grpc.DhcpLeaseUpdate;
import com.jagornet.dhcp.server.grpc.GrpcClient;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;

import io.grpc.stub.StreamObserver;

public class GrpcLinkSync implements Runnable {

	private static Logger log = LoggerFactory.getLogger(GrpcLinkSync.class);

	// the DhcpLink to be synced
	private DhcpLink dhcpLink;
	// thread synchronization latch
	private CountDownLatch linkSyncLatch;
	// gRPC client for communicating to peer
	private GrpcClient grpcClient;
	private boolean unsyncedLeasesOnly;
	// service for handling requests from peer
	private DhcpLeasesService dhcpLeasesService;
	private Instant startInstant;

	public GrpcLinkSync(DhcpLink dhcpLink,
							CountDownLatch linkSyncLatch,
							boolean unsyncedLeasesOnly,
							GrpcClient grpcClient) {

		this.dhcpLink = dhcpLink;
		this.linkSyncLatch = linkSyncLatch;
		this.unsyncedLeasesOnly = unsyncedLeasesOnly;
		this.grpcClient = grpcClient;

		dhcpLeasesService = new DhcpLeasesService();
	}
	
	@Override
	public void run() {
		byte[] linkStartIp = dhcpLink.getSubnet().getSubnetAddress().getAddress();
		byte[] linkEndIp = dhcpLink.getSubnet().getEndAddress().getAddress();
		// set link syncing state now or in process method?
		dhcpLink.setState(DhcpLink.State.SYNCING);
		log.info("Starting lease sync for link: " + dhcpLink.getLinkAddress());
		startInstant = Instant.now();
		HaDhcpLeaseUpdateObserver observer = new HaDhcpLeaseUpdateObserver();
		grpcClient.getDhcpLeases(linkStartIp, linkEndIp, unsyncedLeasesOnly, observer);
	}
	
	public class HaDhcpLeaseUpdateObserver implements StreamObserver<DhcpLeaseUpdate> {
		
        @Override
        public void onNext(DhcpLeaseUpdate value) {
			if (log.isDebugEnabled()) {
				log.debug("DhcpLease update onNext value: " + value);
			}
			else if (log.isInfoEnabled()) {
				log.info("DhcpLase update onNext: IP= " + value.getIpAddress());
			}
            DhcpLease dhcpLease = DhcpLeaseUtil.grpcToDhcpLease(value);
			// mark this lease as 'synced'
			dhcpLease.setHaPeerState(dhcpLease.getState());
			if (dhcpLeasesService.createOrUpdateDhcpLease(dhcpLease)) {
				// now tell the peer server we're in sync
				grpcClient.updateDhcpLease(dhcpLease);
			}
			else {
				// should be caught by onError anyway
				log.warn("DhcpLease update failed");
			}
        }

        @Override
        public void onError(Throwable t) {
			log.error("DhcpLease update onError: " + t);
			if (t.getCause() != null) {
				log.error("Cause: " + t.getCause());
			}
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(startInstant, finish).toMillis();
			log.error("Failed lease sync for link: " + dhcpLink.getLinkAddress() + 
						" timeElapsed=" + timeElapsed + "ms");
			linkSyncLatch.countDown();
		}

        @Override
        public void onCompleted() {
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(startInstant, finish).toMillis();
			dhcpLink.setState(DhcpLink.State.OK);
			log.info("Completed lease sync for link: " + dhcpLink.getLinkAddress() + 
						" timeElapsed=" + timeElapsed + "ms");
			linkSyncLatch.countDown();
		}
	}
}	
