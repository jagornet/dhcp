package com.jagornet.dhcp.server.rest.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.IaManager;
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
	
	public List<InetAddress> getAllLeaseIPs() {
		List<InetAddress> ips = null;
		try {
			//TODO: check/fix this hack that gets all IPs?
			ips = leaseManager.findExistingLeaseIPs(InetAddress.getByName("0.0.0.0"), 
					InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
		} catch (UnknownHostException e) {
			log.error("Exception getting all leases: " + e);
		}
		return ips;
	}
	
	public DhcpLease createDhcpLease(DhcpLease dhcpLease) {
		leaseManager.insertDhcpLease(dhcpLease);
		return dhcpLease;	// or getDhcpLease(dhcpLease.getIpAddress)
	}

	public DhcpLease getDhcpLease(InetAddress ipAddress) {
		return leaseManager.findDhcpLeaseForInetAddr(ipAddress);
	}

	public void updateDhcpLease(InetAddress ipAddress, DhcpLease dhcpLease) {
		if (Util.compareInetAddrs(ipAddress, dhcpLease.getIpAddress()) == 0) {
			leaseManager.updateDhcpLease(dhcpLease);
		}
		else {
			log.error("Update failed: cannot change IP=" + ipAddress.getHostAddress() +
						" to IP=" + dhcpLease.getIpAddress().getHostAddress());
		}
	}

	public void deleteDhcpLease(InetAddress ipAddress) {
		DhcpLease dhcpLease = getDhcpLease(ipAddress);
		if (dhcpLease != null) {
			leaseManager.deleteDhcpLease(dhcpLease);
		}
		else {
			log.error("Delete failed: no lease found for IP=" + ipAddress.getHostAddress());
		}
	}

}
