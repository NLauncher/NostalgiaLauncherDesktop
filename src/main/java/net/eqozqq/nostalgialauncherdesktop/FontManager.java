package net.eqozqq.nostalgialauncherdesktop;

import java.awt.Font;
import java.io.InputStream;

public class FontManager {
    private static Font minecraftFont = null;
    private static Font regularFont = null;
    private static Font blackFont = null;

    private static Font loadFont(String resourcePath) {
        try (InputStream fontStream = FontManager.class.getResourceAsStream(resourcePath)) {
            if (fontStream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, fontStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Font getMinecraftFont(int style, float size) {
        if (minecraftFont == null) {
            minecraftFont = loadFont("/Minecraft.ttf");
        }
        if (minecraftFont != null) {
            return minecraftFont.deriveFont(style, size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    public static Font getRegularFont(int style, float size) {
        if (regularFont == null) {
            regularFont = loadFont("/MPLUS1p-Regular.ttf");
        }
        if (regularFont != null) {
            return regularFont.deriveFont(style, size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    public static Font getBlackFont(int style, float size) {
        if (blackFont == null) {
            blackFont = loadFont("/MPLUS1p-Black.ttf");
        }
        if (blackFont != null) {
            return blackFont.deriveFont(style, size);
        }
        return new Font("SansSerif", style, (int) size);
    }
}
