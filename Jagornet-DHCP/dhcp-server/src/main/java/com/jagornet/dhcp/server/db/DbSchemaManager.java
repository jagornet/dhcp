/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DbSchemaManager.java is part of Jagornet DHCP.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;

/**
 * The Class DbSchemaManager.
 * 
 * @author A. Gregory Rabil
 */
public class DbSchemaManager
{    
    private static Logger log = LoggerFactory.getLogger(DbSchemaManager.class);
    
    // JDBC schema types support both v1 and v2 schemas
    public static final String SCHEMATYPE_JDBC_DERBY = "jdbc-derby";
    public static final String SCHEMATYPE_JDBC_H2 = "jdbc-h2";
    public static final String SCHEMATYPE_JDBC_SQLITE = "jdbc-sqlite";

    public static final String APP_CONTEXT_JDBC_DATASOURCE_FILENAME = "context_jdbc_datasource.xml";
    public static final String APP_CONTEXT_JDBC_V1SCHEMA_FILENAME = "context_jdbc_v1schema.xml";
    public static final String APP_CONTEXT_JDBC_V2SCHEMA_FILENAME = "context_jdbc_v2schema.xml";  
    
    // other schema types support only v2 schema
    public static final String SCHEMATYPE_SQLITE = "sqlite";
    public static final String APP_CONTEXT_SQLITE_V2SCHEMA_FILENAME = "context_sqlite_v2schema.xml";    
    public static final String SCHEMATYPE_MONGO = "mongo";
    public static final String APP_CONTEXT_MONGO_V2SCHEMA_FILENAME = "context_mongo_v2schema.xml";    
    public static final String SCHEMATYPE_FILE = "file";
    public static final String APP_CONTEXT_FILE_V2SCHEMA_FILENAME = "context_file_v2schema.xml";    
    

    public static final String DB_HOME = DhcpConstants.JAGORNET_DHCP_HOME != null ? 
        							(DhcpConstants.JAGORNET_DHCP_HOME + "/db/") : "db/";
        							
//	public static final String SCHEMA_FILENAME = DB_HOME + "jagornet-dhcp-server-schema.sql";
//	public static final String SCHEMA_DERBY_FILENAME = DB_HOME + "jagornet-dhcp-server-schema-derby.sql";
    public static final String SCHEMA_FILENAME = "classpath:jagornet-dhcp-server-schema.sql";
	public static final String SCHEMA_DERBY_FILENAME = "classpath:jagornet-dhcp-server-schema-derby.sql";
	
	public static final String[] TABLE_NAMES = { "DHCPOPTION", "IAADDRESS", "IAPREFIX", "IDENTITYASSOC" };

//	public static final String SCHEMA_V2_FILENAME = DB_HOME + "jagornet-dhcp-server-schema-v2.sql";
//	public static final String SCHEMA_DERBY_V2_FILENAME = DB_HOME + "jagornet-dhcp-server-schema-derby-v2.sql";
	public static final String SCHEMA_V2_FILENAME = "classpath:jagornet-dhcp-server-schema-v2.sql";
	public static final String SCHEMA_DERBY_V2_FILENAME = "classpath:jagornet-dhcp-server-schema-derby-v2.sql";

    public static final String[] TABLE_NAMES_V2 = { "DHCPLEASE" };

	private DbSchemaManager() {
		// prevent instantiation
	}
    
