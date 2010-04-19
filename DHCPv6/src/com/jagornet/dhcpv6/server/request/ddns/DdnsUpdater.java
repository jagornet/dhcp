/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DdnsUpdater.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.AddressBindingPool;
import com.jagornet.dhcpv6.server.request.binding.BindingAddress;
import com.jagornet.dhcpv6.xml.Link;

/**
 * The Class DdnsUpdater.
 * 
 * @author A. Gregory Rabil
 */
public class DdnsUpdater implements Runnable
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DdnsUpdater.class);

	/** The executor. */
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	/** The sync. */
	private boolean sync;
	
	/** The fwd zone. */
	private String fwdZone;
	
	/** The fwd ttl. */
	private long fwdTtl;
	
	/** The fwd server. */
	private String fwdServer;
	
	/** The fwd tsig key name. */
	private String fwdTsigKeyName;
	
	/** The fwd tsig algorithm. */
	private String fwdTsigAlgorithm;
	
	/** The fwd tsig key data. */
	private String fwdTsigKeyData;
	
	/** The rev zone. */
	private String revZone;
	
	/** The rev zone bit length. */
	private int revZoneBitLength;
	
	/** The rev ttl. */
	private long revTtl;
	
	/** The rev server. */
	private String revServer;
	
	/** The rev tsig key name. */
	private String revTsigKeyName;
	
	/** The rev tsig algorithm. */
	private String revTsigAlgorithm;
	
	/** The rev tsig key data. */
	private String revTsigKeyData;
	
	private DhcpMessage requestMsg;
	
	/** The client link. */
	private Link clientLink;
	
	/** The binding addr. */
	private BindingAddress bindingAddr;
	
	/** The fqdn. */
	private String fqdn;
	
	/** The duid. */
	private byte[] duid;
	
	/** The do forward update. */
	private boolean doForwardUpdate;
	
	/** The is delete. */
	private boolean isDelete;

	/**
	 * Instantiates a new ddns updater.
	 * 
	 * @param clientLink the client link
	 * @param bindingAddr the binding addr
	 * @param fqdn the fqdn
	 * @param duid the duid
	 * @param doForwardUpdate the do forward update
	 * @param isDelete the is delete
	 */
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
	
	/**
	 * Instantiates a new ddns updater.
	 *
	 * @param requestMsg the request msg
	 * @param clientLink the client link
	 * @param bindingAddr the binding addr
	 * @param fqdn the fqdn
	 * @param doForwardUpdate the do forward update
	 * @param isDelete the is delete
	 */
	public DdnsUpdater(DhcpMessage requestMsg, Link clientLink, BindingAddress bindingAddr, 
			String fqdn, boolean doForwardUpdate, boolean isDelete)
	{
		this.requestMsg = requestMsg;
		this.duid = requestMsg.getDhcpClientIdOption().getDuid();
		this.clientLink = clientLink;
		this.bindingAddr = bindingAddr;
		this.fqdn = fqdn;
		this.doForwardUpdate = doForwardUpdate;
		this.isDelete = isDelete;
	}
	
	/**
	 * Process updates.
	 */
	public void processUpdates()
	{
		if (sync) {
			run();
		}
		else {
			executor.submit(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
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
	
	/**
	 * Sets the up policies.
	 * 
	 * @param addrBindingPool the new up policies
	 */
	private void setupPolicies(AddressBindingPool addrBindingPool)
	{
		sync = DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_SYNCHRONIZE);
		
		String zone = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_FORWARD_ZONE_NAME);
		if ((zone != null) && !zone.isEmpty())
			fwdZone = zone;
		
		zone = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_NAME);
		if ((zone != null) && !zone.isEmpty())
			revZone = zone;
		
		revZoneBitLength = DhcpServerPolicies.effectivePolicyAsInt(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_BITLENGTH);
		
		long ttl = 0;
		float ttlFloat = DhcpServerPolicies.effectivePolicyAsFloat(requestMsg,
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_TTL);
		if (ttlFloat < 1) {
			// if less than one, then percentage of lifetime seconds
			ttl = (long) (addrBindingPool.getValidLifetime() * ttlFloat);
		}
		else {
			// if greater than one, then absolute number of seconds
			ttl = (long) ttlFloat;
		}
		
		fwdTtl = ttl;
		ttlFloat = DhcpServerPolicies.effectivePolicyAsFloat(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_FORWARD_ZONE_TTL);
		if (ttlFloat < 1) {
			// if less than one, then percentage of lifetime seconds
			fwdTtl = (long) (addrBindingPool.getValidLifetime() * ttlFloat);
		}
		else {
			// if greater than one, then absolute number of seconds
			fwdTtl = (long) ttlFloat;
		}
		
		revTtl = ttl;
		ttlFloat = DhcpServerPolicies.effectivePolicyAsFloat(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_TTL);
		if (ttlFloat < 1) {
			// if less than one, then percentage of lifetime seconds
			revTtl = (long) (addrBindingPool.getValidLifetime() * ttlFloat);
		}
		else {
			// if greater than one, then absolute number of seconds
			revTtl = (long) ttlFloat;
		}

		String server = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_SERVER);
		
		fwdServer = server;
		revServer = server;
		
		server = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_FORWARD_ZONE_SERVER);
		if ((server != null) && !server.isEmpty())
			fwdServer = server;
		server = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_SERVER);
		if ((server != null) && !server.isEmpty())
			revServer = server;
		
		String tsigKeyName = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_TSIG_KEYNAME);

		fwdTsigKeyName = tsigKeyName;
		revTsigKeyName = tsigKeyName;

		tsigKeyName = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_FORWARD_ZONE_TSIG_KEYNAME);
		if ((tsigKeyName != null) && !tsigKeyName.isEmpty())
			fwdTsigKeyName = tsigKeyName;
		tsigKeyName = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_TSIG_KEYNAME);
		if ((tsigKeyName != null) && !tsigKeyName.isEmpty())
			revTsigKeyName = tsigKeyName;
		
		String tsigAlgorithm = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_TSIG_ALGORITHM);
		
		fwdTsigAlgorithm = tsigAlgorithm;
		revTsigAlgorithm = tsigAlgorithm;

		tsigAlgorithm = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_FORWARD_ZONE_TSIG_ALGORITHM);
		if ((tsigAlgorithm != null) && !tsigAlgorithm.isEmpty())
			fwdTsigAlgorithm = tsigAlgorithm;
		tsigAlgorithm = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_TSIG_ALGORITHM);
		if ((tsigAlgorithm != null) && !tsigAlgorithm.isEmpty())
			revTsigAlgorithm = tsigAlgorithm;
		
		String tsigKeyData = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_TSIG_KEYDATA);
		
		fwdTsigKeyData = tsigKeyData;
		revTsigKeyData = tsigKeyData;
		
		tsigKeyData = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_FORWARD_ZONE_TSIG_KEYDATA);
		if ((tsigKeyData != null) && !tsigKeyData.isEmpty())
			fwdTsigKeyData = tsigKeyData;
		tsigKeyData = DhcpServerPolicies.effectivePolicy(requestMsg, 
				addrBindingPool.getAddressPool(), clientLink, Property.DDNS_REVERSE_ZONE_TSIG_KEYDATA);
		if ((tsigKeyData != null) && !tsigKeyData.isEmpty())
			revTsigKeyData = tsigKeyData;
	}	
}
