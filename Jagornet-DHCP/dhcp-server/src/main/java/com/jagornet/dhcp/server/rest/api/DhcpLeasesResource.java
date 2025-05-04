package com.jagornet.dhcp.server.rest.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseCallbackHandler;
import com.jagornet.dhcp.server.db.InetAddressCallbackHandler;
import com.jagornet.dhcp.server.db.ProcessLeaseException;

@Path(DhcpLeasesResource.PATH)
public class DhcpLeasesResource {
	
	private static Logger log = LoggerFactory.getLogger(DhcpLeasesResource.class);
	
	public static final String PATH = "dhcpleases";
	public static final String IPS = "/ips";
	public static final String PATHPARAM_IPADDRESS = "ipaddress";
	public static final String IPADDRESS = "/{" + PATHPARAM_IPADDRESS + "}";
	public static final String IPSTREAM = "/ipstream";
	public static final String DHCPLEASESTREAM = "/dhcpleasestream";

	public static final String QUERYPARAM_START = "start";
	public static final String QUERYPARAM_END = "end";
	public static final String QUERYPARAM_HAUPDATE = "haupdate";
	public static final String QUERYPARAM_HAUPDATE_ALL = "all";
	public static final String QUERYPARAM_HAUPDATE_UNSYNCED = "unsynced";

	private DhcpLeasesService leasesService;
	private DhcpServerStatusService statusService;
	private ObjectMapper objectMapper;
	
	public DhcpLeasesResource() {
		leasesService = new DhcpLeasesService();
		statusService = new DhcpServerStatusService();
		objectMapper = new JacksonObjectMapper().getJsonObjectMapper();
	}

	public static String buildPostPath(String ip) {
//		return DhcpLeasesResource.PATH_IPS + "/" + ip;
		return DhcpLeasesResource.PATH;
	}

	public static String buildPutPath(String ip) {
//		return DhcpLeasesResource.PATH_IPS + "/" + ip;
		return DhcpLeasesResource.PATH + "/" + ip;
	}

    @GET 
    @Secured	// registration AuthenticationFilter
    @Path(IPS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDhcpLeaseIps(@QueryParam(QUERYPARAM_START) String start,
    								@QueryParam(QUERYPARAM_END) String end) {
    	// this will be served at http://localhost/dhcpleases
    	try {
    		log.debug("getDhcpLeaseIps: start=" + start + " end=" + end);
        	StringBuilder sb = new StringBuilder();
    		List<InetAddress> ips = leasesService.getRangeLeaseIPs(start, end);
        	if ((ips != null) && !ips.isEmpty()) {
        		for (InetAddress ip : ips) {
    				sb.append(ip.getHostAddress())
    					.append(System.lineSeparator());
    			}
        	}
        	return Response.ok(sb.toString()).build();
		}
		catch (Exception ex) {
			log.error("Exception caught in getDhcpLeaseIps", ex);
			return Response.serverError().entity(ex).build();
		}
    }

    @GET 
    @Secured	// registration AuthenticationFilter
    @Path(IPSTREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDhcpLeaseIpStream(@QueryParam(QUERYPARAM_START) String start,
										 @QueryParam(QUERYPARAM_END) String end) {
    	// this will be served at http://localhost/dhcpleases/ipstream
    	/*
    	 * See commented code in BaseAddrBindingManager.ReaperTimerTask.run
    	 * that confirms that implementation below does not leak file descriptors
    	 */
		try {
    		log.debug("getDhcpLeaseIpStream: start=" + start + " end=" + end);
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					Writer writer = new BufferedWriter(new OutputStreamWriter(os));
					leasesService.getRangeLeaseIPs(start, end, 
							new InetAddressCallbackHandler() {
						@Override
						public void processInetAddress(InetAddress inetAddr) throws IOException {
							writer.write(inetAddr.getHostAddress());
							writer.write(System.lineSeparator());
							writer.flush();  // important to flush
						}
					});
			    }
			};
			return Response.ok(stream).build();
		}
		catch (Exception ex) {
			log.error("Exception caught in getDhcpLeaseIpStream", ex);
			return Response.serverError().entity(ex).build();
		}
    }

