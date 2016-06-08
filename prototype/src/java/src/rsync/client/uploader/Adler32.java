package rsync.client.uploader;


import java.util.*;

/**
 * Implementation of Mark Adler's checksum algorithm which enables rolling calculation
 * for faster computation.
 *
 * See: https://en.wikipedia.org/wiki/Adler-32#The_algorithm
 */
public class Adler32 {

    // Large prime number used for Adler-32 calcuation
    private final int MOD_ADLER = 65521;
    private HashMap<Long, Map.Entry<Long, Long>> ABmap;

    public Adler32() {
        this.ABmap = new HashMap<Long, Map.Entry<Long, Long>>();
    }

    /**
     * Calculates the Adler32 checksum for a given block of data.
     *
     * The intermediate values are stored in a HashMap for rolling calculations,
     * accessible using the checksum produced.
     *
     * @param block         the block of data to calculate the checksum for
     * @param blockSize     the size of the data block (can be less than block.length)
     * @return              a long representation of the Adler32 checksum for the data block
     */
    public long calc(byte[] block, int blockSize) {
        long result;
        long a = 0;
        long b = 0;
        for (int i = 0; i < blockSize; i++) {
            a += (long) block[i];
            b += (blockSize - i) * (long) block[i];
        }
        // https://en.wikipedia.org/wiki/Modulo_operation
        // Java's Math.floorMod == Python's %
        a = Math.floorMod(a, this.MOD_ADLER);
        b = Math.floorMod(b, this.MOD_ADLER);
        result = this.get_checksum(a, b);

        // a and b can be re-used to calculate next checksum in a rolling fashion
        Map.Entry<Long, Long> value = new AbstractMap.SimpleEntry<>(a, b);
        this.ABmap.put(result, value);
        return result;
    }

    public long calc(long adler32Value, int blockSize, byte firstByte, byte nextByte) {
        Map.Entry<Long, Long> pair = this.ABmap.get(adler32Value);
        long a = pair.getKey();
        long b = pair.getValue();
        a -= (long) firstByte;
        a += (long) nextByte;
        b -= blockSize * (long) firstByte;
        b += a;
        return this.get_checksum(a, b);
    }

    private long get_checksum(long a, long b) {
        return (long) (a + (b * Math.pow(2, 16)));
    }
}