package com.jagornet.dhcp.server.rest.api;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	private static Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
	
    public static final String REALM = "jagornet";
    //TODO: Make client get token first via /authenticate API
    //		endpoint, then use that in the Bearer token
    // private static final String AUTHENTICATION_SCHEME = "Bearer";
    // NOTE: this is now public, and referenced by AuthFilterSecurityContext 
    public static final String AUTHENTICATION_SCHEME = "Basic";
    
    public static enum AuthRole { REST_API, HA_PEER };
    public static Map<String, String> userMap = new HashMap<String, String>();
    static {
    	Map<String, String> map = new HashMap<String, String>();
    	map.put(DhcpServerPolicies.globalPolicy(Property.REST_API_USERNAME),
    			DhcpServerPolicies.globalPolicy(Property.REST_API_PASSWORD));
    	map.put(DhcpServerPolicies.globalPolicy(Property.HA_PEER_USERNAME),
    			DhcpServerPolicies.globalPolicy(Property.HA_PEER_PASSWORD));
    	userMap = Collections.unmodifiableMap(map);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader
                            .substring(AUTHENTICATION_SCHEME.length()).trim();
        try {

            // Validate the token
        	// TODO: use a JWT token as described in the answer here:
        	// https://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey
            //validateToken(token);
        	
        	String username = validateUser(token);
        	
    		SecurityContext securityContext = requestContext.getSecurityContext();
        	if (securityContext != null) {
        		// base properties
//        		log.debug("securityContext.class=" + securityContext.getClass());
//    			log.debug("securityContext.isSecure=" + securityContext.isSecure());
//    			log.debug("securityContext.authenticationScheme=" +
//    						securityContext.getAuthenticationScheme());
//        		log.debug("securityContext.userPrincipal=" +
//    						securityContext.getUserPrincipal());
        		// extended wrapper class
        		AuthFilterSecurityContext authSecurityContext = 
        				new AuthFilterSecurityContext(
        						requestContext.getSecurityContext(), username);
        		log.debug("authSecurityContext.remoteAddress=" +
						  	authSecurityContext.getRemoteAddress());
            	requestContext.setSecurityContext(authSecurityContext);
        	}            
        }
        catch (Exception ex) {
        	log.error("Exception caught in filter", ex);
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {

        // Check if the Authorization header is valid
        // It must not be null and must be prefixed with "Bearer" plus a whitespace
        // The authentication scheme comparison must be case-insensitive
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                    .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {

        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, 
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .build());
    }

    private void validateToken(String token) throws Exception {
        // Check if the token was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
    	
    	byte[] decodedBytes = Base64.getDecoder().decode(token);
    	String decodedString = new String(decodedBytes);
    	
    	log.debug("token=" + decodedString);
    }

    private String validateUser(String token) throws Exception {
        // Check if the token was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
    	
    	byte[] decodedBytes = Base64.getDecoder().decode(token);
    	String decodedString = new String(decodedBytes);
    	
    	//TODO: suppress password in log
    	log.debug("authToken=" + decodedString);
    	if (decodedString != null) {
    		int i = decodedString.indexOf(':');
    		if ((i > 0) && ((i+1) < decodedString.length())) {
    			String username = decodedString.substring(0, i); 
    			String password = decodedString.substring(i+1);
    			if (userMap.containsKey(username)) {
    				if (password.equals(userMap.get(username))) {
    					return username;
    				}
    			}
    		}
    	}
    	return null;
    }
}
