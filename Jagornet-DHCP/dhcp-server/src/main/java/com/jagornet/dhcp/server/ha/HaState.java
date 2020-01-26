package com.jagornet.dhcp.server.ha;

import java.util.Date;

public class HaState {

	public String state;
	public Date timestamp;
	
	@Override
	public String toString() {
		return "Timestamp=" + timestamp + 
				" state=" + state;
	}
}
