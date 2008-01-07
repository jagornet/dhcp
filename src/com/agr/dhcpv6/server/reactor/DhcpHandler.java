package com.agr.dhcpv6.server.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

final class DhcpHandler implements Runnable {
    final protected SocketChannel socket;
    final protected SelectionKey sk;
    protected ByteBuffer input = ByteBuffer.allocate(1024);
    protected ByteBuffer output = ByteBuffer.allocate(1024);
    protected static final int READING = 0, SENDING = 1;
    protected int state = READING;
    
    public DhcpHandler(Selector sel, SocketChannel c)
            throws IOException {
        socket = c; 
        socket.configureBlocking(false);
        // Optionally try first read now
        sk = socket.register(sel, 0);
        sk.attach(this);
        sk.interestOps(SelectionKey.OP_READ);
        sel.wakeup();
    }

    boolean inputIsComplete() { return true; }
    boolean outputIsComplete() { return true; }
    void process() { /* ... */ }
    
    public void run() {
        try {
            if (state == READING) 
                read();
            else if (state == SENDING) 
                send();
        } catch (IOException ex) { /* ... */ }
    }
    
    void read() throws IOException {
        socket.read(input);
        if (inputIsComplete()) {
            process();
            state = SENDING;
            // Normally also do first write now
            sk.interestOps(SelectionKey.OP_WRITE);
        }
    }
    void send() throws IOException {
        socket.write(output);
        if (outputIsComplete()) 
            sk.cancel();
    }
}    