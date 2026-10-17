package com.acadexia.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable Swing UI component factory adhering to the Dark Navy Hover Theme design system.
 * Features:
 * - Glowing hover buttons with rounded geometry & gradient painting
 * - Modern sidebar navigation buttons with active electric-blue indicator pills
 * - Executive KPI stat cards with glowing borders
 * - Styled alternating data tables with clean padding
 * - Focus-glowing text input fields
 */
public final class UIComponents {

    private UIComponents() {}

    /**
     * Modern rounded button with electric blue hover glow and subtle gradients.
     */
    public static class GlowButton extends JButton {
        private final Color baseTop;
        private final Color baseBottom;
        private final Color hoverTop;
        private final Color hoverBottom;
        private final Color glowColor;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int cornerArc = 12;

        public GlowButton(String text, Color baseTop, Color baseBottom, Color hoverTop, Color hoverBottom, Color glowColor) {
            super(text);
            this.baseTop = baseTop;
            this.baseBottom = baseBottom;
            this.hoverTop = hoverTop;
            this.hoverBottom = hoverBottom;
            this.glowColor = glowColor != null ? glowColor : UITheme.CYAN_ACCENT;

            setFont(UITheme.FONT_BOLD);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setCornerArc(int arc) {
            this.cornerArc = arc;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color c1 = isPressed ? baseBottom : (isHovered ? hoverTop : baseTop);
            Color c2 = isPressed ? baseTop : (isHovered ? hoverBottom : baseBottom);

            // Paint glow if hovered
            if (isHovered) {
                g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 60));
                g2.fillRoundRect(0, 0, w, h, cornerArc + 2, cornerArc + 2);
            }

            // Fill gradient background
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(1, 1, w - 2, h - 2, cornerArc, cornerArc);

            // Outline
            if (isHovered) {
                g2.setColor(glowColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, cornerArc, cornerArc);
            } else {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, cornerArc, cornerArc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Creates an elevated card panel with rounded border and subtle dark navy gradient.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, UITheme.CARD_BG, 0, h, new Color(15, 23, 42));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

                g2.setColor(UITheme.CARD_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        return panel;
    }

    /**
     * Creates an interactive KPI / Statistic summary card with glowing hover effect.
     */
    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        return createStatCard(title, value, subtitle, accentColor, null);
    }

