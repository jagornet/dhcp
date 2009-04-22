/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpInfoRequestProcessor.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.request;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.DhcpDnsServersOption;
import com.jagornet.dhcpv6.option.DhcpDomainSearchListOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpIaTaOption;
import com.jagornet.dhcpv6.option.DhcpInfoRefreshTimeOption;
import com.jagornet.dhcpv6.option.DhcpNisDomainNameOption;
import com.jagornet.dhcpv6.option.DhcpNisPlusDomainNameOption;
import com.jagornet.dhcpv6.option.DhcpNisPlusServersOption;
import com.jagornet.dhcpv6.option.DhcpNisServersOption;
import com.jagornet.dhcpv6.option.DhcpOptionRequestOption;
import com.jagornet.dhcpv6.option.DhcpPreferenceOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpSipServerAddressesOption;
import com.jagornet.dhcpv6.option.DhcpSipServerDomainNamesOption;
import com.jagornet.dhcpv6.option.DhcpSntpServersOption;
import com.jagornet.dhcpv6.option.DhcpStatusCodeOption;
import com.jagornet.dhcpv6.option.DhcpVendorInfoOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.ConfigOptionsType;
import com.jagornet.dhcpv6.xml.DnsServersOption;
import com.jagornet.dhcpv6.xml.DomainSearchListOption;
import com.jagornet.dhcpv6.xml.Filter;
import com.jagornet.dhcpv6.xml.FilterExpression;
import com.jagornet.dhcpv6.xml.FilterExpressionsType;
import com.jagornet.dhcpv6.xml.FiltersType;
import com.jagornet.dhcpv6.xml.InfoRefreshTimeOption;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.NisDomainNameOption;
import com.jagornet.dhcpv6.xml.NisPlusDomainNameOption;
import com.jagornet.dhcpv6.xml.NisPlusServersOption;
import com.jagornet.dhcpv6.xml.NisServersOption;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.PreferenceOption;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.SipServerAddressesOption;
import com.jagornet.dhcpv6.xml.SipServerDomainNamesOption;
import com.jagornet.dhcpv6.xml.SntpServersOption;
import com.jagornet.dhcpv6.xml.StatusCodeOption;
import com.jagornet.dhcpv6.xml.UnsignedShortListOptionType;
import com.jagornet.dhcpv6.xml.VendorInfoOption;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

