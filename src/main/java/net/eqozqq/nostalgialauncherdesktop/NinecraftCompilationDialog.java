package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.function.Consumer;

public class NinecraftCompilationDialog extends JDialog {
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton closeButton;
    private JButton installDepsButton;
    private Runnable installAction;
    private LocaleManager localeManager;
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    private JRadioButton originalRepoRadio;
    private JRadioButton customRepoRadio;
    private JTextField repoUrlField;
    private JButton startCompileButton;
    
    private Consumer<String> onStartCompilation;

    private static final String ORIGINAL_REPO = "https://github.com/NLauncher/Ninecraft.git";

    public NinecraftCompilationDialog(JFrame parent, LocaleManager localeManager) {
        super(parent, localeManager.get("dialog.compilation.title"), true);
        this.localeManager = localeManager;
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.add(createSetupPanel(), "SETUP");
        mainPanel.add(createLogPanel(), "LOG");
        
        add(mainPanel);
    }

    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JLabel titleLabel = new JLabel(localeManager.get("dialog.compilation.title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);
        
        gbc.gridy++;
        ButtonGroup group = new ButtonGroup();
        String originalText = localeManager.has("dialog.compilation.source.original") ? 
                localeManager.get("dialog.compilation.source.original") : "Use original Ninecraft repository";
        originalRepoRadio = new JRadioButton(originalText);
        originalRepoRadio.setSelected(true);
        group.add(originalRepoRadio);
        panel.add(originalRepoRadio, gbc);
        
        gbc.gridy++;
        String customText = localeManager.has("dialog.compilation.source.custom") ? 
                localeManager.get("dialog.compilation.source.custom") : "Use another repository";
        customRepoRadio = new JRadioButton(customText);
        group.add(customRepoRadio);
        panel.add(customRepoRadio, gbc);
        
        gbc.gridy++;
        repoUrlField = new JTextField(ORIGINAL_REPO);
        repoUrlField.setEnabled(false);
        panel.add(repoUrlField, gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        String startText = localeManager.has("button.startCompilation") ? 
                localeManager.get("button.startCompilation") : "Start Compilation";
        startCompileButton = new JButton(startText);
        startCompileButton.setPreferredSize(new Dimension(200, 40));
        panel.add(startCompileButton, gbc);
        
        originalRepoRadio.addActionListener(e -> repoUrlField.setEnabled(false));
        customRepoRadio.addActionListener(e -> {
            repoUrlField.setEnabled(true);
            repoUrlField.setText("");
            repoUrlField.requestFocus();
        });
        
        startCompileButton.addActionListener(e -> {
            String url = originalRepoRadio.isSelected() ? ORIGINAL_REPO : repoUrlField.getText().trim();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(this, "URL cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            cardLayout.show(mainPanel, "LOG");
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            if (onStartCompilation != null) {
                onStartCompilation.accept(url);
            }
        });
        
        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
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

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    public void setOnStartCompilation(Consumer<String> action) {
        this.onStartCompilation = action;
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