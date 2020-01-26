package com.jagornet.dhcp.server.rest.cli;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
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

import com.google.gson.Gson;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.rest.JerseyRestServer;

public class JerseyRestClient {

	private static Logger log = LoggerFactory.getLogger(JerseyRestClient.class);

	private static Gson gson = new Gson();
	
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
		    
		    // TODO: consider two policies or rename current policy?
			int timeout = DhcpServerPolicies.globalPolicyAsInt(
										Property.HA_POLL_REPLY_TIMEOUT);
			clientConfig.property(ClientProperties.CONNECT_TIMEOUT, timeout);
			clientConfig.property(ClientProperties.READ_TIMEOUT, timeout);

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

	private WebTarget buildWebTarget(String apiMethod, Map<String, Object> queryParams) {
		WebTarget api = getApiRootTarget();
		WebTarget method = api.path(apiMethod);
		if (queryParams != null) {
			for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
				if ((entry.getKey() != null) && (entry.getValue() != null)) {
					method = method.queryParam(entry.getKey(), entry.getValue());
				}
			}
		}
		return method;
	}
	
	public String doGet(String apiMethod) {
		return this.doGet(apiMethod, null);
	}
	
	public String doGet(String apiMethod, Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		// accept text/plain response data
		Invocation.Builder builder = method.request(MediaType.TEXT_PLAIN);
		log.debug("Invoking sync get on: " + method.getUri());
		try {
			// keep it generic here by using primitives
			// let the caller handle the data marshaling
			return builder.get(String.class);
		}
		catch (Exception ex) {
			log.error(apiMethod + " sync get failed: " + ex);
			return null;
		}
	}
	
