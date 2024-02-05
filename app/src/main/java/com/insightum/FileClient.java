package com.insightum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FileClient {
    private static final Logger logger = LoggerFactory.getLogger(FileClient.class);
    private String host;
    private int port;
    private String filePath;

    public FileClient(String host, int port, String filePath) {
        this.host = host;
        this.port = port;
        this.filePath = filePath;
    }

    public void sendFileWithMetadata() {
        logger.info("In sendFileWithMetaData");
        // Metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("FileName", new File(filePath).getName());
        metadata.put("ContentType", "text/plain");
        logger.info("FileName : {} , Content type : {} ", metadata.get("FileName"), metadata.get("ContentType"));

        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true);
             FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

//            // Sending metadata
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
            writer.println("END_METADATA"); // Signal the end of metadata


            // Sending file content
            byte[] buffer = new byte[4096];
            int count;
            while ((count = bis.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.flush();
            socket.shutdownOutput();

            // Reading response from server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            System.out.println(response);
            logger.info("Server response: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java FileClient <host> <port> <file-path>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String filePath = args[2];

        FileClient client = new FileClient(host, port, filePath);
        client.sendFileWithMetadata();
    }
}