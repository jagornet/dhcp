package com.jagornet.dhcp.server.rest;

import java.net.InetAddress;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.rest.util.CertificateUtils;
import com.jagornet.dhcp.server.rest.util.CertificateUtils.CertificateDetails;

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
    
//    public static final String SSL_X509_CERT_CHAIN_FILE_PEM = "jagornetCA.pem";
//    public static final String SSL_PKCS8_KEY_FILE_PEM = "dev.jagornet.com.pk8.pem";
    public static final String SSL_X509_CERT_CHAIN_FILE_PEM = "dhcp.jagornet.com.cert.pem";
    public static final String SSL_PKCS8_KEY_FILE_PEM = "dhcp.jagornet.com.key.pkcs8";
    
	// TODO: rename keystore filename to match client vs server naming convention
	public static final String SERVER_KEYSTORE_FILENAME = "jagornet_dhcp_server.jks";
	public static final String SERVER_KEYSTORE_PASSWORD = "jagornet";
	public static final String CLIENT_KEYSTORE_FILENAME = "jagornet_dhcp_client.jks";
	public static final String CLIENT_KEYSTORE_PASSWORD = "jagornet";

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

    public static Channel startNettyServer(InetAddress httpsAddr, int httpsPort) {
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
/*        	
	        SslContextBuilder sslContextBuilder = SslContextBuilder
	        		.forServer(ClassLoader.getSystemResourceAsStream(SSL_X509_CERT_CHAIN_FILE_PEM),
	        				ClassLoader.getSystemResourceAsStream(SSL_PKCS8_KEY_FILE_PEM));
*/
        	CertificateDetails primaryCertDetails = 
        			CertificateUtils.getCertificateDetails(
        					SERVER_KEYSTORE_FILENAME, SERVER_KEYSTORE_PASSWORD);
	        SslContextBuilder sslContextBuilder = SslContextBuilder
	        		.forServer(primaryCertDetails.getPrivateKey(), 
	        				primaryCertDetails.getX509Certificate());

// off for testing via browser	        sslContextBuilder.trustManager(ClassLoader.getSystemResourceAsStream(SSL_TRUSTED_CERT_CHAIN_FILE_PEM));
	        sslContextBuilder.trustManager(primaryCertDetails.getTrustedCertificate());
// off for testing via browser	        sslContextBuilder.clientAuth(ClientAuth.REQUIRE);
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
