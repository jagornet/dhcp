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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * The Class DbSchemaManager.
 */
public class DbSchemaManager
{    
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DbSchemaManager.class);

    public static String SCHEMA_FILENAME = DhcpConstants.DHCPV6_HOME != null ? 
        	(DhcpConstants.DHCPV6_HOME + "/db/jagornet-dhcpv6-schema.sql") : 
        		"db/jagornet-dhcpv6-schema.sql";

    public static String[] TABLE_NAMES = { "DHCPOPTION", "IAADDRESS", "IAPREFIX", "IDENTITYASSOC" };
	
	/**
	 * Validate schema.
	 * 
	 * @param dataSource the data source
	 * 
	 * @throws Exception the exception
	 */
	public static void validateSchema(DataSource dataSource) throws Exception
	{
        List<String> tableNames = new ArrayList<String>();

        Connection conn = dataSource.getConnection();
		DatabaseMetaData dbMetaData = conn.getMetaData();
		
		log.info("JDBC Connection Info: url=" + dbMetaData.getURL() +
				"\n" +
				"database=" + dbMetaData.getDatabaseProductName() +
				" v" + dbMetaData.getDatabaseProductVersion() +
				"\n" +
				"driver=" + dbMetaData.getDriverName() +
				" v" + dbMetaData.getDriverVersion());
		
        String[] types = { "TABLE" };
        ResultSet rs = dbMetaData.getTables(null, null, "%", types);
        if (rs.next()) {
            tableNames.add(rs.getString("TABLE_NAME"));
        }
        else {
        	createSchema(dataSource);
            dbMetaData = conn.getMetaData();
            rs = dbMetaData.getTables(null, null, "%", types);
        }
        while (rs.next()){
          tableNames.add(rs.getString("TABLE_NAME"));
        }
        
		if (tableNames.size() == TABLE_NAMES.length) {
			for (int i=0; i<TABLE_NAMES.length; i++) {
				if (!tableNames.contains(TABLE_NAMES[i])) {
					throw new IllegalStateException("Invalid database schema: unknown tables");
				}
			}
		}
		else {
			throw new IllegalStateException("Invalid database schema: wrong number of tables");
		}
	}
	
	/**
	 * Creates the schema.
	 * 
	 * @param dataSource the data source
	 * 
	 * @throws Exception the exception
	 */
	public static void createSchema(DataSource dataSource) throws Exception
	{
    	JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    	StringBuilder schema = new StringBuilder();
    	BufferedReader br = new BufferedReader(new FileReader(SCHEMA_FILENAME));
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
}
