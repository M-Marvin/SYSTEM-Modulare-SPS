package spssystem;

public class ByteUtility {
	
	public static byte[] mergeData(byte[]... data) {
		
		int length = 0;
		for (byte[] arr : data) length += arr.length;
		
		byte[] bytes = new byte[length];
		int index = 0;
		for (byte[] arr : data) {
			for (byte b : arr) bytes[index++] = b;
		}
		
		return bytes;
		
	}
	
	public static byte[] intToBytes(int i) {
		byte[] bytes = new byte[Integer.BYTES];
		for (int ib = 0; ib < Integer.BYTES; ib++) {
			bytes[ib] = (byte) (i >> (ib * 8));
		}
		return bytes;
	}
	
	public static int bytesToInt(byte[] bytes) {
		int bc = Math.min(bytes.length, Integer.BYTES);
		int i = 0;
		for (int ib = 0; ib < bc; ib++) {
			i |= (bytes[ib] << (ib * 8));
		}
		return i;
	}

	public static byte[] copyArr(byte[] byteArr, int from, int to) {
		
		int max = Math.min(byteArr.length, to);
		byte[] copyArr = new byte[to - from + 1];
		for (int bc = from; bc <= max; bc++) {
			copyArr[bc - from] = byteArr[bc];
		}
		return copyArr;
		
	}

	public static byte[] replaceBytes(byte[] byteArr, int arrOffset, byte[] data) {
		
		for (int bc = 0; bc < data.length; bc++) {
			byteArr[bc + arrOffset] = data[bc];
		}
		return byteArr;
		
	}
	
}
