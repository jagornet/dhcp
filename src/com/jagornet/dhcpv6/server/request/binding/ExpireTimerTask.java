/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ExpireTimerTask.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaAddress;

// TODO: Auto-generated Javadoc
/**
 * The Class ExpireTimerTask.
 */
public class ExpireTimerTask extends TimerTask
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(ExpireTimerTask.class);

	/** The binding manager. */
	private static BindingManager bindingManager = BindingManager.getInstance();
	
	/** The ia addr. */
	private IaAddress iaAddr;
	
	/**
	 * Instantiates a new expire timer task.
	 * 
	 * @param iaAddr the ia addr
	 */
	public ExpireTimerTask(IaAddress iaAddr)
	{
		this.iaAddr = iaAddr;
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		log.info("Expiring binding for address: " + 
				iaAddr.getIpAddress().getHostAddress());
		bindingManager.expireIaAddress(iaAddr);
	}

}
