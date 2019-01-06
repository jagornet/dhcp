/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DdnsUpdater.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.failover;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.failover.FailoverStateManager.State;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.ddns.ForwardDdnsUpdate;
import com.jagornet.dhcp.server.request.ddns.ReverseDdnsUpdate;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramChannel;

/**
 * The Class DdnsUpdater.
 * 
 * @author A. Gregory Rabil
 */
public class FailoverBindingUpdater implements Runnable
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(FailoverBindingUpdater.class);

	/** The executor. */
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	/** The sync. */
	private boolean sync;
	
	/** The binding. */
	private Binding binding;
	
	/** The is delete. */
	private boolean isDelete;
	
	private FailoverStateManager fsm;

	private int timeout;
	private int failLimit;
	private int bndAckTimeoutCnt = 0;

	private FailoverBindingCallback callback;
	public FailoverBindingUpdater(Binding binding, boolean isDelete,
			FailoverBindingCallback callback)
	{
		this.binding = binding;
		this.isDelete = isDelete;
		this.callback = callback;
		fsm = DhcpServerConfiguration.getInstance().getFailoverStateManager();
		//TODO: separate policies
		timeout = DhcpServerPolicies.globalPolicyAsInt(
				Property.FAILOVER_POLL_REPLY_TIMEOUT);			
		failLimit = DhcpServerPolicies.globalPolicyAsInt(
				Property.FAILOVER_POLL_REPLY_FAILURE_COUNT);
	}
	
	/**
	 * Process updates.
	 */
	public void processUpdates()
	{
		if (sync) {
			run();
		}
		else {
			executor.submit(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		if (fsm != null) {
			DatagramChannel channel = fsm.getChannel();
			FailoverMessage bndMessage = 
					new FailoverMessage(channel.localAddress(), fsm.getPeerAddress());
			//TODO: handle update
			if (isDelete) {
				bndMessage.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDDEL);
			}
			else {
				bndMessage.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDADD);
			}
			bndMessage.setBinding(binding);
			try {
				ChannelFuture future = channel.writeAndFlush(bndMessage);
				future.await();
				if (future.isSuccess()) {
					waitForBndAck();
				}
				else {
					log.error("Failed to send binding update message");
				}
			}
			catch (Exception ex) {
				log.error("Exception running binding update task: " + ex);
			}
			
		}
	}
	
	private synchronized void waitForBndAck() throws InterruptedException {
		fsm.setState(State.PRIMARY_AWAITING_BNDACK);
		log.info("Waiting " + timeout + " seconds for binding ack...");
		this.wait(timeout*1000);	// milliseconds
		// wait expired, increment failed counter
		bndAckTimeoutCnt++;
		log.warn("Binding ack timeout exceeded " + bndAckTimeoutCnt + " times");
		if (bndAckTimeoutCnt >= failLimit) {
			log.warn("Binding ack failure count limit reached: " + failLimit);
			fsm.setState(State.PRIMARY_RUNNING);
		}
	}
	
}
