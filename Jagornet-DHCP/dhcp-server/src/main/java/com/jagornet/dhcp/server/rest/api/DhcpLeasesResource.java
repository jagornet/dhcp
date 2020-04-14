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

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseCallbackHandler;
import com.jagornet.dhcp.server.db.InetAddressCallbackHandler;

@Path(DhcpLeasesResource.PATH)
public class DhcpLeasesResource {
	
	private static Logger log = LoggerFactory.getLogger(DhcpLeasesResource.class);
	
	public static final String PATH = "dhcpleases";
	public static final String IPS = "/ips";
	public static final String PATHPARAM_IPADDRESS = "ipaddress";
	public static final String IPADDRESS = "/{" + PATHPARAM_IPADDRESS + "}";
	public static final String IPSTREAM = "/ipstream";
	public static final String DHCPLEASESSTREAM = "/dhcpleases";
	public static final String JSONLEASES = "/jsonleases";
	public static final String JSONLEASESTREAM = "/jsonleasestream";
	public static final String GSONLEASES = "/gsonleases";
	public static final String GSONLEASESTREAM = "/gsonleasestream";

	public static final String QUERYPARAM_START = "start";
	public static final String QUERYPARAM_END = "end";
	public static final String QUERYPARAM_FORMAT = "format";
	public static final String QUERYPARAM_HAUPDATE = "haupdate";
	public static final String QUERYPARAM_HAUPDATE_ALL = "all";
	public static final String QUERYPARAM_HAUPDATE_UNSYNCED = "unsynced";

	private DhcpLeasesService leasesService;
	private DhcpServerStatusService statusService;
	private Gson gson;
	
