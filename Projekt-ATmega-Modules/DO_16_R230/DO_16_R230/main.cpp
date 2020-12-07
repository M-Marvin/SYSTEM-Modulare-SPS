/*
 * main.c
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

#include <xc.h>
#include <comInterface.c>

int main(void)
{
	ICOM COM;
	
	
	while(1)
    {
		
		COM.update(void);
				
    }
	
}