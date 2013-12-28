/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcLeaseManager is part of DHCPv6.
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
package com.jagornet.dhcpv6.db;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Range;

/**
 * The SqliteLeaseManager implementation class for the IaManager interface.
 * This is the main database access class for handling client bindings.
 * 
 * @author A. Gregory Rabil
 */
public class SqliteLeaseManager extends LeaseManager
{	
	private static Logger log = LoggerFactory.getLogger(SqliteLeaseManager.class);
	
	protected static File DATABASE_FILE = new File("db/sqlite/jagornet-dhcpv6");
	
	private static final ThreadLocal<SQLiteConnection> threadConnection =
		new ThreadLocal<SQLiteConnection>() {
		    @Override
		    protected SQLiteConnection initialValue() {
				SQLiteConnection connection = new SQLiteConnection(DATABASE_FILE);
				try {
					connection.open();
					log.debug("Created new SQLite connection: " + connection.toString());
				}
				catch (SQLiteException ex) {
					log.error("Failed to open connection: " + ex);
				}
				return connection;
		    }
		    @Override
		    protected void finalize() throws Throwable {
		    	SQLiteConnection connection = this.get();
		    	if (connection != null) {
		    		log.debug("Disposing SQLite connection: " + connection.toString());
		    		connection.dispose();
		    	}
		    };
	};

	protected static SQLiteConnection getSQLiteConnection() {
		return threadConnection.get();
	}

	// Spring bean init-method
	public void init() throws Exception {
		if (!DATABASE_FILE.exists()) {
			File path = DATABASE_FILE.getParentFile();
			if (path != null) {
				path.mkdirs();
			}
			String schemaFilename = DbSchemaManager.SCHEMA_V2_FILENAME;
			log.info("Creating new SQLite schema from file: " + schemaFilename);
			SQLiteConnection sqliteConnection = getSQLiteConnection();
			List<String> schemaDDL = DbSchemaManager.getSchemaDDL(schemaFilename);
			for (String ddl : schemaDDL) {
				sqliteConnection.exec(ddl);
			}
		}
		else {
			//TODO: validate the existing schema
		}
	}
	
	/**
	 * Insert dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void insertDhcpLease(final DhcpLease lease)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("insert into dhcplease" +
						" (ipaddress, duid, iatype, iaid, prefixlen, state," +
						" starttime, preferredendtime, validendtime," +
						" ia_options, ipaddr_options)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");			
			statement.bind(1, lease.getIpAddress().getAddress());
			statement.bind(2, lease.getDuid());
			statement.bind(3, lease.getIatype());
			statement.bind(4, lease.getIaid());
			statement.bind(5, lease.getPrefixLength());
			statement.bind(6, lease.getState());
			statement.bind(7, lease.getStartTime().getTime());
			statement.bind(8, lease.getPreferredEndTime().getTime());
			statement.bind(9, lease.getValidEndTime().getTime());
			statement.bind(10, encodeOptions(lease.getIaDhcpOptions()));
			statement.bind(11, encodeOptions(lease.getIaAddrDhcpOptions()));
			
			while (statement.step()) {
				log.debug("insertDhcpLease: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("insertDhcpLease failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/**
	 * Update dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void updateDhcpLease(final DhcpLease lease)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("update dhcplease" +
						" set state=?," +
						" starttime=?," +
						" preferredendtime=?," +
						" validendtime=?," +
						" ia_options=?," +
						" ipaddr_options=?" +
						" where ipaddress=?");
			statement.bind(1, lease.getState());
			statement.bind(2, lease.getStartTime().getTime());
			statement.bind(3, lease.getPreferredEndTime().getTime());
			statement.bind(4, lease.getValidEndTime().getTime());
			statement.bind(5, encodeOptions(lease.getIaDhcpOptions()));
			statement.bind(6, encodeOptions(lease.getIaAddrDhcpOptions()));
			statement.bind(7, lease.getIpAddress().getAddress());
			
			while (statement.step()) {
				log.debug("updateDhcpLease: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("updateDhcpLease failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/**
	 * Delete dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void deleteDhcpLease(final DhcpLease lease)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("delete from dhcplease" +
					 	" where ipaddress=?");
			statement.bind(1, lease.getIpAddress().getAddress());
			
			while (statement.step()) {
				log.debug("deleteDhcpLease: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("deleteDhcpLease failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/**
	 * Update ia options.
	 */
	protected void updateIaOptions(final InetAddress inetAddr, 
									final Collection<DhcpOption> iaOptions)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("update dhcplease" +
						" set ia_options=?" +
						" where ipaddress=?");
			statement.bind(1, encodeOptions(iaOptions));
			statement.bind(2, inetAddr.getAddress());
			
