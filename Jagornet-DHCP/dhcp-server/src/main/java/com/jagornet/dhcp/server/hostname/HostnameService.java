/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.jagornet.dhcp.server.hostname;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.Filter;
import com.jagornet.dhcp.server.config.xml.FiltersType;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.config.xml.LinkFiltersType;

/**
 * Service to orchestrate hostname scrubbing, validation, and generation
 * based on hierarchical DHCP server policies.
 * 
 * @author A. Gregory Rabil
 */
public class HostnameService {

	private static Logger log = LoggerFactory.getLogger(HostnameService.class);

	public static final String MODE_AS_IS = "as-is";
	public static final String MODE_SCRUB = "scrub";
	public static final String MODE_SCRUB_OR_GENERATE = "scrub-or-generate";
	public static final String MODE_ALWAYS_GENERATE = "always-generate";
	public static final String MODE_DISABLED = "disabled";

	/**
	 * Resolves the client hostname and FQDN according to configuration and request
	 * details.
	 * 
	 * @param requestMsg         the DHCP request message
	 * @param clientLink         the client DHCP link
	 * @param configObj          the binding configuration object (pool/link/etc.)
	 * @param clientSuppliedName the client-supplied hostname or FQDN (if any)
	 * @param assignedIp         the IP address assigned or leased to the client
	 * @param chAddr             client hardware address (MAC)
	 * @param duid               client DUID
	 * @return HostnameResolution result
	 */
	public static HostnameResolution resolveHostname(DhcpMessage requestMsg,
			DhcpLink clientLink, DhcpConfigObject configObj, String clientSuppliedName,
			InetAddress assignedIp, byte[] chAddr, byte[] duid) {

		Link link = (clientLink != null) ? clientLink.getLink() : null;

		String mode = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, link, Property.HOSTNAME_MODE);
		if (mode == null || mode.trim().isEmpty()) {
			mode = MODE_AS_IS;
		}
		mode = mode.trim().toLowerCase();

		if (mode.equals(MODE_DISABLED)) {
			return new HostnameResolution(null, null, false, false, false, false);
		}

		String domain = DhcpServerPolicies.effectivePolicy(link, Property.DDNS_DOMAIN);
		if (domain == null || domain.trim().isEmpty()) {
			domain = DhcpServerPolicies.effectivePolicy(link, Property.DDNS_FORWARD_ZONE_NAME);
		}
		if (domain == null || domain.trim().isEmpty()) {
			DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
			if (config != null) {
				domain = config.findConfiguredForwardDomain(requestMsg, link, configObj, assignedIp);
			}
		}
		if (domain != null) {
			domain = domain.trim();
			while (domain.startsWith(".")) {
				domain = domain.substring(1);
			}
			while (domain.endsWith(".")) {
				domain = domain.substring(0, domain.length() - 1);
			}
			if (domain.isEmpty()) {
				domain = null;
			}
		}

