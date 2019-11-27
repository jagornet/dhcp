package com.jagornet.dhcp.server.db;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.request.binding.Range;

public class RandomAccessFileLeaseManager extends LeaseManager {
	
	private static Logger log = LoggerFactory.getLogger(RandomAccessFileLeaseManager.class);

	private static final Path LEASES_FILENAME = Paths.get(DhcpConstants.JAGORNET_DHCP_HOME +
			File.separatorChar + "db/jagornet-dhcp.leases");
	
	private static RandomAccessFile leasesFile = null;
	private static ConcurrentHashMap<InetAddress, Long> leasesMap = null;
	
	@Override
	public void init() throws Exception {
		/*
		 * The "rws" and "rwd" modes work much like the force(boolean) method of the 
		 * FileChannel class, passing arguments of true and false, respectively, except 
		 * that they always apply to every I/O operation and are therefore often more 
		 * efficient. If the file resides on a local storage device then when an invocation 
		 * of a method of this class returns it is guaranteed that all changes made to the 
		 * file by that invocation will have been written to that device. This is useful 
		 * for ensuring that critical information is not lost in the event of a system 
		 * crash. If the file does not reside on a local device then no such guarantee is 
		 * made.
		 * The "rwd" mode can be used to reduce the number of I/O operations performed. 
		 * Using "rwd" only requires updates to the file's content to be written to storage; 
		 * using "rws" requires updates to both the file's content and its metadata to be 
		 * written, which generally requires at least one more low-level I/O operation.
		 */
		leasesFile = new RandomAccessFile(LEASES_FILENAME.toFile(), "rws");
		leasesMap = new ConcurrentHashMap<InetAddress, Long>();
		long leasePointer = leasesFile.getFilePointer(); 	// zero
		String jsonLease = leasesFile.readLine();
		while (jsonLease != null) {
			DhcpLease dhcpLease = DhcpLease.fromJson(jsonLease);
			if (dhcpLease != null) {
				leasesMap.put(dhcpLease.getIpAddress(), leasePointer);
			}
			else {
				log.error("Failed to build DhcpLease from JSON: " + jsonLease);
			}
			leasePointer = leasesFile.getFilePointer();
			jsonLease = leasesFile.readLine();
		}
		log.info("Leases map size: " + leasesMap.size());
	}

	@Override
	public void insertDhcpLease(DhcpLease lease) {
		if (leasesMap.containsKey(lease.getIpAddress())) {
			log.warn("Leases map already contains IP: " + lease.getIpAddress().getHostAddress());
			updateDhcpLease(lease);
		}
		else {
			
		}
	}

	@Override
	public void updateDhcpLease(DhcpLease lease) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteDhcpLease(DhcpLease lease) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateIpAddress(InetAddress inetAddr, byte state, short prefixlen, Date start, Date preferred,
			Date valid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIpAddress(InetAddress inetAddr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateIaOptions(InetAddress inetAddr, Collection<DhcpOption> iaOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateIpAddrOptions(InetAddress inetAddr, Collection<DhcpOption> ipAddrOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DhcpLease> findDhcpLeasesForIA(byte[] duid, byte iatype, long iaid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DhcpLease findDhcpLeaseForInetAddr(InetAddress inetAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<InetAddress> findExistingLeaseIPs(InetAddress startAddr, InetAddress endAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DhcpLease> findUnusedLeases(InetAddress startAddr, InetAddress endAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DhcpLease findUnusedLease(InetAddress startAddr, InetAddress endAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DhcpLease> findExpiredLeases(byte iatype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reconcileLeases(List<Range> ranges) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllLeases() {
		// TODO Auto-generated method stub

	}

}
