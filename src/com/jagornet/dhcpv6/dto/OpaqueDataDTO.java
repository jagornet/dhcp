package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

public class OpaqueDataDTO implements Serializable
{
    protected String asciiValue;
    protected byte[] hexValue;

    public String getAsciiValue()
    {
        return asciiValue;
    }
    public void setAsciiValue(String asciiValue)
    {
        this.asciiValue = asciiValue;
    }
    public byte[] getHexValue()
    {
        return hexValue;
    }
    public void setHexValue(byte[] hexValue)
    {
        this.hexValue = hexValue;
    }
}
