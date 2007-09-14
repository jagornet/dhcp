package com.agr.dhcpv6.util;

/**
 * Title:        IpAddr
 * Description:  Wrapper class to add some convenience methods to InetAddress
 * Copyright:    Copyright (c) 2000
 * Company: AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

import java.net.*;

public class IpAddr
{
    private InetAddress inetAddr;

    public IpAddr(InetAddress inetAddr)
    {
	    this.inetAddr = inetAddr;
    }

    public IpAddr(String name_or_ip)
	    throws UnknownHostException
    {
	    inetAddr = InetAddress.getByName(name_or_ip);
    }

    public IpAddr(long ip)
	    throws UnknownHostException
    {
		long i0 = (ip >>> 24);              // r_shift out the last three bytes
		long i1 = (((int)ip << 8) >>> 24);  // l_shift first byte out of int, then unsigned r_shift back to one byte
		long i2 = (((int)ip << 16) >>> 24); // l_shift first 2 bytes out of int, then unsigned r_shift back to one byte
		long i3 = (((int)ip << 24) >>> 24); // l_shift first 3 bytes out of int, then unsigned r_shift back to one byte
		inetAddr = InetAddress.getByName(i0 + "." + i1 + "." + i2 + "." + i3);
    }

	public static long asLong(String name_or_ip)
		throws UnknownHostException
	{
		if(name_or_ip != null) {
			IpAddr ia = new IpAddr(name_or_ip);
			return ia.toLong();
		}
		else {
			return 0;
		}
	}

    public long toLong()
    {
		byte[] b = inetAddr.getAddress();
		long i0 = ((b[0] << 24) >>> 24);     // unsigned
		long i1 = ((b[1] << 24) >>> 24);     // unsigned
		long i2 = ((b[2] << 24) >>> 24);     // unsigned
		long i3 = ((b[3] << 24) >>> 24);     // unsigned
		long l = (i0 << 24) +
				 (i1 << 16) +
				 (i2 << 8) +
				 (i3);
		return l;
    }

	public String toPaddedString()
	{
		byte[] b = inetAddr.getAddress();
		long i0 = ((b[0] << 24) >>> 24);     // unsigned
		long i1 = ((b[1] << 24) >>> 24);     // unsigned
		long i2 = ((b[2] << 24) >>> 24);     // unsigned
		long i3 = ((b[3] << 24) >>> 24);     // unsigned
		String s0 = String.valueOf(i0);
		String s1 = String.valueOf(i1);
		String s2 = String.valueOf(i2);
		String s3 = String.valueOf(i3);
		return prepend(s0, 3, '0')  + '.' +
			prepend(s1, 3, '0') + '.' +
			prepend(s2, 3, '0') + '.' +
			prepend(s3, 3, '0');
	}

	public String prepend(String s, int len, char c) {
		if (s.length() == len)
			return s;

		StringBuffer sb = new StringBuffer(s);
		while (sb.length() < len) {
			sb.insert(0, c);
		}
		return sb.toString();
	}

	public InetAddress getInetAddress()
	{
		return inetAddr;
	}

    public String getHostAddressString()
    {
		return inetAddr.getHostAddress();
    }

    public static void main(String args[])
    {
		try {
			IpAddr ip1 = new IpAddr("192.168.0.2");
			long l1 = ip1.toLong();
			System.out.println("l1=" + l1);
			IpAddr ip2 = new IpAddr("192.168.0.3");
			long l2 = ip2.toLong();
			System.out.println("l2=" + l2);
			IpAddr ip3 = new IpAddr("24.1.2.3");
			long l3 = ip3.toLong();
			System.out.println("l3=" + l3);
			// ip4 = 192.168.0.0
			IpAddr ip4 = new IpAddr(3232235520l);   // that's an 'l' at the end for 'long'
			System.out.println("ip4=" + ip4.getHostAddressString());

			if(l1<l2) {
			    System.out.println("l1<l2");
			}
			else {
		    	System.out.println("l1!<l2");
			}
			if(l1<l3) {
	    		System.out.println("l1<l3");
			}
			else {
				System.out.println("l1!<l3");
			}

			byte b1[] = ip1.inetAddr.getAddress();
			int i = 0;
			for(int j=0; j<b1.length; j++) {
				i = (int)b1[j];
				i <<= 24;       // shift out the implied sign bit
				i >>>= 24;      // shift back unsigned to orginal precision
				System.out.println("b1[" + j + "]=" + i);
			}

			IpAddr ip5 = new IpAddr("25.27.9.2");
			IpAddr ip6 = new IpAddr("25.27.11.254");

			System.out.println("ip6-ip5 = " + (ip6.toLong() - ip5.toLong()));
			for(long j=ip5.toLong(); j<=ip6.toLong(); j++) {
			    System.out.println((new IpAddr(j)).getHostAddressString());
			}
		}
		catch (Exception e) {
			System.err.println(e);
		}
    }
}
