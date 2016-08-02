package rsync.client.uploader.tests;

import rsync.client.uploader.Adler32;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class Adler32Test {

    private static int BLOCK_SIZE_BYTES = 1024;
    private static String TEST_FILE_PATH = "./src/test/java/rsync/client/uploader/assets/sm_img.jpeg";
    private static String ROLLING_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/sm_img_rolling.sum";

    Adler32 adler;
    BufferedInputStream dataStream;

    @Before
    public void setUp() throws Exception {
        this.adler = new Adler32();
        this.dataStream = getBufferedIStream();
    }

    @Test
    public void testCalcForBlock() throws Exception {
        byte[] b = new byte[BLOCK_SIZE_BYTES];
        int bytesRead;
        for (long expectedChecksum : getRollingChecksumList()) {
            bytesRead = dataStream.read(b);
            long hash = adler.calc(b, bytesRead);
            assertEquals(expectedChecksum, hash);
        }
    }

    @Test
    public void testCalcForRolling() throws Exception {
        dataStream.mark(3 * BLOCK_SIZE_BYTES);
        byte[] b = new byte[BLOCK_SIZE_BYTES];
        int bytesRead = dataStream.read(b);
        byte previousFirstByte = b[0];

        long rollingChecksum = adler.calc(b, bytesRead);
        long expectedChecksum = getRollingChecksumList().get(0);
        assertEquals(expectedChecksum, rollingChecksum);

        for (int i = 1; i < BLOCK_SIZE_BYTES; i++) {
            // Reset data stream and skip first i bytes for each iteration.
            dataStream.reset();
            dataStream.skip(i);
            bytesRead = dataStream.read(b);

            rollingChecksum = adler.calc(rollingChecksum, bytesRead, previousFirstByte, b[bytesRead-1]);
            expectedChecksum = adler.calc(b, bytesRead);
            assertEquals(expectedChecksum, rollingChecksum);

            previousFirstByte = b[0];
        }
    }

    private static BufferedInputStream getBufferedIStream() throws FileNotFoundException {
        BufferedInputStream dataStream = new BufferedInputStream(new FileInputStream(TEST_FILE_PATH));
        return dataStream;
    }

    private static List<Long> getRollingChecksumList() throws IOException {
        List<Long> rollingChecksums = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(ROLLING_CHECKSUM_FILENAME)))) {
            String line;
            while ((line = br.readLine()) != null) {
                long b = Long.parseLong(line);
                rollingChecksums.add(b);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            throw e;
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }
        return rollingChecksums;
    }
}