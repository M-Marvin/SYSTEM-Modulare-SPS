package netcom;

public abstract class INetComHandler {
	
	protected NetCom network;
	
	public INetComHandler(NetCom network) {
		this.network = network;
	}
	
	public abstract void onDataRecived(String ip, int port, byte[] data);
	
	public void sendData(String ip, int port, byte[] data) {
		
		this.network.sendToClient(ip, port, data);
		
	}
	
}
