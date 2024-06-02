package Prototype;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.imageio.ImageIO;

public class MainApp {
    public static final JTextComponent TitleField = null;
    private JFrame frame;
    private JTextField noteTitleField;
    private JTextArea noteContentArea;
    private JComboBox<String> folderComboBox;
    private JList<String> noteList;
    private DefaultListModel<String> noteListModel;
    private ArrayList<Folder> folders;
    private File selectedFolder;
    private Highlighter highlighter;

    public MainApp() {
        folders = new ArrayList<>();
        frame = new JFrame("Note Taking App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        initialize();
    }

    private void initialize() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Folder selection panel
        JPanel folderPanel = new JPanel(new FlowLayout());
        JButton selectFolderButton = new JButton("Select/Create Folder");
        selectFolderButton.addActionListener(new SelectFolderListener());
        folderPanel.add(selectFolderButton);
        folderComboBox = new JComboBox<>();
        folderComboBox.addActionListener(new FolderSelectionListener());
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

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JButton openTodoListButton = new JButton("Open To-Do List");
        openTodoListButton.addActionListener(e -> new ToDoListApp());
        buttonPanel.add(openTodoListButton, BorderLayout.NORTH);

        JButton saveNoteButton = new JButton("Save Note");
        saveNoteButton.addActionListener(new SaveNoteListener());
        buttonPanel.add(saveNoteButton, BorderLayout.SOUTH);

        JButton saveAsImageButton = new JButton("Save as Image");
        saveAsImageButton.addActionListener(new SaveAsImageListener());
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
                    loadNoteContent(selectedNoteTitle.split(" - ")[0]);
                }
            }
        });
        panel.add(new JScrollPane(noteList), BorderLayout.EAST);

        // Highlighting panel
        JPanel highlightPanel = new JPanel(new FlowLayout());
        JButton highlightYellowButton = new JButton("Highlight Yellow");
        highlightYellowButton.setBackground(Color.YELLOW);
        highlightYellowButton.addActionListener(new HighlightListener(Color.YELLOW));
        highlightPanel.add(highlightYellowButton);

        JButton highlightPinkButton = new JButton("Highlight Pink");
        highlightPinkButton.setBackground(Color.PINK);
        highlightPinkButton.addActionListener(new HighlightListener(Color.PINK));
        highlightPanel.add(highlightPinkButton);

        JButton highlightBlueButton = new JButton("Highlight Blue");
        highlightBlueButton.setBackground(Color.CYAN);
        highlightBlueButton.addActionListener(new HighlightListener(Color.CYAN));
        highlightPanel.add(highlightBlueButton);

        JButton clearHighlightButton = new JButton("Clear Highlight");
        clearHighlightButton.addActionListener(new ClearHighlightListener());
        highlightPanel.add(clearHighlightButton);

        panel.add(highlightPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void loadNoteContent(String noteTitle) {
        if (selectedFolder != null) {
            File noteFile = new File(selectedFolder, noteTitle + ".properties");
            if (noteFile.exists()) {
                Properties props = new Properties();
                try (FileReader reader = new FileReader(noteFile)) {
                    props.load(reader);
                    noteTitleField.setText(props.getProperty("title"));
                    noteContentArea.setText(props.getProperty("content"));
                    
                    // Restore highlights
                    for (String key : props.stringPropertyNames()) {
                        if (key.startsWith("highlight_")) {
                            int start = Integer.parseInt(key.substring(10));
                            int length = Integer.parseInt(props.getProperty(key));
                            try {
                                highlighter.addHighlight(start, start + length, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
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

    private class SelectFolderListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                selectedFolder = fileChooser.getSelectedFile();
                folderComboBox.addItem(selectedFolder.getName());
                folderComboBox.setSelectedItem(selectedFolder.getName());
                Folder folder = new Folder(selectedFolder.getPath());
                folders.add(folder);
                noteListModel.clear();
                for (Note note : folder.getNotes()) {
                    noteListModel.addElement(note.getTitle() + " - " + note.getFormattedCreationTime());
                }
            }
        }
    }

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
                    props.setProperty("highlight_" + start, String.valueOf(length));
                }
            }

            try (FileWriter writer = new FileWriter(noteFile)) {
                props.store(writer, "Note Properties");
                noteListModel.addElement(noteTitle + " - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(noteFile.lastModified())));
                JOptionPane.showMessageDialog(frame, "Note saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                TitleField.setText("");
                noteContentArea.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error saving note", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class FolderSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFolder != null) {
                Folder folder = new Folder(selectedFolder.getPath());
                noteListModel.clear();
                for (Note note : folder.getNotes()) {
                    noteListModel.addElement(note.getTitle() + " - " + note.getFormattedCreationTime());
                }
            }
        }
    }

    private class HighlightListener implements ActionListener {
        private Color color;

        public HighlightListener(Color color) {
            this.color = color;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (highlighter == null) {
                highlighter = noteContentArea.getHighlighter();
            }
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(color);
            int start = noteContentArea.getSelectionStart();
            int end = noteContentArea.getSelectionEnd();
            try {
                highlighter.addHighlight(start, end, painter);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ClearHighlightListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (highlighter != null) {
                highlighter.removeAllHighlights();
            }
        }
    }

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
            fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG files", "jpg", "jpeg"));
            int option = fileChooser.showSaveDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".jpg") && !outputFile.getName().toLowerCase().endsWith(".jpeg")) {
                    outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".jpg");
                }
                try {
                    BufferedImage image = new BufferedImage(noteContentArea.getWidth(), noteContentArea.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics();
                    noteContentArea.paint(graphics);
                    graphics.dispose();
                    ImageIO.write(image, "jpg", outputFile);
                    JOptionPane.showMessageDialog(frame, "Note saved as image successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error saving note as image", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}