	public DhcpLeasesResource() {
		leasesService = new DhcpLeasesService();
		statusService = new DhcpServerStatusService();
		gson = new Gson();
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
        	StringBuilder sb = new StringBuilder();
    		List<InetAddress> ips = leasesService.getRangeLeaseIPs(start, end);
        	if ((ips != null) && !ips.isEmpty()) {
        		for (InetAddress ip : ips) {
    				sb.append(ip.getHostAddress()).append(System.lineSeparator());
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
    @Path(DHCPLEASESSTREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDhcpLeaseStream(@QueryParam(QUERYPARAM_START) String start,
			   						   @QueryParam(QUERYPARAM_END) String end,
		    						   @QueryParam(QUERYPARAM_FORMAT) String format,
		    						   @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
    	Response response = null;
    	if ("gson".equalsIgnoreCase(format)) {
    		response = getGsonDhcpLeaseStream(start, end, haUpdate);
    	}
    	else { // default if ("json".equalsIgnoreCase(format)){
    		response = getJsonDhcpLeaseStream(start, end, haUpdate);
    	}
    	
		//TODO: consider this implementation - hack to set HA FSM state?
    	if ((haUpdate != null) &&
    		(response.getStatus() == Status.OK.ordinal())) {
    		statusService.setHaState(DhcpServerStatusService.SYNCING_TO_PEER);
    	}
    	return response;
    }
    
    @GET
    @Secured	// registration AuthenticationFilter
    @Path(JSONLEASESTREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJsonDhcpLeaseStream(@QueryParam(QUERYPARAM_START) String start,
			 							   @QueryParam(QUERYPARAM_END) String end,
			    						   @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
    	// this will be served at http://localhost/dhcpleases/jsonleasestream
    	final boolean unsyncedLeasesOnly = 
    			QUERYPARAM_HAUPDATE_UNSYNCED.equalsIgnoreCase(haUpdate) ? true : false;
    	/*
    	 * See commented code in BaseAddrBindingManager.ReaperTimerTask.run
    	 * that confirms that implementation below does not leak file descriptors
    	 */
    	try {
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					Writer writer = new BufferedWriter(new OutputStreamWriter(os));
					leasesService.getRangeLeases(start, end, 
							new DhcpLeaseCallbackHandler() {
						@Override
						public void processDhcpLease(DhcpLease dhcpLease) throws Exception {
							writer.write(dhcpLease.toJson());
							writer.write(System.lineSeparator());
							writer.flush();  // important to flush
						}
					}, unsyncedLeasesOnly);
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
    @Path(GSONLEASESTREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getGsonDhcpLeaseStream(@QueryParam(QUERYPARAM_START) String start,
			   							   @QueryParam(QUERYPARAM_END) String end,
			    						   @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
    	// this will be served at http://localhost/dhcpleases/gsonleasestream
    	final boolean unsyncedLeasesOnly = 
    			QUERYPARAM_HAUPDATE_UNSYNCED.equalsIgnoreCase(haUpdate) ? true : false;
    	/*
    	 * See commented code in BaseAddrBindingManager.ReaperTimerTask.run
    	 * that confirms that implementation below does not leak file descriptors
    	 */
    	try {
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
					writer.beginArray();
					leasesService.getRangeLeases(start, end,
							new DhcpLeaseCallbackHandler() {
						@Override
						public void processDhcpLease(DhcpLease dhcpLease) throws Exception {
							gson.toJson(dhcpLease, DhcpLease.class, writer);
						}
					}, unsyncedLeasesOnly);
					writer.endArray();
					writer.close();
			    }
			};
			return Response.ok(stream).build();    	
    	}
    	catch (Exception ex) {
			log.error("Exception caught in getGsonDhcpLeaseStream", ex);
			return Response.serverError().entity(ex).build();
    	}
    }
    
    @GET 
    @Secured	// registration AuthenticationFilter
//  @Path(PATH_IPS_IPADDRESS)
    @Path(IPADDRESS)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DhcpLease getDhcpLease(@PathParam("ipaddress") String ipAddress) 
//    		throws UnknownHostException {
//		return service.getDhcpLease(InetAddress.getByName(ipAddress));
//    }
//    @Produces(MediaType.TEXT_PLAIN)	// JSON?
//    @Produces(MediaType.APPLICATION_JSON)
    public Response getDhcpLease(@PathParam(PATHPARAM_IPADDRESS) String ipAddress,
    							 @QueryParam(QUERYPARAM_FORMAT) String format) {
    	// this will be served at http://localhost/dhcpleases/{ipaddress}
    	try {
			DhcpLease dhcpLease = leasesService.getDhcpLease(InetAddress.getByName(ipAddress));
			if (dhcpLease != null) {
				Object leaseJson = null;
				if ((format != null) && format.equalsIgnoreCase("json")) {
					leaseJson = dhcpLease.toJson();
				}
				else if ((format != null) && format.equalsIgnoreCase("gson")) {
					leaseJson = gson.toJson(dhcpLease);
				}
				else {
					// this DhcpLeaseJson object will be
					// auto-marshalled to JSON via the
					// ResponseBuilder below, which is needed
					// because the auto-marshalling does not
					// handle the InetAddress of DhcpLease
					leaseJson = new DhcpLeaseJson(dhcpLease);
				}
			    return Response
			    	      .status(Response.Status.OK)
			    	      .entity(leaseJson)
			    	      .type(MediaType.APPLICATION_JSON)
			    	      .build();
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
//    @Path(PATH_IPS)
//    @Consumes(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postDhcpLease(String dhcpLeasePostData,
    							  @QueryParam(QUERYPARAM_FORMAT) String format,
    							  @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
    	try {
    		DhcpLease postedDhcpLease = buildDhcpLeaseFromJson(format, dhcpLeasePostData);
        	if (postedDhcpLease != null) {
        		if ("true".equalsIgnoreCase(haUpdate)) {
        			// if this update is from the HA peer, then we are
        			// syncing the lease, so set the haPeerState=state
        			postedDhcpLease.setHaPeerState(postedDhcpLease.getState());
        		}
        		/*
        		 * POST should be for create only, use PUT for create/update
        		 * 
	    		InetAddress inetAddr = postedDhcpLease.getIpAddress();
	    		DhcpLease existingDhcpLease = service.getDhcpLease(inetAddr);
	    		if (existingDhcpLease != null) {
	    			service.updateDhcpLease(inetAddr, postedDhcpLease);
	    			// TODO: what if update fails? just fall-through to catch exception?
	    			return buildResponse(format, postedDhcpLease);
	    		}
	    		else {
	    		*/
	    	    	if (leasesService.createDhcpLease(postedDhcpLease)) {
	        			return buildResponseFromDhcpLease(format, postedDhcpLease);	    	    		
	    	    	}
	    	    	else {
	    	    		return Response
	    	    				.serverError()
	    	    				.entity("POST - createDhcpLease failed!")
	    	    				.build();
	    	    	}
	    		/* } */
	    	}
	    	else {
	    		return Response
	    				.serverError()
	    				.entity("POST - DhcpLease fromJson failed!")
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
//    @Path(PATH_IPS_IPADDRESS)
    @Path(IPADDRESS)
//    @Consumes(MediaType.APPLICATION_JSON)
    public Response putDhcpLease(@PathParam(PATHPARAM_IPADDRESS) String ipAddress,
				    		 	 String dhcpLeasePostData,
				    		 	 @QueryParam(QUERYPARAM_FORMAT) String format,
				    		 	 @QueryParam(QUERYPARAM_HAUPDATE) String haUpdate) {
		try {
			DhcpLease postedDhcpLease = buildDhcpLeaseFromJson(format, dhcpLeasePostData);
			if (postedDhcpLease != null) {
				if ("true".equalsIgnoreCase(haUpdate)) {
					// if this update is from the HA peer, then we are
					// syncing the lease, so set the haPeerState=state
					postedDhcpLease.setHaPeerState(postedDhcpLease.getState());
				}
				InetAddress inetAddr = postedDhcpLease.getIpAddress();
				DhcpLease existingDhcpLease = leasesService.getDhcpLease(inetAddr);
				if (existingDhcpLease != null) {
					if (leasesService.updateDhcpLease(inetAddr, postedDhcpLease)) {
						// TODO: what if update fails? just fall-through to catch exception?
						return buildResponseFromDhcpLease(format, postedDhcpLease);
					}
					else {
						return Response
								.serverError()
								.entity("PUT - updateDhcpLease failed!")
								.build();
					}
				}
				else {
					if (leasesService.createDhcpLease(postedDhcpLease)) {
						return buildResponseFromDhcpLease(format, postedDhcpLease);	    	    		
					}
					else {
						return Response
								.serverError()
								.entity("PUT - createDhcpLease failed!")
								.build();
					}
				}
			}
			else {
				return Response
					.serverError()
					.entity("PUT - buildDhcpLeaseFromJson failed!")
					.build();
			}
		}
		catch (Exception ex) {
			log.error("Exception caught in postDhcpLease", ex);
			return Response.serverError().entity(ex).build();
		}
    }
 
    @DELETE
    @Secured	// registration AuthenticationFilter
//    @Path(PATH_IPS_IPADDRESS)
    @Path(IPADDRESS)
    public Response deleteDhcpLease(@PathParam(PATHPARAM_IPADDRESS) String ipAddress) {
    	try {
    		if (leasesService.deleteDhcpLease(InetAddress.getByName(ipAddress))) {
    			return Response.ok().build();
    		}
    		else {
    			return Response.serverError().build();
    		}
		}
		catch (Exception ex) {
			log.error("Exception caught in deleteDhcpLease", ex);
			return Response.serverError().entity(ex).build();
		}
    }
    

    private DhcpLease buildDhcpLeaseFromJson(String format, String dhcpLeaseData) {
		DhcpLease dhcpLease = null;
    	if ("gson".equalsIgnoreCase(format)) {
    		dhcpLease = gson.fromJson(dhcpLeaseData, DhcpLease.class);
    	}
    	else { // default if ("json".equalsIgnoreCase(format)){
    		dhcpLease = DhcpLease.fromJson(dhcpLeaseData); 
    	}
    	return dhcpLease;
    }
    
	private Response buildResponseFromDhcpLease(String format, DhcpLease postedDhcpLease) {
		String response = null;
		if ("gson".equalsIgnoreCase(format)) {
			response = gson.toJson(postedDhcpLease);
		}
		else { // default if ("json".equalsIgnoreCase(format)){
			response = postedDhcpLease.toJson();
		}
		return Response
					.status(Response.Status.OK)
					.entity(response)
					.type(MediaType.APPLICATION_JSON)
					.build();
	}
    
}
