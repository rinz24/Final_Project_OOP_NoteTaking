package Prototype;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a note with a title, content, and creation timestamp.
 * Provides functionality to save the note to a file.
 */
public class Note {
    private String title;
    private String content;
    private LocalDateTime creationTime;

    /**
     * Constructs a Note with the specified title, content, and creation timestamp.
     * @param title the title of the note
     * @param content the content of the note
     * @param creationTime the creation timestamp of the note
     */
    public Note(String title, String content, LocalDateTime creationTime) {
        this.title = title;
        this.content = content;
        this.creationTime = creationTime;
    }

    /**
     * Constructs a Note with the specified title and content.
     * The creation timestamp is set to the current time.
     * @param title the title of the note
     * @param content the content of the note
     */
    public Note(String title, String content) {
        this(title, content, LocalDateTime.now());
    }

    /**
     * Saves the note to a file in the specified folder.
     * @param folderName the name of the folder to save the note in
     */
    public void saveToFile(String folderName) {
        File file = new File(folderName + "/" + title + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the note content from a file in the specified folder.
     * @param folderName the name of the folder to load the note from
     */
    public void loadFromFile(String folderName) {
        File file = new File(folderName + "/" + title + ".txt");
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.content = contentBuilder.toString();
    }

    /**
     * Returns the title of the note.
     * @return the title of the note
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the content of the note.
     * @return the content of the note
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the creation timestamp of the note.
     * @return the creation timestamp of the note
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Returns a formatted string representation of the creation timestamp.
     * @return a formatted string representation of the creation timestamp
     */
    public String getFormattedCreationTime() {
        return creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
