package com.jagornet.dhcp.server.rest.api;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<Exception> {

    public Response toResponse(Exception e) {    	
    	
        return Response
		        .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
		        .type(MediaType.TEXT_PLAIN)
		        .entity("Catching in ApplicationExceptionMapper : " + e)
		        .build();
    }
}
