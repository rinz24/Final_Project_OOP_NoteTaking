package Prototype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ToDoListApp {
    private JFrame frame;
    private JPanel todoItemsPanel;
    private ArrayList<TodoItem> todoItems;

    public ToDoListApp() {
        todoItems = new ArrayList<>();
        frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        initialize();
    }

    private void initialize() {
        JPanel panel = new JPanel(new BorderLayout());

        // To-do list panel
        JPanel todoPanel = new JPanel(new BorderLayout());
        todoPanel.setBorder(BorderFactory.createTitledBorder("To-Do List"));
        todoItemsPanel = new JPanel(new GridLayout(0, 1));
        JButton addTodoButton = new JButton("Add Todo");
        addTodoButton.addActionListener(new AddTodoListener());
        todoPanel.add(todoItemsPanel, BorderLayout.CENTER);
        todoPanel.add(addTodoButton, BorderLayout.SOUTH);

        panel.add(todoPanel, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }

    private class AddTodoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField todoField = new JTextField(20);
            JTextField dateField = new JTextField(10);
            dateField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE || c == '/')) {
                        e.consume();
                    }
                }
            });
            JButton deleteButton = new JButton("Delete");
            TodoItem todoItem = new TodoItem(todoField, dateField);
            deleteButton.addActionListener(ev -> {
                todoItemsPanel.remove(todoItem.getCheckBox());
                todoItemsPanel.remove(todoField);
                todoItemsPanel.remove(dateField);
                todoItemsPanel.remove(deleteButton);
                todoItems.remove(todoItem);
                sortTodoItems();
                frame.revalidate();
                frame.repaint();
            });

            todoItems.add(todoItem);
            sortTodoItems();
        }
    }

    private void sortTodoItems() {
        todoItems.sort((item1, item2) -> {
            Date date1 = parseDate(item1.getDateField().getText());
            Date date2 = parseDate(item2.getDateField().getText());
            if (date1 == null) return 1;
            if (date2 == null) return -1;
            return date1.compareTo(date2);
        });

        todoItemsPanel.removeAll();
        for (TodoItem item : todoItems) {
            JPanel todoItemPanel = new JPanel(new FlowLayout());
            todoItemPanel.add(item.getCheckBox());
            todoItemPanel.add(item.getTodoField());
            todoItemPanel.add(item.getDateField());
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                todoItemsPanel.remove(todoItemPanel);
                todoItems.remove(item);
                sortTodoItems();
                frame.revalidate();
                frame.repaint();
            });
            todoItemPanel.add(deleteButton);
            todoItemsPanel.add(todoItemPanel);
        }
        frame.revalidate();
        frame.repaint();
    }

    private Date parseDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private class TodoItem {
        private JCheckBox checkBox;
        private JTextField todoField;
        private JTextField dateField;

        public TodoItem(JTextField todoField, JTextField dateField) {
            this.checkBox = new JCheckBox();
            this.todoField = todoField;
            this.dateField = dateField;
        }

        public JCheckBox getCheckBox() {
            return checkBox;
        }

        public JTextField getTodoField() {
            return todoField;
        }

        public JTextField getDateField() {
            return dateField;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListApp::new);
    }
}
