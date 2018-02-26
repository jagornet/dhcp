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
package com.jagornet.dhcp.server.db;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcp.server.JagornetDhcpServer;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.BaseBindingManager;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;

/**
 * The Class BaseDbTestCase.
 */
public abstract class BaseTestCase
{	
    public static final String DEFAULT_SCHEMA_TYPE = DbSchemaManager.SCHEMATYPE_JDBC_DERBY;
	public static final int DEFAULT_SCHEMA_VERSION = 2;
	protected static String configFilename = "dhcpserver-basetest.xml";
	protected static DhcpServerConfiguration config;	
	protected static ApplicationContext ctx;
		
	@Before
	public void setUp() throws Exception {
		System.out.println("setUp");
//		System.getProperties().list(System.out);
		System.out.println("testSystemVariable=" + System.getProperty("testSystemVariable"));
		System.out.println("jagornet.dhcp.home=" + System.getProperty("jagornet.dhcp.home"));
//		super.setUp();
//		config.getIaMgr().deleteAllIAs();
	}
	
	@After
	public void tearDown() throws Exception {
//		super.tearDown();
//		config.getIaMgr().deleteAllIAs();
	}
	
	protected static void initializeContext() {
		initializeContext(DEFAULT_SCHEMA_TYPE, DEFAULT_SCHEMA_VERSION);
	}
	
	protected static void initializeContext(String schemaType, int schemaVersion) {
		try {
//			Thread.dumpStack();;			
			
			System.out.println("Initializing context: schemaType=" + schemaType +
								" schemaVersion=" + schemaVersion);
//			System.getProperties().list(System.out);
			System.out.println("testSystemVariable=" + System.getProperty("testSystemVariable"));
			System.out.println("jagornet.dhcp.home=" + System.getProperty("jagornet.dhcp.home"));
			
			config = DhcpServerConfiguration.getInstance();
			config.init(configFilename);

			DhcpServerPolicies.setProperty(Property.DATABASE_SCHEMA_TYTPE, schemaType);
			DhcpServerPolicies.setProperty(Property.DATABASE_SCHEMA_VERSION, Integer.toString(schemaVersion));		
			if (schemaType.contains("derby")) {
				File dbDerby = new File("db/derby");
				if (dbDerby.exists() && dbDerby.isDirectory()) {
					// start with a fresh database
					System.out.println("Cleaning " + dbDerby + "...");
					FileUtils.cleanDirectory(dbDerby);
				}
			}
			else if (schemaType.contains("h2")) {
				File dbH2 = new File("db/h2");
				if (dbH2.exists() && dbH2.isDirectory()) {
					// start with a fresh database
					System.out.println("Cleaning " + dbH2 + "...");
					FileUtils.cleanDirectory(dbH2);
				}
			}
			else if (schemaType.contains("sqlite")) {
				File dbSqlite = new File("db/sqlite");
				if (dbSqlite.exists() && dbSqlite.isDirectory()) {
					// start with a fresh database
					System.out.println("Cleaning " + dbSqlite + "...");
					FileUtils.cleanDirectory(dbSqlite);
				}
			}
			String[] appContext = JagornetDhcpServer.getAppContextFiles(schemaType, schemaVersion);

			ctx = new ClassPathXmlApplicationContext(appContext);
			
			IaManager iaMgr = (IaManager)ctx.getBean("iaManager");
			iaMgr.init();
			config.setIaMgr(iaMgr);

			V6NaAddrBindingManager v6NaAddrBindingMgr = 
					(V6NaAddrBindingManager) ctx.getBean("v6NaAddrBindingManager");
			v6NaAddrBindingMgr.init();
			config.setV6NaAddrBindingMgr(v6NaAddrBindingMgr);

			V6TaAddrBindingManager v6TaAddrBindingMgr = 
					(V6TaAddrBindingManager) ctx.getBean("v6TaAddrBindingManager");
			v6TaAddrBindingMgr.init();
			config.setV6TaAddrBindingMgr(v6TaAddrBindingMgr);
			
			V6PrefixBindingManager v6PrefixBindingMgr = 
					(V6PrefixBindingManager) ctx.getBean("v6PrefixBindingManager");
			v6PrefixBindingMgr.init();
			config.setV6PrefixBindingMgr(v6PrefixBindingMgr);
			
			V4AddrBindingManager v4AddrBindingMgr = 
					(V4AddrBindingManager) ctx.getBean("v4AddrBindingManager");
			v4AddrBindingMgr.init();
			config.setV4AddrBindingMgr(v4AddrBindingMgr);
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}
	
	protected static void closeContext() {
		BaseBindingManager bindingMgr = (BaseBindingManager) config.getV6NaAddrBindingMgr();
		if (bindingMgr != null) {
			bindingMgr.close();
		}
		bindingMgr = (BaseBindingManager) config.getV6TaAddrBindingMgr();
		if (bindingMgr != null) {
			bindingMgr.close();
		}
		bindingMgr = (BaseBindingManager) config.getV6PrefixBindingMgr();
		if (bindingMgr != null) {
			bindingMgr.close();
		}
		bindingMgr = (BaseBindingManager) config.getV4AddrBindingMgr();
		if (bindingMgr != null) {
			bindingMgr.close();
		}
		String schemaType = DhcpServerPolicies.globalPolicy(Property.DATABASE_SCHEMA_TYTPE);
		if (schemaType.contains("derby")) {	
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException e) {
				if (e.getSQLState().equals("XJ015")) {
					// ignore normal derby shutdown exception
				}
				else {
					e.printStackTrace();
				}
			}
		}
		if (ctx != null) {
			((ClassPathXmlApplicationContext) ctx).close();
		}
	}
}
