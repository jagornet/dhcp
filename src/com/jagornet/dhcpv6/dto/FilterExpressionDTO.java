/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file FilterExpressionDTO.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

import com.jagornet.dhcpv6.dto.option.base.OptionExpressionDTO;

/**
 * The Class FilterExpressionDTO.
 * 
 * @author A. Gregory Rabil
 */
public class FilterExpressionDTO implements Serializable
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The option expression. */
	protected OptionExpressionDTO optionExpression;
	
	/** The custom expression. */
	protected String customExpression;
	
	/**
	 * Gets the option expression.
	 * 
	 * @return the option expression
	 */
	public OptionExpressionDTO getOptionExpression() {
		return optionExpression;
	}
	
	/**
	 * Sets the option expression.
	 * 
	 * @param optionExpression the new option expression
	 */
	public void setOptionExpression(OptionExpressionDTO optionExpression) {
		this.optionExpression = optionExpression;
	}
	
	/**
	 * Gets the custom expression.
	 * 
	 * @return the custom expression
	 */
	public String getCustomExpression() {
		return customExpression;
	}
	
	/**
	 * Sets the custom expression.
	 * 
	 * @param customExpression the new custom expression
	 */
	public void setCustomExpression(String customExpression) {
		this.customExpression = customExpression;
	}
}
