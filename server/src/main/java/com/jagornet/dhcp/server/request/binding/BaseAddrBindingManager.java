/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseAddrBindingManager.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.binding;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.db.IaAddress;
import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.util.Util;

/**
 * The Class BaseAddressBindingManager.
 * Second-level abstract class that extends BaseBindingManager to
 * add behavior specific to address bindings.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseAddrBindingManager extends BaseBindingManager
{
	private static Logger log = LoggerFactory.getLogger(BaseAddrBindingManager.class);
    
	public BaseAddrBindingManager()
	{
		super();
	}
    
	protected void startReaper()
	{
		//TODO: separate properties for address/prefix binding managers?
		long reaperStartupDelay = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_STARTUP_DELAY);
		long reaperRunPeriod =
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_RUN_PERIOD);

		reaper = new Timer("BindingReaper");
		reaper.schedule(new ReaperTimerTask(), reaperStartupDelay, reaperRunPeriod);
	}
    
    /**
     * Perform the DDNS delete processing when a lease is released or expired.
     * 
     * @param iaAddr the released or expired IaAddress 
     */
    protected abstract void ddnsDelete(IdentityAssoc ia, IaAddress iaAddr);
    
    /**
     * Return the IA type for this binding.  This is a hack to allow consolidated
     * code in this base class (i.e. expireAddresses) for use by the subclasses.
     * 
     * @return
     */
    protected abstract byte getIaType();
	
    /**
     * Release an IaAddress.  If policy dictates, the address will be deleted,
     * otherwise the state will be marked as released instead.  In either case,
     * a DDNS delete will be issued for the address binding.
     * 
     * @param iaAddr the IaAddress to be released
     */
	public void releaseIaAddress(IdentityAssoc ia, IaAddress iaAddr)
	{
		try {
			log.info("Releasing address: " + iaAddr.getIpAddress().getHostAddress());
			ddnsDelete(ia, iaAddr);
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				iaMgr.deleteIaAddr(iaAddr);
				// free the address only if it is deleted from the db,
				// otherwise, we will get a unique constraint violation
				// if another client obtains this released IP address
				freeAddress(iaAddr.getIpAddress());
			}
			else {
				iaAddr.setStartTime(null);
				iaAddr.setPreferredEndTime(null);
				iaAddr.setValidEndTime(null);
				iaAddr.setState(IaAddress.RELEASED);
				iaMgr.updateIaAddr(iaAddr);
				log.info("Address released: " + iaAddr.toString());
			}
		}
		catch (Exception ex) {
			log.error("Failed to release address", ex);
		}
	}
	
	/**
	 * Decline an IaAddress.  This is done when the client declines an address.
	 * Perform a DDNS delete just in case it was already registered, then mark
	 * the address as declined (unavailable).
	 * 
	 * @param iaAddr the declined IaAddress.
	 */
	public void declineIaAddress(IdentityAssoc ia, IaAddress iaAddr)
	{
		try {
			log.info("Declining address: " + iaAddr.getIpAddress().getHostAddress());
			ddnsDelete(ia, iaAddr);
			iaAddr.setStartTime(null);
			iaAddr.setPreferredEndTime(null);
			iaAddr.setValidEndTime(null);
			iaAddr.setState(IaAddress.DECLINED);
			iaMgr.updateIaAddr(iaAddr);
			log.info("Address declined: " + iaAddr.toString());
		}
		catch (Exception ex) {
			log.error("Failed to decline address", ex);
		}
	}
	
	/**
	 * Callback from the ExpireTimerTask started when the lease was granted.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void expireIaAddress(IdentityAssoc ia, IaAddress iaAddr)
	{
		try {
			log.info("Expiring: " + iaAddr.toString());
			ddnsDelete(ia, iaAddr);
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				log.debug("Deleting expired address: " + iaAddr.getIpAddress());
				iaMgr.deleteIaAddr(iaAddr);
				// free the address only if it is deleted from the db,
				// otherwise, we will get a unique constraint violation
				// if another client obtains this released IP address
				freeAddress(iaAddr.getIpAddress());
			}
			else {
				iaAddr.setStartTime(null);
				iaAddr.setPreferredEndTime(null);
				iaAddr.setValidEndTime(null);
				iaAddr.setState(IaAddress.EXPIRED);
				log.debug("Updating expired address: " + iaAddr.getIpAddress());
				iaMgr.updateIaAddr(iaAddr);
			}
		}
		catch (Exception ex) {
			log.error("Failed to expire address", ex);
		}
	}
	
	/**
	 * Callback from the ReaperTimerTask started when the BindingManager initialized.
	 * Find any expired addresses as of now, and expire them already.
	 */
	public void expireAddresses()
	{
		List<IdentityAssoc> expiredIAs = iaMgr.findExpiredIAs(getIaType());
		if ((expiredIAs != null) && !expiredIAs.isEmpty()) {
			log.info("Found " + expiredIAs.size() + " expired bindings of type: " + 
					IdentityAssoc.iaTypeToString(getIaType()));
			for (IdentityAssoc ia : expiredIAs) {
				Collection<? extends IaAddress> expiredAddrs = ia.getIaAddresses();
				if ((expiredAddrs != null) && !expiredAddrs.isEmpty()) {
					// due to the implementation of findExpiredIAs, each IdentityAssoc
					// SHOULD have only one IaAddress within it to be expired
					log.info("Found " + expiredAddrs.size() + " expired bindings for IA: " + 
							"duid=" + Util.toHexString(ia.getDuid()) + " iaid=" + ia.getIaid());
					for (IaAddress iaAddress : expiredAddrs) {
						expireIaAddress(ia, iaAddress);
					}
				}
			}
		}
	}
	
	/**
	 * The Class ReaperTimerTask.
	 */
	class ReaperTimerTask extends TimerTask
	{		
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
//			log.debug("Expiring addresses...");
			expireAddresses();
		}
	}
}
