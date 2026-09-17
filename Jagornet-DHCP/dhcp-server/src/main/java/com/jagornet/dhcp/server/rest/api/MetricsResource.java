package com.jagornet.dhcp.server.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.jagornet.dhcp.server.metrics.DhcpMetrics;

/**
 * REST resource for Prometheus metrics scraping.
 * Endpoint is secured using @Secured annotation.
 */
@Path("/metrics")
public class MetricsResource {

    @GET
    @Secured
    @Produces(MediaType.TEXT_PLAIN)
    public Response getMetrics() {
        String metricsData = DhcpMetrics.getInstance().scrape();
        return Response.ok(metricsData).build();
    }
}
