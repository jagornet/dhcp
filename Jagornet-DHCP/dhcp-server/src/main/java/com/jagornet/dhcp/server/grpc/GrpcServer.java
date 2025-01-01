package com.jagornet.dhcp.server.grpc;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.util.MtlsConfig;

import io.grpc.Server;
import io.grpc.ServerCredentials;
import io.grpc.TlsServerCredentials;
import io.grpc.netty.NettyServerBuilder;

public class GrpcServer {

    private static Logger log = LoggerFactory.getLogger(GrpcServer.class);

    // 9067: J=9, A=0, G=6, g(RPC)=6 (letter encoding relative zero)
	public static final int GRPC_SERVER_PORT = 9066;

	// address to listen for requests on
	private InetAddress grpcAddr;
	// port to listen for requests on
	private int grpcPort;
	// the mTLS config for this server
	private MtlsConfig mtlsConfig;

    private Server server;
        
    public GrpcServer(InetAddress grpcAddr, int grpcPort, MtlsConfig mtlsConfig) {
		this.grpcAddr = grpcAddr;
		this.grpcPort = grpcPort;
		this.mtlsConfig = mtlsConfig;
    }

    public Server startNettyServer() {
        try {
            InetSocketAddress socketAddr = new InetSocketAddress(grpcAddr, grpcPort);
            
            TlsServerCredentials.Builder mtlsBuilder = TlsServerCredentials.newBuilder();
            mtlsBuilder.keyManager(mtlsConfig.getKeyManagerFactory().getKeyManagers());
            mtlsBuilder.trustManager(mtlsConfig.getTrustManagerFactory().getTrustManagers());
            ServerCredentials serverCreds = mtlsBuilder.build();

            //
            // Use the NettyServerBuilder to bind to a specific address. Oddly, the Netty
            // JAX-RS Jersey NettyHttpContainerProvider only supports the wildcard address.
            // Note that using the NettyServerBuilder is only available if NOT using the
            // grpc-netty-shaded artifact dependency in the dhcp-server pom.xml
            // see https://github.com/grpc/grpc-java/blob/master/SECURITY.md
            // 
            // Server server = Grpc.newServerBuilderForPort(grpcPort, serverCreds)
            //                                     .addService(new HaService())
            //                                     .build()
            //                                     .start();

            log.debug("Creating Netty gRPC server on: " +  socketAddr);
            Server server = NettyServerBuilder.forAddress(socketAddr, serverCreds)
                                                .addService(new HaService())
                                                .build()
                                                .start();
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    shutdown();
                }
            });
            return server;
        }
        catch (Exception ex) {
        	log.error("Failed to start Netty gRPC Server: " + ex);
        	return null;
        }
    }
 
    public void shutdown() {
        if (server != null) {
            log.info("Server shutting down...");
            server.shutdown();
            log.info("Server shutdown complete.");
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
            this.shutdown();
        }
    }

    // NOTE: must remove leasesService from HaService for this test to run
    public static void main(String[] args) {
        MtlsConfig mtls = MtlsConfig.getDefaultServerInstance();
        try {
            GrpcServer grpcServer = new GrpcServer(InetAddress.getLocalHost(), GRPC_SERVER_PORT, mtls);
            grpcServer.startNettyServer();
            grpcServer.blockUntilShutdown();
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}