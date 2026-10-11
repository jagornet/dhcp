/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.ha.HaBackupFSM;
import com.jagornet.dhcp.server.ha.HaClient;
import com.jagornet.dhcp.server.ha.HaPrimaryFSM;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusService;

/**
 * Test implementation of {@link HaClient} for High Availability unit and integration tests.
 * 
 * @author A. Gregory Rabil
 */
public class TestHaClient implements HaClient {

	private volatile String status = DhcpServerStatusService.STATUS_OK;
	private volatile HaPrimaryFSM.State primaryHaState = HaPrimaryFSM.State.PRIMARY_RUNNING;
	private volatile HaBackupFSM.State backupHaState = HaBackupFSM.State.BACKUP_POLLING;

	private final List<DhcpLease> receivedLeaseUpdates = Collections.synchronizedList(new ArrayList<>());
	private final List<DhcpLease> receivedAsyncLeaseUpdates = Collections.synchronizedList(new ArrayList<>());

	private final AtomicInteger getStatusCallCount = new AtomicInteger();
	private final AtomicInteger getPrimaryStateCallCount = new AtomicInteger();
	private final AtomicInteger getBackupStateCallCount = new AtomicInteger();
	private final AtomicInteger updateLeaseCallCount = new AtomicInteger();

	private volatile boolean simulateSyncFailure = false;

	@Override
	public String getStatus() {
		getStatusCallCount.incrementAndGet();
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public HaPrimaryFSM.State getPrimaryHaState() {
		getPrimaryStateCallCount.incrementAndGet();
		return primaryHaState;
	}

	public void setPrimaryHaState(HaPrimaryFSM.State primaryHaState) {
		this.primaryHaState = primaryHaState;
	}

	@Override
	public HaBackupFSM.State getBackupHaState() {
		getBackupStateCallCount.incrementAndGet();
		return backupHaState;
	}

	public void setBackupHaState(HaBackupFSM.State backupHaState) {
		this.backupHaState = backupHaState;
	}

	@Override
	public DhcpLease updateDhcpLease(DhcpLease dhcpLease) {
		updateLeaseCallCount.incrementAndGet();
		receivedLeaseUpdates.add(dhcpLease);
		if (simulateSyncFailure) {
			return null;
		}
		DhcpLease response = dhcpLease.clone();
		response.setHaPeerState(response.getState());
		return response;
	}

	@Override
	public void updateDhcpLeaseAsync(DhcpLease dhcpLease, DhcpLease expectedDhcpLease) {
		updateLeaseCallCount.incrementAndGet();
		receivedAsyncLeaseUpdates.add(dhcpLease);
	}

	@Override
	public Runnable buildLinkSyncThread(DhcpLink dhcpLink, CountDownLatch linkSyncLatch, boolean unsyncedLeasesOnly) {
		return () -> {
			try {
				dhcpLink.setState(DhcpLink.State.SYNCING);
				if (!simulateSyncFailure) {
					dhcpLink.setState(DhcpLink.State.OK);
				}
			} finally {
				if (linkSyncLatch != null) {
					linkSyncLatch.countDown();
				}
			}
		};
	}

	public List<DhcpLease> getReceivedLeaseUpdates() {
		return Collections.unmodifiableList(receivedLeaseUpdates);
	}

	public List<DhcpLease> getReceivedAsyncLeaseUpdates() {
		return Collections.unmodifiableList(receivedAsyncLeaseUpdates);
	}

	public int getGetStatusCallCount() {
		return getStatusCallCount.get();
	}

	public int getUpdateLeaseCallCount() {
		return updateLeaseCallCount.get();
	}

	public void setSimulateSyncFailure(boolean simulateSyncFailure) {
		this.simulateSyncFailure = simulateSyncFailure;
	}

	public void clear() {
		receivedLeaseUpdates.clear();
		receivedAsyncLeaseUpdates.clear();
		getStatusCallCount.set(0);
		getPrimaryStateCallCount.set(0);
		getBackupStateCallCount.set(0);
		updateLeaseCallCount.set(0);
	}
}
