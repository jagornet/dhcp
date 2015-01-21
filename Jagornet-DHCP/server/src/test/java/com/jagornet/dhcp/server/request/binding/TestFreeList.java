/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestFreeList.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.binding;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.request.binding.FreeList;

// TODO: Auto-generated Javadoc
/**
 * The Class TestFreeList.
 */
public class TestFreeList extends TestCase
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(TestFreeList.class);
	
	/** The prefix64 list. */
	private FreeList prefix64List;
	
	/** The smallPool list. */
	private FreeList smallPool;
	
	/**
	 * Instantiates a new test free list.
	 * 
	 * @param name the name
	 */
	public TestFreeList(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("TestCase: " + this.getName());
		memorySizeDump();
		prefix64List = new FreeList(
				new BigInteger(InetAddress.getByName("3ffe::0").getAddress()),
				new BigInteger(InetAddress.getByName("3ffe::ffff:ffff:ffff:ffff").getAddress()));
		smallPool = new FreeList(
				new BigInteger(InetAddress.getByName("3ffe::0").getAddress()),
				new BigInteger(InetAddress.getByName("3ffe::2").getAddress()));
		memorySizeDump();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		prefix64List = null;
		System.gc();
	}
	
	/**
	 * Memory size dump.
	 */
	private void memorySizeDump() {
		Runtime rt = Runtime.getRuntime();
		log.info("Total: " + rt.totalMemory());
		log.info("Free: " + rt.freeMemory());
	}
	
	/**
	 * Test set low ip.
	 * 
	 * @throws Exception the exception
	 */
	public void testSetLowIp() throws Exception
	{
		InetAddress low = InetAddress.getByName("3ffe::1");
		prefix64List.setUsed(new BigInteger(low.getAddress()));
		assertTrue(prefix64List.isUsed(new BigInteger(low.getAddress())));
		log.debug(prefix64List.toString());
	}

	/**
	 * Test set mid ip.
	 * 
	 * @throws Exception the exception
	 */
	public void testSetMidIp1() throws Exception
	{
		InetAddress mid = InetAddress.getByName("3ffe::7fff:fffe");
		prefix64List.setUsed(new BigInteger(mid.getAddress()));
		assertTrue(prefix64List.isUsed(new BigInteger(mid.getAddress())));
		log.debug(prefix64List.toString());
	}

	/**
	 * Test set mid ip.
	 * 
	 * @throws Exception the exception
	 */
	public void testSetMidIp2() throws Exception
	{
		InetAddress mid = InetAddress.getByName("3ffe::ffff:ffff");
		prefix64List.setUsed(new BigInteger(mid.getAddress()));
		assertTrue(prefix64List.isUsed(new BigInteger(mid.getAddress())));
		log.debug(prefix64List.toString());
	}
	
	/**
	 * Test set high ip.
	 * 
	 * @throws Exception the exception
	 */
	public void testSetHighIp() throws Exception
	{
		InetAddress high = InetAddress.getByName("3ffe::ffff:ffff:ffff:ffff");
		prefix64List.setUsed(new BigInteger(high.getAddress()));
		assertTrue(prefix64List.isUsed(new BigInteger(high.getAddress())));
		log.debug(prefix64List.toString());
	}
	
	/**
	 * Test get next free address.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetNextFreeAddress() throws Exception
	{
		InetAddress ip = InetAddress.getByAddress(smallPool.getNextFree().toByteArray());
		assertNotNull(ip);
		assertEquals(InetAddress.getByName("3ffe::0"), ip);
		ip = InetAddress.getByAddress(smallPool.getNextFree().toByteArray());
		assertNotNull(ip);
		assertEquals(InetAddress.getByName("3ffe::1"), ip);
		ip = InetAddress.getByAddress(smallPool.getNextFree().toByteArray());
		assertNotNull(ip);
		assertEquals(InetAddress.getByName("3ffe::2"), ip);
		BigInteger bi = smallPool.getNextFree();
		assertNull(bi);
	}
	
	/**
	 * Test get many free addresses.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetManyFreeAddresses() throws Exception
	{
		memorySizeDump();
		BigInteger bi = new BigInteger(InetAddress.getByName("3ffe::0").getAddress());
		for (int i=0; i<1000000; i++) {	// 1M leases
			InetAddress ip = InetAddress.getByAddress(prefix64List.getNextFree().toByteArray());
			assertNotNull(ip);
			assertEquals(InetAddress.getByAddress(bi.toByteArray()), ip);
			bi = bi.add(BigInteger.ONE);
			if ((i > 0) && (i % 1000 == 0)) {
				log.info(i + " addresses allocated");
				memorySizeDump();
			}
		}
		log.debug(prefix64List.toString());
	}
	
	/**
	 * Test threaded get addresses.
	 * 
	 * @throws Exception the exception
	 */
	public void testThreadedGetAddresses() throws Exception
	{
		memorySizeDump();
		ThreadGroup testGroup = new ThreadGroup("TestThreadGroup");
		for (int i=0; i<100; i++) {	// 100 threads
			Thread t = new Thread(testGroup, new Runnable() {

				public void run() {
					for (int i=0; i<10000; i++) {	// 10K leases per thread
						InetAddress ip = null;
						try {
							ip = InetAddress.getByAddress(prefix64List.getNextFree().toByteArray());
							assertNotNull(ip);
							log.info("IP=" + ip.getHostAddress());
						}
						catch (UnknownHostException ex) {
							fail(ex.getMessage());
						}
					}
				}
				
			}, "TestThread-"+i);
			t.start();
		}
		while (testGroup.activeCount() > 0) {
			Thread.sleep(5000);	// wait another 5 seconds for threads to complete
		}
	}
	
	/**
	 * Suite.
	 * 
	 * @return the test
	 */
	public static Test suite()
	{
//		return new TestSuite(TestFreeList.class);
		TestSuite suite = new TestSuite();
//		suite.addTest(new TestFreeList("testGetManyFreeAddresses"));
		suite.addTest(new TestFreeList("testSetLowIp"));
		suite.addTest(new TestFreeList("testSetMidIp1"));
		suite.addTest(new TestFreeList("testSetMidIp2"));
		suite.addTest(new TestFreeList("testSetHighIp"));
		return suite;
	}
}
