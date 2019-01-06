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

@Path("dhcpleases")
public class DhcpLeasesResource {
	
	private DhcpLeasesService service;
	
	public DhcpLeasesResource() {
		service = new DhcpLeasesService();
	}
	
//	@GET
//    @Produces("application/json")
//    public List<DhcpLease> getDhcpLeases() {
//		return getDhcpLeaseIps();
//	}
 
    @GET @Path("/ips")
    @Produces(MediaType.APPLICATION_JSON)
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
    
    @GET @Path("/ips/{ipaddress}")
    @Produces(MediaType.APPLICATION_JSON)
//    public DhcpLease getDhcpLease(@PathParam("ipaddress") String ipAddress) 
//    		throws UnknownHostException {
//		return service.getDhcpLease(InetAddress.getByName(ipAddress));
//    }
    public String getDhcpLease(@PathParam("ipaddress") String ipAddress) 
    		throws UnknownHostException {
    	String leaseJson = null;
		DhcpLease dhcpLease = service.getDhcpLease(InetAddress.getByName(ipAddress));
		if (dhcpLease != null) {
			leaseJson = dhcpLease.toJson();
		}
		return leaseJson;
    }

    @POST @Path("/ips")
    @Consumes(MediaType.APPLICATION_JSON)
    public void postDhcpLease(DhcpLease dhcpLease) { 
    	service.createDhcpLease(dhcpLease);
    }

    @PUT @Path("/ips/{ipaddress}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void putDhcpLeaseBy(@PathParam("ipaddress") String ipAddress, 
    									DhcpLease dhcpLease) throws UnknownHostException { 
    	service.updateDhcpLease(InetAddress.getByName(ipAddress), dhcpLease);
    }
 
    @DELETE @Path("/ips/{ipaddress}")
    public void deleteDhcpLeaseBy(@PathParam("ipaddress") String ipAddress)
    		throws UnknownHostException { 
    	service.deleteDhcpLease(InetAddress.getByName(ipAddress));
    }
}
