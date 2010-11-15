/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIaAddressDAO.java is part of DHCPv6.
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
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.jagornet.dhcpv6.server.request.binding.Range;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;

/**
 * The JdbcIaAddressDAO implementation class for the IaAddressDAO interface.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcIaAddressDAO extends SimpleJdbcDaoSupport implements IaAddressDAO 
{
    private static Logger log = LoggerFactory.getLogger(JdbcIaAddressDAO.class);

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#create(com.jagornet.dhcpv6.db.IaAddress)
	 */

	public void create(IaAddress iaAddr) 
	{
		String insertQuery = "insert into iaaddress" +
				" (ipaddress, starttime, preferredendtime, validendtime, state, identityassoc_id)" +
				" values (?, ?, ?, ?, ?, ?)";
		
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
				new InsertIaAddrPreparedStatementCreator(insertQuery, iaAddr), 
				newKey);
		Number newId = newKey.getKey();
		if (newId != null) {
			iaAddr.setId(newId.longValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#update(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void update(IaAddress iaAddr)
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
				new UpdateIaAddrPreparedStatementSetter(iaAddr));
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#getById()
	 */
	public IaAddress getById(Long id) 
	{
		try {
		    return getSimpleJdbcTemplate().queryForObject(
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
        return getSimpleJdbcTemplate().queryForObject(
                "select * from iaaddress where ipaddress = ?", 
                new IaAddrRowMapper(), inetAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getSimpleJdbcTemplate().update(
        		"delete from iaaddress where id = ?", id);
    }

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#deleteByInetAddress(java.net.InetAddress)
	 */
	public void deleteByInetAddress(InetAddress inetAddr) {
        getSimpleJdbcTemplate().update(
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
	public List<IaAddress> findAllByIdentityAssocId(long identityAssocId)
	{
        return getSimpleJdbcTemplate().query(
                "select * from iaaddress where identityassoc_id = ? order by ipaddress", 
                new IaAddrRowMapper(), identityAssocId);
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
	public List<IaAddress> findAllByRange(InetAddress startAddr, InetAddress endAddr)
	{
        return getSimpleJdbcTemplate().query(
                "select * from iaaddress where ipaddress >= ? and ipaddress <= ? order by ipaddress", 
                new IaAddrRowMapper(), 
                startAddr.getAddress(), 
                endAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddressDAO#findAllOlderThan(java.util.Date)
	 */
	public List<IaAddress> findAllOlderThan(Date date)
	{
        return getJdbcTemplate().query(
                "select * from iaaddress where validendtime < ? order by validendtime",
                new GmtTimestampPreparedStatementSetter(date),
                new IaAddrRowMapper());
	}
	
	/**
	 * The Class InsertIaAddrPreparedStatementCreator.
	 */
	protected class InsertIaAddrPreparedStatementCreator implements PreparedStatementCreator
	{
		/** The sql. */
		private String sql;
		
		/** The ia addr. */
		private IaAddress iaAddr;
		
		/**
		 * Instantiates a new insert ia addr prepared statement setter.
		 * 
		 * @param sql the sql
		 * @param iaAddr the ia addr
		 */
		public InsertIaAddrPreparedStatementCreator(String sql, IaAddress iaAddr) {
			this.sql = sql;
			this.iaAddr = iaAddr;
		}
		
		@Override
		public PreparedStatement createPreparedStatement(Connection conn)
				throws SQLException
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
			PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setBytes(1, iaAddr.getIpAddress().getAddress());
			java.sql.Timestamp sts = new java.sql.Timestamp(iaAddr.getStartTime().getTime());
			ps.setTimestamp(2, sts, Util.GMT_CALENDAR);
			java.sql.Timestamp pts = new java.sql.Timestamp(iaAddr.getPreferredEndTime().getTime());
			ps.setTimestamp(3, pts, Util.GMT_CALENDAR);
			java.sql.Timestamp vts = new java.sql.Timestamp(iaAddr.getValidEndTime().getTime());
			ps.setTimestamp(4, vts, Util.GMT_CALENDAR);
			ps.setByte(5, iaAddr.getState());
			ps.setLong(6, iaAddr.getIdentityAssocId());
			return ps;
		}
	}
	
	/**
	 * The Class UpdateIaAddrPreparedStatementSetter.
	 */
	protected class UpdateIaAddrPreparedStatementSetter implements PreparedStatementSetter
	{
		
		/** The ia addr. */
		private IaAddress iaAddr;
		
		/**
		 * Instantiates a new update ia addr prepared statement setter.
		 * 
		 * @param iaAddr the ia addr
		 */
		public UpdateIaAddrPreparedStatementSetter(IaAddress iaAddr) {
			this.iaAddr = iaAddr;
		}		
		
		/* (non-Javadoc)
		 * @see org.springframework.jdbc.core.PreparedStatementSetter#setValues(java.sql.PreparedStatement)
		 */
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
		
	}
	
	/**
	 * The Class GmtTimestampPreparedStatementSetter.
	 */
	protected class GmtTimestampPreparedStatementSetter implements PreparedStatementSetter
	{
		
		/** The date. */
		private Date date;
		
		/**
		 * Instantiates a new gmt timestamp prepared statement setter.
		 * 
		 * @param date the date
		 */
		public GmtTimestampPreparedStatementSetter(Date date) {
			this.date = date;
		}
		
		/* (non-Javadoc)
		 * @see org.springframework.jdbc.core.PreparedStatementSetter#setValues(java.sql.PreparedStatement)
		 */
		@Override
		public void setValues(PreparedStatement ps) throws SQLException {
			java.sql.Timestamp ts = new java.sql.Timestamp(date.getTime());
			ps.setTimestamp(1, ts, Util.GMT_CALENDAR);
		}
		
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