/*
 * Just use doGet methods instead of doGetString and doGetDhcpLease
 * 
	public String doGetString(String apiMethod) {
		return this.doGetString(apiMethod, null);
	}
	
	public String doGetString(String apiMethod, String format) {
		try {
			// invoke HTTP GET synchronously, and read the response string
			// get will throw exception if response is not a string
			log.debug("Invoking sync get String on: " + apiMethod +
					  " format=" + format);
			String response;
			if (format != null) {
				response = getApiRootTarget()
							.path(apiMethod)
							.queryParam("format", format)
							.request(MediaType.TEXT_PLAIN)
							.get(String.class);
			}
			else { 
				response = getApiRootTarget()
							.path(apiMethod)
							.request(MediaType.TEXT_PLAIN)
							.get(String.class);
			}				
			log.debug("Response: " + response);
			return response;
		}
		catch (Exception ex) {
			log.error("Failed to process sync get String: " + ex);
			return null;
		}
	}
	
	public DhcpLease doGetDhcpLease(String apiMethod) {
		return this.doGetDhcpLease(apiMethod, null);
	}
	
	public DhcpLease doGetDhcpLease(String apiMethod, String format) {
		try {
			// invoke HTTP GET synchronously, and read the response data
			// get will throw exception if response is not a DhcpLease
			log.debug("Invoking sync get DhcpLease on: " + apiMethod +
					  " format=" + format);
			DhcpLease dhcpLease = null;
			if ((format != null) && format.equalsIgnoreCase("json")) {
				Response response = getApiRootTarget()
										.path(apiMethod)
										.queryParam("format", format)
										.request(MediaType.APPLICATION_JSON)
										.get();
				log.debug("Response: " + response);
				if (response.getStatus() == Response.Status.OK.getStatusCode()) {
					Reader reader = new InputStreamReader((InputStream)response.getEntity());
					dhcpLease = DhcpLease.fromJson(reader);
				}				
			}
			else if ((format != null) && format.equalsIgnoreCase("gson")) {
				Response response = getApiRootTarget()
										.path(apiMethod)
										.queryParam("format", format)
										.request(MediaType.APPLICATION_JSON)
										.get();
				log.debug("Response: " + response);
				if (response.getStatus() == Response.Status.OK.getStatusCode()) {
					Reader reader = new InputStreamReader((InputStream)response.getEntity());
					dhcpLease = gson.fromJson(reader, DhcpLease.class);
				}
			}
			else {
				DhcpLeaseJson leaseJson = getApiRootTarget()
											.path(apiMethod)
											.request(MediaType.APPLICATION_JSON)
											.get(DhcpLeaseJson.class);
				dhcpLease = leaseJson.toDhcpLease();
			}
			return dhcpLease;
		}
		catch (Exception ex) {
			log.error("Failed to process sync get DhcpLease: " + ex);
			return null;
		}
	}
 */
	
	
	public InputStream doGetStream(String apiMethod, Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		try {
			log.debug("Invoking stream get on: '" + method.getUri());
			InputStream responseStream = invocationBuilder.get(InputStream.class);
//			StringBuilder sb = new StringBuilder();
//			int c = responseStream.read();
//			while (c != -1) {
//				log.debug("ResponseStream: availableByteCount=" + responseStream.available());
//				log.debug("char=" + c);
//				if (c != 10) {	// line separator
//					sb.append((char)c);
//				}
//				else {
//					log.debug("IP=" + sb.toString());
//					sb.setLength(0);
//				}
//				c = responseStream.read();
//			}
//			return null;
			return responseStream;
		}
		catch (Exception ex) {
			log.error(apiMethod + " stream get failed: " + ex);
			return null;
		}
	}
	
	public Future<String> doGetAsync(String apiMethod,
									 InvocationCallback<String> callback) {
		return this.doGetAsync(apiMethod, callback, null);
	}	
	
	public Future<String> doGetAsync(String apiMethod,
									 InvocationCallback<String> callback,
									 Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		// accept text/plain response data
		Invocation.Builder builder = method.request(MediaType.TEXT_PLAIN);
		log.debug("Invoking sync get on: " + method.getUri());
		try {
			// keep it generic here by using primitives
			// let the caller handle the data marshaling
			return builder.async().get(callback);
		}
		catch (Exception ex) {
			log.error(apiMethod + " async get failed: " + ex);
			return null;
		}
	}
	
	public String doPut(String apiMethod, String data) {
		return this.doPut(apiMethod, data, null);
	}
	
	public String doPut(String apiMethod, String data,
			 			Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		try {
			log.debug("Invoking sync put on: " + method.getUri());
			String response = invocationBuilder.put(
					Entity.entity(data, MediaType.TEXT_PLAIN), String.class);
			log.debug("Response: " + response);
			return response;
		}
		catch (Exception ex) {
			log.error(apiMethod + " sync put failed: " + ex);
			return null;
		}
	}
	
	public Future<String> doPutAsync(String apiMethod, String data, 
									 InvocationCallback<String> callback) {
		return this.doPutAsync(apiMethod, data, callback, null);
	}
	
	public Future<String> doPutAsync(String apiMethod, String data, 
									 InvocationCallback<String> callback,
							 		 Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
        Future<String> entityFuture = invocationBuilder.async().put(
				Entity.entity(data, MediaType.TEXT_PLAIN), callback);
        return entityFuture;
	}
	
	public String doPost(String apiMethod, String data) {
		return this.doPost(apiMethod, data, null);
	}
	
	public String doPost(String apiMethod, String data,
	 		 			 Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		try {
			log.debug("Invoking sync post on: " + method.getUri());
//			String response = invocationBuilder.post(
//					Entity.entity(json, MediaType.APPLICATION_JSON), String.class);
//			Response response = invocationBuilder.post(
//					Entity.entity(json, MediaType.APPLICATION_JSON));
			String response = invocationBuilder.post(
					Entity.entity(data, MediaType.TEXT_PLAIN), String.class);
			log.debug("Response: " + response);
			return response;
		}
		catch (Exception ex) {
			log.error(apiMethod + " sync post failed: " + ex);
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

	public Future<String> doPostAsync(String apiMethod, String data, 
									  InvocationCallback<String> callback) {
		return this.doPostAsync(apiMethod, data, callback, null);
	}
	
	public Future<String> doPostAsync(String apiMethod, String data, 
									  InvocationCallback<String> callback,
								 	  Map<String, Object> queryParams) {
		WebTarget method = buildWebTarget(apiMethod, queryParams);
		// accept text/plain response data
		Invocation.Builder invocationBuilder = method.request(MediaType.TEXT_PLAIN);
		log.debug("Invoking async post on: " + method.getUri());
        Future<String> entityFuture = invocationBuilder.async().post(
				Entity.entity(data, MediaType.TEXT_PLAIN), callback);
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
		String api = "dhcpserverstatus";
		String format = null;
		
		if ((args != null) && (args.length > 0)) {
			for (int i=0; i<args.length; i++) {
				if (args[i].equals("-h")) {
					host = args[++i];
				}
				if (args[i].equals("-p")) {
					port = Integer.valueOf(args[++i]);
				}
				if (args[i].equals("-a")) {
					api = args[++i];
				}
				if (args[i].equals("-f")) {
					format = args[++i];
				}
			}
		}
		
		try {
			JerseyRestClient client = new JerseyRestClient(host, port);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("format", format);
			String response = client.doGet(api, params);
			System.out.println("String: " + response);
			
			DhcpLease dhcpLease = null;
			if ("json".equals(format)) {
				dhcpLease = DhcpLease.fromJson(response);
			}
			else if ("gson".equals(format)) {
				dhcpLease = gson.fromJson(response, DhcpLease.class);
			}
			System.out.println("DhcpLease: " + dhcpLease);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}