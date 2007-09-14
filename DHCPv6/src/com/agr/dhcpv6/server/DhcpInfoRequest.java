package com.agr.dhcpv6.server;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.option.DhcpClientIdOption;
import com.agr.dhcpv6.option.DhcpComparableOption;
import com.agr.dhcpv6.option.DhcpDnsServersOption;
import com.agr.dhcpv6.option.DhcpDomainListOption;
import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.option.DhcpPreferenceOption;
import com.agr.dhcpv6.option.DhcpServerIdOption;
import com.agr.dhcpv6.option.DhcpVendorInfoOption;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig;
import com.agr.dhcpv6.server.config.xml.DnsServersOption;
import com.agr.dhcpv6.server.config.xml.DomainListOption;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.PreferenceOption;
import com.agr.dhcpv6.server.config.xml.ServerIdOption;
import com.agr.dhcpv6.server.config.xml.VendorInfoOption;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig.FilterGroups;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * Title:        DhcpInfoRequest
 * Description:  A DhcpMessageHandler for INFO_REQUEST messages.  Basically,
 *               this class implements the processing of a stateless DHCPv6
 *               server.
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpInfoRequest implements DhcpMessageHandler
{
    private DhcpChannel dhcpChannel;
    private DhcpMessage requestMsg;
    private DhcpMessage replyMsg;
    private DhcpV6ServerConfig dhcpServerConfig;

    private static Log log = LogFactory.getLog(DhcpInfoRequest.class);

    /**
     * Construct an DhcpInfoRequest handler
     *
     * @param   reqMsg  must be an INFO_REQUEST type DhcpMessage
     */
    public DhcpInfoRequest(DhcpChannel channel, DhcpMessage reqMsg)
    {
        dhcpChannel = channel;
        requestMsg = reqMsg;
        dhcpServerConfig = DhcpServer.getDhcpServerConfig();
    }

    /**
     * Handle the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     */
    public void run()
    {
/**
 * FROM RFC 3315:
 * 
 * 15.12. Information-request Message

   Clients MUST discard any received Information-request messages.

   Servers MUST discard any received Information-request message that
   meets any of the following conditions:

   -  The message includes a Server Identifier option and the DUID in
      the option does not match the server's DUID.

   -  The message includes an IA option.
 */     
        
        log.info("Processing " + requestMsg.toString());

        ServerIdOption serverIdOption = dhcpServerConfig.getServerIdOption();
        if (serverIdOption == null) {
            log.error("Invalid configuration - ServerId option must not be null!");
            return;
        }

        DhcpServerIdOption dhcpServerIdOption =
            new DhcpServerIdOption(serverIdOption);
        
        DhcpClientIdOption clientIdOption = null;
        DhcpServerIdOption requestedServerIdOption = null;
        Collection<DhcpOption> options = requestMsg.getOptions();
        if (options != null) {
            for (DhcpOption option : options) {
                log.debug(option.toString());
                if (option instanceof DhcpClientIdOption) {
                    clientIdOption = (DhcpClientIdOption)option;
                }
                if (option instanceof DhcpServerIdOption) {
                    requestedServerIdOption = (DhcpServerIdOption)option;
                }
            }
        }
        
        // if the client provided a ServerID option, then it MUST
        // match our configured ServerID, otherwise ignore the request
        if ( (requestedServerIdOption != null) &&
             !dhcpServerIdOption.equals(requestedServerIdOption) ) {
            log.warn("Ingoring Info-Request message: " +
                     "Requested ServerID: " + requestedServerIdOption +
                     "My ServerID: " + dhcpServerIdOption);
            return;
        }
        
        // build a message destined to the host:port which sent the request
        replyMsg = new DhcpMessage(requestMsg.getHost(), requestMsg.getPort());
        // copy the transaction ID into the reply
        replyMsg.setTransactionId(requestMsg.getTransactionId());
        // this is a reply message
        replyMsg.setMessageType(DhcpConstants.REPLY);

        // MUST put Server Identifier DUID in REPLY message
        replyMsg.setOption(dhcpServerIdOption);
        
        // MUST copy Client Identifier DUID if given in INFO_REQUEST message
        if (clientIdOption != null) {
            replyMsg.setOption(clientIdOption);
        }
        else {
            log.warn("No ClientId option supplied in Info-Request");
        }

        setGlobalOptions();
        processGlobalFilterGroups();

        dhcpChannel.send(replyMsg);
    }

    private void setGlobalOptions()
    {
        setPreferenceOption(dhcpServerConfig.getPreferenceOption());
        setDnsServersOption(dhcpServerConfig.getDnsServersOption());
        setDomainListOption(dhcpServerConfig.getDomainListOption());
        setVendorInfoOption(dhcpServerConfig.getVendorInfoOption());
    }
    
    private void setPreferenceOption(PreferenceOption preferenceOption)
    {
        if (preferenceOption != null) {
            replyMsg.setOption(new DhcpPreferenceOption(preferenceOption)); 
        }
    }    
    
    private void setDnsServersOption(DnsServersOption dnsServersOption)
    {
        if (dnsServersOption != null) {
            replyMsg.setOption(new DhcpDnsServersOption(dnsServersOption)); 
        }
    }

    private void setDomainListOption(DomainListOption domainListOption)
    {
        if (domainListOption != null) {
            replyMsg.setOption(new DhcpDomainListOption(domainListOption)); 
        }
    }

    private void setVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        if (vendorInfoOption != null) {
            replyMsg.setOption(new DhcpVendorInfoOption(vendorInfoOption)); 
        }
    }
    
    private void processGlobalFilterGroups()
    {
        List<FilterGroups> filterGroups = dhcpServerConfig.getFilterGroups();
        if (filterGroups != null) {
            for (FilterGroups filter : filterGroups) {
                List<OptionExpression> expressions = filter.getOptionExpressions();
                if (expressions != null) {
                    boolean matches = true;     // assume match
                    for (OptionExpression expression : expressions) {
                        DhcpOption option = requestMsg.getOption(expression.getCode());
                        if (option != null) {
                            // found the filter option in the request,
                            // so check if the expression matches
                            if (!evaluateExpression(expression, option)) {
                                // it must match all expressions for the filter
                                // group (i.e. expressions are ANDed), so if
                                // just one doesn't match, then we're done
                                matches = false;
                                break;
                            }
                        }
                        else {
                            // if the expression option wasn't found in the
                            // request message, then it can't match
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        // got a match, apply filter group options to the reply message
                        setFilterGroupOptions(filter);                        
                    }
                }
            }
        }        
    }
    
    private boolean evaluateExpression(OptionExpression expression, DhcpOption option)
    {
        boolean matches = false;
        if (option instanceof DhcpComparableOption) {
            matches = ((DhcpComparableOption)option).matches(expression);
        }
        else {
            log.error("Configured option expression is not comparable:" +
                      " code=" + expression.getCode());
        }
        return matches;
    }
    
    private void setFilterGroupOptions(FilterGroups filter)
    {
        setPreferenceOption(filter.getPreferenceOption());
        setDnsServersOption(filter.getDnsServersOption());
        setDomainListOption(filter.getDomainListOption());
        setVendorInfoOption(filter.getVendorInfoOption());
    }
}
