package spssystem.programm;

import spssystem.SPSSystem;
import spssystem.modules.DO_16_R230;

public class Programm {
	
	public DO_16_R230 output1;
	public boolean b1;
	
	public void onStart() {
		
		// Wenn SPS Eingeschaltet (Extern per SPSSystem.start())
		this.output1 = new DO_16_R230((byte) 1, 10);
		
	}
	
	public void onStop() {
		
		// Wenn SPSSystem.stop()
		
	}
	
	public void run() {
		
		System.out.println("Run");
		//SPSSystem.getSystem().stop();
		
		if (SPSSystem.getSystem().getSystemClock() % 10 == 0) {
			b1 = !b1;
			output1.setOutput(2, b1);
			System.out.println("Output 2 = " + b1);
		}
		
	}
	
}
