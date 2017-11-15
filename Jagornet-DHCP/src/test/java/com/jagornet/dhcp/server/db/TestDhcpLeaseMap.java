package com.jagornet.dhcp.server.db;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.IdentityAssoc;

import junit.framework.TestCase;

public class TestDhcpLeaseMap extends TestCase {
	
	public void testSize() {

		InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName("10.0.0.0");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Runtime rt = Runtime.getRuntime();
		long startingUsedMemory = rt.totalMemory() - rt.freeMemory();
		System.out.println("Starting used memory: " + startingUsedMemory);
		
		Map<InetAddress, DhcpLease> leaseMap = new HashMap<InetAddress, DhcpLease>();
		for (long i=0; i<1000000; i++) {
			BigInteger bi = BigInteger.valueOf(i);
			try {
				inetAddr = InetAddress.getByAddress(new BigInteger(inetAddr.getAddress()).add(BigInteger.ONE).toByteArray());
				DhcpLease lease = new DhcpLease();
				lease.setIpAddress(inetAddr);
				lease.setDuid(bi.toByteArray());
				lease.setIaid(0);
				lease.setIatype(IdentityAssoc.V4_TYPE);
				lease.setStartTime(new Date());
				lease.setPreferredEndTime(new Date());
				lease.setValidEndTime(new Date());
				lease.setState(IdentityAssoc.ADVERTISED);
				leaseMap.put(inetAddr, lease);
				long usedMemory = rt.totalMemory() - rt.freeMemory();
				if (i % 1000 == 0) {
					usedMemory = usedMemory - startingUsedMemory;
					System.out.println("Used memory for " + i + " leases: " + usedMemory);
					if (i > 0) {
						System.out.println("Average size per lease: " + (usedMemory/i));
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}

}
