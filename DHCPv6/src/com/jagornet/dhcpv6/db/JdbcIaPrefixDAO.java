/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIaPrefixDAO.java is part of DHCPv6.
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
 * The JdbcIaPrefixDAO implementation class for the IaPrefixDAO interface.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcIaPrefixDAO extends SimpleJdbcDaoSupport implements IaPrefixDAO 
{
    private static Logger log = LoggerFactory.getLogger(JdbcIaPrefixDAO.class);

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#create(com.jagornet.dhcpv6.db.IaPrefix)
	 */

	public void create(IaPrefix iaPrefix) 
	{
		String insertQuery = "insert into iaprefix" +
				" (prefixaddress, prefixlength, starttime, preferredendtime," +
				" validendtime, state, identityassoc_id)" +
				" values (?, ?, ?, ?, ?, ?, ?)";
		
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
				new InsertIaPrefixPreparedStatementCreator(insertQuery, iaPrefix), 
				newKey);
		Number newId = newKey.getKey();
		if (newId != null) {
			iaPrefix.setId(newId.longValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#update(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void update(IaPrefix iaPrefix)
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
				new UpdateIaPrefixPreparedStatementSetter(iaPrefix));
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#getById()
	 */
	public IaPrefix getById(Long id) 
	{
		try {
		    return getSimpleJdbcTemplate().queryForObject(
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
        return getSimpleJdbcTemplate().queryForObject(
                "select * from iaprefix where ipprefix = ?", 
                new IaPrefixRowMapper(), inetAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getSimpleJdbcTemplate().update(
        		"delete from iaprefix where id = ?", id);
    }

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#deleteByInetAddress(java.net.InetAddress)
	 */
	public void deleteByInetAddress(InetAddress inetAddr) {
        getSimpleJdbcTemplate().update(
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
	public List<IaPrefix> findAllByIdentityAssocId(long identityAssocId)
	{
        return getSimpleJdbcTemplate().query(
                "select * from iaprefix where identityassoc_id = ? order by prefixaddress", 
                new IaPrefixRowMapper(), identityAssocId);
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
	public List<IaPrefix> findAllByRange(InetAddress startAddr, InetAddress endAddr)
	{
        return getSimpleJdbcTemplate().query(
                "select * from iaprefix" +
                " where prefixaddress >= ? and prefixaddress <= ?" +
                " order by prefixaddress", 
                new IaPrefixRowMapper(), 
                startAddr.getAddress(), 
                endAddr.getAddress());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaPrefixDAO#findAllOlderThan(java.util.Date)
	 */
	public List<IaPrefix> findAllOlderThan(Date date)
	{
        return getJdbcTemplate().query(
                "select * from iaprefix where validendtime < ? order by validendtime",
                new GmtTimestampPreparedStatementCreator(date),
                new IaPrefixRowMapper());
	}
	
	/**
	 * The Class InsertIaPrefixPreparedStatementCreator.
	 */
	protected class InsertIaPrefixPreparedStatementCreator implements PreparedStatementCreator
	{
		/** The sql. */
		private String sql;
		
		/** The ia prefix. */
		private IaPrefix iaPrefix;
		
		/**
		 * Instantiates a new insert ia prefix prepared statement creator.
		 *
		 * @param sql the sql
		 * @param iaPrefix the ia prefix
		 */
		public InsertIaPrefixPreparedStatementCreator(String sql, IaPrefix iaPrefix) {
			this.sql = sql;
			this.iaPrefix = iaPrefix;
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
			ps.setBytes(1, iaPrefix.getIpAddress().getAddress());
			ps.setInt(2, iaPrefix.getPrefixLength());
			java.sql.Timestamp sts = new java.sql.Timestamp(iaPrefix.getStartTime().getTime());
			ps.setTimestamp(3, sts, Util.GMT_CALENDAR);
			java.sql.Timestamp pts = new java.sql.Timestamp(iaPrefix.getPreferredEndTime().getTime());
			ps.setTimestamp(4, pts, Util.GMT_CALENDAR);
			java.sql.Timestamp vts = new java.sql.Timestamp(iaPrefix.getValidEndTime().getTime());
			ps.setTimestamp(5, vts, Util.GMT_CALENDAR);
			ps.setByte(6, iaPrefix.getState());
			ps.setLong(7, iaPrefix.getIdentityAssocId());
			return ps;
		}
	}
	
	/**
	 * The Class UpdateIaPrefixPreparedStatementSetter.
	 */
	protected class UpdateIaPrefixPreparedStatementSetter implements PreparedStatementSetter
	{
		
		/** The ia prefix. */
		private IaPrefix iaPrefix;
		
		/**
		 * Instantiates a new update ia prefix prepared statement setter.
		 * 
		 * @param iaPrefix the ia prefix
		 */
		public UpdateIaPrefixPreparedStatementSetter(IaPrefix iaPrefix) {
			this.iaPrefix = iaPrefix;
		}		
		
		/* (non-Javadoc)
		 * @see org.springframework.jdbc.core.PreparedStatementSetter#setValues(java.sql.PreparedStatement)
		 */
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
		
	}
	
	/**
	 * The Class GmtTimestampPreparedStatementCreator.
	 */
	protected class GmtTimestampPreparedStatementCreator implements PreparedStatementSetter
	{
		
		/** The date. */
		private Date date;
		
		/**
		 * Instantiates a new gmt timestamp prepared statement creator.
		 * 
		 * @param date the date
		 */
		public GmtTimestampPreparedStatementCreator(Date date) {
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
