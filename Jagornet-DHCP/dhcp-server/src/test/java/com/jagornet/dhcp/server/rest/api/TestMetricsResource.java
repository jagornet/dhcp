package com.jagornet.dhcp.server.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.jagornet.dhcp.server.metrics.DhcpMetrics;

public class TestMetricsResource {

    @Test
    public void testMetricsEndpoint() {
        // Record sample metrics
        DhcpMetrics.getInstance().incrementPacketCounter("v4", "DISCOVER", "success");
        DhcpMetrics.getInstance().recordPacketLatency("v4", "DISCOVER", 1500000L);

        MetricsResource resource = new MetricsResource();
        Response response = resource.getMetrics();

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        
        String metricsText = (String) response.getEntity();
        assertNotNull(metricsText);
        assertTrue(metricsText.contains("dhcp_packets_total"));
        assertTrue(metricsText.contains("type=\"DISCOVER\""));
        assertTrue(metricsText.contains("version=\"v4\""));
    }
}
