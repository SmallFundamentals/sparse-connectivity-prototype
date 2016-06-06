package client_uploader;

import java.io.*;
import java.lang.Math;
import java.nio.Buffer;
import java.nio.file.*;
import java.security.*;
import java.util.*;

// http://commons.apache.org/proper/commons-codec/download_codec.cgi
import org.apache.commons.codec.binary.Hex;

class ClientUploader {

	public final static String MD5_CHECKSUM_FILENAME = "md5_checksum";
	public final static String ROLLING_CHECKSUM_FILENAME = "rolling_checksum";
	public final static String UPLOAD_FILENAME = "sm_img.jpeg";
	public final static int BLOCK_SIZE = 1024;

	public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, DigestException {
		Path path = FileSystems.getDefault().getPath(UPLOAD_FILENAME);
		PrintWriter fos = new PrintWriter("rolling_checksum_java", "UTF-8");
		PrintWriter mdfos = new PrintWriter("md5_checksum_java", "UTF-8");
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
		int bytesRead;

		Adler32 adler = new Adler32();
		MessageDigest md = MessageDigest.getInstance("MD5");

		while ((bytesRead = dataStream.read(b)) != -1) {
			long hash = adler.calc(b, bytesRead);
			md.update(b, 0, bytesRead);
			byte[] md5 = md.digest();
			fos.write(String.valueOf(hash) + "\n");
			mdfos.write(Hex.encodeHexString(md5) + "\n");
			md.reset();
		}

		/*
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
		*/
		fos.close();
		mdfos.close();
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
