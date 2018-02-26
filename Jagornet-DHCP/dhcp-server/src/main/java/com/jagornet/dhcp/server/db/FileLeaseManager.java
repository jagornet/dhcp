package com.jagornet.dhcp.server.db;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.Range;

public class FileLeaseManager extends LeaseManager {

	private static final Path LEASES_DIR = Paths.get(DhcpConstants.JAGORNET_DHCP_HOME +
			File.separatorChar + "db/fileleases");
	
	private static IpFileChannels ipFileChannels = IpFileChannels.getInstance();
	
	@Override
	public void init() throws Exception {
		if (!Files.isDirectory(LEASES_DIR, new LinkOption[] {})) {
			Files.createDirectories(LEASES_DIR);
		}

	}

	@Override
	public void updateIpAddress(final InetAddress inetAddr, 
								final byte state, final short prefixlen,
								final Date start, final Date preferred, final Date valid) {
		
		DhcpLease lease = load(inetAddr);
		if (lease != null) {
			lease.setState(state);
			if (prefixlen > 0) {
				lease.setPrefixLength(prefixlen);
			}
			lease.setStartTime(start);
			lease.setPreferredEndTime(preferred);
			lease.setValidEndTime(valid);
			store(lease);
		}
	}

	@Override
	public void deleteIpAddress(InetAddress inetAddr) {
		remove(inetAddr);
	}

	@Override
	public List<InetAddress> findExistingLeaseIPs(InetAddress startAddr, InetAddress endAddr) {
		try {
			DirectoryStream<Path> ipFiles =
					Files.newDirectoryStream(LEASES_DIR, new DirectoryStream.Filter<Path>() {
			    public boolean accept(Path file) throws IOException {
		            InetAddress ip = buildIpFromFilename(file.getFileName().toString());
		            if (ip != null) {
		            	return (Util.inclusiveBetween(ip, startAddr, endAddr));
		            }
		            return false;
			    }
			});
			List<InetAddress> inetAddrs = new ArrayList<InetAddress>();
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            if (ip != null) {
	            	inetAddrs.add(ip);
	            }
			}
			return inetAddrs;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<DhcpLease> findUnusedLeases(InetAddress startAddr, InetAddress endAddr) {
		List<DhcpLease> dhcpLeases = null;
		List<InetAddress> inetAddrs = findExistingIPs(startAddr, endAddr);
		if (inetAddrs != null) {
			long offerExpireMillis = 
					DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_OFFER_EXPIRATION);
				final long offerExpiration = new Date().getTime() - offerExpireMillis;
			dhcpLeases = new ArrayList<DhcpLease>();
			for (InetAddress inetAddr : inetAddrs) {
				DhcpLease lease = load(inetAddr);
				if (lease != null) {
					if ((lease.getState() == IaAddress.RELEASED) ||
							(lease.getState() == IaAddress.EXPIRED) ||
							((lease.getState() == IaAddress.ADVERTISED) && 
									(lease.getStartTime().getTime() <= offerExpiration))) {
						dhcpLeases.add(lease);
					}
				}
			}
		}
		return dhcpLeases;
	}

