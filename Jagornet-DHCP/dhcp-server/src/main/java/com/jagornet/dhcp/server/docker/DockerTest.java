package com.jagornet.dhcp.server.docker;

public class DockerTest implements Runnable {

	
	public static void main(String[] args) {
		DockerTest dt = new DockerTest();
		dt.run();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException ex) {
				
			}
		}
		
	}

}
