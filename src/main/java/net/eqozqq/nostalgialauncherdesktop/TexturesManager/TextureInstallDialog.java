package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class TextureInstallDialog extends JDialog {
    private final LocaleManager localeManager;
    private final double scaleFactor;
    private final File archiveFile;

    private JComboBox<String> instanceComboBox;
    private JComboBox<String> versionComboBox;
    private JButton installButton;
    private boolean success = false;

    public TextureInstallDialog(Frame owner, LocaleManager localeManager, double scaleFactor, File archiveFile) {
        super(owner, localeManager.get("dialog.installTexture.title", "Install Texture"), true);
        this.localeManager = localeManager;
        this.scaleFactor = scaleFactor;
        this.archiveFile = archiveFile;

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
        content.add(new JLabel(localeManager.get("label.selectInstance", "Select Instance:")), gbc);

        instanceComboBox = new JComboBox<>(InstanceManager.getInstance().getInstances().toArray(new String[0]));
        instanceComboBox.setSelectedItem(InstanceManager.getInstance().getActiveInstance());
        instanceComboBox.addActionListener(e -> updateVersions());
        gbc.gridx = 1;
        content.add(instanceComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        content.add(new JLabel(localeManager.get("label.selectVersion", "Select Version:")), gbc);

        versionComboBox = new JComboBox<>();
        gbc.gridx = 1;
        content.add(versionComboBox, gbc);

        installButton = new JButton(localeManager.get("button.install", "Install"));
        installButton.setFont(installButton.getFont().deriveFont(Font.BOLD));
        installButton.addActionListener(e -> handleInstall());

        updateVersions();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(installButton);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void updateVersions() {
        String selectedInstance = (String) instanceComboBox.getSelectedItem();
        if (selectedInstance == null)
            return;

        versionComboBox.removeAllItems();

        String oldInstance = InstanceManager.getInstance().getActiveInstance();
        InstanceManager.getInstance().setActiveInstance(selectedInstance);

        File versionsDir = new File(InstanceManager.getInstance().resolvePath("versions"));
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] dirs = versionsDir.list((current, name) -> new File(current, name).isDirectory());
            if (dirs != null) {
                Arrays.sort(dirs);
                for (String dir : dirs) {
                    if (!dir.equals("_LevelCache") && !dir.equals("default_textures")
                            && !dir.equals("cache") && !dir.startsWith(".")
                            && !dir.contains("launcher_components")) {
                        versionComboBox.addItem(dir);
                    }
                }
            }
        }

        InstanceManager.getInstance().setActiveInstance(oldInstance);

        if (installButton != null) {
            installButton.setEnabled(versionComboBox.getItemCount() > 0);
        }
    }

    private void handleInstall() {
        String selectedInstance = (String) instanceComboBox.getSelectedItem();
        String selectedVersion = (String) versionComboBox.getSelectedItem();

        if (selectedInstance == null || selectedVersion == null)
            return;

        String confirmMsg = localeManager.get("dialog.confirmInstallTexture.message",
                archiveFile != null ? archiveFile.getName() : "textures", selectedVersion, selectedInstance);
        int choice = JOptionPane.showConfirmDialog(this, confirmMsg,
                localeManager.get("dialog.confirmInstall.title", "Confirm Installation"),
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION)
            return;

        String oldInstance = InstanceManager.getInstance().getActiveInstance();
        InstanceManager.getInstance().setActiveInstance(selectedInstance);

        File destDir = new File(InstanceManager.getInstance().resolvePath("versions/" + selectedVersion));
        try {
            ArchiveExtractor.install(archiveFile, destDir);
            success = true;
            JOptionPane.showMessageDialog(this, localeManager.get("info.texturesInstalled"),
                    localeManager.get("dialog.success.title"), JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localeManager.get("error.installTexturesFailed", e.getMessage()),
                    localeManager.get("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        } finally {
            InstanceManager.getInstance().setActiveInstance(oldInstance);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}