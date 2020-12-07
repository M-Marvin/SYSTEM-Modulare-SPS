package spssystem;

import netcom.NetCom;
import spssystem.programm.NetworkHandler;
import spssystem.programm.Programm;

public class SPSSystem {
	
	public static final int SPS_PORT = 25565;
	
	private static SPSSystem instance;
	
	public NetCom network;
	public NetworkHandler networkHandler;
	public IOSystem ioSystem;
	public long systemClock;
	public Programm prog;
	
	protected boolean isRunning;
	
	public static void main(String[] args) {
		
		new SPSSystem().start();
		
	}
	
	public static SPSSystem getSystem() {
		return instance;
	}
	
	public NetworkHandler getNetworkHandler() {
		return networkHandler;
	}
	
	public long getSystemClock() {
		return systemClock;
	}
	
	public IOSystem getIOSystem() {
		return ioSystem;
	}
	
	public void start() {
		
		instance = this;
		
		this.network = new NetCom();
		this.network.openConnectServer(SPS_PORT);
		this.networkHandler = network.createHandler(NetworkHandler::new);
		
		this.ioSystem = new IOSystem();
		
		this.prog = new Programm();
		this.prog.onStart();
		
		isRunning = true;
		while (isRunning) {
			
			this.systemClock++;
			if (this.systemClock > Long.MAX_VALUE) this.systemClock = 0;
			
			// Core EVA handling
			this.ioSystem.readInputs();
			this.prog.run();
			this.ioSystem.writeOutputs();
			
			try {
				// Clock Speed
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		this.prog.onStop();
		
		this.network.closeServer();
		
	}
	
	public void stop() {
		
		this.isRunning = false;
		
	}
		
}
