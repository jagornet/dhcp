/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ReverseDdnsUpdate.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.request.ddns;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Update;

/**
 * The Class ReverseDdnsUpdate.
 * 
 * @author A. Gregory Rabil
 */
public class ReverseDdnsUpdate extends DdnsUpdate 
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(ReverseDdnsUpdate.class);

	/** The rev zone bit length. */
	protected int revZoneBitLength = 64;	// default
	
	/**
	 * Instantiates a new reverse ddns update.
	 * 
	 * @param fqdn the fqdn
	 * @param inetAddr the inet addr
	 * @param duid the duid
	 */
	public ReverseDdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid)
	{
		super(fqdn, inetAddr, duid);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendAdd()
	 */
	public void sendAdd() throws TextParseException, IOException
	{
		Resolver res = createResolver();

		String revIp = buildReverseIpString();
		
		Name owner = new Name(revIp.toString());
		PTRRecord ptr = new PTRRecord(owner, DClass.IN, ttl, new Name(fqdn));
		
		Name _zone = buildZoneName(revIp);
		
		Update update = new Update(_zone);
		update.delete(owner);
		update.add(ptr);

		if (log.isDebugEnabled()) {
			log.debug("Sending DDNS update:\n" + update.toString());
		}
		else if (log.isInfoEnabled()) {
			log.info("Sending DDNS update (replace): " + ptr.toString());
		}
		Message response = res.send(update);

		if (response.getRcode() != Rcode.NOERROR) {
			log.error("Failed to update REVERSE RRSet: " +
					Rcode.string(response.getRcode()));
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendDelete()
	 */
	public void sendDelete() throws TextParseException, IOException
	{
		Resolver res = createResolver();

		String revIp = buildReverseIpString();
		
		Name owner = new Name(revIp);
		PTRRecord ptr = new PTRRecord(owner, DClass.IN, 0, new Name(fqdn));
		
		Name _zone = buildZoneName(revIp);
		
		Update update = new Update(_zone);
		update.delete(ptr);

		if (log.isDebugEnabled()) {
			log.debug("Sending DDNS update:\n" + update.toString());
		}
		else if (log.isInfoEnabled()) {
			log.info("Sending DDNS update (delete): " + ptr.toString());
		}
		Message response = res.send(update);

		if (response.getRcode() != Rcode.NOERROR) {
			log.error("Failed to update REVERSE RRSet: " +
					Rcode.string(response.getRcode()));
		}
	}
	
	/**
	 * Builds the reverse ip string.
	 * 
	 * @return the string
	 */
	private String buildReverseIpString()
	{
		String[] flds = inetAddr.getHostAddress().split(":");
		if (flds.length != 8) {
			throw new IllegalStateException("Invalid IPv6 Address: " + 
					inetAddr.getHostAddress());
		}
		StringBuilder expanded = new StringBuilder();
		for (int i=0; i<8; i++) {
			StringBuilder sb = new StringBuilder();
			if (flds[i].length() < 4) {
				for (int j=0; j<4-flds[i].length(); j++) {
					sb.append('0');
				}
			}
			sb.append(flds[i]);
			expanded.append(sb);
		}
		StringBuilder reversed = expanded.reverse();
		StringBuilder revIp = new StringBuilder();
		for (int i=0; i<reversed.length(); i++) {
			revIp.append(reversed.substring(i, i+1));
			revIp.append('.');
		}
		revIp.append("ip6.arpa.");
		return revIp.toString();
	}
	
	/**
	 * Builds the zone name.
	 * 
	 * @param revIp the rev ip
	 * 
	 * @return the name
	 * 
	 * @throws TextParseException the text parse exception
	 */
	private Name buildZoneName(String revIp) throws TextParseException
	{
		Name _zone = null;
		if (zone != null) {
			_zone = new Name(zone);
		}
		else {
			int p = 64 - (revZoneBitLength/2);
			_zone = new Name(revIp.substring(p)); 
		}
		return _zone;
	}

	/**
	 * Gets the rev zone bit length.
	 * 
	 * @return the rev zone bit length
	 */
	public int getRevZoneBitLength() {
		return revZoneBitLength;
	}

	/**
	 * Sets the rev zone bit length.
	 * 
	 * @param revZoneBitLength the new rev zone bit length
	 */
	public void setRevZoneBitLength(int revZoneBitLength) {
		this.revZoneBitLength = revZoneBitLength;
	}
}
