package com.jagornet.dhcp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.Options;
import org.junit.Test;

public class TestJagornetDhcpServerCli {

    @Test
    public void testVersionOutput() {
        Version v = new Version();
        String versionStr = v.getVersion();
        assertNotNull(versionStr);
        assertTrue(versionStr.contains("Copyright 2009-"));
        assertTrue(versionStr.contains("Jagornet Technologies, LLC"));
    }

    @Test
    public void testCliPortOptions() throws Exception {
        JagornetDhcpServer server = new JagornetDhcpServer(new String[] {
            "-4p", "10067",
            "-6p", "10547",
            "-gp", "9066",
            "-hp", "9067"
        });
        Options options = server.options;
        assertNotNull(options);
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

        assertEquals(10067, server.v4PortNumber);
        assertEquals(10547, server.v6PortNumber);
        assertEquals(9066, server.grpcPortNumber);
        assertEquals(9067, server.httpsPortNumber);
    }
}
