package Prototype;

import javax.swing.*;  // For creating the GUI components
import javax.swing.filechooser.FileNameExtensionFilter;  // For filtering file types in JFileChooser
import javax.swing.text.BadLocationException;  // For handling bad locations in text components
import javax.swing.text.DefaultHighlighter;  // For highlighting text
import javax.swing.text.Highlighter;  // For highlighting text in JTextComponent
import javax.swing.text.JTextComponent;  // Base class for text components like JTextField and JTextArea
import java.awt.*;  // For layout managers, colors, and graphics
import java.awt.event.ActionEvent;  // For handling action events
import java.awt.event.ActionListener;  // For creating action listeners
import java.awt.image.BufferedImage;  // For handling images
import java.io.File;  // For file handling
import java.io.FileReader;  // For reading files
import java.io.FileWriter;  // For writing files
import java.io.IOException;  // For handling IO exceptions
import java.text.SimpleDateFormat;  // For formatting dates
import java.util.ArrayList;  // For using ArrayList collection
import java.util.Date;  // For handling dates
import java.util.Properties;  // For storing and retrieving properties
import javax.imageio.ImageIO;  // For reading and writing images

public class MainApp extends Window {
    public static final JTextComponent TitleField = null;  // Unused static field, can be removed
    private JTextField noteTitleField;  // Text field for note title
    private JTextArea noteContentArea;  // Text area for note content
    private JComboBox<String> folderComboBox;  // Dropdown for selecting folders
    private JList<String> noteList;  // List for displaying notes
    private DefaultListModel<String> noteListModel;  // Model for managing note list data
    private ArrayList<Folder> folders;  // List of folders
    private File selectedFolder;  // Currently selected folder
    private Highlighter highlighter;  // Highlighter for text area

    public MainApp() {
        super(900,700);
        folders = new ArrayList<>();  // Initialize folder list
    }

