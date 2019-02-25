package com.jagornet.dhcp.server.rest.cli;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.Future;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.rest.JerseyRestServer;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesResource;

public class JerseyRestClient {

	private static Logger log = LoggerFactory.getLogger(JerseyRestClient.class);

	private String host;
	private int port;
	private Client client;
	private WebTarget apiRootTarget;
	
	public JerseyRestClient(String host, int port) 
			throws GeneralSecurityException, IOException {
		this(host, port,
				JerseyRestServer.CLIENT_KEYSTORE_FILENAME,
				JerseyRestServer.CLIENT_KEYSTORE_PASSWORD);
	}
	
	public JerseyRestClient(String host, int port,
			String keyStoreFilename, String keyStorePassword) 
					throws GeneralSecurityException, IOException {
		
		this.host = host;
		this.port = port;

		InputStream keyStoreInputStream = null;
		try {
			keyStoreInputStream = ClassLoader.getSystemResourceAsStream(keyStoreFilename);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore ts = KeyStore.getInstance("JKS");
			ts.load(keyStoreInputStream, keyStorePassword.toCharArray());
			tmf.init(ts);
	
			// ts.load above closes the keystore input stream, so open it again
			keyStoreInputStream = ClassLoader.getSystemResourceAsStream(keyStoreFilename);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(keyStoreInputStream, keyStorePassword.toCharArray());
			kmf.init(ks, keyStorePassword.toCharArray());
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

//			client = ClientBuilder.newBuilder()
//					.sslContext(ctx)
//					.hostnameVerifier(new NoopHostnameVerifier())
//					.connectTimeout(500, TimeUnit.MILLISECONDS)
//					.readTimeout(2000, TimeUnit.MILLISECONDS)
//					.build();

			/*
			 * Using a pooling connection manager seems like a good idea
			 * on the surface, but in practice it does not behave as
			 * expected, and updates hang after DefaultMaxPerRoute(20)
			 * requests.  Must be some way to re-use the connections...
			 * 
			*/
			ClientConfig clientConfig = new ClientConfig();
		    // values are in milliseconds
		    clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 500);
		    clientConfig.property(ClientProperties.READ_TIMEOUT, 2000);
		    
			SSLConnectionSocketFactory sslConnSocketFactory =
					new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier());
		    Registry<ConnectionSocketFactory> socketFactoryRegistry =
		    		RegistryBuilder.<ConnectionSocketFactory>create()
		    		.register("https", sslConnSocketFactory)
		    		.build();
		    PoolingHttpClientConnectionManager connectionManager = 
		    		new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		    connectionManager.setMaxTotal(100);
		    connectionManager.setDefaultMaxPerRoute(20);
		    //connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost")), 40);
		    
		    clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
		    clientConfig.connectorProvider(new ApacheConnectorProvider());

			client = ClientBuilder.newBuilder()
					.withConfig(clientConfig)
					.build();
		}
		finally {
			if (keyStoreInputStream != null) {
				try { keyStoreInputStream.close(); } catch(IOException ex)  { }
			}
		}
	}
	
	private WebTarget getApiRootTarget() {
		if (apiRootTarget == null) {
			apiRootTarget = client.target("https://" + host + ":" + port);
		}
		return apiRootTarget;
	}
	
	public String doGet(String apiMethod) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		try {
			// invoke HTTP GET synchronously, and read the response data
			// TODO - consider using Response object to check status=2xx first
			log.debug("Invoking sync get on: " + method.getUri());
			String response = invocationBuilder.get(String.class);
			log.debug("Response: " + response);
			return response;
		}
		catch (Exception ex) {
			log.error("Failed to process sync get: " + ex);
			return null;
		}
	}
	
	public Future<String> doGetAsync(String apiMethod,
									 InvocationCallback<String> callback) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
        Future<String> entityFuture = invocationBuilder.async().get(callback);
        return entityFuture;
	}
	
	public String doPut(String apiMethod, String json) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept application/json response data as String.class below?
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		try {
			log.debug("Invoking sync put on: " + method.getUri());
			String response = invocationBuilder.put(
					Entity.entity(json, MediaType.APPLICATION_JSON), String.class);
			log.debug("Response: " + response);
			return response;
		}
		catch (Exception ex) {
			log.error("Failed to process sync put: " + ex);
			return null;
		}
	}
	
	public Future<String> doPutAsync(String apiMethod, String json, 
									 InvocationCallback<String> callback) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
        Future<String> entityFuture = invocationBuilder.async().put(
				Entity.entity(json, MediaType.APPLICATION_JSON), callback);
        return entityFuture;
	}
	
	public String doPost(String apiMethod, String json) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept application/json response data as String.class below?
		Invocation.Builder invocationBuilder = method.request();
		try {
			log.debug("Invoking sync post on: " + method.getUri());
//			String response = invocationBuilder.post(
//					Entity.entity(json, MediaType.APPLICATION_JSON), String.class);
//			Response response = invocationBuilder.post(
//					Entity.entity(json, MediaType.APPLICATION_JSON));
			Response response = invocationBuilder.post(
					Entity.entity(json, MediaType.TEXT_PLAIN));
			log.debug("Response: " + response);
			String data = response.getEntity().toString();
			response.close();
			return data;
		}
		catch (Exception ex) {
			log.error("Failed to process sync post: ", ex);
			return null;
		}
	}
	
	/*
	public Future<String> doPostAsync(String apiMethod, String json, 
									  InvocationCallback<String> callback) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
			log.debug("Invoking async post on: " + method.getUri());
        Future<String> entityFuture = invocationBuilder.async().post(
				Entity.entity(json, MediaType.APPLICATION_JSON), callback);
        return entityFuture;
	}
	*/
	
	public Future<Response> doPostAsync(String apiMethod, String json, 
									  InvocationCallback<Response> callback) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		log.debug("Invoking async post on: " + method.getUri());
        Future<Response> entityFuture = invocationBuilder.async().post(
				Entity.entity(json, MediaType.TEXT_PLAIN), callback);
        return entityFuture;
	}

	public String doDelete(String apiMethod) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		try {
			log.debug("Invoking sync delete on: " + method.getUri());
			// invoke HTTP DELETE synchronously, and read the response data
			// TODO - consider using Response object to check status=2xx first
			String response = invocationBuilder.delete(String.class);
			log.debug("Response: " + response);
			return response;
		}
		catch (Exception ex) {
			log.error("Failed to process sync delete: " + ex);
			return null;
		}
	}
	
	public Future<String> doDeleteAsync(String apiMethod,
										InvocationCallback<String> callback) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
        Future<String> entityFuture = invocationBuilder.async().delete(callback);
        return entityFuture;
	}
	
	public static void main(String[] args) {
		String host = "localhost";
		int port = Integer.valueOf(DhcpServerPolicies.Property.HA_PEER_PORT.value());
		String ip = "all";
		
		if ((args != null) && (args.length > 0)) {
			for (int i=0; i<args.length; i++) {
				if (args[i].equals("-h")) {
					host = args[++i];
				}
				if (args[i].equals("-p")) {
					port = Integer.valueOf(args[++i]);
				}
				if (args[i].equals("-i")) {
					ip = args[++i];
				}
			}
		}
		
		try {
			JerseyRestClient client = new JerseyRestClient(host, port);
			String path = DhcpLeasesResource.PATH;
			if (!ip.equals("all")) {
				path = path + "/" + ip;
			}
			String ips = client.doGet(path);
			System.out.println(ips);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
