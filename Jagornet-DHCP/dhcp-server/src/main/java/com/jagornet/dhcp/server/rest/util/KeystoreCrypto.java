package com.jagornet.dhcp.server.rest.util;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;

import javax.crypto.Cipher;

public class KeystoreCrypto {

	private String keystoreFilename;
	private char[] keystorePassword;
	
	public KeystoreCrypto(String keystoreFilename, String keystorePassword) {
		this.keystoreFilename = keystoreFilename;
		this.keystorePassword = keystorePassword.toCharArray();
	}
	
	private KeyStore loadKeystore() throws Exception {
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		FileInputStream fis = new FileInputStream(keystoreFilename);
		ks.load(fis, keystorePassword);
		return ks;
	}
	
	public String encrypt(String plainText) throws Exception {
		KeyStore ks = loadKeystore();
		// we can encrypt with either the cert or the key
		// and then decrypt with the other
		// but makes more sense to encrypt with public cert
		// and decrypt with the private key, especially if
		// we are encrypting/decrypting passwords because
		// then anyone can encrypt the password, but only
		// someone with the key can read the password, and
		// this is the way python cryptography module works
		Certificate cert = ks.getCertificate("jagornet-dhcp-server");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, cert);
		byte[] encodedBytes = cipher.doFinal(plainText.getBytes());
		// return base 64 encoded string
		return Base64.getEncoder().encodeToString(encodedBytes);	
	}
	
	public String decrypt(String base64EncodedText) throws Exception {
		KeyStore ks = loadKeystore();
		Key serverKey = ks.getKey("jagornet-dhcp-server", keystorePassword);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, serverKey);
		byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedText);
		byte[] decryptedBytes = cipher.doFinal(decodedBytes);
		return new String(decryptedBytes);	
	}
	
	public static void main(String[] args) {
		String keystoreFilename = null;
		String keystorePassword = null;
		String plainText = null;
		String encodedEncryptedText = null;
		
		try {
			if (args.length >= 6) {
				for (int i = 0; i < args.length; i++) {
					if (args[i].contentEquals("-k")) {
						keystoreFilename = args[++i];
					}
					if (args[i].contentEquals("-p")) {
						keystorePassword = args[++i];
					}
					if (args[i].contentEquals("-e")) {
						plainText = args[++i];
					}
					if (args[i].contentEquals("-d")) {
						encodedEncryptedText = args[++i];
					}
				}
				KeystoreCrypto kc = new KeystoreCrypto(keystoreFilename, keystorePassword);
				if (plainText != null) {
					System.out.println("plainText=" + plainText);
					System.out.println("encodedEncryptedText=" + kc.encrypt(plainText));
				}
				else if (encodedEncryptedText != null) {
					System.out.println("encodedEncryptedText=" + encodedEncryptedText);
					System.out.println("plainText=" + kc.decrypt(encodedEncryptedText));
				}
			}
			else {
				System.err.println("Usage: KeystoreCrypto -k <keystoreFilename> -p <keystorePassword> [ -e plainText | -d encodedEncryptedText ]");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
