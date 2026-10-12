/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core;

import java.util.Calendar;

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
		Package pkg = Package.getPackage("com.jagornet.dhcp.core"); 
		StringBuilder sb = new StringBuilder();
		sb.append(pkg.getImplementationTitle());
		sb.append(' ');
		sb.append(pkg.getImplementationVersion());
		sb.append(System.getProperty("line.separator"));
		sb.append("Copyright ");
		sb.append(pkg.getImplementationVendor());
		sb.append(" 2009-");
		sb.append(Calendar.getInstance().get(Calendar.YEAR));
		sb.append(".  All Rights Reserved.");
		return sb.toString();
	}
}
