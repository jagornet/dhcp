package com.jagornet.dhcp.server.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Root resource (exposed at "dhcpserverstatus" path)
 */
@Path(DhcpServerStatusResource.PATH)
public class DhcpServerStatusResource {
	
	private static Logger log = LoggerFactory.getLogger(DhcpServerStatusResource.class);
	
	public static final String PATH = "dhcpserverstatus";
	public static final String HASTATE = "/hastate";
	
	private DhcpServerStatusService service;
	
	public DhcpServerStatusResource() {
		service = new DhcpServerStatusService();
	}

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Secured	// registration AuthenticationFilter
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus(@Context ContainerRequestContext context) {
    	SecurityContext securityContext = context.getSecurityContext();
		if (securityContext != null) {
			// AuthRole determines endpoint
			if (securityContext.isUserInRole(
					AuthenticationFilter.AuthRole.HA_PEER.toString())) {
				// "internal" HA peer endpoint
				return service.haPeerGetStatus();
			}
			else if (securityContext.isUserInRole(
					AuthenticationFilter.AuthRole.REST_API.toString())) {
				// external REST API endpoint
				return service.getStatus();
			}
		}
		log.error("Failed to verify security context role");
		return null;
    }
    
    @GET
    @Secured	// registration AuthenticationFilter
    @Path(HASTATE)
    @Produces(MediaType.TEXT_PLAIN)
    public String getHaState(@Context ContainerRequestContext context) {
    	SecurityContext securityContext = context.getSecurityContext();
		if (securityContext != null) {
			// AuthRole determines endpoint
			if (securityContext.isUserInRole(
					AuthenticationFilter.AuthRole.HA_PEER.toString())) {
				// "internal" HA peer endpoint
				return service.haPeerGetHaState();
			}
			else if (securityContext.isUserInRole(
					AuthenticationFilter.AuthRole.REST_API.toString())) {
				// external REST API endpoint
				return service.getHaState();
			}
		}
		log.error("Failed to verify security context role");
		return null;
    }
}
