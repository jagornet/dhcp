/*
 * Copyright (c) 2004 Diamond IP Technologies, Inc.
 * All international rights reserved. Unauthorized use prohibited.
 *
 * $Log: $
 * 
 */

package com.agr.dhcpv6.server;

import java.net.Inet6Address;

/**
 * <p>Title: DhcpRelayMessage </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpRelayMessage extends DhcpMessage
{
    private byte relayMessageType = 0;
    private byte hopCount = 0;
    private Inet6Address linkAddress = null;
    private Inet6Address peerAddress = null;
    
    public byte getHopCount()
    {
        return hopCount;
    }
    public void setHopCount(byte hopCount)
    {
        this.hopCount = hopCount;
    }
    public Inet6Address getLinkAddress()
    {
        return linkAddress;
    }
    public void setLinkAddress(Inet6Address linkAddress)
    {
        this.linkAddress = linkAddress;
    }
    public Inet6Address getPeerAddress()
    {
        return peerAddress;
    }
    public void setPeerAddress(Inet6Address peerAddress)
    {
        this.peerAddress = peerAddress;
    }
    public byte getRelayMessageType()
    {
        return relayMessageType;
    }
    public void setRelayMessageType(byte relayMessageType)
    {
        this.relayMessageType = relayMessageType;
    }
}
