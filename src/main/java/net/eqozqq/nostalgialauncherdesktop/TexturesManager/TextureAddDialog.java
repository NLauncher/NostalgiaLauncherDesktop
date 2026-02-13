package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TextureAddDialog extends JDialog {
    private final LocaleManager localeManager;
    private final double scaleFactor;
    private final boolean isDark;

    private JTextField nameField;
    private JTextField pathField;
    private File selectedFile;
    private boolean success = false;

    public TextureAddDialog(Frame owner, LocaleManager localeManager, boolean isDark, double scaleFactor) {
        super(owner, localeManager.get("dialog.addTexture.title", "Add Texture"), true);
        this.localeManager = localeManager;
        this.isDark = isDark;
        this.scaleFactor = scaleFactor;

        setLayout(new BorderLayout());
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder((int) (20 * scaleFactor), (int) (20 * scaleFactor), (int) (20 * scaleFactor),
                (int) (20 * scaleFactor)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets((int) (10 * scaleFactor), (int) (10 * scaleFactor), (int) (10 * scaleFactor),
                (int) (10 * scaleFactor));
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        content.add(new JLabel(localeManager.get("label.textureName", "Texture Name:")), gbc);

        nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        content.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        content.add(new JLabel(localeManager.get("label.selectArchive", "Select Archive:")), gbc);

        pathField = new JTextField(15);
        pathField.setEditable(false);
        gbc.gridx = 1;
        content.add(pathField, gbc);

        JButton browseButton = new JButton(localeManager.get("button.browse", "Browse"));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Archives (zip, rar, tar, 7z)", "zip", "rar", "tar", "7z", "mcpack"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                pathField.setText(selectedFile.getName());
                if (nameField.getText().trim().isEmpty()) {
                    String name = selectedFile.getName();
                    int lastDot = name.lastIndexOf('.');
                    if (lastDot > 0)
                        name = name.substring(0, lastDot);
                    nameField.setText(name);
                }
            }
        });
        gbc.gridx = 2;
        content.add(browseButton, gbc);

        JButton addButton = new JButton(localeManager.get("button.add", "Add"));
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD));
        addButton.addActionListener(e -> handleAdd());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void handleAdd() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.emptyName", "Please enter a name."),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, localeManager.get("error.noFileSelected", "Please select a file."),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        File texturesDir = new File(InstanceManager.getDataRoot(), "textures");
        if (!texturesDir.exists())
            texturesDir.mkdirs();

        String ext = "";
        String fileName = selectedFile.getName();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0)
            ext = fileName.substring(lastDot);

        File targetFile = new File(texturesDir, name + ext);
        try {
            Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            success = true;
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    localeManager.get("error.addTextureFailed", "Failed to add texture: " + e.getMessage()),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}