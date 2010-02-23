package com.jagornet.dhcpv6.server.request.ddns;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Update;

public class ReverseDdnsUpdate extends DdnsUpdate 
{
	private static Logger log = LoggerFactory.getLogger(ReverseDdnsUpdate.class);

	protected int revZoneBitLength = 64;	// default
	
	public ReverseDdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid) throws Exception
	{
		super(fqdn, inetAddr, duid);
	}
	
	public void sendAdd() throws Exception
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
	
	public void sendDelete() throws Exception
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
			revIp.append(".");
		}
		revIp.append("ip6.arpa.");
		return revIp.toString();
	}
	
	private Name buildZoneName(String revIp) throws Exception
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

	public int getRevZoneBitLength() {
		return revZoneBitLength;
	}

	public void setRevZoneBitLength(int revZoneBitLength) {
		this.revZoneBitLength = revZoneBitLength;
	}
}
