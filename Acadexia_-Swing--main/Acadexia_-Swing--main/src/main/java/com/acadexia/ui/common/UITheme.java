package com.acadexia.ui.common;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized design system for Acadexia.
 *
 * All colors, fonts, and spacing are defined here.
 * No class outside this file should hardcode colors or fonts.
 */
public final class UITheme {

    // =========================================================================
    // COLOR PALETTE — Professional academic blue/white theme
    // =========================================================================

    /** Primary brand blue */
    public static final Color PRIMARY         = new Color(0x2563EB);
    /** Darker blue for hover states */
    public static final Color PRIMARY_DARK    = new Color(0x1D4ED8);
    /** Lightest blue tint for selected sidebar items */
    public static final Color PRIMARY_LIGHT   = new Color(0xEFF6FF);

    /** Main application background */
    public static final Color BG              = new Color(0xF8FAFC);
    /** Surface (cards, panels) */
    public static final Color SURFACE         = new Color(0xFFFFFF);
    /** Sidebar background */
    public static final Color SIDEBAR_BG      = new Color(0x1E293B);
    /** Sidebar text (normal) */
    public static final Color SIDEBAR_TEXT    = new Color(0x94A3B8);
    /** Sidebar text (active/hover) */
    public static final Color SIDEBAR_ACTIVE  = new Color(0xFFFFFF);
    /** Sidebar active item background */
    public static final Color SIDEBAR_SEL_BG  = new Color(0x2563EB);

    /** Primary text */
    public static final Color TEXT            = new Color(0x0F172A);
    /** Secondary / muted text */
    public static final Color TEXT_MUTED      = new Color(0x64748B);
    /** Placeholder text */
    public static final Color TEXT_PLACEHOLDER= new Color(0x94A3B8);

    /** Default border color */
    public static final Color BORDER          = new Color(0xE2E8F0);
    /** Input focus border */
    public static final Color BORDER_FOCUS    = new Color(0x2563EB);

    /** Success green */
    public static final Color SUCCESS         = new Color(0x16A34A);
    /** Success light background */
    public static final Color SUCCESS_LIGHT   = new Color(0xF0FDF4);
    /** Warning amber */
    public static final Color WARNING         = new Color(0xF59E0B);
    /** Warning light background */
    public static final Color WARNING_LIGHT   = new Color(0xFFFBEB);
    /** Danger red */
    public static final Color DANGER          = new Color(0xDC2626);
    /** Danger light background */
    public static final Color DANGER_LIGHT    = new Color(0xFEF2F2);
    /** Info blue */
    public static final Color INFO            = new Color(0x0EA5E9);
    /** Info light background */
    public static final Color INFO_LIGHT      = new Color(0xF0F9FF);

    /** Table header background */
    public static final Color TABLE_HEADER_BG = new Color(0xF1F5F9);
    /** Table even row */
    public static final Color TABLE_ROW_EVEN  = new Color(0xFFFFFF);
    /** Table odd row */
    public static final Color TABLE_ROW_ODD   = new Color(0xF8FAFC);
    /** Table selection */
    public static final Color TABLE_SEL       = new Color(0xEFF6FF);
    /** Table selection text */
    public static final Color TABLE_SEL_TEXT  = new Color(0x1D4ED8);

    // ── LEGACY ALIASES — keep old names so existing dashboard code compiles ─────
    /** @deprecated use BG */
    @Deprecated public static final Color BG_DARK         = SIDEBAR_BG;
    /** @deprecated use SURFACE */
    @Deprecated public static final Color CARD_BG         = SURFACE;
    /** @deprecated use BORDER */
    @Deprecated public static final Color CARD_BORDER     = BORDER;
    /** @deprecated use PRIMARY */
    @Deprecated public static final Color PRIMARY_HOVER   = PRIMARY_DARK;
    /** @deprecated use TEXT */
    @Deprecated public static final Color TEXT_PRIMARY    = TEXT;

    // =========================================================================
    // TYPOGRAPHY
    // =========================================================================

