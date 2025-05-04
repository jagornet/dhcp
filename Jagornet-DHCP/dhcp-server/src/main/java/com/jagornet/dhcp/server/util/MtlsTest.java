package com.jagornet.dhcp.server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Utility CLI for testing server SSL certificate keystores
 * with client SSL certificate truststores.
 * 
 * @author A. Gregory Rabil
 *
 */
public class MtlsTest {

	public static void main(String[] args) {

		try {
			Options options = new Options();
			options.addOption(new Option("s", "server", true, "The port to create a test SSL socket server on using the keystore"));
			options.addOption(new Option("c", "client", true, "The port on the server for the client to connect to using the truststore"));
			options.addOption(new Option("h", "host", true, "The host to connect to when running the client"));
			options.addOption(new Option("kf", "keystoreFilename", true, "The Java keystore filename"));
			options.addOption(new Option("kp", "keystorePassword", true, "The Java keystore password"));
			options.addOption(new Option("tf", "truststoreFilename", true, "The Java truststore filename"));
			options.addOption(new Option("tp", "truststorePassword", true, "The Java truststore password"));
			options.addOption(new Option("?", "help", false, "Show this help"));
			
			CommandLineParser parser = new BasicParser();
            HelpFormatter formatter = new HelpFormatter();
			CommandLine cmd = null;
			try {
				cmd = parser.parse(options, args);
			}
			catch (ParseException ex) {
				System.err.println(ex);
				formatter.printHelp( "MtlsTest", options );
				System.exit(1);
			}

            String ksFilename = getFilenameOption(cmd, "keystoreFilename");
            String ksPassword = getPasswordOption(cmd, "keystorePassword");
            String tsFilename = getFilenameOption(cmd, "truststoreFilename");
            String tsPassword = getPasswordOption(cmd, "truststorePassword");
            if ((ksFilename == null) || (ksPassword == null) || (tsFilename == null) || (tsPassword == null)) {
				formatter.printHelp( "MtlsTest", options );
				System.exit(1);
            }

			MtlsConfig mtlsConfig = new MtlsConfig(ksFilename, ksPassword, tsFilename, tsPassword);
			
            if (cmd.hasOption("server")) {

				SSLContext sslContext = mtlsConfig.getSslContext();
				runServer(sslContext, Integer.parseInt(cmd.getOptionValue("server")));
			}
			else if (cmd.hasOption("client")) {
				if (!cmd.hasOption("host")) {
					System.err.println("Must specify a host to connect to");
				}
				String host = cmd.getOptionValue("host");
				if (host == null) {
                    formatter.printHelp( "MtlsTest", options );
					System.exit(1);
				}

				SSLContext sslContext = mtlsConfig.getSslContext();
				runClient(sslContext, host, Integer.parseInt(cmd.getOptionValue("client")));
			}
			else {
				formatter.printHelp( "MtlsTest", options );
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex);
			System.exit(1);
		}
	}
	
	private static String getFilenameOption(CommandLine cmd, String filenameOption) {
		if (!cmd.hasOption(filenameOption)) {
			System.err.println("Must specify a " + filenameOption);
			return null;
		}
		else {
			String filename = cmd.getOptionValue(filenameOption);
			if (!new File(filename).exists()) {
				System.err.println("File not found: " + filename);
				return null;
			}
			return filename;
		}
	}
	
	private static String getPasswordOption(CommandLine cmd, String passwordOption) {
		if (!cmd.hasOption(passwordOption)) {
			System.err.println("Must specify a " + passwordOption);
		}
		return cmd.getOptionValue(passwordOption);
	}

	public static int runServer(SSLContext sslContext, int port) throws IOException {
		ServerSocket serverSocket = createServerSocket(sslContext, port);
		Thread thread = new Thread() {
			public void run() {
				try {
					while (true) {
						System.out.println("Server listening on socket: " + serverSocket.toString());
						Socket socket = serverSocket.accept();
						System.out.println("Server Accepted socket: " + socket.toString());
	//		            OutputStream rawOut = socket.getOutputStream();
	//		            PrintWriter out = new PrintWriter(
	//		                                new BufferedWriter(
	//		                                new OutputStreamWriter(
	//		                                rawOut)));
						BufferedReader in =
								new BufferedReader(
									new InputStreamReader(socket.getInputStream()));
						String data = in.readLine();
						System.out.println("Data: " + data);
					}
				} 
				catch (IOException ex) {
					//e.printStackTrace();
					System.err.println(ex);
					System.exit(1);
				}
			}
		};
		thread.start();
		return serverSocket.getLocalPort();
	}
	
	public static void runClient(SSLContext sslContext, String host, int port) throws IOException {
		Socket clientSocket = createClientSocket(sslContext, host, port);
		System.out.println("Client Connected socket: " + clientSocket.toString());
        PrintWriter out = new PrintWriter(
                new BufferedWriter(
                new OutputStreamWriter(
                clientSocket.getOutputStream())));

		out.println("Hello World");
		out.println();
		out.flush();
		/*
		* Make sure there were no surprises
		*/
		if (out.checkError()) {
			System.err.println("SSLSocketClient:  java.io.PrintWriter error");
		}
	}
	
	// public static ServerSocket createServerSocket(KeyManagerFactory kmf, TrustManagerFactory tmf, int port) throws IOException {
	// 	SSLContext serverCtx = MtlsConfig.buildSSLContext(kmf.getKeyManagers(), tmf.getTrustManagers());
	public static ServerSocket createServerSocket(SSLContext serverCtx, int port) throws IOException {
		SSLServerSocketFactory serverFactory = serverCtx.getServerSocketFactory();
		SSLServerSocket serverSocket = (SSLServerSocket) serverFactory.createServerSocket(port);
		// Enable client authentication
		serverSocket.setNeedClientAuth(true);
		//serverSocket.bind(null);
		return serverSocket;
	}

	// public static Socket createClientSocket(KeyManagerFactory kmf, TrustManagerFactory tmf, String host, int port) throws IOException {
	// 	SSLContext clientCtx = MtlsConfig.buildSSLContext(kmf.getKeyManagers(), tmf.getTrustManagers());
	public static Socket createClientSocket(SSLContext clientCtx, String host, int port) throws IOException {
		SSLSocketFactory clientFactory = clientCtx.getSocketFactory();
		SSLSocket clientSocket = (SSLSocket)clientFactory.createSocket(host, port);
		clientSocket.startHandshake();
		return clientSocket;
	}
}
