package com.jagornet.dhcp.server.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.netty.httpserver.NettySecurityContext;


/**
 * Root resource (exposed at "dhcpserverstatus" path)
 */
@Path(DhcpServerStatusResource.PATH)
public class DhcpServerStatusResource {
	
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
    @Secured
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus(@Context ContainerRequestContext context) {
		SecurityContext securityContext = context.getSecurityContext();
    	if (securityContext != null) {
			// TODO: No sysout and do NPE checks!
    		System.out.println(securityContext.isSecure());
    		System.out.println(securityContext.getAuthenticationScheme());
    		System.out.println(securityContext.getUserPrincipal().getName());
    		if (securityContext instanceof AuthFilterSecurityContext) {
    			AuthFilterSecurityContext authSecurityContext = 
    					(AuthFilterSecurityContext)securityContext;
        		System.out.println(authSecurityContext
			        				.getNettySecurityContext()
			        				.getNettyContext()
			        				.channel()
			        				.remoteAddress());
    		}
    	}
    	return service.getStatus();
    }
    
    @GET
    @Path(HASTATE)
    @Produces(MediaType.TEXT_PLAIN)
    public String getHaState() {
        return service.getHaState();
    }
}
