package com.jagornet.dhcp.server.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.server.config.DhcpLink;

/**
 * Root resource (exposed at "dhcplinks" path)
 */
@Path(DhcpLinksResource.PATH)
public class DhcpLinksResource {
	
	private static Logger log = LoggerFactory.getLogger(DhcpLinksResource.class);
	
	public static final String PATH = "dhcplinks";
	public static final String PATHPARAM_SUBNET = "subnet";
	public static final String SUBNET = "/{" + PATHPARAM_SUBNET + "}";
	
	private DhcpLinksService service;
	
	public DhcpLinksResource() {
		service = new DhcpLinksService();
	}

	@GET
    @Secured	// registration AuthenticationFilter
    @Path(SUBNET)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLinkState(@PathParam(PATHPARAM_SUBNET) String subnetDashForSlash) {
		try {
			String[] s = subnetDashForSlash.split("-");
			if ((s != null) && (s.length == 2)) {
				Subnet subnet = new Subnet(s[0], s[1]);
				DhcpLink.State state = service.getLinkState(subnet);
				if (state != null) {
					return Response.ok(state).build();
				}
				else {
					return Response
							.serverError()
							.entity("Link state not found for subnet: " + subnet)
							.build();
				}
			}
			else {
				return Response
						.serverError()
						.entity("Subnet invalid: expected 'subnetaddr-prefixlen'")
						.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in getLinkState: " + ex);
			return Response.serverError().entity(ex).build();
		} 
    }
}
