package com.jagornet.dhcp.server.rest.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.JagornetDhcpServer;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.ha.HaBackupFSM;
import com.jagornet.dhcp.server.ha.HaPrimaryFSM;

public class DhcpServerStatusService {
	
	private static Logger log = LoggerFactory.getLogger(DhcpServerStatusService.class);

	public static final String STATUS_OK = "all-systems-go";
	public static final String SYNCING_TO_PEER = "syncing-to-peer";

    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();

	public String getStatus() {
		return STATUS_OK;
	}
	
	public String haPeerGetStatus() {
		return getStatus();
	}
	
	public String getHaState() {
		if (JagornetDhcpServer.haPrimaryFSM != null) {
			return getHaPrimaryState(JagornetDhcpServer.haPrimaryFSM);
		}
		else if (JagornetDhcpServer.haBackupFSM != null) {
			return getHaBackupState(JagornetDhcpServer.haBackupFSM);
		}
		return "HA not configured";
	}
	
	public String haPeerGetHaState() {
		if (JagornetDhcpServer.haPrimaryFSM != null) {
			HaPrimaryFSM haPrimaryFSM = JagornetDhcpServer.haPrimaryFSM;
			// if we are primary, and we get an HA state request
			// from the backup, then the backup is available
			log.info("HA Primary received HA state request from Backup, " + 
					 "assuming that Backup is polling");
			haPrimaryFSM.setBackupState(HaBackupFSM.State.BACKUP_POLLING);
			return getHaPrimaryState(haPrimaryFSM);
		}
		else if (JagornetDhcpServer.haBackupFSM != null) {
			HaBackupFSM haBackupFSM = JagornetDhcpServer.haBackupFSM;
			// if we are backup, and we get an HA state request
			// from the primary, then assume the primary is running
			// which means we should "cease and desist" and keep polling
			log.info("HA Backup received HA state request from Primary, " + 
					 "assuming that Primary is running");
			haBackupFSM.setState(HaBackupFSM.State.BACKUP_POLLING);
			haBackupFSM.setPrimaryState(HaPrimaryFSM.State.PRIMARY_RUNNING);
			return getHaBackupState(haBackupFSM);
		}
		return "HA not configured";
	}
	
	public void setHaState(String state) {
		if (SYNCING_TO_PEER.equalsIgnoreCase(state)) {
			if (JagornetDhcpServer.haPrimaryFSM != null) {
				// don't change "my" state
				// dhcpServerConfig.getHaPrimaryFSM().setState(HaPrimaryFSM.State.PRIMARY_SYNCING_TO_BACKUP);
				// change state of backup in this primary's FSM
				JagornetDhcpServer.haPrimaryFSM.setBackupState(
						HaBackupFSM.State.BACKUP_SYNCING_FROM_PRIMARY);
			}
			else if (JagornetDhcpServer.haBackupFSM != null) {
				// don't change "my" state
				// dhcpServerConfig.getHaBackupFSM().setState(HaBackupFSM.State.BACKUP_SYNCING_TO_PRIMARY);
				// change state of primary in this backup's FSM
				JagornetDhcpServer.haBackupFSM.setPrimaryState(
						HaPrimaryFSM.State.PRIMARY_SYNCING_FROM_BACKUP);
			}
			else {
				log.error("HA not configured");
			}
		}
		else {
			log.warn("Ignoring HA state change: " + state);
		}
	}
	
	private String getHaPrimaryState(HaPrimaryFSM haPrimaryFSM) {
		return haPrimaryFSM.getState().toString();
	}
	
	private String getHaBackupState(HaBackupFSM haBackupFSM) {
		return haBackupFSM.getState().toString();
	}

}
