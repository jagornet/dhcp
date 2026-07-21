package com.jagornet.dhcp.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;


public class DhcpServerPoliciesTest {

    @Before
    public void setUp() {
        // Reset SERVER_PROPERTIES to default before each test
        DhcpServerPolicies.SERVER_PROPERTIES = new Properties(DhcpServerPolicies.DEFAULT_PROPERTIES);
    }

    @Test
    public void testDefaultProperties() {
        for (DhcpServerPolicies.Property prop : DhcpServerPolicies.Property.values()) {
            assertEquals(prop.value(), DhcpServerPolicies.getProperties().getProperty(prop.key()));
        }
    }

    @Test
    public void testSetProperty() {
        DhcpServerPolicies.setProperty(DhcpServerPolicies.Property.BINDING_MANAGER_REAPER_STARTUP_DELAY, "20000");
        assertEquals("20000", DhcpServerPolicies.getProperties().getProperty(DhcpServerPolicies.Property.BINDING_MANAGER_REAPER_STARTUP_DELAY.key()));
    }

    @Test
    public void testLoadPropertiesFile() throws IOException {
        // Create a temporary properties file for testing
        String tempFileName = "test.properties";
        java.nio.file.Files.write(java.nio.file.Paths.get(tempFileName), "binding.manager.reaper.startupDelay=30000".getBytes());

        DhcpServerPolicies.loadPropertiesFile(tempFileName);
        assertEquals("30000", DhcpServerPolicies.getProperties().getProperty(DhcpServerPolicies.Property.BINDING_MANAGER_REAPER_STARTUP_DELAY.key()));

        // Clean up the temporary file
        java.nio.file.Files.delete(java.nio.file.Paths.get(tempFileName));
    }

    @Test
    public void testGlobalPolicy() {
        assertEquals(DhcpServerPolicies.Property.BINDING_MANAGER_REAPER_STARTUP_DELAY.value(), DhcpServerPolicies.globalPolicy(DhcpServerPolicies.Property.BINDING_MANAGER_REAPER_STARTUP_DELAY));
    }

    @Test
    public void testGlobalPolicyAsBoolean() {
        assertTrue(DhcpServerPolicies.globalPolicyAsBoolean(DhcpServerPolicies.Property.BINDING_MANAGER_RECONCILE_POOLS_ON_STARTUP));
    }

    @Test
    public void testGlobalPolicyAsInt() {
        assertEquals(16, DhcpServerPolicies.globalPolicyAsInt(DhcpServerPolicies.Property.CHANNEL_THREADPOOL_SIZE));
    }

    @Test
    public void testGlobalPolicyAsLong() {
        assertEquals(60000L, DhcpServerPolicies.globalPolicyAsLong(DhcpServerPolicies.Property.BINDING_MANAGER_REAPER_RUN_PERIOD));
    }

    @Test
    public void testGlobalPolicyAsFloat() {
        assertEquals(0.5f, DhcpServerPolicies.globalPolicyAsFloat(DhcpServerPolicies.Property.V6_IA_NA_T1), 0.0f);
    }
}