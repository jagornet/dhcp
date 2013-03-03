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

import java.io.IOException;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Range;
import com.jagornet.dhcpv6.util.Util;

/**
 * The JdbcIaManager implementation class for the IaManager interface.
 * This is the main database access class for handling client bindings.
 * 
 * @author A. Gregory Rabil
 */
public class JdbcIaManager extends JdbcDaoSupport implements IaManager
{		
	private static Logger log = LoggerFactory.getLogger(JdbcIaManager.class);

	protected IdentityAssocDAO iaDao;
	protected IaAddressDAO iaAddrDao;
	protected IaPrefixDAO iaPrefixDao;
	protected DhcpOptionDAO dhcpOptDao;
	
	// Spring bean init-method
	public void init() throws Exception {
		DbSchemaManager.validateSchema(getDataSource(),
				DhcpServerPolicies.globalPolicy(Property.DATABASE_SCHEMA_FILENAME), 1);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#createIA(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void createIA(IdentityAssoc ia)
	{
		if (ia != null) {
			log.debug("Creating: " + ia.toString());
			iaDao.create(ia);	// sets the id
			Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
			if (iaAddrs != null) {
				for (IaAddress iaAddr : iaAddrs) {
					iaAddr.setIdentityAssocId(ia.getId());
					if (ia.getIatype() == IdentityAssoc.PD_TYPE)
						iaPrefixDao.create((IaPrefix)iaAddr);
					else
						iaAddrDao.create(iaAddr);	//sets the id
					Collection<DhcpOption> opts = iaAddr.getDhcpOptions();
					if (opts != null) {
						for (DhcpOption opt : opts) {
							if (ia.getIatype() == IdentityAssoc.PD_TYPE)
								opt.setIaPrefixId(iaAddr.getId());
							else
								opt.setIaAddressId(iaAddr.getId());
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
	public void updateIA(IdentityAssoc ia, Collection<? extends IaAddress> addAddrs,
			Collection<? extends IaAddress> updateAddrs, Collection<? extends IaAddress> delAddrs)
	{
		iaDao.update(ia);
		if (addAddrs != null) {
			for (IaAddress addAddr : addAddrs) {
				// ensure the address points to the given IA
				addAddr.setIdentityAssocId(ia.getId());
				if (ia.getIatype() == IdentityAssoc.PD_TYPE)
					iaPrefixDao.create((IaPrefix)addAddr);
				else
					iaAddrDao.create(addAddr);
			}
		}
		if (updateAddrs != null) {
			for (IaAddress updateAddr : updateAddrs) {
				// ensure the address points to the given IA
				updateAddr.setIdentityAssocId(ia.getId());
				if (ia.getIatype() == IdentityAssoc.PD_TYPE)
					iaPrefixDao.update((IaPrefix)updateAddr);
				else
					iaAddrDao.update(updateAddr);
			}
		}
		if (delAddrs != null) {
			for (IaAddress delAddr : delAddrs) {
				if (ia.getIatype() == IdentityAssoc.PD_TYPE)
					iaPrefixDao.deleteById(delAddr.getId());
				else
					iaAddrDao.deleteById(delAddr.getId());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIA(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void deleteIA(IdentityAssoc ia)
	{
		if (ia != null) {
			log.debug("Deleting: " + ia.toString());
			// just delete the IdentityAssoc object, and allow the
			// database constraints (cascade delete) take care of
			// deleting any associated IaAddress, IaPrefix or DhcpOption objects,
			// further cascading to delete any DhcpOption objects
			// for any IaAddress or IaPrefix objects which are deleted
			iaDao.deleteById(ia.getId());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#getIA(long)
	 */
	public IdentityAssoc getIA(long id)
	{
		return iaDao.getById(id);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findIA(byte[], byte, long)
	 */
	public IdentityAssoc findIA(byte[] duid, byte iatype, long iaid)
	{
		IdentityAssoc ia = iaDao.getByKey(duid, iatype, iaid);
		if (ia != null) {
			List<? extends IaAddress> iaAddrs = null;
			if (ia.getIatype() == IdentityAssoc.PD_TYPE)
				iaAddrs = iaPrefixDao.findAllByIdentityAssocId(ia.getId());
			else
				iaAddrs = iaAddrDao.findAllByIdentityAssocId(ia.getId());
			if (iaAddrs != null) {
				for (IaAddress iaAddr : iaAddrs) {
					List<DhcpOption> opts = null;
					if (ia.getIatype() == IdentityAssoc.PD_TYPE)
						opts = dhcpOptDao.findAllByIaPrefixId(iaAddr.getId());
					else
						opts = dhcpOptDao.findAllByIaAddressId(iaAddr.getId());
					if (opts != null) {
						iaAddr.setDhcpOptions(new ArrayList<DhcpOption>(opts));
					}
				}
				ia.setIaAddresses(new ArrayList<IaAddress>(iaAddrs));
			}
			List<DhcpOption> opts = dhcpOptDao.findAllByIdentityAssocId(ia.getId());
			if (opts != null) {
				ia.setDhcpOptions(new ArrayList<DhcpOption>(opts));
			}
		}
		return ia;
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findIA(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public IdentityAssoc findIA(IaAddress iaAddr)
	{
		IdentityAssoc ia = null;
		if (iaAddr != null) {
			try {
				ia = iaDao.getById(iaAddr.getIdentityAssocId());
			}
			catch (EmptyResultDataAccessException ex) {
				log.debug("No IdenityAssoc found for ID=" +iaAddr.getIdentityAssocId() +
						": " + ex);
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
				IaAddress iaAddr = iaAddrDao.getByInetAddress(inetAddr);
				if (iaAddr != null) {
					ia = iaDao.getById(iaAddr.getIdentityAssocId());
				}
			}
			catch (EmptyResultDataAccessException ex) {
				log.debug("No IaAddress found for IP=" + inetAddr.getHostAddress() +
						": " + ex);
			}
		}
		return ia;
	}
	
	public List<IdentityAssoc> findExpiredIAs(byte iatype)
	{
		List<IdentityAssoc> expiredIAs = null;
		//TODO: improve on this logic, see hack below
		List<IaAddress> iaAddrs = iaAddrDao.findExpiredAddresses(iatype);
		if (iaAddrs != null) {
			expiredIAs = new ArrayList<IdentityAssoc>();
			for (IaAddress iaAddr : iaAddrs) {
				List<IaAddress> _iaAddrs = new ArrayList<IaAddress>();
				// populate the IaAddress with the associated DhcpOptions
				iaAddr.setDhcpOptions(dhcpOptDao.findAllByIaAddressId(iaAddr.getId()));
				// find the IA for this expired IaAddress
				IdentityAssoc ia = findIA(iaAddr);
				// now add this one expired IaAddress to the
				// fabricated expired IA for the returned list
				_iaAddrs.add(iaAddr);
				// this hack is likely to be a problem someday...
				ia.setIaAddresses(_iaAddrs);
				expiredIAs.add(ia);
			}
		}
		return expiredIAs;
	}

	public void saveDhcpOption(IaAddress iaAddr, 
							   com.jagornet.dhcpv6.option.base.BaseDhcpOption baseOption)
	{
		try {
			byte[] newVal = baseOption.encode().array();
			// don't store the option code, start with length to
			// simplify decoding when retrieving from database
			if (baseOption.isV4()) {
				newVal = Arrays.copyOfRange(newVal, 1, newVal.length);
			}
			else {
				newVal = Arrays.copyOfRange(newVal, 2, newVal.length);
			}
//			DhcpOption dbOption = iaAddr.getDhcpOption(baseOption.getCode());
			DhcpOption dbOption = findIaAddressOption(iaAddr, baseOption);
			if (dbOption == null) {
				dbOption = new com.jagornet.dhcpv6.db.DhcpOption();
				dbOption.setCode(baseOption.getCode());
				dbOption.setValue(newVal);
				addDhcpOption(iaAddr, dbOption);
			}
			else {
				if(!Arrays.equals(dbOption.getValue(), newVal)) {
					dbOption.setValue(newVal);
					updateDhcpOption(dbOption);
				}
			}
		} 
		catch (IOException ex) {
			log.error("Failed to update binding with option", ex);
		}
		
	}

	public void deleteDhcpOption(IaAddress iaAddr, 
							   com.jagornet.dhcpv6.option.base.BaseDhcpOption baseOption)
	{
		DhcpOption dbOption = findIaAddressOption(iaAddr, baseOption);
		if (dbOption != null) {
			dhcpOptDao.deleteById(dbOption.getId());
		}
	}

	protected DhcpOption findIaAddressOption(IaAddress iaAddr,
			com.jagornet.dhcpv6.option.base.BaseDhcpOption baseOption) 
	{
		DhcpOption dbOption = null;
		List<DhcpOption> iaAddrOptions = dhcpOptDao.findAllByIaAddressId(iaAddr.getId());
		if (iaAddrOptions != null) {
			for (DhcpOption iaAddrOption : iaAddrOptions) {
				if (iaAddrOption.getCode() == baseOption.getCode()) {
					dbOption = iaAddrOption;
					break;
				}
			}
		}
		return dbOption;
	}
	
	protected void addDhcpOption(IdentityAssoc ia, DhcpOption option)
	{
		// ensure the DhcpOption references this IA, and nothing else
		option.setIdentityAssocId(ia.getId());
		option.setIaAddressId(null);
		option.setIaPrefixId(null);
		
		dhcpOptDao.create(option);
	}
	
	protected void addDhcpOption(IaAddress iaAddr, DhcpOption option)
	{
		// ensure the DhcpOption references this IA_ADDR, and nothing else
		option.setIdentityAssocId(null);
		option.setIaAddressId(iaAddr.getId());
		option.setIaPrefixId(null);
		
		dhcpOptDao.create(option);
	}
	
	protected void addDhcpOption(IaPrefix iaPrefix, DhcpOption option)
	{
		// ensure the DhcpOption references this IA_PREFIX, and nothing else
		option.setIdentityAssocId(null);
		option.setIaAddressId(null);
		option.setIaPrefixId(iaPrefix.getId());
		
		dhcpOptDao.create(option);
	}
	
	protected void updateDhcpOption(DhcpOption option)
	{
		dhcpOptDao.update(option);
	}
	
	protected void deleteDhcpOption(DhcpOption option)
	{
		dhcpOptDao.deleteById(option.getId());
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
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIaPrefix(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void updateIaPrefix(IaPrefix iaPrefix)
	{
		iaPrefixDao.update(iaPrefix);
		expireIA(iaPrefix.getIdentityAssocId());
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIaPrefix(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void deleteIaPrefix(IaPrefix iaPrefix)
	{
		iaPrefixDao.deleteById(iaPrefix.getId());
		deleteExpiredIA(iaPrefix.getIdentityAssocId());
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExistingIPs(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<InetAddress> findExistingIPs(InetAddress startAddr, InetAddress endAddr) {
		return iaAddrDao.findExistingIPs(startAddr, endAddr);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaAddresses(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaAddress> findUnusedIaAddresses(InetAddress startAddr, InetAddress endAddr) {
		return iaAddrDao.findUnusedByRange(startAddr, endAddr);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExpiredIaAddresses(byte)
	 */
	@Override
	public List<IaAddress> findExpiredIaAddresses(byte iatype) {
		return iaAddrDao.findExpiredAddresses(iatype);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findUnusedIaPrefixes(java.net.InetAddress, java.net.InetAddress)
	 */
	@Override
	public List<IaPrefix> findUnusedIaPrefixes(InetAddress startAddr, InetAddress endAddr) {
		return iaPrefixDao.findUnusedByRange(startAddr, endAddr);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExpiredIaPrefixes()
	 */
	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
		return iaPrefixDao.findAllOlderThan(new Date());
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#reconcileIaAddresses(java.util.List)
	 */
	@Override
	public void reconcileIaAddresses(List<Range> ranges) {
		iaAddrDao.deleteNotInRanges(ranges);
	}

	/**
	 * Expire ia.
	 * 
	 * @param id the id
	 */
	protected void expireIA(final Long id)
	{	
		getJdbcTemplate().update(
				"update identityassoc set state=" + IdentityAssoc.EXPIRED +
				" where id=?" +
				" and not exists" +
				" (select 1 from iaaddress" +
				" where identityassoc_id=identityassoc.id" +
				" and validendtime>=?)",
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
	protected void deleteExpiredIA(final Long id)
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
	protected void expireIAs()
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
	 * Gets the ia prefix dao.
	 * 
	 * @return the ia prefix dao
	 */
	public IaPrefixDAO getIaPrefixDao() {
		return iaPrefixDao;
	}

	/**
	 * Sets the ia prefix dao.
	 * 
	 * @param iaPrefixDao the ia prefix dao
	 */
	public void setIaPrefixDao(IaPrefixDAO iaPrefixDao) {
		this.iaPrefixDao = iaPrefixDao;
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
