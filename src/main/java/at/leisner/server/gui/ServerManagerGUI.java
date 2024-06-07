package at.leisner.server.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.leisner.server.FantaServer;
import at.leisner.server.command.Command;
import at.leisner.server.command.FantaCommandManager;

public class ServerManagerGUI extends JFrame {
    private JTextField inputField;
    private JTextPane outputArea;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionModel;
    private JPopupMenu suggestionPopup;
    private int suggestionIndex = 0;
    private FantaCommandManager commandManager;
    private FantaServer server;
    private final ServerManagerGUI serverManagerGUI;
    private final List<String> commands = new ArrayList<>();
    private int commandIndex = 0;

    public ServerManagerGUI(FantaServer server, FantaCommandManager commandManager) {
        serverManagerGUI = this;
        this.commandManager = commandManager;
        this.server = server;
    }
    public void start() {
        SwingUtilities.invokeLater(this::start0);
    }
    private void start0() {
        setTitle("Command Suggestion Window");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_TAB) {
                    String input = inputField.getText();
                    updateSuggestions(input);
                }
            }
        });

        // Override Tab key behavior for inputField
        inputField.setFocusTraversalKeysEnabled(false);
        InputMap inputMap = inputField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = inputField.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("TAB"), "CycleSuggestion");
        actionMap.put("CycleSuggestion", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cycleSuggestion();
            }
        });

        outputArea = new JTextPane();
        outputArea.setEditable(false);

        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedValue = suggestionList.getSelectedValue();
                    if (selectedValue != null) {
                        updateLastArgument(selectedValue);
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });

        suggestionPopup = new JPopupMenu();
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        suggestionPopup.add(scrollPane);
        suggestionPopup.setFocusable(false);

        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!suggestionPopup.isVisible() && !suggestionModel.isEmpty()) {
                    showSuggestionPopup();
                } else if (inputField.getText().isBlank()) {
                    updateSuggestions("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                suggestionPopup.setVisible(false);
            }
        });

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (commandIndex > 0) {
                        commandIndex--;
                        inputField.setText(commands.get(commandIndex));
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (commandIndex+1 < commands.size()) {
                        commandIndex++;
                        inputField.setText(commands.get(commandIndex));
                    } else {
                        if (commandIndex < commands.size()) commandIndex++;
                        inputField.setText("");
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN && !suggestionModel.isEmpty()) {
                    suggestionList.setSelectedIndex(0);
                    suggestionList.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    commands.add(inputField.getText());
                    commandIndex = commands.size();
                    commandManager.executeCommand(inputField.getText());
                    inputField.setText("");
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selectedValue = suggestionList.getSelectedValue();
                    if (selectedValue != null) {
                        updateLastArgument(selectedValue);
                        appendOutput("Selected suggestion: " + selectedValue);
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });

        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void updateSuggestions(String input) {
        List<String> suggestions;
        if (!input.endsWith(" ")) {
            suggestions = commandManager.getCommandNameList().stream().filter((s) -> s.startsWith(input.strip())).toList();
        } else {
            String[] parts = input.split("\\s+", 2);
            String label = parts[0];
            String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];
            if (parts.length > 1 && parts[1].endsWith(" ")) {
                List<String> temp = new ArrayList<>(List.of(args));
                temp.add("");
                args = temp.toArray(String[]::new);
            }
//            if (parts.length > 1 && parts[1].isBlank()) args = new String[1];
            suggestions = commandManager.getTabCompletionSuggestions(label, args);
        }

        suggestionModel.clear();
        if (suggestions != null) {
            for (String suggestion : suggestions) {
                suggestionModel.addElement(suggestion);
            }
        }

        if (!suggestionModel.isEmpty() && inputField.isFocusOwner()) {
            showSuggestionPopup();
        } else {
            suggestionPopup.setVisible(false);
        }

        // Scroll to the bottom of the list to always show the last suggestion
        if (!suggestionModel.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                int lastIndex = suggestionModel.getSize() - 1;
                suggestionList.ensureIndexIsVisible(lastIndex);
            });
        }
        suggestionPopup.setVisible(false);
        showSuggestionPopup();
    }

    private void showSuggestionPopup() {
        suggestionPopup.show(inputField, 0, -inputField.getHeight() - suggestionPopup.getPreferredSize().height);
        inputField.requestFocusInWindow();
    }

    private void cycleSuggestion() {
        if (suggestionModel.getSize() > 0) {
            if (suggestionIndex >= suggestionModel.getSize()) suggestionIndex = 0;
            updateLastArgument(suggestionModel.get(suggestionIndex));
            suggestionIndex = (suggestionIndex + 1) >= suggestionModel.getSize() ? 0 : suggestionIndex + 1;
        }
    }

    private void updateLastArgument(String newArgument) {
        String input = inputField.getText();
        String[] parts = input.split(" ");
        if (parts.length > 0) {
            if (!input.endsWith(" ")) {
                parts[parts.length - 1] = newArgument;
                inputField.setText(String.join(" ", parts));
            } else {
                inputField.setText(input + newArgument);
            }
        }
    }

    public void appendOutput(String text) {
        if (outputArea == null) return;
        StyledTextArea.addTextWithANSIFormatting(outputArea, text.endsWith("\n") ? text : text+"\n");
    }

    public static void main(String[] args) {
        // Create a mock CommandManager for demonstration purposes
        FantaCommandManager commandManager = new FantaCommandManager(null);
        commandManager.registerGlobalCommand(new Command("test") {
            @Override
            public void execute(String s, String[] strings) {
                System.out.println(s);
                System.out.println(Arrays.toString(strings));
            }

            @Override
            public List<String> tabComplete(String label, String[] args) {
                if (args.length > 5) return new ArrayList<>();
                if (args.length == 1) return List.of("hallo1", "dasdada");
                if (args.length == 2) return List.of("hallo2", "adasdasdd");
                if (args.length == 3) return List.of("hallo3", "dasdasda");
                if (args.length == 4) return List.of("hallo4", "opjkl,");
                return List.of("hallo6", "u0ojio9u0ß0poioz9poih");
            }
        });
        commandManager.registerGlobalCommand(new Command("hallo") {
            @Override
            public void execute(String s, String[] strings) {
                System.out.println(s);
                System.out.println(Arrays.toString(strings));
            }

            @Override
            public List<String> tabComplete(String label, String[] args) {
                if (args.length > 5) return new ArrayList<>();
                if (args.length == 1) return List.of("hallo1", "dasdada");
                if (args.length == 2) return List.of("hallo2", "adasdasdd");
                if (args.length == 3) return List.of("hallo3", "dasdasda");
                if (args.length == 4) return List.of("hallo4", "opjkl,");
                return List.of("hallo6", "12");
            }
        });

        new ServerManagerGUI(null,commandManager).start();
    }
}