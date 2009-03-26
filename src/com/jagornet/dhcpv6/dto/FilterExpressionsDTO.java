/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file FilterExpressionsDTO.java is part of DHCPv6.
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

import java.util.List;

/**
 * The Class FilterExpressionsDTO.
 * 
 * @author A. Gregory Rabil
 */
public class FilterExpressionsDTO
{	
	/** The filter expression list. */
	protected List<FilterExpressionDTO> filterExpressionList;

	/**
	 * Gets the filter expression list.
	 * 
	 * @return the filter expression list
	 */
	public List<FilterExpressionDTO> getFilterExpressionList() 
	{
		return filterExpressionList;
	}

	/**
	 * Gets the filter expression array.
	 * This method is here to satisfy Dozer mapping with
	 * code, instead of via the dozermap.xml file.
	 * 
	 * @return the filter expression array
	 */
	public FilterExpressionDTO[] getFilterExpressionArray()
	{
		if (filterExpressionList != null)
			return filterExpressionList.toArray(new FilterExpressionDTO[0]);
		
		return null;
	}
	
	/**
	 * Sets the filter expression list.
	 * 
	 * @param filterExpressionList the new filter expression list
	 */
	public void setFilterExpressionList(List<FilterExpressionDTO> filterExpressionList) 
	{
		this.filterExpressionList = filterExpressionList;
	}
	
}