    /**
     * Creates an interactive KPI / Statistic summary card with icon, accent glow, and hover elevation.
     */
    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor, String icon) {
        final Color accent = accentColor != null ? accentColor : UITheme.CYAN_ACCENT;

        class HoverStatCard extends JPanel {
            private boolean hovered = false;

            HoverStatCard() {
                setOpaque(false);
                setLayout(new BorderLayout(10, 8));
                setBorder(new EmptyBorder(14, 16, 14, 16));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });

                // Top Header Row (Icon + Title)
                JPanel topRow = new JPanel(new BorderLayout(8, 0));
                topRow.setOpaque(false);

                JLabel lblTitle = new JLabel(title.toUpperCase());
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblTitle.setForeground(UITheme.TEXT_MUTED);
                topRow.add(lblTitle, BorderLayout.CENTER);

                if (icon != null && !icon.isEmpty()) {
                    JLabel lblIcon = new JLabel(icon);
                    lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    topRow.add(lblIcon, BorderLayout.EAST);
                }

                // Center Value
                JLabel lblVal = new JLabel(value);
                lblVal.setFont(UITheme.FONT_STAT_NUM);
                lblVal.setForeground(accent);

                // Bottom Subtitle
                JLabel lblSub = new JLabel(subtitle);
                lblSub.setFont(UITheme.FONT_SMALL);
                lblSub.setForeground(UITheme.TEXT_SECONDARY);

                add(topRow, BorderLayout.NORTH);
                add(lblVal, BorderLayout.CENTER);
                add(lblSub, BorderLayout.SOUTH);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                Color topBg = hovered ? UITheme.CARD_BG_HOVER : UITheme.CARD_BG;
                Color botBg = hovered ? new Color(20, 31, 56) : new Color(14, 21, 38);
                GradientPaint gp = new GradientPaint(0, 0, topBg, 0, h, botBg);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

                // Left Accent Indicator Strip
                g2.setColor(accent);
                g2.fillRoundRect(0, 4, 4, h - 8, 3, 3);

                // Border (Glowing when hovered)
                if (hovered) {
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(UITheme.CARD_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

                g2.dispose();
            }
        }

        return new HoverStatCard();
    }

    /**
     * Creates a modern styled Primary button with Electric Blue glowing hover.
     */
    public static JButton createPrimaryButton(String text) {
        return new GlowButton(
                text,
                new Color(37, 99, 235),       // #2563EB Vivid Blue
                new Color(29, 78, 216),       // #1D4ED8 Deep Blue
                new Color(59, 130, 246),      // #3B82F6 Bright Blue
                new Color(37, 99, 235),
                UITheme.CYAN_ACCENT
        );
    }

    /**
     * Creates a Secondary / Dark Navy elevated button with glowing blue outline.
     */
    public static JButton createSecondaryButton(String text) {
        return new GlowButton(
                text,
                new Color(28, 40, 72),        // Dark Slate Blue
                new Color(18, 26, 48),
                new Color(38, 54, 96),        // Lighter on hover
                new Color(26, 38, 70),
                new Color(59, 130, 246)
        );
    }

    /**
     * Creates a Danger / Crimson action button.
     */
    public static JButton createDangerButton(String text) {
        return new GlowButton(
                text,
                new Color(220, 38, 38),       // Red-600
                new Color(185, 28, 28),       // Red-700
                new Color(239, 68, 68),       // Red-500
                new Color(220, 38, 38),
                new Color(248, 113, 113)
        );
    }

    /**
     * Creates a Success / Emerald action button.
     */
    public static JButton createSuccessButton(String text) {
        return new GlowButton(
                text,
                new Color(5, 150, 105),       // Emerald-600
                new Color(4, 120, 87),        // Emerald-700
                new Color(16, 185, 129),      // Emerald-500
                new Color(5, 150, 105),
                new Color(52, 211, 153)
        );
    }

    /**
     * Creates an Electric Cyan / High-impact accent button.
     */
    public static JButton createElectricButton(String text) {
        GlowButton btn = new GlowButton(
                text,
                new Color(2, 132, 199),       // Sky-600
                new Color(37, 99, 235),       // Blue-600
                new Color(0, 210, 255),       // Cyan Neon
                new Color(2, 132, 199),
                new Color(255, 255, 255)
        );
        btn.setForeground(Color.WHITE);
        return btn;
    }

    /**
     * Modern Sidebar Navigation Button with glowing hover and active indicator pill.
     */
    public static class SidebarNavButton extends JButton {
        private boolean active = false;
        private boolean hovered = false;
        private final String iconText;
        private final String labelText;

        public SidebarNavButton(String icon, String label) {
            super(icon + "  " + label);
            this.iconText = icon;
            this.labelText = label;

            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(UITheme.TEXT_SECONDARY);
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 16, 10, 12));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : (hovered ? Color.WHITE : UITheme.TEXT_SECONDARY));
            repaint();
        }

        public boolean isActive() {
            return active;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (active) {
                // Active background: Dark Blue illuminated gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(26, 42, 80), w, 0, new Color(16, 26, 50));
                g2.setPaint(gp);
                g2.fillRoundRect(4, 2, w - 8, h - 4, 10, 10);

                // Active left glowing electric cyan pill
                g2.setColor(UITheme.CYAN_ACCENT);
                g2.fillRoundRect(4, 5, 4, h - 10, 3, 3);

                // Subtle border
                g2.setColor(new Color(37, 99, 235, 120));
                g2.drawRoundRect(4, 2, w - 8, h - 4, 10, 10);
            } else if (hovered) {
                // Hover background
                g2.setColor(new Color(22, 34, 62));
                g2.fillRoundRect(4, 2, w - 8, h - 4, 10, 10);

                g2.setColor(new Color(59, 130, 246, 80));
                g2.drawRoundRect(4, 2, w - 8, h - 4, 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Creates a styled modern JTable with alternating dark navy row striping,
     * glowing electric blue selection, and clean padding.
     */
    public static JTable createStyledTable(TableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return getModel().isCellEditable(row, column);
            }
        };

        table.setFont(UITheme.FONT_REGULAR);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(UITheme.CARD_BG);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setSelectionBackground(UITheme.PRIMARY);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_BOLD);
        header.setBackground(UITheme.TOPBAR_BG);
        header.setForeground(UITheme.TEXT_SECONDARY);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER));

        // Cell padding renderer with alternating dark navy rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(4, 14, 4, 14));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.CARD_BG : new Color(24, 35, 64));
                    c.setForeground(UITheme.TEXT_PRIMARY);
                } else {
                    c.setBackground(UITheme.PRIMARY);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        return table;
    }

    /**
     * Creates a modern status pill / badge component.
     */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(fg != null ? fg : Color.WHITE);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.setOpaque(false);
        return badge;
    }

    /**
     * Creates a section header banner with subtitle.
     */
    public static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);
        return header;
    }

    /**
     * Creates a text search field with placeholder and glowing electric-blue focus outline.
     */
    public static JTextField createSearchField(String placeholder, int columns) {
        JTextField tf = new JTextField(columns) {
            private boolean focused = false;
            {
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        focused = true;
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        focused = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(12, 18, 33));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                if (focused) {
                    g2.setColor(UITheme.CYAN_ACCENT);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(UITheme.CARD_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setFont(UITheme.FONT_REGULAR);
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setCaretColor(UITheme.CYAN_ACCENT);
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(7, 12, 7, 12));
        return tf;
    }
}
