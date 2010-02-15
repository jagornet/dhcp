package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.xml.AddressBindingsType;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;

public class NaAddrBindingManager 
		extends AddressBindingManager 
		implements NaAddrBindingManagerInterface
{
	public NaAddrBindingManager() throws Exception
	{
		super();
	}
	
	@Override
	protected AddressBindingsType getAddressBindingsType(Link link) {
		return link.getNaAddrBindings();
	}

	@Override
	protected AddressPoolsType getAddressPoolsType(LinkFilter linkFilter) {
		return linkFilter.getNaAddrPools();
	}

	@Override
	protected AddressPoolsType getAddressPoolsType(Link link) {
		return link.getNaAddrPools();
	}

	@Override
	public Binding findCurrentBinding(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg) {
		
		byte[] duid = getDuid(clientIdOption);
		long iaid = iaNaOption.getIaNaOption().getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
				iaid, requestMsg);
	}

	@Override
	public Binding createSolicitBinding(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg, boolean rapidCommit) {
		
		byte[] duid = getDuid(clientIdOption);
		long iaid = iaNaOption.getIaNaOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaNaOption);
		
		return super.createSolicitBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
				iaid, requestAddrs, requestMsg, rapidCommit);
	}

	@Override
	public Binding updateBinding(Binding binding, Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg, byte state) {
		
		byte[] duid = getDuid(clientIdOption);
		long iaid = iaNaOption.getIaNaOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaNaOption);
		
		return super.updateBinding(binding, clientLink, duid, IdentityAssoc.NA_TYPE,
				iaid, requestAddrs, requestMsg, state);
	}
	
	private List<InetAddress> getInetAddrs(DhcpIaNaOption iaNaOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpIaAddrOption> iaAddrs = iaNaOption.getIaAddrOptions();
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
