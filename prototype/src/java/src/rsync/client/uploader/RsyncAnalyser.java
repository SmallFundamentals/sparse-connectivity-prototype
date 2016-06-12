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
     * Given two set of hashes for a supposedly possibly incomplete remote version of a file
     * (specified by update(BufferedInputStream)), generate instructions for the remote machine to recreate the file.
     *
     * @param remoteRollingChksms      a List of Adler-32 checksums calculated for the remote file
     * @param remoteMD5Chksms          a List of MD5 checksums calculated for the remote file
     * @param blockSizes               the size of the data block for each checksum, in corresponding order
     */
    public Object generate(List<Long> remoteRollingChksms, List<String> remoteMD5Chksms, List<Integer> blockSizes) {
        // TODO: Have a list of tuples for the two checksums instead of two lists.
        // TODO: Differentiate between different types of errors
        // TODO: Is it better to simplify blockSizes such that it's only a tuple of (defaultSize: lastBlockSize)?
        if (this.dataStream == null) {
            return null;
        }

        // We mandate that the blocks must be of a uniform size
        if (!this.checkBlockSizes(blockSizes)) {
            // TODO: Error code here for invalid block sizes
            return null;
        }

        // Preprocess remote rolling checksums for faster lookup
        HashMap<Long, Integer> remoteRollingMap = this.preprocessRemoteRollingChecksums(remoteRollingChksms);

        // For each local checksum (at every offset), check to see if it is present in the map.
        // If it is, check MD5. Skip by $(defaultBlockSize) and write its index to the instructions
            // For everything between it and its previous ending, write those bytes to the instructions
            // If rolling matches but MD5 doesn't, we assume it's a coincidence and move on?
        // If it's not, go to next byte and repeat.

        // When we finish all blocks of size $(defaultBlockSize), do we look at blocks of lesser sizes, since
        // it's possible that there's a dangling block on remote file?
        // Self answer: No. If our local checking ends with less than a block left, we simply
        // send all those bytes over?
        // Could be made more efficient, but not very likely that there's a dangling block at the end...
            // Once the end is reached, or there's not enough for a block, we can use incrementally smaller sizes
            // starting from the end and repeat until a block size of 0 is reached.
    }

    private boolean checkBlockSizes(List<Integer> blockSizes) {
        for (int i = 1; i < blockSizes.size(); i++) {
            if ((blockSizes.get(i) != blockSizes.get(i-1)) && (i != blockSizes.size() - 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Given a list of checksums, convert it into a HashMap of its checksum values to its indices.
     *
     * @param remoteRollingChksms       a List of Adler-32 rolling checksums
     * @return                          a mapping of checksums to its index
     */
    private HashMap<Long, Integer> preprocessRemoteRollingChecksums(List<Long> remoteRollingChksms) {
        HashMap<Long, Integer> ret = new HashMap<>();
        for (int i = 0; i < remoteRollingChksms.size(); i++) {
            ret.put(remoteRollingChksms.get(i), i);
        }
        return ret;
    }

    /*
    THIS IS WRONG. KEEPING SO THAT I CAN REUSE CODE LATER.

    private HashMap<Long, Integer> preprocessLocalRollingChecksums(int blockSize) throws IOException {
        // Public methods should check this before calling helper
        assert dataStream != null;

        HashMap<Long, Integer> ret = new HashMap<>();
        byte[] firstBlock = new byte[blockSize];
        byte[] b = new byte[1];

        // Local file should not have less than a single block of data.
        if (dataStream.read(firstBlock) < blockSize) {
            // TODO: Throw an exception here
            return null;
        }

        int i = 0;
        byte previousFirstByte = firstBlock[0];
        long rollingChecksum = adler.calc(firstBlock, blockSize);
        ret.put(rollingChecksum, i++);
        while (dataStream.read(b) == 1) {
            rollingChecksum = adler.calc(rollingChecksum, blockSize, previousFirstByte, b[0]);
            previousFirstByte = b[0];
            ret.put(rollingChecksum, i++);
        }
        return ret;
    }
    */

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
