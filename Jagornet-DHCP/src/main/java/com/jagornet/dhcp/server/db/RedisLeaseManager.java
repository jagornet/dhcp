package com.jagornet.dhcp.server.db;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

import com.jagornet.dhcp.server.request.binding.Range;
import com.jagornet.dhcp.util.Util;

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
	public void updateIaAddr(IaAddress iaAddr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIaAddr(IaAddress iaAddr) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<InetAddress> findExistingIPs(InetAddress startAddr, InetAddress endAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IaAddress> findUnusedIaAddresses(InetAddress startAddr, InetAddress endAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IaPrefix> findUnusedIaPrefixes(InetAddress startAddr, InetAddress endAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reconcileIaAddresses(List<Range> ranges) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllIAs() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void insertDhcpLease(DhcpLease lease) {
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
	protected void updateDhcpLease(DhcpLease lease) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void deleteDhcpLease(DhcpLease lease) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateIaOptions(InetAddress inetAddr, Collection<DhcpOption> iaOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateIpAddrOptions(InetAddress inetAddr, Collection<DhcpOption> ipAddrOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	protected List<DhcpLease> findDhcpLeasesForIA(byte[] duid, byte iatype, long iaid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DhcpLease findDhcpLeaseForInetAddr(InetAddress inetAddr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<DhcpLease> findExpiredLeases(byte iatype) {
		// TODO Auto-generated method stub
		return null;
	}

}
