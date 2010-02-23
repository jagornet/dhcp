package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaTaOption;
import com.jagornet.dhcpv6.xml.AddressBindingsType;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;

public class TaAddrBindingManager 
		extends AddressBindingManager 
		implements TaAddrBindingManagerInterface
{
	public TaAddrBindingManager() throws Exception
	{
		super();
	}
	
	@Override
	protected AddressBindingsType getAddressBindingsType(Link link) {
		return link.getTaAddrBindings();
	}

	@Override
	protected AddressPoolsType getAddressPoolsType(LinkFilter linkFilter) {
		return linkFilter.getTaAddrPools();
	}

	@Override
	protected AddressPoolsType getAddressPoolsType(Link link) {
		return link.getTaAddrPools();
	}

	@Override
	public Binding findCurrentBinding(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaTaOption iaTaOption,
			DhcpMessage requestMsg) {
		
		byte[] duid = clientIdOption.getDuid();;
		long iaid = iaTaOption.getIaTaOption().getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.TA_TYPE, 
				iaid, requestMsg);
	}

	@Override
	public Binding createSolicitBinding(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaTaOption iaTaOption,
			DhcpMessage requestMsg, boolean rapidCommit) {
		
		byte[] duid = clientIdOption.getDuid();;
		long iaid = iaTaOption.getIaTaOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaTaOption);
		
		return super.createSolicitBinding(clientLink, duid, IdentityAssoc.TA_TYPE, 
				iaid, requestAddrs, requestMsg, rapidCommit);
	}

	@Override
	public Binding updateBinding(Binding binding, Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaTaOption iaTaOption,
			DhcpMessage requestMsg, byte state) {
		
		byte[] duid = clientIdOption.getDuid();;
		long iaid = iaTaOption.getIaTaOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaTaOption);
		
		return super.updateBinding(binding, clientLink, duid, IdentityAssoc.TA_TYPE,
				iaid, requestAddrs, requestMsg, state);
	}
	
	private List<InetAddress> getInetAddrs(DhcpIaTaOption iaTaOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpIaAddrOption> iaAddrs = iaTaOption.getIaAddrOptions();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			inetAddrs = new ArrayList<InetAddress>();
			for (DhcpIaAddrOption iaAddr : iaAddrs) {
				InetAddress inetAddr = iaAddr.getInetAddress();
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}
}
