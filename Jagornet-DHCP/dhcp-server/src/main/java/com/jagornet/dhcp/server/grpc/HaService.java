package com.jagornet.dhcp.server.grpc;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Empty;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseCallbackHandler;
import com.jagornet.dhcp.server.db.DhcpLeaseUtil;
import com.jagornet.dhcp.server.db.ProcessLeaseException;
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
        // this is the server-side implementation of the HA server, so imitate the
        // behavior of the REST server which has a special haPeer endpoint for getStatus
        StatusResponse response = StatusResponse.newBuilder()
                                                .setStatus(statusService.haPeerGetStatus())
                                                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getHaState(Empty request, StreamObserver<HaStateResponse> responseObserver) {
        // this is the server-side implementation of the HA server, so imitate the
        // behavior of the REST server which has a special haPeer endpoint for getHaState
        HaStateResponse response = HaStateResponse.newBuilder()
                                                .setHaState(statusService.haPeerGetHaState())
                                                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void updateLease(DhcpLeaseUpdate request, StreamObserver<DhcpLeaseUpdate> responseObserver) {
        DhcpLease dhcpLease = DhcpLeaseUtil.grpcToDhcpLease(request);
        log.debug("Update lease: " + dhcpLease);
        // this update is from the HA peer, so set the haPeerState=state
        dhcpLease.setHaPeerState(dhcpLease.getState());
        if (leasesService.createOrUpdateDhcpLease(dhcpLease)) {
            DhcpLeaseUpdate response = DhcpLeaseUtil.dhcpLeaseToGrpc(dhcpLease);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else {
            responseObserver.onError(new IllegalStateException("createOrUpdateDhcpLease failed"));
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
                public void processDhcpLease(DhcpLease dhcpLease) throws ProcessLeaseException {
                    log.debug("Next dhcpLease: " + dhcpLease);
                    DhcpLeaseUpdate leaseUpdate = DhcpLeaseUtil.dhcpLeaseToGrpc(dhcpLease);
                    if (leaseUpdate == null) {
                        throw new ProcessLeaseException("Failed to convert DhcpLease to gRPC DhcpLeaseUpdate");
                    }
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
