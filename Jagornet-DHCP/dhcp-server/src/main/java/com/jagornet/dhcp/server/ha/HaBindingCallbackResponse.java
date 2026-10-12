/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.ha;

import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.request.binding.Binding;

public class HaBindingCallbackResponse implements InvocationCallback<Response> {
	
	private static Logger log = LoggerFactory.getLogger(HaBindingCallbackResponse.class);
	
    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
	private Binding binding;
	
	public HaBindingCallbackResponse(Binding binding) {
		this.binding = binding;
	}

	@Override
	public void completed(Response response) {
		log.info("Binding update completed for: " + binding +
				 " Response=" + response);
		
	}

	@Override
	public void failed(Throwable throwable) {
		log.error("Binding update failed for: " + binding + ": " + throwable);
		
	}
}
