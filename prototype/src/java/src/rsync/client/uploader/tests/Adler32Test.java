package rsync.client.uploader.tests;

import rsync.client.uploader.Adler32;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class Adler32Test {

    private static String TEST_FILE_PATH = "./src/rsync/client/uploader/tests/assets/sm_img.jpeg";
    private static String ROLLING_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/sm_img_rolling.sum";
    private static int BLOCK_SIZE_BYTES = 1024;

    Adler32 adler;
    BufferedInputStream dataStream;

    @Before
    public void setUp() throws Exception {
        this.adler = new Adler32();
        this.dataStream = getBufferedIStream();
    }

    @Test
    public void testCalcForBlock() throws Exception {
        /*
        TO BE FINISHED

        byte[] b = new byte[BLOCK_SIZE_BYTES];
        int bytesRead = 0;

        bytesRead = dataStream.read(b);
        long hash = adler.calc(b, bytesRead);
        assertEquals(expectedChecksum, hash);

        List<Long> expectedChecksums = getRollingChecksumList();
        */
    }

    @Test
    public void testCalcForRolling() throws Exception {
        byte[] b = new byte[BLOCK_SIZE_BYTES];
        int bytesRead = 0;
        for (long expectedChecksum : getRollingChecksumList()) {
            bytesRead = dataStream.read(b);
            long hash = adler.calc(b, bytesRead);
            assertEquals(expectedChecksum, hash);
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