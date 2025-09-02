package net.eqozqq.nostalgialauncherdesktop;

import javax.swing.*;
import java.awt.*;
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

    public AddCustomVersionDialog(JFrame parent) {
        super(parent, "Add custom version", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Version name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        nameField = new JTextField(20);
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Source:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JPanel sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        urlRadioButton = new JRadioButton("URL");
        fileRadioButton = new JRadioButton("File");
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
        add(new JLabel("Path/URL:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(cards, gbc);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        urlRadioButton.setSelected(true);

        CardLayout cl = (CardLayout)(cards.getLayout());
        urlRadioButton.addActionListener(e -> cl.show(cards, "URL"));
        fileRadioButton.addActionListener(e -> cl.show(cards, "File"));

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("APK Files", "apk"));
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
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (urlRadioButton.isSelected()) {
            path = urlField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "URL cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                new URL(path);
            } catch (MalformedURLException ex) {
                JOptionPane.showMessageDialog(this, "Invalid URL.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            path = filePathField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "File path cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                JOptionPane.showMessageDialog(this, "Invalid file path.", "Error", JOptionPane.ERROR_MESSAGE);
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