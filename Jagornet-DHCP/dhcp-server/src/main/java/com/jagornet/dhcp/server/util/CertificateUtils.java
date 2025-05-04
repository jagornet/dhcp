package com.jagornet.dhcp.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class CertificateUtils {

	public static CertificateDetails getCertificateDetails(String jksPath, String jksPassword) {
	 
		CertificateDetails certDetails = null;
		try {	 
			KeyStore keyStore = KeyStore.getInstance("JKS");
	 
			// Provide location of Java Keystore and password for access
			if (new File(jksPath).exists()) {
				keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());
			}
			else {
				keyStore.load(ClassLoader.getSystemResourceAsStream(jksPath), jksPassword.toCharArray());
			}
	 
			// iterate over all aliases
			Enumeration<String> es = keyStore.aliases();
			String alias = "";
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				if (keyStore.isKeyEntry(alias)) {					 
					KeyStore.PrivateKeyEntry pkEntry = 
							(KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, 
									new KeyStore.PasswordProtection(jksPassword.toCharArray()));
		 
					PrivateKey myPrivateKey = pkEntry.getPrivateKey();
		 
					// Load certificate chain
					Certificate[] chain = keyStore.getCertificateChain(alias);

					if (certDetails == null) {
						certDetails = new CertificateDetails();
					}
					certDetails.setPrivateKey(myPrivateKey);
					certDetails.setX509Certificate((X509Certificate) chain[0]);
				}
				else if (keyStore.isCertificateEntry(alias)) {

					if (certDetails == null) {
						certDetails = new CertificateDetails();
					}
					Certificate trustedCert = keyStore.getCertificate(alias);
					certDetails.setTrustedCertificate((X509Certificate) trustedCert);
				}
			}
	 
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			e.printStackTrace();
		}
	 
		return certDetails;
	}
	
	public static class CertificateDetails {
		 
		private PrivateKey privateKey;
		private X509Certificate x509Certificate;
		private X509Certificate trustedCertificate;
		
		public PrivateKey getPrivateKey() {
			return privateKey;
		}
		public void setPrivateKey(PrivateKey privateKey) {
			this.privateKey = privateKey;
		}
		public X509Certificate getX509Certificate() {
			return x509Certificate;
		}
		public void setX509Certificate(X509Certificate x509Certificate) {
			this.x509Certificate = x509Certificate;
		}
		public X509Certificate getTrustedCertificate() {
			return trustedCertificate;
		}
		public void setTrustedCertificate(X509Certificate trustedCertificate) {
			this.trustedCertificate = trustedCertificate;
		}
	}
}