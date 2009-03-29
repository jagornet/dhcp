/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file FiltersDTO.java is part of DHCPv6.
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
import java.util.List;

/**
 * The Class FiltersDTO.
 * 
 * @author A. Gregory Rabil
 */
public class FiltersDTO implements Serializable
{	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/** The filter list. */
	protected List<FilterDTO> filterList;

	/**
	 * Gets the filter list.
	 * 
	 * @return the filter list
	 */
	public List<FilterDTO> getFilterList()
	{
		return filterList;
	}

	/**
	 * Gets the filter array.
	 * This method is here to satisfy Dozer mapping with
	 * code, instead of via the dozermap.xml file.
	 * 
	 * @return the filter array
	 */
	public FilterDTO[] getFilterArray()
	{
		if (filterList != null)
			return filterList.toArray(new FilterDTO[0]);
		
		return null;
	}
	
	/**
	 * Sets the filter list.
	 * 
	 * @param filterList the new filter list
	 */
	public void setFilterList(List<FilterDTO> filterList)
	{
		this.filterList = filterList;
	}
	
}
