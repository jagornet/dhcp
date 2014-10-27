/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DdnsUpdate.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.request.ddns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TextParseException;

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
	public DdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid)
	{
		this.fqdn = fqdn;
		this.inetAddr = inetAddr;
		
		byte[] buf = new byte[duid.length + fqdn.getBytes().length];
		System.arraycopy(duid, 0, buf, 0, duid.length);
		System.arraycopy(fqdn.getBytes(), 0, buf, duid.length, fqdn.getBytes().length);
		
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
		Resolver res = new SimpleResolver(server);
		if ((tsigKeyName != null) && (tsigKeyName.length() > 0)) {
			TSIG tsig = null;
			if (tsigAlgorithm != null) {
				tsig = new TSIG(new Name(tsigAlgorithm), tsigKeyName, tsigKeyData);
			}
			else {
				tsig = new TSIG(tsigKeyName, tsigKeyData);
			}
			res.setTSIGKey(tsig);
		}
		return res;
	}
	
	/**
	 * Send add.
	 * 
	 * @throws TextParseException the text parse exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract boolean sendAdd() throws TextParseException, IOException;
	
	/**
	 * Send delete.
	 * 
	 * @throws TextParseException the text parse exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract boolean sendDelete() throws TextParseException, IOException;
	
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
