/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDbTestCase.java is part of DHCPv6.
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

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcpv6.server.DhcpV6Server;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseDbTestCase.
 */
public class BaseTestCase extends TestCase
{	
	public static final String DEFAULT_SCHEMA_TYPE = DbSchemaManager.SCHEMATYPE_JDBC_DERBY;
	public static final int DEFAULT_SCHEMA_VERSION = 2;
	
	/** The config filename. */
	public static String configFilename = "test/dhcpv6server-basetest.xml";

	/** The config. */
	protected static DhcpServerConfiguration config;
	
	/** The ctx. */
	protected static ApplicationContext ctx;
	
	public BaseTestCase() {
		this(DEFAULT_SCHEMA_TYPE, DEFAULT_SCHEMA_VERSION);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// wipe the database before each test
		config.getIaMgr().deleteAllIAs();
	}
	
	public BaseTestCase(String schemaType, int schemaVersion) {
				
		DhcpServerConfiguration.configFilename = configFilename;
		if (config == null) {
			try {
				config = DhcpServerConfiguration.getInstance();

				DhcpServerPolicies.setProperty(Property.DATABASE_SCHEMA_TYTPE, schemaType);
				DhcpServerPolicies.setProperty(Property.DATABASE_SCHEMA_VERSION, Integer.toString(schemaVersion));		
				if (schemaType.contains("derby")) {
					// start with a fresh database
					System.out.println("Cleaning db/derby...");
					FileUtils.cleanDirectory(new File("db/derby"));
				}
				else if (schemaType.contains("h2")) {
					// start with a fresh database
					System.out.println("Cleaning db/h2...");
					FileUtils.cleanDirectory(new File("db/h2"));
				}
				else if (schemaType.contains("sqlite")) {
					// start with a fresh database
					System.out.println("Cleaning db/sqlite...");
					FileUtils.cleanDirectory(new File("db/sqlite"));
				}
				String[] appContext = DhcpV6Server.getAppContextFiles(schemaType, schemaVersion);

				ctx = new ClassPathXmlApplicationContext(appContext);
				
				IaManager iaMgr = (IaManager)ctx.getBean("iaManager");
				iaMgr.init();
				config.setIaMgr(iaMgr);

				NaAddrBindingManager naAddrBindingMgr = 
						(NaAddrBindingManager) ctx.getBean("naAddrBindingManager");
				naAddrBindingMgr.init();
				config.setNaAddrBindingMgr(naAddrBindingMgr);

				TaAddrBindingManager taAddrBindingMgr = 
						(TaAddrBindingManager) ctx.getBean("taAddrBindingManager");
				taAddrBindingMgr.init();
				config.setTaAddrBindingMgr(taAddrBindingMgr);
				
				PrefixBindingManager prefixBindingMgr = 
						(PrefixBindingManager) ctx.getBean("prefixBindingManager");
				prefixBindingMgr.init();
				config.setPrefixBindingMgr(prefixBindingMgr);
				
				V4AddrBindingManager v4AddrBindingMgr = 
						(V4AddrBindingManager) ctx.getBean("v4AddrBindingManager");
				v4AddrBindingMgr.init();
				config.setV4AddrBindingMgr(v4AddrBindingMgr);
			} 
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
