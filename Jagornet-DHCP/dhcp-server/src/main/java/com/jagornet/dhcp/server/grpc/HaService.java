package com.jagornet.dhcp.server.grpc;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Empty;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseCallbackHandler;
import com.jagornet.dhcp.server.db.DhcpLeaseUtil;
import com.jagornet.dhcp.server.grpc.HaServiceGrpc.HaServiceImplBase;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusService;

import io.grpc.stub.StreamObserver;

public class HaService extends HaServiceImplBase {

    private static Logger log = LoggerFactory.getLogger(HaService.class);

	private DhcpServerStatusService statusService;
    private DhcpLeasesService leasesService;
	
	public HaService() {
		statusService = new DhcpServerStatusService();
        leasesService = new DhcpLeasesService();
	}

    @Override
    public void getStatus(Empty request, StreamObserver<StatusResponse> responseObserver) {
        StatusResponse response = StatusResponse.newBuilder()
                                                .setStatus(statusService.getStatus())
                                                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getHaState(Empty request, StreamObserver<HaStateResponse> responseObserver) {
        HaStateResponse response = HaStateResponse.newBuilder()
                                                .setHaState(statusService.getHaState())
                                                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void updateLease(DhcpLeaseUpdate request, StreamObserver<DhcpLeaseUpdate> responseObserver) {
        DhcpLease lease = DhcpLeaseUtil.grpcToDhcpLease(request);
        log.debug("Update lease: " + lease);
        // ?? leasesService.createOrUpdateDhcpLease(lease);
        if (leasesService.updateDhcpLease(lease.getIpAddress(), lease)) {
            // if the update succeeded, then return the request lease
            responseObserver.onNext(request);
            responseObserver.onCompleted();
        }
        else {
            responseObserver.onError(new IllegalStateException("updateLease failed"));
        }
    }

    @Override
    public void getLeases(DhcpLeasesRequest request, StreamObserver<DhcpLeaseUpdate> responseObserver) {
        try {
            InetAddress startIp = InetAddress.getByAddress(request.getStartIpAddress().toByteArray());
            InetAddress endIp = InetAddress.getByAddress(request.getEndIpAddress().toByteArray());
            boolean unsyncedLeasesOnly = request.getUnsyncedLeasesOnly();
            log.info("Getting leases for range:"
                        + " start=" + startIp.getHostAddress()
                        + " end=" + endIp.getHostAddress()
                        + " unsyncedLeasesOnly=" + unsyncedLeasesOnly);
            leasesService.getRangeLeases(startIp, endIp, new DhcpLeaseCallbackHandler() {
                @Override
                public void processDhcpLease(DhcpLease dhcpLease) throws Exception {
                    log.debug("Next dhcpLease: " + dhcpLease);
                    DhcpLeaseUpdate leaseUpdate = DhcpLeaseUtil.dhcpLeaseToGrpc(dhcpLease);
                    responseObserver.onNext(leaseUpdate);
                }
            }, unsyncedLeasesOnly);
            // are we really done here, or does this continue asychronously?
            // TODO: no, it doesn't continue aysync, so we have to wait...
            log.info("Completed getting leases for range:"
                        + " start=" + startIp.getHostAddress()
                        + " end=" + endIp.getHostAddress()
                        + " unsyncedLeasesOnly=" + unsyncedLeasesOnly);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
