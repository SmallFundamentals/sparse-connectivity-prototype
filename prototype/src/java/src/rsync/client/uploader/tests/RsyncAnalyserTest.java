package rsync.client.uploader.tests;

import org.junit.Before;
import org.junit.Test;
import rsync.client.uploader.RsyncAnalyser;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class RsyncAnalyserTest {

    private static String TEST_FILE_PATH = "./src/rsync/client/uploader/tests/assets/sm_img.jpeg";
    private static String ROLLING_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/sm_img_rolling.sum";
    private static String MD5_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/sm_img_md5.sum";
    private static String PARTIAL_0_ROLLING_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/partial_0_rolling.sum";
    private static String PARTIAL_0_MD5_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/partial_0_md5.sum";

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
    }

    @Test
    public void testGeneratePartialFile() throws Exception {
        this.analyser.update(this.dataStream);
        List<Long> rolling = this.getRollingChecksumList(PARTIAL_0_ROLLING_CHECKSUM_FILENAME);
        List<String> md5 = this.getMD5ChecksumList(PARTIAL_0_MD5_CHECKSUM_FILENAME);
        List<Object> instructions = this.analyser.generate(rolling, md5, 1024, 1024);
        System.out.println(instructions);
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
}