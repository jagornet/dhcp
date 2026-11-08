/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.Options;
import org.junit.Test;

import com.jagornet.dhcp.server.util.ListInterfaces;

public class TestJagornetDhcpServerCli {

    @Test
    public void testVersionOutput() {
        Version v = new Version();
        String versionStr = v.getVersion();
        assertNotNull(versionStr);
        assertTrue(versionStr.contains("Jagornet DHCP Server"));
        assertTrue(versionStr.contains("Copyright "));
        assertTrue(versionStr.contains("Jagornet Technologies, LLC"));
    }

    @Test
    public void testCliPortOptions() throws Exception {
        JagornetDhcpServer server = new JagornetDhcpServer(new String[] {
            "-c", "config/dhcpserver.xml",
            "-4p", "10067",
            "-6p", "10547",
            "-gp", "9066",
            "-hp", "9067"
        });
        Options options = server.options;
        assertNotNull(options);
        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("4p"));
        assertTrue(options.hasOption("6p"));
        assertTrue(options.hasOption("gp"));
        assertTrue(options.hasOption("hp"));
        assertTrue(options.hasOption("4u"));
        assertTrue(options.hasOption("4b"));
        assertTrue(options.hasOption("6u"));
        assertTrue(options.hasOption("6m"));
        assertTrue(options.hasOption("ga"));
        assertTrue(options.hasOption("ha"));
        assertFalse(options.hasOption("li"));
        assertTrue(options.hasOption("h"));
        assertTrue(options.hasOption("help"));
        assertTrue(options.hasOption("?"));

        assertEquals("config/dhcpserver.xml", server.configFilename);
        assertEquals(10067, server.v4PortNumber);
        assertEquals(10547, server.v6PortNumber);
        assertEquals(9066, server.grpcPortNumber);
        assertEquals(9067, server.httpsPortNumber);
    }

    @Test
    public void testMissingConfigFileRejected() {
        JagornetDhcpServer server = new JagornetDhcpServer(new String[] {
            "-4p", "10067"
        }, false);
        assertFalse(server.parseOptions(new String[] { "-4p", "10067" }));
    }

    @Test
    public void testGenerateTestConfigCliOptions() {
        com.jagornet.dhcp.server.config.GenerateTestConfig gen = 
            new com.jagornet.dhcp.server.config.GenerateTestConfig(new String[] { "-h" }, false);
        Options options = gen.getOptions();
        assertNotNull(options);
        assertTrue(options.hasOption("f"));
        assertTrue(options.hasOption("i"));
        assertTrue(options.hasOption("v"));
        assertTrue(options.hasOption("l"));
        assertTrue(options.hasOption("h"));
        assertTrue(options.hasOption("help"));
        assertTrue(options.hasOption("?"));
    }

    @Test
    public void testListInterfacesUtility() throws Exception {
        ListInterfaces li = new ListInterfaces(new String[] { "-a" }, false);
        Options options = li.getOptions();
        assertNotNull(options);
        assertTrue(options.hasOption("a"));
        assertTrue(options.hasOption("h"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ListInterfaces.printInterfaces(true, new PrintStream(baos));
        assertTrue(baos.toString().length() > 0);
    }
}
