package com.jagornet.dhcp.server.rest.api;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
