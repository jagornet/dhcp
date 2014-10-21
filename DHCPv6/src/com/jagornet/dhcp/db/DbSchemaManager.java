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
package com.jagornet.dhcp.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jagornet.dhcp.util.DhcpConstants;

/**
 * The Class DbSchemaManager.
 * 
 * @author A. Gregory Rabil
 */
public class DbSchemaManager
{    
    private static Logger log = LoggerFactory.getLogger(DbSchemaManager.class);
    
    public static String SCHEMATYPE_JDBC_DERBY = "jdbc-derby";
    public static String SCHEMATYPE_JDBC_H2 = "jdbc-h2";
    public static String SCHEMATYPE_JDBC_SQLITE = "jdbc-sqlite";
    public static String SCHEMATYPE_SQLITE = "sqlite";
    public static String SCHEMATYPE_MONGO = "mongo";

    public static String DB_HOME = DhcpConstants.JAGORNET_DHCP_HOME != null ? 
        							(DhcpConstants.JAGORNET_DHCP_HOME + "/db/") : "db/";
        							
    public static String SCHEMA_FILENAME = DB_HOME + "jagornet-dhcpv6-schema.sql";
	public static String SCHEMA_DERBY_FILENAME = DB_HOME + "jagornet-dhcpv6-schema-derby.sql";
	
	public static String[] TABLE_NAMES = { "DHCPOPTION", "IAADDRESS", "IAPREFIX", "IDENTITYASSOC" };

	public static String SCHEMA_V2_FILENAME = DB_HOME + "jagornet-dhcpv6-schema-v2.sql";
	public static String SCHEMA_DERBY_V2_FILENAME = DB_HOME + "jagornet-dhcpv6-schema-derby-v2.sql";

    public static String[] TABLE_NAMES_V2 = { "DHCPLEASE" };
	
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
		
        List<String> tableNames = new ArrayList<String>();

        Connection conn = dataSource.getConnection();
		DatabaseMetaData dbMetaData = conn.getMetaData();
		
		log.info("JDBC Connection Info:\n" +
				"url = " + dbMetaData.getURL() +
				"\n" +
				"database = " + dbMetaData.getDatabaseProductName() +
				" " + dbMetaData.getDatabaseProductVersion() +
				"\n" +
				"driver = " + dbMetaData.getDriverName() +
				" " + dbMetaData.getDriverVersion());
		
        String[] types = { "TABLE" };
        ResultSet rs = dbMetaData.getTables(null, null, "%", types);
        if (rs.next()) {
            tableNames.add(rs.getString("TABLE_NAME"));
        }
        else {
        	createSchema(dataSource, schemaFilename);
            dbMetaData = conn.getMetaData();
            rs = dbMetaData.getTables(null, null, "%", types);
            schemaCreated = true;
        }
        while (rs.next()){
          tableNames.add(rs.getString("TABLE_NAME"));
        }
        
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
	
	public static List<String> getSchemaDDL(String schemaFilename) throws IOException {
		List<String> schema = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
	    	StringBuilder ddl = new StringBuilder();
	    	fr = new FileReader(schemaFilename);
	    	br = new BufferedReader(fr);
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
			if (fr != null) {
				fr.close();
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
