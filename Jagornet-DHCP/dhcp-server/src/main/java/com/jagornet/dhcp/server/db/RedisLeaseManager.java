package com.jagornet.dhcp.server.db;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.request.binding.Range;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
//import redis.clients.johm.JOhm;

public class RedisLeaseManager extends LeaseManager {
	
	JedisPool jedisPool;

	@Override
	public void init() throws Exception {
		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
//		jedisPool = new JedisPool(new GenericObjectPoolConfig(), "localhost");
//		JOhm.setPool(jedisPool);
	}

	@Override
	public void updateIpAddress(final InetAddress inetAddr, 
			final byte state, final short prefixlen,
			final Date start, final Date preferred, final Date valid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIpAddress(final InetAddress inetAddr) {
		// TODO Auto-generated method stub

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
	public void reconcileLeases(List<Range> ranges) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllLeases() {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertDhcpLease(DhcpLease lease) {
		String ip = lease.getIpAddress().getHostAddress();
		int expireSecs = (int)
				((lease.getValidEndTime().getTime() - lease.getStartTime().getTime()) / 1000);
		Transaction tx = jedisPool.getResource().multi();
		tx.setex(ip, expireSecs, lease.toJson());
		tx.zadd("tuple", 0, 
				Util.toHexString(lease.getDuid()) + "-" + 
						lease.iatype + "-" + lease.iaid + "-" + ip);
		tx.exec();
		//JOhm.expire(lease, seconds);
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
	public List<DhcpLease> findExpiredLeases(byte iatype) {
		// TODO Auto-generated method stub
		return null;
	}

}
