package com.jagornet.dhcp.server.rest;

import java.net.InetAddress;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.util.MtlsConfig;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class JerseyRestServer {

    private static Logger log = LoggerFactory.getLogger(JerseyRestServer.class);

    // 9067: J=9, A=0, G=6, H(ttps)=7 (letter encoding relative zero)
	public static final int HTTPS_SERVER_PORT = 9067;
	
	// Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "https://localhost:" + HTTPS_SERVER_PORT + 
    										 "/jagornetdhcpserver/";
    public static final String API_PACKAGE = "com.jagornet.dhcp.server.rest.api";
 
	// address to listen for requests on
	private InetAddress httpsAddr;
	// port to listen for requests on
	private int httpsPort;
	// the mTLS config for this server
	private MtlsConfig mtlsConfig;

	public JerseyRestServer(InetAddress httpsAddr, int httpsPort, MtlsConfig mtlsConfig) {
		this.httpsAddr = httpsAddr;
		this.httpsPort = httpsPort;
		this.mtlsConfig = mtlsConfig;
	}

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
//    public static HttpServer startGrizzlyServer() {
//        // create a resource config that scans for JAX-RS resources and providers
//        // in API_PACKAGE
//        final ResourceConfig rc = new ResourceConfig().packages(API_PACKAGE);
//
//        // create and start a new instance of grizzly http server
//        // exposing the Jersey application at BASE_URI
//        log.info("Creating Grizzly HTTP server on: " + BASE_URI);
//        //return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
//        return null;
//    }

    public Channel startNettyServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in API_PACKAGE
        final ResourceConfig resourceConfig = new ResourceConfig().packages(API_PACKAGE);
        
        /*
         * NOTE:
		 * Netty HTTP container does not support deployment on context paths 
		 * other than root path ("/"). Non-root context path is ignored during 
		 * deployment.
         */
		// NOTE: Jersey NettyHttpContainerProvider does not support 
        // binding to a specific address, so always binds to wildcard
		// see: https://github.com/eclipse-ee4j/jersey/issues/4045
        URI baseUri = UriBuilder.fromUri("https://" + httpsAddr.getHostAddress() + "/")
//        						.host(httpsAddr.getHostAddress())
        						.port(httpsPort)
        						.build();
        log.info("Creating Netty HTTPS server on: " + baseUri);
        try {
	        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(mtlsConfig.getKeyManagerFactory());
	        sslContextBuilder.trustManager(mtlsConfig.getTrustManagerFactory());
			// the REST API requires username/password authentication,
			// so we don't want to validate the client certificate
			// sslContextBuilder.clientAuth(ClientAuth.REQUIRE);
	        SslContext sslContext = sslContextBuilder.build();
	        Channel server = NettyHttpContainerProvider
	        		.createServer(baseUri, resourceConfig, sslContext, false);
	        return server;
        }
        catch (Exception ex) {
        	log.error("Failed to start Netty HTTP Server: " + ex);
        	return null;
        }
    }
}