    /** App brand / hero title  (32px bold) */
    public static final Font FONT_BRAND       = new Font("Segoe UI", Font.BOLD, 32);
    /** Page / frame title (22px bold) */
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 22);
    /** Section heading (18px bold) */
    public static final Font FONT_SECTION     = new Font("Segoe UI", Font.BOLD, 18);
    /** Card heading (15px bold) */
    public static final Font FONT_HEADING     = new Font("Segoe UI", Font.BOLD, 15);
    /** Body bold (13px bold) */
    public static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD, 13);
    /** Body regular (13px plain) */
    public static final Font FONT_REGULAR     = new Font("Segoe UI", Font.PLAIN, 13);
    /** Subtitle / description (13px plain) */
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 13);
    /** Small label (11px plain) */
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    /** Sidebar nav item (13px bold) */
    public static final Font FONT_NAV         = new Font("Segoe UI", Font.BOLD, 13);
    /** KPI stat number (28px bold) */
    public static final Font FONT_STAT_NUM    = new Font("Segoe UI", Font.BOLD, 28);
    /** KPI stat label (11px plain) */
    public static final Font FONT_STAT_LABEL  = new Font("Segoe UI", Font.PLAIN, 11);
    /** Table cell (13px plain) */
    public static final Font FONT_TABLE       = new Font("Segoe UI", Font.PLAIN, 13);
    /** Table header (12px bold) */
    public static final Font FONT_TABLE_HEADER= new Font("Segoe UI", Font.BOLD, 12);
    /** Button label (13px bold) */
    public static final Font FONT_BUTTON      = new Font("Segoe UI", Font.BOLD, 13);

    // =========================================================================
    // SPACING & SIZING
    // =========================================================================

    public static final int SIDEBAR_WIDTH     = 230;
    public static final int TOPBAR_HEIGHT     = 60;
    public static final int CARD_ARC          = 12;
    public static final int BTN_ARC           = 8;
    public static final int TABLE_ROW_HEIGHT  = 38;
    public static final int TABLE_HEADER_H    = 40;

    // Standard padding insets
    public static final Insets PAD_CARD       = new Insets(20, 22, 20, 22);
    public static final Insets PAD_BTN        = new Insets(9, 18, 9, 18);
    public static final Insets PAD_PAGE       = new Insets(24, 28, 24, 28);

    // =========================================================================

    private UITheme() {}

    /**
     * Initializes FlatLightLaf and injects global UI manager defaults.
     * Call once at application startup before any Swing component is created.
     */
    public static void setup() {
        try {
            FlatLightLaf.setup();

            // Global component styling
            UIManager.put("Button.arc",                  BTN_ARC);
            UIManager.put("Component.arc",               8);
            UIManager.put("TextComponent.arc",           6);
            UIManager.put("ScrollBar.thumbArc",          6);
            UIManager.put("ProgressBar.arc",             6);

            // Colors
            UIManager.put("Panel.background",            BG);
            UIManager.put("ScrollPane.background",       BG);
            UIManager.put("Viewport.background",         BG);
            UIManager.put("Table.background",            TABLE_ROW_EVEN);
            UIManager.put("Table.alternateRowColor",     TABLE_ROW_ODD);
            UIManager.put("Table.selectionBackground",   TABLE_SEL);
            UIManager.put("Table.selectionForeground",   TABLE_SEL_TEXT);
            UIManager.put("Table.rowHeight",             TABLE_ROW_HEIGHT);
            UIManager.put("TableHeader.background",      TABLE_HEADER_BG);
            UIManager.put("TableHeader.foreground",      TEXT_MUTED);
            UIManager.put("TableHeader.font",            FONT_TABLE_HEADER);
            UIManager.put("TableHeader.height",          TABLE_HEADER_H);

            UIManager.put("TabbedPane.showTabSeparators",  true);
            UIManager.put("TabbedPane.underlineColor",     PRIMARY);
            UIManager.put("TabbedPane.selectedForeground", PRIMARY);

            UIManager.put("TextField.background",        SURFACE);
            UIManager.put("PasswordField.background",    SURFACE);
            UIManager.put("ComboBox.background",         SURFACE);

            UIManager.put("Button.background",           PRIMARY);
            UIManager.put("Button.foreground",           Color.WHITE);

            UIManager.put("ToolTip.background",          new Color(0x1E293B));
            UIManager.put("ToolTip.foreground",          Color.WHITE);
            UIManager.put("ToolTip.border",              BorderFactory.createEmptyBorder(6, 10, 6, 10));

        } catch (Exception e) {
            System.err.println("Could not initialize FlatLightLaf: " + e.getMessage());
        }
    }
}
