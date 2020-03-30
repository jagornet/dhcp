package com.jagornet.dhcp.server.rest.api;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.netty.httpserver.NettySecurityContext;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.rest.api.AuthenticationFilter.AuthRole;

public class AuthFilterSecurityContext implements SecurityContext {

	private SecurityContext origSecurityContext;
	private String username;
	
	public AuthFilterSecurityContext(SecurityContext origSecurityContext, String username) {
		this.origSecurityContext = origSecurityContext;
		this.username = username;
	}
	
    @Override
    public Principal getUserPrincipal() {
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String role) {
		AuthRole authRole = AuthenticationFilter.AuthRole.valueOf(role);
		if (authRole != null) {
			if (authRole.equals(AuthRole.REST_API)) {
				if (username.equals(DhcpServerPolicies.globalPolicy(Property.REST_API_USERNAME))) {
					return true;
				}
			}
			else if (authRole.equals(AuthRole.HA_PEER)) {
				if (username.equals(DhcpServerPolicies.globalPolicy(Property.HA_PEER_USERNAME))) {
					return true;
				}
			}
			return false;
		}
		// no role to check
		return true;
    }

    @Override
    public boolean isSecure() {
        // Netty is hard-coded to return false
    	// return origSecurityContext.isSecure();
    	return true;	// only https is supported, so must be secure
    }

    @Override
    public String getAuthenticationScheme() {
        return AuthenticationFilter.AUTHENTICATION_SCHEME;
    }
    
    public NettySecurityContext getNettySecurityContext() {
		if (origSecurityContext instanceof NettySecurityContext) {
			return (NettySecurityContext)origSecurityContext;
		}
		return null;
    }

}
