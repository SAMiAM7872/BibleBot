package bibleverse.server;

import bibleverse.database.BibleDatabase;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class BibleServer {
    private static final int PORT = 9090;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running;
    
    public BibleServer() {
        threadPool = Executors.newFixedThreadPool(10); // Allow up to 10 clients
        running = true;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Bible Server started on port " + PORT);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        BibleServer server = new BibleServer();
        server.start();
    }
    
    // Inner class to handle client connections
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Process commands from client
                    if (inputLine.startsWith("GET_VERSE:")) {
                        String reference = inputLine.substring(10).trim();
                        String verse = BibleDatabase.getInstance().getVerse(reference);
                        out.println("VERSE:" + verse);
                    } else if (inputLine.startsWith("SAVE_VERSE:")) {
                        String[] parts = inputLine.substring(11).split(":", 2);
                        if (parts.length == 2) {
                            BibleDatabase.getInstance().saveVerse(parts[0].trim(), parts[1].trim());
                            out.println("RESPONSE:Verse saved successfully");
                        }
                    } else if (inputLine.equals("QUIT")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}