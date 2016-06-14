package rsync.client.uploader.tests;

import org.junit.Before;
import org.junit.Test;
import rsync.client.uploader.RsyncAnalyser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.Assert.*;

public class RsyncAnalyserTest {

    private static String TEST_FILE_PATH = "./src/rsync/client/uploader/tests/assets/sm_img.jpeg";

    RsyncAnalyser analyser;
    BufferedInputStream dataStream;

    @Before
    public void setUp() throws Exception {
        this.analyser = new RsyncAnalyser();
        this.dataStream = getBufferedIStream();
    }

    @Test
    public void testGenerate() throws Exception {
        this.analyser.update(this.dataStream);
        List<Byte> instructions = this.analyser.generate(new ArrayList<>(), new ArrayList<>(), 1024, 1024);
        System.out.println(instructions.size());
        System.out.println(instructions);
    }

    @Test
    public void testPreprocessRemoteRollingChecksums() {

    }

    @Test
    public void testGetMD5HashString() {

    }

    private static BufferedInputStream getBufferedIStream() throws FileNotFoundException {
        BufferedInputStream dataStream = new BufferedInputStream(new FileInputStream(TEST_FILE_PATH));
        return dataStream;
    }
}