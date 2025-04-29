package bibleverse.database;

import java.util.HashMap;
import java.util.Map;

public class BibleDatabase {
    private Map<String, String> verses;
    private static BibleDatabase instance;
    
    private BibleDatabase() {
        verses = new HashMap<>();
        initializeDatabase();
    }
    
    public static synchronized BibleDatabase getInstance() {
        if (instance == null) {
            instance = new BibleDatabase();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        // Add some sample verses - in a real application you would load these from a file
        verses.put("John 3:16", "For God so loved the world that He gave His only begotten Son, that whoever believes in Him should not perish but have everlasting life.");
        verses.put("Psalm 23:1", "The LORD is my shepherd; I shall not want.");
        verses.put("Romans 8:28", "And we know that all things work together for good to those who love God, to those who are the called according to His purpose.");
        verses.put("Philippians 4:13", "I can do all things through Christ who strengthens me.");
        verses.put("Genesis 1:1", "In the beginning God created the heavens and the earth.");
        // Add more verses as needed
    }
    
    public String getVerse(String reference) {
        return verses.getOrDefault(reference, "Verse not found. Please check the reference format (e.g., 'John 3:16').");
    }
    
    public void saveVerse(String reference, String text) {
        verses.put(reference, text);
    }
}