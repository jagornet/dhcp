/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.core.option.v4.DhcpV4RelayAgentInfoOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4SubnetSelectionOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.option.DhcpUnknownOption;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import java.net.UnknownHostException;

/**
 * Title: DhcpV4MessageHandler
 * Description: The main DHCPv4 message handler class.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4MessageHandler {
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4MessageHandler.class);

	private DhcpV4MessageHandler() {
		// prevent instantiation
	}

	public static DhcpV4Message handleMessage(InetAddress localAddress, DhcpV4Message dhcpMessage) {
		long startTime = System.nanoTime();
		DhcpV4Message replyMessage = null;
		if (dhcpMessage.getOp() == DhcpConstants.V4_OP_REQUEST) {
			InetAddress linkAddress = null;
			if (DhcpServerPolicies.globalPolicyAsBoolean(Property.V4_SUBNET_SELECTION)) {
				DhcpOption opt118 = dhcpMessage.getDhcpOption(DhcpConstants.V4OPTION_SUBNET_SELECTION);
				if (opt118 != null && opt118 instanceof DhcpV4SubnetSelectionOption) {
					try {
						linkAddress = InetAddress.getByName(((DhcpV4SubnetSelectionOption) opt118).getIpAddress());
						log.info("Handling client request using Subnet Selection Option address: " +
								linkAddress.getHostAddress());
					} catch (UnknownHostException e) {
						log.error("Invalid IP address in Subnet Selection Option: " + e.getMessage());
					}
				}
				if (linkAddress == null) {
					DhcpOption opt82 = dhcpMessage.getDhcpOption(DhcpConstants.V4OPTION_RELAY_INFO);
					if (opt82 != null) {
						DhcpV4RelayAgentInfoOption raiOpt = null;
						if (opt82 instanceof DhcpV4RelayAgentInfoOption) {
							raiOpt = (DhcpV4RelayAgentInfoOption) opt82;
						} else if (opt82 instanceof BaseOpaqueDataOption) {
							raiOpt = new DhcpV4RelayAgentInfoOption();
							raiOpt.setOpaqueData(((BaseOpaqueDataOption) opt82).getOpaqueData());
						}
						if (raiOpt != null) {
							linkAddress = raiOpt.getLinkSelectionAddress();
							if (linkAddress != null) {
								log.info("Handling client request using Link Selection Sub-option address: " +
										linkAddress.getHostAddress());
							}
						}
					}
				}
			}

			if (linkAddress == null) {
				if (dhcpMessage.getGiAddr().equals(DhcpConstants.ZEROADDR_V4)) {
					linkAddress = localAddress;
					log.info("Handling client request on local client link address: " +
							linkAddress.getHostAddress());
				} else {
					linkAddress = dhcpMessage.getGiAddr();
					log.info("Handling client request on remote client link address: " +
							linkAddress.getHostAddress());
				}
			}
			DhcpV4MsgTypeOption msgTypeOption = (DhcpV4MsgTypeOption) dhcpMessage
					.getDhcpOption(DhcpConstants.V4OPTION_MESSAGE_TYPE);
			if (msgTypeOption != null) {
				short msgType = msgTypeOption.getUnsignedByte();
				String msgTypeStr = DhcpConstants.getV4MessageString(msgType);
				DhcpV4MessageProcessor processor = null;
				switch (msgType) {
					case DhcpConstants.V4MESSAGE_TYPE_DISCOVER:
						processor = new DhcpV4DiscoverProcessor(dhcpMessage, linkAddress);
						break;
					case DhcpConstants.V4MESSAGE_TYPE_REQUEST:
						processor = new DhcpV4RequestProcessor(dhcpMessage, linkAddress);
						break;
					case DhcpConstants.V4MESSAGE_TYPE_DECLINE:
						processor = new DhcpV4DeclineProcessor(dhcpMessage, linkAddress);
						break;
					case DhcpConstants.V4MESSAGE_TYPE_RELEASE:
						processor = new DhcpV4ReleaseProcessor(dhcpMessage, linkAddress);
						break;
					case DhcpConstants.V4MESSAGE_TYPE_INFORM:
						processor = new DhcpV4InformProcessor(dhcpMessage, linkAddress);
						break;
					default:
						log.error("Unknown message type.");
						break;
				}
				if (processor != null) {
					replyMessage = processor.processMessage();
					String result = (replyMessage != null) ? "processed" : "no_reply";
					com.jagornet.dhcp.server.metrics.DhcpMetrics.getInstance()
							.incrementPacketCounter("v4", msgTypeStr, result);
					com.jagornet.dhcp.server.metrics.DhcpMetrics.getInstance()
							.recordPacketLatency("v4", msgTypeStr, System.nanoTime() - startTime);
					if (replyMessage != null) {
						String replyTypeStr = DhcpConstants.getV4MessageString(replyMessage.getMessageType());
						com.jagornet.dhcp.server.metrics.DhcpMetrics.getInstance()
								.incrementPacketCounter("v4", replyTypeStr, "sent");
					}
				} else {
					log.error("No processor found for message type: " + msgType);
					com.jagornet.dhcp.server.metrics.DhcpMetrics.getInstance()
							.incrementPacketCounter("v4", msgTypeStr, "unsupported");
				}
			} else {
				log.error("No message type option found in request.");
				com.jagornet.dhcp.server.metrics.DhcpMetrics.getInstance()
						.incrementPacketCounter("v4", "UNKNOWN", "missing_option");
			}
		} else {
			log.error("Unsupported op code: " + dhcpMessage.getOp());
			com.jagornet.dhcp.server.metrics.DhcpMetrics.getInstance()
					.incrementPacketCounter("v4", "UNKNOWN", "unsupported_opcode");
		}
		return replyMessage;
	}

}
