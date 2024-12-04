package com.jagornet.dhcp.server.db;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.jagornet.dhcp.server.grpc.DhcpLeaseUpdate;
import com.jagornet.dhcp.server.grpc.DhcpOption;
import com.jagornet.dhcp.server.rest.api.JacksonObjectMapper;
// import com.jagornet.dhcp.grpc;

public class DhcpLeaseUtil {
	
	private static Logger log = LoggerFactory.getLogger(DhcpLeaseUtil.class);
	
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
		} 
		catch (Exception e) {
			log.error("Failed to convert DhcpLease to JSON string", e);
			return null;
		}
	}
	
	public static DhcpLease jsonToDhcpLease(String json) {
		try {
			return getMapper().readValue(json, DhcpLease.class);
		} 
		catch (Exception e) {
			log.error("Failed to convert JSON string to DhcpLease", e);
			return null;	
		}
	}
	
	public static DhcpLease jsonToDhcpLease(InputStream json) {
		try {
			return getMapper().readValue(json, DhcpLease.class);
		} 
		catch (Exception e) {
			log.error("Failed to convert JSON input stream to DhcpLease", e);
			return null;
		}
	}

	public static DhcpLeaseUpdate dhcpLeaseToGrpc(DhcpLease dhcpLease) {
		try {
			DhcpLeaseUpdate.Builder leaseBuilder = 
				DhcpLeaseUpdate.newBuilder()
					.setIpAddress(ByteString.copyFrom(dhcpLease.getIpAddress().getAddress()))
					.setDuid(ByteString.copyFrom(dhcpLease.getDuid()))
					.setIatype(ByteString.copyFrom(new byte[] { dhcpLease.getIatype() }))
					.setIaid(Long.valueOf(dhcpLease.getIaid()).intValue())
					.setPrefixLength(dhcpLease.prefixLength)
					.setState(ByteString.copyFrom(new byte[] { dhcpLease.getState() }))
					.setHaPeerState(ByteString.copyFrom(new byte[] { dhcpLease.getHaPeerState() }))
					.setStartTime(dhcpLease.getStartTime().getTime())
					.setPreferredEndTime(dhcpLease.getPreferredEndTime().getTime())
					.setValidEndTime(dhcpLease.getValidEndTime().getTime());
			
			if (dhcpLease.getDhcpOptions() != null) {
				for (com.jagornet.dhcp.core.option.base.DhcpOption option : dhcpLease.getDhcpOptions()) {
					leaseBuilder.addDhcpOptions(dhcpOptionToGrpc(option));
				}
			}
			
			if (dhcpLease.getIaDhcpOptions() != null) {
				for (com.jagornet.dhcp.core.option.base.DhcpOption option : dhcpLease.getIaDhcpOptions()) {
					leaseBuilder.addIaDhcpOptions(dhcpOptionToGrpc(option));
				}
			}
			
			if (dhcpLease.getIaAddrDhcpOptions() != null) {
				for (com.jagornet.dhcp.core.option.base.DhcpOption option : dhcpLease.getIaAddrDhcpOptions()) {
					leaseBuilder.addIaAddrDhcpOptions(dhcpOptionToGrpc(option));
				}
			}

			return leaseBuilder.build();
		} 
		catch (Exception e) {
			log.error("Failed to convert DhcpLease to gRPC DhcpLeaseUpdate", e);
			return null;
		}
	}

	public static DhcpOption dhcpOptionToGrpc(com.jagornet.dhcp.core.option.base.DhcpOption option) {
		try {
			DhcpOption.Builder optionBuilder =
						DhcpOption.newBuilder()
							.setCode(option.getCode())
							.setName(option.getName())
							.setLen(option.getLength())
							.setData(ByteString.copyFrom(option.getRawData()))
							.setV4(option.isV4());

			return optionBuilder.build();
		} 
		catch (Exception e) {
			log.error("Failed to convert base DhcpOption to gRPC DhcpOption", e);
			return null;
		}
}

	public static DhcpLease grpcToDhcpLease(DhcpLeaseUpdate leaseUpdate) {
		try {
			DhcpLease dhcpLease = new DhcpLease();
			dhcpLease.setIpAddress(InetAddress.getByAddress(leaseUpdate.getIpAddress().toByteArray()));
			dhcpLease.setDuid(leaseUpdate.getDuid().toByteArray());
			dhcpLease.setIatype(leaseUpdate.getIatype().byteAt(0));
			dhcpLease.setIaid(leaseUpdate.getIaid());
			dhcpLease.setPrefixLength((short)leaseUpdate.getPrefixLength());
			dhcpLease.setState(leaseUpdate.getState().byteAt(0));
			dhcpLease.setHaPeerState(leaseUpdate.getHaPeerState().byteAt(0));
			dhcpLease.setStartTime(new Date(leaseUpdate.getStartTime()));
			dhcpLease.setPreferredEndTime(new Date(leaseUpdate.getPreferredEndTime()));
			dhcpLease.setValidEndTime(new Date(leaseUpdate.getValidEndTime()));
			return dhcpLease;
		} 
		catch (Exception e) {
			log.error("Failed to convert gRPC DhcpLeaseUpdate to DhcpLease", e);
			return null;
		}
	}

	public static com.jagornet.dhcp.core.option.base.DhcpOption grpcToDhcpOption(DhcpOption option) {
		try {
			com.jagornet.dhcp.core.option.base.DhcpOption dhcpOption = 
				new com.jagornet.dhcp.core.option.generic.GenericOpaqueDataOption(option.getCode(), option.getName());
			dhcpOption.setRawData(option.getData().asReadOnlyByteBuffer().array());
			dhcpOption.setV4(option.getV4());
			return dhcpOption;
		} 
		catch (Exception e) {
			log.error("Failed to convert gRPC DhcpOption to generic opaque data DhcpOption", e);
			return null;
		}
	}

}
