/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIaAddressDAO.java is part of Jagornet DHCP.
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.jagornet.dhcp.server.request.binding.Range;
import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.util.Util;

/**
 * The JdbcIaAddressDAO implementation class for the IaAddressDAO interface.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcIaAddressDAO extends JdbcDaoSupport implements IaAddressDAO 
{
    private static Logger log = LoggerFactory.getLogger(JdbcIaAddressDAO.class);

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#create(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void create(final IaAddress iaAddr) 
	{
		/**
		 * Note: see https://issues.apache.org/jira/browse/DERBY-3609
		 * "Formally, Derby does not support getGeneratedKeys since 
		 * DatabaseMetaData.supportsGetGeneratedKeys() returns false. 
		 * However, Statement.getGeneratedKeys() is partially implemented,
		 * ... since it will only return a meaningful result when an single 
		 * row insert is done with INSERT...VALUES"
		 * 
		 * Spring has thus provided a workaround as described here:
		 * http://jira.springframework.org/browse/SPR-5306
		 */
		GeneratedKeyHolder newKey = new GeneratedKeyHolder();
		getJdbcTemplate().update(
				new PreparedStatementCreator() {					
					@Override
					public PreparedStatement createPreparedStatement(Connection conn)
							throws SQLException
					{
						PreparedStatement ps = conn.prepareStatement("insert into iaaddress" +
								" (ipaddress, starttime, preferredendtime, validendtime," +
								" state, identityassoc_id)" +
								" values (?, ?, ?, ?, ?, ?)", 
								PreparedStatement.RETURN_GENERATED_KEYS);
						ps.setBytes(1, iaAddr.getIpAddress().getAddress());
						java.sql.Timestamp sts = 
							new java.sql.Timestamp(iaAddr.getStartTime().getTime());
						ps.setTimestamp(2, sts, Util.GMT_CALENDAR);
						java.sql.Timestamp pts = 
							new java.sql.Timestamp(iaAddr.getPreferredEndTime().getTime());
						ps.setTimestamp(3, pts, Util.GMT_CALENDAR);
						java.sql.Timestamp vts = 
							new java.sql.Timestamp(iaAddr.getValidEndTime().getTime());
						ps.setTimestamp(4, vts, Util.GMT_CALENDAR);
						ps.setByte(5, iaAddr.getState());
						ps.setLong(6, iaAddr.getIdentityAssocId());
						return ps;
					}
				}, 
				newKey);
		Number newId = newKey.getKey();
		if (newId != null) {
			iaAddr.setId(newId.longValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#update(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void update(final IaAddress iaAddr)
	{
		String updateQuery = "update iaaddress" +
							" set ipaddress=?," +
							" starttime=?," +
							" preferredendtime=?," +
							" validendtime=?," +
							" state=?," +
							" identityassoc_id=?" +
							" where id=?";
		getJdbcTemplate().update(updateQuery, 
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setBytes(1, iaAddr.getIpAddress().getAddress());
				Date start = iaAddr.getStartTime();
				if (start != null) {
					java.sql.Timestamp sts = new java.sql.Timestamp(start.getTime());
					ps.setTimestamp(2, sts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(2, java.sql.Types.TIMESTAMP);
				}
				Date preferred = iaAddr.getPreferredEndTime();
				if (preferred != null) {
					java.sql.Timestamp pts = new java.sql.Timestamp(preferred.getTime());
					ps.setTimestamp(3, pts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(3, java.sql.Types.TIMESTAMP);
				}
				Date valid = iaAddr.getValidEndTime();
				if (valid != null) {
					java.sql.Timestamp vts = new java.sql.Timestamp(valid.getTime());
					ps.setTimestamp(4, vts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(4, java.sql.Types.TIMESTAMP);
				}
				ps.setByte(5, iaAddr.getState());
				ps.setLong(6, iaAddr.getIdentityAssocId());
				ps.setLong(7, iaAddr.getId());
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#getById()
	 */
	public IaAddress getById(Long id) 
	{
		try {
		    return getJdbcTemplate().queryForObject(
		            "select * from iaaddress where id = ?", 
		            new IaAddrRowMapper(), id);
		}
		catch (EmptyResultDataAccessException ex) {
			log.warn("IaAddress not found for ID=" + id + ": " + ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#getByInetAddress(InetAddress)
	 */
	public IaAddress getByInetAddress(InetAddress inetAddr) 
	{
        return getJdbcTemplate().queryForObject(
                "select * from iaaddress where ipaddress = ?", 
                new IaAddrRowMapper(), inetAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getJdbcTemplate().update(
        		"delete from iaaddress where id = ?", id);
    }

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#deleteByInetAddress(java.net.InetAddress)
	 */
	public void deleteByInetAddress(InetAddress inetAddr) {
        getJdbcTemplate().update(
        		"delete from iaaddress where ipaddress = ?", 
        		inetAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#deleteNotInRanges(java.util.List)
	 */
	public void deleteNotInRanges(List<Range> ranges)
	{
		List<byte[]> args = new ArrayList<byte[]>();
		StringBuilder query = new StringBuilder();
		query.append("delete from iaaddress where ipaddress");
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

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findAllByIdentityAssocId(long)
	 */
	public List<IaAddress> findAllByIdentityAssocId(final long identityAssocId)
	{
        return getJdbcTemplate().query(
                "select * from iaaddress where identityassoc_id = ? order by ipaddress",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setLong(1, identityAssocId);
            		}
            	},
                new IaAddrRowMapper());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findAllBySubnet(com.jagornet.dhcpv6.util.Subnet)
	 */
	public List<IaAddress> findAllBySubnet(Subnet subnet)
	{
        return findAllByRange(subnet.getSubnetAddress(), subnet.getEndAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findAllByRange(java.net.InetAddress, java.net.InetAddress)
	 */
	public List<IaAddress> findAllByRange(final InetAddress startAddr, final InetAddress endAddr)
	{
        return getJdbcTemplate().query(
                "select * from iaaddress where ipaddress >= ? and ipaddress <= ? order by ipaddress",
                new PreparedStatementSetter() {
                	@Override
                	public void setValues(PreparedStatement ps) throws SQLException {
                		ps.setBytes(1, startAddr.getAddress());
                		ps.setBytes(2, endAddr.getAddress());
                	}
                },
                new IaAddrRowMapper());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findExpiredAddresses(byte)
	 */
	public List<IaAddress> findExpiredAddresses(final byte iatype)
	{
        return getJdbcTemplate().query(
                "select * from iaaddress a" +
                " join identityassoc ia on ia.id=a.identityassoc_id" +
                " where ia.iatype = ?" +
                " and a.state != " + IaAddress.STATIC +
                " and a.validendtime < ? order by a.validendtime",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setByte(1, iatype);
            			java.sql.Timestamp ts = new java.sql.Timestamp(new Date().getTime());
            			ps.setTimestamp(2, ts, Util.GMT_CALENDAR);
            		}
                },
                new IaAddrRowMapper());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findUnusedByRange(java.net.InetAddress, java.net.InetAddress)
	 */
	public List<IaAddress> findUnusedByRange(final InetAddress startAddr, final InetAddress endAddr)
	{
		final long offerExpiration = new Date().getTime() - 12000;	// 2 min = 120 sec = 12000 ms
        return getJdbcTemplate().query(
                "select * from iaaddress" +
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
                new IaAddrRowMapper());
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findExistingIPs(java.net.InetAddress, java.net.InetAddress)
	 */
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr)
	{
        return getJdbcTemplate().query(
                "select ipaddress from iaaddress" +
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

    /**
     * The Class IaAddrRowMapper.
     */
    protected class IaAddrRowMapper implements RowMapper<IaAddress> 
    {
    	
	    /* (non-Javadoc)
	     * @see org.springframework.jdbc.core.simple.RowMapper#mapRow(java.sql.ResultSet, int)
	     */
	    @Override
        public IaAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
        	IaAddress iaAddr = new IaAddress();
        	iaAddr.setId(rs.getLong("id"));
        	try {
				iaAddr.setIpAddress(InetAddress.getByAddress(rs.getBytes("ipaddress")));
			} 
        	catch (UnknownHostException e) {
        		// re-throw as SQLException
				throw new SQLException("Unable to map ipaddress", e);
			}
			iaAddr.setStartTime(rs.getTimestamp("starttime", Util.GMT_CALENDAR));
			iaAddr.setPreferredEndTime(rs.getTimestamp("preferredendtime", Util.GMT_CALENDAR));
			iaAddr.setValidEndTime(rs.getTimestamp("validendtime", Util.GMT_CALENDAR));
			iaAddr.setState(rs.getByte("state"));
			iaAddr.setIdentityAssocId(rs.getLong("identityassoc_id"));
            return iaAddr;
        }
    }
}
