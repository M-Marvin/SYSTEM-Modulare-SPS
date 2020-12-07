package netcom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map.Entry;

public class NetCom {
	
	public static final int MAX_DATA_LENGTH = 65535;
	
	protected ServerSocket serverSocket;
	protected Thread serverAcceptor;
	
	protected HashMap<Thread, Socket> connetedClients;
	protected INetComHandler handler;
	
	public NetCom() {
		
			this.connetedClients = new HashMap<Thread, Socket>();

			System.out.println("NetCom started!");
		
	}
	
	public void openConnectServer(int port) {
		
		try {

			this.serverSocket = new ServerSocket(port);
			this.serverAcceptor = new Thread(() -> {
				
				while (!serverSocket.isClosed()) {
					
					try {
						
						Socket clientSocket = serverSocket.accept();
						
						this.addClient(clientSocket);
						
					} catch (IOException e) {
						System.err.println("Error on Accept Client!");
						e.printStackTrace();
					}
					
				}
				
			});
			this.serverAcceptor.setName("ServerAcceptor");
			
			this.serverAcceptor.start();
			
		} catch (IOException e) {
			System.err.println("Can not Instanziate ServerInstance!");
			System.err.println(e.getMessage());
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void closeServer() {
		
		try {
			
			if (this.serverSocket != null) {
				this.serverSocket.close();
				this.serverAcceptor.stop();
			}
			
			for (Socket clientSocket : this.connetedClients.values()) {
				this.disconnectFromClient(clientSocket);
			}
			
		} catch (IOException e) {
			System.err.println("Error by disconecting from Client!");
			e.printStackTrace();
		}
				
	}
	
	@SuppressWarnings("unchecked")
	public <T extends INetComHandler> T createHandler(INetHandlerFactory<T> netHandlerFactory) {
		return (T) (this.handler = netHandlerFactory.get(this));
	}
	
	@FunctionalInterface
	public interface INetHandlerFactory<T> {
		T get(NetCom network);
	}
	
	protected void addClient(Socket clientSocket) {
		
		Thread clientHandler = new Thread(() -> {
			
			Socket socket = connetedClients.get(Thread.currentThread());
			
			while (!clientSocket.isClosed()) {
				
				try {
					
					if (socket.getInputStream().available() > 2) {
						
						byte[] lengthBytes = new byte[2];
						socket.getInputStream().read(lengthBytes, 0, 2);
						
						int length = ((lengthBytes[0]) | (lengthBytes[1] << 8));
						
						if (length > 0) {
							
							byte[] data = new byte[length];
							socket.getInputStream().read(data, 0, length);
							
							handler.onDataRecived(socket.getInetAddress().getHostAddress(), socket.getPort(), data);
							
						}
						
					}
					
					if (!socket.isConnected() || socket.isClosed()) {
						
						disconnectFromClient(socket);
						
					}
					
				} catch (IOException e) {
					System.err.println("Error on read recived Data from Client " + clientSocket.getInetAddress().getHostName() + " !");
					System.err.println(e.getMessage());
				}
				
			}

			this.connetedClients.remove(Thread.currentThread());
			System.out.println("Client " + clientSocket.getInetAddress().getHostName() + " disconnected!");
			
		});
		clientHandler.setName("ClientReader_" + clientSocket.getInetAddress().getHostName());
		
		this.connetedClients.put(clientHandler, clientSocket);
		clientHandler.start();
		
		System.out.println("Client " + clientSocket.getInetAddress().getHostName() + " connected!");
		
	}
	
	public void sendToClient(String ip, int port, byte[] data) {
		
		for (Socket clientSocket : this.connetedClients.values()) {
			
			if (clientSocket.getInetAddress().getHostAddress().equals(ip) && clientSocket.getPort() == port) {
				
				sendToClient(clientSocket, data);
				
			}
			
		}
		
	}
	
	public void sendToClient(Socket clientSocket, byte[] data) {
		
		try {

			int length = data.length;
			
			if (length < MAX_DATA_LENGTH) {
				
				byte b1 = (byte) length;
				byte b2 = (byte) (length >> 8);
				
				byte[] bytes = new byte[2 + length];
				
				for (int i = 0; i < length; i++) {
					bytes[2 + i] = data[i];
				}
											
				bytes[0] = b1;
				bytes[1] = b2;
				
				clientSocket.getOutputStream().write(bytes);
				
			}
			
		} catch (SocketException e) {
			System.err.println("Connection interrupted to " + clientSocket.getInetAddress().getHostName() + " !");
			System.err.println(e.getMessage());
			disconnectFromClient(clientSocket);
		} catch (IOException e) {
			System.err.println("Error on send Data to Client " + clientSocket.getInetAddress().getHostName() + " !");
			System.err.println(e.getMessage());
		}
		
	}
	
	@SuppressWarnings("resource")
	public boolean connectToClient(String ip, int port) {
		
		try {
			
			Socket clientSocket = new Socket(ip, port);
			
			if (clientSocket.isConnected()) {
				
				addClient(clientSocket);
				return true;
				
			}
			
		} catch (IOException e) {
			System.err.println("Failed to connect with " + ip + " at Port " + port + "!");
			System.err.println(e.getMessage());
		}
		
		return false;
		
	}

	public void disconnectFromClient(String ip, int port) {
		
		for (Socket clientSocket : this.connetedClients.values()) {
			
			if (clientSocket.getInetAddress().getHostAddress().equals(ip) && clientSocket.getPort() == port) {
				
				disconnectFromClient(clientSocket);
				
			}
			
		}
		
	}
	
	public boolean disconnectFromClient(Socket clientSocket) {
		
		if (isConnectedToClient(clientSocket)) {
			
			try {
				
				clientSocket.close();
				
				for (Entry<Thread, Socket> entry : this.connetedClients.entrySet()) {
					
					if (entry.getValue() == clientSocket) {

						this.connetedClients.remove(entry.getKey());
						
					}
					
				}
				
				System.out.println("Client " + clientSocket.getInetAddress().getHostName() + " disconnected!");
				
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
			
		}
		
		return true;
		
	}

	public boolean isConnectedToClient(String ip, int port) {
		
		for (Socket clientSocket : this.connetedClients.values()) {
			
			if (clientSocket.getInetAddress().getHostAddress().equals(ip) && clientSocket.getPort() == port) {
				
				return isConnectedToClient(clientSocket);
				
			}
			
		}
		
		return false;
		
	}
	
	public boolean isConnectedToClient(Socket socket) {
		
		for (Entry<Thread, Socket> entry : this.connetedClients.entrySet()) {
			
			if (entry.getValue().equals(socket)) return true;
			
		}
		
		return false;
		
	}

	public Socket[] getConnetedClients() {
		return connetedClients.values().toArray(new Socket[] {});
	}
	
}
