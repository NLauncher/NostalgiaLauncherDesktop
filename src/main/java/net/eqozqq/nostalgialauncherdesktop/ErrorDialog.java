package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ErrorDialog extends JDialog {

    public ErrorDialog(Component parent, String title, String message) {
        super(getParentFrame(parent), title, true);

        setSize(500, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        mainPanel.add(iconLabel, BorderLayout.WEST);

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(message);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            copyButton.setText("Copied!");
            Timer timer = new Timer(1500, evt -> copyButton.setText("Copy"));
            timer.setRepeats(false);
            timer.start();
        });

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());

        buttonPanel.add(copyButton);
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        getRootPane().setDefaultButton(okButton);
    }

    private static JFrame getParentFrame(Component component) {
        if (component == null)
            return null;
        if (component instanceof JFrame)
            return (JFrame) component;
        return (JFrame) SwingUtilities.getWindowAncestor(component);
    }

    public static void show(Component parent, String title, String message) {
        SwingUtilities.invokeLater(() -> {
            ErrorDialog dialog = new ErrorDialog(parent, title, message);
            dialog.setVisible(true);
        });
    }

    public static void showSync(Component parent, String title, String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            ErrorDialog dialog = new ErrorDialog(parent, title, message);
            dialog.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    ErrorDialog dialog = new ErrorDialog(parent, title, message);
                    dialog.setVisible(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
