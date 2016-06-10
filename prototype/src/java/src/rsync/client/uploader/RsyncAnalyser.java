package rsync.client.uploader;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.security.*;
import java.util.*;

public class RsyncAnalyser {

    private BufferedInputStream dataStream;
    private Adler32 adler;
    private MessageDigest md;

    public RsyncAnalyser() throws NoSuchAlgorithmException {
        this.adler = new Adler32();
        this.md = MessageDigest.getInstance("MD5");
    }

    public void update(BufferedInputStream dataStream) {
        this.dataStream = dataStream;
    }

    /**
     * Given two set of hashes for a supposedly possibly incomplete remote version of a file (specified by update(BufferedInputStream)),
     * generate instructions for the remote machine to recreate the file.
     *
     * @param rollingChecksums      a List of Adler-32 checksums calculated for the remote file
     * @param md5Checksums          a List of MD5 checksums calculated for the remote file
     * @param blockSizes             the size of the data block for each checksum, in corresponding order
     */
    public void generate(List<Long> rollingChecksums, List<String> md5Checksums, List<Integer> blockSizes) {
        // Needs additional reading to better design this method.
    }

    /**
     * Given a block of data, return a String representation of its MD5 checksum.
     *
     * @param block         the block of data to calculate the checksum for
     * @param len           the length of the data block to calculate on (can be less than block.length)
     * @return              a String representation of the MD5 checksum for the data block
     */
    private String getMD5HashString(byte[] block, int len) {
        this.md.reset();
        this.md.update(block, 0, len);
        byte[] md5 = md.digest();
        return Hex.encodeHexString(md5);
    }
}
