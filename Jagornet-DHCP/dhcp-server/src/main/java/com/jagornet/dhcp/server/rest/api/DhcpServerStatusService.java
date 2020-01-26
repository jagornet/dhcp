package com.jagornet.dhcp.server.rest.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.ha.HaBackupFSM;
import com.jagornet.dhcp.server.ha.HaPrimaryFSM;

public class DhcpServerStatusService {
	
	private static Logger log = LoggerFactory.getLogger(DhcpServerStatusService.class);

	public static final String STATUS_OK = "Kickin' ass!";
	public static final String SYNCING_TO_PEER = "syncing-to-peer";

    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();

	public String getStatus() {
		return STATUS_OK;
	}
	
	public String getHaState() {
		if (dhcpServerConfig.getHaPrimaryFSM() != null) {
			// if we are primary, and we get an HA state request
			// from the backup, then the backup is available
			log.info("HA Primary received HA state request from Backup, " + 
					 "assuming that Backup is polling");
			dhcpServerConfig.getHaPrimaryFSM().setBackupState(HaBackupFSM.State.BACKUP_POLLING);
			return dhcpServerConfig.getHaPrimaryFSM().getState().toString();
		}
		else if (dhcpServerConfig.getHaBackupFSM() != null) {
			// if we are backup, and we get an HA state request
			// from the primary, then assume the primary is running
			log.info("HA Backup received HA state request from Primary, " + 
					 "assuming that Primary is running");
			dhcpServerConfig.getHaBackupFSM().setPrimaryState(HaPrimaryFSM.State.PRIMARY_RUNNING);
			return dhcpServerConfig.getHaBackupFSM().getState().toString();
		}
		else {
			return "HA not configured";
		}
	}
	
	public void setHaState(String state) {
		if (SYNCING_TO_PEER.equalsIgnoreCase(state)) {
			if (dhcpServerConfig.getHaPrimaryFSM() != null) {
				// don't change "my" state
				// dhcpServerConfig.getHaPrimaryFSM().setState(HaPrimaryFSM.State.PRIMARY_SYNCING_TO_BACKUP);
				// change state of backup in this primary's FSM
				dhcpServerConfig.getHaPrimaryFSM().setBackupState(
						HaBackupFSM.State.BACKUP_SYNCING_FROM_PRIMARY);
			}
			else if (dhcpServerConfig.getHaBackupFSM() != null) {
				// don't change "my" state
				// dhcpServerConfig.getHaBackupFSM().setState(HaBackupFSM.State.BACKUP_SYNCING_TO_PRIMARY);
				// change state of primary in this backup's FSM
				dhcpServerConfig.getHaBackupFSM().setPrimaryState(
						HaPrimaryFSM.State.PRIMARY_SYNCING_FROM_BACKUP);
			}
			else {
				// TOdo: log error
			}
		}
		else {
			// TOdo: log error
		}
	}

}
