/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Version.java is part of DHCPv6.
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
package com.jagornet.dhcpv6;

/**
 * @author A. Gregory Rabil
 *
 */
public class Version
{
	public static void main(String[] args)
	{
		System.out.println(getVersion());
	}
	
	public static String getVersion()
	{
		Package pkg = Package.getPackage("com.jagornet.dhcpv6"); 
		StringBuilder sb = new StringBuilder();
		sb.append(pkg.getImplementationTitle());
		sb.append(' ');
		sb.append(pkg.getImplementationVersion());
		sb.append(System.getProperty("line.separator"));
		sb.append("Copyright ");
		sb.append(pkg.getImplementationVendor());
		sb.append(" 2009-2013.  All Rights Reserved.");
		return sb.toString();
	}
}
