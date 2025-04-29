package bibleverse;

import bibleverse.server.BibleServer;
import bibleverse.client.BibleClient;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Start the server in a separate thread
        new Thread(() -> {
            BibleServer server = new BibleServer();
            server.start();
        }).start();
        
        // Give the server a moment to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Start the client on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            BibleClient client = new BibleClient();
            client.setVisible(true);
        });
    }
}
