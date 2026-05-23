cat << 'INNER_EOF' > /tmp/handler_diff
<<<<<<< SEARCH
                        } else if (opt82 instanceof com.jagornet.dhcp.core.option.DhcpUnknownOption) {
                            if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getHex() != null) {
                                relayData = ((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getHex();
                            } else if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getAscii() != null) {
                                relayData = ((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getAscii().getBytes();
                            }
                        }
=======
                        } else if (opt82 instanceof com.jagornet.dhcp.core.option.DhcpUnknownOption) {
                            if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData() != null) {
                                if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getHex() != null) {
                                    relayData = ((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getHex();
                                } else if (((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getAscii() != null) {
                                    relayData = ((com.jagornet.dhcp.core.option.DhcpUnknownOption) opt82).getOpaqueData().getAscii().getBytes();
                                }
                            }
                        }
>>>>>>> REPLACE
INNER_EOF
