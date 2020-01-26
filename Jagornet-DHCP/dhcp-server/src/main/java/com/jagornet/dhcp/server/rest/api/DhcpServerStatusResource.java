package com.jagornet.dhcp.server.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus() {
        return service.getStatus();
    }
    
    @GET
    @Path(HASTATE)
    @Produces(MediaType.TEXT_PLAIN)
    public String getHaState() {
        return service.getHaState();
    }
}
