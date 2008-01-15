package com.agr.dhcpv6.dto;

public class OpaqueDataDTO
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
