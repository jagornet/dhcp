/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.testutil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TSIGRecord;

/**
 * Lightweight in-memory UDP DNS server test double for testing DDNS updates.
 * Binds an ephemeral local UDP port, decodes incoming RFC 2136 DNS UPDATE messages,
 * records query history and section records, and replies with configurable RCODEs
 * and optional TSIG signatures.
 * 
 * @author A. Gregory Rabil
 */
public class TestDnsServer implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(TestDnsServer.class);

	private final DatagramSocket socket;
	private final int port;
	private final Thread listenerThread;
	private final AtomicBoolean running = new AtomicBoolean(true);

	private final List<Message> receivedQueries = new CopyOnWriteArrayList<Message>();
	private final AtomicInteger responseRcode = new AtomicInteger(Rcode.NOERROR);
	private volatile TSIG tsig;

	// Optional custom response handler for advanced scenarios (e.g., YXDOMAIN on first attempt then NOERROR)
	private volatile java.util.function.Function<Message, Integer> rcodeProvider;

	public TestDnsServer() throws IOException {
		this.socket = new DatagramSocket(0, InetAddress.getByName("127.0.0.1"));
		this.port = socket.getLocalPort();
		this.listenerThread = new Thread(new Runnable() {
			public void run() {
				listenLoop();
			}
		}, "TestDnsServer-" + port);
		this.listenerThread.setDaemon(true);
		this.listenerThread.start();
		log.info("TestDnsServer started on 127.0.0.1:{}", port);
	}

	private void listenLoop() {
		byte[] buffer = new byte[4096];
		while (running.get() && !socket.isClosed()) {
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
				Message query = new Message(data);
				receivedQueries.add(query);

				int rcode = responseRcode.get();
				if (rcodeProvider != null) {
					Integer provided = rcodeProvider.apply(query);
					if (provided != null) {
						rcode = provided.intValue();
					}
				}

				Message response = new Message(query.getHeader().getID());
				Header respHeader = response.getHeader();
				respHeader.setOpcode(query.getHeader().getOpcode());
				respHeader.setFlag(Flags.QR);
				respHeader.setRcode(rcode);

				// Copy zone section if present
				Record question = query.getQuestion();
				if (question != null) {
					response.addRecord(question, Section.QUESTION);
				}

				TSIGRecord queryTsig = query.getTSIG();
				if (queryTsig != null && tsig != null) {
					tsig.apply(response, queryTsig);
				}

				byte[] respBytes = response.toWire();
				DatagramPacket respPacket = new DatagramPacket(respBytes, respBytes.length,
						packet.getAddress(), packet.getPort());
				socket.send(respPacket);
			}
			catch (IOException e) {
				if (!running.get() || socket.isClosed()) {
					break;
				}
				log.warn("TestDnsServer socket exception: {}", e.getMessage());
			}
		}
	}

	public int getPort() {
		return port;
	}

	public String getHostPort() {
		return "127.0.0.1:" + port;
	}

	public List<Message> getReceivedQueries() {
		return Collections.unmodifiableList(receivedQueries);
	}

	public Message getLastQuery() {
		if (receivedQueries.isEmpty()) {
			return null;
		}
		return receivedQueries.get(receivedQueries.size() - 1);
	}

	public List<Record> getLastPrerequisites() {
		Message last = getLastQuery();
		if (last == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(last.getSectionArray(Section.PREREQ));
	}

	public List<Record> getLastUpdates() {
		Message last = getLastQuery();
		if (last == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(last.getSectionArray(Section.UPDATE));
	}

	public void setResponseRcode(int rcode) {
		this.responseRcode.set(rcode);
	}

	public void setRcodeProvider(java.util.function.Function<Message, Integer> provider) {
		this.rcodeProvider = provider;
	}

	public void setTsig(String keyName, String keyData) {
		if (keyName == null || keyData == null) {
			this.tsig = null;
			return;
		}
		try {
			this.tsig = new TSIG(keyName.endsWith(".") ? keyName : keyName + ".", keyData);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize TSIG: " + e.getMessage(), e);
		}
	}

	public void setTsig(String algorithm, String keyName, String keyData) {
		if (keyName == null || keyData == null) {
			this.tsig = null;
			return;
		}
		try {
			String algo = algorithm.endsWith(".") ? algorithm : algorithm + ".";
			String name = keyName.endsWith(".") ? keyName : keyName + ".";
			this.tsig = new TSIG(new Name(algo), name, keyData);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize TSIG: " + e.getMessage(), e);
		}
	}

	public void clear() {
		receivedQueries.clear();
		responseRcode.set(Rcode.NOERROR);
		rcodeProvider = null;
		tsig = null;
	}

	@Override
	public void close() {
		running.set(false);
		if (!socket.isClosed()) {
			socket.close();
		}
		try {
			listenerThread.join(1000);
		}
		catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
		log.info("TestDnsServer on port {} stopped", port);
	}
}
