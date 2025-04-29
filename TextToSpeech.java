package bibleverse.client;

import javax.swing.JOptionPane;
import java.io.File;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
import java.awt.HeadlessException;

public class TextToSpeech {
    private Voice voice;
    
    public TextToSpeech() {
        try {
            // Register voice directory
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            
            // Explicitly set audio player
            System.setProperty("com.sun.speech.freetts.audio.AudioPlayer", "com.sun.speech.freetts.audio.JavaClipAudioPlayer");
            
            // Print available voices for debugging
            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice[] voices = voiceManager.getVoices();
            System.out.println("Available voices:");
            for (Voice v : voices) {
                System.out.println("  " + v.getName());
            }
            
            // Get the "kevin" voice
            voice = voiceManager.getVoice("kevin");
            
            if (voice != null) {
                voice.allocate();
                System.out.println("Voice allocated successfully");
            } else {
                System.err.println("Error: Cannot find voice 'kevin'");
                JOptionPane.showMessageDialog(null, "Text-to-speech voice not found. Speech feature will not work.", 
                                          "TTS Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (HeadlessException e) {
            System.err.println("TTS initialization error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Text-to-speech error: " + e.getMessage(), 
                                         "TTS Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void speak(String text) {
        try {
            if (voice != null) {
                System.out.println("Speaking: " + text);
                voice.speak(text);
            } else {
                System.err.println("Error: Voice not initialized");
            }
        } catch (Exception e) {
            System.err.println("TTS error: " + e.getMessage());
        }
    }
    
    public void close() {
        try {
            if (voice != null) {
                voice.deallocate();
            }
        } catch (Exception e) {
            System.err.println("Error closing TTS: " + e.getMessage());
        }
    }
    
    // For testing
    public static void main(String[] args) {
        TextToSpeech tts = new TextToSpeech();
        tts.speak("Hello, this is a test of the text to speech system.");
        tts.close();
    }
}