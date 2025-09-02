package net.eqozqq.nostalgialauncherdesktop.TexturesManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.eqozqq.nostalgialauncherdesktop.Version;
import net.eqozqq.nostalgialauncherdesktop.VersionManager;

public class TexturesManagerDialog extends JDialog {

    private JList<String> versionList;
    private DefaultListModel<String> listModel;
    private String selectedVersion;

    public TexturesManagerDialog(JFrame parent) {
        super(parent, "Textures Manager", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new CardLayout());

        add(createVersionSelectionPanel(), "SELECT_VERSION");
    }

    private JPanel createVersionSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Select the version for which you want to install the texture:", SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        versionList = new JList<>(listModel);
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(versionList), BorderLayout.CENTER);

        JButton selectButton = new JButton("Select");
        selectButton.setPreferredSize(new Dimension(0, 35));
        selectButton.addActionListener(e -> {
            selectedVersion = versionList.getSelectedValue();
            if (selectedVersion != null) {
                getContentPane().add(createTexturePanel(), "TEXTURE_PANEL");
                CardLayout cl = (CardLayout)(getContentPane().getLayout());
                cl.show(getContentPane(), "TEXTURE_PANEL");
                setTitle("Textures Manager - " + selectedVersion);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a version.", "No Version Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        panel.add(selectButton, BorderLayout.SOUTH);

        loadInstalledVersions();
        return panel;
    }

    private JPanel createTexturePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel fileSelectionPanel = new JPanel(new BorderLayout(5, 0));
        JTextField archivePathField = new JTextField(30);
        archivePathField.setEditable(false);
        JButton browseButton = new JButton("Browse...");
        fileSelectionPanel.add(archivePathField, BorderLayout.CENTER);
        fileSelectionPanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(fileSelectionPanel, gbc);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton unpackButton = new JButton("Unpack Textures");
        JButton restoreButton = new JButton("Restore Default Textures");
        
        Dimension buttonSize = new Dimension(0, 35);
        unpackButton.setPreferredSize(buttonSize);
        restoreButton.setPreferredSize(buttonSize);

        buttonsPanel.add(unpackButton);
        buttonsPanel.add(restoreButton);
        
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonsPanel, gbc);


        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Texture Archive");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archives", "zip", "rar", "tar", "7z"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        unpackButton.addActionListener(e -> {
            String path = archivePathField.getText();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an archive file.", "No File Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                File archiveFile = new File(path);
                File versionDir = new File("versions/" + selectedVersion);
                ArchiveExtractor.extract(archiveFile, versionDir);
                JOptionPane.showMessageDialog(this, "Textures installed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to install textures:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        restoreButton.addActionListener(e -> restoreDefaultTextures());

        return panel;
    }

    private void loadInstalledVersions() {
        File versionsDir = new File("versions");
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] directories = versionsDir.list((current, name) -> new File(current, name).isDirectory());
            if (directories != null) {
                Arrays.sort(directories);
                for (String dirName : directories) {
                    if (!dirName.equals("_LevelCache") && !dirName.equals("default_textures")) {
                        listModel.addElement(dirName);
                    }
                }
            }
        }
    }
    
    private void restoreDefaultTextures() {
        VersionManager versionManager = new VersionManager();
        try {
            List<Version> allVersions = versionManager.loadVersions("https://raw.githubusercontent.com/NLauncher/components/main/versions.json");
            Version targetVersion = allVersions.stream()
                .filter(v -> v.getName().equals(selectedVersion))
                .findFirst()
                .orElse(null);

            if (targetVersion == null || targetVersion.getUrl().startsWith("file:")) {
                JOptionPane.showMessageDialog(this, "Default textures can only be restored for official versions.", "Cannot Restore", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this, "This will download the original game files and replace your current textures.\nAre you sure you want to continue?", "Confirm Restore", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;

            ProgressMonitor progressMonitor = new ProgressMonitor(this, "Downloading " + selectedVersion, "", 0, 100);
            progressMonitor.setMillisToPopup(0);

            File apkFile = versionManager.downloadVersion(targetVersion, progress -> {
                progressMonitor.setProgress((int) (progress * 100));
            });
            
            progressMonitor.setNote("Extracting textures...");
            progressMonitor.setProgress(0);

            ArchiveExtractor.extractDefaultTextures(apkFile, new File("versions/" + selectedVersion));
            
            progressMonitor.close();
            JOptionPane.showMessageDialog(this, "Default textures have been restored.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to restore textures:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}