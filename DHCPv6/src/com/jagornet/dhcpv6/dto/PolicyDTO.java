package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

public class PolicyDTO implements Serializable
{
	protected String name;
	protected String value;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
