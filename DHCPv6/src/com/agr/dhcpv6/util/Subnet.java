package com.agr.dhcpv6.util;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

import java.net.*;

public class Subnet
{
    private IpAddr startIp;
    private IpAddr mask;

    public Subnet(String startIpStr, String maskStr)
		throws UnknownHostException
    {
		this((new IpAddr(startIpStr)), (new IpAddr(maskStr)));
    }
    public Subnet(IpAddr startIp, IpAddr mask)
    {
		this.startIp = startIp;
		this.mask = mask;
    }
    public IpAddr getStartIp()
    {
		return startIp;
    }
    public IpAddr getMask()
    {
		return mask;
    }
    public IpAddr getEndIp()
		throws UnknownHostException
    {
		long end = startIp.toLong() + (~mask.toLong());
		return new IpAddr(end);
    }
    public static void main(String[] args)
    {
		try {
			Subnet subnet1 = new Subnet("192.168.0.0", "255.255.224.0");
			System.out.println("endip = " + subnet1.getEndIp().getHostAddressString());
		}
		catch (Exception e) {
			System.err.println(e);
		}
    }
}