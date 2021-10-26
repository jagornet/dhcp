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
package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.net.Inet4Address;
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
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.Range;

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
	
	protected static String LIMIT_ONE_CLAUSE = 
			DhcpServerPolicies.globalPolicy(Property.DATABASE_SCHEMA_TYTPE).equals("jdbc-derby") ?
					" fetch first 1 rows only" : " limit 1";
	
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
		super.init();
        String schemaType = DhcpServerPolicies.globalPolicy(Property.DATABASE_SCHEMA_TYTPE);
        if (schemaType.toLowerCase().contains("derby")) {
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
	public int insertDhcpLease(final DhcpLease lease)
	{
		int cnt = getJdbcTemplate().update("insert into dhcplease" +
				" (ipaddress, duid, iatype, iaid, prefixlen, state, hapeerstate," +
				" starttime, preferredendtime, validendtime," +
				" options, ia_options, ipaddr_options)" +
				" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				int i = 1;
				ps.setBytes(i++, lease.getIpAddress().getAddress());
				ps.setBytes(i++, lease.getDuid());
				ps.setByte(i++, lease.getIatype());
				ps.setLong(i++, lease.getIaid());
				ps.setShort(i++, lease.getPrefixLength());
				ps.setByte(i++, lease.getState());
				ps.setByte(i++, lease.getHaPeerState());
				if (lease.getStartTime() != null) {
					java.sql.Timestamp sts = 
							new java.sql.Timestamp(lease.getStartTime().getTime());
					ps.setTimestamp(i++, sts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				if (lease.getPreferredEndTime() != null) {
					java.sql.Timestamp pts =
							new java.sql.Timestamp(lease.getPreferredEndTime().getTime());
					ps.setTimestamp(i++, pts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				if (lease.getValidEndTime() != null) {
					java.sql.Timestamp vts = 
						new java.sql.Timestamp(lease.getValidEndTime().getTime());
					ps.setTimestamp(i++, vts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				ps.setBytes(i++, encodeOptions(lease.getDhcpOptions(),
							(lease.getIatype() == IdentityAssoc.V4_TYPE)));
				ps.setBytes(i++, encodeOptions(lease.getIaDhcpOptions(),
							(lease.getIatype() == IdentityAssoc.V4_TYPE)));
				ps.setBytes(i++, encodeOptions(lease.getIaAddrDhcpOptions(),
							(lease.getIatype() == IdentityAssoc.V4_TYPE)));
			}
		});
		log.debug("Inserted " + cnt + " dhcplease objects");
		return cnt;
	}
	
	/**
	 * Update dhcp lease.
	 *
	 * @param lease the lease
	 */
	public int updateDhcpLease(final DhcpLease lease)
	{
		int cnt = getJdbcTemplate().update("update dhcplease" +
				" set state=?," +
				" hapeerstate=?," +
				" starttime=?," +
				" preferredendtime=?," +
				" validendtime=?," +
				" options=?," +
				" ia_options=?," +
				" ipaddr_options=?" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				int i = 1;
				ps.setByte(i++, lease.getState());
				ps.setByte(i++, lease.getHaPeerState());
				if (lease.getStartTime() != null) {
					java.sql.Timestamp sts = 
							new java.sql.Timestamp(lease.getStartTime().getTime());
					ps.setTimestamp(i++, sts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				if (lease.getPreferredEndTime() != null) {
					java.sql.Timestamp pts =
							new java.sql.Timestamp(lease.getPreferredEndTime().getTime());
					ps.setTimestamp(i++, pts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				if (lease.getValidEndTime() != null) {
					java.sql.Timestamp vts = 
						new java.sql.Timestamp(lease.getValidEndTime().getTime());
					ps.setTimestamp(i++, vts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				ps.setBytes(i++, encodeOptions(lease.getDhcpOptions(),
							(lease.getIatype() == IdentityAssoc.V4_TYPE)));
				ps.setBytes(i++, encodeOptions(lease.getIaDhcpOptions(),
							(lease.getIatype() == IdentityAssoc.V4_TYPE)));
				ps.setBytes(i++, encodeOptions(lease.getIaAddrDhcpOptions(),
							(lease.getIatype() == IdentityAssoc.V4_TYPE)));
				ps.setBytes(i++, lease.getIpAddress().getAddress());
			}
		});
		log.debug("Updated " + cnt + " dhcplease objects");
		return cnt;
	}
	
	/**
	 * Delete dhcp lease.
	 *
	 * @param lease the lease
	 */
	public int deleteDhcpLease(final DhcpLease lease)
	{
		int cnt = getJdbcTemplate().update("delete from dhcplease" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, lease.getIpAddress().getAddress());
			}
		});
		log.debug("Deleted " + cnt + " dhcplease objects");
		return cnt;
	}
	
	/**
	 * Update ia options.
	 */
	public int updateIaOptions(final InetAddress inetAddr, 
							   final Collection<DhcpOption> iaOptions)
	{
		int cnt = getJdbcTemplate().update("update dhcplease" +
				" set ia_options=?" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, encodeOptions(iaOptions,
							inetAddr instanceof Inet4Address));
				ps.setBytes(2, inetAddr.getAddress());
			}
		});
		log.debug("Updated ia_options in " + cnt + " dhcplease objects");
		return cnt;
	}
	
	/**
	 * Update ipaddr options.
	 */
	public int updateIpAddrOptions(final InetAddress inetAddr,
								   final Collection<DhcpOption> ipAddrOptions)
	{
		int cnt = getJdbcTemplate().update("update dhcplease" +
				" set ipaddr_options=?" +
				" where ipaddress=?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps)
					throws SQLException {
				ps.setBytes(1, encodeOptions(ipAddrOptions,
							inetAddr instanceof Inet4Address));
				ps.setBytes(2, inetAddr.getAddress());
			}
		});
		log.debug("Updated ipaddr_options in " + cnt + " dhcplease objects");
		return cnt;
	}

	/**
	 * Find dhcp leases for ia.
	 *
	 * @param duid the duid
	 * @param iatype the iatype
	 * @param iaid the iaid
	 * @return the list
	 */
	public List<DhcpLease> findDhcpLeasesForIA(final byte[] duid, final byte iatype, final long iaid)
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
	public DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr)
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

	@Override
	public int updateIpAddress(final InetAddress inetAddr, 
							   final byte state, final byte haPeerState, final short prefixlen,
							   final Date start, final Date preferred, final Date valid,
							   Collection<DhcpOption> ipAddrOptions) {
		int cnt = getJdbcTemplate().update("update dhcplease" +
				" set state = ?," +
				" hapeerstate = ?," +
				((prefixlen > 0) ? " prefixlen = ?," : "") + 
				" starttime = ?," +
				" preferredendtime = ?," +
				" validendtime = ?," +
				" ipaddr_options = ?" +
				" where ipaddress = ?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int i = 1;
				ps.setByte(i++, state);
				ps.setByte(i++, haPeerState);
				if (prefixlen > 0) {
					ps.setShort(i++, prefixlen);
				}
				if (start != null) {
					java.sql.Timestamp sts = new java.sql.Timestamp(start.getTime());
					ps.setTimestamp(i++, sts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				if (preferred != null) {
					java.sql.Timestamp pts = new java.sql.Timestamp(preferred.getTime());
					ps.setTimestamp(i++, pts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				if (valid != null) {
					java.sql.Timestamp vts = new java.sql.Timestamp(valid.getTime());
					ps.setTimestamp(i++, vts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				ps.setBytes(i++, encodeOptions(ipAddrOptions,
							inetAddr instanceof Inet4Address));
				ps.setBytes(i++, inetAddr.getAddress());
			}
		});
		log.debug("Updated ipAddress in " + cnt + " dhcplease objects");
		return cnt;
	}

	@Override
	public int deleteIpAddress(final InetAddress inetAddr)
	{
		int cnt = getJdbcTemplate().update("delete from dhcplease" +
				" where ipaddress = ?",
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setBytes(1, inetAddr.getAddress());
			}
		});
		log.info("Deleted " + cnt + " dhcplease objects by ipAddress");
		return cnt;
	}

	@Override
	public List<InetAddress> findExistingLeaseIPs(final InetAddress startAddr, 
													final InetAddress endAddr)
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

	@Override
	public void findExistingLeaseIPs(final InetAddress startAddr, 
									 final InetAddress endAddr,
									 InetAddressCallbackHandler inetAddressCallbackHandler)
	{
        getJdbcTemplate().query(
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
                new RowCallbackHandler() {
                	@Override
                	public void processRow(ResultSet rs) throws SQLException {
                    	InetAddress inetAddr = null;
                    	try {
                			inetAddr = InetAddress.getByAddress(rs.getBytes("ipaddress"));
                			inetAddressCallbackHandler.processInetAddress(inetAddr);
                		} 
                    	catch (IOException e) {
                    		// re-throw as SQLException
                			throw new SQLException("Unable to map ipaddress", e);
                		}
                	}
                });
	}

	@Override
	public void findExistingLeases(final InetAddress startAddr, 
								   final InetAddress endAddr,
								   DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler)
	{
        getJdbcTemplate().query(
                "select * from dhcplease" +
                " where ipaddress >= ? and ipaddress <= ?" +
                " order by ipaddress", 
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setBytes(1, startAddr.getAddress());
						ps.setBytes(2, endAddr.getAddress());
					}                	
                },
                new RowCallbackHandler() {
                	@Override
                	public void processRow(ResultSet rs) throws SQLException {
                    	try {
                	    	ResultSetExtractor<DhcpLease> rsExtractor = 
                	    			new DhcpLeaseResultSetExtractor();
                	    	DhcpLease dhcpLease = rsExtractor.extractData(rs);
                			dhcpLeaseCallbackHandler.processDhcpLease(dhcpLease);
                		} 
                    	catch (Exception e) {
                    		// re-throw as SQLException
                			throw new SQLException("Unable to map ipaddress", e);
                		}
                	}
                });
	}

	@Override
	public void findUnsyncedLeases(final InetAddress startAddr, 
								   final InetAddress endAddr,
								   DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler)
	{
        getJdbcTemplate().query(
                "select * from dhcplease" +
                " where hapeerstate = " + IaAddress.UNKNOWN +
                " and ipaddress >= ? and ipaddress <= ?" +
                " order by ipaddress", 
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setBytes(1, startAddr.getAddress());
						ps.setBytes(2, endAddr.getAddress());
					}                	
                },
                new RowCallbackHandler() {
                	@Override
                	public void processRow(ResultSet rs) throws SQLException {
                    	try {
                	    	ResultSetExtractor<DhcpLease> rsExtractor = 
                	    			new DhcpLeaseResultSetExtractor();
                	    	DhcpLease dhcpLease = rsExtractor.extractData(rs);
                			dhcpLeaseCallbackHandler.processDhcpLease(dhcpLease);
                		} 
                    	catch (Exception e) {
                    		// re-throw as SQLException
                			throw new SQLException("Unable to map ipaddress", e);
                		}
                	}
                });
	}
	
	@Override
	public int setAllLeasesUnsynced() {
		return getJdbcTemplate().update("update dhcplease" +
								 		" set hapeerstate=" + IaAddress.UNKNOWN);
	}
	
	@Override
	public List<DhcpLease> findUnusedLeases(final InetAddress startAddr, final InetAddress endAddr)
	{
		final long offerExpiration = new Date().getTime() - offerExpireMillis;
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where ((state=" + IaAddress.AVAILABLE + ")" +
                " or (state=" + IaAddress.OFFERED +
                " and starttime <= ?))" +
                " and ipaddress >= ? and ipaddress <= ?" +
                " order by state, validendtime, ipaddress",
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						java.sql.Timestamp ts = new java.sql.Timestamp(offerExpiration);
						ps.setTimestamp(1, ts, Util.GMT_CALENDAR);
						ps.setBytes(2, startAddr.getAddress());
						ps.setBytes(3, endAddr.getAddress());
					}                	
                },
                new DhcpLeaseRowMapper());
		return leases;
	}

	@Override
	/*
	public DhcpLease findUnusedLease(InetAddress startAddr, InetAddress endAddr) {
		final long offerExpiration = new Date().getTime() - offerExpireMillis;
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where ((state=" + IaAddress.AVAILABLE + ")" +
                " or (state=" + IaAddress.OFFERED +
                " and starttime <= ?))" +
                " and ipaddress >= ? and ipaddress <= ?" +
//                " order by state, validendtime, ipaddress" + 
                LIMIT_ONE_CLAUSE,
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						java.sql.Timestamp ts = new java.sql.Timestamp(offerExpiration);
						ps.setTimestamp(1, ts, Util.GMT_CALENDAR);
						ps.setBytes(2, startAddr.getAddress());
						ps.setBytes(3, endAddr.getAddress());
					}                	
                },
                new DhcpLeaseRowMapper());
		if ((leases != null) && leases.size() > 0) {
			return leases.get(0);
		}
		return null;	
	}
	*/
	public DhcpLease findUnusedLease(InetAddress startAddr, InetAddress endAddr) {
        List<DhcpLease> leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where state=" + IaAddress.AVAILABLE +
                " and ipaddress >= ? and ipaddress <= ?" +
//                " order by state, validendtime, ipaddress" + 
                LIMIT_ONE_CLAUSE,
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setBytes(1, startAddr.getAddress());
						ps.setBytes(2, endAddr.getAddress());
					}                	
                },
                new DhcpLeaseRowMapper());
		if ((leases != null) && leases.size() > 0) {
			return leases.get(0);
		}
		// no "available" leases, now see if any offers have expired
		// using a separate query instead of 'or' clause, because 'or'
		// appears to impact performance in the nominal case
		final long offerExpiration = new Date().getTime() - offerExpireMillis;
        leases = getJdbcTemplate().query(
                "select * from dhcplease" +
                " where (state=" + IaAddress.OFFERED +
                " and starttime <= ?)" +
                " and ipaddress >= ? and ipaddress <= ?" +
//                " order by state, validendtime, ipaddress" + 
                LIMIT_ONE_CLAUSE,
                new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						java.sql.Timestamp ts = new java.sql.Timestamp(offerExpiration);
						ps.setTimestamp(1, ts, Util.GMT_CALENDAR);
						ps.setBytes(2, startAddr.getAddress());
						ps.setBytes(3, endAddr.getAddress());
					}                	
                },
                new DhcpLeaseRowMapper());
		if ((leases != null) && leases.size() > 0) {
			return leases.get(0);
		}
		return null;		
	}
	
	public List<DhcpLease> findExpiredLeases(final byte iatype) {
        return getJdbcTemplate().query(
                "select * from dhcplease" +
                " where iatype = ?" +
                " and state = " + IaAddress.LEASED +
                " and validendtime < ?" +
                " order by validendtime",
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
	
	@Override
	public void reconcileLeases(final List<Range> ranges) {
		// TODO: what if the query string is huge?
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
		int cnt = getJdbcTemplate().update(query.toString(), args.toArray());
		log.info("Deleted " + cnt + " dhcplease objects by reconciling on configured pool ranges");		
		
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
			lease.setHaPeerState(rs.getByte("hapeerstate"));
			lease.setStartTime(rs.getTimestamp("starttime", Util.GMT_CALENDAR));
			lease.setPreferredEndTime(rs.getTimestamp("preferredendtime", Util.GMT_CALENDAR));
			lease.setValidEndTime(rs.getTimestamp("validendtime", Util.GMT_CALENDAR));
			lease.setDhcpOptions(decodeOptions(rs.getBytes("options"), 
								(lease.getIatype() == IdentityAssoc.V4_TYPE)));
			lease.setIaDhcpOptions(decodeOptions(rs.getBytes("ia_options"), 
								(lease.getIatype() == IdentityAssoc.V4_TYPE)));
			lease.setIaAddrDhcpOptions(decodeOptions(rs.getBytes("ipaddr_options"), 
								(lease.getIatype() == IdentityAssoc.V4_TYPE)));
            return lease;
		};
    }

	/**
	 * For unit tests only
	 */
    @Override
	public int deleteAllLeases() {
		int cnt = getJdbcTemplate().update("delete from dhcplease");
		log.info("Deleted all " + cnt + " dhcpleases");
		return cnt;
	}
}
