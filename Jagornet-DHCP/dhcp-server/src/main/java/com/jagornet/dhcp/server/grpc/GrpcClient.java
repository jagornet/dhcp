package com.jagornet.dhcp.server.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseUtil;
import com.jagornet.dhcp.server.util.MtlsConfig;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;
import io.grpc.stub.StreamObserver;

public class GrpcClient {

    private static Logger log = LoggerFactory.getLogger(GrpcClient.class);
	
	private String grpcHost;
	private int grpcPort;
    ManagedChannel channel;
    private HaServiceGrpc.HaServiceStub asyncStub;
    private HaServiceGrpc.HaServiceBlockingStub blockingStub;
    private HaServiceGrpc.HaServiceFutureStub futureStub;
	
	public GrpcClient(String grpcHost, int grpcPort, MtlsConfig mtlsConfig) {

        this.grpcHost = grpcHost;
        this.grpcPort = grpcPort;

        ChannelCredentials channelCreds = 
                TlsChannelCredentials.newBuilder()
                                    .keyManager(mtlsConfig.getKeyManagerFactory().getKeyManagers())
                                    .trustManager(mtlsConfig.getTrustManagerFactory().getTrustManagers())
                                    .build();

        channel = Grpc.newChannelBuilderForAddress(grpcHost, grpcPort, channelCreds)
                        .overrideAuthority("jagornet-server")   // this must be in the server SAN info
                        .build();

        asyncStub = HaServiceGrpc.newStub(channel);
        blockingStub = HaServiceGrpc.newBlockingStub(channel);
        futureStub = HaServiceGrpc.newFutureStub(channel);

	}

    public String getStatus() {
        try {
            StatusResponse response = blockingStub.getStatus(Empty.newBuilder().build());
            return response.getStatus();
        }
        catch (StatusRuntimeException ex) {
            log.error("getStatus failed: " + ex + ": " + ex.getCause());
            return null;
        }
    }

    public String getHaState() {
        try {
            HaStateResponse response = blockingStub.getHaState(Empty.newBuilder().build());
            return response.getHaState();
        }
        catch (StatusRuntimeException ex) {
            log.error("getHaState failed: " + ex + ": " + ex.getCause());
            return null;
        }
    }

    public DhcpLease updateDhcpLease(DhcpLease dhcpLease) {
        try {
            DhcpLeaseUpdate update = blockingStub.updateLease(DhcpLeaseUtil.dhcpLeaseToGrpc(dhcpLease));
            return DhcpLeaseUtil.grpcToDhcpLease(update);
        }
        catch (StatusRuntimeException ex) {
            log.error("updateLease failed: " + ex + ": " + ex.getCause());
            return null;
        }
    }

    public void updateDhcpLeaseAsync(DhcpLease dhcpLease, StreamObserver<DhcpLeaseUpdate> responseObserver) {
        asyncStub.updateLease(DhcpLeaseUtil.dhcpLeaseToGrpc(dhcpLease), responseObserver);
    }

    public void getDhcpLeases(byte[] startIp, byte[] endIp, boolean unsyncedLeasesOnly,
                                StreamObserver<DhcpLeaseUpdate> responseObserver) {
                                    
		DhcpLeasesRequest.Builder builder = DhcpLeasesRequest.newBuilder();
		builder.setStartIpAddress(ByteString.copyFrom(startIp));
		builder.setEndIpAddress(ByteString.copyFrom(endIp));
		builder.setUnsyncedLeasesOnly(unsyncedLeasesOnly);
		DhcpLeasesRequest request = builder.build();
        asyncStub.getLeases(request, responseObserver);
    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    public static void main(String[] args) {
        MtlsConfig mtls = MtlsConfig.getDefaultClientInstance();
        GrpcClient client = new GrpcClient("localhost", GrpcServer.GRPC_SERVER_PORT, mtls);
        String status = client.getStatus();
        System.out.println("Got server status: " + status);
    }
}