    public static List<String> getDbContextFiles(String schemaType, int schemaVersion) throws Exception {
    	
    	List<String> dbContexts = new ArrayList<>();

    	if (schemaType.startsWith("jdbc")) {
    		// convention over configuration
        	String schemaContext = "context_" + schemaType + ".xml";
        	if (ClassLoader.getSystemResource(schemaContext) == null) {
        		throw new DhcpServerConfigException("Schema context not found on classpath: " + schemaContext);
        	}
            String versionContext = null;
        	if (schemaVersion == 1) {
        		versionContext = APP_CONTEXT_JDBC_V1SCHEMA_FILENAME;
        	}
        	else if (schemaVersion == 2) {
        		versionContext = APP_CONTEXT_JDBC_V2SCHEMA_FILENAME;
        	}
        	else {
        		throw new DhcpServerConfigException("Unsupported schema version: " + schemaVersion);
        	}        	
//        	FileUtils.forceMkdir(new File(DB_HOME + schemaType));
        	dbContexts.add(schemaContext);
        	dbContexts.add(APP_CONTEXT_JDBC_DATASOURCE_FILENAME);
        	dbContexts.add(versionContext);
    	}
        else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_SQLITE)) {
    		FileUtils.forceMkdir(new File(DB_HOME + "sqlite"));
        	dbContexts.add(APP_CONTEXT_SQLITE_V2SCHEMA_FILENAME);
        }
        else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_MONGO)) {
        	dbContexts.add(APP_CONTEXT_MONGO_V2SCHEMA_FILENAME);
        }
        else if (schemaType.equalsIgnoreCase(DbSchemaManager.SCHEMATYPE_FILE)) {
        	dbContexts.add(APP_CONTEXT_FILE_V2SCHEMA_FILENAME);
        }
        else {
        	throw new DhcpServerConfigException("Unsupported schema type: " + schemaType);
        }
    	return dbContexts;
    }
	
	/**
	 * Validate schema.
	 * 
	 * @param dataSource the data source
	 * 
	 * @throws SQLException if there is a problem with the database
	 * @throws IOExcpetion if there is a problem reading the schema file
	 * 
	 * returns true if database was created, false otherwise
	 */
	public static boolean validateSchema(DataSource dataSource, String schemaFilename, int schemaVersion) 
						throws SQLException, IOException
	{
		boolean schemaCreated = false;
		
        List<String> tableNames = new ArrayList<>();

        Connection conn = null;
		try {
			conn = dataSource.getConnection();
			DatabaseMetaData dbMetaData = conn.getMetaData();
			
			String dbProduct = dbMetaData.getDatabaseProductName();
			log.info("JDBC Connection Info:\n" +
					"url = " + dbMetaData.getURL() +
					"\n" +
					"database = " + dbProduct +
					" " + dbMetaData.getDatabaseProductVersion() +
					"\n" +
					"driver = " + dbMetaData.getDriverName() +
					" " + dbMetaData.getDriverVersion());
			
			ResultSet crs = dbMetaData.getCatalogs();
			while (crs.next()) {
				String cat = crs.getString("TABLE_CAT");
				log.info("CATALOG: " + cat);
			}
			ResultSet srs = dbMetaData.getSchemas();
			while (srs.next()) {
				log.info("SCHEMA (CATALOG): " + 
						srs.getString("TABLE_SCHEM") +
						" (" + srs.getString("TABLE_CATALOG") + ")");
			}
			
			String catalog = null;		// "JAGORNET-DHCP"
			String schema = null;
			if (dbProduct.equals("H2")) {
				log.debug("Using PUBLIC schema for H2 2.0.206+");
				schema = "PUBLIC";		// for H2 2.0.206: _not_ INFORMATION_SCHEMA
			}
			String[] types = { "TABLE" };
			ResultSet rs = dbMetaData.getTables(catalog, schema, "%", types);
			if (rs.next()) {
				tableNames.add(rs.getString("TABLE_NAME"));
			}
			else {
				createSchema(dataSource, schemaFilename);
				dbMetaData = conn.getMetaData();
				rs = dbMetaData.getTables(catalog, schema, "%", types);
				schemaCreated = true;
			}
			while (rs.next()){
			tableNames.add(rs.getString("TABLE_NAME"));
			}
			
			log.info("TABLE_NAMES: " + String.join(", ", tableNames));
			String[] schemaTableNames;
			if (schemaVersion <= 1) {
				schemaTableNames = TABLE_NAMES;
			}
			else {
				schemaTableNames = TABLE_NAMES_V2;
			}
			
			if (tableNames.size() == schemaTableNames.length) {
				for (int i=0; i<schemaTableNames.length; i++) {
					if (!tableNames.contains(schemaTableNames[i])) {
						throw new IllegalStateException("Invalid database schema: unknown tables");
					}
				}
			}
			else {
				throw new IllegalStateException("Invalid database schema: wrong number of tables");
			}
			
			return schemaCreated;
		}
		finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	public static List<String> getSchemaDDL(String schemaFilename) throws IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource(schemaFilename);
		InputStream is = resource.getInputStream();
		List<String> schema = new ArrayList<>();
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
	    	StringBuilder ddl = new StringBuilder();
	    	isr = new InputStreamReader(is);
	    	br = new BufferedReader(isr);
	    	String line = br.readLine();
	    	while (line != null) {
	    		if (!line.startsWith("-- ")) {
	    			ddl.append(line);
	    		}
	    		if (ddl.toString().endsWith(";")) {
	    			ddl.setLength(ddl.length()-1);
	    			schema.add(ddl.toString());
		        	ddl.setLength(0);
	    		}
	    		line = br.readLine();
	    	}
		}
		finally {
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
		}
		return schema;
	}
	
	/**
	 * Creates the schema.
	 * 
	 * @param dataSource the data source
	 * 
	 * @throws SQLException if there is a problem with the database
	 * @throws IOExcpetion if there is a problem reading the schema file
	 */
	public static void createSchema(DataSource dataSource, String schemaFilename) 
						throws SQLException, IOException
	{
		log.info("Creating new JDBC schema from file: " + schemaFilename);
	    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
	    List<String> schemaDDL = getSchemaDDL(schemaFilename);
	    for (String ddl : schemaDDL) {
			jdbc.execute(ddl);
		}
	}
}
