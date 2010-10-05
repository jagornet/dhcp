package com.jagornet.dhcpv6.server.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.jagornet.dhcpv6.message.DhcpMessage;

public class DhcpUnicastChannelDecoder extends DhcpChannelDecoder 
{
	public DhcpUnicastChannelDecoder(InetSocketAddress localSocketAddress)
	{
		super(localSocketAddress);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		Object obj = super.decode(ctx, channel, msg);
		if (obj instanceof DhcpMessage) {
			// if we receive the packet on the NIO channel, 
			// then it must be a unicast packet
			((DhcpMessage)obj).setUnicast(true);
		}
		return obj;
	}
}
