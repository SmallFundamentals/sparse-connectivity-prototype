package rsync.client.uploader;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.security.*;
import java.util.*;

public class RsyncAnalyser {

    private BufferedInputStream dataStream;
    private Adler32 adler;
    private MessageDigest md;
    // TODO: Check that data can't contain -1
    private final byte SEQUENCE_DELIMITER = -1;

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
     * @param remoteRollingChksms       a List of Adler-32 checksums calculated for the remote file
     * @param remoteMD5Chksms           a List of MD5 checksums calculated for the remote file
     * @param defaultBlockSize          the size of data blocks used to calculate checksums, in bytes
     * @param lastBlockSize             remote file size % default block size
     */
    public Object generate(List<Long> remoteRollingChksms, List<String> remoteMD5Chksms, int defaultBlockSize,
            int lastBlockSize) throws IOException {
        // TODO: Have a list of tuples for the two checksums instead of two lists.
        // TODO: Differentiate between different types of errors
        if (this.dataStream == null) {
            return null;
        }

        // We mandate that the blocks must be of a uniform size
        if (lastBlockSize > defaultBlockSize) {
            // TODO: Error code here for invalid block sizes
            return null;
        }

        // Preprocess remote rolling checksums for faster lookup
        HashMap<Long, Integer> remoteRollingMap = this.preprocessRemoteRollingChecksums(remoteRollingChksms);

        byte[] fullBlock = new byte[defaultBlockSize];
        byte[] b = new byte[1];
        int bytesRead;

        int i = 0;
        int lastBlockEnd = -1;
        byte previousFirstByte = 0;
        long rollingChecksum = 0;

        List<Byte> instructions = new ArrayList<>();
        List<Byte> instrBuffer = new ArrayList<>();

        // For each local checksum (at every offset), check to see if it is present in the map.
        while (dataStream.available() > 0) {
            assert i > lastBlockEnd;
            // A previous calculation is available to do rolling checksum calculation on
            if ((i - lastBlockEnd > 1) && (rollingChecksum != 0)) {
                // Read 1 additional byte and do rolling checksum calculation
                bytesRead = this.dataStream.read(b);
                assert bytesRead == 1;
                rollingChecksum = adler.calc(rollingChecksum, defaultBlockSize, previousFirstByte, b[0]);
            }
            // Start a new block for calculation.
            else {
                // Do full block read
                bytesRead = this.dataStream.read(fullBlock);
                if (bytesRead < defaultBlockSize) {
                    // If we read less than a full block but there's still data left, something unexpected is wrong.
                    assert dataStream.available() == 0;
                    // Write all remaining bytes to instructions and update counters
                    instructions.add(SEQUENCE_DELIMITER);
                    for (byte nextByte : fullBlock) {
                        instructions.add(nextByte);
                    }
                    instructions.add(SEQUENCE_DELIMITER);
                    lastBlockEnd = i + bytesRead;
                    break;
                }
                rollingChecksum = adler.calc(fullBlock, defaultBlockSize);
            }

            Integer remoteIDX = remoteRollingMap.get(rollingChecksum);

            // Local block found in remote hashes
            if (remoteIDX != null) {
                // Validate MD5 checksum.
                if (remoteMD5Chksms.get(remoteIDX) == this.getMD5HashString(fullBlock, defaultBlockSize)) {
                    if (instrBuffer.size() > 0) {
                        instructions.add(SEQUENCE_DELIMITER);
                        instructions.addAll(instrBuffer);
                        instructions.add(SEQUENCE_DELIMITER);
                        instrBuffer.clear();
                    }
                    instructions.add((byte)(int)remoteIDX);
                    // TODO: Convert remoteRollingMap to <Long, List<Integer>> instead
                    // Prevent duplicate matches to same block
                    // If this scenario is actually valid, then the map would only create 1 entry for the colliding hashes

                    // Increment counters and go onto next iteration
                    lastBlockEnd = i + defaultBlockSize;
                    i += defaultBlockSize;
                    continue;
                }
                // MD5 doesn't match; we assume it's a coincidence
            }

            // Local block not found. Store byte in buffer, increment counters
            previousFirstByte = fullBlock[0];
            instrBuffer.add(previousFirstByte);
            i++;
        }

        // Clear buffer one last time
        if (instrBuffer.size() > 0) {
            instructions.add(SEQUENCE_DELIMITER);
            instructions.addAll(instrBuffer);
            instructions.add(SEQUENCE_DELIMITER);
        }

        return instructions;
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
