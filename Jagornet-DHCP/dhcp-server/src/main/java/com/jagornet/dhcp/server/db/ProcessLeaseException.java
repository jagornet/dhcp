package com.jagornet.dhcp.server.db;

public class ProcessLeaseException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProcessLeaseException() {
        super();
    }

    public ProcessLeaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessLeaseException(String message) {
        super(message);
    }

    public ProcessLeaseException(Throwable cause) {
        super(cause);
    }

}