    /**
     * Initializes the main UI components.
     */
    void initialize() {
        JPanel panel = new JPanel();  // Main panel
        panel.setLayout(new BorderLayout());  // Set layout manager

        // Folder selection panel
        JPanel folderPanel = new JPanel(new FlowLayout());
        JButton selectFolderButton = new JButton("Select/Create Folder");
        selectFolderButton.addActionListener(new SelectFolderListener());  // Add listener for folder selection
        folderPanel.add(selectFolderButton);
        folderComboBox = new JComboBox<>();
        folderComboBox.addActionListener(new FolderSelectionListener());  // Add listener for folder selection change
        folderPanel.add(folderComboBox);

        panel.add(folderPanel, BorderLayout.NORTH);

        // Note creation panel
        JPanel notePanel = new JPanel(new BorderLayout());
        JPanel noteInputPanel = new JPanel(new GridLayout(3, 2));
        noteInputPanel.add(new JLabel("Note Title:"));
        noteTitleField = new JTextField();
        noteInputPanel.add(noteTitleField);
        noteInputPanel.add(new JLabel("Note Content:"));
        noteContentArea = new JTextArea(40, 70);
        noteInputPanel.add(new JScrollPane(noteContentArea));
        notePanel.add(noteInputPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new BorderLayout());

        JButton openTodoListButton = new JButton("Open To-Do List");
        openTodoListButton.addActionListener(e -> new ToDoListApp());  // Open To-Do List application
        buttonPanel.add(openTodoListButton, BorderLayout.NORTH);

        JButton saveNoteButton = new JButton("Save Note");
        saveNoteButton.addActionListener(new SaveNoteListener());  // Save note action
        buttonPanel.add(saveNoteButton, BorderLayout.SOUTH);

        JButton saveAsImageButton = new JButton("Save as Image");
        saveAsImageButton.addActionListener(new SaveAsImageListener());  // Save note as image action
        buttonPanel.add(saveAsImageButton, BorderLayout.CENTER);

        notePanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(notePanel, BorderLayout.CENTER);

        // Note list panel
        noteListModel = new DefaultListModel<>();
        noteList = new JList<>(noteListModel);
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedNoteTitle = noteList.getSelectedValue();
                if (selectedNoteTitle != null) {
                    loadNoteContent(selectedNoteTitle.split(" - ")[0]);  // Load selected note content
                }
            }
        });
        panel.add(new JScrollPane(noteList), BorderLayout.EAST);

        // Highlighting panel
        JPanel highlightPanel = new JPanel(new FlowLayout());
        JButton highlightYellowButton = new JButton("Highlight Yellow");
        highlightYellowButton.setBackground(Color.YELLOW);
        highlightYellowButton.addActionListener(new HighlightListener(Color.YELLOW));  // Highlight yellow action
        highlightPanel.add(highlightYellowButton);

        JButton highlightPinkButton = new JButton("Highlight Pink");
        highlightPinkButton.setBackground(Color.PINK);
        highlightPinkButton.addActionListener(new HighlightListener(Color.PINK));  // Highlight pink action
        highlightPanel.add(highlightPinkButton);

        JButton highlightBlueButton = new JButton("Highlight Blue");
        highlightBlueButton.setBackground(Color.CYAN);
        highlightBlueButton.addActionListener(new HighlightListener(Color.CYAN));  // Highlight blue action
        highlightPanel.add(highlightBlueButton);

        JButton clearHighlightButton = new JButton("Clear Highlight");
        clearHighlightButton.addActionListener(new ClearHighlightListener());  // Clear highlight action
        highlightPanel.add(clearHighlightButton);

        panel.add(highlightPanel, BorderLayout.SOUTH);

        frame.add(panel);  // Add main panel to frame
        frame.setVisible(true);  // Make frame visible
    }

    /**
     * Loads the content of a note by its title.
     * @param noteTitle the title of the note to load
     */
    private void loadNoteContent(String noteTitle) {
        if (selectedFolder != null) {
            File noteFile = new File(selectedFolder, noteTitle + ".properties");  // Locate note file
            if (noteFile.exists()) {
                Properties props = new Properties();
                try (FileReader reader = new FileReader(noteFile)) {
                    props.load(reader);  // Load properties from file
                    noteTitleField.setText(props.getProperty("title"));  // Set title
                    noteContentArea.setText(props.getProperty("content"));  // Set content
                    
                    // Restore highlights
                    for (String key : props.stringPropertyNames()) {
                        if (key.startsWith("highlight_")) {
                            int start = Integer.parseInt(key.substring(10));
                            int length = Integer.parseInt(props.getProperty(key));
                            try {
                                highlighter.addHighlight(start, start + length, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));  // Restore highlight
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Listener for selecting or creating a folder.
     */
    private class SelectFolderListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  // Only directories can be selected
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                selectedFolder = fileChooser.getSelectedFile();  // Get selected folder
                folderComboBox.addItem(selectedFolder.getName());  // Add folder name to combo box
                folderComboBox.setSelectedItem(selectedFolder.getName());
                Folder folder = new Folder(selectedFolder.getPath());
                folders.add(folder);
                noteListModel.clear();
                for (Note note : folder.getNotes()) {
                    noteListModel.addElement(note.getTitle() + " - " + note.getFormattedCreationTime());  // Add notes to list
                }
            }
        }
    }

    /**
     * Listener for saving a note.
     */
    private class SaveNoteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String noteTitle = noteTitleField.getText();
            String noteContent = noteContentArea.getText();
            if (noteTitle.isEmpty() || noteContent.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Note title and content cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedFolder == null) {
                JOptionPane.showMessageDialog(frame, "No folder selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Properties props = new Properties();
            props.setProperty("title", noteTitle);
            props.setProperty("content", noteContent);
            File noteFile = new File(selectedFolder, noteTitle + ".properties");

            // Save highlights
            if (highlighter != null) {
                Highlighter.Highlight[] highlights = highlighter.getHighlights();
                for (Highlighter.Highlight highlight : highlights) {
                    int start = highlight.getStartOffset();
                    int end = highlight.getEndOffset();
                    int length = end - start;
                    props.setProperty("highlight_" + start, String.valueOf(length));  // Store highlight properties
                }
            }

            try (FileWriter writer = new FileWriter(noteFile)) {
                props.store(writer, "Note Properties");
                noteListModel.addElement(noteTitle + " - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(noteFile.lastModified())));  // Add note to list
                JOptionPane.showMessageDialog(frame, "Note saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                TitleField.setText("");  // Clear title field
                noteContentArea.setText("");  // Clear content area
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error saving note", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Listener for folder selection change.
     */
    private class FolderSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFolder != null) {
                Folder folder = new Folder(selectedFolder.getPath());
                noteListModel.clear();
                for (Note note : folder.getNotes()) {
                    noteListModel.addElement(note.getTitle() + " - " + note.getFormattedCreationTime());  // Update note list
                }
            }
        }
    }

    /**
     * Listener for highlighting text.
     */
    private class HighlightListener implements ActionListener {
        private Color color;

        public HighlightListener(Color color) {
            this.color = color;  // Set highlight color
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (highlighter == null) {
                highlighter = noteContentArea.getHighlighter();  // Initialize highlighter
            }
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(color);
            int start = noteContentArea.getSelectionStart();
            int end = noteContentArea.getSelectionEnd();
            try {
                highlighter.addHighlight(start, end, painter);  // Add highlight to selected text
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Listener for clearing highlights.
     */
    private class ClearHighlightListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (highlighter != null) {
                highlighter.removeAllHighlights();  // Clear all highlights
            }
        }
    }

    /**
     * Listener for saving a note as an image.
     */
    private class SaveAsImageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String noteTitle = noteTitleField.getText();
            String noteContent = noteContentArea.getText();
            if (noteTitle.isEmpty() || noteContent.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Note title and content cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedFolder == null) {
                JOptionPane.showMessageDialog(frame, "No folder selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG files", "jpg", "jpeg"));  // Set file filter for JPEG
            int option = fileChooser.showSaveDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".jpg") && !outputFile.getName().toLowerCase().endsWith(".jpeg")) {
                    outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".jpg");  // Ensure file has .jpg extension
                }
                try {
                    BufferedImage image = new BufferedImage(noteContentArea.getWidth(), noteContentArea.getHeight(), BufferedImage.TYPE_INT_RGB);  // Create image
                    Graphics2D graphics = image.createGraphics();
                    noteContentArea.paint(graphics);  // Paint note content onto image
                    graphics.dispose();
                    ImageIO.write(image, "jpg", outputFile);  // Write image to file
                    JOptionPane.showMessageDialog(frame, "Note saved as image successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error saving note as image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Main method to start the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);  // Run the application
    }
}
