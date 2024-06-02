package Prototype;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a folder that contains notes.
 * Provides functionality to create a folder and manage notes within it.
 */
public class Folder {
    // The name of the folder
    private String name;
    // A list to store the notes in the folder
    private List<Note> notes;

    /**
     * Constructs a Folder with the specified name.
     * Initializes the notes list and creates the folder in the file system.
     * @param name the name of the folder
     */
    public Folder(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
        createFolder();
    }

    /**
     * Creates a folder in the file system if it does not already exist.
     */
    private void createFolder() {
        File folder = new File(name);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * Adds a note to the folder.
     * @param note the note to be added
     */
    public void addNote(Note note) {
        notes.add(note);
    }

    /**
     * Returns the list of notes in the folder.
     * @return the list of notes
     */
    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Returns the name of the folder.
     * @return the name of the folder
     */
    public String getName() {
        return name;
    }

    /**
     * Finds a note by its title.
     * @param title the title of the note
     * @return the note if found, otherwise null
     */
    public Note getNoteByTitle(String title) {
        return notes.stream().filter(note -> note.getTitle().equals(title)).findFirst().orElse(null);
    }
}
