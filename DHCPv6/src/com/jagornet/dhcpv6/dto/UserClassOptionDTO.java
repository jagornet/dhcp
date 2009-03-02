/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file UserClassOptionDTO.java is part of DHCPv6.
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

/*
 *   This file UserClassOptionDTO.java is part of DHCPv6.
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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class UserClassOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public class UserClassOptionDTO extends BaseOptionDTO
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/** The user classes. */
    protected List<OpaqueDataDTO> userClasses;

    /**
     * Gets the user classes.
     * 
     * @return the user classes
     */
    public List<OpaqueDataDTO> getUserClasses()
    {
        return userClasses;
    }

    /**
     * Sets the user classes.
     * 
     * @param userClasses the new user classes
     */
    public void setUserClasses(List<OpaqueDataDTO> userClasses)
    {
        this.userClasses = userClasses;
    }

    /**
     * Adds the user class.
     * 
     * @param userClass the user class
     */
    public void addUserClass(OpaqueDataDTO userClass)
    {
        if (this.userClasses == null)
            this.userClasses = new ArrayList<OpaqueDataDTO>();
        userClasses.add(userClass);
    }
}
