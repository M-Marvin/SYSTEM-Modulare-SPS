package spssystem;

public class IOSystem {
	
	protected boolean clockPulseState;
	protected byte[] inputList;
	protected byte[] inputBytes;
	protected byte[] outputList;
	protected byte[] outputBytes;
	
	public IOSystem() {
		this.inputBytes = new byte[0];
		this.inputList = new byte[0];
		this.outputBytes = new byte[0];
		this.outputList = new byte[0];
	}

	public void registerInputRange(byte adress, byte byteWidth, int requestTime) {
		
		inputList = ByteUtility.mergeData(inputList, new byte[] {adress, byteWidth}, ByteUtility.intToBytes(requestTime));
		inputBytes = ByteUtility.mergeData(this.inputBytes, new byte[byteWidth]);
		
	}
	
	public void registerOutputRange(byte adress, byte byteWidth, int updateTime) {
		
		outputList = ByteUtility.mergeData(outputList, new byte[] {adress, byteWidth}, ByteUtility.intToBytes(updateTime));
		outputBytes = ByteUtility.mergeData(this.outputBytes, new byte[byteWidth]);
		
	}
	
	public void setOutputByte(byte adr, int targetByte, byte ioByte) {

		int ioByteOffset = 0;
		for (int modCnt = 0; modCnt < outputList.length; modCnt += 6) {
			
			byte adress = outputList[modCnt + 0];
			byte byteWidth = outputList[modCnt + 1];
			
			if (adress == adr && targetByte < byteWidth) {
				
				this.outputBytes[ioByteOffset + targetByte] = ioByte;
				
			}
			
			ioByteOffset += byteWidth;
			
		}
				
	}
	
	public byte getOutputByte(byte adr, int targetByte) {
		
		int ioByteOffset = 0;
		for (int modCnt = 0; modCnt < outputList.length; modCnt += 6) {
			
			byte adress = outputList[modCnt + 0];
			byte byteWidth = outputList[modCnt + 1];
			
			if (adress == adr && targetByte < byteWidth) {
				
				return this.outputBytes[ioByteOffset + targetByte];
				
			}
			
			ioByteOffset += byteWidth;
			
		}
		
		return 0;
		
	}

	public void setInputByte(byte adr, int targetByte, byte ioByte) {

		int ioByteOffset = 0;
		for (int modCnt = 0; modCnt < inputList.length; modCnt += 6) {
			
			byte adress = inputList[modCnt + 0];
			byte byteWidth = inputList[modCnt + 1];
			
			if (adress == adr && targetByte < byteWidth) {
				
				this.inputBytes[ioByteOffset + targetByte] = ioByte;
				
			}
			
			ioByteOffset += byteWidth;
			
		}
				
	}
	
	public byte getInputByte(byte adr, int targetByte) {
		
		int ioByteOffset = 0;
		for (int modCnt = 0; modCnt < inputList.length; modCnt += 6) {
			
			byte adress = inputList[modCnt + 0];
			byte byteWidth = inputList[modCnt + 1];
			
			if (adress == adr && targetByte < byteWidth) {
				
				return this.inputBytes[ioByteOffset + targetByte];
				
			}
			
			ioByteOffset += byteWidth;
			
		}
		
		return 0;
		
	}
	
	public void readInputs() {

		long sysClock = SPSSystem.getSystem().getSystemClock();
		int ioByteOffset = 0;
		
		for (int modCnt = 0; modCnt < inputList.length; modCnt += 6) {
			
			byte adress = inputList[modCnt + 0];
			byte byteWidth = inputList[modCnt + 1];
			int requestTime = ByteUtility.bytesToInt(ByteUtility.copyArr(inputList, modCnt + 2, modCnt + 5));
			
			if (sysClock % requestTime == 0) {
				
				byte[] data = readModul(adress, byteWidth);
				ByteUtility.replaceBytes(this.inputBytes, ioByteOffset, data);
				
			}
			
			ioByteOffset += byteWidth;
			
		}
		
	}
	
	public void writeOutputs() {
		
		long sysClock = SPSSystem.getSystem().getSystemClock();
		int ioByteOffset = 0;
		
		for (int modCnt = 0; modCnt < outputList.length; modCnt += 6) {
			
			byte adress = outputList[modCnt + 0];
			byte byteWidth = outputList[modCnt + 1];
			int requestTime = ByteUtility.bytesToInt(ByteUtility.copyArr(outputList, modCnt + 2, modCnt + 5));
			
			if (sysClock % requestTime == 0) {
				
				byte[] data = ByteUtility.copyArr(this.outputBytes, ioByteOffset, ioByteOffset + byteWidth - 1);
				writeModul(adress, data);
				
			}
			
			ioByteOffset += byteWidth;
			
		}
		
	}
	
	protected byte[] readModul(byte adress, byte byteWidth) {
		
		sendByte(adress);
		return readBytes(byteWidth);
				
	}
	
	protected void writeModul(byte adress, byte[] data) {
		
		sendByte(adress);
		sendByte((byte) data.length);
		for (int bc = 0; bc < (byte) data.length; bc++) sendByte(data[bc]);
		
	}
	
	protected short byteParser;
	
	protected void sendByte(byte b) {
		
		byteParser = 0;
		while (byteParser < 8) {
			
			clockPulseState = !clockPulseState;
			
			boolean gpio1, gpio2, gpio3;
			
			if (clockPulseState) {
				
				gpio1 = true;
				gpio2 = (b & (1 << byteParser)) > 0;
				gpio3 = (b & (1 << (byteParser + 1))) > 0;
				byteParser += 2;
				
			} else {
				
				gpio1 = gpio2 = gpio3 = false;
				
			}
			
			System.out.println(gpio1 + " " + gpio2 + " " + gpio3);
			// TODO SET GPIO
			
		}
		
	}
	
	protected byte[] readBytes(byte bytes) {
		
		byte[] data = new byte[bytes];
		byte currentByte = 0;
		
		byteParser = 0;
		while (byteParser < bytes * 8) {
			
			// TODO SET GPIO (1)
			
			clockPulseState = !clockPulseState;
			
			if (clockPulseState) {
				
				// TODO READ GPIO
				boolean gpio4 = true;
				boolean gpio5 = true;
				
				currentByte |= ((gpio4 ? 1 : 0) << (byteParser % 8));
				currentByte |= ((gpio5 ? 1 : 0) << ((byteParser + 1) % 8));
				
				byteParser += 2;
				
				if (byteParser % 8 == 0) {
					data[byteParser / 8] = currentByte;
					currentByte = 0;
				}
				
			}
			
		}
		
		return data;
		
	}
	
}
