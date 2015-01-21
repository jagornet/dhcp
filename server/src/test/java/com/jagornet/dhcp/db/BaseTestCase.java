/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseTestCase.java is part of Jagornet DHCP.
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

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcp.db.DbSchemaManager;
import com.jagornet.dhcp.db.IaManager;
import com.jagornet.dhcp.server.JagornetDhcpServer;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseDbTestCase.
 */
public class BaseTestCase extends TestCase
{	
	public static final String DEFAULT_SCHEMA_TYPE = DbSchemaManager.SCHEMATYPE_JDBC_DERBY;
	public static final int DEFAULT_SCHEMA_VERSION = 2;
	
	/** The config filename. */
	public static String configFilename = "test/dhcpserver-basetest.xml";

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
				String[] appContext = JagornetDhcpServer.getAppContextFiles(schemaType, schemaVersion);

				ctx = new ClassPathXmlApplicationContext(appContext);
				
				IaManager iaMgr = (IaManager)ctx.getBean("iaManager");
				iaMgr.init();
				config.setIaMgr(iaMgr);

				V6NaAddrBindingManager v6NaAddrBindingMgr = 
						(V6NaAddrBindingManager) ctx.getBean("v6NaAddrBindingManager");
				v6NaAddrBindingMgr.init();
				config.setNaAddrBindingMgr(v6NaAddrBindingMgr);

				V6TaAddrBindingManager v6TaAddrBindingMgr = 
						(V6TaAddrBindingManager) ctx.getBean("v6TaAddrBindingManager");
				v6TaAddrBindingMgr.init();
				config.setTaAddrBindingMgr(v6TaAddrBindingMgr);
				
				V6PrefixBindingManager v6PrefixBindingMgr = 
						(V6PrefixBindingManager) ctx.getBean("v6PrefixBindingManager");
				v6PrefixBindingMgr.init();
				config.setPrefixBindingMgr(v6PrefixBindingMgr);
				
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
