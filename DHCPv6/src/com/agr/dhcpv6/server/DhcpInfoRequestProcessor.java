package com.agr.dhcpv6.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.option.DhcpClientIdOption;
import com.agr.dhcpv6.option.DhcpComparableOption;
import com.agr.dhcpv6.option.DhcpDnsServersOption;
import com.agr.dhcpv6.option.DhcpDomainSearchListOption;
import com.agr.dhcpv6.option.DhcpIaNaOption;
import com.agr.dhcpv6.option.DhcpIaTaOption;
import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.option.DhcpPreferenceOption;
import com.agr.dhcpv6.option.DhcpServerIdOption;
import com.agr.dhcpv6.option.DhcpSipServerAddressesOption;
import com.agr.dhcpv6.option.DhcpSipServerDomainNamesOption;
import com.agr.dhcpv6.option.DhcpVendorInfoOption;
import com.agr.dhcpv6.server.config.DhcpServerConfiguration;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig;
import com.agr.dhcpv6.server.config.xml.DnsServersOption;
import com.agr.dhcpv6.server.config.xml.DomainSearchListOption;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.PreferenceOption;
import com.agr.dhcpv6.server.config.xml.ServerIdOption;
import com.agr.dhcpv6.server.config.xml.SipServerAddressesOption;
import com.agr.dhcpv6.server.config.xml.SipServerDomainNamesOption;
import com.agr.dhcpv6.server.config.xml.VendorInfoOption;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig.FilterGroups;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig.Links;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * Title:        DhcpInfoRequestProcessor
 * Description:  The main class for processing INFO_REQUEST messages.
 * 
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpInfoRequestProcessor
{
    private static Log log = LogFactory.getLog(DhcpInfoRequestProcessor.class);

    protected static DhcpV6ServerConfig dhcpServerConfig = 
                                        DhcpServerConfiguration.getConfig();

    protected InetAddress clientLink;
    protected DhcpMessage requestMsg;
    protected DhcpMessage replyMsg;

    /**
     * Construct an DhcpInfoRequest processor
     *
     * @param   clientLink  the interface address for the client link
     * @param   reqMsg  must be an INFO_REQUEST type DhcpMessage
     */
    public DhcpInfoRequestProcessor(InetAddress clientLink, DhcpMessage reqMsg)
    {
        this.clientLink = clientLink;
        this.requestMsg = reqMsg;
    }

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     */
    public DhcpMessage process()
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
        
        log.info("Processing: " + requestMsg.toString());

        ServerIdOption serverIdOption = dhcpServerConfig.getServerIdOption();
        if (serverIdOption == null) {
            log.error("Invalid configuration - ServerId option must not be null!");
            return null;
        }

        DhcpServerIdOption dhcpServerIdOption =
            new DhcpServerIdOption(serverIdOption);
        
        boolean hasIaOption = false;
        DhcpClientIdOption clientIdOption = null;
        DhcpServerIdOption requestedServerIdOption = null;
        Collection<DhcpOption> options = requestMsg.getOptions();
        if (options != null) {
            for (DhcpOption option : options) {
                log.debug(option.toString());
                if (option instanceof DhcpClientIdOption) {
                    clientIdOption = (DhcpClientIdOption)option;
                }
                else if (option instanceof DhcpServerIdOption) {
                    requestedServerIdOption = (DhcpServerIdOption)option;
                }
                else if (option instanceof DhcpIaNaOption) {
                    hasIaOption = true;
                }
                else if (option instanceof DhcpIaTaOption) {
                    hasIaOption = true;
                }
            }
        }
        
        // if the client message has an IA option (IA_NA, IA_TA)
        // then the Stateless DHCPv6 server must ignore the request
        if (hasIaOption) {
            log.warn("Ignoring Info-Request message: " +
                     " client message contains an IA option.");
            return null;
        }
        
        // if the client provided a ServerID option, then it MUST
        // match our configured ServerID, otherwise ignore the request
        if ( (requestedServerIdOption != null) &&
             !dhcpServerIdOption.equals(requestedServerIdOption) ) {
            log.warn("Ingoring Info-Request message: " +
                     "Requested ServerID: " + requestedServerIdOption +
                     "My ServerID: " + dhcpServerIdOption);
            return null;
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

        // put any globally defined options in the reply packet
        setGlobalOptions();
        
        // process global filter groups
        processFilterGroups(dhcpServerConfig.getFilterGroups());
        
        // handle configuration for the client's link
        Links link = DhcpServerConfiguration.findLinkForAddress(clientLink);
        if (link != null) {
            log.info("Processing configuration for link: " + link.getAddress());
            processLink(link);
        }

        log.info("Built: " + replyMsg.toString());
        
        return replyMsg;
    }

    private void setGlobalOptions()
    {
        setPreferenceOption(dhcpServerConfig.getPreferenceOption());
        setDnsServersOption(dhcpServerConfig.getDnsServersOption());
        setDomainSearchListOption(dhcpServerConfig.getDomainSearchListOption());
        setSipServerAddressesOption(dhcpServerConfig.getSipServerAddressesOption());
        setSipServerDomainNamesOption(dhcpServerConfig.getSipServerDomainNamesOption());
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

    private void setDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        if (domainSearchListOption != null) {
            replyMsg.setOption(new DhcpDomainSearchListOption(domainSearchListOption)); 
        }
    }
    
    private void setSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        if (sipServerAddressesOption != null) {
            replyMsg.setOption(new DhcpSipServerAddressesOption(sipServerAddressesOption)); 
        }
    }

    private void setSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        if (sipServerDomainNamesOption != null) {
            replyMsg.setOption(new DhcpSipServerDomainNamesOption(sipServerDomainNamesOption)); 
        }
    }

    private void setVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        if (vendorInfoOption != null) {
            replyMsg.setOption(new DhcpVendorInfoOption(vendorInfoOption)); 
        }
    }
    
    private void processFilterGroups(List<FilterGroups> filterGroups)
    {
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
                        log.info("Request matches filter: " + filter.getName());
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
        setDomainSearchListOption(filter.getDomainSearchListOption());
        setSipServerAddressesOption(filter.getSipServerAddressesOption());
        setSipServerDomainNamesOption(filter.getSipServerDomainNamesOption());
        setVendorInfoOption(filter.getVendorInfoOption());
    }
    
    private void processLink(Links link)
    {
        if (link != null) {
            setLinkOptions(link);
            // Oddly enough, the JAXB compiler creates two separate inner classes
            // of the DhcpV6ServerConfiguration class to represent the global
            // filter groups and the link-specific filter groups, even though
            // these two classes are the same.
            // So we'll "convert" the link-specific filters to "global" filters 
            // so that we can reuse the filter handling in processFilterGroups()
            if (link.getFilterGroups() != null) {
                List<Links.FilterGroups> linkFilterGroups = link.getFilterGroups();
                List<FilterGroups> filterGroups = new ArrayList<FilterGroups>();
                for (Links.FilterGroups linkFilterGroup : linkFilterGroups) {
                    FilterGroups filterGroup = new FilterGroups();
                    try {
                        // copy (destination, source)
                        BeanUtils.copyProperties(filterGroup, linkFilterGroup);
                        filterGroups.add(filterGroup);
                    }
                    catch (Exception ex) {
                        log.error("Failed to convert Links.FilterGroups to FilterGroups" + 
                                  ex);
                    }
                }
                processFilterGroups(filterGroups);
            }
        }
    }
    
    private void setLinkOptions(Links link)
    {
        setPreferenceOption(link.getPreferenceOption());
        setDnsServersOption(link.getDnsServersOption());
        setDomainSearchListOption(link.getDomainSearchListOption());
        setSipServerAddressesOption(link.getSipServerAddressesOption());
        setSipServerDomainNamesOption(link.getSipServerDomainNamesOption());
        setVendorInfoOption(link.getVendorInfoOption());
    }
}