/**
 * Title: DhcpInfoRequestProcessor
 * Description: The main class for processing INFO_REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpInfoRequestProcessor implements DhcpRequestProcessor
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpInfoRequestProcessor.class);

    /** The dhcp server config. */
    protected static DhcpV6ServerConfig dhcpServerConfig = 
                                        DhcpServerConfiguration.getConfig();

    /** The client link address. */
    protected final InetAddress clientLinkAddress;
    
    /** The request message. */
    protected final DhcpMessage requestMsg;
    
    /** The reply message. */
    protected DhcpMessage replyMsg;
    
    /** The requested option codes. */
    protected List<Integer> requestedOptionCodes;
    
    /**
     * Construct an DhcpInfoRequest processor.
     * 
     * @param clientLinkAddress the client link address
     * @param reqMsg the Info-Request message
     */
    public DhcpInfoRequestProcessor(InetAddress clientLinkAddress, DhcpMessage reqMsg)
    {
        this.clientLinkAddress = clientLinkAddress;
        this.requestMsg = reqMsg;
        Map<Integer, DhcpOption> optionMap = this.requestMsg.getDhcpOptions();
        if (optionMap != null) {
        	DhcpOptionRequestOption oro = 
        		(DhcpOptionRequestOption) optionMap.get(DhcpConstants.OPTION_ORO);
        	if (oro != null) {
        		UnsignedShortListOptionType ushortListOption = oro.getUnsignedShortListOption();
        		if (ushortListOption != null) {
        			requestedOptionCodes = ushortListOption.getUnsignedShortList();
        		}
        	}
        }
        else {
        	log.error("No options found in Info-RequestMessage!");
        }    	
    }

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return a Reply DhcpMessage
     */
    public DhcpMessage process()
    {
/*
 * FROM RFC 3315:
 * 
 * 15.12. Information-request Message
 *
 *  Clients MUST discard any received Information-request messages.
 *
 *  Servers MUST discard any received Information-request message that
 *  meets any of the following conditions:
 *
 *  -  The message includes a Server Identifier option and the DUID in
 *     the option does not match the server's DUID.
 *
 *  -  The message includes an IA option.
 *  
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
        
        // build a reply message using the local and remote sockets from the request
        replyMsg = new DhcpMessage(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
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
        FiltersType filtersType = dhcpServerConfig.getFilters();
        if (filtersType != null) {
        	processFilters(filtersType.getFilterList());
        }
        
        // handle configuration for the client's link
        Link link = DhcpServerConfiguration.findLinkForAddress(clientLinkAddress);
        if (link != null) {
            log.info("Processing configuration for link: " + link.getAddress());
            processLink(link);
        }

        log.info("Built: " + replyMsg.toString());
        
        return replyMsg;
    }

    /**
     * Sets configured global options in the reply.
     */
    private void setGlobalOptions()
    {
    	setConfigurationOptions(dhcpServerConfig.getOptions());
    }

    /**
     * Sets the configuration options in the reply.
     * 
     * @param configOptions the new standard options
     */
    private void setConfigurationOptions(ConfigOptionsType configOptions)
    {
    	if (configOptions != null) {
	        setPreferenceOption(configOptions.getPreferenceOption());
	        // don't set this option for stateless servers?
	        // setServerUnicastOption(dhcpServerConfig.getServerUnicastOption());
	        setStatusCodeOption(configOptions.getStatusCodeOption());
	        setVendorInfoOption(configOptions.getVendorInfoOption());
	        setDnsServersOption(configOptions.getDnsServersOption());
	        setDomainSearchListOption(configOptions.getDomainSearchListOption());
	        setSipServerAddressesOption(configOptions.getSipServerAddressesOption());
	        setSipServerDomainNamesOption(configOptions.getSipServerDomainNamesOption());
	        setNisServersOption(configOptions.getNisServersOption());
	        setNisDomainNameOption(configOptions.getNisDomainNameOption());
	        setNisPlusServersOption(configOptions.getNisPlusServersOption());
	        setNisPlusDomainNameOption(configOptions.getNisPlusDomainNameOption());
	        setSntpServersOption(configOptions.getSntpServersOption());
	        setInfoRefreshTimeOption(configOptions.getInfoRefreshTimeOption());
    	}
    }
    
    /**
     * Check if the client requested a particular option in the OptionRequestOption.
     * If no OptionRequestOption was supplied by the client, then assume that it
     * wants any option.
     * 
     * @param optionCode the option code to check if the client requested
     * 
     * @return true, if successful
     */
    private boolean clientWantsOption(int optionCode)
    {
    	if (requestedOptionCodes != null) {
    		// if the client requested it, then send it
    		return requestedOptionCodes.contains(optionCode);
    	}
    	
    	// if there is no ORO, then the client did not request the option,
    	// so now check if configured to send only requested options
    	if (DhcpServerConfiguration.getBooleanPolicy("sendRequestedOptionsOnly", true)) {
    		// don't send the option
    		return false;
    	}
    	
    	// if we're not sending requested options only,
    	// then we're sending whatever we have configured
    	return true;
    }
    
    /**
     * Sets the preference option.
     * 
     * @param preferenceOption the new preference option
     */
    private void setPreferenceOption(PreferenceOption preferenceOption)
    {
        if (preferenceOption != null) {
        	if (clientWantsOption(preferenceOption.getCode()))
        		replyMsg.setOption(new DhcpPreferenceOption(preferenceOption)); 
        }
    }    
    
    /**
     * Sets the status code option.
     * 
     * @param statusCodeOption the new status code option
     */
    private void setStatusCodeOption(StatusCodeOption statusCodeOption)
    {
        if (statusCodeOption != null) {
        	if (clientWantsOption(statusCodeOption.getCode()))
        		replyMsg.setOption(new DhcpStatusCodeOption(statusCodeOption)); 
        }
    }    

    /**
     * Sets the vendor info option.
     * 
     * @param vendorInfoOption the new vendor info option
     */
    private void setVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        if (vendorInfoOption != null) {
        	if (clientWantsOption(vendorInfoOption.getCode()))
        		replyMsg.setOption(new DhcpVendorInfoOption(vendorInfoOption)); 
        }
    }
    
    /**
     * Sets the dns servers option.
     * 
     * @param dnsServersOption the new dns servers option
     */
    private void setDnsServersOption(DnsServersOption dnsServersOption)
    {
        if (dnsServersOption != null) {
        	if (clientWantsOption(dnsServersOption.getCode()))
        			replyMsg.setOption(new DhcpDnsServersOption(dnsServersOption)); 
        }
    }

    /**
     * Sets the domain search list option.
     * 
     * @param domainSearchListOption the new domain search list option
     */
    private void setDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        if (domainSearchListOption != null) {
        	if (clientWantsOption(domainSearchListOption.getCode()))
        			replyMsg.setOption(new DhcpDomainSearchListOption(domainSearchListOption)); 
        }
    }
    
    /**
     * Sets the sip server addresses option.
     * 
     * @param sipServerAddressesOption the new sip server addresses option
     */
    private void setSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        if (sipServerAddressesOption != null) {
        	if (clientWantsOption(sipServerAddressesOption.getCode()))
        		replyMsg.setOption(new DhcpSipServerAddressesOption(sipServerAddressesOption)); 
        }
    }

    /**
     * Sets the sip server domain names option.
     * 
     * @param sipServerDomainNamesOption the new sip server domain names option
     */
    private void setSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        if (sipServerDomainNamesOption != null) {
        	if (clientWantsOption(sipServerDomainNamesOption.getCode()))
        		replyMsg.setOption(new DhcpSipServerDomainNamesOption(sipServerDomainNamesOption)); 
        }
    }

    /**
     * Sets the nis servers option.
     * 
     * @param nisServersOption the new nis servers option
     */
    private void setNisServersOption(NisServersOption nisServersOption)
    {
        if (nisServersOption != null) {
        	if (clientWantsOption(nisServersOption.getCode()))
        		replyMsg.setOption(new DhcpNisServersOption(nisServersOption)); 
        }
    }

    /**
     * Sets the nis domain name option.
     * 
     * @param nisDomainNameOption the new nis domain name option
     */
    private void setNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        if (nisDomainNameOption != null) {
        	if (clientWantsOption(nisDomainNameOption.getCode()))
        		replyMsg.setOption(new DhcpNisDomainNameOption(nisDomainNameOption)); 
        }
    }

    /**
     * Sets the nis plus servers option.
     * 
     * @param nisPlusServersOption the new nis plus servers option
     */
    private void setNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        if (nisPlusServersOption != null) {
        	if (clientWantsOption(nisPlusServersOption.getCode()))
        		replyMsg.setOption(new DhcpNisPlusServersOption(nisPlusServersOption)); 
        }
    }

    /**
     * Sets the nis plus domain name option.
     * 
     * @param nisPlusDomainNameOption the new nis plus domain name option
     */
    private void setNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        if (nisPlusDomainNameOption != null) {
        	if (clientWantsOption(nisPlusDomainNameOption.getCode()))
        		replyMsg.setOption(new DhcpNisPlusDomainNameOption(nisPlusDomainNameOption)); 
        }
    }

    /**
     * Sets the sntp servers option.
     * 
     * @param sntpServersOption the new sntp servers option
     */
    private void setSntpServersOption(SntpServersOption sntpServersOption)
    {
        if (sntpServersOption != null) {
        	if (clientWantsOption(sntpServersOption.getCode()))
        		replyMsg.setOption(new DhcpSntpServersOption(sntpServersOption)); 
        }
    }

    /**
     * Sets the info refresh time option.
     * 
     * @param infoRefreshTimeOption the new info refresh time option
     */
    private void setInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        if (infoRefreshTimeOption != null) {
        	if (clientWantsOption(infoRefreshTimeOption.getCode()))
        		replyMsg.setOption(new DhcpInfoRefreshTimeOption(infoRefreshTimeOption)); 
        }
    }
    
    /**
     * Process filters.  Test all filter expressions to see that they
     * match the request, and then apply any options configured for the
     * filter.
     * 
     * @param filters the filters
     */
    private void processFilters(List<Filter> filters)
    {
        if (filters != null) {
            for (Filter filter : filters) {
            	FilterExpressionsType filterExprs = filter.getFilterExpressions();
            	if (filterExprs != null) {
	            	List<FilterExpression> expressions = filterExprs.getFilterExpressionList();
	                if (expressions != null) {
	                    boolean matches = true;     // assume match
	                    for (FilterExpression expression : expressions) {
	                    	OptionExpression optexpr = expression.getOptionExpression();
	                    	// TODO: handle CustomExpression filters
	                        DhcpOption option = requestMsg.getOption(optexpr.getCode());
	                        if (option != null) {
	                            // found the filter option in the request,
	                            // so check if the expression matches
	                            if (!evaluateExpression(optexpr, option)) {
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
	                        setFilterOptions(filter);                        
	                    }
	                }
            	}
            }
        }        
    }
    
    /**
     * Evaluate expression.  Determine if an option matches based on an expression.
     * 
     * @param expression the option expression
     * @param option the option to compare
     * 
     * @return true, if successful
     */
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
    
    /**
     * Sets the filter options.
     * 
     * @param filter the new filter options
     */
    private void setFilterOptions(Filter filter)
    {
    	setConfigurationOptions(filter.getOptions());
    }
    
    /**
     * Process link.  Set any options configured for the link,
     * and then process any filters defined for the link.
     * 
     * @param link the link
     */
    private void processLink(Link link)
    {
        if (link != null) {
            setLinkOptions(link);
            FiltersType filtersType = link.getFilters();
            if (filtersType != null) {
            	processFilters(filtersType.getFilterList());
            }
        }
    }
    
    /**
     * Sets the link options.
     * 
     * @param link the new link options
     */
    private void setLinkOptions(Link link)
    {
    	setConfigurationOptions(link.getOptions());
    }
}
