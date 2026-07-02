package net.eqozqq.nostalgialauncherdesktop.marketplace;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.eqozqq.nostalgialauncherdesktop.FontManager;
import net.eqozqq.nostalgialauncherdesktop.LocaleManager;
import net.eqozqq.nostalgialauncherdesktop.NostalgiaLauncherDesktop;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MarketplacePanel extends JPanel {
    private final LocaleManager localeManager;
    private final boolean isDark;
    private final double scaleFactor;
    
    private final JPanel gridPanel;
    private final CardLayout cardLayout;
    private final JPanel cards;
    
    private static final String TEXTURES_JSON = "https://raw.githubusercontent.com/MCPE-Source/textures/main/textures.json";
    private static final String MAPS_JSON = "https://raw.githubusercontent.com/MCPE-Source/maps/main/maps.json";
    
    private static final String TEXTURES_BASE_URL = "https://raw.githubusercontent.com/MCPE-Source/textures/main/";
    private static final String MAPS_BASE_URL = "https://raw.githubusercontent.com/MCPE-Source/maps/main/";

    private String currentTab = "maps";
    private JButton mapsBtn;
    private JButton texturesBtn;
    
    private List<MarketplaceItem> allItems = new ArrayList<>();
    private JTextField searchField;
    private JComboBox<String> versionCombo;

    public MarketplacePanel(LocaleManager localeManager, String themeName, double scaleFactor) {
        this.localeManager = localeManager;
        this.isDark = themeName.contains("Dark");
        this.scaleFactor = scaleFactor;
        
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mainCard = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(30, 30, 30, 200));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 200));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        mainCard.setOpaque(false);
        mainCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topHeaderPanel = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) {
                    g2d.setColor(new Color(20, 20, 20, 150));
                } else {
                    g2d.setColor(new Color(200, 200, 200, 150));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        topHeaderPanel.setOpaque(false);
        topHeaderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navPanel.setOpaque(false);
        
        mapsBtn = createTabButton(localeManager.get("market.tab.maps", "Maps"), "maps");
        texturesBtn = createTabButton(localeManager.get("market.tab.textures", "Textures"), "textures");
        
        mapsBtn.addActionListener(e -> switchTab("maps"));
        texturesBtn.addActionListener(e -> switchTab("textures"));
        
        navPanel.add(mapsBtn);
        navPanel.add(texturesBtn);
        
        topHeaderPanel.add(navPanel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        filterPanel.setOpaque(false);
        
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", localeManager.get("market.search", "Search..."));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        
        versionCombo = new JComboBox<>(new String[]{ localeManager.get("market.version.all", "All Versions") });
        versionCombo.addActionListener(e -> applyFilters());
        
        filterPanel.add(searchField);
        filterPanel.add(versionCombo);
        
        topHeaderPanel.add(filterPanel, BorderLayout.CENTER);
        
        mainCard.add(topHeaderPanel, BorderLayout.NORTH);

        gridPanel = new ScrollableGridPanel(new GridLayout(0, 2, 15, 15));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(5, 5, 20, 5));
        
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        
        JPanel loadingPanel = new JPanel(new GridBagLayout());
        loadingPanel.setOpaque(false);
        JLabel loadingLabel = new JLabel(localeManager.get("market.status.loading", "Loading..."));
        loadingLabel.setFont(FontManager.getRegularFont(Font.BOLD, 18f));
        loadingLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        loadingPanel.add(loadingLabel);
        
        cards.add(loadingPanel, "loading");
        cards.add(scrollPane, "grid");
        
        mainCard.add(cards, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);
        
        switchTab("maps");
    }

    private JButton createTabButton(String text, String tabId) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = tabId.equals(currentTab);
                if (isActive) {
                    g2d.setColor(isDark ? new Color(100, 100, 100, 230) : new Color(200, 200, 200, 230));
                } else {
                    g2d.setColor(isDark ? new Color(65, 65, 65, 230) : new Color(240, 240, 240, 230));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(FontManager.getRegularFont(Font.BOLD, 16f * (float) scaleFactor));
        btn.setForeground(isDark ? Color.WHITE : Color.BLACK);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension((int) (150 * scaleFactor), (int) (40 * scaleFactor)));
        return btn;
    }

    private void switchTab(String tab) {
        currentTab = tab;
        if (mapsBtn != null) mapsBtn.repaint();
        if (texturesBtn != null) texturesBtn.repaint();
        if (searchField != null) searchField.setText("");
        cardLayout.show(cards, "loading");
        loadData();
    }

    private void loadData() {
        SwingWorker<List<MarketplaceItem>, Void> worker = new SwingWorker<List<MarketplaceItem>, Void>() {
            @Override
            protected List<MarketplaceItem> doInBackground() throws Exception {
                String urlStr = currentTab.equals("maps") ? MAPS_JSON : TEXTURES_JSON;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    List<MarketplaceItem> list = new Gson().fromJson(reader, new TypeToken<List<MarketplaceItem>>(){}.getType());
                    if (list != null) {
                        list.sort((a, b) -> {
                            String dateA = a.date != null ? a.date : "";
                            String dateB = b.date != null ? b.date : "";
                            return dateB.compareTo(dateA);
                        });
                    }
                    return list;
                }
            }

            @Override
            protected void done() {
                try {
                    allItems = get();
                    updateVersionCombo();
                    applyFilters();
                    cardLayout.show(cards, "grid");
                } catch (Exception e) {
                    e.printStackTrace();
                    gridPanel.removeAll();
                    JLabel err = new JLabel(localeManager.get("market.error.load", "Failed to load data."));
                    err.setForeground(Color.RED);
                    gridPanel.add(err);
                    cardLayout.show(cards, "grid");
                }
            }
        };
        worker.execute();
    }

    private void updateVersionCombo() {
        if (versionCombo == null) return;
        versionCombo.removeAllItems();
        versionCombo.addItem(localeManager.get("market.version.all", "All Versions"));
        if (allItems != null) {
            java.util.Set<String> versions = new java.util.TreeSet<>((a,b) -> b.compareTo(a));
            for (MarketplaceItem item : allItems) {
                if (item.version != null && !item.version.isEmpty()) {
                    versions.add(item.version);
                }
            }
            for (String v : versions) {
                versionCombo.addItem(v);
            }
        }
    }

    private void applyFilters() {
        if (allItems == null) return;
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        boolean isAllVersions = versionCombo == null || versionCombo.getSelectedIndex() <= 0;
        String selectedVersion = isAllVersions ? null : (String) versionCombo.getSelectedItem();
        
        List<MarketplaceItem> filtered = new ArrayList<>();
        for (MarketplaceItem item : allItems) {
            boolean matchesSearch = searchText.isEmpty() || 
                (item.title != null && item.title.toLowerCase().contains(searchText)) ||
                (item.short_description != null && item.short_description.toLowerCase().contains(searchText));
                
            boolean matchesVersion = isAllVersions || (item.version != null && item.version.equals(selectedVersion));
            
            if (matchesSearch && matchesVersion) {
                filtered.add(item);
            }
        }
        populateGrid(filtered);
    }

    private void populateGrid(List<MarketplaceItem> items) {
        gridPanel.removeAll();
        if (items != null) {
            String baseUrl = currentTab.equals("maps") ? MAPS_BASE_URL : TEXTURES_BASE_URL;
            for (MarketplaceItem item : items) {
                gridPanel.add(createItemCard(item, baseUrl));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createItemCard(MarketplaceItem item, String baseUrl) {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDark) g2d.setColor(new Color(45, 45, 45, 230));
                else g2d.setColor(new Color(230, 230, 230, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel imageContainer = new JPanel() {
            private BufferedImage image;
            private boolean loaded = false;
            
            {
                setOpaque(false);
                setPreferredSize(new Dimension((int)(200*scaleFactor), (int)(180*scaleFactor)));
                if (item.thumbnail != null) {
                    loadThumbnailImage(this, baseUrl + item.thumbnail);
                }
            }

            public void setImage(BufferedImage img) {
                this.image = img;
                this.loaded = true;
                repaint();
            }

            public void setError() {
                this.loaded = true;
                this.image = null;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                java.awt.geom.RoundRectangle2D roundedRectangle = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.clip(roundedRectangle);

                if (loaded && image != null) {
                    g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2d.setColor(isDark ? new Color(60, 60, 60) : new Color(200, 200, 200));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(isDark ? Color.WHITE : Color.BLACK);
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = loaded ? localeManager.get("market.status.noImage", "No Image") : localeManager.get("market.status.loadingImage", "Loading...");
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(text, x, y);
                }
                g2d.dispose();
            }
        };
        card.add(imageContainer, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(12, 12, 15, 12));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(item.title != null ? item.title : localeManager.get("market.unknown", "Unknown"));
        titleLabel.setFont(FontManager.getRegularFont(Font.BOLD, 15f * (float)scaleFactor));
        titleLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
        titlePanel.add(titleLabel);
        
        JLabel versionLabel = new JLabel("  " + (item.version != null ? item.version : ""));
        versionLabel.setFont(FontManager.getRegularFont(Font.BOLD, 12f * (float)scaleFactor));
        versionLabel.setForeground(isDark ? new Color(180, 180, 180) : new Color(100, 100, 100));
        titlePanel.add(versionLabel);
        
        infoPanel.add(titlePanel);
        
        infoPanel.add(Box.createVerticalStrut(6));
        
        JLabel descLabel = new JLabel("<html><body style='width:" + (int)(200*scaleFactor) + "px; color:" + (isDark ? "#C8C8C8" : "#3C3C3C") + "'>" + (item.short_description != null ? item.short_description : "") + "</body></html>");
        descLabel.setFont(FontManager.getRegularFont(Font.PLAIN, 12f * (float)scaleFactor));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descLabel);
        
        card.add(infoPanel, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MarketplaceItemDialog((Frame) SwingUtilities.getWindowAncestor(MarketplacePanel.this),
                        item, baseUrl, isDark, scaleFactor, localeManager).setVisible(true);
            }
        });

        return card;
    }

    private void loadThumbnailImage(JPanel container, String urlStr) {
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                String fixedUrl = urlStr.replace(" ", "%20");
                BufferedImage img = ImageIO.read(new URL(fixedUrl));
                if (img != null) {
                    int w = (int) (200 * scaleFactor);
                    int h = (int) (180 * scaleFactor);
                    BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D bGr = bimg.createGraphics();
                    bGr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    bGr.drawImage(img, 0, 0, w, h, null);
                    bGr.dispose();
                    return bimg;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        try {
                            container.getClass().getMethod("setImage", BufferedImage.class).invoke(container, img);
                        } catch (Exception ignored) {}
                    } else {
                        try {
                            container.getClass().getMethod("setError").invoke(container);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    try {
                        container.getClass().getMethod("setError").invoke(container);
                    } catch (Exception ignored) {}
                }
            }
        };
        worker.execute();
    }

    private class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            scrollbar.setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(8, super.getPreferredSize(c).height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isThumbRollover() || isDragging) {
                g2d.setColor(isDark ? new Color(255, 255, 255, 80) : new Color(0, 0, 0, 80));
            } else {
                g2d.setColor(isDark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 40));
            }

            g2d.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
            g2d.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }
    }

    private class ScrollableGridPanel extends JPanel implements Scrollable {
        public ScrollableGridPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
