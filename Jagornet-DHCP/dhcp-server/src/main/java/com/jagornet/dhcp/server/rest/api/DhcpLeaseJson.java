package com.jagornet.dhcp.server.rest.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.server.db.DhcpLease;

/**
 * Container/wrapper class for auto-marshalling of DhcpLease to/from JSON
 * via JAX-RS entity POJO handling, which does not support InetAddress,
 * so this class is needed to deal with the IP as a string
 * 
 * @author agrabil
 *
 */
public class DhcpLeaseJson {

	DhcpLease dhcpLease;
	
	// this empty constructor is needed for building
	// DhcpLease from JSON
	public DhcpLeaseJson() {
		dhcpLease = new DhcpLease();
	}
	public DhcpLeaseJson(DhcpLease dhcpLease) {
		this.dhcpLease = dhcpLease;
	}

	// DO NOT want getDhcpLease, because then the
	// auto-marshalling will call that method
	public DhcpLease toDhcpLease() {
		return dhcpLease;
	}
	
	public void setDhcpLease(DhcpLease dhcpLease) {
		this.dhcpLease = dhcpLease;
	}
	
	public String getIpAddress() {
		return dhcpLease.getIpAddress().getHostAddress();
	}
 	public void setIpAddress(String ipAddress) throws UnknownHostException {
		dhcpLease.setIpAddress(InetAddress.getByName(ipAddress));
	}

 	public byte[] getDuid() {
		return dhcpLease.getDuid();
	}
	public void setDuid(byte[] duid) {
		dhcpLease.setDuid(duid);
	}

	public byte getIatype() {
		return dhcpLease.getIatype();
	}
	public void setIatype(byte iatype) {
		dhcpLease.setIatype(iatype);
	}

	public long getIaid() {
		return dhcpLease.getIaid();
	}
	public void setIaid(long iaid) {
		dhcpLease.setIaid(iaid);
	}

	public short getPrefixLength() {
		return dhcpLease.getPrefixLength();
	}
	public void setPrefixLength(short prefixLength) {
		dhcpLease.setPrefixLength(prefixLength);
	}

	public byte getState() {
		return dhcpLease.getState();
	}
	public void setState(byte state) {
		dhcpLease.setState(state);
	}

	public byte getHaPeerState() {
		return dhcpLease.getHaPeerState();
	}
	public void setHaPeerState(byte haPeerState) {
		dhcpLease.setHaPeerState(haPeerState);
	}

	public Date getStartTime() {
		return dhcpLease.getStartTime();
	}
	public void setStartTime(Date startTime) {
		dhcpLease.setStartTime(startTime);
	}

	public Date getPreferredEndTime() {
		return dhcpLease.getPreferredEndTime();
	}
	public void setPreferredEndTime(Date preferredEndTime) {
		dhcpLease.setPreferredEndTime(preferredEndTime);
	}

	public Date getValidEndTime() {
		return dhcpLease.getValidEndTime();
	}
	public void setValidEndTime(Date validEndTime) {
		dhcpLease.setValidEndTime(validEndTime);
	}

	public Collection<DhcpOption> getIaDhcpOptions() {
		return dhcpLease.getIaDhcpOptions();
	}
	public void setIaDhcpOptions(Collection<DhcpOption> iaDhcpOptions) {
		dhcpLease.setIaDhcpOptions(iaDhcpOptions);
	}

	public Collection<DhcpOption> getIaAddrDhcpOptions() {
		return dhcpLease.getIaAddrDhcpOptions();
	}
	public void setIaAddrDhcpOptions(Collection<DhcpOption> iaAddrDhcpOptions) {
		dhcpLease.setIaAddrDhcpOptions(iaAddrDhcpOptions);
	}
}
