/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
	static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
	
	String implVendor = null;
	String implTitle = null;
	String implVersion = null;
	
	public Version() {
		Package pkg = Version.class.getPackage();
		if (pkg == null) {
			pkg = Version.class.getClassLoader().getDefinedPackage("com.jagornet.dhcp.server");
		}
		if (pkg != null) {
			implVendor = pkg.getImplementationVendor();
			implTitle = pkg.getImplementationTitle();
			implVersion = pkg.getImplementationVersion();
		}
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
					String mfImplVendor = attrs.getValue("Implementation-Vendor");
					if ((mfImplVendor != null) && mfImplVendor.startsWith("Jagornet")) {
						implVendor = mfImplVendor;
						implTitle = attrs.getValue("Implementation-Title");
						implVersion = attrs.getValue("Implementation-Version");	
					}
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
