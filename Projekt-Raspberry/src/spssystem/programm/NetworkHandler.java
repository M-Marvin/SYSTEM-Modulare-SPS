package spssystem.programm;

import java.io.IOException;

import netcom.INetComHandler;
import netcom.NetCom;
import spssystem.DataPackage;

public class NetworkHandler extends INetComHandler {
	
	public NetworkHandler(NetCom network) {
		super(network);
	}
	
	@Override
	public void onDataRecived(String ip, int port, byte[] data) {
		
		DataPackage dataPkt = DataPackage.fromBytes(data);
		
		// Handle Recived Packages
		
	}
	
	public void sendPackage(String ip, int port, DataPackage pkt) {
		
		try {

			// Send Packages
			
			this.sendData(ip, port, pkt.toBytes());
			
		} catch (IOException e) {
			System.err.println("Error on Convert DataPakage to Bytes!");
			System.err.println(e.getMessage());
		}
		
	}
	
}
