/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file MongoLeaseManager.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.Range;
import com.jagornet.dhcp.util.DhcpConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * The MongoLeaseManager implementation class for the IaManager interface.
 * This is the main database access class for handling client bindings.
 * 
 * @author A. Gregory Rabil
 */
public class MongoLeaseManager extends LeaseManager
{	
	private static Logger log = LoggerFactory.getLogger(MongoLeaseManager.class);

	private Mongo mongoClient;
	private DB database;
	private DBCollection dhcpLeases;
	
	// Spring bean init-method
	public void init() throws Exception {
		mongoClient = new Mongo(getMongoServer());
		mongoClient.setWriteConcern(WriteConcern.SAFE);		// throw exceptions on failed write
		database = mongoClient.getDB("jagornet-dhcpv6");
		log.info("Connected to jagornet-dhcpv6 via Mongo client: " + mongoClient.toString());
		dhcpLeases = database.getCollection("DHCPLEASE");
		dhcpLeases.ensureIndex(new BasicDBObject("ipAddress", 1), "pkey", true);
		dhcpLeases.ensureIndex(new BasicDBObject("duid", 1)
									.append("iatype", 1)
									.append("iaid", 1),
									"tuple", false);
		dhcpLeases.ensureIndex("duid");
		dhcpLeases.ensureIndex("iatype");
		dhcpLeases.ensureIndex("state");
		dhcpLeases.ensureIndex("validEndTime");
	}
	
