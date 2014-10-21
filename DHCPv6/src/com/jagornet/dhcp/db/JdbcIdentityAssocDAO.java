/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIdentityAssocDAO.java is part of Jagornet DHCP.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.jagornet.dhcp.util.Util;

/**
 * The JdbcIdentityAssocDAO implementation class for the IdentityAssocDAO interface.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcIdentityAssocDAO extends JdbcDaoSupport implements IdentityAssocDAO 
{
    private static Logger log = LoggerFactory.getLogger(JdbcIdentityAssocDAO.class);

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#create(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void create(final IdentityAssoc ia) 
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
						PreparedStatement ps = conn.prepareStatement("insert into identityassoc" +
								" (duid, iatype, iaid, state)" +
								" values (?, ?, ?, ?)",
								PreparedStatement.RETURN_GENERATED_KEYS);
						ps.setBytes(1, ia.getDuid());
						ps.setByte(2, ia.getIatype());
						ps.setLong(3, ia.getIaid());
						ps.setByte(4, ia.getState());
						return ps;
					}
				},
				newKey);
		Number newId = newKey.getKey();
		if (newId != null) {
			ia.setId(newId.longValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#update(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void update(final IdentityAssoc ia)
	{
		String updateQuery = "update identityassoc" +
							" set duid=?," +
							" iatype=?," +
							" iaid=?," +
							" state=?" +
							" where id=?";
		getJdbcTemplate().update(updateQuery,
				new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setBytes(1, ia.getDuid());
				ps.setByte(2, ia.getIatype());
				ps.setLong(3, ia.getIaid());
				ps.setByte(4, ia.getState()); 
				ps.setLong(5, ia.getId());
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getJdbcTemplate().update(
        		"delete from identityassoc where id = ?", id);
    }

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#getById()
	 */
	public IdentityAssoc getById(Long id) 
	{
		try {
		    return getJdbcTemplate().queryForObject(
		            "select * from identityassoc where id = ?", 
		            new IaRowMapper(), id);
		}
		catch (EmptyResultDataAccessException ex) {
			log.warn("IdentityAssoc not found for ID=" + id + ": " + ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#getByKey(byte[], byte, long)
	 */
	public IdentityAssoc getByKey(byte[] duid, byte iatype, long iaid) 
	{
		try {
	        return getJdbcTemplate().queryForObject(
	                "select * from identityassoc where duid = ? and iatype = ? and iaid = ?", 
	                new IaRowMapper(), duid, iatype, iaid);
		}
		catch (EmptyResultDataAccessException ex) {
			// trace level message, because this is expected to happen sometimes
			log.trace("IdenityAssoc not found for DUID=" + Util.toHexString(duid) +
					" IATYPE=" + iatype + " IAID=" + iaid + ": " + ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#findAllByDuid(byte[])
	 */
	public List<IdentityAssoc> findAllByDuid(byte[] duid) {
        return getJdbcTemplate().query(
                "select * from identityassoc where duid = ?", 
                new IaRowMapper(), duid);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#findAllByIaid(long)
	 */
	public List<IdentityAssoc> findAllByIaid(long iaid) {
        return getJdbcTemplate().query(
                "select * from identityassoc where iaid = ?", 
                new IaRowMapper(), iaid);
	}

    /**
     * The Class IaRowMapper.
     */
    protected class IaRowMapper implements RowMapper<IdentityAssoc> 
    {	     
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.simple.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        public IdentityAssoc mapRow(ResultSet rs, int rowNum) throws SQLException {
        	IdentityAssoc ia = new IdentityAssoc();
        	ia.setId(rs.getLong("id"));
        	ia.setDuid(rs.getBytes("duid"));
        	ia.setIatype(rs.getByte("iatype"));
        	ia.setIaid(rs.getLong("iaid"));
        	ia.setState(rs.getByte("state"));
            return ia;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#deleteAll()
     */
	public void deleteAll() {
		int cnt  = getJdbcTemplate().update("delete from identityassoc");
		log.info("Deleted all " + cnt + " identityassocs");
	}
}
