package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;

public class HelpDialog extends JDialog {

    public HelpDialog(Component parent, LocaleManager localeManager) {
        super(getParentFrame(parent), localeManager.get("help.dialog.title", "Having a problem?"), true);

        setSize(550, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.questionIcon"));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        mainPanel.add(iconLabel, BorderLayout.WEST);

        String docUrl = "https://nlauncher.github.io/docs/common-problems.html";
        String discordUrl = "https://discord.gg/4fv4RrTav4";

        String htmlText = "<html><body style='width: 350px; font-family: sans-serif;'>" +
                "<p style='margin-bottom: 10px;'>" +
                localeManager.get("help.dialog.text1",
                        "If you are experiencing any issues, first check whether the solution is described on the common problems page:")
                +
                "</p>" +
                "<a href='" + docUrl + "'>" + docUrl + "</a>" +
                "</p>" +
                "<p>" +
                localeManager.get("help.dialog.text2",
                        "If no solution is found, join our Discord server where you can describe your problem and get help from the community.")
                +
                "</p>" +
                "</body></html>";

        JEditorPane textPane = new JEditorPane("text/html", htmlText);
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setFont(UIManager.getFont("Label.font"));

        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                openUrl(e.getURL().toString());
            }
        });

        mainPanel.add(textPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton discordButton = new JButton(localeManager.get("help.dialog.discord", "Discord"));
        discordButton.addActionListener(e -> openUrl(discordUrl));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());

        buttonPanel.add(discordButton);
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        getRootPane().setDefaultButton(okButton);
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception ignored) {
        }

        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("linux") || os.contains("unix")) {
                Runtime.getRuntime().exec(new String[] { "xdg-open", url });
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[] { "open", url });
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[] { "cmd", "/c", "start", url.replace("&", "^&") });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not open browser. Please visit:\n" + url,
                    "Open URL", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static JFrame getParentFrame(Component component) {
        if (component == null)
            return null;
        if (component instanceof JFrame)
            return (JFrame) component;
        return (JFrame) SwingUtilities.getWindowAncestor(component);
    }

    public static void show(Component parent, LocaleManager localeManager) {
        SwingUtilities.invokeLater(() -> {
            HelpDialog dialog = new HelpDialog(parent, localeManager);
            dialog.setVisible(true);
        });
    }
}
