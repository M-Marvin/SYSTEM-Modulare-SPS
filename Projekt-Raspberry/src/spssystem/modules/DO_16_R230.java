package spssystem.modules;

import spssystem.IOModule;

public class DO_16_R230 extends IOModule {

	public DO_16_R230(byte adress, int updateTime) {
		super(adress);
		io.registerOutputRange(adress, (byte) 2, updateTime);
	}
	
	public void setOutput(int index, boolean state) {
		int targetByte = (index - 1) / 8;
		byte ioByte = io.getOutputByte(adress, targetByte);
		byte setByte = (byte) (1 << (index - 1) % 8);
		
		if (state) {
			ioByte |= setByte;
		} else {
			ioByte &= ~setByte;
		}
		
		io.setOutputByte(adress, targetByte, ioByte);
	}
	
}
