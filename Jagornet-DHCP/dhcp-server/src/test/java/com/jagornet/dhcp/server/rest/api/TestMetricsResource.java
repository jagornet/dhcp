/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.ws.rs.core.Response;

import org.junit.Test;

import com.jagornet.dhcp.server.metrics.DhcpMetrics;

public class TestMetricsResource {

    @Test
    public void testMetricsEndpoint() {
        // Record sample metrics
        DhcpMetrics.getInstance().incrementPacketCounter("v4", "DISCOVER", "processed");
        DhcpMetrics.getInstance().incrementPacketCounter("v4", "OFFER", "sent");
        DhcpMetrics.getInstance().recordPacketLatency("v4", "DISCOVER", 1500000L);

        MetricsResource resource = new MetricsResource();
        Response response = resource.getMetrics();

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        
        String metricsText = (String) response.getEntity();
        assertNotNull(metricsText);
        assertTrue(metricsText.contains("dhcp_packets_total"));
        assertTrue(metricsText.contains("type=\"DISCOVER\""));
        assertTrue(metricsText.contains("type=\"OFFER\""));
        assertTrue(metricsText.contains("result=\"processed\""));
        assertTrue(metricsText.contains("result=\"sent\""));
        assertTrue(metricsText.contains("version=\"v4\""));
    }
}
