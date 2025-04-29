package bibleverse.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class BibleClient extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    // GUI Components
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel loginPanel;
    
    private JTextField verseReferenceField;
    private JTextArea verseTextArea;
    private JButton getVerseButton;
    private JButton saveVerseButton;
    private JButton logoutButton;
    private JPanel mainPanel;
    private JButton textToSpeechButton;
    
    private Timer inactivityTimer;
    private final int INACTIVITY_TIMEOUT = 30000; // 30 seconds
    
    public BibleClient() {
        // Set up the frame
        setTitle("Bible Verse Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeLoginPanel();
        initializeMainPanel();
        
        // Initially show only login panel
        getContentPane().add(loginPanel);
        
        // Set up inactivity timer
        inactivityTimer = new Timer(INACTIVITY_TIMEOUT, e -> handleInactivity());
        inactivityTimer.setRepeats(false);
    }
    
    private void initializeLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel welcomeLabel = new JLabel("Welcome, I'm glad you woke up today");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(welcomeLabel, gbc);
        
        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(userLabel, gbc);
        
        userField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(userField, gbc);
        
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passLabel, gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(passwordField, gbc);
        
        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);
        
        loginButton.addActionListener(e -> handleLogin());
    }
    
    private void initializeMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel greetingLabel = new JLabel("Which verse of the Bible would you like to read?");
        topPanel.add(greetingLabel);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout());
        
        verseReferenceField = new JTextField(15);
        inputPanel.add(new JLabel("Verse reference (e.g., John 3:16):"));
        inputPanel.add(verseReferenceField);
        
        getVerseButton = new JButton("Get Verse");
        getVerseButton.addActionListener(e -> getVerse());
        inputPanel.add(getVerseButton);
        
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        
        verseTextArea = new JTextArea(10, 40);
        verseTextArea.setEditable(true);
        verseTextArea.setLineWrap(true);
        verseTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(verseTextArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        
        textToSpeechButton = new JButton("Read Aloud");
        textToSpeechButton.addActionListener(e -> readVerse());
        bottomPanel.add(textToSpeechButton);
        
        saveVerseButton = new JButton("Save Verse");
        saveVerseButton.addActionListener(e -> saveVerse());
        bottomPanel.add(saveVerseButton);
        
        JButton anotherVerseButton = new JButton("Another Verse");
        anotherVerseButton.addActionListener(e -> resetForAnotherVerse());
        bottomPanel.add(anotherVerseButton);
        
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        bottomPanel.add(logoutButton);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add listeners to reset the inactivity timer
        MouseAdapter activityListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                resetInactivityTimer();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                resetInactivityTimer();
            }
        };
        
        mainPanel.addMouseListener(activityListener);
        mainPanel.addMouseMotionListener(activityListener);
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 9090);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage(), 
                                         "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleLogin() {
        // Simple validation - in a real app, this would authenticate against a database
        if (userField.getText().isEmpty() || new String(passwordField.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.", 
                                         "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Connect to server
        connectToServer();
        
        // Switch to main panel
        getContentPane().removeAll();
        getContentPane().add(mainPanel);
        revalidate();
        repaint();
        
        // Start inactivity timer
        inactivityTimer.start();
    }
    
    private void getVerse() {
        String reference = verseReferenceField.getText().trim();
        if (reference.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a verse reference.", 
                                         "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        resetInactivityTimer();
        
        if (out != null) {
            // Send request to server
            out.println("GET_VERSE:" + reference);
            
            // Get response
            try {
                String response = in.readLine();
                if (response != null && response.startsWith("VERSE:")) {
                    String verse = response.substring(6);
                    verseTextArea.setText(verse);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error receiving verse: " + e.getMessage(), 
                                             "Communication Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveVerse() {
        String reference = verseReferenceField.getText().trim();
        String verseText = verseTextArea.getText().trim();
        
        if (reference.isEmpty() || verseText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Both verse reference and text are required.", 
                                         "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        resetInactivityTimer();
        
        if (out != null) {
            // Send save request to server
            out.println("SAVE_VERSE:" + reference + ":" + verseText);
            
            // Get response
            try {
                String response = in.readLine();
                if (response != null && response.startsWith("RESPONSE:")) {
                    JOptionPane.showMessageDialog(this, response.substring(9), 
                                                 "Save Result", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving verse: " + e.getMessage(), 
                                             "Communication Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void readVerse() {
        // Implement text-to-speech functionality
        String verseText = verseTextArea.getText();
        if (verseText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No verse to read.", 
                                         "Text-to-Speech", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        resetInactivityTimer();
        
        // Create a new thread for TTS to avoid freezing UI
        new Thread(() -> {
            TextToSpeech tts = new TextToSpeech();
            tts.speak(verseText);
        }).start();
    }
    
    private void resetForAnotherVerse() {
        verseReferenceField.setText("");
        verseTextArea.setText("");
        verseReferenceField.requestFocus();
        resetInactivityTimer();
    }
    
    private void resetInactivityTimer() {
        inactivityTimer.stop();
        inactivityTimer.start();
    }
    
    private void handleInactivity() {
        JOptionPane.showMessageDialog(this, "Have a blessed day!", 
                                     "Session Timeout", JOptionPane.INFORMATION_MESSAGE);
        logout();
    }
    
    private void logout() {
        // Close connection with server
        if (out != null) {
            out.println("QUIT");
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        
        // Return to login panel
        getContentPane().removeAll();
        getContentPane().add(loginPanel);
        userField.setText("");
        passwordField.setText("");
        revalidate();
        repaint();
        
        // Stop inactivity timer
        inactivityTimer.stop();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BibleClient client = new BibleClient();
            client.setVisible(true);
        });
    }
}