cat << 'INNER_EOF' > /tmp/handler_diff
<<<<<<< SEARCH
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
=======
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
>>>>>>> REPLACE
INNER_EOF
