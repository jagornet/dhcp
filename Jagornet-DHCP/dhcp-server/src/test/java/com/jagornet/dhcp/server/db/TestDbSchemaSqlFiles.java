package com.jagornet.dhcp.server.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TestDbSchemaSqlFiles {

	@Test
	public void testH2SchemaDDLParsing() throws Exception {
		assertEquals(DbSchemaManager.SCHEMA_H2_V2_FILENAME, DbSchemaManager.SCHEMA_V2_FILENAME);
		List<String> ddl = DbSchemaManager.getSchemaDDL(DbSchemaManager.SCHEMA_H2_V2_FILENAME);
		assertNotNull(ddl);
		assertTrue("H2 schema should contain multiple DDL statements", ddl.size() >= 7);

		String createTable = ddl.get(0);
		assertTrue("H2 create table should create DHCPLEASE", 
				createTable.toUpperCase().contains("CREATE TABLE DHCPLEASE"));
		assertTrue("H2 should use VARBINARY for IPADDRESS", 
				createTable.toUpperCase().contains("IPADDRESS VARBINARY(16)"));
		assertTrue("H2 should use BLOB for OPTIONS", 
				createTable.toUpperCase().contains("OPTIONS BLOB"));

		boolean hasTupleIndex = false;
		for (String stmt : ddl) {
			if (stmt.toUpperCase().contains("TUPLE_NDX")) {
				hasTupleIndex = true;
				break;
			}
		}
		assertTrue("H2 DDL should contain TUPLE_NDX index", hasTupleIndex);
	}

	@Test
	public void testPostgresSchemaDDLParsing() throws Exception {
		List<String> ddl = DbSchemaManager.getSchemaDDL(DbSchemaManager.SCHEMA_POSTGRES_V2_FILENAME);
		assertNotNull(ddl);
		assertTrue("Postgres schema should contain multiple DDL statements", ddl.size() >= 7);
		
		String createTable = ddl.get(0);
		assertTrue("Postgres create table should create DHCPLEASE", 
				createTable.toUpperCase().contains("CREATE TABLE DHCPLEASE"));
		assertTrue("Postgres should use BYTEA for IPADDRESS", 
				createTable.toUpperCase().contains("IPADDRESS BYTEA"));
		assertTrue("Postgres should use BYTEA for DUID", 
				createTable.toUpperCase().contains("DUID BYTEA"));
		assertTrue("Postgres should use BYTEA for OPTIONS", 
				createTable.toUpperCase().contains("OPTIONS BYTEA"));

		boolean hasTupleIndex = false;
		for (String stmt : ddl) {
			if (stmt.toUpperCase().contains("TUPLE_NDX")) {
				hasTupleIndex = true;
				break;
			}
		}
		assertTrue("Postgres DDL should contain TUPLE_NDX index", hasTupleIndex);
	}

	@Test
	public void testMysqlSchemaDDLParsing() throws Exception {
		List<String> ddl = DbSchemaManager.getSchemaDDL(DbSchemaManager.SCHEMA_MYSQL_V2_FILENAME);
		assertNotNull(ddl);
		assertTrue("MySQL schema should contain multiple DDL statements", ddl.size() >= 7);

		String createTable = ddl.get(0);
		assertTrue("MySQL create table should create DHCPLEASE", 
				createTable.toUpperCase().contains("CREATE TABLE DHCPLEASE"));
		assertTrue("MySQL should use VARBINARY for IPADDRESS", 
				createTable.toUpperCase().contains("IPADDRESS VARBINARY(16)"));
		assertTrue("MySQL should use VARBINARY for DUID", 
				createTable.toUpperCase().contains("DUID VARBINARY(130)"));
		assertTrue("MySQL should specify InnoDB engine", 
				createTable.toUpperCase().contains("ENGINE=INNODB"));

		boolean hasTupleIndex = false;
		for (String stmt : ddl) {
			if (stmt.toUpperCase().contains("TUPLE_NDX")) {
				hasTupleIndex = true;
				break;
			}
		}
		assertTrue("MySQL DDL should contain TUPLE_NDX index", hasTupleIndex);
	}

	@Test
	public void testSqliteSchemaDDLParsing() throws Exception {
		List<String> ddl = DbSchemaManager.getSchemaDDL(DbSchemaManager.SCHEMA_SQLITE_V2_FILENAME);
		assertNotNull(ddl);
		assertTrue("SQLite schema should contain multiple DDL statements", ddl.size() >= 7);

		String createTable = ddl.get(0);
		assertTrue("SQLite create table should create DHCPLEASE", 
				createTable.toUpperCase().contains("CREATE TABLE DHCPLEASE"));
		assertTrue("SQLite should use VARBINARY for IPADDRESS", 
				createTable.toUpperCase().contains("IPADDRESS VARBINARY(16)"));
		assertTrue("SQLite should use BLOB for OPTIONS", 
				createTable.toUpperCase().contains("OPTIONS BLOB"));

		boolean hasTupleIndex = false;
		for (String stmt : ddl) {
			if (stmt.toUpperCase().contains("TUPLE_NDX")) {
				hasTupleIndex = true;
				break;
			}
		}
		assertTrue("SQLite DDL should contain TUPLE_NDX index", hasTupleIndex);
	}

	@Test
	public void testGetSchemaV2Filename() {
		assertEquals(DbSchemaManager.SCHEMA_POSTGRES_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("jdbc-postgres"));
		assertEquals(DbSchemaManager.SCHEMA_POSTGRES_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("PostgreSQL"));
		assertEquals(DbSchemaManager.SCHEMA_MYSQL_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("jdbc-mysql"));
		assertEquals(DbSchemaManager.SCHEMA_MARIADB_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("jdbc-mariadb"));
		assertEquals(DbSchemaManager.SCHEMA_DERBY_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("jdbc-derby"));
		assertEquals(DbSchemaManager.SCHEMA_H2_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("jdbc-h2"));
		assertEquals(DbSchemaManager.SCHEMA_SQLITE_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("jdbc-sqlite"));
		assertEquals(DbSchemaManager.SCHEMA_SQLITE_V2_FILENAME, 
				DbSchemaManager.getSchemaV2Filename("sqlite"));
	}

	@Test
	public void testGetDbContextFiles() throws Exception {
		List<String> pgContexts = DbSchemaManager.getDbContextFiles("jdbc-postgres", 2);
		assertTrue(pgContexts.contains("context_jdbc-postgres.xml"));
		assertTrue(pgContexts.contains(DbSchemaManager.APP_CONTEXT_JDBC_DATASOURCE_FILENAME));
		assertTrue(pgContexts.contains(DbSchemaManager.APP_CONTEXT_JDBC_V2SCHEMA_FILENAME));

		List<String> mysqlContexts = DbSchemaManager.getDbContextFiles("jdbc-mysql", 2);
		assertTrue(mysqlContexts.contains("context_jdbc-mysql.xml"));

		List<String> mariadbContexts = DbSchemaManager.getDbContextFiles("jdbc-mariadb", 2);
		assertTrue(mariadbContexts.contains("context_jdbc-mariadb.xml"));

		List<String> sqliteContexts = DbSchemaManager.getDbContextFiles("jdbc-sqlite", 2);
		assertTrue(sqliteContexts.contains("context_jdbc-sqlite.xml"));
	}
}
