package rsync.client.uploader;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkUtil {

    private static final String GET_CHECKSUMS_URL = "http://localhost:5000/request/checksums";
    private static final String UPLOAD_INSTRUCTION_URL = "http://localhost:5000/upload/instructions";

    /**
     * Get rolling and md5 checksum from the server
     * @param fileName
     * @return ChecksumResult
     */
    public static ChecksumResult getChecksumResult(String fileName) throws FileNotFoundException {
        return getChecksumResult(fileName, fileName);
    }

    /**
     * Get rolling and md5 checksum from the server
     * @param localFileName
     * @param remoteFileName
     * @return ChecksumResult
     */
    public static ChecksumResult getChecksumResult(String localFileName, String remoteFileName)
            throws FileNotFoundException {
        File file = new File(localFileName);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        long fileSize = file.length();

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(GET_CHECKSUMS_URL);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", remoteFileName));
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
     * @param fileName
     * @param instructions
     */
    public static void send(String fileName, List<Object> instructions) {
        System.out.println(String.format("-- Sending instructions for '%s' --", fileName));
        int idx = 0;
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof ArrayList) {
                List<Byte> data = (List<Byte>) instructions.get(i);
                Byte[] bytes = data.toArray(new Byte[data.size()]);
                byte[] rawBytes = getRawBytes(bytes);

                System.out.println(String.format("Sending block #%d, size = %d", idx, data.size()));
                sendBinary(fileName, idx, rawBytes);
                idx += (data.size() / 1024);
            } else {
                idx++;
            }
            System.out.println(idx);
        }
    }

    /**
     * Send a binary chunk with its index to server
     * @param fileName
     * @param index
     * @param bytes
     */
    public static void sendBinary(String fileName, int index, byte[] bytes) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(UPLOAD_INSTRUCTION_URL);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("chunk", bytes, ContentType.DEFAULT_BINARY, fileName);
        builder.addTextBody("index", String.valueOf(index), ContentType.TEXT_PLAIN);
        builder.addTextBody("name", fileName, ContentType.TEXT_PLAIN);

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
     * DEPRECATED. Use send() combined with runserver.py to test.
     *
     * Writes to a file to be parsed by python client, instead of sending it through network protocols
     * @param instructions
     */
    public static void pseudosend(List<Object> instructions) throws IOException {
        // Write to python folder so that it's easy to test
        Path file = Paths.get("../py/in/instr.out");
        // Write an empty byte to clear the file
        Files.write(file, new byte[0]);
        System.out.println("Pseudo-sending by writing to file");
        try {
            for (int i = 0; i < instructions.size(); i++) {
                if (instructions.get(i) instanceof ArrayList) {
                    // Raw byte data
                    List<Byte> data = (List<Byte>) instructions.get(i);
                    Byte[] bytes = data.toArray(new Byte[data.size()]);
                    System.out.print("Raw data of size ");
                    System.out.println(data.size());
                    byte[] rawBytes = getRawBytes(bytes);
                    Files.write(file, rawBytes, StandardOpenOption.APPEND);
                } else if (instructions.get(i) instanceof Integer) {
                    // Int
                    List<String> lines = Arrays.asList(Integer.toString((int) instructions.get(i)));
                    System.out.print("Index ");
                    System.out.println(instructions.get(i));
                    Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }
    }

    private static byte[] getRawBytes(Byte[] data) {
        byte[] raw = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            raw[i] = data[i];
        }
        return raw;
    }
}
