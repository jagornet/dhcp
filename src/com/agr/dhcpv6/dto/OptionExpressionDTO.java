package com.agr.dhcpv6.dto;

public class OptionExpressionDTO extends OpaqueDataOptionDTO
{
	protected String operator;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}
