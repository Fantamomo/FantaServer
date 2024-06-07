package at.leisner.server.gui;

import at.leisner.server.util.ColorCodes;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.*;

public class StyledTextArea {
    public static void main(String[] args) {
        JFrame frame = new JFrame("AnsiTextField Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Erstelle das Textpane
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false); // Das Textfeld soll nicht bearbeitbar sein
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Monospace-Schriftart für einheitliche Darstellung

        // Füge das Textpane zur Frame hinzu
        JScrollPane scrollPane = new JScrollPane(textPane);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Setze die Größe der Frame
        frame.setSize(400, 300);
        frame.setVisible(true);

        // Beispieltext mit ANSI-Farbformatierung
        String exampleText = "\u001B[31mThis is red text\u001B[0m\n" +
                ColorCodes.GREEN+"This is green text\u001B[0m\n" +
                "\u001B[33mThis is yellow text\u001B[0m\n" +
                "\u001B[34mThis is blue text\u001B[0m\n" +
                "\u001B[35mThis is purple text\u001B[0m\n" +
                "\u001B[36mThis is cyan text\u001B[0m\n" +
                "\u001B[1mThis is kursive\u001B[0m\n" +
                "\u001B[3mThis is kursive\u001B[0m\n" +
                "\u001B[4mThis is underlined\u001B[0m\n" +
                "\u001B[33m\u001B[7mThis is reverse\u001B[0m\n" +
                "\u001B[9mThis is durchgeschrichen\u001B[0m\n" +
                "\u001B[21mThis is error\u001B[0m\n" +
                "\u001B[0mBack to normal";

        // Füge den Beispieltext zum Textpane hinzu
        addTextWithANSIFormatting(textPane, exampleText);
    }

    // Methode zum Hinzufügen von Text mit ANSI-Farbformatierung zum Textpane
    public static void addTextWithANSIFormatting(JTextPane textPane, String text) {
        // Erzeuge ein neues StyledDocument für das Textpane
        StyledDocument doc = textPane.getStyledDocument();

        // Regulärer Ausdruck zum Erfassen von ANSI-Escape-Sequenzen
        Pattern pattern = Pattern.compile("\u001B\\[[;\\d]*m");
        Matcher matcher = pattern.matcher(text);

        // Erzeuge einen Standardstil für den Text
        Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);

        // Speichert den aktuellen Stil
        Style currentStyle = defaultStyle;

        // Iteriere über den Text und füge Teile mit und ohne Formatierung hinzu
        int lastIndex = 0;
        while (matcher.find()) {
            String formattedText = text.substring(lastIndex, matcher.start());
            appendStyledText(doc, formattedText, currentStyle);
            currentStyle = applyANSIFormatting(textPane, doc, matcher.group(), currentStyle);
            lastIndex = matcher.end();
        }

        // Füge den restlichen Text hinzu
        String restText = text.substring(lastIndex);
        appendStyledText(doc, restText, currentStyle);
    }

    // Methode zum Anwenden von ANSI-Farbformatierung auf den Text im Textpane
    // Methode zum Anwenden von ANSI-Farbformatierung auf den Text im Textpane
    private static Style applyANSIFormatting(JTextPane textPane, StyledDocument doc, String ansiSequence, Style currentStyle) {
        // Entferne das Escapezeichen und die 'm'
        ansiSequence = ansiSequence.substring(2, ansiSequence.length() - 1);

        // Extrahiere die Farb- und Stilinformationen
//        String[] codes = ansiSequence.split(";");
//        for (String code : codes) {
//            int value = Integer.parseInt(code);
            switch (/*value*/ansiSequence) {
                case "0": // Reset
                    currentStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
                    break;
                case "1": // Fettdruck
                    currentStyle = textPane.addStyle("Bold", currentStyle);
                    StyleConstants.setBold(currentStyle, true);
                    break;
                case "3": // Kursiv
                    currentStyle = textPane.addStyle("Italic", currentStyle);
                    StyleConstants.setItalic(currentStyle, true);
                    break;
                case "4": // Unterstrichen
                    currentStyle = textPane.addStyle("Underline", currentStyle);
                    StyleConstants.setUnderline(currentStyle, true);
                    break;
                case "9": // Durchgestrichen
                    currentStyle = textPane.addStyle("Strikethrough", currentStyle);
                    StyleConstants.setStrikeThrough(currentStyle, true);
                    break;
                case "7": // Reverse
                    currentStyle = textPane.addStyle("Reverse", currentStyle);
                    StyleConstants.setBackground(currentStyle, StyleConstants.getForeground(currentStyle));
                    StyleConstants.setForeground(currentStyle, StyleConstants.getBackground(currentStyle));
                    break;
                case "37":
                    currentStyle = textPane.addStyle("BLACK(original WHITE)", currentStyle);
                    StyleConstants.setForeground(currentStyle, Color.BLACK);
                default:
                    for (ColorCodes colorCode : ColorCodes.values()) {
                        if (ansiSequence.equals(colorCode.getAnsiCode())) {
                            currentStyle = textPane.addStyle(colorCode.name(), currentStyle);
                            StyleConstants.setForeground(currentStyle, colorCode.getColor());
                            break;
                        }
                    }
                // Weitere Farben und Stile können hier hinzugefügt werden
            }
//        }

        return currentStyle;
    }

    // Methode zum Anhängen von formatiertem Text an das StyledDocument
    private static void appendStyledText(StyledDocument doc, String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
