import java.io.*;
import java.lang.Math;
import java.util.*;
import java.nio.Buffer;
import java.nio.file.*;

class ClientUploader {

	public final static String MD5_CHECKSUM_FILENAME = "md5_checksum";
	public final static String ROLLING_CHECKSUM_FILENAME = "rolling_checksum";
	public final static String UPLOAD_FILENAME = "sm_img.jpeg";
	public final static int BLOCK_SIZE = 1024;

	// To be encapsulated in a class
	// public static long a = 0;
	// public static long b = 0;

	public static void main(String[] args) throws IOException{
		Path path = FileSystems.getDefault().getPath(UPLOAD_FILENAME);
		// FileOutputStream fos = new FileOutputStream("raw_bytefile_java");
		PrintWriter fos = new PrintWriter("rolling_checksum_java", "UTF-8");
		BufferedInputStream dataStream;
		try {
			dataStream = new BufferedInputStream(new FileInputStream(UPLOAD_FILENAME));
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return;
		}

		// Supposedly Rolling checksums received from client, each integer is one checksum for a block
		// Here we define consistently such that a block is 1024 bytes
		List<Long> getRollingChecksumList = getRollingChecksumList();

		// Begin rolling checksum process
		int currentIndex = 0;
		byte[] b = new byte[BLOCK_SIZE];

		int bytesRead;
		int count = 0;
		while ((bytesRead = dataStream.read(b)) != -1) {
			long hash = adler32(b, bytesRead, count++);
			fos.write(String.valueOf(hash) + "\n");
			// System.out.println(hash);
		}
		fos.close();
		return;
		/*
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
		*/
		// String string = new String(byte[] bytes, Charset charset);
	}

	private static long adler32(byte[] block, int block_size, int block_number) throws FileNotFoundException, UnsupportedEncodingException {
		String filename = "adler_" + block_number + "_java";
		PrintWriter fos = new PrintWriter(filename, "UTF-8");

		int largePrime = 65521;
		long a = 0;
		long b = 0;
		for (int i = 0; i < block_size; i++) {
			String output = String.format("%d %d\n", a, b);
			fos.write(output);
			a += (long) block[i];
			b += (block_size - i) * (long) block[i];
		}
		fos.close();
		//System.out.println(String.format("BEFORE MOD %d %d", a, b));
		a = Math.floorMod(a, largePrime);
		b = Math.floorMod(b, largePrime);

		System.out.println(String.format("AFTER MOD %d %d", a, b));

		return (long)(a + (b * Math.pow(2, 16)));
	}

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