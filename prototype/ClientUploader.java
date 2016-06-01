import java.io.*;
import java.lang.Math;
import java.util.*;
import java.nio.file.*;

class ClientUploader {

	public final static String MD5_CHECKSUM_FILENAME = "md5_checksum";
	public final static String ROLLING_CHECKSUM_FILENAME = "rolling_checksum";
	public final static String UPLOAD_FILENAME = "img.png";
	public final static int BLOCK_SIZE = 1024;

	// To be encapsulated in a class
	// public static long a = 0;
	// public static long b = 0;

	public static void main(String[] args) {
		Path path = FileSystems.getDefault().getPath(UPLOAD_FILENAME);
		BufferedInputStream dataStream;
		try {
			dataStream = new BufferedInputStream(new FileInputStream(UPLOAD_FILENAME));
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return;
		} 

		// Supposedly Rolling checksums received from client, each integer is one checksum for a block
		// Here we define consistently such that a block is 1024 bytes
		List<Long> rollingChecksums = getRollingChecksumList();

		// Begin rolling checksum process
		int currentIndex = 0;
		byte[] b = new byte[BLOCK_SIZE];
		int result = getByteFromBufferedIStream(dataStream, b, 0);
		long localRollingChecksum = adler32(b);
		
		for (long serverChecksum : rollingChecksums) {
			while (localRollingChecksum != serverChecksum && result != -1) {
				System.out.println("Block mismatch... " + localRollingChecksum + " vs. " + serverChecksum);
				System.out.println("Incrementing local block by 1");
				currentIndex++;
				result = getByteFromBufferedIStream(dataStream, b, 1);
			}
			if (result != -1) {
				System.out.println("Block match: " + localRollingChecksum);
				currentIndex += BLOCK_SIZE;
				result = getByteFromBufferedIStream(dataStream, b, 0);
				localRollingChecksum = adler32(b);
			}
		}

		// String string = new String(byte[] bytes, Charset charset);
	}

	private static long adler32(byte[] block) {
		int largePrime = 65521;
		long a = 0;
		long b = 0;
		for (int i = 0; i < block.length; i++) {
			a += (long) block[i];
			b += (block.length - i) * (long) block[i];
		}
		a = a % largePrime;
		b = b % largePrime;

		if ((long)(a + (b * Math.pow(2, 16))) < 0) 
			System.out.println(a + " " + b);

		return (long)(a + (b * Math.pow(2, 16)));
	}
	/*
	private static long adler32(byte[] oldBlock, byte nextByte) {
		int largePrime = 65521;
		a -= oldBlock[0];
		a += nextByte;
		b -= oldBlock.length * oldBlock[0];
		b += a;
		a = a % largePrime;
		b = b % largePrime;
		return (long)(a + (b * Math.pow(2, 16)));
	}
	*/
	private static int getByteFromBufferedIStream(BufferedInputStream stream, byte[] b, int skip) {
		int result = -1;
		int available = -1;
		try {
			available = stream.available();
			if (skip == 0) {
				result = stream.read(b);
			} else {
				byte[] skippedBytes = new byte[skip];
				stream.read(skippedBytes);
				result = stream.read(b);
			}
			
		} catch (IOException e) {
			System.out.println(e);
		} catch (IndexOutOfBoundsException e) {
			System.out.println(e);
			System.out.println(available);
		}
		return result;
	}

	private static List<Long> getRollingChecksumList() {
		// In actual implementation, this should be obtained from the response of initial request to server
		//   instead of read from a file.

		List<Long> rollingChecksums = new ArrayList<Long>();

		try (BufferedReader br = new BufferedReader(new FileReader(new File(ROLLING_CHECKSUM_FILENAME)))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	long b = Long.parseLong(line);
		    	rollingChecksums.add(b);
		    }
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

		return rollingChecksums;
	}
}