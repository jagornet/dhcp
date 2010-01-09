/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIaManager.java is part of DHCPv6.
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import com.jagornet.dhcpv6.util.Util;

// TODO: Auto-generated Javadoc
// although this is a Manager and not a DAO, we'll extend SimpleJdbcDaoSupport
// to provide easy use of SimpleJdbcTemplate with less configuration in context.xml
/**
 * The Class JdbcIaManager.
 */
public class JdbcIaManager extends SimpleJdbcDaoSupport implements IaManager
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(JdbcIaManager.class);

	/** The ia dao. */
	protected IdentityAssocDAO iaDao;
	
	/** The ia addr dao. */
	protected IaAddressDAO iaAddrDao;
	
	/** The dhcp opt dao. */
	protected DhcpOptionDAO dhcpOptDao;

	/**
	 * Create an IdentityAssoc object, including any contained
	 * IpAddresses and DhcpOptions, as well as any DhcpOptions
	 * contained in the IpAddresses themselves.
	 * 
	 * @param ia the ia
	 */
	public void createIA(IdentityAssoc ia)
	{
		if (ia != null) {
			log.debug("Creating: " + ia.toString());
			iaDao.create(ia);	// sets the id
			Collection<? extends IaAddress> ips = ia.getIaAddresses();
			if (ips != null) {
				for (IaAddress ip : ips) {
					ip.setIdentityAssocId(ia.getId());
					iaAddrDao.create(ip);	//sets the id
					Collection<DhcpOption> opts = ip.getDhcpOptions();
					if (opts != null) {
						for (DhcpOption opt : opts) {
							opt.setIpAddressId(ip.getId());
							dhcpOptDao.create(opt);
						}
					}
				}
			}
			Collection<DhcpOption> opts = ia.getDhcpOptions();
			if (opts != null) {
				for (DhcpOption opt : opts) {
					opt.setIdentityAssocId(ia.getId());
					dhcpOptDao.create(opt);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIA(com.jagornet.dhcpv6.db.IdentityAssoc, java.util.Collection, java.util.Collection, java.util.Collection)
	 */
	public void updateIA(IdentityAssoc ia, Collection<IaAddress> addAddrs,
			Collection<IaAddress> updateAddrs, Collection<IaAddress> delAddrs)
	{
		iaDao.update(ia);
		if (addAddrs != null) {
			for (IaAddress addAddr : addAddrs) {
				iaAddrDao.create(addAddr);
			}
		}
		if (updateAddrs != null) {
			for (IaAddress updateAddr : updateAddrs) {
				iaAddrDao.update(updateAddr);
			}
		}
		if (delAddrs != null) {
			for (IaAddress delAddr : delAddrs) {
				iaAddrDao.deleteById(delAddr.getId());
			}
		}
	}

	/**
	 * Delete an IdentityAssoc object, and allow the database
	 * constraints (cascade delete) to care of deleting any
	 * contained IpAddresses and DhcpOptions, and further
	 * cascading to delete any DhcpOptions contained in the
	 * IpAddresses themselves.
	 * 
	 * @param ia the ia
	 */
	public void deleteIA(IdentityAssoc ia)
	{
		if (ia != null) {
			log.debug("Deleting: " + ia.toString());
			// just delete the IdentityAssoc object, and allow the
			// database constraints (cascade delete) take care of
			// deleting any associated IpAddress or DhcpOption objects,
			// further cascading to delete any DhcpOption objects
			// for any IpAddress objects which are deleted
			iaDao.deleteById(ia.getId());
		}
	}

	/**
	 * Locate an IdentityAssoc object by the key tuple duid-iaid-iatype.
	 * Populate any contained IpAddresses and DhcpOptions, as well as
	 * any DhcpOptions contained in the IpAddresses themselves.
	 * 
	 * @param duid the duid
	 * @param iatype the iatype
	 * @param iaid the iaid
	 * 
	 * @return a fully-populated IdentityAssoc, or null if not found
	 */
	public IdentityAssoc findIA(byte[] duid, byte iatype, long iaid)
	{
		IdentityAssoc ia = iaDao.getByKey(duid, iatype, iaid);
		if (ia != null) {
			List<IaAddress> ips = iaAddrDao.findAllByIdentityAssocId(ia.getId());
			if (ips != null) {
				for (IaAddress ip : ips) {
					List<DhcpOption> opts = dhcpOptDao.findAllByIaAddressId(ip.getId());
					if (opts != null) {
						ip.setDhcpOptions(new ArrayList<DhcpOption>(opts));
					}
				}
				ia.setIaAddresses(new ArrayList<IaAddress>(ips));
			}
			List<DhcpOption> opts = dhcpOptDao.findAllByIdentityAssocId(ia.getId());
			if (opts != null) {
				ia.setDhcpOptions(new ArrayList<DhcpOption>(opts));
			}
		}
		return ia;
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findIA(java.net.InetAddress)
	 */
	public IdentityAssoc findIA(InetAddress inetAddr)
	{
		IdentityAssoc ia = null;
		if (inetAddr != null) {
			try {
				IaAddress ipAddr = iaAddrDao.getByInetAddress(inetAddr);
				if (ipAddr != null) {
					ia = iaDao.getById(ipAddr.getIdentityAssocId());
				}
			}
			catch (EmptyResultDataAccessException ex) {
				log.debug("No IaAddress found for IP=" + inetAddr.getHostAddress());
			}
		}
		return ia;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void updateIaAddr(IaAddress iaAddr)
	{
		iaAddrDao.update(iaAddr);
		expireIA(iaAddr.getIdentityAssocId());
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIaAddr(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void deleteIaAddr(IaAddress iaAddr)
	{
		iaAddrDao.deleteById(iaAddr.getId());
		deleteExpiredIA(iaAddr.getIdentityAssocId());
	}

	/**
	 * Expire ia.
	 * 
	 * @param id the id
	 */
	public void expireIA(final Long id)
	{	
		getJdbcTemplate().update(
				"update identityassoc set state=" + IdentityAssoc.EXPIRED +
				" where id=?" +
				" and not exists (select 1 from iaaddress" +
				"				  where identityassoc_id=identityassoc.id" +
				"				  and validendtime>=?)",
				new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps)
							throws SQLException {
						ps.setLong(1, id);
						java.sql.Timestamp now = new java.sql.Timestamp((new Date()).getTime());
						ps.setTimestamp(2, now, Util.GMT_CALENDAR);
					}
				});
		
	}

	/**
	 * Delete expired ia.
	 * 
	 * @param id the id
	 */
	public void deleteExpiredIA(final Long id)
	{	
		getJdbcTemplate().update(
				"delete from identityassoc" +
				" where id=?" +
				" and not exists (select 1 from iaaddress" +
				"				  where identityassoc_id=identityassoc.id" +
				"				  and validendtime is not null and validendtime>=?)",
				new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps)
							throws SQLException {
						ps.setLong(1, id);
						java.sql.Timestamp now = new java.sql.Timestamp((new Date()).getTime());
						ps.setTimestamp(2, now, Util.GMT_CALENDAR);
					}
				});
		
	}
	
	/**
	 * Expire i as.
	 */
	public void expireIAs()
	{
		getJdbcTemplate().update(
				"update identityassoc set state=" + IdentityAssoc.EXPIRED +
				" where exists (select 1 from iaaddress where identityassoc_id=identityassoc.id and validendtime<?)" +
				" and not exists (select 1 from iaaddress where identityassoc_id=identityassoc.id and validendtime>=?)",
				new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps)
							throws SQLException {
						java.sql.Timestamp now = new java.sql.Timestamp((new Date()).getTime());
						ps.setTimestamp(1, now, Util.GMT_CALENDAR);
						ps.setTimestamp(2, now, Util.GMT_CALENDAR);
					}
				});
	}

	/**
	 * Gets the ia dao.
	 * 
	 * @return the ia dao
	 */
	public IdentityAssocDAO getIaDao() {
		return iaDao;
	}

	/**
	 * Sets the ia dao.
	 * 
	 * @param iaDao the new ia dao
	 */
	public void setIaDao(IdentityAssocDAO iaDao) {
		this.iaDao = iaDao;
	}

	/**
	 * Gets the ia addr dao.
	 * 
	 * @return the ia addr dao
	 */
	public IaAddressDAO getIaAddrDao() {
		return iaAddrDao;
	}

	/**
	 * Sets the ia addr dao.
	 * 
	 * @param iaAddrDao the new ia addr dao
	 */
	public void setIaAddrDao(IaAddressDAO iaAddrDao) {
		this.iaAddrDao = iaAddrDao;
	}

	/**
	 * Gets the dhcp opt dao.
	 * 
	 * @return the dhcp opt dao
	 */
	public DhcpOptionDAO getDhcpOptDao() {
		return dhcpOptDao;
	}

	/**
	 * Sets the dhcp opt dao.
	 * 
	 * @param dhcpOptDao the new dhcp opt dao
	 */
	public void setDhcpOptDao(DhcpOptionDAO dhcpOptDao) {
		this.dhcpOptDao = dhcpOptDao;
	}
	
}
