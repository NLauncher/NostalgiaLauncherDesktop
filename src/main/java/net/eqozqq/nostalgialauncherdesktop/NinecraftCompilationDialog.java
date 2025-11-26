package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class NinecraftCompilationDialog extends JDialog {
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton closeButton;
    private JButton installDepsButton;
    private Runnable installAction;
    private LocaleManager localeManager;

    public NinecraftCompilationDialog(JFrame parent, LocaleManager localeManager) {
        super(parent, localeManager.get("dialog.compilation.title"), true);
        this.localeManager = localeManager;
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        statusLabel = new JLabel(localeManager.get("dialog.compilation.status.preparing"));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(progressBar, BorderLayout.SOUTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        installDepsButton = new JButton(localeManager.get("button.installDependencies"));
        installDepsButton.setVisible(false);
        installDepsButton.addActionListener(e -> {
            if (installAction != null) {
                installAction.run();
            }
        });

        closeButton = new JButton(localeManager.get("button.cancel"));
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> dispose());
        
        bottomPanel.add(installDepsButton);
        bottomPanel.add(closeButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
        });
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public void showInstallButton(Runnable action) {
        SwingUtilities.invokeLater(() -> {
            this.installAction = action;
            installDepsButton.setVisible(true);
            closeButton.setEnabled(true);
            closeButton.setText(localeManager.get("button.close"));
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        });
    }

    public void compilationFinished(boolean success) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(success ? 100 : 0);
            statusLabel.setText(success ? localeManager.get("dialog.compilation.status.success") : localeManager.get("dialog.compilation.status.failed"));
            closeButton.setText(localeManager.get("button.close"));
            closeButton.setEnabled(true);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            
            if (success) {
                installDepsButton.setVisible(false);
            }
        });
    }
}