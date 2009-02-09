package com.jagornet.dhcpv6.dto;

public class OpaqueDataOptionDTO extends BaseOptionDTO
{
	protected OpaqueDataDTO data;

	public OpaqueDataDTO getData() {
		return data;
	}

	public void setData(OpaqueDataDTO opaqueData) {
		this.data = opaqueData;
	}
	
	// provide the necessary getter/setter
	// accessor methods to support DTO mapping
    public String getAsciiValue()
    {
    	if (data != null)
    		return data.getAsciiValue();
    	
    	return null;
    }
    public void setAsciiValue(String asciiValue)
    {
    	if (data == null)
    		data = new OpaqueDataDTO();
    	
    	data.setAsciiValue(asciiValue);
    	data.setHexValue(null);	// it can't be both
    }
    public byte[] getHexValue()
    {
    	if (data != null)
    		return data.getHexValue();
    	
        return null;
    }
    public void setHexValue(byte[] hexValue)
    {
    	if (data == null)
    		data = new OpaqueDataDTO();
    	
        data.setHexValue(hexValue);
        data.setAsciiValue(null);	// it can't be both
    }
}
