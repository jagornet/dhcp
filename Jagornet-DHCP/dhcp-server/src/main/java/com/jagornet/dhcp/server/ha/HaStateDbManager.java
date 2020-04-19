package com.jagornet.dhcp.server.ha;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

/**
 * HaStateDbManager - manager class for the High Availability state database file
 * @author agrabil
 *
 */
public class HaStateDbManager {

	private static Logger log = LoggerFactory.getLogger(HaStateDbManager.class);

	private File stateDbFile;
	private HaStateDb haStateDb;
	private int maxStoredStates;
	private Gson gson;
	private HaState currentHaState;
	
	public HaStateDbManager() {
		stateDbFile = new File(DhcpServerPolicies.globalPolicy(Property.HA_DATABASE_FILE));
		if (!stateDbFile.isAbsolute()) {
			stateDbFile = new File(DhcpConstants.JAGORNET_DHCP_HOME, stateDbFile.getPath());
		}
		maxStoredStates = DhcpServerPolicies.globalPolicyAsInt(Property.HA_DATABASE_MAX_STORED_STATES);
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	// for testing
	public void deleteStateDbFile() throws Exception {
		stateDbFile.delete();
	}
	
	public HaState init(DhcpServerConfiguration.HaRole haRole) throws Exception {
		boolean isBackup = haRole.equals(DhcpServerConfiguration.HaRole.BACKUP);
		HaState myLastSavedHaState = null;
		log.info("Initializing HA state DB: " + stateDbFile);
		haStateDb = new HaStateDb();
		haStateDb.haRole = haRole;
		haStateDb.haStates = new ArrayList<HaState>();
		if (!stateDbFile.exists()) {
			File haDbDir = stateDbFile.getParentFile();
			if (!haDbDir.exists()) {
				haDbDir.mkdirs();
			}
			stateDbFile.createNewFile();	// create if not exists
		}
		else {
			JsonReader jsonReader = null;
			try {
				jsonReader = new JsonReader(new FileReader(stateDbFile));
				if (stateDbFile.length() > 0) {
					HaState haState = null;
					jsonReader.beginArray();
					while (jsonReader.hasNext()) {
						haState = gson.fromJson(jsonReader, HaState.class);
						log.debug("Read HA state: " + haState);
						if (haState != null) {
							haStateDb.haStates.add(haState);
							if (!isBackup && isPrimaryState(haState.state)) {
								myLastSavedHaState = haState;
							}
							else if (isBackup && isBackupState(haState.state)) {
								myLastSavedHaState = haState;
							}
						}
					}
					jsonReader.endArray();
					jsonReader.close();
				}
			}
			finally {
				try {
					if (jsonReader != null) {
						jsonReader.close();
					}
				}
				catch (IOException ex) {
					log.error("Failed to close jsonReader: " + ex);
				}
			}
		}
		if (myLastSavedHaState == null) {
			log.info("No saved HA states");
			if (!isBackup) {
				currentHaState = updatePrimaryState(HaPrimaryFSM.State.PRIMARY_INIT);
			}
			else {
				currentHaState = updateBackupState(HaBackupFSM.State.BACKUP_INIT);
			}
		}
		else {
			log.info("Last saved HA state: " + myLastSavedHaState);
			currentHaState = myLastSavedHaState;
		}
		return currentHaState;
	}
	
	public boolean isPrimaryState(String state) {
		for (HaPrimaryFSM.State s : HaPrimaryFSM.State.values()) {
			if (s.name().equals(state)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBackupState(String state) {
		for (HaBackupFSM.State s : HaBackupFSM.State.values()) {
			if (s.name().equals(state)) {
				return true;
			}
		}
		return false;
	}
	
	public HaState updatePrimaryState(HaPrimaryFSM.State primaryState) throws Exception {
		HaState haState = new HaState();
		haState.timestamp = new Date();
		haState.state = primaryState.toString();
		log.info("Update primary state: " + haState);
		haStateDb.haStates.add(haState);
		writeHaStateDb();
		return haState;
	}
	
	public HaState updateBackupState(HaBackupFSM.State backupState) throws Exception {
		HaState haState = new HaState();
		haState.timestamp = new Date();
		haState.state = backupState.toString();
		log.info("Update backup state: " + haState);
		haStateDb.haStates.add(haState);
		writeHaStateDb();
		return haState;
	}
	
	private void writeHaStateDb() throws Exception {
		if (haStateDb.haStates.size() > maxStoredStates) {
			haStateDb.haStates = haStateDb.haStates.subList(0, maxStoredStates);
		}
		JsonWriter jsonWriter = null;
		try {
			jsonWriter = new JsonWriter(new FileWriter(stateDbFile));
			jsonWriter.setIndent("  ");
			jsonWriter.beginArray();
			for (HaState haState : haStateDb.haStates) {
				gson.toJson(haState, HaState.class, jsonWriter);
			}
			jsonWriter.endArray();
			jsonWriter.flush();
		}
		finally {
			try {
				if (jsonWriter != null) {
					jsonWriter.close();
				}
			}
			catch (IOException ex) {
				log.error("Failed to close jsonWriter: " + ex);
			}
		}
	}
}
