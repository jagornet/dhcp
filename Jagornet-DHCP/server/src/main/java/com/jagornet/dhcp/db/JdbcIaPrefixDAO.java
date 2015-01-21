/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIaPrefixDAO.java is part of Jagornet DHCP.
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
 * The JdbcIaPrefixDAO implementation class for the IaPrefixDAO interface.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcIaPrefixDAO extends JdbcDaoSupport implements IaPrefixDAO 
{
    private static Logger log = LoggerFactory.getLogger(JdbcIaPrefixDAO.class);

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#create(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void create(final IaPrefix iaPrefix) 
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
						PreparedStatement ps = conn.prepareStatement("insert into iaprefix" +
								" (prefixaddress, prefixlength, starttime, preferredendtime," +
								" validendtime, state, identityassoc_id)" +
								" values (?, ?, ?, ?, ?, ?, ?)", 
								PreparedStatement.RETURN_GENERATED_KEYS);
						ps.setBytes(1, iaPrefix.getIpAddress().getAddress());
						ps.setInt(2, iaPrefix.getPrefixLength());
						java.sql.Timestamp sts = 
							new java.sql.Timestamp(iaPrefix.getStartTime().getTime());
						ps.setTimestamp(3, sts, Util.GMT_CALENDAR);
						java.sql.Timestamp pts = 
							new java.sql.Timestamp(iaPrefix.getPreferredEndTime().getTime());
						ps.setTimestamp(4, pts, Util.GMT_CALENDAR);
						java.sql.Timestamp vts = 
							new java.sql.Timestamp(iaPrefix.getValidEndTime().getTime());
						ps.setTimestamp(5, vts, Util.GMT_CALENDAR);
						ps.setByte(6, iaPrefix.getState());
						ps.setLong(7, iaPrefix.getIdentityAssocId());
						return ps;
					}
				}, 
				newKey);
		Number newId = newKey.getKey();
		if (newId != null) {
			iaPrefix.setId(newId.longValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#update(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void update(final IaPrefix iaPrefix)
	{
		String updateQuery = "update iaprefix" +
							" set prefixaddress=?," +
							" prefixlength=?," +
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
				ps.setBytes(1, iaPrefix.getIpAddress().getAddress());
				ps.setInt(2, iaPrefix.getPrefixLength());
				Date start = iaPrefix.getStartTime();
				if (start != null) {
					java.sql.Timestamp sts = new java.sql.Timestamp(start.getTime());
					ps.setTimestamp(3, sts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(3, java.sql.Types.TIMESTAMP);
				}
				Date preferred = iaPrefix.getPreferredEndTime();
				if (preferred != null) {
					java.sql.Timestamp pts = new java.sql.Timestamp(preferred.getTime());
					ps.setTimestamp(4, pts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(4, java.sql.Types.TIMESTAMP);
				}
				Date valid = iaPrefix.getValidEndTime();
				if (valid != null) {
					java.sql.Timestamp vts = new java.sql.Timestamp(valid.getTime());
					ps.setTimestamp(5, vts, Util.GMT_CALENDAR);
				}
				else {
					ps.setNull(5, java.sql.Types.TIMESTAMP);
				}
				ps.setByte(6, iaPrefix.getState());
				ps.setLong(7, iaPrefix.getIdentityAssocId());
				ps.setLong(8, iaPrefix.getId());
			}			
		});
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#getById()
	 */
	public IaPrefix getById(Long id) 
	{
		try {
		    return getJdbcTemplate().queryForObject(
		            "select * from iaprefix where id = ?", 
		            new IaPrefixRowMapper(), id);
		}
		catch (EmptyResultDataAccessException ex) {
			log.warn("IaPrefix not found for ID=" + id + ": " + ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#getByInetAddress(InetAddress)
	 */
	public IaPrefix getByInetAddress(InetAddress inetAddr) 
	{
        return getJdbcTemplate().queryForObject(
                "select * from iaprefix where ipprefix = ?", 
                new IaPrefixRowMapper(), inetAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getJdbcTemplate().update(
        		"delete from iaprefix where id = ?", id);
    }

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#deleteByInetAddress(java.net.InetAddress)
	 */
	public void deleteByInetAddress(InetAddress inetAddr) {
        getJdbcTemplate().update(
        		"delete from iaprefix where prefixaddress = ?", 
        		inetAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#deleteNotInRanges(java.util.List)
	 */
	public void deleteNotInRanges(List<Range> ranges)
	{
		List<byte[]> args = new ArrayList<byte[]>();
		StringBuilder query = new StringBuilder();
		query.append("delete from iaprefix where prefixaddress");
		Iterator<Range> rangeIter = ranges.iterator();
		while (rangeIter.hasNext()) {
			Range range = rangeIter.next();
			query.append(" not between ? and ?");
			args.add(range.getStartAddress().getAddress());
			args.add(range.getEndAddress().getAddress());
			if (rangeIter.hasNext())
				query.append(" and prefixaddress");
		}
		getJdbcTemplate().update(query.toString(), args.toArray());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findAllByIdentityAssocId(long)
	 */
	public List<IaPrefix> findAllByIdentityAssocId(final long identityAssocId)
	{
        return getJdbcTemplate().query(
                "select * from iaprefix where identityassoc_id = ? order by prefixaddress",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setLong(1, identityAssocId);
            		}
            	},
                new IaPrefixRowMapper());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findAllBySubnet(com.jagornet.dhcpv6.util.Subnet)
	 */
	public List<IaPrefix> findAllBySubnet(Subnet subnet)
	{
        return findAllByRange(subnet.getSubnetAddress(), subnet.getEndAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findAllByRange(java.net.InetAddress, java.net.InetAddress)
	 */
	public List<IaPrefix> findAllByRange(final InetAddress startAddr, final InetAddress endAddr)
	{
        return getJdbcTemplate().query(
                "select * from iaprefix" +
                " where prefixaddress >= ? and prefixaddress <= ?" +
                " order by prefixaddress",
                new PreparedStatementSetter() {
                	@Override
                	public void setValues(PreparedStatement ps) throws SQLException {
                		ps.setBytes(1, startAddr.getAddress());
                		ps.setBytes(2, endAddr.getAddress());
                	}
                },
                new IaPrefixRowMapper());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findAllOlderThan(java.util.Date)
	 */
	public List<IaPrefix> findAllOlderThan(Date date)
	{
        return getJdbcTemplate().query(
                "select * from iaprefix" +
                " join identityassoc ia on identityassoc_id=ia.id" +
                " where ia.iatype = ?" +
                " and validendtime < ? order by validendtime",
                new PreparedStatementSetter() {
            		@Override
            		public void setValues(PreparedStatement ps) throws SQLException {
            			ps.setByte(1, IdentityAssoc.PD_TYPE);
            			java.sql.Timestamp ts = new java.sql.Timestamp(new Date().getTime());
            			ps.setTimestamp(2, ts, Util.GMT_CALENDAR);
            		}
                },
                new IaPrefixRowMapper());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findUnusedByRange(java.net.InetAddress, java.net.InetAddress)
	 */
	public List<IaPrefix> findUnusedByRange(final InetAddress startAddr, final InetAddress endAddr)
	{
		final long offerExpiration = new Date().getTime() - 12000;	// 2 min = 120 sec = 12000 ms
        return getJdbcTemplate().query(
                "select * from iaprefix" +
                " where ((state=" + IaPrefix.ADVERTISED +
                " and starttime <= ?)" +
                " or (state=" + IaPrefix.EXPIRED +
                " or state=" + IaPrefix.RELEASED + "))" +
                " and prefixaddress >= ? and prefixaddress <= ?" +
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
                new IaPrefixRowMapper());
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findExistingIPs(java.net.InetAddress, java.net.InetAddress)
	 */
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr)
	{
        return getJdbcTemplate().query(
                "select prefixaddress from iaprefix" +
                " where prefixaddress >= ? and prefixaddress <= ?" +
                " order by prefixaddress", 
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
                			inetAddr = InetAddress.getByAddress(rs.getBytes("prefixaddress"));
                		} 
                    	catch (UnknownHostException e) {
                    		// re-throw as SQLException
                			throw new SQLException("Unable to map prefixaddress", e);
                		}
                        return inetAddr;
                    }
                });
	}

    /**
     * The Class IaPrefixRowMapper.
     */
    protected class IaPrefixRowMapper implements RowMapper<IaPrefix> 
    {
    	
	    /* (non-Javadoc)
	     * @see org.springframework.jdbc.core.simple.RowMapper#mapRow(java.sql.ResultSet, int)
	     */
	    @Override
        public IaPrefix mapRow(ResultSet rs, int rowNum) throws SQLException {
        	IaPrefix iaPrefix = new IaPrefix();
        	iaPrefix.setId(rs.getLong("id"));
        	try {
				iaPrefix.setIpAddress(InetAddress.getByAddress(rs.getBytes("prefixaddress")));
			}
        	catch (UnknownHostException e) {
				// re-throw as SQLException
				throw new SQLException("Unable to map ipaddress", e);
			}
			iaPrefix.setPrefixLength(rs.getShort("prefixlength"));
			iaPrefix.setStartTime(rs.getTimestamp("starttime", Util.GMT_CALENDAR));
			iaPrefix.setPreferredEndTime(rs.getTimestamp("preferredendtime", Util.GMT_CALENDAR));
			iaPrefix.setValidEndTime(rs.getTimestamp("validendtime", Util.GMT_CALENDAR));
			iaPrefix.setState(rs.getByte("state"));
			iaPrefix.setIdentityAssocId(rs.getLong("identityassoc_id"));
            return iaPrefix;
        }
    }
}
