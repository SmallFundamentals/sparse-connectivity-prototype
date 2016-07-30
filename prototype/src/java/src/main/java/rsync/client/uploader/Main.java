package rsync.client.uploader;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

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
        analyser.update(dataStream);

        String PARTIAL_0_ROLLING_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/partial_0_rolling.sum";
        String PARTIAL_0_MD5_CHECKSUM_FILENAME = "./src/rsync/client/uploader/tests/assets/partial_0_md5.sum";
        List<Long> rolling = getRollingChecksumList(PARTIAL_0_ROLLING_CHECKSUM_FILENAME);
        List<String> md5 = getMD5ChecksumList(PARTIAL_0_MD5_CHECKSUM_FILENAME);
        List<Object> instructions = analyser.generate(rolling, md5, 1024, 1024);
        pseudosend(instructions);
    }

    private static void send() {

    }

    /**
     * Writes to a file to be parsed by python client, instead of sending it through network protocols
     * @param instructions
     */
    private static void pseudosend(List<Object> instructions) throws IOException {
        Path file = Paths.get("instr.out");
        // Write an empty byte to clear the file
        Files.write(file, new byte[0]);
        try {
            for (int i = 0; i < instructions.size(); i++) {
                if (instructions.get(i) instanceof ArrayList) {
                    // Raw byte data
                    List<Byte> data = (List<Byte>) instructions.get(i);
                    Byte[] bytes = data.toArray(new Byte[data.size()]);
                    Files.write(file, getRawBytes(bytes), StandardOpenOption.APPEND);
                } else if (instructions.get(i) instanceof Integer) {
                    // Int
                    List<String> lines = Arrays.asList(Integer.toString((int) instructions.get(i)));
                    Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
                }
            }
            List<String> lines = Arrays.asList("\n");
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }
    }

    // TODO: Put this in a util file so it can be reused
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

    private static byte[] getRawBytes(Byte[] data) {
        byte[] raw = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            raw[i] = data[i];
        }
        return raw;
    }
}
