package rsync.client.uploader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class Main {

    private final static String TEST_FILE_NAME = "test.img";
    private final static String GET_CHECKSUMS_URL = "http://localhost:5000/request/checksums";
    private final static String UPLOAD_INSTRUCTION_URL = "http://localhost:5000/upload/instructions";

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

        String PARTIAL_1_ROLLING_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/partial_1_rolling.sum";
        String PARTIAL_1_MD5_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/partial_1_md5.sum";
        String PARTIAL_0_ROLLING_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/partial_0_rolling.sum";
        String PARTIAL_0_MD5_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/partial_0_md5.sum";
        String ROLLING_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/sm_img_rolling.sum";
        String MD5_CHECKSUM_FILENAME = "./src/test/java/rsync/client/uploader/assets/sm_img_md5.sum";

        String ROLLING_FILENAME = PARTIAL_1_ROLLING_CHECKSUM_FILENAME;
        String MD5_FILENAME = PARTIAL_1_MD5_CHECKSUM_FILENAME;

        List<Long> rolling = getRollingChecksumList(ROLLING_FILENAME);
        List<String> md5 = getMD5ChecksumList(MD5_FILENAME);
        List<Object> instructions = analyser.generate(rolling, md5, 1024, 1024);
        pseudosend(instructions);
        send(instructions);

        // If both checksums are empty, it means that we're trying to send a file for the first time.
        // The server creates a zero-filled file in such case.
        System.out.println("Getting checksums from the server");
        ChecksumResult checksumResult = getChecksums();
        System.out.println(checksumResult.getRolling());
        System.out.println(checksumResult.getMd5());
    }

    private static ChecksumResult getChecksums() {
        File file = new File(UPLOAD_FILENAME);
        long fileSize = file.length();

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(GET_CHECKSUMS_URL);

        // TODO: Use a real file name
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", TEST_FILE_NAME));
        params.add(new BasicNameValuePair("size", String.valueOf(fileSize)));

        ChecksumResult result = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(post);
            String jsonString = EntityUtils.toString(response.getEntity());
            result = new Gson().fromJson(jsonString, ChecksumResult.class);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return result;
    }

    /**
     * Extract data from instructions and send it via POST request.
     * @param instructions
     */
    private static void send(List<Object> instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof ArrayList) {
                // Raw byte data
                List<Byte> data = (List<Byte>) instructions.get(i);
                Byte[] bytes = data.toArray(new Byte[data.size()]);
                byte[] rawBytes = getRawBytes(bytes, true);

                System.out.println(String.format("Sending block #%d, size = %d", i, data.size()));
                // TODO: Use a real file name
                sendBinary(TEST_FILE_NAME, i, rawBytes);
            }
        }
    }

    /**
     * Send a binary chunk with its index to server
     * @param fileName
     * @param index
     * @param bytes
     */
    private static void sendBinary(String fileName, int index, byte[] bytes) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(UPLOAD_INSTRUCTION_URL);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("chunk", bytes, ContentType.DEFAULT_BINARY, fileName);
        builder.addTextBody("index", String.valueOf(index), ContentType.TEXT_PLAIN);

        HttpEntity entity = builder.build();
        post.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println(String.format("Status: %d Response: %s", statusCode, responseBody));
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }

    /**
     * Writes to a file to be parsed by python client, instead of sending it through network protocols
     * @param instructions
     */
    private static void pseudosend(List<Object> instructions) throws IOException {
        // Write to python folder so that it's easy to test
        Path file = Paths.get("../py/instr.out");
        // Write an empty byte to clear the file
        Files.write(file, new byte[0]);
        System.out.println(instructions);
        try {
            for (int i = 0; i < instructions.size(); i++) {
                if (instructions.get(i) instanceof ArrayList) {
                    // Raw byte data
                    List<Byte> data = (List<Byte>) instructions.get(i);
                    Byte[] bytes = data.toArray(new Byte[data.size()]);
                    System.out.println(i);
                    System.out.println(data.size());
                    byte[] rawBytes = getRawBytes(bytes, true);
                    Files.write(file, rawBytes, StandardOpenOption.APPEND);
                } else if (instructions.get(i) instanceof Integer) {
                    // Int
                    List<String> lines = Arrays.asList(Integer.toString((int) instructions.get(i)));
                    Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
                }
            }
            // TODO (blakeyu): WHY DID I DO THIS?? This caused the 2 bytes bug.
            // List<String> lines = Arrays.asList("\n");
            // Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
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

    private static byte[] getRawBytes(Byte[] data, boolean hasNewLineChar) {
        if (hasNewLineChar) {
            byte[] raw = new byte[data.length + 1];
            for (int i = 0; i < data.length; i++) {
                raw[i] = data[i];
            }
            raw[data.length] = '\n';
            return raw;
        } else {
            return getRawBytes(data);
        }
    }

    private static class ChecksumResult {
        private List<String> rolling;
        private List<String> md5;

        public List<String> getRolling() {
            return this.rolling;
        }

        public void setRolling(ArrayList<String> rolling) {
            this.rolling = rolling;
        }

        public List<String> getMd5() {
            return this.md5;
        }

        public void setMd5(ArrayList<String> md5) {
            this.md5 = md5;
        }
    }
}
