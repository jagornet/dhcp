package com.jagornet.dhcp.server.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class JerseyRestServer {

    private static Logger log = LoggerFactory.getLogger(JerseyRestServer.class);

    // 9060 is actually registered with IANA, but relative to zero,
	// J=9, A=0, G=6, then 0 is the first port in the "JAG" range!
	public static final int HTTP_SERVER_PORT = 9060;
	
	// Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:" + HTTP_SERVER_PORT + 
    										 "/jagornetdhcpserver/";
    public static final String API_PACKAGE = "com.jagornet.dhcp.server.rest.api";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in API_PACKAGE
        final ResourceConfig rc = new ResourceConfig().packages(API_PACKAGE);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        log.info("Creating Grizzly HTTP server on: " + BASE_URI);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static Channel startNettyServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in API_PACKAGE
        final ResourceConfig rc = new ResourceConfig().packages(API_PACKAGE);
        
        /*
         * NOTE:
		 * Netty HTTP container does not support deployment on context paths 
		 * other than root path ("/"). Non-root context path is ignored during 
		 * deployment.
         */
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(HTTP_SERVER_PORT).build();
        log.info("Creating Netty HTTP server on: " + baseUri);
        Channel server = NettyHttpContainerProvider.createServer(baseUri, rc, false);
        return server;
    }
}