		String template = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, link,
				Property.HOSTNAME_GENERATE_TEMPLATE);
		boolean allowUnderscores = DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg, configObj, link,
				Property.HOSTNAME_SCRUB_ALLOW_UNDERSCORES);
		String replaceChar = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, link,
				Property.HOSTNAME_SCRUB_REPLACE_CHAR);
		boolean collapseHyphens = DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg, configObj, link,
				Property.HOSTNAME_SCRUB_COLLAPSE_HYPHENS);
		String stripDomainsStr = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, link,
				Property.HOSTNAME_SCRUB_STRIP_DOMAINS);
		List<String> stripDomains = HostnameScrubber.parseStripDomains(stripDomainsStr);

		String sendOption12Policy = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, link,
				Property.HOSTNAME_SEND_OPTION12);
		if (sendOption12Policy == null || sendOption12Policy.trim().isEmpty()) {
			sendOption12Policy = "on-change";
		}
		sendOption12Policy = sendOption12Policy.trim().toLowerCase();

		String matchedFilterName = findMatchingFilterName(requestMsg, clientLink);

		String finalHostname = null;
		String finalFqdn = null;
		boolean modified = false;
		boolean generated = false;
		boolean valid = false;

		boolean hasClientName = (clientSuppliedName != null && !clientSuppliedName.trim().isEmpty());

		if (mode.equals(MODE_ALWAYS_GENERATE)) {
			finalHostname = HostnameGenerator.generate(template, assignedIp, chAddr, duid, matchedFilterName);
			finalFqdn = buildFqdn(finalHostname, domain);
			modified = hasClientName ? !finalHostname.equalsIgnoreCase(clientSuppliedName) : true;
			generated = true;
			valid = (finalHostname != null && !finalHostname.isEmpty());
		} else if (hasClientName) {
			String raw = clientSuppliedName.trim();
			if (mode.equals(MODE_AS_IS)) {
				// Keep client-supplied name as-is
				finalHostname = extractFirstLabel(raw);
				finalFqdn = buildFqdnFromClient(raw, domain);
				modified = false;
				generated = false;
				valid = true;
			} else if (mode.equals(MODE_SCRUB) || mode.equals(MODE_SCRUB_OR_GENERATE)) {
				HostnameScrubber.ScrubResult scrubResult = HostnameScrubber.scrub(
						raw, allowUnderscores, replaceChar, collapseHyphens, stripDomains);

				if (scrubResult.isValid()) {
					String clean = scrubResult.getScrubbedName();
					finalHostname = extractFirstLabel(clean);
					finalFqdn = buildFqdnFromClient(clean, domain);
					modified = scrubResult.isModified();
					generated = false;
					valid = true;
				} else if (mode.equals(MODE_SCRUB_OR_GENERATE)) {
					// Fallback to generator
					finalHostname = HostnameGenerator.generate(template, assignedIp, chAddr, duid, matchedFilterName);
					finalFqdn = buildFqdn(finalHostname, domain);
					modified = true;
					generated = true;
					valid = (finalHostname != null && !finalHostname.isEmpty());
				} else {
					// Scrub only, but failed
					valid = false;
					modified = true;
				}
			}
		} else {
			// No client-supplied name
			if (mode.equals(MODE_SCRUB_OR_GENERATE)) {
				finalHostname = HostnameGenerator.generate(template, assignedIp, chAddr, duid, matchedFilterName);
				finalFqdn = buildFqdn(finalHostname, domain);
				modified = true;
				generated = true;
				valid = (finalHostname != null && !finalHostname.isEmpty());
			} else {
				valid = false;
			}
		}

		boolean sendOption12 = computeSendOption12(sendOption12Policy, modified, requestMsg);

		HostnameResolution resolution = new HostnameResolution(
				finalHostname, finalFqdn, modified, generated, valid, sendOption12);

		if (log.isDebugEnabled()) {
			log.debug("Resolved hostname: " + resolution);
		}

		return resolution;
	}

	private static boolean computeSendOption12(String policy, boolean modified, DhcpMessage requestMsg) {
		if ("always".equals(policy)) {
			return true;
		}
		if ("never".equals(policy)) {
			return false;
		}
		if ("on-change".equals(policy)) {
			return modified;
		}
		if ("if-requested".equals(policy)) {
			if (requestMsg != null) {
				List<Integer> requested = requestMsg.getRequestedOptionCodes();
				if (requested != null && requested.contains(DhcpConstants.V4OPTION_HOSTNAME)) {
					return true;
				}
			}
			return false;
		}
		return modified;
	}

	private static String extractFirstLabel(String name) {
		if (name == null)
			return null;
		int dot = name.indexOf('.');
		if (dot > 0) {
			return name.substring(0, dot);
		}
		return name;
	}

	private static String buildFqdn(String hostname, String domain) {
		if (hostname == null)
			return null;
		if (domain != null && !domain.isEmpty()) {
			return hostname + "." + domain;
		}
		return hostname;
	}

	private static String buildFqdnFromClient(String clientName, String configuredDomain) {
		if (clientName == null)
			return null;
		int dot = clientName.indexOf('.');
		if (configuredDomain != null && !configuredDomain.isEmpty()) {
			if (dot > 0) {
				return clientName.substring(0, dot + 1) + configuredDomain;
			} else {
				return clientName + "." + configuredDomain;
			}
		}
		return clientName;
	}

	private static String findMatchingFilterName(DhcpMessage requestMsg, DhcpLink clientLink) {
		if (requestMsg == null)
			return null;
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		if (config == null)
			return null;

		// Check link filters first
		if (clientLink != null && clientLink.getLink() != null) {
			LinkFiltersType linkFiltersType = clientLink.getLink().getLinkFilters();
			if (linkFiltersType != null) {
				List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
				if (linkFilters != null) {
					for (LinkFilter lf : linkFilters) {
						if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, lf)) {
							return lf.getName();
						}
					}
				}
			}
		}

		// Check global filters
		FiltersType globalFiltersType = config.getGlobalFilters();
		if (globalFiltersType != null) {
			List<Filter> filters = globalFiltersType.getFilterList();
			if (filters != null) {
				for (Filter f : filters) {
					if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, f)) {
						return f.getName();
					}
				}
			}
		}

		return null;
	}
}
