cat << 'INNER_EOF' > /tmp/handler_diff
<<<<<<< SEARCH
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
=======
                    DhcpOption opt82 = dhcpMessage.getDhcpOption(DhcpConstants.V4OPTION_RELAY_INFO);
                    if (opt82 != null && opt82 instanceof BaseOpaqueDataOption) {
                        byte[] relayData = null;
                        if (((BaseOpaqueDataOption) opt82).getOpaqueData().getHex() != null) {
                            relayData = ((BaseOpaqueDataOption) opt82).getOpaqueData().getHex();
                        } else if (((BaseOpaqueDataOption) opt82).getOpaqueData().getAscii() != null) {
                            relayData = ((BaseOpaqueDataOption) opt82).getOpaqueData().getAscii().getBytes();
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
>>>>>>> REPLACE
INNER_EOF
