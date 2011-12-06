package com.jagornet.dhcpv6.message;

import com.jagornet.dhcpv6.option.base.DhcpOption;

public interface DhcpMessageInterface {
	public void setMessageType(short msgType);
	public short getMessageType();
	public DhcpOption getDhcpOption(int optionCode);
}
