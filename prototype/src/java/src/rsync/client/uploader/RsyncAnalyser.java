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
     * @param remoteRollingChksms      a List of Adler-32 checksums calculated for the remote file
     * @param remoteMD5Chksms          a List of MD5 checksums calculated for the remote file
     * @param blockSizes               the size of the data block for each checksum, in corresponding order
     */
    public Object generate(List<Long> remoteRollingChksms, List<String> remoteMD5Chksms, List<Integer> blockSizes) {
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

        List<Long> localRollingChksms;
        try {
            localRollingChksms = this.preprocessLocalRollingChecksums(blockSizes.get(0));
        }
        catch (IOException e) {
            // TODO: Error code here for cannot access local file
            return null;
        }

        // Do a linear scan on remote rolling checksum. Record the spaces skipped

        // MD5 validate each block

        // Process results
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
     * Preprocess the local file for transmission by calculating its rolling checksum at every offset
     * for a given block size.
     *
     * @param blockSize         size of each data block in bytes
     * @return                  a List of Adler32 rolling checksums
     * @throws IOException
     */
    private List<Long> preprocessLocalRollingChecksums(int blockSize) throws IOException {
        // Public methods should check this before calling helper
        assert dataStream != null;

        List<Long> ret = new ArrayList<>();
        byte[] firstBlock = new byte[blockSize];
        byte[] b = new byte[1];

        // Local file should not have less than a single block of data.
        if (dataStream.read(firstBlock) < blockSize) {
            // TODO: Throw an exception here
            return null;
        }

        byte previousFirstByte = firstBlock[0];
        long rollingChecksum = adler.calc(firstBlock, blockSize);
        ret.add(rollingChecksum);
        while (dataStream.read(b) == 1) {
            rollingChecksum = adler.calc(rollingChecksum, blockSize, previousFirstByte, b[0]);
            previousFirstByte = b[0];
            ret.add(rollingChecksum);
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
