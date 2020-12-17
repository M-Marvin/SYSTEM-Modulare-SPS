
/*
 * comInterface.c
 * Communication Protokoll for Modular Raspberry SPS Modules
 *
 * Created: 11/16/2020 7:30:22 PM
 *  Author: Marvin
 * 
 * PIN CONFIG
 * Relays
 * PORTD 0-7 + PORTB 0-7
 * COM Serial OUT
 * PORTC 0-1
 * COM Serial IN
 * PORTC 2-3
 * PORTC Clock
 * PINC 4
 * PORTC Enable
 * PINC 5
 */ 

#include <avr/io.h>

class ICOM {
	
	const static int8_t PINC_CLCK = 0x10;
	const static int8_t PORTC_COM_1 = 0x1;
	const static int8_t PORTC_COM_2 = 0x2;
	const static int8_t PINC_COM_1 = 0x4;
	const static int8_t PINC_COM_2 = 0x8;
	const static int8_t PINC_ENABLE = 0x20;
	const static int MAX_TIMEOUT = 2000;
	
	bool doRead;
	int8_t recivedData[255] {0};
	int8_t recivedDataLength;
	int8_t remainingRecivePackets;
	int8_t byteBuffer;
	int8_t byteBufferPointer;

	bool requestSend;
	bool doSend;
	int8_t sendData[255] {0};
	int8_t sendDataLength;
	int8_t remainingSendPackets;
	int8_t byteBuffer2;
	int8_t byteBufferPointer2;
	
	bool hasHandledClckPulse;
	int8_t handleTimer;
	
	bool enableListening;
	
	public: ICOM () {
		
		reset();
		
		hasHandledClckPulse = false;
		handleTimer = 0;
		enableListening = false;
		
		DDRC = PORTC_COM_1 | PORTC_COM_2;
		
	}

	public: void update() {
		
		bool enable = (PINC & PINC_ENABLE) > 0;
		
		// Prevent listening to half message (on startup)
		if (!enableListening && !enable) enableListening = true;
		
		if (enable && enableListening)  {
			
			bool clckHigh = (PINC & PINC_CLCK) > 0;
			
			// Count up handleTimer when no clckPulses
			if (clckHigh && !hasHandledClckPulse) {
				handleTimer = 0;
			} else if (hasHandledClckPulse) {
				handleTimer++;
			}
			
			if (clckHigh && !hasHandledClckPulse) {
				
				hasHandledClckPulse = true;
				
				if (!isSending()) {
					
					// get recived Value
					bool recivedBit1 = (PINC & PINC_COM_1) > 0;
					bool recivedBit2 = (PINC & PINC_COM_2) > 0;
					byteBuffer |= (recivedBit1 << (byteBufferPointer + 0));
					byteBuffer |= (recivedBit2 << (byteBufferPointer + 1));
					byteBufferPointer += 2;
					
					if (byteBufferPointer >= 8) {
						
						int8_t recived = byteBuffer;
						byteBuffer = 0;
						byteBufferPointer = 0;
						
						if (remainingRecivePackets == 0 && recived > 0 && !isSending() && !isReading()) {
							
							// Set RemainingPackets to recived Value and Start Reading
							doRead = true;
							remainingRecivePackets = recived;
							recivedData[recived] = {0};
							recivedDataLength = recived;
							return;
							
						}
						
						if (isReading() && remainingRecivePackets > 0) {
							
							// Add recived Value to recivedPackets and count as Packet
							recivedData[recivedDataLength - remainingRecivePackets] = recived;
							remainingRecivePackets--;
							
							if (remainingRecivePackets <= 0) {
								
								doRead = false;
								
							}
							
						}
						
					}
					
				} else if (!isReading() && remainingSendPackets >= 0) {
					
					// Send Ports Low
					PORTC = (PORTC & ~PORTC_COM_1);
					PORTC = (PORTC & ~PORTC_COM_2);
					
					if (byteBufferPointer2 >= 8) {
						
						byteBuffer2 = sendData[sendDataLength - remainingSendPackets];
						byteBufferPointer2 = 0;
						
					}
					
					bool sendBit1 = (byteBuffer2 & (1 << (byteBufferPointer2 + 0)));
					bool sendBit2 = (byteBuffer2 & (1 << (byteBufferPointer2 + 1)));
					PORTC = (PORTC & ~PORTC_COM_1) | (sendBit1 ? PORTC_COM_1 : 0);
					PORTC = (PORTC & ~PORTC_COM_2) | (sendBit2 ? PORTC_COM_2 : 0);
					byteBufferPointer2 += 2;
							
					if (byteBufferPointer2 >= 8) remainingSendPackets--;
					
					if (remainingSendPackets < 0) {
						
						doSend = false;
						sendDataLength = 0;
						
					}
					
				}
				
			} else if (hasHandledClckPulse && !clckHigh) {
				
				// Start sending wehen reading complete
				if (requestSend && !doRead) {
					doSend = true;
					requestSend = false;
				}
				
				hasHandledClckPulse = false;
			}
			
			// Reset all when handleTimer > MAX_TIMEOUT
			if (handleTimer > MAX_TIMEOUT) {
				
				reset();
				handleTimer = 0;
				
			}
			
		}
		
	}
	
	public: void reset() {
		doRead = false;
		recivedData[255] = {0};
		recivedDataLength = 0;
		remainingRecivePackets = 0;
		byteBuffer = 0;
		byteBufferPointer = 0;
		
		requestSend = false;
		
		doSend = false;
		sendData[255] = {0};
		sendDataLength = 0;
		remainingSendPackets = 0;
		byteBuffer2 = 0;
		byteBufferPointer2 = 8;
	}
	
	public: int hasData()
	{
		if (recivedDataLength > 0 && !isReading()) {
			return recivedDataLength;
		} else {
			return 0;
		}
	}
	
	public: int8_t* getData()
	{
		recivedDataLength = 0;
		int8_t* data = recivedData;
		recivedData[255] = {0};
		return data;
	}

	public: void setSendData(int8_t* data, int8_t length)
	{
		sendData[length + 1] = {0};
		sendData[0] = length;
		for (int8_t i = 0; i < length; i++) sendData[i + 1] = data[i];
		remainingSendPackets = length;
		sendDataLength = length;
		byteBuffer2 = 0;
		byteBufferPointer2 = 8;
		
	}
	
	public: void startSending() {
		
		if (remainingSendPackets > 0 && !isSending()) requestSend = true;
		
	}
	
	public: bool isDataSend() {
		return sendDataLength == 0 && !isSending();
	}

	public: bool isSending() {
		return remainingSendPackets >= 0 && doSend;
	}
	
	public: bool isReading() {
		return remainingRecivePackets > 0 && doRead;
	}
	
};