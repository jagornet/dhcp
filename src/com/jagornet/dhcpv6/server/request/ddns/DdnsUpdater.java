package com.jagornet.dhcpv6.server.request.ddns;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.AddressBindingPool;
import com.jagornet.dhcpv6.server.request.binding.BindingAddress;
import com.jagornet.dhcpv6.xml.Link;

public class DdnsUpdater implements Runnable
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DdnsUpdater.class);

	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	private boolean sync;
	private String fwdZone;
	private long fwdTtl;
	private String fwdServer;
	private String fwdTsigKeyName;
	private String fwdTsigAlgorithm;
	private String fwdTsigKeyData;
	private String revZone;
	private int revZoneBitLength;
	private long revTtl;
	private String revServer;
	private String revTsigKeyName;
	private String revTsigAlgorithm;
	private String revTsigKeyData;
	
	private Link clientLink;
	private BindingAddress bindingAddr;
	private String fqdn;
	private byte[] duid;
	private boolean doForwardUpdate;
	private boolean isDelete;
	
	public DdnsUpdater(Link clientLink, BindingAddress bindingAddr, String fqdn, 
			byte[] duid, boolean doForwardUpdate, boolean isDelete)
	{
		this.clientLink = clientLink;
		this.bindingAddr = bindingAddr;
		this.fqdn = fqdn;
		this.duid = duid;
		this.doForwardUpdate = doForwardUpdate;
		this.isDelete = isDelete;
	}
	
	public void processUpdates()
	{
		if (sync) {
			run();
		}
		else {
			executor.submit(this);
		}
	}
	
	public void run()
	{
		setupPolicies((AddressBindingPool) bindingAddr.getBindingPool());						
		InetAddress inetAddr = bindingAddr.getIpAddress();
		try {
			if (doForwardUpdate) {
				ForwardDdnsUpdate fwdUpdate = new ForwardDdnsUpdate(fqdn, inetAddr, duid);
				fwdUpdate.setServer(fwdServer);
				fwdUpdate.setZone(fwdZone);
				fwdUpdate.setTtl(fwdTtl);
				fwdUpdate.setTsigKeyName(fwdTsigKeyName);
				fwdUpdate.setTsigAlgorithm(fwdTsigAlgorithm);
				fwdUpdate.setTsigKeyData(fwdTsigKeyData);
				if (!isDelete)
					fwdUpdate.sendAdd();
				else
					fwdUpdate.sendDelete();
			}
			ReverseDdnsUpdate revUpdate = new ReverseDdnsUpdate(fqdn, inetAddr, duid);
			revUpdate.setServer(revServer);
			revUpdate.setZone(revZone);
			revUpdate.setRevZoneBitLength(revZoneBitLength);
			revUpdate.setTtl(revTtl);
			revUpdate.setTsigKeyName(revTsigKeyName);
			revUpdate.setTsigAlgorithm(revTsigAlgorithm);
			revUpdate.setTsigKeyData(revTsigKeyData);
			if (!isDelete)
				revUpdate.sendAdd();
			else
				revUpdate.sendDelete();
		}
		catch (Exception ex) {
			log.error("Failure performing DDNS updates", ex);
		}				
	}
	
	private void setupPolicies(AddressBindingPool addrBindingPool)
	{
		sync = DhcpServerPolicies.effectivePolicyAsBoolean(clientLink, Property.DDNS_SYNCHRONIZE);
		
		String zone = DhcpServerPolicies.effectivePolicy(clientLink, Property.DDNS_FORWARD_ZONE_NAME);
		if ((zone != null) && !zone.isEmpty())
			fwdZone = zone;
		
		zone = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_NAME);
		if ((zone != null) && !zone.isEmpty())
			revZone = zone;
		
		revZoneBitLength = DhcpServerPolicies.effectivePolicyAsInt(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_BITLENGTH);
		
		long ttl = 0;
		float ttlFloat = DhcpServerPolicies.effectivePolicyAsFloat(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_TTL);
		if (ttlFloat < 1) {
			// if less than one, then percentage of lifetime seconds
			ttl = (long) (addrBindingPool.getValidLifetime() * ttlFloat);
		}
		else {
			// if greater than one, then absolute number of seconds
			ttl = (long) ttlFloat;
		}
		
		fwdTtl = ttl;
		ttlFloat = DhcpServerPolicies.effectivePolicyAsFloat(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_FORWARD_ZONE_TTL);
		if (ttlFloat < 1) {
			// if less than one, then percentage of lifetime seconds
			fwdTtl = (long) (addrBindingPool.getValidLifetime() * ttlFloat);
		}
		else {
			// if greater than one, then absolute number of seconds
			fwdTtl = (long) ttlFloat;
		}
		
		revTtl = ttl;
		ttlFloat = DhcpServerPolicies.effectivePolicyAsFloat(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_TTL);
		if (ttlFloat < 1) {
			// if less than one, then percentage of lifetime seconds
			revTtl = (long) (addrBindingPool.getValidLifetime() * ttlFloat);
		}
		else {
			// if greater than one, then absolute number of seconds
			revTtl = (long) ttlFloat;
		}

		String server = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_SERVER);
		
		fwdServer = server;
		revServer = server;
		
		server = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_FORWARD_ZONE_SERVER);
		if ((server != null) && !server.isEmpty())
			fwdServer = server;
		server = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_SERVER);
		if ((server != null) && !server.isEmpty())
			revServer = server;
		
		String tsigKeyName = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_TSIG_KEYNAME);

		fwdTsigKeyName = tsigKeyName;
		revTsigKeyName = tsigKeyName;

		tsigKeyName = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_FORWARD_ZONE_TSIG_KEYNAME);
		if ((tsigKeyName != null) && !tsigKeyName.isEmpty())
			fwdTsigKeyName = tsigKeyName;
		tsigKeyName = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_TSIG_KEYNAME);
		if ((tsigKeyName != null) && !tsigKeyName.isEmpty())
			revTsigKeyName = tsigKeyName;
		
		String tsigAlgorithm = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_TSIG_ALGORITHM);
		
		fwdTsigAlgorithm = tsigAlgorithm;
		revTsigAlgorithm = tsigAlgorithm;

		tsigAlgorithm = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_FORWARD_ZONE_TSIG_ALGORITHM);
		if ((tsigAlgorithm != null) && !tsigAlgorithm.isEmpty())
			fwdTsigAlgorithm = tsigAlgorithm;
		tsigAlgorithm = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_TSIG_ALGORITHM);
		if ((tsigAlgorithm != null) && !tsigAlgorithm.isEmpty())
			revTsigAlgorithm = tsigAlgorithm;
		
		String tsigKeyData = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_TSIG_KEYDATA);
		
		fwdTsigKeyData = tsigKeyData;
		revTsigKeyData = tsigKeyData;
		
		tsigKeyData = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_FORWARD_ZONE_TSIG_KEYDATA);
		if ((tsigKeyData != null) && !tsigKeyData.isEmpty())
			fwdTsigKeyData = tsigKeyData;
		tsigKeyData = DhcpServerPolicies.effectivePolicy(addrBindingPool.getAddressPool(), 
				clientLink, Property.DDNS_REVERSE_ZONE_TSIG_KEYDATA);
		if ((tsigKeyData != null) && !tsigKeyData.isEmpty())
			revTsigKeyData = tsigKeyData;
	}	
}
