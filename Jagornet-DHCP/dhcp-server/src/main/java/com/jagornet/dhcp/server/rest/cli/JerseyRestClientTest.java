package com.jagornet.dhcp.server.rest.cli;

import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

public class JerseyRestClientTest {
	
	public static void main(String[] args) {
		try {
			//TODO: fix hack to skip server name checking?
			HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);
			HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore ts = KeyStore.getInstance("JKS");
//			ts.load(ClassLoader.getSystemResourceAsStream("jagornetCA.jks"), "jagornet".toCharArray());
//			ts.load(ClassLoader.getSystemResourceAsStream("dhcp.jagornet.com.cert.jks"), "jagornet".toCharArray());
			ts.load(ClassLoader.getSystemResourceAsStream("jagornet_dhcp_backup.jks"), "jagornet".toCharArray());
//			ts.load(ClassLoader.getSystemResourceAsStream("server.dhcp.jagornet.com.cert.jks"), "jagornet".toCharArray());
			tmf.init(ts);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance("JKS");
//			ks.load(ClassLoader.getSystemResourceAsStream("jagornet-client.jks"), "jagornet".toCharArray());
//			ks.load(ClassLoader.getSystemResourceAsStream("client.dhcp.jagornet.com.key.jks.3"), "jagornet".toCharArray());
			ks.load(ClassLoader.getSystemResourceAsStream("jagornet_dhcp_backup.jks"), "jagornet".toCharArray());
			kmf.init(ks, "jagornet".toCharArray());
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

//			SSLContext ctx = SSLContext.getInstance("TLS");
//			ctx.init(null, tmf.getTrustManagers(), null);
			Client client = ClientBuilder.newBuilder()
					.sslContext(ctx)
					.hostnameVerifier(hostnameVerifier)
					.build();
			WebTarget api = client.target("https://localhost:9060");
			WebTarget status = api.path("dhcpserverstatus");
			Invocation.Builder invocationBuilder = status.request("text/plain");
			String response = invocationBuilder.get(String.class);
			System.out.println(response);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
