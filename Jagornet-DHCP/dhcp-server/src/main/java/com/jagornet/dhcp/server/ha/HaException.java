package com.jagornet.dhcp.server.ha;

public class HaException extends Exception {
    
    public HaException(Exception ex) {
        super(ex);
    }

    public HaException(String msg) {
        super(msg);
    }

    public HaException(String msg, Exception ex) {
        super(msg, ex);
    }
}
