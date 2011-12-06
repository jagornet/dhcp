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
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.sql.DataSource;

import org.dbunit.DBTestCase;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.xml.sax.InputSource;

import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseDbTestCase.
 */
public class BaseDbTestCase extends DBTestCase
{
	/** The config filename. */
	public static String configFilename = "test/dhcpv6server-sample.xml";
	
	/** The context filename. */
	public static String contextFilename = "com/jagornet/dhcpv6/context.xml";
	
	/** The init data set. */
	public static File initDataSet = new File("test/dbunit-jagornet-dhcpv6-v2-empty.xml");

	/** The config. */
	protected static DhcpServerConfiguration config;
	
	/** The ctx. */
	protected static ApplicationContext ctx;

	protected int schemaVersion = 1;
	
	private boolean schemaValidated;
	
	static
	{
		DhcpServerConfiguration.configFilename = configFilename;
		config = DhcpServerConfiguration.getInstance();
		ctx = new ClassPathXmlApplicationContext(contextFilename);
		
		config.setNaAddrBindingMgr(
				(NaAddrBindingManager) ctx.getBean("naAddrBindingManager"));
		config.setTaAddrBindingMgr(
				(TaAddrBindingManager) ctx.getBean("taAddrBindingManager"));
		config.setPrefixBindingMgr(
				(PrefixBindingManager) ctx.getBean("prefixBindingManager"));
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		if (!schemaValidated) {
			DbSchemaManager.validateSchema((DataSource) ctx.getBean("dataSource"), schemaVersion);
			schemaValidated = true;
		}
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	@Override
	protected IDataSet getDataSet() throws Exception {
		// return an empty dataset
		return new FlatXmlDataSet(
				new FlatXmlProducer(
						new InputSource(
								new FileInputStream(initDataSet))));
		}
	
	/* (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDatabaseTester()
	 */
	@Override
	protected IDatabaseTester getDatabaseTester() throws Exception {
		return new DataSourceDatabaseTester(getDataSource());
	}

	
	/**
	 * Gets the data source.
	 * 
	 * @return the data source
	 */
	protected static DriverManagerDataSource getDataSource() {
		return (DriverManagerDataSource)ctx.getBean("dataSource");
	}

	
	/**
	 * Xml dump.
	 * 
	 * @throws Exception the exception
	 */
	public static void xmlDump() throws Exception
	{
		File xmlFile = new File("test/dbunit-jagornet-dhcpv6.xml");
		IDatabaseConnection connection = new DatabaseDataSourceConnection(getDataSource());
        // full database export
        FlatXmlDataSet.write(connection.createDataSet(), 
        		new FileOutputStream(xmlFile));
	}
	
	/**
	 * Dtd gen.
	 * 
	 * @throws Exception the exception
	 */
	public static void dtdGen() throws Exception
	{
		File dtdFile = new File("test/dbunit-jagornet-dhcpv6.dtd");
		IDatabaseConnection connection = new DatabaseDataSourceConnection(getDataSource());
        // write DTD file
        FlatDtdDataSet.write(connection.createDataSet(), 
        		new FileOutputStream(dtdFile));
	}
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		try {
			if ((args != null) && (args.length > 0)) {
				if (args[0].equalsIgnoreCase("xmlDump")) {
					xmlDump();
				}
				else if (args[0].equalsIgnoreCase("dtdGen")) {
					dtdGen();
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
