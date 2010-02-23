package com.jagornet.dhcpv6.server.request.ddns;

import java.net.InetAddress;
import java.security.MessageDigest;

import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;

public abstract class DdnsUpdate
{
	protected String fqdn;
	protected InetAddress inetAddr;
	protected byte[] data;
	
	protected String server;
	protected long ttl;
	protected String zone;
	
	protected String tsigKeyName;
	protected String tsigAlgorithm;
	protected String tsigKeyData;
	
	public DdnsUpdate(String fqdn, InetAddress inetAddr, byte[] duid) throws Exception
	{
		this.fqdn = fqdn;
		this.inetAddr = inetAddr;
		
		byte[] buf = new byte[duid.length + fqdn.getBytes().length];
		System.arraycopy(duid, 0, buf, 0, duid.length);
		System.arraycopy(fqdn.getBytes(), 0, buf, duid.length, fqdn.getBytes().length);
		
		MessageDigest sha1 = MessageDigest.getInstance("SHA-256");
		byte[] hash = sha1.digest(buf);
		
		this.data = new byte[3 + hash.length];
		data[0] = (byte)0x00;
		data[1] = (byte)0x02;
		data[2] = (byte)0x01;
		System.arraycopy(hash, 0, data, 3, hash.length);
	}
	
	protected Resolver createResolver() throws Exception
	{
		Resolver res = new SimpleResolver(server);
		if (tsigKeyName != null) {
			TSIG tsig = null;
			if (tsigAlgorithm != null) {
				tsig = new TSIG(new Name(tsigAlgorithm), tsigKeyName, tsigKeyData);
			}
			else {
				tsig = new TSIG(tsigKeyName, tsigKeyData);
			}
			res.setTSIGKey(tsig);
		}
		return res;
	}
	
	public abstract void sendAdd() throws Exception;
	
	public abstract void sendDelete() throws Exception;
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getTsigKeyName() {
		return tsigKeyName;
	}

	public void setTsigKeyName(String tsigKeyName) {
		this.tsigKeyName = tsigKeyName;
	}

	public String getTsigAlgorithm() {
		return tsigAlgorithm;
	}

	public void setTsigAlgorithm(String tsigAlgorithm) {
		this.tsigAlgorithm = tsigAlgorithm;
	}

	public String getTsigKeyData() {
		return tsigKeyData;
	}

	public void setTsigKeyData(String tsigKeyData) {
		this.tsigKeyData = tsigKeyData;
	}

	public static void main(String[] args)
	{
		try {
			String fqdn = args[0];
			long ttl = Long.parseLong(args[1]);
			InetAddress inetAddr = InetAddress.getByName(args[2]);
			byte[] duid = args[3].getBytes();
			String server = args[4];
			
			ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, inetAddr, duid);
			fwd.setServer(server);
			fwd.setTtl(ttl);
			fwd.setTsigKeyName("macbook-ubuntu.");
			fwd.setTsigAlgorithm("hmac-sha256.");
			fwd.setTsigKeyData("3BE05CzQLXTm5ouGljhJeQ==");
			fwd.sendAdd();
			
			ReverseDdnsUpdate rev = new ReverseDdnsUpdate(fqdn, inetAddr, duid);
			rev.setRevZoneBitLength(48);
			rev.setServer(server);
			rev.setTtl(ttl);
			rev.setTsigKeyName("macbook-ubuntu.");
			rev.setTsigAlgorithm("hmac-sha256.");
			rev.setTsigKeyData("3BE05CzQLXTm5ouGljhJeQ==");
			rev.sendAdd();

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
