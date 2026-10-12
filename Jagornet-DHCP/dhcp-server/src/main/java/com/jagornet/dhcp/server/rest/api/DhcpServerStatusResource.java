/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.rest.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

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