    @GET
    @Secured	// registration AuthenticationFilter
    @Path(DHCPLEASESTREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDhcpLeaseStream(@QueryParam(QUERYPARAM_START) String start,
			   						   @QueryParam(QUERYPARAM_END) String end,
		    						   @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {

		log.debug("getDhcpLeaseStream: start=" + start + " end=" + end);
    	// this will be served at http://localhost/dhcpleases/dhcpleasestream
    	Response response = getJsonDhcpLeaseStream(start, end, haUpdate);
    	
		//TODO: consider this implementation - hack to set HA FSM state?
    	if ((haUpdate != null) &&
    		(response.getStatus() == Status.OK.ordinal())) {
    		statusService.setHaState(DhcpServerStatusService.SYNCING_TO_PEER);
    	}
    	return response;
    }

    protected Response getJsonDhcpLeaseStream(String start, String end, String haUpdate) {
    	final boolean unsyncedLeasesOnly = 
    			QUERYPARAM_HAUPDATE_UNSYNCED.equalsIgnoreCase(haUpdate) ? true : false;
    	/*
    	 * See commented code in BaseAddrBindingManager.ReaperTimerTask.run
    	 * that confirms that implementation below does not leak file descriptors
    	 */
    	try {
    		log.debug("getJsonDhcpLeaseStream: start=" + start + " end=" + end);
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					JsonFactory factory = objectMapper.getFactory();
					try (JsonGenerator generator = factory.createGenerator(os)) {
						generator.writeStartArray();
						leasesService.getRangeLeases(start, end,
								new DhcpLeaseCallbackHandler() {
							@Override
							public void processDhcpLease(DhcpLease dhcpLease) throws ProcessLeaseException {
								log.debug("Writing DhcpLease to JSON stream: " + dhcpLease);
								try{
									generator.writeObject(dhcpLease);
									generator.flush();
								}
								catch (IOException e) {
									log.error("Failed to write DhcpLease to JSON stream", e);
									throw new ProcessLeaseException(e);
								}
							}
						}, unsyncedLeasesOnly);
						log.debug("DhcpLease JSON stream complete");
						generator.writeEndArray();
					}
			    }
			};
			return Response.ok(stream).build();    	
    	}
    	catch (Exception ex) {
			log.error("Exception caught in getJsonDhcpLeaseStream", ex);
			return Response.serverError().entity(ex).build();
    	}
    }
    
    @GET 
    @Secured	// registration AuthenticationFilter
    @Path(IPADDRESS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDhcpLease(@PathParam(PATHPARAM_IPADDRESS) String ipAddress) {
    	// this will be served at http://localhost/dhcpleases/{ipaddress}
    	try {
    		log.debug("getDhcpLease: ipAddress=" + ipAddress);
			DhcpLease dhcpLease = leasesService.getDhcpLease(InetAddress.getByName(ipAddress));
			if (dhcpLease != null) {
				return Response.ok(dhcpLease).build();
			}
			else {
				return Response.status(Response.Status.NOT_FOUND)
								.entity("Lease not found for IP: " + ipAddress)
								.build();
			}
    	}
    	catch (Exception ex) {
			log.error("Exception caught in getDhcpLease", ex);
			return Response.serverError().entity(ex).build();
    	}
    }

    @POST 
    @Secured	// registration AuthenticationFilter
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDhcpLease(DhcpLease dhcpLease,
    							  @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
    	try {
    		log.debug("postDhcpLease: data=" + dhcpLease);
        	if (dhcpLease != null) {
        		if ("true".equalsIgnoreCase(haUpdate)) {
        			// if this update is from the HA peer, then we are
        			// syncing the lease, so set the haPeerState=state
        			dhcpLease.setHaPeerState(dhcpLease.getState());
        		}
    	    	if (leasesService.createDhcpLease(dhcpLease)) {
        			return Response.ok(dhcpLease).build();  	    		
    	    	}
    	    	else {
    	    		return Response
    	    				.serverError()
    	    				.entity("POST - createDhcpLease failed!")
    	    				.build();
    	    	}
	    	}
	    	else {
	    		return Response
	    				.status(Response.Status.BAD_REQUEST)
	    				.entity("POST - dhcpLease is null!")
	    				.build();
	    	}
        }
    	catch (Exception ex) {
			log.error("Exception caught in postDhcpLease", ex);
    		return Response.serverError().entity(ex).build();
    	}
	}
	
    @PUT
    @Secured	// registration AuthenticationFilter
    @Path(IPADDRESS)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putDhcpLease(@PathParam(PATHPARAM_IPADDRESS) String ipAddress,
				    		 	 DhcpLease dhcpLease,
				    		 	 @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
		try {
    		log.debug("putDhcpLease: data=" + dhcpLease);
			if (dhcpLease != null) {
				if ("true".equalsIgnoreCase(haUpdate)) {
					// if this update is from the HA peer, then we are
					// syncing the lease, so set the haPeerState=state
					dhcpLease.setHaPeerState(dhcpLease.getState());
				}
				if (leasesService.createOrUpdateDhcpLease(dhcpLease)) {
					return Response.ok(dhcpLease).build();
				}
				else {
					return Response
							.serverError()
							.entity("PUT - createOrUpdateDhcpLease failed!")
							.build();
				}
			}
			else {
	    		return Response
	    				.status(Response.Status.BAD_REQUEST)
	    				.entity("PUT - dhcpLease is null!")
	    				.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in putDhcpLease", ex);
			return Response.serverError().entity(ex).build();
		}
    }
 
    @DELETE
    @Secured	// registration AuthenticationFilter
    @Path(IPADDRESS)
    public Response deleteDhcpLease(@PathParam(PATHPARAM_IPADDRESS) String ipAddress) {
    	try {
    		log.debug("deleteDhcpLease: ipAddress=" + ipAddress);
    		if (leasesService.deleteDhcpLease(InetAddress.getByName(ipAddress))) {
    			return Response.ok().build();
    		}
    		else {
				return Response
						.serverError()
						.entity("DELETE - deleteDhcpLease failed!")
						.build();
    		}
		}
		catch (Exception ex) {
			log.error("Exception caught in deleteDhcpLease", ex);
			return Response.serverError().entity(ex).build();
		}
    }
    
}
