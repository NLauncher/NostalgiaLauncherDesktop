package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class AddCustomVersionDialog extends JDialog {
    private JTextField nameField;
    private JTextField urlField;
    private JTextField filePathField;
    private JRadioButton urlRadioButton;
    private JRadioButton fileRadioButton;
    private JButton browseButton;
    private JButton saveButton;
    private JButton cancelButton;
    private Version newVersion;
    private JPanel cards;
    private LocaleManager localeManager;

    public AddCustomVersionDialog(JFrame parent, LocaleManager localeManager) {
        super(parent, localeManager.get("dialog.addCustomVersion.title"), true);
        this.localeManager = localeManager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel(localeManager.get("label.versionName")), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        nameField = new JTextField(20);
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel(localeManager.get("label.source")), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JPanel sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        urlRadioButton = new JRadioButton(localeManager.get("radio.url"));
        fileRadioButton = new JRadioButton(localeManager.get("radio.file"));
        ButtonGroup sourceGroup = new ButtonGroup();
        sourceGroup.add(urlRadioButton);
        sourceGroup.add(fileRadioButton);
        sourcePanel.add(urlRadioButton);
        sourcePanel.add(fileRadioButton);
        add(sourcePanel, gbc);

        cards = new JPanel(new CardLayout());
        urlField = new JTextField(20);

        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePathField = new JTextField(15);
        browseButton = new JButton("...");
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);

        cards.add(urlField, "URL");
        cards.add(filePanel, "File");

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel(localeManager.get("label.pathUrl")), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(cards, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel x86InfoLabel = new JLabel(localeManager.get("info.x86ApkOnly"));
        x86InfoLabel.setFont(x86InfoLabel.getFont().deriveFont(Font.ITALIC, 11f));
        x86InfoLabel.setForeground(new Color(100, 100, 100));
        add(x86InfoLabel, gbc);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton(localeManager.get("button.save"));
        cancelButton = new JButton(localeManager.get("button.cancel"));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        urlRadioButton.setSelected(true);

        CardLayout cl = (CardLayout) (cards.getLayout());
        urlRadioButton.addActionListener(e -> cl.show(cards, "URL"));
        fileRadioButton.addActionListener(e -> cl.show(cards, "File"));

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    localeManager.get("fileChooser.apkFiles"), "apk"));
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
            }
        });

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(parent);
    }

    private void onSave() {
        String name = nameField.getText().trim();
        String path;

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.nameEmpty"),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (urlRadioButton.isSelected()) {
            path = urlField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.urlEmpty"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                new URL(path);
            } catch (MalformedURLException ex) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.invalidUrl"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            path = filePathField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.filePathEmpty"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                JOptionPane.showMessageDialog(this, localeManager.get("error.invalidFilePath"),
                        localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            path = file.toURI().toString();
        }

        newVersion = new Version(name, path);
        dispose();
    }

    public Version getNewVersion() {
        return newVersion;
    }
}