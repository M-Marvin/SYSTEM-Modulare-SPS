/*
 * CPPFile1.cpp
 * Modul System fpr DO_16_R230
 *
 * Created: 11.12.2020 16:06:42
 *  Author: marvi
 */ 

#include <avr/io.h>
#include <avr/eeprom.h>
#include "comInterface.cpp"

class System {
	
	// Default-Adress to programm the adress in memory;
	const static int8_t MASTER_ADRESS = 0x0;
	
	// Time to set the adress, after RESET
	const static long BOOT_LOCK_TIME = 5000;
	
	// Address of the module!
	int8_t address = 1;
	
	// Timer since the module has be started!
	int bootTimer = 0;
	
	public: void start() {
		
		DDRD = 0xFF;
		DDRB = 0xFF;
		
		ICOM com;
		
		loadAdress();
		
		while(1)
		{
			
			int8_t dataLength = com.hasData();
			
			if (dataLength > 0) {
				
				int8_t* data = com.getData();
				
				if (data[0] == address && bootTimer == BOOT_LOCK_TIME) {
					
					if (dataLength == 1) {
						
						int8_t portStates[2] = {0};
							
							portStates[0] = PORTC;
							portStates[1] = PORTC;
						
							com.setSendData(portStates, 2);
							com.startSending();
							
						} else if (dataLength == 3) {
						
						PORTD = data[0];
						PORTB = data[1];
						
					}
					
				} else if (data[0] == MASTER_ADRESS && bootTimer < BOOT_LOCK_TIME) {
					
					if (dataLength == 2) {
						
						setAdress(data[1]);
						int8_t confirmMsg[2] = {0};
						confirmMsg[0] = MASTER_ADRESS;
						confirmMsg[1] = address;
						com.setSendData(confirmMsg, 2);
						com.startSending();
						
					}
					
				}
				
			}
			
			if (bootTimer < BOOT_LOCK_TIME) bootTimer++;
			
			com.update();
			
		}
		
	}
	
	public: void setAdress(int8_t adress) {
		uint8_t data = (uint8_t) adress;
		eeprom_update_byte((uint8_t *) 0x0, data);
		loadAdress();
	}
	
	public: void loadAdress() {
		uint8_t data = eeprom_read_byte((uint8_t *) 0x0);
		address = (int8_t) data;
	}
	
};