	@Override
	public void reconcileLeases(List<Range> ranges) {
		try {
			DirectoryStream<Path> ipFiles =
					Files.newDirectoryStream(LEASES_DIR, new DirectoryStream.Filter<Path>() {
			    public boolean accept(Path file) throws IOException {
		            InetAddress ip = buildIpFromFilename(file.getFileName().toString());
		            for (Range range : ranges) {
						if (Util.inclusiveBetween(ip, range.getStartAddress(), range.getEndAddress())) {
							// found this IP, so don't return it so it is not deleted
							return false;
						}
					}
		            return true;
			    }
			});
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            remove(ip);
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void deleteAllLeases() {
		try {
			DirectoryStream<Path> ipFiles = Files.newDirectoryStream(LEASES_DIR);
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            if (ip != null) {
	            	remove(ip);
	            }
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void insertDhcpLease(DhcpLease lease) {
		store(lease);
	}

	@Override
	protected void updateDhcpLease(DhcpLease lease) {
		store(lease);
	}

	@Override
	protected void deleteDhcpLease(DhcpLease lease) {
		remove(lease.getIpAddress());
	}

	@Override
	protected void updateIaOptions(InetAddress inetAddr, Collection<DhcpOption> iaOptions) {
		DhcpLease lease = load(inetAddr);
		if (lease != null) {
			lease.setIaDhcpOptions(iaOptions);
			store(lease);
		}
	}

	@Override
	protected void updateIpAddrOptions(InetAddress inetAddr, Collection<DhcpOption> ipAddrOptions) {
		DhcpLease lease = load(inetAddr);
		if (lease != null) {
			lease.setIaAddrDhcpOptions(ipAddrOptions);
			store(lease);
		}
	}

	@Override
	protected List<DhcpLease> findDhcpLeasesForIA(byte[] duid, byte iatype, long iaid) {
		//TODO: brute force is no way to do this
		List<DhcpLease> iaLeases = new ArrayList<DhcpLease>();
		try {
			DirectoryStream<Path> ipFiles = Files.newDirectoryStream(LEASES_DIR);
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            if (ip != null) {
	            	DhcpLease lease = load(ip);
	            	if (lease != null) {
	            		if (Arrays.equals(lease.getDuid(), duid) &&
	            				(lease.getIatype() == iatype) &&
	            				(lease.getIaid() == iaid)) {
	            			iaLeases.add(lease);
	            		}
	            	}
	            }
			}
			return iaLeases;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected DhcpLease findDhcpLeaseForInetAddr(InetAddress inetAddr) {
		return load(inetAddr);
	}

	@Override
	protected List<DhcpLease> findExpiredLeases(byte iatype) {
		//TODO: brute force is no way to do this
		List<DhcpLease> expiredLeases = new ArrayList<DhcpLease>();
		try {
			DirectoryStream<Path> ipFiles = Files.newDirectoryStream(LEASES_DIR);
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            if (ip != null) {
	            	DhcpLease lease = load(ip);
	            	if (lease != null) {
	            		if (lease.getIatype() == iatype) {
	            			Date validEndTime = lease.getValidEndTime();
	            			if (validEndTime != null) {
	            				if (validEndTime.getTime() > new Date().getTime()) {
	            					expiredLeases.add(lease);
	            				}
	            			}
	            		}
	            	}
	            }
			}
			return expiredLeases;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void store(DhcpLease lease) {
		try {
			AsynchronousFileChannel fc = getAsyncFileChannel(lease.getIpAddress());
			if (fc != null) {
				ByteBuffer buf = ByteBuffer.wrap(lease.toJson().getBytes());
				Future<Integer> future = fc.write(buf, 0);
				int wrote = future.get();
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DhcpLease load(InetAddress inetAddress) {
		try {
			AsynchronousFileChannel fc = getAsyncFileChannel(inetAddress);
			if (fc != null) {
				ByteBuffer buf = ByteBuffer.allocate(1024);
				Future<Integer> future = fc.read(buf, 0);
				int read = future.get();
				buf.rewind();
				byte[] data = buf.array();
				DhcpLease lease = DhcpLease.fromJson(new String(data));
				return lease;
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void remove(InetAddress inetAddress) {
		try {
			ipFileChannels.removeIpFileChannel(inetAddress);
			Path file = Paths.get(LEASES_DIR.toString() + File.separatorChar +
					buildFilenameFromIp(inetAddress));
			Files.delete(file);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static AsynchronousFileChannel getAsyncFileChannel(InetAddress inetAddress) throws IOException {
		AsynchronousFileChannel asyncFileChannel = ipFileChannels.getIpFileChannel(inetAddress);
		if (asyncFileChannel == null) {
			try {
				Path file = Paths.get(LEASES_DIR.toString() + File.separatorChar +
									buildFilenameFromIp(inetAddress));
				asyncFileChannel = AsynchronousFileChannel.open(file, 
						StandardOpenOption.CREATE,
						StandardOpenOption.READ,
						StandardOpenOption.WRITE);
				ipFileChannels.putIpFileChannel(inetAddress, asyncFileChannel);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
		}
		return asyncFileChannel;
	}

    public static String buildFilenameFromIp(InetAddress ip) {
		StringBuilder sb = new StringBuilder();
		if (ip instanceof Inet4Address) {
			String s = ip.getHostAddress();
			String[] octets = s.split("\\.");
			for (String str : octets) {
				if (sb.length() > 0) {
					sb.append('.');
				}
				if (str.length() < 3) {
					for (int i=str.length(); i<3; i++) {
						sb.append('0');
					}
				}
				sb.append(str);
			}
		}
		else {
			String s = ip.getHostAddress();
			s = s.replaceAll(":", ".");
			String[] pieces = s.split("\\.");
			for (String str : pieces) {
				if (sb.length() > 0) {
					sb.append('.');
				}
				if (str.length() < 4) {
					for (int i = str.length(); i<4; i++) {
						sb.append('0');
					}
				}
				sb.append(str);
			}
		}
		return sb.toString();
    }

    public static InetAddress buildIpFromFilename(String filename) {
    	InetAddress inetAddr = null;
		if (filename.lastIndexOf('.') == 34) {
			filename = filename.replace('.', ':');
		}
		try {
			inetAddr = InetAddress.getByName(filename);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return inetAddr;
    }
}
