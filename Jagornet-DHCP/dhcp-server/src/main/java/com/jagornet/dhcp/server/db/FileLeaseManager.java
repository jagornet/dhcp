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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.request.binding.Range;

public class FileLeaseManager extends LeaseManager {

	private static Logger log = LoggerFactory.getLogger(FileLeaseManager.class);
	
	protected static final Path LEASES_DIR = Paths.get(DhcpConstants.JAGORNET_DHCP_HOME +
			File.separatorChar + "db/fileleases");
	
	protected IpFileChannels ipFileChannels;
	
	@Override
	public void init() throws Exception {
		super.init();
		if (!Files.isDirectory(LEASES_DIR, new LinkOption[] {})) {
			Files.createDirectories(LEASES_DIR);
		}
		//TODO: expose this as a Property
		ipFileChannels = new IpFileChannels(1000);
	}

	@Override
	public int updateIpAddress(final InetAddress inetAddr, 
								final byte state, final byte haPeerState, final short prefixlen,
								final Date start, final Date preferred, final Date valid) {
		
		int cnt = 0;
		DhcpLease lease = load(inetAddr);
		if (lease != null) {
			lease.setState(state);
			lease.setHaPeerState(haPeerState);
			if (prefixlen > 0) {
				lease.setPrefixLength(prefixlen);
			}
			lease.setStartTime(start);
			lease.setPreferredEndTime(preferred);
			lease.setValidEndTime(valid);
			cnt = store(lease);
		}
		return cnt;
	}

	@Override
	public int deleteIpAddress(InetAddress inetAddr) {
		remove(inetAddr);
		return 1;
	}

	@Override
	public List<InetAddress> findExistingLeaseIPs(InetAddress startAddr, InetAddress endAddr) {
		try {
//			log.info("findExistingLeaseIPs in dir=" + LEASES_DIR);
			DirectoryStream<Path> ipFiles =
					Files.newDirectoryStream(LEASES_DIR, new DirectoryStream.Filter<Path>() {
			    public boolean accept(Path file) throws IOException {
		            InetAddress ip = buildIpFromFilename(file.getFileName().toString());
		            if (ip != null) {
//		            	log.debug("findExistingLeaseIPs.ipFiles.accept.ip=" + ip.getHostAddress());
		            	return (Util.inclusiveBetween(ip, startAddr, endAddr));
		            }
//	            	log.debug("findExistingLeaseIPs.ipFiles.accept.ip=null");
		            return false;
			    }
			});
			List<InetAddress> inetAddrs = new ArrayList<InetAddress>();
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            if (ip != null) {
//	            	log.debug("findExistingLeaseIPs.ipFiles.ip=" + ip.getHostAddress());
	            	inetAddrs.add(ip);
	            }
	            else {
//	            	log.debug("findExistingLeaseIPs.ipFiles.ip=null");
	            }
			}
//			log.info("Found " + inetAddrs.size() + " inetAddrs");
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
			final long offerExpiration = new Date().getTime() - offerExpireMillis;
//			log.debug("Checking " + inetAddrs.size() + " leases for offerExpriation=" + new Date(offerExpiration));
			dhcpLeases = new ArrayList<DhcpLease>();
			for (InetAddress inetAddr : inetAddrs) {
				DhcpLease lease = load(inetAddr);
				if (lease != null) {
//					log.debug("Checking lease: " + lease);
					if ((lease.getState() == IaAddress.AVAILABLE) ||
						((lease.getState() == IaAddress.OFFERED) && 
						(lease.getStartTime().getTime() <= offerExpiration))) {
//						log.debug("Found unused lease: " + lease);
						dhcpLeases.add(lease);
					}
				}
			}
		}
		return dhcpLeases;
	}

	@Override
	public DhcpLease findUnusedLease(InetAddress startAddr, InetAddress endAddr) {
		DhcpLease dhcpLease = null;
		List<InetAddress> inetAddrs = findExistingIPs(startAddr, endAddr);
		if (inetAddrs != null) {
			final long offerExpiration = new Date().getTime() - offerExpireMillis;
//			log.debug("Checking " + inetAddrs.size() + " leases for offerExpriation=" + new Date(offerExpiration));
			for (InetAddress inetAddr : inetAddrs) {
				DhcpLease lease = load(inetAddr);
				if (lease != null) {
//					log.debug("Checking lease: " + lease);
					if ((lease.getState() == IaAddress.AVAILABLE) ||
						((lease.getState() == IaAddress.OFFERED) && 
						(lease.getStartTime().getTime() <= offerExpiration))) {
//						log.debug("Found unused lease: " + lease);
						dhcpLease = lease;
						break;
					}
				}
			}
		}
		return dhcpLease;
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
	public int deleteAllLeases() {
		int cnt = 0;
		try {
			DirectoryStream<Path> ipFiles = Files.newDirectoryStream(LEASES_DIR);
			for (Path ipFile : ipFiles) {
	            InetAddress ip = buildIpFromFilename(ipFile.getFileName().toString());
	            if (ip != null) {
	            	remove(ip);
	            	cnt++;
	            }
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cnt;
	}

	@Override
	public int insertDhcpLease(DhcpLease lease) {
		return store(lease);
	}

	@Override
	public int updateDhcpLease(DhcpLease lease) {
		return store(lease);
	}

	@Override
	public int deleteDhcpLease(DhcpLease lease) {
		remove(lease.getIpAddress());
		return 1;
	}

	@Override
	public int updateIaOptions(InetAddress inetAddr, Collection<DhcpOption> iaOptions) {
		int cnt = 0;
		DhcpLease lease = load(inetAddr);
		if (lease != null) {
			lease.setIaDhcpOptions(iaOptions);
			cnt = store(lease);
		}
		return cnt;
	}

	@Override
	public int updateIpAddrOptions(InetAddress inetAddr, Collection<DhcpOption> ipAddrOptions) {
		int cnt = 0;
		DhcpLease lease = load(inetAddr);
		if (lease != null) {
			lease.setIaAddrDhcpOptions(ipAddrOptions);
			cnt = store(lease);
		}
		return cnt;
	}

	@Override
	public List<DhcpLease> findDhcpLeasesForIA(byte[] duid, byte iatype, long iaid) {
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
	public DhcpLease findDhcpLeaseForInetAddr(InetAddress inetAddr) {
		return load(inetAddr);
	}

	@Override
	public List<DhcpLease> findExpiredLeases(byte iatype) {
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
	            				if (validEndTime.getTime() < new Date().getTime()) {
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
	
	public int store(DhcpLease lease) {
		try {
			AsynchronousFileChannel fc = getAsyncFileChannel(lease.getIpAddress());
			if (fc != null) {
				ByteBuffer buf = ByteBuffer.wrap(lease.toJson().getBytes());
				Future<Integer> future = fc.write(buf, 0);
				int wrote = future.get();
				fc.truncate(wrote);
				return wrote;
			}
			else {
				log.error("Could not get AsynchronousFileChannel for IP=" + 
							lease.getIpAddress().getHostAddress());
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public DhcpLease load(InetAddress inetAddress) {
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
			else {
				log.error("Could not get AsynchronousFileChannel for IP=" + 
							inetAddress.getHostAddress());
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void remove(InetAddress inetAddress) {
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
	
	public AsynchronousFileChannel getAsyncFileChannel(InetAddress inetAddress) throws IOException {
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
