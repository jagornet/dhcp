/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DbSchemaManager.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * The Class DbSchemaManager.
 * 
 * @author A. Gregory Rabil
 */
public class DbSchemaManager
{    
    private static Logger log = LoggerFactory.getLogger(DbSchemaManager.class);

    public static String SCHEMA_FILENAME = DhcpConstants.DHCPV6_HOME != null ? 
        	(DhcpConstants.DHCPV6_HOME + "/db/jagornet-dhcpv6-schema.sql") : 
        		"db/jagornet-dhcpv6-schema.sql";
	
	public static String[] TABLE_NAMES = { "DHCPOPTION", "IAADDRESS", "IAPREFIX", "IDENTITYASSOC" };

	
    public static String SCHEMA_FILENAME_V2 = DhcpConstants.DHCPV6_HOME != null ? 
        	(DhcpConstants.DHCPV6_HOME + "/db/jagornet-dhcpv6-schema-v2.sql") : 
        		"db/jagornet-dhcpv6-schema-v2.sql";

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
	public static boolean validateSchema(DataSource dataSource, int schemaVersion) 
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
        	createSchema(dataSource, schemaVersion);
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
	
	/**
	 * Creates the schema.
	 * 
	 * @param dataSource the data source
	 * 
	 * @throws SQLException if there is a problem with the database
	 * @throws IOExcpetion if there is a problem reading the schema file
	 */
	public static void createSchema(DataSource dataSource, int schemaVersion) 
						throws SQLException, IOException
	{
		String schemaFilename;
		if (schemaVersion <= 1) {
			schemaFilename = SCHEMA_FILENAME;
		}
		else {
			schemaFilename = SCHEMA_FILENAME_V2;
		}
		
		FileReader fr = null;
		BufferedReader br = null;
		try {
	    	JdbcTemplate jdbc = new JdbcTemplate(dataSource);
	    	StringBuilder schema = new StringBuilder();
	    	fr = new FileReader(schemaFilename);
	    	br = new BufferedReader(fr);
	    	String line = br.readLine();
	    	while (line != null) {
	    		if (!line.startsWith("-- ")) {
	    			schema.append(line);
	    		}
	    		if (schema.toString().endsWith(";")) {
	    			schema.setLength(schema.length()-1);
		        	jdbc.execute(schema.toString());
		        	schema.setLength(0);
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
	}
}
