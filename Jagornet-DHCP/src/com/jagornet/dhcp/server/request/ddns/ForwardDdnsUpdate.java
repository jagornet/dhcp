/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ForwardDdnsUpdate.java is part of Jagornet DHCP.
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
import java.net.Inet6Address;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.DHCIDRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
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
	public boolean sendAdd() throws TextParseException, IOException
	{
		boolean rc = false;
		
		Resolver res = createResolver();
		
		Name owner = new Name(fqdn);
		Record a_aaaa = null;
		if (inetAddr instanceof Inet6Address) {
			a_aaaa = new AAAARecord(owner, DClass.IN, ttl, inetAddr);
		}
		else {
			a_aaaa = new ARecord(owner, DClass.IN, ttl, inetAddr);
		}
		DHCIDRecord dhcid = new DHCIDRecord(owner, DClass.IN, ttl, data);

		Name _zone = buildZoneName(fqdn);
		
		Update update = new Update(_zone);
		update.absent(owner);
		update.add(a_aaaa);
		update.add(dhcid);

		if (log.isDebugEnabled()) {
			log.debug("Sending forward DDNS update (not-exist/add) to server=" + server + ":\n" +  
					update.toString());
		}
		else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (not-exist/add): " + a_aaaa.toString());
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			log.info("Forward DDNS update (not-exist/add) succeeded: " + a_aaaa.toString());
			rc = true;
		}
		else {
			if (response.getRcode() == Rcode.YXDOMAIN) {
				update = new Update(_zone);
				update.present(owner);
				dhcid = new DHCIDRecord(owner, DClass.IN, 0, data);
				update.present(dhcid);
				update.add(a_aaaa);
				if (log.isDebugEnabled()) {
					log.debug("Sending forward DDNS update (exist/update) to server=" + server + ":\n" + 
							update.toString());
				}
				else if (log.isInfoEnabled()) {
					log.info("Sending forward DDNS update (exist/update): " + a_aaaa.toString());
				}
				response = res.send(update);
				if (response.getRcode() == Rcode.NOERROR) {
					log.info("Forward DDNS update (exist/update) succeeded: " + a_aaaa.toString());
					rc = true;
				}
				else {
					log.error("Forward DDNS update (exist/update) failed (rcode=" +
							Rcode.string(response.getRcode()) + "): " + a_aaaa.toString());
				}
			}
			else {
				log.error("Forward DDNS update (not-exist/add) failed (rcode=" +
						Rcode.string(response.getRcode()) + "): " + a_aaaa.toString());
			}
		}
		return rc;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendDelete()
	 */
	public boolean sendDelete() throws TextParseException, IOException
	{
		boolean rc = false;
		
		Resolver res = createResolver();
		
		Name owner = new Name(fqdn);		
		Record a_aaaa = null;
		if (inetAddr instanceof Inet6Address) {
			a_aaaa = new AAAARecord(owner, DClass.IN, 0, inetAddr);
		}
		else {
			a_aaaa = new ARecord(owner, DClass.IN, 0, inetAddr);
		}
		DHCIDRecord dhcid = new DHCIDRecord(owner, DClass.IN, 0, data);

		Name _zone = buildZoneName(fqdn);
		
		Update update = new Update(_zone);
		update.present(dhcid);
		update.delete(a_aaaa);

		if (log.isDebugEnabled()) {
			log.debug("Sending forward DDNS update (exist/delete) to server=" + server + ":\n" + 
					update.toString());
		}
		else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (exist/delete): " + a_aaaa.toString());
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			update = new Update(_zone);
			update.present(dhcid);
			update.absent(owner, Type.A);
			update.absent(owner, Type.AAAA);
			update.delete(owner);
			if (log.isDebugEnabled()) {
				log.debug("Sending forward DDNS update (not-exist/delete) to server=" + server + ":\n" + 
						update.toString());
			}
			else if (log.isInfoEnabled()) {
				log.info("Sending forward DDNS update (not-exist/delete): " + owner.toString());
			}
			response = res.send(update);
			if (response.getRcode() == Rcode.NOERROR) {
				log.info("Forward DDNS update (not-exist/delete) succeeded: " + owner.toString());
				rc = true;
			}
			else {
				log.error("Forward DDNS update (not-exist/delete) failed (rcode=" +
						Rcode.string(response.getRcode()) + "): " + owner.toString());			
			}
		}
		else {
			log.error("Forward DDNS update (exist/delete) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa.toString());			
		}
		return rc;
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
