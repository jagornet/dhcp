package com.jagornet.dhcpv6.server.request.ddns;

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
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;

public class ForwardDdnsUpdate extends DdnsUpdate 
{
	private static Logger log = LoggerFactory.getLogger(ForwardDdnsUpdate.class);

	public ForwardDdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid) throws Exception
	{
		super(fqdn, inetAddr, duid);
	}
	
	public void sendAdd() throws Exception
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
	
	public void sendDelete() throws Exception
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
	
	private Name buildZoneName(String fqdn) throws Exception
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
