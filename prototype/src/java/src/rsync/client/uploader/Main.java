package rsync.client.uploader;

import java.io.*;
import java.security.*;
import java.util.*;

// http://commons.apache.org/proper/commons-codec/download_codec.cgi
import org.apache.commons.codec.binary.Hex;

public class Main {

    public final static String MD5_CHECKSUM_FILENAME = "md5_checksum";
    public final static String ROLLING_CHECKSUM_FILENAME = "rolling_checksum";
    public final static String UPLOAD_FILENAME = "../../assets/sm_img.jpeg";
    public final static int BLOCK_SIZE = 1024;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, DigestException {
        BufferedInputStream dataStream;
        try {
            dataStream = new BufferedInputStream(new FileInputStream(UPLOAD_FILENAME));
        } catch (FileNotFoundException e) {
            System.out.println(e);
            return;
        }

        RsyncAnalyser analyser = new RsyncAnalyser();
    }

    private static void send() {

    }
}
