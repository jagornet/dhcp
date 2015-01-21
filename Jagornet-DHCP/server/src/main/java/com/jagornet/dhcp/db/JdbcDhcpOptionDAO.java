/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcDhcpOptionDAO.java is part of Jagornet DHCP.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * The JdbcDhcpOptionDAO implementation class for DhcpOptionDAO interface.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcDhcpOptionDAO extends JdbcDaoSupport implements DhcpOptionDAO 
{
    private static Logger log = LoggerFactory.getLogger(JdbcDhcpOptionDAO.class);
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#create(com.jagornet.dhcpv6.db.DhcpOption)
	 */
	public void create(DhcpOption option) 
	{
		SimpleJdbcInsert insertOption = new SimpleJdbcInsert(getDataSource())
                    							.withTableName("dhcpoption")
                    							.usingGeneratedKeyColumns("id");
		
		Long iaId = option.getIdentityAssocId();
		Long iaAddrId = option.getIaAddressId();
		Long iaPrefixId = option.getIaPrefixId();
		
		if ((iaId == null) && (iaAddrId == null) && (iaPrefixId == null)) {
			throw new IllegalStateException(
					"DhcpOption must reference either an IdentityAssoc, an IaAddress");
		}
		if ((iaId != null) && (iaAddrId != null)) {
			throw new IllegalStateException(
					"DhcpOption cannot reference both an IdentityAssoc and an IaAddress");
		}
		if ((iaId != null) && (iaPrefixId != null)) {
			throw new IllegalStateException(
					"DhcpOption cannot reference both an IdentityAssoc and an IaPrefix");
		}
		if ((iaAddrId != null) && (iaPrefixId != null)) {
			throw new IllegalStateException(
					"DhcpOption cannot reference both an IaAddress and an IaPrefix");
		}
		
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("code", option.getCode());
        parameters.put("value", option.getValue());
        // TODO: verify that the option has only one foreign key
        if (iaId != null) {
        	parameters.put("identityassoc_id", iaId);
        	insertOption.usingColumns("code", "value", "identityassoc_id");
        }
        else if (iaAddrId != null) {
        	parameters.put("iaaddress_id", iaAddrId);
        	insertOption.usingColumns("code", "value", "iaaddress_id");
        }
        else if (iaPrefixId != null) {
        	parameters.put("iaprefix_id", iaAddrId);
        	insertOption.usingColumns("code", "value", "iaprefix_id");
        }
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
        Number newId = insertOption.executeAndReturnKey(parameters);
        if (newId != null) {
        	option.setId(newId.longValue());
        }
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#update(com.jagornet.dhcpv6.db.DhcpOption)
	 */
	public void update(DhcpOption option)
	{
		getJdbcTemplate().update(
				"update dhcpoption set value=? where id=?",
				new Object[] { option.getValue(), option.getId() });
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getJdbcTemplate().update(
        		"delete from dhcpoption where id = ?", id);
    }
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#getById()
	 */
	public DhcpOption getById(Long id) 
	{
		try {
		    return getJdbcTemplate().queryForObject(
		            "select * from dhcpoption where id = ?", 
		            new OptionRowMapper(), id);
		}
		catch (EmptyResultDataAccessException ex) {
			log.warn("DhcpOption not found for ID=" + id + ": " + ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#findAllByIdentityAssocId(long)
	 */
	public List<DhcpOption> findAllByIdentityAssocId(long identityAssocId)
	{
        return getJdbcTemplate().query(
                "select * from dhcpoption where identityassoc_id = ? order by code", 
                new OptionRowMapper(), identityAssocId);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#findAllByIaAddressId(long)
	 */
	public List<DhcpOption> findAllByIaAddressId(long iaAddressId)
	{
        return getJdbcTemplate().query(
                "select * from dhcpoption where iaaddress_id = ? order by code", 
                new OptionRowMapper(), iaAddressId);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.DhcpOptionDAO#findAllByIaPrefixId(long)
	 */
	public List<DhcpOption> findAllByIaPrefixId(long iaPrefixId)
	{
        return getJdbcTemplate().query(
                "select * from dhcpoption where iaprefix_id = ? order by code", 
                new OptionRowMapper(), iaPrefixId);
	}

    /**
     * The Class OptionRowMapper.
     */
    protected class OptionRowMapper implements RowMapper<DhcpOption> 
    {	
        
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.simple.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        public DhcpOption mapRow(ResultSet rs, int rowNum) throws SQLException {
        	DhcpOption option = new DhcpOption();
        	option.setId(rs.getLong("id"));
        	option.setCode(rs.getInt("code"));
        	option.setValue(rs.getBytes("value"));
        	option.setIdentityAssocId(rs.getLong("identityassoc_id"));
        	option.setIaAddressId(rs.getLong("iaaddress_id"));
        	option.setIaPrefixId(rs.getLong("iaprefix_id"));
            return option;
        }
    }
}
