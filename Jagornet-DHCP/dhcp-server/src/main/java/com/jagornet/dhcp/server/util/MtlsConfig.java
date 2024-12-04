package com.jagornet.dhcp.server.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.DhcpConstants;

public class MtlsConfig {

    private static Logger log = LoggerFactory.getLogger(MtlsConfig.class);

    // public static final String CLIENT_KEYSTORE_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME + "/config/jagornet_dhcp_client.jks";
	// public static final String CLIENT_KEYSTORE_PASSWORD = "jagornet";
    // public static final String CLIENT_TRUSTSTORE_FILENAME = CLIENT_KEYSTORE_FILENAME;
    // public static final String CLIENT_TRUSTSTORE_PASSWORD = CLIENT_KEYSTORE_PASSWORD;

    public static final String CLIENT_KEYSTORE_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME + "/config/jagornet_client_keystore.p12";
	public static final String CLIENT_KEYSTORE_PASSWORD = "jagornet";
    public static final String CLIENT_TRUSTSTORE_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME + "/config/jagornet_client_truststore.p12";
    public static final String CLIENT_TRUSTSTORE_PASSWORD = "jagornet";

    // public static final String SERVER_KEYSTORE_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME + "/config/jagornet_dhcp_server.jks";
	// public static final String SERVER_KEYSTORE_PASSWORD = "jagornet";
    // public static final String SERVER_TRUSTSTORE_FILENAME = SERVER_KEYSTORE_FILENAME;
    // public static final String SERVER_TRUSTSTORE_PASSWORD = SERVER_KEYSTORE_PASSWORD;

    public static final String SERVER_KEYSTORE_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME + "/config/jagornet_server_keystore.p12";
	public static final String SERVER_KEYSTORE_PASSWORD = "jagornet";
    public static final String SERVER_TRUSTSTORE_FILENAME = DhcpConstants.JAGORNET_DHCP_HOME + "/config/jagornet_server_truststore.p12";
    public static final String SERVER_TRUSTSTORE_PASSWORD = "jagornet";

    private static MtlsConfig defaultClientInstance;
    private static MtlsConfig defaultServerInstance;

    private String keystoreFilename;
    private String keystorePassword;
    private String truststoreFilename;
    private String truststorePassword;
    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    private SSLContext sslContext;

    public static MtlsConfig getDefaultClientInstance() {
        if (defaultClientInstance == null) {
            defaultClientInstance = new MtlsConfig(CLIENT_KEYSTORE_FILENAME, CLIENT_KEYSTORE_PASSWORD,
                                                    CLIENT_TRUSTSTORE_FILENAME, CLIENT_TRUSTSTORE_PASSWORD);
        }
        return defaultClientInstance;
    }

    public static MtlsConfig getDefaultServerInstance() {
        if (defaultServerInstance == null) {
            defaultServerInstance = new MtlsConfig(SERVER_KEYSTORE_FILENAME, SERVER_KEYSTORE_PASSWORD,
                                                    SERVER_TRUSTSTORE_FILENAME, SERVER_TRUSTSTORE_PASSWORD);
        }
        return defaultServerInstance;
    }
    public MtlsConfig(String keystoreFilename, String keystorePassword,
                      String truststoreFilename, String truststorePassword) {

        this.keystoreFilename = keystoreFilename;
        this.keystorePassword = keystorePassword;
        this.truststoreFilename = truststoreFilename;
        this.truststorePassword = truststorePassword;

        buildSslContext();
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public KeyManagerFactory getKeyManagerFactory() {
        return keyManagerFactory;
    }

    public TrustManagerFactory getTrustManagerFactory() {
        return trustManagerFactory;
    }
    
    protected void buildSslContext() {
        keyManagerFactory = buildKeyManagerFactory(keystoreFilename, keystorePassword);
        trustManagerFactory = buildTrustManagerFactory(truststoreFilename, truststorePassword);
        sslContext = buildSSLContext(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers());
    }

	public static SSLContext buildSSLContext(KeyManager[] km, TrustManager[] tm) {
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(km, tm, null);
		}
		catch (Exception ex) {
			log.error("Failed to create SSLContext", ex);
            throw new IllegalStateException("Failed to create SSLContext: " + ex);
		}
		return ctx;
	}

	public static KeyManagerFactory buildKeyManagerFactory(String ksFilename, String ksPassword) {
        KeyManagerFactory kmf = null;
        InputStream stream = null;
        try {
	        log.debug("Loading keystore: " + ksFilename);

            KeyStore ks = KeyStore.getInstance("PKCS12");
            // stream = MtlsConfig.class.getResourceAsStream(ksFilename);
            stream = new FileInputStream(ksFilename);
            ks.load(stream, ksPassword.toCharArray());

            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, ksPassword.toCharArray());
        }
		catch (Exception ex) {
			log.error("Failed to create KeyManagerFactory", ex);
            throw new IllegalStateException("Failed to create KeyManagerFactory: " + ex);
		}
        finally {
        	if (stream != null) {
        		try { stream.close(); } catch (IOException ex) { }
        	}
        }
		return kmf;
	}
	
	public static TrustManagerFactory buildTrustManagerFactory(String tsFilename, String tsPassword) {
		TrustManagerFactory tmf = null;
        InputStream stream = null;
        try {
	        log.debug("Loading truststore: " + tsFilename);

            KeyStore ts = KeyStore.getInstance("PKCS12");
            // stream = MtlsConfig.class.getResourceAsStream(tsFilename);
            stream = new FileInputStream(tsFilename);
            ts.load(stream, tsPassword.toCharArray());

            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
        }
		catch (Exception ex) {
			log.error("Failed to create TrustManagerFactory", ex);
            throw new IllegalStateException("Failed to create TrustManagerFactory: " + ex);
		}
        finally {
        	if (stream != null) {
        		try { stream.close(); } catch (IOException ex) { }
        	}
        }
		return tmf;
	}

}
