package net.eqozqq.nostalgialauncherdesktop;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class NavigationPanel extends JPanel {

    private final LocaleManager localeManager;
    private final double scaleFactor;
    private final boolean isDark;
    
    private final List<NavButton> navButtons = new ArrayList<>();
    private final List<NavButton> allButtons = new ArrayList<>(); 
    private NavButton selectedButton;
    private Consumer<String> onNavigate;
    
    private boolean isCollapsed;
    private final NavButton collapseBtn;
    private final Preferences prefs;
    
    private final int EXPANDED_WIDTH;
    private final int COLLAPSED_WIDTH;
    private static final String PREF_COLLAPSED = "nav_collapsed";

    public static final String NAV_HOME = "home";
    public static final String NAV_WORLDS = "worlds";
    public static final String NAV_TEXTURES = "textures";
    public static final String NAV_INSTANCES = "instances";
    public static final String NAV_SETTINGS = "settings";

    public NavigationPanel(LocaleManager localeManager, double scaleFactor, String themeName) {
        this.localeManager = localeManager;
        this.scaleFactor = scaleFactor;
        this.isDark = themeName.contains("Dark");
        this.prefs = Preferences.userNodeForPackage(NavigationPanel.class);
        this.isCollapsed = prefs.getBoolean(PREF_COLLAPSED, false);
        
        this.EXPANDED_WIDTH = (int) (220 * scaleFactor);
        this.COLLAPSED_WIDTH = (int) (60 * scaleFactor);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(isCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH, 0));
        setOpaque(false);

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.setBorder(new EmptyBorder(
                (int) (20 * scaleFactor), (int) (10 * scaleFactor),
                (int) (10 * scaleFactor), (int) (10 * scaleFactor)));

        NavButton homeBtn = createNavButton(
                "icons/home_outline.svg", "icons/home_fill.svg",
                localeManager.get("nav.home"), NAV_HOME);
        
        NavButton worldsBtn = createNavButton(
                "icons/explore_outline.svg", "icons/explore_fill.svg",
                localeManager.get("nav.worlds"), NAV_WORLDS);
        
        NavButton texturesBtn = createNavButton(
                "icons/texture_outline.svg", "icons/texture_fill.svg",
                localeManager.get("nav.textures"), NAV_TEXTURES);
        
        NavButton instancesBtn = createNavButton(
                "icons/stacks_outline.svg", "icons/stacks_fill.svg",
                localeManager.get("nav.instances"), NAV_INSTANCES);
        
        NavButton settingsBtn = createNavButton(
                "icons/settings_outline.svg", "icons/settings_fill.svg",
                localeManager.get("nav.settings"), NAV_SETTINGS);

        addNavButton(topSection, homeBtn);
        addNavButton(topSection, worldsBtn);
        addNavButton(topSection, texturesBtn);
        addNavButton(topSection, instancesBtn);
        
        topSection.add(Box.createVerticalStrut((int) (20 * scaleFactor)));
        topSection.add(new JSeparator(SwingConstants.HORIZONTAL) {{
            setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            setForeground(isDark ? new Color(80, 80, 80) : new Color(200, 200, 200));
            setBackground(isDark ? new Color(80, 80, 80) : new Color(200, 200, 200));
        }});
        topSection.add(Box.createVerticalStrut((int) (20 * scaleFactor)));
        
        addNavButton(topSection, settingsBtn);

        navButtons.add(homeBtn);
        navButtons.add(worldsBtn);
        navButtons.add(texturesBtn);
        navButtons.add(instancesBtn);
        navButtons.add(settingsBtn);

        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.setBorder(new EmptyBorder(
                (int) (10 * scaleFactor), (int) (10 * scaleFactor),
                (int) (20 * scaleFactor), (int) (10 * scaleFactor)));

        NavButton discordBtn = createLinkButton("icons/discord.svg", localeManager.get("nav.discord"),
                "https://discord.gg/4fv4RrTav4");
        NavButton websiteBtn = createLinkButton("icons/globe_outline.svg", localeManager.get("nav.website"),
                "https://nlauncher.github.io/");

        addNavButton(bottomSection, discordBtn);
        addNavButton(bottomSection, websiteBtn);
        
        bottomSection.add(Box.createVerticalGlue());
        
        String initialIcon = isCollapsed ? "icons/keyboard_double_arrow_right.svg" : "icons/keyboard_double_arrow_left.svg";
        collapseBtn = new NavButton(initialIcon, initialIcon, "", null);
        
        collapseBtn.setIconOnly(true);
        
        for (java.awt.event.MouseListener ml : collapseBtn.getMouseListeners()) {
            collapseBtn.removeMouseListener(ml);
        }
        collapseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleNavigation();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                collapseBtn.setHovered(true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                collapseBtn.setHovered(false);
            }
        });

        bottomSection.add(collapseBtn);
        allButtons.add(collapseBtn);

        add(topSection, BorderLayout.NORTH);
        add(bottomSection, BorderLayout.SOUTH);

        if (isCollapsed) {
            for (NavButton btn : allButtons) {
                btn.setCollapsed(true);
            }
        }

        setSelectedNav(NAV_HOME);
    }

    private void toggleNavigation() {
        isCollapsed = !isCollapsed;
        prefs.putBoolean(PREF_COLLAPSED, isCollapsed);
        
        setPreferredSize(new Dimension(isCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH, 0));
        
        for (NavButton btn : allButtons) {
            btn.setCollapsed(isCollapsed);
        }
        
        String iconPath = isCollapsed ? "icons/keyboard_double_arrow_right.svg" : "icons/keyboard_double_arrow_left.svg";
        collapseBtn.updateIcons(iconPath, iconPath);
        
        revalidate();
        repaint();
        
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isDark) {
            g2d.setColor(new Color(30, 30, 30, 180));
        } else {
            g2d.setColor(new Color(245, 245, 245, 180));
        }
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
        super.paintComponent(g);
    }

    private void addNavButton(JPanel panel, NavButton button) {
        panel.add(button);
        panel.add(Box.createVerticalStrut((int) (5 * scaleFactor)));
    }

    private Font getRegularFont(int style, float size) {
        try (InputStream fontStream = NavigationPanel.class.getResourceAsStream("/MPLUS1p-Regular.ttf")) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("SansSerif", style, (int) size);
    }

    private NavButton createNavButton(String iconPathOutline, String iconPathFill, String text, String navId) {
        NavButton button = new NavButton(iconPathOutline, iconPathFill, text, navId);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedNav(navId);
                if (onNavigate != null) {
                    onNavigate.accept(navId);
                }
            }
        });
        allButtons.add(button);
        return button;
    }

    private NavButton createLinkButton(String iconPath, String text, String url) {
        NavButton button = new NavButton(iconPath, iconPath, text, null);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        allButtons.add(button);
        return button;
    }

    public void setSelectedNav(String navId) {
        for (NavButton btn : navButtons) {
            boolean isSelected = btn.getNavId() != null && btn.getNavId().equals(navId);
            btn.setSelected(isSelected);
            if (isSelected) {
                selectedButton = btn;
            }
        }
        repaint();
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    private class NavButton extends JPanel {
        private final String navId;
        private boolean selected = false;
        private boolean hovered = false;
        private boolean isIconOnly = false;
        private final JLabel iconLabel;
        private final JLabel textLabel;
        private final Component spacer;
        
        private FlatSVGIcon iconOutline;
        private FlatSVGIcon iconFill;

        public NavButton(String iconPathOutline, String iconPathFill, String text, String navId) {
            this.navId = navId;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(
                    (int) (10 * scaleFactor), (int) (15 * scaleFactor),
                    (int) (10 * scaleFactor), (int) (15 * scaleFactor)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) (45 * scaleFactor)));

            iconLabel = new JLabel();
            updateIcons(iconPathOutline, iconPathFill);
            
            textLabel = new JLabel(text);
            textLabel.setFont(getRegularFont(Font.PLAIN, (float) (14 * scaleFactor)));
            updateColors();

            add(iconLabel);
            spacer = Box.createHorizontalStrut((int) (15 * scaleFactor));
            add(spacer);
            add(textLabel);
            add(Box.createHorizontalGlue());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    updateColors();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    updateColors();
                    repaint();
                }
            });
        }

        public void updateIcons(String pathOutline, String pathFill) {
            iconOutline = new FlatSVGIcon(pathOutline, (int) (20 * scaleFactor), (int) (20 * scaleFactor));
            iconFill = new FlatSVGIcon(pathFill, (int) (20 * scaleFactor), (int) (20 * scaleFactor));
            iconLabel.setIcon(selected ? iconFill : iconOutline);
            updateIconColor(selected || hovered);
        }

        public void setIconOnly(boolean iconOnly) {
            this.isIconOnly = iconOnly;
            if (iconOnly) {
                textLabel.setVisible(false);
                spacer.setVisible(false);
            }
        }

        public void setCollapsed(boolean collapsed) {
            if (isIconOnly) {
                textLabel.setVisible(false);
                spacer.setVisible(false);
            } else {
                textLabel.setVisible(!collapsed);
                spacer.setVisible(!collapsed);
            }
            
            if (collapsed) {
                 setBorder(new EmptyBorder(
                    (int) (10 * scaleFactor), (int) (10 * scaleFactor),
                    (int) (10 * scaleFactor), (int) (10 * scaleFactor)));
            } else {
                 setBorder(new EmptyBorder(
                    (int) (10 * scaleFactor), (int) (15 * scaleFactor),
                    (int) (10 * scaleFactor), (int) (15 * scaleFactor)));
            }
            revalidate();
        }
        
        public void setHovered(boolean h) {
            this.hovered = h;
            updateColors();
            repaint();
        }

        private void updateColors() {
            FlatSVGIcon currentIcon = selected ? iconFill : iconOutline;
            iconLabel.setIcon(currentIcon);
            
            boolean highlight = selected || hovered;
            
            if (highlight) {
                textLabel.setForeground(isDark ? Color.WHITE : Color.BLACK);
            } else {
                textLabel.setForeground(isDark ? new Color(180, 180, 180) : new Color(100, 100, 100));
            }
            updateIconColor(highlight);
        }

        private void updateIconColor(boolean highlight) {
            Color color;
            if (highlight) {
                color = isDark ? Color.WHITE : Color.BLACK;
            } else {
                color = isDark ? new Color(180, 180, 180) : new Color(100, 100, 100);
            }
            
            FlatSVGIcon currentIcon = (FlatSVGIcon) iconLabel.getIcon();
            if (currentIcon != null) {
                currentIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
                iconLabel.repaint();
            }
        }

        public String getNavId() {
            return navId;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            textLabel.setFont(getRegularFont(Font.PLAIN, (float) (14 * scaleFactor)));
            updateColors();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selected) {
                g2d.setColor(isDark ? new Color(255, 255, 255, 30) : new Color(0, 0, 0, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hovered) {
                g2d.setColor(isDark ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}