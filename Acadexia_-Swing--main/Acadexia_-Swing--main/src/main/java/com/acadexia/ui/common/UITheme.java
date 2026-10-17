package com.acadexia.ui.common;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Premium Dark Navy Design System configuration for Acadexia.
 * Features:
 * - Deep navy canvas background (#0A0E1A / #0F172A)
 * - Elevated dark blue cards & panels (#131D36 / #192646)
 * - Vibrant electric blue (#2563EB) and neon cyan (#00D2FF) glowing accents
 * - Crisp white & soft slate typography
 * - Smooth hover feedback, rounded borders, and executive aesthetics
 */
public final class UITheme {

    // --- Premium Dark Navy & Dark Blue Palette ---
    public static final Color BG_DARK          = new Color(10, 14, 26);     // #0A0E1A Deepest Midnight Navy
    public static final Color BG_DARK_ALT      = new Color(15, 23, 42);     // #0F172A Slate Navy Base
    public static final Color BG_SURFACE       = new Color(13, 20, 36);     // #0D1424 Surface Canvas
    public static final Color SIDEBAR_BG       = new Color(8, 12, 22);      // #080C16 Ultra-dark Navy Sidebar
    public static final Color SIDEBAR_BORDER   = new Color(28, 42, 74);     // #1C2A4A Sidebar border

    // Elevated Panels & Cards
    public static final Color CARD_BG          = new Color(19, 29, 54);     // #131D36 Elevated Dark Blue Card
    public static final Color CARD_BG_HOVER    = new Color(25, 38, 70);     // #192646 Card Hover
    public static final Color CARD_BORDER      = new Color(34, 52, 94);     // #22345E Sleek Border
    public static final Color CARD_BORDER_GLOW = new Color(0, 210, 255);    // #00D2FF Glowing Border
    public static final Color TOPBAR_BG        = new Color(13, 19, 34);     // #0D1322 Topbar background

    // Electric Blue & Vibrant Accents
    public static final Color PRIMARY          = new Color(37, 99, 235);    // #2563EB Electric Blue Accent
    public static final Color PRIMARY_HOVER    = new Color(59, 130, 246);   // #3B82F6 Bright Blue Hover
    public static final Color PRIMARY_GLOW     = new Color(0, 210, 255, 120); // Neon Cyan-Blue Glow
    public static final Color CYAN_ACCENT      = new Color(0, 210, 255);    // #00D2FF Neon Cyan
    public static final Color ACCENT_INDIGO    = new Color(99, 102, 241);   // #6366F1 Modern Indigo
    public static final Color ACCENT_PURPLE    = new Color(168, 85, 247);   // #A855F7 Violet Accent

    // Typography Colors
    public static final Color TEXT_PRIMARY     = new Color(248, 250, 252);  // #F8FAFC Crisp White
    public static final Color TEXT_SECONDARY   = new Color(203, 213, 225);  // #CBD5E1 Slate Light
    public static final Color TEXT_MUTED       = new Color(148, 163, 184);  // #94A3B8 Slate Muted
    public static final Color TEXT_DIM         = new Color(100, 116, 139);  // #64748B Dim Slate

    // Semantic Status Colors
    public static final Color SUCCESS          = new Color(16, 185, 129);   // #10B981 Emerald
    public static final Color WARNING          = new Color(245, 158, 11);   // #F59E0B Amber
    public static final Color DANGER           = new Color(239, 68, 68);    // #EF4444 Rose Red
    public static final Color INFO             = new Color(14, 165, 233);   // #0EA5E9 Sky Blue

    // Modern Typography
    public static final Font FONT_HERO         = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION      = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD         = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL        = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_NUM     = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_STAT_NUM_LG  = new Font("Segoe UI", Font.BOLD, 32);

    private UITheme() {}

    /**
     * Initializes FlatLaf Dark theme and injects global UI defaults.
     */
    public static void setup() {
        try {
            FlatDarkLaf.setup();

            // Component roundness & geometry
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 10);

            // ScrollBar Colors
            UIManager.put("ScrollBar.track", new Color(10, 14, 26));
            UIManager.put("ScrollBar.thumb", new Color(28, 42, 74));
            UIManager.put("ScrollBar.hoverThumbColor", new Color(45, 68, 118));

            // TabbedPane Styling
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", new Color(25, 38, 70));
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", CYAN_ACCENT);
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));

            // Table Styling
            UIManager.put("Table.background", CARD_BG);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", PRIMARY);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", new Color(26, 38, 68));
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("TableHeader.font", FONT_BOLD);
            UIManager.put("TableHeader.height", 38);
            UIManager.put("TableHeader.background", new Color(13, 19, 34));
            UIManager.put("TableHeader.foreground", TEXT_SECONDARY);

            // Input Fields
            UIManager.put("TextField.background", new Color(12, 18, 33));
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.caretColor", CYAN_ACCENT);
            UIManager.put("PasswordField.background", new Color(12, 18, 33));
            UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
            UIManager.put("ComboBox.background", new Color(16, 24, 43));
            UIManager.put("ComboBox.foreground", TEXT_PRIMARY);

        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf Dark: " + e.getMessage());
        }
    }
}
