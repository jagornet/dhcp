package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

public class FilterExpressionDTO implements Serializable
{
	protected OptionExpressionDTO optionExpression;
	protected String customExpression;
	
	public OptionExpressionDTO getOptionExpression() {
		return optionExpression;
	}
	public void setOptionExpression(OptionExpressionDTO optionExpression) {
		this.optionExpression = optionExpression;
	}
	public String getCustomExpression() {
		return customExpression;
	}
	public void setCustomExpression(String customExpression) {
		this.customExpression = customExpression;
	}
}
