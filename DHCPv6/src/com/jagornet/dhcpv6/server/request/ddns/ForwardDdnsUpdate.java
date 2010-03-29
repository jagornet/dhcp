/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ForwardDdnsUpdate.java is part of DHCPv6.
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
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.DHCIDRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;

/**
 * The Class ForwardDdnsUpdate.
 * 
 * @author A. Gregory Rabil
 */
public class ForwardDdnsUpdate extends DdnsUpdate 
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(ForwardDdnsUpdate.class);

	/**
	 * Instantiates a new forward ddns update.
	 * 
	 * @param fqdn the fqdn
	 * @param inetAddr the inet addr
	 * @param duid the duid
	 */
	public ForwardDdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid)
	{
		super(fqdn, inetAddr, duid);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendAdd()
	 */
	public void sendAdd() throws TextParseException, IOException
	{
		Resolver res = createResolver();
		
		Name owner = new Name(fqdn);
		AAAARecord aaaa = new AAAARecord(owner, DClass.IN, ttl, inetAddr);
		DHCIDRecord dhcid = new DHCIDRecord(owner, DClass.IN, ttl, data);

		Name _zone = buildZoneName(fqdn);
		
		Update update = new Update(_zone);
		update.absent(owner);
		update.add(aaaa);
		update.add(dhcid);

		if (log.isDebugEnabled()) {
			log.debug("Sending DDNS update:\n" + update.toString());
		}
		else if (log.isInfoEnabled()) {
			log.info("Sending DDNS update (not-exist/add): " + aaaa.toString());
		}
		Message response = res.send(update);

		if (response.getRcode() != Rcode.NOERROR) {
			if (response.getRcode() == Rcode.YXDOMAIN) {
				update = new Update(_zone);
				update.present(owner);
				dhcid = new DHCIDRecord(owner, DClass.IN, 0, data);
				update.present(dhcid);
				update.add(aaaa);
				if (log.isDebugEnabled()) {
					log.debug("Sending DDNS update:\n" + update.toString());
				}
				else if (log.isInfoEnabled()) {
					log.info("Sending DDNS update (exist/add): " + aaaa.toString());
				}
				response = res.send(update);
				if (response.getRcode() != Rcode.NOERROR) {
					log.error("Failed to update existing FORWARD RRSet: " +
							Rcode.string(response.getRcode()));
				}
			}
			else {
				log.error("Failed to create new FORWARD RRSet: " +
						Rcode.string(response.getRcode()));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendDelete()
	 */
	public void sendDelete() throws TextParseException, IOException
	{
		Resolver res = createResolver();
		
		Name owner = new Name(fqdn);
		AAAARecord aaaa = new AAAARecord(owner, DClass.IN, 0, inetAddr);
		DHCIDRecord dhcid = new DHCIDRecord(owner, DClass.IN, 0, data);

		Name _zone = buildZoneName(fqdn);
		
		Update update = new Update(_zone);
		update.present(dhcid);
		update.delete(aaaa);

		if (log.isDebugEnabled()) {
			log.debug("Sending DDNS update: " + update);
		}
		else if (log.isInfoEnabled()) {
			log.info("Sending DDNS update (exist/delete): " + aaaa.toString());
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			update = new Update(_zone);
			update.present(dhcid);
			update.absent(owner, Type.A);
			update.absent(owner, Type.AAAA);
			update.delete(owner);
			if (log.isDebugEnabled()) {
				log.debug("Sending DDNS update: " + update);
			}
			else if (log.isInfoEnabled()) {
				log.info("Sending DDNS update (not-exist/delete): " + owner.toString());
			}
			response = res.send(update);
			if (response.getRcode() != Rcode.NOERROR) {
				log.error("Failed to update FORWARD RRSet: " +
						Rcode.string(response.getRcode()));
			}
		}
		
	}
	
	/**
	 * Builds the zone name.
	 * 
	 * @param fqdn the fqdn
	 * 
	 * @return the name
	 * 
	 * @throws TextParseException the text parse exception
	 */
	private Name buildZoneName(String fqdn) throws TextParseException
	{
		Name _zone = null;
		if (zone != null) {
			_zone = new Name(zone);
		}
		else {
			int p = fqdn.indexOf('.');
			_zone = new Name(fqdn.substring(p+1));
		}
		return _zone;
	}
}
