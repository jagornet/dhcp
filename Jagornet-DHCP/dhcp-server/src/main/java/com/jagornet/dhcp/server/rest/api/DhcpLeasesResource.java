package com.jagornet.dhcp.server.rest.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.jagornet.dhcp.server.db.DhcpLease;

@Path(DhcpLeasesResource.PATH)
public class DhcpLeasesResource {
	
	public static final String PATH = "dhcpleases";
	public static final String PATH_IPS = PATH + "/ips";
	public static final String IPADDRESS = "/{ipaddress}";
	public static final String PATH_IPS_IPADDRESS = PATH_IPS + IPADDRESS;
	
	private DhcpLeasesService service;
	
	public DhcpLeasesResource() {
		service = new DhcpLeasesService();
	}
	
//	@GET
//    @Produces("application/json")
//    public List<DhcpLease> getDhcpLeases() {
//		return getDhcpLeaseIps();
//	}
 
    @GET 
//    @Path(PATH_IPS)
    @Produces(MediaType.TEXT_PLAIN)
//    public List<InetAddress> getDhcpLeaseIps() { 
//    	return service.getAllLeaseIPs();
//    }
    public String getDhcpLeaseIps() { 
    	StringBuilder sb = new StringBuilder();
    	List<InetAddress> ips = service.getAllLeaseIPs();
    	if ((ips != null) && !ips.isEmpty()) {
    		for (InetAddress ip : ips) {
				sb.append(ip.getHostAddress()).append(System.lineSeparator());
			}
    	}
    	return sb.toString();
    }
    
    @GET 
//    @Secured
//  @Path(PATH_IPS_IPADDRESS)
    @Path(IPADDRESS)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DhcpLease getDhcpLease(@PathParam("ipaddress") String ipAddress) 
//    		throws UnknownHostException {
//		return service.getDhcpLease(InetAddress.getByName(ipAddress));
//    }
    @Produces(MediaType.TEXT_PLAIN)
    public String getDhcpLease(@PathParam("ipaddress") String ipAddress) 
    		throws UnknownHostException {
    	String leaseJson = null;
		DhcpLease dhcpLease = service.getDhcpLease(InetAddress.getByName(ipAddress));
		if (dhcpLease != null) {
			leaseJson = dhcpLease.toJson();
		}
		return leaseJson;
    }

    @POST 
//    @Secured
//    @Path(PATH_IPS)
//    @Consumes(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String postDhcpLease(String dhcpLeaseData) {
    	DhcpLease dhcpLease = DhcpLease.fromJson(dhcpLeaseData);
    	InetAddress ipAddress = dhcpLease.getIpAddress();
		DhcpLease existingDhcpLease = service.getDhcpLease(ipAddress);
		if (existingDhcpLease != null) {
			service.updateDhcpLease(ipAddress, dhcpLease);
			return "UPDATED: " + dhcpLease;
		}
		else {
	    	DhcpLease newDhcpLease = service.createDhcpLease(dhcpLease);
	    	if (newDhcpLease != null) {
	    		return "CREATED: " + newDhcpLease.toString();
	    	}
	    	else {
	    		return "HTTP POST DhcpLease failed!";
	    	}
		}
    }

    @PUT
//    @Secured
//    @Path(PATH_IPS_IPADDRESS)
    @Path(IPADDRESS)
    @Consumes(MediaType.APPLICATION_JSON)
    public void putDhcpLeaseBy(@PathParam("ipaddress") String ipAddress, 
    									DhcpLease dhcpLease) throws UnknownHostException { 
    	service.updateDhcpLease(InetAddress.getByName(ipAddress), dhcpLease);
    }
 
    @DELETE
//    @Secured
//    @Path(PATH_IPS_IPADDRESS)
    @Path(IPADDRESS)
    public void deleteDhcpLeaseBy(@PathParam("ipaddress") String ipAddress)
    		throws UnknownHostException { 
    	service.deleteDhcpLease(InetAddress.getByName(ipAddress));
    }
}
