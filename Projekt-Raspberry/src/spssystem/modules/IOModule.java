package spssystem.modules;

import spssystem.IOSystem;
import spssystem.SPSSystem;

public abstract class IOModule {
	
	protected byte adress;
	protected IOSystem io;
	
	public IOModule(byte adress) {
		this.adress = adress;
		this.io = SPSSystem.getSystem().getIOSystem();
	}
	
}
