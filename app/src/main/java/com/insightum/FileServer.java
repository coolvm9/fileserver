package com.insightum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FileServer {
    private static final Logger logger = LoggerFactory.getLogger(FileServer.class);

    private int port;

    public FileServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server is listening on port " + port);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected");

                    // Processing client connection in a new thread
                    new Thread(() -> processClient(socket)).start();
                } catch (IOException e) {
                    logger.error("Server exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            logger.error("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void processClient(Socket socket) {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();

            Map<String, String> metadata = new HashMap<>();
            String line;
            while (!(line = reader.readLine()).equals("END_METADATA")) {
                int separatorIndex = line.indexOf('=');
                if (separatorIndex != -1) {
                    metadata.put(line.substring(0, separatorIndex), line.substring(separatorIndex + 1));
                }
            }

            // Print received metadata
            logger.info("Received metadata:");
            metadata.forEach((key, value) -> System.out.println(key + ": " + value));

            // Assuming the next bytes are the file content
            File receivedFile = new File("received_file");
            try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            logger.info("File received successfully.");

            // Process the file (Placeholder for processing logic)
            File processedFile = processFile(receivedFile);

            // Send the processed file back to client
            try (FileInputStream fis = new FileInputStream(processedFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }

            logger.info("Processed file sent back to client.");

        } catch (IOException ex) {
            logger.error("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                logger.error("Failed to close the socket");
            }
        }
    }

    private File processFile(File file) {
        // Placeholder for file processing logic.
        // This could be anything, e.g., modifying the file, converting its format, etc.
        // For simplicity, let's just return the file as-is.
        logger.info("Processing file:  {} " , file.getName());
        // Implement your processing logic here and return the new file
        return file;
    }

    public static void main(String[] args) {
        int port = 5051; // Change as required
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid port number provided. Using default port {} " , port);
            }
        }

        logger.info("Starting server at {}", port);
        FileServer server = new FileServer(port);
        server.start();
    }
}

