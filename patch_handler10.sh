cat << 'INNER_EOF' > /tmp/handler_diff
<<<<<<< SEARCH
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
                        byte[] relayData = null;
                        if (opt82 instanceof BaseOpaqueDataOption) {
                            if (((BaseOpaqueDataOption) opt82).getOpaqueData().getHex() != null) {
                                relayData = ((BaseOpaqueDataOption) opt82).getOpaqueData().getHex();
                            } else if (((BaseOpaqueDataOption) opt82).getOpaqueData().getAscii() != null) {
                                relayData = ((BaseOpaqueDataOption) opt82).getOpaqueData().getAscii().getBytes();
                            }
                        }
                        if (relayData != null) {
                            int offset = 0;
                            while (offset < relayData.length) {
                                int subOptCode = relayData[offset++] & 0xFF;
                                if (offset >= relayData.length) break;
                                int subOptLen = relayData[offset++] & 0xFF;
                                if (offset + subOptLen > relayData.length) break;

                                if (subOptCode == 5 && subOptLen == 4) { // Link Selection Sub-option
                                    byte[] ipBytes = new byte[4];
                                    System.arraycopy(relayData, offset, ipBytes, 0, 4);
                                    try {
                                        linkAddress = InetAddress.getByAddress(ipBytes);
                                        log.info("Handling client request using Link Selection Sub-option address: " +
                                                linkAddress.getHostAddress());
                                    } catch (UnknownHostException e) {
                                        log.error("Invalid IP address in Link Selection Sub-option: " + e.getMessage());
                                    }
                                    break;
                                }
                                offset += subOptLen;
                            }
                        }
                    }
		    }
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
                    if (opt82 != null) {
                        byte[] relayData = null;
                        if (opt82 instanceof BaseOpaqueDataOption) {
                            if (((BaseOpaqueDataOption) opt82).getOpaqueData().getHex() != null) {
                                relayData = ((BaseOpaqueDataOption) opt82).getOpaqueData().getHex();
                            } else if (((BaseOpaqueDataOption) opt82).getOpaqueData().getAscii() != null) {
                                relayData = ((BaseOpaqueDataOption) opt82).getOpaqueData().getAscii().getBytes();
                            }
                        } else if (opt82 instanceof com.jagornet.dhcp.core.option.DhcpUnknownOption) {
                            if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getHex() != null) {
                                relayData = ((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getHex();
                            } else if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getAscii() != null) {
                                relayData = ((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getAscii().getBytes();
                            }
                        }
                        if (relayData != null) {
                            int offset = 0;
                            while (offset < relayData.length) {
                                int subOptCode = relayData[offset++] & 0xFF;
                                if (offset >= relayData.length) break;
                                int subOptLen = relayData[offset++] & 0xFF;
                                if (offset + subOptLen > relayData.length) break;

                                if (subOptCode == 5 && subOptLen == 4) { // Link Selection Sub-option
                                    byte[] ipBytes = new byte[4];
                                    System.arraycopy(relayData, offset, ipBytes, 0, 4);
                                    try {
                                        linkAddress = InetAddress.getByAddress(ipBytes);
                                        log.info("Handling client request using Link Selection Sub-option address: " +
                                                linkAddress.getHostAddress());
                                    } catch (UnknownHostException e) {
                                        log.error("Invalid IP address in Link Selection Sub-option: " + e.getMessage());
                                    }
                                    break;
                                }
                                offset += subOptLen;
                            }
                        }
                    }
		    }
		}
>>>>>>> REPLACE
INNER_EOF
patch -p1 -d Jagornet-DHCP < /tmp/handler_diff
