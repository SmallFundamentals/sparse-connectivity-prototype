package rsync.client.uploader.tests;

import org.junit.Before;
import org.junit.Test;
import rsync.client.uploader.RsyncAnalyser;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class RsyncAnalyserTest {

    private static String TEST_FILE_PATH = "./src/test/java/rsync/client/uploader/assets/sm_img.jpeg";
    private static String ROLLING_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/sm_img_rolling.sum";
    private static String MD5_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/sm_img_md5.sum";
    private static String PARTIAL_0_ROLLING_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/partial_0_rolling.sum";
    private static String PARTIAL_0_MD5_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/partial_0_md5.sum";

    RsyncAnalyser analyser;
    BufferedInputStream dataStream;

    @Before
    public void setUp() throws Exception {
        this.analyser = new RsyncAnalyser();
        this.dataStream = getBufferedIStream();
    }

    @Test
    public void testGenerateNewFile() throws Exception {
        int expectedSize = this.dataStream.available();
        this.analyser.update(this.dataStream);
        List<Object> instructions = this.analyser.generate(new ArrayList<>(), new ArrayList<>(), 1024, 1024);
        assertEquals(expectedSize, ((List<Byte>)instructions.get(0)).size());
        printSuccessMessage("testGenerateNewFile");
    }

    @Test
    public void testGenerateCompleteFile() throws Exception {
        this.analyser.update(this.dataStream);
        List<Long> rolling = this.getRollingChecksumList(ROLLING_CHECKSUM_FILENAME);
        List<String> md5 = this.getMD5ChecksumList(MD5_CHECKSUM_FILENAME);
        List<Object> instructions = this.analyser.generate(rolling, md5, 1024, 1024);

        for (int i = 0; i < 19; i++) {
            assertEquals(i, instructions.get(i));
        }
        printSuccessMessage("testGenerateCompleteFile");
    }

    @Test
    public void testGeneratePartialFile() throws Exception {
        this.analyser.update(this.dataStream);
        List<Long> rolling = this.getRollingChecksumList(PARTIAL_0_ROLLING_CHECKSUM_FILENAME);
        List<String> md5 = this.getMD5ChecksumList(PARTIAL_0_MD5_CHECKSUM_FILENAME);
        List<Object> instructions = this.analyser.generate(rolling, md5, 1024, 1024);

        int i = 0;
        List<Integer> missingBlock = new ArrayList<Integer>();
        missingBlock.add(2);
        missingBlock.add(17);
        for (int count = 0; count < instructions.size(); count++) {
            if (instructions.get(count) instanceof Integer) {
                assertEquals(i, instructions.get(count));
                i++;
            }
            else {
                assertEquals((long)count, (long)missingBlock.get(0));
                missingBlock.remove(0);
            }
        }
        assertEquals(0, missingBlock.size());
        printSuccessMessage("testGeneratePartialFile");
    }

    private static BufferedInputStream getBufferedIStream() throws FileNotFoundException {
        BufferedInputStream dataStream = new BufferedInputStream(new FileInputStream(TEST_FILE_PATH));
        return dataStream;
    }

    private static List<Long> getRollingChecksumList(String path) throws IOException {
        List<Long> rollingChecksums = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                long b = Long.parseLong(line);
                rollingChecksums.add(b);
            }
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }
        return rollingChecksums;
    }

    private static List<String> getMD5ChecksumList(String path) throws IOException {
        List<String> checksum = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                checksum.add(line);
            }
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }
        return checksum;
    }

    private static void printSuccessMessage(String message) {
        System.out.println((char)27 + "[32mPASS: " + message + (char)27 + "[0m");
    }
}