	protected ServerAddress getMongoServer() throws Exception {
		ServerAddress serverAddress = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(DhcpConstants.JAGORNET_DHCP_HOME + File.separator + 
										"conf/mongo.properties");
			Properties props = new Properties();
			props.load(fis);
			String mongoPrimary = props.getProperty("mongo.primary");
			if (mongoPrimary != null) {
				String[] server = mongoPrimary.split(":");
				if (server != null) {
					if (server.length > 1) {
						int port = Integer.parseInt(server[1]);
						serverAddress = new ServerAddress(server[0], port);
					}
					else {
						serverAddress = new ServerAddress(server[0]);
					}
				}
				else {
					serverAddress = new ServerAddress();	// fallback to default
				}
			}
			else {
				serverAddress = new ServerAddress();	// fallback to default
			}
		}
		finally {
			if (fis != null) try { fis.close(); } catch (IOException e) { }
		}
		return serverAddress;
	}
	
	protected DBObject convertDhcpLease(final DhcpLease lease) {
		DBObject dbObj = null;
		if (lease != null) {
			dbObj = new BasicDBObject("ipAddress", lease.getIpAddress().getAddress()).
							append("duid", lease.getDuid()).
							append("iatype", lease.getIatype()).
							append("iaid", lease.getIaid()).
							append("prefixLength", lease.getPrefixLength()).
							append("state", lease.getState()).
							append("startTime", lease.getStartTime()).
							append("preferredEndTime", lease.getPreferredEndTime()).
							append("validEndTime", lease.getValidEndTime()).
							append("iaDhcpOptions", convertDhcpOptions(lease.getIaDhcpOptions())).
							append("iaAddrDhcpOptions", convertDhcpOptions(lease.getIaAddrDhcpOptions()));
		}
		return dbObj;
	}
	
	protected DBObject convertDhcpOptions(final Collection<DhcpOption> dhcpOptions) {
		DBObject dbObj = null;
		if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
			BasicDBList dbList = new BasicDBList();
			for (DhcpOption dhcpOption : dhcpOptions) {
				dbList.add(new BasicDBObject("code", dhcpOption.getCode()).
								append("value", dhcpOption.getValue()));
			}
			dbObj = dbList;
		}
		return dbObj;
	}
	
	protected DhcpLease convertDBObject(DBObject dbObj) {
		DhcpLease dhcpLease = null;
		if (dbObj != null) {
			dhcpLease = new DhcpLease();
			try {
				dhcpLease.setIpAddress(InetAddress.getByAddress((byte[])dbObj.get("ipAddress")));
				dhcpLease.setDuid((byte[])dbObj.get("duid"));
				dhcpLease.setIatype(((Integer)dbObj.get("iatype")).byteValue());
				dhcpLease.setIaid(((Long)dbObj.get("iaid")).longValue());
				dhcpLease.setPrefixLength(((Integer)dbObj.get("prefixLength")).shortValue());
				dhcpLease.setState(((Integer)dbObj.get("state")).byteValue());
				dhcpLease.setStartTime((Date)dbObj.get("startTime"));
				dhcpLease.setPreferredEndTime((Date)dbObj.get("preferredEndTime"));
				dhcpLease.setValidEndTime((Date)dbObj.get("validEndTime"));
				dhcpLease.setIaDhcpOptions(convertDBList((BasicDBList)dbObj.get("iaDhcpOptions")));
				dhcpLease.setIaAddrDhcpOptions(convertDBList((BasicDBList)dbObj.get("iaAddrDhcpOptions")));
			} 
			catch (Exception e) {
				log.error("Failed to convert DBObject: " + e);
			}
		}
		return dhcpLease;
	}
	
	protected Collection<DhcpOption> convertDBList(BasicDBList dbList) {
		List<DhcpOption> dhcpOptions = null;
		if ((dbList != null) && !dbList.isEmpty()) {
			dhcpOptions = new ArrayList<DhcpOption>();
			for (Object obj : dbList) {
				DBObject dbObj = (DBObject)obj;
				DhcpOption dhcpOption = new DhcpOption();
				dhcpOption.setCode(((Integer)dbObj.get("code")).intValue());
				dhcpOption.setValue((byte[])dbObj.get("value"));
				dhcpOptions.add(dhcpOption);
			}
		}
		return dhcpOptions;
	}
	
	protected DBObject ipAddressQuery(InetAddress inetAddr) {
		return new BasicDBObject("ipAddress", inetAddr.getAddress());
	}
	
	/**
	 * Insert dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void insertDhcpLease(final DhcpLease lease)
	{
		dhcpLeases.insert(convertDhcpLease(lease));
	}
	
	/**
	 * Update dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void updateDhcpLease(final DhcpLease lease)
	{ 
		dhcpLeases.update(ipAddressQuery(lease.getIpAddress()), 
										convertDhcpLease(lease));
	}
	
	/**
	 * Delete dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void deleteDhcpLease(final DhcpLease lease)
	{
		dhcpLeases.remove(ipAddressQuery(lease.getIpAddress()));
	}
	
	/**
	 * Update ia options.
	 */
	protected void updateIaOptions(final InetAddress inetAddr, 
									final Collection<DhcpOption> iaOptions)
	{
		DBObject update = new BasicDBObject("$set",
				new BasicDBObject("iaDhcpOptions", convertDhcpOptions(iaOptions)));
		dhcpLeases.update(ipAddressQuery(inetAddr), update);
  	}
	
	/**
	 * Update ipaddr options.
	 */
	protected void updateIpAddrOptions(final InetAddress inetAddr,
									final Collection<DhcpOption> ipAddrOptions)
	{
		DBObject update = new BasicDBObject("$set", 
				new BasicDBObject("iaAddrDhcpOptions", convertDhcpOptions(ipAddrOptions)));
		dhcpLeases.update(ipAddressQuery(inetAddr), update);
	}

	/**
	 * Find dhcp leases for ia.
	 *
	 * @param duid the duid
	 * @param iatype the iatype
	 * @param iaid the iaid
	 * @return the list
	 */
	protected List<DhcpLease> findDhcpLeasesForIA(final byte[] duid, final byte iatype, final long iaid)
	{
		List<DhcpLease> leases = null;
		DBObject query = new BasicDBObject("duid", duid).
								append("iatype", iatype).
								append("iaid", iaid);
		// SQL databases will store in ipAddress order because it is a primary key,
		// but Mongo is not so smart because it is just an index on the field
		DBCursor cursor = dhcpLeases.find(query).sort(new BasicDBObject("ipAddress", 1));;
		try {
			if (cursor.count() > 0) {
				leases = new ArrayList<DhcpLease>();
				while (cursor.hasNext()) {
					leases.add(convertDBObject(cursor.next()));
				}
			}
		}
		finally {
			cursor.close();
		}
		return leases;
	}

	/**
	 * Find dhcp lease for InetAddr.
	 *
	 * @param inetAddr the InetAddr
	 * @return the DhcpLease
	 */
	protected DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr)
	{
		DhcpLease lease = null;
		DBObject query = ipAddressQuery(inetAddr);
		DBCursor cursor = dhcpLeases.find(query);
		try {
			if (cursor.count() > 0) {
				if (cursor.count() == 1) {
					lease = convertDBObject(cursor.next());
				}
	        	else {
	        		//TODO: ensure this is impossible with mongo's unique index?
	        		log.error("Found more than one lease for IP=" + 
	        					inetAddr.getHostAddress());
	        	}
			}
		}
		finally {
			cursor.close();
		}
        return lease;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void updateIaAddr(final IaAddress iaAddr)
	{
		DBObject query = ipAddressQuery(iaAddr.getIpAddress());
		BasicDBObject update = new BasicDBObject("state", iaAddr.getState());
		if (iaAddr instanceof IaPrefix) {
			update.append("prefixLength", ((IaPrefix)iaAddr).getPrefixLength());
		}
		update.append("startTime", iaAddr.getStartTime()).
				append("preferredEndTime", iaAddr.getPreferredEndTime()).
				append("validEndTime", iaAddr.getValidEndTime());
		
		dhcpLeases.update(query, new BasicDBObject("$set", update));
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void deleteIaAddr(final IaAddress iaAddr)
	{
		dhcpLeases.remove(ipAddressQuery(iaAddr.getIpAddress()));
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExistingIPs(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr)
	{
		List<InetAddress> inetAddrs = new ArrayList<InetAddress>();

		BasicDBList ipBetw = new BasicDBList();
		ipBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$gte", startAddr.getAddress())));
		ipBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$lte", endAddr.getAddress())));
		
		DBObject query = new BasicDBObject("$and", ipBetw);

		DBCursor cursor = dhcpLeases.find(query).sort(new BasicDBObject("ipAddress", 1));
		try {
			if (cursor.count() > 0) {
				while (cursor.hasNext()) {
					inetAddrs.add(convertDBObject(cursor.next()).getIpAddress());
				}
			}
		}
		finally {
			cursor.close();
		}
		return inetAddrs;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaAddresses(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaAddress> findUnusedIaAddresses(final InetAddress startAddr, final InetAddress endAddr)
	{
		long offerExpireMillis = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_OFFER_EXPIRATION);
		final Date offerExpiration = new Date(new Date().getTime() - offerExpireMillis);

		BasicDBList ipAdvBetw = new BasicDBList();
		ipAdvBetw.add(new BasicDBObject("state", IaAddress.ADVERTISED));
		ipAdvBetw.add(new BasicDBObject("startTime", new BasicDBObject("$lte", offerExpiration)));
		ipAdvBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$gte", startAddr.getAddress())));
		ipAdvBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$lte", endAddr.getAddress())));
		
		BasicDBList ipExpRel = new BasicDBList();
		ipExpRel.add(IaAddress.EXPIRED);
		ipExpRel.add(IaAddress.RELEASED);
		
		BasicDBList ipExpRelBetw = new BasicDBList();
		ipExpRelBetw.add(new BasicDBObject("state", new BasicDBObject("$in", ipExpRel)));
		ipExpRelBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$gte", startAddr.getAddress())));
		ipExpRelBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$lte", endAddr.getAddress())));
		
		BasicDBList ipBetw = new BasicDBList();
		ipBetw.add(new BasicDBObject("$and", ipAdvBetw));
		ipBetw.add(new BasicDBObject("$and", ipExpRelBetw));
		
		DBObject query = new BasicDBObject("$or", ipBetw); 
		DBCursor cursor = dhcpLeases.find(query).sort(new BasicDBObject("state", 1))
												.sort(new BasicDBObject("validEndTime", 1))
												.sort(new BasicDBObject("ipAddress", 1));
		try {
			if (cursor.count() > 0) {
				List<DhcpLease> leases = new ArrayList<DhcpLease>();
				while (cursor.hasNext()) {
					leases.add(convertDBObject(cursor.next()));
				}
				return toIaAddresses(leases);
			}
		}
		finally {
			cursor.close();
		}
		
		return null;
	}

	protected List<DhcpLease> findExpiredLeases(final byte iatype) {
		List<DhcpLease> leases = null;
		DBObject query = new BasicDBObject("iatype", iatype).
									append("state", new BasicDBObject("$ne", IaAddress.STATIC)).
									append("validEndTime", new BasicDBObject("$lt", new Date()));
		DBCursor cursor = dhcpLeases.find(query).sort(new BasicDBObject("validEndTime", 1));
		try {
			if (cursor.count() > 0) {
				leases = new ArrayList<DhcpLease>();
				while (cursor.hasNext()) {
					leases.add(convertDBObject(cursor.next()));
				}
			}
		}
		finally {
			cursor.close();
		}
		return leases;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaPrefixes(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaPrefix> findUnusedIaPrefixes(final InetAddress startAddr, final InetAddress endAddr) {
		long offerExpireMillis = 
				DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_OFFER_EXPIRATION);
			final Date offerExpiration = new Date(new Date().getTime() - offerExpireMillis);

			BasicDBList ipAdvBetw = new BasicDBList();
			ipAdvBetw.add(new BasicDBObject("state", IaPrefix.ADVERTISED));
			ipAdvBetw.add(new BasicDBObject("startTime", new BasicDBObject("$lte", offerExpiration)));
			ipAdvBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$gte", startAddr.getAddress())));
			ipAdvBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$lte", endAddr.getAddress())));
			
			BasicDBList ipExpRel = new BasicDBList();
			ipExpRel.add(IaPrefix.EXPIRED);
			ipExpRel.add(IaPrefix.RELEASED);
			
			BasicDBList ipExpRelBetw = new BasicDBList();
			ipExpRelBetw.add(new BasicDBObject("state", new BasicDBObject("$in", ipExpRel)));
			ipExpRelBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$gte", startAddr.getAddress())));
			ipExpRelBetw.add(new BasicDBObject("ipAddress", new BasicDBObject("$lte", endAddr.getAddress())));
			
			BasicDBList ipBetw = new BasicDBList();
			ipBetw.add(new BasicDBObject("$and", ipAdvBetw));
			ipBetw.add(new BasicDBObject("$and", ipExpRelBetw));
			
			DBObject query = new BasicDBObject("$or", ipBetw); 
			DBCursor cursor = dhcpLeases.find(query).sort(new BasicDBObject("state", 1))
													.sort(new BasicDBObject("validEndTime", 1))
													.sort(new BasicDBObject("ipAddress", 1));
			try {
				if (cursor.count() > 0) {
					List<DhcpLease> leases = new ArrayList<DhcpLease>();
					while (cursor.hasNext()) {
						leases.add(convertDBObject(cursor.next()));
					}
					return toIaPrefixes(leases);
				}
			}
			finally {
				cursor.close();
			}
			
			return null;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExpiredIaPrefixes()
	 */
	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
		List<DhcpLease> leases = null;
		DBObject query = new BasicDBObject("iatype", IdentityAssoc.PD_TYPE).
									append("validEndTime", new BasicDBObject("$lt", new Date()));
		DBCursor cursor = dhcpLeases.find(query).sort(new BasicDBObject("validEndTime", 1));
		try {
			if (cursor.count() > 0) {
				leases = new ArrayList<DhcpLease>();
				while (cursor.hasNext()) {
					leases.add(convertDBObject(cursor.next()));
				}
			}
		}
		finally {
			cursor.close();
		}
		return toIaPrefixes(leases);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#reconcileIaAddresses(java.util.List)
	 */
	@Override
	public void reconcileIaAddresses(List<Range> ranges) {
		
		BasicDBList ipBetwList = new BasicDBList();
		for (Range range : ranges) {
			BasicDBList ipBetw = new BasicDBList();
			ipBetw.add(new BasicDBObject("ipAddress", 
					new BasicDBObject("$lt", range.getStartAddress().getAddress())));
			ipBetw.add(new BasicDBObject("ipAddress", 
					new BasicDBObject("$gt", range.getEndAddress().getAddress())));
			ipBetwList.add(new BasicDBObject("$or", ipBetw));
		}
		
		dhcpLeases.remove(new BasicDBObject("$and", ipBetwList));
	}


	/**
	 * For unit tests only
	 */
	public void deleteAllIAs() {
		DBCursor cursor = dhcpLeases.find();
		try {
			if (cursor.count() > 0) {
				int i=0;
				while (cursor.hasNext()) {
					dhcpLeases.remove(cursor.next());
					i++;
				}
				log.info("Deleted all " + i + " dhcpLeases");
			}
		}
		finally {
			cursor.close();
		}
	}
}
