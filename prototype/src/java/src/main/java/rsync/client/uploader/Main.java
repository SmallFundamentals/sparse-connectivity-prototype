package rsync.client.uploader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Main {

    public final static String ORIGINAL_FILENAME = "../../assets/sm_img.jpeg";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, DigestException {
        for (String partialFileName: args) {
            System.out.println(String.format("UPLOADING for test - '%s'", partialFileName));
            System.out.println();

            ChecksumResult cr = NetworkUtil.getChecksumResult(ORIGINAL_FILENAME, partialFileName);

            BufferedInputStream dataStream;
            try {
                dataStream = new BufferedInputStream(new FileInputStream(ORIGINAL_FILENAME));
            } catch (FileNotFoundException e) {
                System.out.println(e);
                return;
            }

            RsyncAnalyser analyser = new RsyncAnalyser();
            analyser.update(dataStream);

            List<Long> rolling = cr.getRolling();
            List<String> md5 = cr.getMd5();
            List<Object> instructions = analyser.generate(rolling, md5, 1024, 1024);

            NetworkUtil.send(partialFileName, instructions);

            System.out.println();
        }
    }
}
