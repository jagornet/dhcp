package com.jagornet.dhcp.server.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.config.xml.PoliciesType;

/**
 * Root resource (exposed at "dhcpserverconfig" path)
 */
@Path(DhcpServerConfigResource.PATH)
public class DhcpServerConfigResource {
	
	private static Logger log = LoggerFactory.getLogger(DhcpServerConfigResource.class);
	
	public static final String PATH = "dhcpserverconfig";
	public static final String GLOBAL_POLICIES = "/globalpolicies";
	
	private DhcpServerConfigService service;
	
	public DhcpServerConfigResource() {
		service = new DhcpServerConfigService();
	}

	@GET
    @Secured	// registration AuthenticationFilter
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDhcpServerConfig() {
		try {
			DhcpServerConfig config = service.getDhcpServerConfig();
			if (config != null) {
				return Response.ok(config).build();
			}
			else {
				return Response
						.serverError()
						.entity("Server config is null")
						.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in getDhcpServerConfig: " + ex);
			return Response.serverError().entity(ex).build();
		} 
    }
	
    @PUT
    @Secured	// registration AuthenticationFilter
    // Jackson object mapper is only setup for JSON, so XML throws 400/BadRequest 
    // @Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putDhcpServerConfig(DhcpServerConfig dhcpServerConfig) {
		try {
    		log.debug("putDhcpServerConfig: data=" + dhcpServerConfig);
			if (dhcpServerConfig != null) {
				DhcpServerConfig newConfig = service.updateDhcpServerConfig(dhcpServerConfig);
				if (newConfig != null) {
					return Response.ok(newConfig).build();
				}
				else {
					return Response
							.serverError()
							.entity("Server new config is null")
							.build();
				}
			}
			else {
				return Response
						.serverError()
						.entity("Server put config is null")
						.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in putDhcpServerConfig", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		}
    }
	
    @GET
    @Secured
    @Path(GLOBAL_POLICIES)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGlobalPolicies() {
		try {
			PoliciesType policies = service.getGlobalPolicies();
			if (policies != null) {
				return Response.ok(policies).build();
			}
			else {
				return Response
						.serverError()
						.entity("Server global policies is null")
						.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in getGlobalPolicies: " + ex);
			return Response.serverError().entity(ex).build();
		} 
    }
	
    @PUT
    @Secured	// registration AuthenticationFilter
    @Path(GLOBAL_POLICIES)
    // Jackson object mapper is only setup for JSON, so XML throws 400/BadRequest 
    // @Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putGlobalPolicies(PoliciesType globalPolicies) {
		try {
    		log.debug("putGlobalPolicies: data=" + globalPolicies);
			if (globalPolicies != null) {
				PoliciesType newPolicies = service.updateGlobalPolicies(globalPolicies);
				if (newPolicies != null) {
					return Response.ok(newPolicies).build();
				}
				else {
					return Response
							.serverError()
							.entity("Server new global policies is null")
							.build();
				}
			}
			else {
				return Response
						.serverError()
						.entity("Server put global policies is null")
						.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in putGlobalPolicies", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		}
    }
    
}