			while (statement.step()) {
				log.debug("updateIaOptions: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("updateIaOptions failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/**
	 * Update ipaddr options.
	 */
	protected void updateIpAddrOptions(final InetAddress inetAddr,
									final Collection<DhcpOption> ipAddrOptions)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("update dhcplease" +
						" set ipaddr_options=?" +
						" where ipaddress=?");
			statement.bind(1, encodeOptions(ipAddrOptions));
			statement.bind(2, inetAddr.getAddress());
			
			while (statement.step()) {
				log.debug("updateIpAddrOptions: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("updateIpAddrOptions failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
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
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("select * from dhcplease" +
		                " where duid = ?" +
		                " and iatype = ?" +
		                " and iaid = ?");
			statement.bind(1, duid);
            statement.bind(2, iatype);
            statement.bind(3, iaid);			
            return mapLeases(statement);
		}
		catch (SQLiteException ex) {
			log.error("findDhcpLeasesForIA failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}

	/**
	 * Find dhcp lease for InetAddr.
	 *
	 * @param inetAddr the InetAddr
	 * @return the DhcpLease
	 */
	protected DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr)
	{
		List<DhcpLease> leases = null;
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(
		                "select * from dhcplease" +
		                " where ipaddress = ?");
			statement.bind(1, inetAddr.getAddress());
            leases = mapLeases(statement);
            if ((leases != null) && (leases.size() > 0)) {
            	if (leases.size() == 1) {
            		return leases.get(0);
            	}
            	else {
            		//TODO: this really should be impossible because of the unique
            		//		constraint on the IP address
            		log.error("Found more than one lease for IP=" + 
            					inetAddr.getHostAddress());
            	}
            }
		}
		catch (SQLiteException ex) {
			log.error("findDhcpLeaseForInetAddr failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
        return null;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void updateIaAddr(final IaAddress iaAddr)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("update dhcplease" +
					" set state = ?," +
					((iaAddr instanceof IaPrefix) ? " prefixlen = ?," : "") + 
					" starttime = ?," +
					" preferredendtime = ?," +
					" validendtime = ?" +
					" where ipaddress = ?");
			int i=1;
			statement.bind(i++, iaAddr.getState());
			if (iaAddr instanceof IaPrefix) {
				statement.bind(i++, ((IaPrefix)iaAddr).getPrefixLength());
			}
			Date start = iaAddr.getStartTime();
			if (start != null) {
				statement.bind(i++, start.getTime());
			}
			else {
				statement.bindNull(i++);
			}
			Date preferred = iaAddr.getPreferredEndTime();
			if (preferred != null) {
				statement.bind(i++, preferred.getTime());
			}
			else {
				statement.bindNull(i++);
			}
			Date valid = iaAddr.getValidEndTime();
			if (valid != null) {
				statement.bind(i++, valid.getTime());
			}
			else {
				statement.bindNull(i++);
			}
			statement.bind(i++, iaAddr.getIpAddress().getAddress());
			
			while(statement.step()) {
				log.debug("updateIaAddr: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("updateIaAddr failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void deleteIaAddr(final IaAddress iaAddr)
	{
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare("delete from dhcplease" +
						" where ipaddress = ?");
			statement.bind(1, iaAddr.getIpAddress().getAddress());
			
			while(statement.step()) {
				log.debug("deleteIaAddr: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("deleteIaAddr failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExistingIPs(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr)
	{
		List<InetAddress> inetAddrs = new ArrayList<InetAddress>();
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(
		                "select ipaddress from dhcplease" +
		                " where ipaddress >= ? and ipaddress <= ?" +
		                " order by ipaddress");
			statement.bind(1, startAddr.getAddress());
			statement.bind(2, endAddr.getAddress());
			
			while (statement.step()) {
            	InetAddress inetAddr = null;
            	try {
        			inetAddr = InetAddress.getByAddress(statement.columnBlob(0));
        		} 
            	catch (UnknownHostException e) {
        			throw new RuntimeException("Unable to map ipaddress", e);
        		}
                inetAddrs.add(inetAddr);
			}
			return inetAddrs;
		}
		catch (SQLiteException ex) {
			log.error("findExistingIPs failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaAddresses(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaAddress> findUnusedIaAddresses(final InetAddress startAddr, final InetAddress endAddr)
	{
		long offerExpireMillis = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_OFFER_EXPIRATION);
		final long offerExpiration = new Date().getTime() - offerExpireMillis;
		
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(
		                "select * from dhcplease" +
		                " where ((state=" + IaAddress.ADVERTISED +
		                " and starttime <= ?)" +
		                " or (state=" + IaAddress.EXPIRED +
		                " or state=" + IaAddress.RELEASED + "))" +
		                " and ipaddress >= ? and ipaddress <= ?" +
		                " order by state, validendtime, ipaddress");
			statement.bind(1, offerExpiration);
			statement.bind(2, startAddr.getAddress());
			statement.bind(3, endAddr.getAddress());
			
			List<DhcpLease> leases = mapLeases(statement);
			return toIaAddresses(leases);
		}
		catch (SQLiteException ex) {
			log.error("findUnusedIaAddresses failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}

	protected List<DhcpLease> findExpiredLeases(final byte iatype) {
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(
		                "select * from dhcplease" +
		                " where iatype = ?" +
		                " and state != " + IaAddress.STATIC +
		                " and validendtime < ? order by validendtime");
			statement.bind(1, iatype);
			statement.bind(2, new Date().getTime());
			
			return mapLeases(statement);
		}
		catch (SQLiteException ex) {
			log.error("findExpiredLeases failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaPrefixes(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaPrefix> findUnusedIaPrefixes(final InetAddress startAddr, final InetAddress endAddr) {
		long offerExpireMillis = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_OFFER_EXPIRATION);
		final long offerExpiration = new Date().getTime() - offerExpireMillis;
		
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(
		                "select * from dhcplease" +
		                " where ((state=" + IaPrefix.ADVERTISED +
		                " and starttime <= ?)" +
		                " or (state=" + IaPrefix.EXPIRED +
		                " or state=" + IaPrefix.RELEASED + "))" +
		                " and ipaddress >= ? and ipaddress <= ?" +
		                " order by state, validendtime, ipaddress");
			statement.bind(1, offerExpiration);
			statement.bind(2, startAddr.getAddress());
			statement.bind(3, endAddr.getAddress());
			
			List<DhcpLease> leases = mapLeases(statement);
			return toIaPrefixes(leases);
		}
		catch (SQLiteException ex) {
			log.error("findUnusedIaPrefixes failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExpiredIaPrefixes()
	 */
	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(
						"select * from dhcplease" +
		                " where iatype = " + IdentityAssoc.PD_TYPE +
		                " and validendtime < ? order by validendtime");
            statement.bind(1, new Date().getTime());
            List<DhcpLease> leases = mapLeases(statement);
            return toIaPrefixes(leases);
		}
		catch (SQLiteException ex) {
			log.error("findExpiredIaPrefixes failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#reconcileIaAddresses(java.util.List)
	 */
	@Override
	public void reconcileIaAddresses(List<Range> ranges) {
		List<byte[]> args = new ArrayList<byte[]>();
		StringBuilder query = new StringBuilder();
		query.append("delete from dhcplease where ipaddress");
		Iterator<Range> rangeIter = ranges.iterator();
		while (rangeIter.hasNext()) {
			Range range = rangeIter.next();
			query.append(" not between ? and ?");
			args.add(range.getStartAddress().getAddress());
			args.add(range.getEndAddress().getAddress());
			if (rangeIter.hasNext())
				query.append(" and ipaddress");
		}
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			statement = connection.prepare(query.toString());
			int i = 1;
			for (byte[] bs : args) {
				statement.bind(i++, bs);
			}
			while (statement.step()) {
				log.debug("reconcileIaAddresses: step=true");
			}
		}
		catch (SQLiteException ex) {
			log.error("reconcileIaAddresses failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}

    /**
     * The Class DhcpLeaseRowMapper.
     */
    protected List<DhcpLease> mapLeases(SQLiteStatement statement) throws SQLiteException {
		
    	List<DhcpLease> leases = new ArrayList<DhcpLease>();
    	
		while (statement.step()) {
	    	DhcpLease lease = new DhcpLease();
        	try {
				lease.setIpAddress(InetAddress.getByAddress(statement.columnBlob(0)));
			} 
        	catch (UnknownHostException e) {
				throw new RuntimeException("Unable to map dhcplease", e);
			}
	    	lease.setDuid(statement.columnBlob(1));
	    	lease.setIatype((byte)statement.columnInt(2));
	    	lease.setIaid(statement.columnLong(3));
	    	lease.setPrefixLength((short)statement.columnInt(4));
			lease.setState((byte)statement.columnInt(5));
	    	long starttime;
	    	starttime = statement.columnLong(6);
			lease.setStartTime(new Date(starttime));
	    	long preferredendtime;
	    	preferredendtime = statement.columnLong(7);
			lease.setPreferredEndTime(new Date(preferredendtime));
	    	long validendtime;
	    	validendtime = statement.columnLong(8);
			lease.setValidEndTime(new Date(validendtime));
			lease.setIaDhcpOptions(decodeOptions(statement.columnBlob(9)));
			lease.setIaAddrDhcpOptions(decodeOptions(statement.columnBlob(10)));
			leases.add(lease);
		}
		return leases;
    }
    
    private void closeStatement(SQLiteStatement statement) {
		if (statement != null)
			statement.dispose();
    }
    
    private void closeConnection(SQLiteConnection connection) {
//		if (connection != null)
//			connection.dispose();    	
    }

	
    /**
     * For unit tests only
     */
	public void deleteAllIAs() {
		SQLiteConnection connection = null;
		SQLiteStatement statement = null;
		try {
			connection = getSQLiteConnection();
			connection.exec("delete from dhcplease");
		}
		catch (SQLiteException ex) {
			log.error("deleteAllIAs failed", ex);
			throw new RuntimeException(ex);
		}
		finally {
			closeStatement(statement);
			closeConnection(connection);
		}
	}
}
