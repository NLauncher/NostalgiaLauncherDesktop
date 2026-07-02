package net.eqozqq.nostalgialauncherdesktop.marketplace;

import net.eqozqq.nostalgialauncherdesktop.FontManager;
import net.eqozqq.nostalgialauncherdesktop.Instances.InstanceManager;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.NostalgiaLauncherDesktop;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MarketplaceItemDialog extends JDialog {
    private final LocaleManager localeManager;
    private final MarketplaceItem item;
    private final String baseUrl;
    private final double scaleFactor;

    public MarketplaceItemDialog(Frame parent, MarketplaceItem item, String baseUrl, boolean isDark, double scaleFactor, LocaleManager localeManager) {
        super(parent, item.title, true);
        this.item = item;
        this.baseUrl = baseUrl;
        this.scaleFactor = scaleFactor;
        this.localeManager = localeManager;

        setSize((int) (600 * scaleFactor), (int) (500 * scaleFactor));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(item.title != null ? item.title : localeManager.get("market.unknown", "Unknown"));
        titleLabel.setFont(FontManager.getRegularFont(Font.BOLD, 22f * (float) scaleFactor));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel versionLabel = new JLabel(localeManager.get("market.label.version", "Version: ") + (item.version != null ? item.version : ""));
        versionLabel.setFont(FontManager.getRegularFont(Font.PLAIN, 14f * (float) scaleFactor));
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createVerticalStrut(10));

        if (item.author != null && item.author.name != null) {
            JLabel authorLabel = new JLabel(localeManager.get("market.label.publisher", "Publisher: ") + item.author.name);
            authorLabel.setFont(FontManager.getRegularFont(Font.PLAIN, 14f * (float) scaleFactor));
            authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(authorLabel);
            contentPanel.add(Box.createVerticalStrut(10));
        }

        if (item.screenshots != null && !item.screenshots.isEmpty()) {
            JPanel screenshotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            screenshotsPanel.setOpaque(false);
            for (String screenshotUrl : item.screenshots) {
                JLabel screenshotLabel = new JLabel(localeManager.get("market.status.loadingImage", "Loading image..."), SwingConstants.CENTER);
                screenshotLabel.setPreferredSize(new Dimension((int)(250 * scaleFactor), (int)(140 * scaleFactor)));
                screenshotLabel.setOpaque(true);
                screenshotLabel.setBackground(isDark ? new Color(40, 40, 40) : new Color(220, 220, 220));
                
                loadScreenshotAsync(screenshotLabel, baseUrl + screenshotUrl);
                screenshotsPanel.add(screenshotLabel);
            }
            JScrollPane screenshotsScroll = new JScrollPane(screenshotsPanel);
            screenshotsScroll.setBorder(null);
            screenshotsScroll.setOpaque(false);
            screenshotsScroll.getViewport().setOpaque(false);
            screenshotsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            screenshotsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            screenshotsScroll.setPreferredSize(new Dimension((int)(550 * scaleFactor), (int)(160 * scaleFactor)));
            screenshotsScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, (int)(160 * scaleFactor)));
            screenshotsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            contentPanel.add(screenshotsScroll);
            contentPanel.add(Box.createVerticalStrut(15));
        }

        JLabel descLabel = new JLabel("<html><body style='width:" + (int) (520 * scaleFactor) + "px'>"
                + (item.full_description != null ? item.full_description.replace("\n", "<br>") : "") + "</body></html>");
        descLabel.setFont(FontManager.getRegularFont(Font.PLAIN, 14f * (float) scaleFactor));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descLabel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton installBtn = new JButton(localeManager.get("market.button.install", "Install"));
        installBtn.setFont(FontManager.getRegularFont(Font.BOLD, 14f * (float) scaleFactor));
        installBtn.addActionListener(e -> installItem());
        
        JButton cancelBtn = new JButton(localeManager.get("button.cancel", "Cancel"));
        cancelBtn.addActionListener(e -> dispose());
        
        bottomPanel.add(cancelBtn);
        bottomPanel.add(installBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void installItem() {
        if ("maps".equals(item.type)) {
            java.util.List<String> instances = InstanceManager.getInstance().getInstances();
            if (instances.isEmpty()) {
                JOptionPane.showMessageDialog(this, localeManager.get("market.error.noInstances", "No instances available. Create one first."));
                return;
            }
            String selectedInstance = (String) JOptionPane.showInputDialog(this,
                    localeManager.get("market.dialog.selectInstance", "Select Instance to install map:"),
                    localeManager.get("market.dialog.installMapTitle", "Install Map"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    instances.toArray(),
                    InstanceManager.getInstance().getActiveInstance());

            if (selectedInstance != null) {
                String path;
                if (selectedInstance.equals(InstanceManager.getDefaultInstanceName())) {
                    path = InstanceManager.getDataRoot() + File.separator + "game/storage/games/com.mojang/minecraftWorlds";
                } else {
                    path = InstanceManager.getDataRoot() + File.separator + "instances" + File.separator + selectedInstance + File.separator + "game/storage/games/com.mojang/minecraftWorlds";
                }
                File worldsDir = new File(path);
                if (!worldsDir.exists()) worldsDir.mkdirs();
                
                downloadAndExtractZip(worldsDir, localeManager.get("market.progress.extractingMap", "Extracting Map..."));
            }
        } else {
            File texturesDir = NostalgiaLauncherDesktop.getInstance().getTexturesDirectory();
            if (!texturesDir.exists()) texturesDir.mkdirs();
            
            downloadAndSaveZip(texturesDir, localeManager.get("market.progress.downloadingTexture", "Downloading Texture..."));
        }
    }

    private void downloadAndExtractZip(File destDir, String progressTitle) {
        if (item.file == null) return;
        String fixedUrl = (baseUrl + item.file).replace(" ", "%20");
        ProgressMonitor monitor = new ProgressMonitor(this, progressTitle, localeManager.get("market.progress.connecting", "Connecting..."), 0, 100);
        
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                HttpURLConnection conn = (HttpURLConnection) new URL(fixedUrl).openConnection();
                conn.setRequestMethod("GET");
                int length = conn.getContentLength();
                
                try (InputStream is = conn.getInputStream();
                     ZipInputStream zis = new ZipInputStream(is)) {
                    
                    ZipEntry entry;
                    byte[] buffer = new byte[4096];
                    while ((entry = zis.getNextEntry()) != null) {
                        File newFile = new File(destDir, entry.getName());
                        if (entry.isDirectory()) {
                            newFile.mkdirs();
                        } else {
                            newFile.getParentFile().mkdirs();
                            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                monitor.close();
                try {
                    get();
                    JOptionPane.showMessageDialog(MarketplaceItemDialog.this, localeManager.get("market.info.installComplete", "Installation complete!"));
                    dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MarketplaceItemDialog.this, localeManager.get("market.error.installFailed", "Installation failed: ") + e.getMessage(), localeManager.get("dialog.error.title", "Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void downloadAndSaveZip(File destDir, String progressTitle) {
        if (item.file == null) return;
        String fixedUrl = (baseUrl + item.file).replace(" ", "%20");
        ProgressMonitor monitor = new ProgressMonitor(this, progressTitle, localeManager.get("market.progress.connecting", "Connecting..."), 0, 100);
        
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                HttpURLConnection conn = (HttpURLConnection) new URL(fixedUrl).openConnection();
                conn.setRequestMethod("GET");
                
                String fileName = item.file.substring(item.file.lastIndexOf("/") + 1);
                File destFile = new File(destDir, fileName);
                
                try (InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                monitor.close();
                try {
                    get();
                    JOptionPane.showMessageDialog(MarketplaceItemDialog.this, localeManager.get("market.info.installComplete", "Installation complete!"));
                    dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MarketplaceItemDialog.this, localeManager.get("market.error.installFailed", "Installation failed: ") + e.getMessage(), localeManager.get("dialog.error.title", "Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadScreenshotAsync(JLabel label, String urlStr) {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                String fixedUrl = urlStr.replace(" ", "%20");
                BufferedImage img = ImageIO.read(new URL(fixedUrl));
                if (img != null) {
                    int w = (int)(250*scaleFactor);
                    int h = (int)(140*scaleFactor);
                    BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D bGr = bimg.createGraphics();
                    bGr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    bGr.drawImage(img, 0, 0, w, h, null);
                    bGr.dispose();
                    return new ImageIcon(bimg);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        label.setText("");
                        label.setIcon(icon);
                    } else {
                        label.setText(localeManager.get("market.status.noImage", "No Image"));
                    }
                } catch (Exception e) {
                    label.setText(localeManager.get("market.status.imageError", "Image error"));
                }
            }
        };
        worker.execute();
    }
}
