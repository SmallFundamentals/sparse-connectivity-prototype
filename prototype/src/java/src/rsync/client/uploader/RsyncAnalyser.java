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
    public Object generate(List<Long> remoteRollingChksms, List<String> remoteMD5Chksms, int[] blockSizes)
            throws IOException {
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

        int blockSize = blockSizes[0];

        // Preprocess remote rolling checksums for faster lookup
        HashMap<Long, Integer> remoteRollingMap = this.preprocessRemoteRollingChecksums(remoteRollingChksms);

        // If it is, check MD5. Skip by $(defaultBlockSize) and write its index to the instructions
            // For everything between it and its previous ending, write those bytes to the instructions
            // If rolling matches but MD5 doesn't, we assume it's a coincidence and move on?
        // If it's not, go to next byte and repeat.

        byte[] fullBlock = new byte[blockSize];
        byte[] b = new byte[1];
        int bytesRead;
        int i = 0;
        int lastBlockEnd = -1;
        byte previousFirstByte = 0;
        long rollingChecksum = 0;
        // For each local checksum (at every offset), check to see if it is present in the map.
        while (dataStream.available() > 0) {
            assert i > lastBlockEnd;
            // A previous calculation is available to do rolling checksum calculation on
            if ((i - lastBlockEnd > 1) && (rollingChecksum != 0)) {
                // Read 1 additional byte and do rolling checksum calculation
                bytesRead = this.dataStream.read(b);
                assert bytesRead == 1;
                rollingChecksum = adler.calc(rollingChecksum, blockSize, previousFirstByte, b[0]);
            }
            // Start a new block for calculation.
            else {
                // Do full block read
                bytesRead = this.dataStream.read(fullBlock);
                if (bytesRead < blockSize) {
                    // If we read less than a full block but there's still data left, something unexpected is wrong.
                    assert dataStream.available() == 0;
                    // TODO: Write entire block to instructions
                    break;
                }
                rollingChecksum = adler.calc(fullBlock, blockSize);
            }

            Integer remoteIDX = remoteRollingMap.get(rollingChecksum);

            // Local block found in remote hashes
            if (remoteIDX != null) {
                // Validate MD5 checksum.
                if (remoteMD5Chksms.get(remoteIDX) == this.getMD5HashString(fullBlock, blockSize)) {
                    // TODO: Write bytes before and remote index of block to instructions
                    // TODO: Convert remoteRollingMap to <Long, List<Integer>> instead
                    // Prevent duplicate matches to same block
                    // If this scenario is actually valid, then the map would only create 1 entry for the colliding hashes

                    // Increment counters and go onto next iteration
                    lastBlockEnd = i + blockSize;
                    i += blockSize;
                    continue;
                }
                // MD5 doesn't match; we assume it's a coincidence
            }

            // Local block not found. Increment counters and continue
            i++;
            previousFirstByte = fullBlock[0];
        }
        return null;
    }

    private boolean checkBlockSizes(int[] blockSizes) {
        for (int i = 1; i < blockSizes.length; i++) {
            if ((blockSizes[i] != blockSizes[i-1]) && (i != blockSizes.length - 1)) {
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
