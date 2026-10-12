/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.ddns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.DHCIDRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;

import com.jagornet.dhcp.core.util.Util;

/**
 * The Class DdnsUpdate.  The abstract base class for handling DDNS updates
 * according to RFC 4703 and RFC 4704.
 * 
 * @author A. Gregory Rabil
 */
public abstract class DdnsUpdate
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DdnsUpdate.class);

	/** The sha256 msg digest. */
	protected static MessageDigest sha256MsgDigest;
	
	/** The fqdn. */
	protected String fqdn;
	
	/** The inet addr. */
	protected InetAddress inetAddr;
	
	/** The data. */
	protected byte[] data;

	/** The raw duid or client identifier bytes. */
	protected byte[] duid;

	/** The ddns record type (dhcid vs txt). */
	protected DdnsRecordType recordType = DdnsRecordType.DHCID;

	/** The conflict resolution strategy. */
	protected DdnsConflictStrategy conflictStrategy = DdnsConflictStrategy.STRICT;

	/** The dynamic marker token for replace-dynamic strategy. */
	protected String dynamicToken = "dynamic";
	
	/** The server. */
	protected String server;
	
	/** The ttl. */
	protected long ttl;
	
	/** The zone. */
	protected String zone;
	
	/** The tsig key name. */
	protected String tsigKeyName;
	
	/** The tsig algorithm. */
	protected String tsigAlgorithm;
	
	/** The tsig key data. */
	protected String tsigKeyData;
	
	/**
	 * Instantiates a new ddns update.
	 * 
	 * @param fqdn the fqdn
	 * @param inetAddr the inet addr
	 * @param duid the duid
	 */
	protected DdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid)
	{
		this.fqdn = fqdn;
		this.inetAddr = inetAddr;
		this.duid = duid;
		
		if (duid != null) {
			byte[] buf = new byte[duid.length + fqdn.getBytes(StandardCharsets.UTF_8).length];
			System.arraycopy(duid, 0, buf, 0, duid.length);
			System.arraycopy(fqdn.getBytes(StandardCharsets.UTF_8), 0, buf, duid.length, fqdn.getBytes(StandardCharsets.UTF_8).length);
			
			MessageDigest md = getSha256MsgDigest();
			if (md != null) {
				byte[] hash = md.digest(buf);
				
				this.data = new byte[3 + hash.length];
				data[0] = (byte)0x00;
				data[1] = (byte)0x02;
				data[2] = (byte)0x01;
				System.arraycopy(hash, 0, data, 3, hash.length);
			}
		}
	}
	
	/**
	 * Creates the resolver.
	 * 
	 * @return the resolver
	 * 
	 * @throws UnknownHostException the unknown host exception
	 * @throws TextParseException the text parse exception
	 */
	protected Resolver createResolver() throws UnknownHostException, TextParseException 
	{
		Resolver res;
		if (server != null && !server.isEmpty()) {
			int colon = server.lastIndexOf(':');
			if (colon > 0 && !server.endsWith("]")) {
				String host = server.substring(0, colon);
				if (host.startsWith("[") && host.endsWith("]")) {
					host = host.substring(1, host.length() - 1);
				}
				int portNum = Integer.parseInt(server.substring(colon + 1));
				res = new SimpleResolver(host);
				res.setPort(portNum);
			} else {
				res = new SimpleResolver(server);
			}
		} else {
			res = new SimpleResolver();
		}
		if ((tsigKeyName != null) && !tsigKeyName.isEmpty()) {
			TSIG tsig = null;
			String keyName = tsigKeyName.endsWith(".") ? tsigKeyName : tsigKeyName + ".";
			if ((tsigAlgorithm != null) && !tsigAlgorithm.trim().isEmpty()) {
				String algo = tsigAlgorithm.endsWith(".") ? tsigAlgorithm : tsigAlgorithm + ".";
				tsig = new TSIG(new Name(algo), keyName, tsigKeyData);
			}
			else {
				tsig = new TSIG(keyName, tsigKeyData);
			}
			res.setTSIGKey(tsig);
		}
		return res;
	}
	
	/**
	 * Send add.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract boolean sendAdd() throws IOException;
	
	/**
	 * Send delete.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract boolean sendDelete() throws IOException;
	
	/**
	 * Gets the server.
	 * 
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Sets the server.
	 * 
	 * @param server the new server
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * Gets the ttl.
	 * 
	 * @return the ttl
	 */
	public long getTtl() {
		return ttl;
	}

	/**
	 * Sets the ttl.
	 * 
	 * @param ttl the new ttl
	 */
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	/**
	 * Gets the zone.
	 * 
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * Sets the zone.
	 * 
	 * @param zone the new zone
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}

	/**
	 * Gets the tsig key name.
	 * 
	 * @return the tsig key name
	 */
	public String getTsigKeyName() {
		return tsigKeyName;
	}

	/**
	 * Sets the tsig key name.
	 * 
	 * @param tsigKeyName the new tsig key name
	 */
	public void setTsigKeyName(String tsigKeyName) {
		this.tsigKeyName = tsigKeyName;
	}

	/**
	 * Gets the tsig algorithm.
	 * 
	 * @return the tsig algorithm
	 */
	public String getTsigAlgorithm() {
		return tsigAlgorithm;
	}

	/**
	 * Sets the tsig algorithm.
	 * 
	 * @param tsigAlgorithm the new tsig algorithm
	 */
	public void setTsigAlgorithm(String tsigAlgorithm) {
		this.tsigAlgorithm = tsigAlgorithm;
	}

	/**
	 * Gets the tsig key data.
	 * 
	 * @return the tsig key data
	 */
	public String getTsigKeyData() {
		return tsigKeyData;
	}

	/**
	 * Sets the tsig key data.
	 * 
	 * @param tsigKeyData the new tsig key data
	 */
	public void setTsigKeyData(String tsigKeyData) {
		this.tsigKeyData = tsigKeyData;
	}

	public DdnsRecordType getRecordType() {
		return recordType;
	}

	public void setRecordType(DdnsRecordType recordType) {
		this.recordType = (recordType != null) ? recordType : DdnsRecordType.DHCID;
	}

	public DdnsConflictStrategy getConflictStrategy() {
		return conflictStrategy;
	}

	public void setConflictStrategy(DdnsConflictStrategy conflictStrategy) {
		this.conflictStrategy = (conflictStrategy != null) ? conflictStrategy : DdnsConflictStrategy.STRICT;
	}

	public String getDynamicToken() {
		return dynamicToken;
	}

	public void setDynamicToken(String dynamicToken) {
		this.dynamicToken = dynamicToken;
	}

	public byte[] getDuid() {
		return duid;
	}

	public byte[] getData() {
		return data;
	}

	/**
	 * Computes DHCID data for the configured dynamic token.
	 *
	 * @return the DHCID byte array for the dynamic token
	 */
	public byte[] computeDynamicDhcidData() {
		String token = (dynamicToken != null && !dynamicToken.isEmpty()) ? dynamicToken : "dynamic";
		byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
		byte[] fqdnBytes = (fqdn != null) ? fqdn.getBytes(StandardCharsets.UTF_8) : new byte[0];
		byte[] buf = new byte[tokenBytes.length + fqdnBytes.length];
		System.arraycopy(tokenBytes, 0, buf, 0, tokenBytes.length);
		System.arraycopy(fqdnBytes, 0, buf, tokenBytes.length, fqdnBytes.length);

		MessageDigest md = getSha256MsgDigest();
		if (md != null) {
			byte[] hash = md.digest(buf);
			byte[] dynData = new byte[3 + hash.length];
			dynData[0] = (byte) 0x00;
			dynData[1] = (byte) 0x02;
			dynData[2] = (byte) 0x01;
			System.arraycopy(hash, 0, dynData, 3, hash.length);
			return dynData;
		}
		return new byte[0];
	}

	/**
	 * Builds a dynamic marker record (used in replace-dynamic strategy).
	 *
	 * @param owner the record owner Name
	 * @param recordTtl the TTL
	 * @return the dynamic marker Record (TXTRecord or DHCIDRecord)
	 */
	public Record buildDynamicMarkerRecord(Name owner, long recordTtl) {
		if (recordType == DdnsRecordType.TXT) {
			String token = (dynamicToken != null && !dynamicToken.isEmpty()) ? dynamicToken : "dynamic";
			return new TXTRecord(owner, DClass.IN, recordTtl, token);
		} else {
			return new DHCIDRecord(owner, DClass.IN, recordTtl, computeDynamicDhcidData());
		}
	}

	/**
	 * Builds a client-specific record (used in strict strategy).
	 *
	 * @param owner the record owner Name
	 * @param recordTtl the TTL
	 * @return the client Record (TXTRecord or DHCIDRecord)
	 */
	public Record buildClientRecord(Name owner, long recordTtl) {
		if (recordType == DdnsRecordType.TXT) {
			String txtVal = (data != null) ? Util.toHexString(data).toLowerCase() : "";
			return new TXTRecord(owner, DClass.IN, recordTtl, txtVal);
		} else {
			return new DHCIDRecord(owner, DClass.IN, recordTtl, (data != null) ? data : new byte[0]);
		}
	}

	/**
	 * Builds the appropriate identification record based on configured strategy.
	 *
	 * @param owner the record owner Name
	 * @param recordTtl the TTL
	 * @return the identification Record
	 */
	public Record buildIdentificationRecord(Name owner, long recordTtl) {
		if (conflictStrategy == DdnsConflictStrategy.REPLACE_DYNAMIC) {
			return buildDynamicMarkerRecord(owner, recordTtl);
		} else {
			return buildClientRecord(owner, recordTtl);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		try {
			String fqdn = args[0];
			long ttl = Long.parseLong(args[1]);
			InetAddress inetAddr = InetAddress.getByName(args[2]);
			byte[] duid = args[3].getBytes();
			String server = args[4];
			
			ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, inetAddr, duid);
			fwd.setServer(server);
			fwd.setTtl(ttl);
			fwd.setTsigKeyName("macbook-ubuntu.");
			fwd.setTsigAlgorithm("hmac-sha256.");
			fwd.setTsigKeyData("3BE05CzQLXTm5ouGljhJeQ==");
			fwd.sendAdd();
			
			ReverseDdnsUpdate rev = new ReverseDdnsUpdate(fqdn, inetAddr, duid);
			rev.setRevZoneBitLength(48);
			rev.setServer(server);
			rev.setTtl(ttl);
			rev.setTsigKeyName("macbook-ubuntu.");
			rev.setTsigAlgorithm("hmac-sha256.");
			rev.setTsigKeyData("3BE05CzQLXTm5ouGljhJeQ==");
			rev.sendAdd();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Gets the sha256 msg digest.
	 * 
	 * @return the sha256 msg digest
	 */
	protected MessageDigest getSha256MsgDigest() {
		if (sha256MsgDigest == null) {
			try {
				sha256MsgDigest = MessageDigest.getInstance("SHA-256");
			}
			catch (NoSuchAlgorithmException ex) {
				log.error("Failed to get MessageDigest for SHA-256", ex);
			}
		}
		return sha256MsgDigest;
	}
}
