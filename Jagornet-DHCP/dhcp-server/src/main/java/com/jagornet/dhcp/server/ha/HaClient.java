/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.ha;

import java.util.concurrent.CountDownLatch;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.db.DhcpLease;

/**
 * The client-side interface for HA operations
 */
public interface HaClient {
    
    public static enum HaProtocol { REST, GRPC };

    /**
     * Get the status of the peer server as a string.
     * 
     * @return DhcpServerStatusService.STATUS_OK
     */
    public String getStatus();
    
    /**
     * Get the HA state of the primary server.
     */
    public HaPrimaryFSM.State getPrimaryHaState();
    
    /**
     * Get the HA state of the backup server.
     */
    public HaBackupFSM.State getBackupHaState();

    public DhcpLease updateDhcpLease(DhcpLease dhcpLease);

    public void updateDhcpLeaseAsync(DhcpLease dhcpLease, 
                                     DhcpLease expectedDhcpLease);

    public Runnable buildLinkSyncThread(DhcpLink dhcpLink, 
                                        CountDownLatch linkSyncLatch, 
                                        boolean unsyncedLeasesOnly);
}
