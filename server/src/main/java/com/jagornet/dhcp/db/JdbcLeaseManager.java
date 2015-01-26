/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcLeaseManager.java is part of Jagornet DHCP.
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.Range;
import com.jagornet.dhcp.util.Util;

/**
 * The JdbcLeaseManager implementation class for the IaManager interface.
 * This is the main database access class for handling client bindings.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcLeaseManager extends LeaseManager
{	
	private static Logger log = LoggerFactory.getLogger(JdbcLeaseManager.class);
	
	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;
	
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		return jdbcTemplate;
	}
	
	// Spring bean init-method
	public void init() throws Exception {
        String schemaType = DhcpServerPolicies.globalPolicy(Property.DATABASE_SCHEMA_TYTPE);
        if (schemaType.toLowerCase().endsWith("derby")) {
			DbSchemaManager.validateSchema(dataSource, DbSchemaManager.SCHEMA_DERBY_V2_FILENAME, 2);
        }
        else {
        	DbSchemaManager.validateSchema(dataSource, DbSchemaManager.SCHEMA_V2_FILENAME, 2);
        }
	}
	
	/**
	 * Insert dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void insertDhcpLease(final DhcpLease lease)
	{
		getJdbcTemplate().update("insert into dhcplease" +
				" (ipaddress, duid, iatype, iaid, prefixlen, state," +
				" starttime, preferredendtime, validendtime," +
				" ia_options, ipaddr_options)" +
				" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, lease.getIpAddress().getAddress());
				ps.setBytes(2, lease.getDuid());
				ps.setByte(3, lease.getIatype());
				ps.setLong(4, lease.getIaid());
				ps.setShort(5, lease.getPrefixLength());
				ps.setByte(6, lease.getState());
				java.sql.Timestamp sts = 
					new java.sql.Timestamp(lease.getStartTime().getTime());
				ps.setTimestamp(7, sts, Util.GMT_CALENDAR);
				java.sql.Timestamp pts = 
					new java.sql.Timestamp(lease.getPreferredEndTime().getTime());
				ps.setTimestamp(8, pts, Util.GMT_CALENDAR);
				java.sql.Timestamp vts = 
					new java.sql.Timestamp(lease.getValidEndTime().getTime());
				ps.setTimestamp(9, vts, Util.GMT_CALENDAR);
				ps.setBytes(10, encodeOptions(lease.getIaDhcpOptions()));
				ps.setBytes(11, encodeOptions(lease.getIaAddrDhcpOptions()));
			}
		});
	}
	
	/**
	 * Update dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void updateDhcpLease(final DhcpLease lease)
	{
		getJdbcTemplate().update("update dhcplease" +
				" set state=?," +
				" starttime=?," +
				" preferredendtime=?," +
				" validendtime=?," +
				" ia_options=?," +
				" ipaddr_options=?" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setByte(1, lease.getState());
				java.sql.Timestamp sts = 
					new java.sql.Timestamp(lease.getStartTime().getTime());
				ps.setTimestamp(2, sts, Util.GMT_CALENDAR);
				java.sql.Timestamp pts = 
					new java.sql.Timestamp(lease.getPreferredEndTime().getTime());
				ps.setTimestamp(3, pts, Util.GMT_CALENDAR);
				java.sql.Timestamp vts = 
					new java.sql.Timestamp(lease.getValidEndTime().getTime());
				ps.setTimestamp(4, vts, Util.GMT_CALENDAR);
				ps.setBytes(5, encodeOptions(lease.getIaDhcpOptions()));
				ps.setBytes(6, encodeOptions(lease.getIaAddrDhcpOptions()));
				ps.setBytes(7, lease.getIpAddress().getAddress());
			}
		});
	}
	
	/**
	 * Delete dhcp lease.
	 *
	 * @param lease the lease
	 */
	protected void deleteDhcpLease(final DhcpLease lease)
	{
		getJdbcTemplate().update("delete from dhcplease" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, lease.getIpAddress().getAddress());
			}
		});
	}
	
	/**
	 * Update ia options.
	 */
	protected void updateIaOptions(final InetAddress inetAddr, 
									final Collection<DhcpOption> iaOptions)
	{
		getJdbcTemplate().update("update dhcplease" +
				" set ia_options=?" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, encodeOptions(iaOptions));
				ps.setBytes(2, inetAddr.getAddress());
			}
		});
	}
	
	/**
	 * Update ipaddr options.
	 */
	protected void updateIpAddrOptions(final InetAddress inetAddr,
									final Collection<DhcpOption> ipAddrOptions)
	{
		getJdbcTemplate().update("update dhcplease" +
				" set ipaddr_options=?" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, encodeOptions(ipAddrOptions));
				ps.setBytes(2, inetAddr.getAddress());
			}
		});
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
		return getJdbcTemplate().query(
                "select * from dhcplease" +
                " where duid = ?" +
                " and iatype = ?" +
                " and iaid = ?" +
                " order by ipaddress",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setBytes(1, duid);
            			ps.setByte(2, iatype);
            			ps.setLong(3, iaid);
            		}
            	},
                new DhcpLeaseRowMapper());
	}

	/**
	 * Find dhcp lease for InetAddr.
	 *
	 * @param inetAddr the InetAddr
	 * @return the DhcpLease
	 */
	protected DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr)
	{
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where ipaddress = ?",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setBytes(1, inetAddr.getAddress());
            		}
            	},
                new DhcpLeaseRowMapper());
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
        return null;
	}
		
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void updateIaAddr(final IaAddress iaAddr)
	{
		getJdbcTemplate().update("update dhcplease" +
				" set state = ?," +
				((iaAddr instanceof IaPrefix) ? " prefixlen = ?," : "") + 
				" starttime = ?," +
				" preferredendtime = ?," +
				" validendtime = ?" +
				" where ipaddress = ?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int i = 1;
				ps.setByte(i++, iaAddr.getState());
				if (iaAddr instanceof IaPrefix) {
					ps.setShort(i++, ((IaPrefix)iaAddr).getPrefixLength());
				}
				Date start = iaAddr.getStartTime();
				if (start != null) {
					java.sql.Timestamp sts = new java.sql.Timestamp(start.getTime());
					ps.setTimestamp(i++, sts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				Date preferred = iaAddr.getPreferredEndTime();
				if (preferred != null) {
					java.sql.Timestamp pts = new java.sql.Timestamp(preferred.getTime());
					ps.setTimestamp(i++, pts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				Date valid = iaAddr.getValidEndTime();
				if (valid != null) {
					java.sql.Timestamp vts = new java.sql.Timestamp(valid.getTime());
					ps.setTimestamp(i++, vts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				ps.setBytes(i++, iaAddr.getIpAddress().getAddress());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void deleteIaAddr(final IaAddress iaAddr)
	{
		getJdbcTemplate().update("delete from dhcplease" +
				" where ipaddress = ?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setBytes(1, iaAddr.getIpAddress().getAddress());
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExistingIPs(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr)
	{
        return getJdbcTemplate().query(
                "select ipaddress from dhcplease" +
                " where ipaddress >= ? and ipaddress <= ?" +
                " order by ipaddress", 
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setBytes(1, startAddr.getAddress());
						ps.setBytes(2, endAddr.getAddress());
					}                	
                },
                new RowMapper<InetAddress>() {
                    @Override
                    public InetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
                    	InetAddress inetAddr = null;
                    	try {
                			inetAddr = InetAddress.getByAddress(rs.getBytes("ipaddress"));
                		} 
                    	catch (UnknownHostException e) {
                    		// re-throw as SQLException
                			throw new SQLException("Unable to map ipaddress", e);
                		}
                        return inetAddr;
                    }
                });
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
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where ((state=" + IaAddress.ADVERTISED +
                " and starttime <= ?)" +
                " or (state=" + IaAddress.EXPIRED +
                " or state=" + IaAddress.RELEASED + "))" +
                " and ipaddress >= ? and ipaddress <= ?" +
                " order by state, validendtime, ipaddress",
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						java.sql.Timestamp ts = new java.sql.Timestamp(offerExpiration);
						ps.setTimestamp(1, ts);
						ps.setBytes(2, startAddr.getAddress());
						ps.setBytes(3, endAddr.getAddress());
					}                	
                },
                new DhcpLeaseRowMapper());
		return toIaAddresses(leases);
	}

	protected List<DhcpLease> findExpiredLeases(final byte iatype) {
        return getJdbcTemplate().query(
                "select * from dhcplease" +
                " where iatype = ?" +
                " and state != " + IaAddress.STATIC +
                " and validendtime < ? order by validendtime",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setByte(1, iatype);
            			java.sql.Timestamp ts = new java.sql.Timestamp(new Date().getTime());
            			ps.setTimestamp(2, ts, Util.GMT_CALENDAR);
            		}
                },
                new DhcpLeaseRowMapper());
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaPrefixes(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaPrefix> findUnusedIaPrefixes(final InetAddress startAddr, final InetAddress endAddr) {
		final long offerExpiration = new Date().getTime() - 12000;	// 2 min = 120 sec = 12000 ms
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where ((state=" + IaPrefix.ADVERTISED +
                " and starttime <= ?)" +
                " or (state=" + IaPrefix.EXPIRED +
                " or state=" + IaPrefix.RELEASED + "))" +
                " and ipaddress >= ? and ipaddress <= ?" +
                " order by state, validendtime, ipaddress",
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						java.sql.Timestamp ts = new java.sql.Timestamp(offerExpiration);
						ps.setTimestamp(1, ts);
						ps.setBytes(2, startAddr.getAddress());
						ps.setBytes(3, endAddr.getAddress());
					}                	
                },
                new DhcpLeaseRowMapper());
		return toIaPrefixes(leases);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExpiredIaPrefixes()
	 */
	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where iatype = " + IdentityAssoc.PD_TYPE +
                " and validendtime < ? order by validendtime",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			java.sql.Timestamp ts = new java.sql.Timestamp(new Date().getTime());
            			ps.setTimestamp(1, ts, Util.GMT_CALENDAR);
            		}
                },
                new DhcpLeaseRowMapper());
		return toIaPrefixes(leases);
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
		getJdbcTemplate().update(query.toString(), args.toArray());
	}

    /**
     * The Class DhcpLeaseRowMapper.
     */
    protected class DhcpLeaseRowMapper implements RowMapper<DhcpLease> 
    {    	
	    /* (non-Javadoc)
	     * @see org.springframework.jdbc.core.simple.RowMapper#mapRow(java.sql.ResultSet, int)
	     */
	    @Override
        public DhcpLease mapRow(ResultSet rs, int rowNum) throws SQLException {
	    	ResultSetExtractor<DhcpLease> rsExtractor = new DhcpLeaseResultSetExtractor();
	    	return rsExtractor.extractData(rs);
        }
    }
    
    /**
     * The Class DhcpLeaseResultSetExtractor.
     */
    protected class DhcpLeaseResultSetExtractor implements ResultSetExtractor<DhcpLease>
    {
		
		/* (non-Javadoc)
		 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
		 */
		@Override
		public DhcpLease extractData(ResultSet rs)
				throws SQLException, DataAccessException {
	    	DhcpLease lease = new DhcpLease();
	    	lease.setDuid(rs.getBytes("duid"));
	    	lease.setIatype(rs.getByte("iatype"));
	    	lease.setIaid(rs.getLong("iaid"));
        	try {
				lease.setIpAddress(InetAddress.getByAddress(rs.getBytes("ipaddress")));
			} 
        	catch (UnknownHostException e) {
        		// re-throw as SQLException
				throw new SQLException("Unable to map dhcplease", e);
			}
        	lease.setPrefixLength(rs.getShort("prefixlen"));
			lease.setState(rs.getByte("state"));
			lease.setStartTime(rs.getTimestamp("starttime", Util.GMT_CALENDAR));
			lease.setPreferredEndTime(rs.getTimestamp("preferredendtime", Util.GMT_CALENDAR));
			lease.setValidEndTime(rs.getTimestamp("validendtime", Util.GMT_CALENDAR));
			lease.setIaDhcpOptions(decodeOptions(rs.getBytes("ia_options")));
			lease.setIaAddrDhcpOptions(decodeOptions(rs.getBytes("ipaddr_options")));
            return lease;
		};
    }

	/**
	 * For unit tests only
	 */
	public void deleteAllIAs() {
		int cnt = getJdbcTemplate().update("delete from dhcplease");
		log.info("Deleted all " + cnt + " dhcpleases");
	}
}
