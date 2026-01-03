package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialog {
    private static final String APP_VERSION = "1.8.0";

    public static void showError(Component parent, String title, String message, Throwable exception,
            LocaleManager localeManager) {
        StringBuilder fullError = new StringBuilder();
        fullError.append(message);

        if (exception != null) {
            fullError.append("\n\n");
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            fullError.append(sw.toString());
        }

        fullError.append("\n\n--- ").append(localeManager.get("dialog.errorDetails.systemInfo")).append(" ---\n");
        fullError.append("App: NostalgiaLauncher Desktop v").append(APP_VERSION).append("\n");
        fullError.append("OS: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append("\n");
        fullError.append("Arch: ").append(System.getProperty("os.arch")).append("\n");
        fullError.append("Java: ").append(System.getProperty("java.version")).append(" (")
                .append(System.getProperty("java.vendor")).append(")");

        JTextArea textArea = new JTextArea(fullError.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JButton copyButton = new JButton(localeManager.get("button.copyError"));
        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(fullError.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            copyButton.setText("✓ " + localeManager.get("button.copy"));
        });

        JButton closeButton = new JButton(localeManager.get("button.close"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(copyButton);
        buttonPanel.add(closeButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                localeManager.get("dialog.errorDetails.title") + " - " + title, true);
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        closeButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    public static void showError(Component parent, String title, String message, LocaleManager localeManager) {
        showError(parent, title, message, null, localeManager);
    }

    public static void showError(Component parent, LocaleManager localeManager, Throwable exception) {
        String title = localeManager.get("dialog.error.title");
        String message = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
        showError(parent, title, message, exception, localeManager);
    }
}
