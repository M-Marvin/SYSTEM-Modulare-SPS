package spssystem;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class IOSystem {
	
	protected boolean clockPulseState;
	protected byte[] inputList;
	protected byte[] inputBytes;
	protected byte[] outputList;
	protected byte[] outputBytes;
	
	protected GpioController gpio;
	protected GpioPinDigitalOutput outputEnable;
	protected GpioPinDigitalOutput outputSync;
	protected GpioPinDigitalOutput outputCom1;
	protected GpioPinDigitalOutput outputCom2;
	protected GpioPinDigitalInput inputCom1;
	protected GpioPinDigitalInput inputCom2;
	
	public IOSystem() {
		this.inputBytes = new byte[0];
		this.inputList = new byte[0];
		this.outputBytes = new byte[0];
		this.outputList = new byte[0];
		
		try {
			gpio = GpioFactory.getInstance();
			outputEnable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "enable", PinState.LOW);
			outputEnable.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
			outputSync = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08, "sync", PinState.LOW);
			outputSync.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
			outputCom1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_09, "oCom1", PinState.LOW);
			outputCom1.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
			outputCom2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "oCom2", PinState.LOW);
			outputCom2.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
			inputCom1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, "iCom1");
			inputCom1.setPullResistance(PinPullResistance.PULL_DOWN);
			inputCom2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "iCom2");
			inputCom2.setPullResistance(PinPullResistance.PULL_DOWN);
		} catch (UnsatisfiedLinkError e) {
			System.err.println("SPSSystem cant connect to wiringPi netive Lib!");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}
	
	public void shutdown() {
		this.gpio.shutdown();
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
	
	protected void waitTick() {
		try {
			Thread.sleep(0, 100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readInputs() {

		setLow();
		outputEnable.high();
		
		long sysClock = SPSSystem.getSystem().getSystemClock();
		int ioByteOffset = 0;
		
		for (int modCnt = 0; modCnt < inputList.length; modCnt += 6) {
			
			byte adress = inputList[modCnt + 0];
			byte byteWidth = inputList[modCnt + 1];
			int requestTime = ByteUtility.bytesToInt(ByteUtility.copyArr(inputList, modCnt + 2, modCnt + 5));
			
			if (sysClock % requestTime == 0) {
				
				waitTick();
				
				byte[] data = readModul(adress);
				
				if (data.length >= byteWidth) {
					
					byte[] data2 = ByteUtility.copyArr(data, 0, byteWidth);
					ByteUtility.replaceBytes(this.inputBytes, ioByteOffset, data2);
					
				}
				
			}
			
			ioByteOffset += byteWidth;
			
		}

		setLow();
		outputEnable.low();
		
	}
	
	public void writeOutputs() {
		
		setLow();
		outputEnable.high();
		
		long sysClock = SPSSystem.getSystem().getSystemClock();
		int ioByteOffset = 0;
		
		for (int modCnt = 0; modCnt < outputList.length; modCnt += 6) {
			
			byte adress = outputList[modCnt + 0];
			byte byteWidth = outputList[modCnt + 1];
			int requestTime = ByteUtility.bytesToInt(ByteUtility.copyArr(outputList, modCnt + 2, modCnt + 5));
			
			if (sysClock % requestTime == 0) {

				waitTick();
				
				byte[] data = ByteUtility.copyArr(this.outputBytes, ioByteOffset, ioByteOffset + byteWidth - 1);
				writeModul(adress, data);
				
			}
			
			ioByteOffset += byteWidth;
			
		}

		setLow();
		outputEnable.low();
		
	}
	
	public byte[] writeAdressConfig(byte adress, byte masterAdress) {
		
		setLow();
		outputEnable.high();
		
		byte[] data = new byte[] {adress};
		writeModul(masterAdress, data);
		byte[] response = readBytes();
		
		setLow();
		outputEnable.low();
		
		return response;
		
	}
	
	protected void setLow() {

		outputSync.low();
		outputCom1.low();
		outputCom2.low();
		
	}
	
	protected byte[] readModul(byte adress) {
		
		sendByte((byte) 1);
		sendByte(adress);
		return readBytes();
				
	}
	
	protected void writeModul(byte adress, byte[] data) {
		
		sendByte((byte) (data.length + 1));
		sendByte(adress);
		for (int bc = 0; bc < (byte) data.length; bc++) sendByte(data[bc]);
		
	}
	
	protected short byteParser;
	
	protected void sendByte(byte b) {
		
		boolean 
		gpio1 = false, 
		gpio2 = false, 
		gpio3 = false;
		
		byteParser = 0;
		while (byteParser <= 6 || gpio1) {
			
			clockPulseState = !clockPulseState;
			
			if (clockPulseState) {
				
				gpio1 = true;
				gpio2 = (b & (1 << byteParser)) > 0;
				gpio3 = (b & (1 << (byteParser + 1))) > 0;
				byteParser += 2;
				
			} else {
				
				gpio1 = false;
				
			}
			
			outputSync.setState(!gpio1);
			outputCom1.setState(gpio2);
			outputCom2.setState(gpio3);
			
			waitTick();
			
		}
		clockPulseState = false;
		outputSync.setState(clockPulseState);
		waitTick();
		
	}
	
	protected byte[] readBytes() {
		
		setLow();
		
		byte length = -1;
		byte[] data = null;
		byte currentByte = 0;
		
		byteParser = 0;
		while (byteParser / 8 < length || length == -1) {
			
			outputSync.setState(clockPulseState);
			
			clockPulseState = !clockPulseState;
			
			if (!clockPulseState) {
				
				boolean gpio4 = inputCom1.getState() == PinState.HIGH;
				boolean gpio5 = inputCom2.getState() == PinState.HIGH;
				
				currentByte |= ((gpio4 ? 1 : 0) << (byteParser % 8));
				currentByte |= ((gpio5 ? 1 : 0) << ((byteParser + 1) % 8));
				
				byteParser += 2;
				
				if (byteParser % 8 == 0) {
					
					if (length == -1) {
						length = currentByte;
						byteParser = 0;
						data = new byte[length];
					} else if (byteParser / 8 < data.length){
						data[byteParser / 8] = currentByte;
						System.out.println("recive " + currentByte);
					}
					currentByte = 0;
					
				}
				
			}

			waitTick();
			
		}
		
		clockPulseState = false;
		outputSync.setState(clockPulseState);
		waitTick();
		
		return data;
		
	}
	
}
