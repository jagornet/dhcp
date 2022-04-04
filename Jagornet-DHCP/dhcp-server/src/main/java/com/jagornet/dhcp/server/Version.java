/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Version.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author A. Gregory Rabil
 *
 */
public class Version
{
	static String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
	
	String implVendor = null;
	String implTitle = null;
	String implVersion = null;
	
	public Version() {
		Package pkg = Package.getPackage("com.jagornet.dhcp.server");
		implVendor = pkg.getImplementationTitle();
		implTitle = pkg.getImplementationTitle();
		implVersion = pkg.getImplementationVersion();
		// the Package values are null on Docker because there is
		// no jar file, so go find the Manifest in the classpath
		if ((implVendor == null) || (implTitle == null) || (implVersion == null)) {
			getImplAttrsFromManifest();
		}
		if (implVendor == null) {
			implVendor = "Jagornet Technologies, LLC";
		}
		if (implTitle == null) {
			implTitle = "Jagornet DHCP Server";
		}
		if (implVersion == null) {
			implVersion = "";
		}
	}
	
	public String getVersion() {
		StringBuilder sb = new StringBuilder();
		sb.append(implTitle);
		sb.append(' ');
		sb.append(implVersion);
		sb.append(System.getProperty("line.separator"));
		sb.append("Copyright ");
		sb.append("2009-");
		sb.append(Calendar.getInstance().get(Calendar.YEAR));
		sb.append(" ");
		sb.append(implVendor);
		sb.append(".  All Rights Reserved.");
		return sb.toString();
	}
	
	private void getImplAttrsFromManifest() {
		InputStream is = null;
		try {
			is = this.getClass().getResourceAsStream(MANIFEST_FILE);
			if (is != null) {
				Manifest mf = new Manifest(is);
				Attributes attrs = mf.getMainAttributes();
				if (attrs != null) {
					implVendor = attrs.getValue("Implementation-Vendor");
					implTitle = attrs.getValue("Implementation-Title");
					implVersion = attrs.getValue("Implementation-Version");
				}
			}
			else {
				System.err.println("Resource not found: " + MANIFEST_FILE);
			}
		}
		catch (IOException ex) {
			System.err.println(ex);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// nothing
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Version v = new Version();
		System.out.println(v.getVersion());
	}
}
