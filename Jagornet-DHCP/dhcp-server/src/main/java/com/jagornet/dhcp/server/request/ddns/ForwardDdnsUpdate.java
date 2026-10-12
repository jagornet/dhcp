/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
public class ForwardDdnsUpdate extends DdnsUpdate {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(ForwardDdnsUpdate.class);

	/**
	 * Instantiates a new forward ddns update.
	 * 
	 * @param fqdn     the fqdn
	 * @param inetAddr the inet addr
	 * @param duid     the duid
	 */
	public ForwardDdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid) {
		super(fqdn, inetAddr, duid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendAdd()
	 */
	@Override
	public boolean sendAdd() throws IOException {
		Resolver res = createResolver();
		Name owner = new Name(fqdn.endsWith(".") ? fqdn : fqdn + ".");
		Record a_aaaa = (inetAddr instanceof Inet6Address)
				? new AAAARecord(owner, DClass.IN, ttl, inetAddr)
				: new ARecord(owner, DClass.IN, ttl, inetAddr);
		Name _zone = buildZoneName(fqdn);

		switch (conflictStrategy) {
			case UNCONDITIONAL:
				return sendAddUnconditional(res, _zone, owner, a_aaaa);
			case REPLACE_DYNAMIC:
				return sendAddReplaceDynamic(res, _zone, owner, a_aaaa);
			case STRICT:
			default:
				return sendAddStrict(res, _zone, owner, a_aaaa);
		}
	}

	private boolean sendAddStrict(Resolver res, Name zone, Name owner, Record a_aaaa) throws IOException {
		Record idRecord = buildClientRecord(owner, ttl);
		Update update = new Update(zone);
		update.absent(owner);
		update.add(a_aaaa);
		update.add(idRecord);

		if (log.isDebugEnabled()) {
			log.debug("Sending forward DDNS update (strict not-exist/add) to server=" + server + ":\n" + update);
		} else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (strict not-exist/add): " + a_aaaa);
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			log.info("Forward DDNS update (strict not-exist/add) succeeded: " + a_aaaa);
			return true;
		} else if (response.getRcode() == Rcode.YXDOMAIN) {
			update = new Update(zone);
			update.present(owner);
			Record idPrereq = buildClientRecord(owner, 0);
			update.present(idPrereq);
			update.add(a_aaaa);

			if (log.isDebugEnabled()) {
				log.debug("Sending forward DDNS update (strict exist/update) to server=" + server + ":\n" + update);
			} else if (log.isInfoEnabled()) {
				log.info("Sending forward DDNS update (strict exist/update): " + a_aaaa);
			}
			response = res.send(update);
			if (response.getRcode() == Rcode.NOERROR) {
				log.info("Forward DDNS update (strict exist/update) succeeded: " + a_aaaa);
				return true;
			} else {
				log.error("Forward DDNS update (strict exist/update) failed (rcode=" +
						Rcode.string(response.getRcode()) + "): " + a_aaaa);
				return false;
			}
		} else {
			log.error("Forward DDNS update (strict not-exist/add) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa);
			return false;
		}
	}

	private boolean sendAddReplaceDynamic(Resolver res, Name zone, Name owner, Record a_aaaa) throws IOException {
		Record markerRecord = buildDynamicMarkerRecord(owner, ttl);
		Update update = new Update(zone);
		update.absent(owner);
		update.add(a_aaaa);
		update.add(markerRecord);

		if (log.isDebugEnabled()) {
			log.debug(
					"Sending forward DDNS update (replace-dynamic not-exist/add) to server=" + server + ":\n" + update);
		} else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (replace-dynamic not-exist/add): " + a_aaaa);
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			log.info("Forward DDNS update (replace-dynamic not-exist/add) succeeded: " + a_aaaa);
			return true;
		} else if (response.getRcode() == Rcode.YXDOMAIN) {
			// Name already exists. Prerequisite: existing record MUST have the dynamic
			// marker!
			update = new Update(zone);
			update.present(owner);
			Record markerPrereq = buildDynamicMarkerRecord(owner, 0);
			update.present(markerPrereq);
			update.delete(owner, (inetAddr instanceof Inet6Address) ? Type.AAAA : Type.A);
			update.add(a_aaaa);
			update.add(markerRecord);

			if (log.isDebugEnabled()) {
				log.debug("Sending forward DDNS update (replace-dynamic exist/replace) to server=" + server + ":\n"
						+ update);
			} else if (log.isInfoEnabled()) {
				log.info("Sending forward DDNS update (replace-dynamic exist/replace): " + a_aaaa);
			}
			response = res.send(update);
			if (response.getRcode() == Rcode.NOERROR) {
				log.info("Forward DDNS update (replace-dynamic exist/replace) succeeded: " + a_aaaa);
				return true;
			} else {
				log.error("Forward DDNS update (replace-dynamic exist/replace) rejected/failed (rcode=" +
						Rcode.string(response.getRcode()) + "): " + a_aaaa);
				return false;
			}
		} else {
			log.error("Forward DDNS update (replace-dynamic not-exist/add) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa);
			return false;
		}
	}

	private boolean sendAddUnconditional(Resolver res, Name zone, Name owner, Record a_aaaa) throws IOException {
		Record idRecord = buildIdentificationRecord(owner, ttl);
		Update update = new Update(zone);
		update.delete(owner, (inetAddr instanceof Inet6Address) ? Type.AAAA : Type.A);
		update.add(a_aaaa);
		update.delete(owner, (recordType == DdnsRecordType.TXT) ? Type.TXT : Type.DHCID);
		update.add(idRecord);

		if (log.isDebugEnabled()) {
			log.debug("Sending forward DDNS update (unconditional add) to server=" + server + ":\n" + update);
		} else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (unconditional add): " + a_aaaa);
		}
		Message response = res.send(update);
		if (response.getRcode() == Rcode.NOERROR) {
			log.info("Forward DDNS update (unconditional add) succeeded: " + a_aaaa);
			return true;
		} else {
			log.error("Forward DDNS update (unconditional add) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jagornet.dhcpv6.server.request.ddns.DdnsUpdate#sendDelete()
	 */
	@Override
	public boolean sendDelete() throws IOException {
		Resolver res = createResolver();
		Name owner = new Name(fqdn.endsWith(".") ? fqdn : fqdn + ".");
		Record a_aaaa = (inetAddr instanceof Inet6Address)
				? new AAAARecord(owner, DClass.IN, 0, inetAddr)
				: new ARecord(owner, DClass.IN, 0, inetAddr);
		Name _zone = buildZoneName(fqdn);

		switch (conflictStrategy) {
			case UNCONDITIONAL:
				return sendDeleteUnconditional(res, _zone, owner, a_aaaa);
			case REPLACE_DYNAMIC:
				return sendDeleteReplaceDynamic(res, _zone, owner, a_aaaa);
			case STRICT:
			default:
				return sendDeleteStrict(res, _zone, owner, a_aaaa);
		}
	}

	private boolean sendDeleteStrict(Resolver res, Name zone, Name owner, Record a_aaaa) throws IOException {
		Record idPrereq = buildClientRecord(owner, 0);
		Update update = new Update(zone);
		update.present(idPrereq);
		update.delete(a_aaaa);

		if (log.isDebugEnabled()) {
			log.debug("Sending forward DDNS update (strict exist/delete) to server=" + server + ":\n" + update);
		} else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (strict exist/delete): " + a_aaaa);
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			update = new Update(zone);
			update.present(idPrereq);
			update.absent(owner, Type.A);
			update.absent(owner, Type.AAAA);
			update.delete(owner);

			if (log.isDebugEnabled()) {
				log.debug("Sending forward DDNS update (strict not-exist/delete) to server=" + server + ":\n" + update);
			} else if (log.isInfoEnabled()) {
				log.info("Sending forward DDNS update (strict not-exist/delete): " + owner);
			}
			response = res.send(update);
			if (response.getRcode() == Rcode.NOERROR) {
				log.info("Forward DDNS update (strict not-exist/delete) succeeded: " + owner);
			} else {
				log.error("Forward DDNS update (strict not-exist/delete) failed (rcode=" +
						Rcode.string(response.getRcode()) + "): " + owner);
			}
			return true;
		} else {
			log.error("Forward DDNS update (strict exist/delete) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa);
			return false;
		}
	}

	private boolean sendDeleteReplaceDynamic(Resolver res, Name zone, Name owner, Record a_aaaa) throws IOException {
		Record markerPrereq = buildDynamicMarkerRecord(owner, 0);
		Update update = new Update(zone);
		update.present(markerPrereq);
		update.delete(a_aaaa);

		if (log.isDebugEnabled()) {
			log.debug(
					"Sending forward DDNS update (replace-dynamic exist/delete) to server=" + server + ":\n" + update);
		} else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (replace-dynamic exist/delete): " + a_aaaa);
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			update = new Update(zone);
			update.present(markerPrereq);
			update.absent(owner, Type.A);
			update.absent(owner, Type.AAAA);
			update.delete(owner);

			if (log.isDebugEnabled()) {
				log.debug("Sending forward DDNS update (replace-dynamic not-exist/delete) to server=" + server + ":\n"
						+ update);
			} else if (log.isInfoEnabled()) {
				log.info("Sending forward DDNS update (replace-dynamic not-exist/delete): " + owner);
			}
			response = res.send(update);
			if (response.getRcode() == Rcode.NOERROR) {
				log.info("Forward DDNS update (replace-dynamic not-exist/delete) succeeded: " + owner);
			} else {
				log.error("Forward DDNS update (replace-dynamic not-exist/delete) failed (rcode=" +
						Rcode.string(response.getRcode()) + "): " + owner);
			}
			return true;
		} else {
			log.error("Forward DDNS update (replace-dynamic exist/delete) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa);
			return false;
		}
	}

	private boolean sendDeleteUnconditional(Resolver res, Name zone, Name owner, Record a_aaaa) throws IOException {
		Update update = new Update(zone);
		update.delete(a_aaaa);

		if (log.isDebugEnabled()) {
			log.debug("Sending forward DDNS update (unconditional delete) to server=" + server + ":\n" + update);
		} else if (log.isInfoEnabled()) {
			log.info("Sending forward DDNS update (unconditional delete): " + a_aaaa);
		}
		Message response = res.send(update);

		if (response.getRcode() == Rcode.NOERROR) {
			update = new Update(zone);
			update.absent(owner, Type.A);
			update.absent(owner, Type.AAAA);
			update.delete(owner);

			if (log.isDebugEnabled()) {
				log.debug("Sending forward DDNS update (unconditional not-exist/delete) to server=" + server + ":\n"
						+ update);
			} else if (log.isInfoEnabled()) {
				log.info("Sending forward DDNS update (unconditional not-exist/delete): " + owner);
			}
			response = res.send(update);
			if (response.getRcode() == Rcode.NOERROR) {
				log.info("Forward DDNS update (unconditional not-exist/delete) succeeded: " + owner);
			}
			return true;
		} else {
			log.error("Forward DDNS update (unconditional delete) failed (rcode=" +
					Rcode.string(response.getRcode()) + "): " + a_aaaa);
			return false;
		}
	}

	/**
	 * Builds the zone name using the updater's FQDN.
	 *
	 * @return the zone Name
	 * @throws TextParseException
	 */
	public Name buildZoneName() throws TextParseException {
		return buildZoneName(fqdn);
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
	public Name buildZoneName(String fqdn) throws TextParseException {
		Name _zone = null;
		if (zone != null) {
			_zone = new Name(zone.endsWith(".") ? zone : zone + ".");
		} else {
			int p = fqdn.indexOf('.');
			String z = (p >= 0) ? fqdn.substring(p + 1) : fqdn;
			_zone = new Name(z.endsWith(".") ? z : z + ".");
		}
		return _zone;
	}
}
