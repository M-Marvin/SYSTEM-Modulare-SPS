package spssystem.programm;

import spssystem.SPSSystem;
import spssystem.modules.DO_16_R230;

public class Programm {
	
	public DO_16_R230 output1;
	
	public void onStart() {
		
		// Wenn SPS Eingeschaltet (Extern per SPSSystem.start())
		this.output1 = new DO_16_R230((byte) 1, 2);
		
	}
	
	public void onStop() {
		
		// Wenn SPSSystem.stop()
		
	}
	
	public int i;
	
	public void run() {
		
		i++;
		if (i > 200) SPSSystem.getSystem().stop();
		
		if (SPSSystem.getSystem().getSystemClock() % 300 == 0) {
			
			i++;
			
		}
		
		output1.setOutput(i, true);
		
	}
	
}
