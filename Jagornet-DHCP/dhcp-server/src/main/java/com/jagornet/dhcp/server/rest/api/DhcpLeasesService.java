package com.jagornet.dhcp.server.rest.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseCallbackHandler;
import com.jagornet.dhcp.server.db.IaManager;
import com.jagornet.dhcp.server.db.InetAddressCallbackHandler;
import com.jagornet.dhcp.server.db.LeaseManager;

public class DhcpLeasesService {
	
	private static Logger log = LoggerFactory.getLogger(DhcpLeasesService.class);

	private LeaseManager leaseManager;
	
	public DhcpLeasesService() {
		IaManager iaMgr = DhcpServerConfiguration.getInstance().getIaMgr();
		if (iaMgr instanceof LeaseManager) {
			leaseManager = (LeaseManager) iaMgr;
		}
		else {
			throw new IllegalStateException("IaManager must be LeaseManager type");
		}
	}
	
	private InetAddress createStartIp(String start) throws UnknownHostException {
		return (start != null) ? 
				InetAddress.getByName(start) :
				InetAddress.getByName("0.0.0.0");
	}
	
	private InetAddress createEndIp(String end) throws UnknownHostException {
		return (end != null) ? 
				InetAddress.getByName(end):
				InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
	}
	
	public List<InetAddress> getAllLeaseIPs() {
		List<InetAddress> ips = null;
		try {
			ips = getRangeLeaseIPs(null, null);
		} 
		catch (UnknownHostException e) {
			log.error("Exception getting all lease ips: " + e);
		}
		return ips;
	}
	
	public List<InetAddress> getRangeLeaseIPs(String start, String end) 
			throws UnknownHostException {
		InetAddress startIp = createStartIp(start);
		InetAddress endIp = createEndIp(end);
		List<InetAddress> ips = leaseManager.findExistingLeaseIPs(startIp, endIp); 
		if (ips != null) {
			log.info("Found " + ips.size() + " existing leases in range: start=" +
					startIp.getHostAddress() + " end=" + endIp.getHostAddress());
		}
		else {
			log.error("Failed to find any existing leases in range: start=" +
					 startIp.getHostAddress() + " end=" + endIp.getHostAddress());
		}
		return ips;
	}
	
	public void getAllLeaseIPs(InetAddressCallbackHandler inetAddressCallbackHandler) {
		try {
			getRangeLeaseIPs(null, null, inetAddressCallbackHandler);
		
		} catch (UnknownHostException e) {
			log.error("Exception getting all leases: " + e);
		}
	}
	
	public void getRangeLeaseIPs(String start, String end,
								 InetAddressCallbackHandler inetAddressCallbackHandler) 
		throws UnknownHostException {
		
		InetAddress startIp = createStartIp(start);
		InetAddress endIp = createEndIp(end);
		leaseManager.findExistingLeaseIPs(startIp, endIp, inetAddressCallbackHandler); 
	}
	
	public void getAllLeases(DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler) {
		try {
			InetAddress startIp = createStartIp(null);
			InetAddress endIp = createEndIp(null);
				getRangeLeases(startIp, endIp, dhcpLeaseCallbackHandler, false);
		
		} catch (UnknownHostException e) {
			log.error("Exception getting all leases: " + e);
		}
	}

	public void getRangeLeases(String start, String end,
			 				   DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler,
			 				   boolean unsyncedLeasesOnly)
		throws UnknownHostException {
			
		InetAddress startIp = createStartIp(start);
		InetAddress endIp = createEndIp(end);
		this.getRangeLeases(startIp, endIp, dhcpLeaseCallbackHandler, unsyncedLeasesOnly);
	}
	
	public void getRangeLeases(InetAddress startIp, InetAddress endIp,
			 				   DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler,
			 				   boolean unsyncedLeasesOnly) {
			
		if (unsyncedLeasesOnly) {
			log.info("Getting unsynced leases for IP range: " +
					 startIp.getHostAddress() + "-" + endIp.getHostAddress());
			leaseManager.findUnsyncedLeases(startIp, endIp, dhcpLeaseCallbackHandler);
		}
		else {
			// this peer received a request for all leases, which means the other
			// peer is initializing for the first time (or after catastrophic failure)
			// and has no lease information whatsoever, which means this peer should
			// automatically set all leases as unsynced, to safeguard against the
			// possibility that the other peer would fail in mid-initial sync and then
			// restart in an non-initial state, requesting only unsynced leases, but
			// actually was never "in sync" to begin with, so just start over to be sure
			//TODO: consider this as described above, but reset haPeerState per range?
//			log.info("Resetting haPeerState on all leases");
//			leaseManager.setAllLeasesUnsynced();
			log.info("Getting all existing leases for IP range: " +
					 startIp.getHostAddress() + "-" + endIp.getHostAddress());
			leaseManager.findExistingLeases(startIp, endIp, dhcpLeaseCallbackHandler);
		}
	}
	
	public boolean createOrUpdateDhcpLease(DhcpLease dhcpLease) {
		DhcpLease foundLease = getDhcpLease(dhcpLease.getIpAddress());
		if (foundLease == null) {
			return createDhcpLease(dhcpLease);
		}
		else {
			return updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
		}
	}
	public boolean createDhcpLease(DhcpLease dhcpLease) {
		log.info("Creating DhcpLease: " + dhcpLease);
		return (leaseManager.insertDhcpLease(dhcpLease) == 1);
	}

	public DhcpLease getDhcpLease(InetAddress ipAddress) {
		String ipStr = ipAddress.getHostAddress();
		log.info("Finding DhcpLease for IP=" + ipStr);
		DhcpLease dhcpLease = leaseManager.findDhcpLeaseForInetAddr(ipAddress);
		if (dhcpLease != null) {
			log.info("Found DhcpLease for IP=" + ipStr + ": " + dhcpLease);
		}
		else {
			log.info("No DhcpLease found for IP=" + ipStr);
		}
		return dhcpLease;
	}
	
	public boolean updateDhcpLease(InetAddress ipAddress, DhcpLease dhcpLease) {
		String ipStr = ipAddress.getHostAddress();
		if (Util.compareInetAddrs(ipAddress, dhcpLease.getIpAddress()) == 0) {
			log.info("Updating DhcpLease for IP=" + ipStr + ": " + dhcpLease);
			// TODO: handle error condition from update, or just bubble up?
			if (leaseManager.updateDhcpLease(dhcpLease) == 1) {
				return true;
			}
		}
		else {
			log.error("Update failed: cannot change IP=" + ipStr +
						" to IP=" + dhcpLease.getIpAddress().getHostAddress());
		}
		return false;
	}

	public boolean deleteDhcpLease(InetAddress ipAddress) {
		String ipStr = ipAddress.getHostAddress();
		log.info("Finding DhcpLease for IP=" + ipStr);
		DhcpLease dhcpLease = getDhcpLease(ipAddress);
		if (dhcpLease != null) {
			if (leaseManager.deleteDhcpLease(dhcpLease) == 1) {
				return true;
			}
		}
		else {
			log.error("Delete failed: no lease found for IP=" + ipStr);
		}
		return false;
	}

}
