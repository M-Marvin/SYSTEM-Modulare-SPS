/*
 * comInterface.c
 *
 * Created: 11/16/2020 7:30:22 PM
 *  Author: Marvin
 * 
 * PIN CONFIG
 * Relays
 * PORTD 0-7 + PORTC 0-3
 * COM Serial OUT
 * PORTB 0-2
 * COM Serial IN
 * PINB 3-5
 * COM Clock
 * PINB 6
 * 
 * Free
 * PB 7 + PC 4-5
 *
 */ 

public static class ICOM {
	
	int recivedData[];
	int recivedDataLength = 0;
	int remainingRecivePackets = 0;

	int sendData[];
	int sendDataLength = 0;
	int remainingSendPackets = 0;
	bool hasSendStartByte = false;

	bool hasHandledClckPulse = false;

	public void update(void)
	{
		
		bool clckHigh = (PINB & 0x40) > 0;
		
		if (clckHigh && !hasHandledClckPulse) {
			
			hasHandledClckPulse = true;
			
			// get recived Value
			int recived = (PINB >> 2) & 0x7;
			
			if (remainingRecivePackets == 0) {
				
				// Set RemainingPackets to recived Value
				remainingRecivePackets = recived;
				recivedData = int[recived];
				recivedDataLength = recived;
				
				} else {
				
				// Add recived Value to recivedPackets and count as Packet
				recivedData[recivedData - remainingRecivePackets] = recived;
				remainingRecivePackets--;
				
			}
			
			// set send Value
			if (remainingSendPackets > 0) {
				
				if (!hasSendStartByte) {
					
					// send Length of Data as first Packet
					hasSendStartByte = true;
					int send = sendDataLength;
					
					int portState = PORTB;
					portState = (portState | send) & (send | 0xF8);
					PORTB = portState;
					
					} else {
					
					// send Packet
					int send = sendData[recivedDataLength - remainingRecivePackets];
					remainingSendPackets--;
					
					int portState = PORTB;
					portState = (portState | send) & (send | 0xF8);
					PORTB = portState;
					
				}
				
			}
			
			} else if (hasHandledClckPulse && !clckHigh) {
			hasHandledClckPulse = false;
		}
		
	}

	public bool hasData()
	{
		return remainingRecivePackets == 0 && recivedDataLength > 0;
	}

	public int[] getData()
	{
		recivedDataLength = 0;
		return recivedData;
	}

	public void sendData(int[] data)
	{
		sendData = data;
		remainingSendPackets = *(&data + 1) - data;
		sendDataLength = *(&data + 1) - data;
	}

	public bool isSending() {
		return remainingSendPackets > 0;
	}

};