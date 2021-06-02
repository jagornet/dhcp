package com.jagornet.dhcp.server.db;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagornet.dhcp.server.rest.api.JacksonObjectMapper;

public class DhcpLeaseJsonUtil {
	
	private static Logger log = LoggerFactory.getLogger(DhcpLeaseJsonUtil.class);
	
	public static JacksonObjectMapper jsonMapper = null;
	
	public static ObjectMapper getMapper() {
		if (jsonMapper == null) {
			jsonMapper = new JacksonObjectMapper();
		}
		return jsonMapper.getJsonObjectMapper();
	}

	public static String dhcpLeaseToJson(DhcpLease dhcpLease) {
		try {
			return getMapper().writeValueAsString(dhcpLease);
		} catch (Exception e) {
			log.error("Failed to convert DhcpLease to JSON string", e);
			return null;
		}
	}
	
	public static DhcpLease jsonToDhcpLease(String json) {
		try {
			return getMapper().readValue(json, DhcpLease.class);
		} catch (Exception e) {
			log.error("Failed to convert JSON string to DhcpLease", e);
			return null;	
		}
	}
	
	public static DhcpLease jsonToDhcpLease(InputStream json) {
		try {
			return getMapper().readValue(json, DhcpLease.class);
		} catch (Exception e) {
			log.error("Failed to convert JSON input stream to DhcpLease", e);
			return null;
		}
	}
}
