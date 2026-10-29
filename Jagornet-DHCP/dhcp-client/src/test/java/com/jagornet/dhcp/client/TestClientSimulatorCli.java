/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.Options;
import org.junit.Test;

import com.jagornet.dhcp.core.util.DhcpConstants;

public class TestClientSimulatorCli {

    @Test
    public void testClientSimulatorV4CliOptions() throws Exception {
        ClientSimulatorV4 sim = new ClientSimulatorV4(new String[] {
                "-p", "10067",
                "-hn",
                "-n", "25"
        }, false);

        Options options = sim.options;
        assertNotNull(options);
        assertTrue(options.hasOption("p"));
        assertTrue(options.hasOption("cp"));
        assertTrue(options.hasOption("sp"));
        assertTrue(options.hasOption("hn"));
        assertTrue(options.hasOption("h")); // -h is now help
        assertTrue(options.hasOption("help"));
        assertTrue(options.hasOption("?"));

        assertEquals(10067, sim.serverPort);
        assertEquals(10068, sim.clientPort); // serverPort + 1 automatically paired
        assertEquals(25, sim.numRequests);
        assertTrue(sim.sendHostname);
    }

    @Test
    public void testClientSimulatorV4EmptyArgsShowsHelp() throws Exception {
        ClientSimulatorV4 sim = new ClientSimulatorV4(new String[] {}, false);
        // parseOptions returns false on empty args, so options are setup but execution doesn't run
        assertFalse(sim.sendHostname);
        assertEquals(100, sim.numRequests);
    }

    @Test
    public void testClientSimulatorV4HelpOption() throws Exception {
        ClientSimulatorV4 simH = new ClientSimulatorV4(new String[] { "-h" }, false);
        // parseOptions returns false on help, so sendHostname is unchanged (false)
        assertFalse(simH.sendHostname);

        ClientSimulatorV4 simHelp = new ClientSimulatorV4(new String[] { "--help" }, false);
        assertFalse(simHelp.sendHostname);

        ClientSimulatorV4 simQuestion = new ClientSimulatorV4(new String[] { "-?" }, false);
        assertFalse(simQuestion.sendHostname);
    }

    @Test
    public void testClientSimulatorV4PortOverrides() throws Exception {
        ClientSimulatorV4 sim = new ClientSimulatorV4(new String[] {
                "-sp", "10067",
                "-cp", "10069"
        }, false);

        assertEquals(10067, sim.serverPort);
        assertEquals(10069, sim.clientPort);
    }

    @Test
    public void testClientSimulatorV6CliOptions() throws Exception {
        ClientSimulatorV6 sim = new ClientSimulatorV6(new String[] {
                "-p", "10547",
                "-f",
                "-n", "10"
        }, false);

        Options options = sim.options;
        assertNotNull(options);
        assertTrue(options.hasOption("p"));
        assertTrue(options.hasOption("cp"));
        assertTrue(options.hasOption("sp"));
        assertTrue(options.hasOption("f"));
        assertTrue(options.hasOption("h"));
        assertTrue(options.hasOption("help"));
        assertTrue(options.hasOption("?"));

        assertEquals(10547, sim.serverPort);
        assertEquals(10548, sim.clientPort); // serverPort + 1 automatically paired
        assertEquals(10, sim.numRequests);
        assertTrue(sim.sendFqdn);
    }

    @Test
    public void testClientSimulatorV6EmptyArgsShowsHelp() throws Exception {
        ClientSimulatorV6 sim = new ClientSimulatorV6(new String[] {}, false);
        assertFalse(sim.sendFqdn);
        assertEquals(100, sim.numRequests);
    }

    @Test
    public void testClientSimulatorV6HelpOption() throws Exception {
        ClientSimulatorV6 simH = new ClientSimulatorV6(new String[] { "-h" }, false);
        assertFalse(simH.sendFqdn);

        ClientSimulatorV6 simHelp = new ClientSimulatorV6(new String[] { "--help" }, false);
        assertFalse(simHelp.sendFqdn);

        ClientSimulatorV6 simQuestion = new ClientSimulatorV6(new String[] { "-?" }, false);
        assertFalse(simQuestion.sendFqdn);
    }
}
