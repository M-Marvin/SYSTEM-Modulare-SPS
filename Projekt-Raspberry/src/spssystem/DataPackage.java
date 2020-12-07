package spssystem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class DataPackage {
	
	protected HashMap<String, Object> dataList;
	
	public DataPackage() {
		this.dataList = new HashMap<String, Object>();
	}
	
	public <T> void putData(String key, Object data) {
		this.dataList.put(key, data);
	}
	
	public Object getData(String key) {
		return this.dataList.get(key);
	}
	
	public byte[] toBytes() throws IOException {
		
		byte[] bytes = new byte[0];
		
		for (Entry<String, Object> entry : this.dataList.entrySet()) {
			
			byte[] keyBytes = entry.getKey().getBytes();
			if (keyBytes.length > Byte.MAX_VALUE) throw new IOException("Key-Name to long, max Byte-Size " + Byte.MAX_VALUE);
			keyBytes = ByteUtility.mergeData(new byte[] {(byte) keyBytes.length}, keyBytes);
			byte[] dataBytes = null;
			
			Object data = entry.getValue();
			if (data instanceof String) {
				dataBytes = ((String) data).getBytes();
				if (dataBytes.length > Byte.MAX_VALUE) throw new IOException("String to long, max Byte-Size " + Byte.MAX_VALUE);
				dataBytes = ByteUtility.mergeData(new byte[] {1, (byte) dataBytes.length}, dataBytes);
			} else if (data instanceof Boolean) {
				dataBytes = new byte[1];
				dataBytes[0] = (byte) (((Boolean) data) ? 1 : 0);
				dataBytes = ByteUtility.mergeData(new byte[] {2, 1}, dataBytes);
			} else if (data instanceof Integer) {
				int i = (int) data;
				dataBytes = new byte[Integer.BYTES];
				for (int ib = 0; ib < Integer.BYTES; ib++) {
					dataBytes[ib] = (byte) (i >> (ib  * 8));
				}
				dataBytes = ByteUtility.mergeData(new byte[] {3, Integer.BYTES}, dataBytes);
			}
			
			if (dataBytes != null) {
				
				byte[] bytePart = ByteUtility.mergeData(keyBytes, dataBytes);
				bytes = ByteUtility.mergeData(bytes, bytePart);
				
			}
			
		}
		
		return bytes;
		
	}
	
	public static DataPackage fromBytes(byte[] bytes) {
		
		DataPackage dataPkt = new DataPackage();
		
		for (int i1 = 0; i1 < bytes.length; i1++) {
			
			int keyLength = bytes[i1];
			
			byte[] keyBytes = new byte[keyLength];
			for (int i2 = 0; i2 < keyLength; i2++) {
				keyBytes[i2] = bytes[i1 + i2 + 1];
			}
			String key = new String(keyBytes);
			
			i1 += keyLength + 1;
			
			int dataType = bytes[i1];
			int dataLength = bytes[i1 + 1];
			
			byte[] dataBytes = new byte[dataLength];
			for (int i3 = 0; i3 < dataLength; i3++) {
				dataBytes[i3] = bytes[i1 + i3 + 2];
			}
			
			Object data = null;
			if (dataType == 1) {
				data = new String(dataBytes);
			} else if (dataType == 2) {
				data = dataBytes[0] == 1;
			} else if (dataType == 3) {
				int i = 0;
				for (int ib = 0; ib < dataLength; ib++) {
					i |= dataBytes[ib] << (ib * 8);
				}
				data = i;
			}
			
			i1 += dataLength + 2;
			
			dataPkt.putData(key, data);
			
		}
		
		return dataPkt;
		
	}
	
}
