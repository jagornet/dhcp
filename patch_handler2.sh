cat << 'INNER_EOF' > /tmp/handler_diff
<<<<<<< SEARCH
		if (dhcpMessage.getGiAddr().equals(DhcpConstants.ZEROADDR_V4)) {
			linkAddress = localAddress;
				log.info("Handling client request on local client link address: " +
						linkAddress.getHostAddress());
		}
		else {
			linkAddress = dhcpMessage.getGiAddr();
				log.info("Handling client request on remote client link address: " +
						linkAddress.getHostAddress());
		}
=======
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
                    if (opt82 != null && opt82 instanceof BaseOpaqueDataOption) {
                        DhcpOption linkSelection = ((BaseOpaqueDataOption) opt82).getDhcpOption(5);
                        if (linkSelection != null && linkSelection instanceof BaseOpaqueDataOption) {
                            byte[] ipBytes = ((BaseOpaqueDataOption) linkSelection).getOpaqueData().getOpaque();
                            if (ipBytes != null && ipBytes.length == 4) {
                                try {
                                    linkAddress = InetAddress.getByAddress(ipBytes);
                                    log.info("Handling client request using Link Selection Sub-option address: " +
                                            linkAddress.getHostAddress());
                                } catch (UnknownHostException e) {
                                    log.error("Invalid IP address in Link Selection Sub-option: " + e.getMessage());
                                }
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
			}
			else {
				linkAddress = dhcpMessage.getGiAddr();
				log.info("Handling client request on remote client link address: " +
						linkAddress.getHostAddress());
			}
		}
>>>>>>> REPLACE
INNER_EOF
