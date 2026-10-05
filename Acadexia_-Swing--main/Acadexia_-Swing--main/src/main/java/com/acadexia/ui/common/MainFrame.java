package com.acadexia.ui.common;

import com.acadexia.model.User;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base navigation shell for all role dashboards.
 *
 * Structure:
 *   ┌────────────────────────────────────────────────────┐
 *   │  SIDEBAR (230px)  │  CONTENT AREA (CardLayout)     │
 *   │  ┌─────────────┐  │  ┌──────────────────────────┐  │
 *   │  │  Brand logo │  │  │  Top bar (60px)           │  │
 *   │  │  User info  │  │  ├──────────────────────────┤  │
 *   │  │─────────────│  │  │                          │  │
 *   │  │  Nav items  │  │  │   Page panel             │  │
 *   │  │             │  │  │   (swaps on nav click)   │  │
 *   │  │─────────────│  │  │                          │  │
 *   │  │  Logout     │  │  └──────────────────────────┘  │
 *   │  └─────────────┘  │                                │
 *   └────────────────────────────────────────────────────┘
 *
 * Subclasses call:
 *   addNavItem(icon, label, panelKey, panel)
 *   showPanel(key)
 */
public abstract class MainFrame extends JFrame {

    // ── layout panels ─────────────────────────────────────────────────────────
    private final JPanel     sidebar       = new JPanel();
    private final JPanel     contentShell  = new JPanel(new BorderLayout());
    private final JPanel     contentArea   = new JPanel();
    private final CardLayout cardLayout    = new CardLayout();
    private final JLabel     lblPageTitle  = new JLabel("Dashboard");

    // ── nav state ─────────────────────────────────────────────────────────────
    private final List<SidebarNavButton> navButtons = new ArrayList<>();
    private String activeKey = null;

    // ── constructor ───────────────────────────────────────────────────────────
    protected MainFrame(String frameTitle, int width, int height) {
        setTitle(frameTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setMinimumSize(new Dimension(1024, 660));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG);
        buildShell();
    }

    // ── shell construction ────────────────────────────────────────────────────
    private void buildShell() {
        setLayout(new BorderLayout());

        buildSidebar();

        contentArea.setLayout(cardLayout);
        contentArea.setBackground(UITheme.BG);

        contentShell.setBackground(UITheme.BG);
        contentShell.add(buildTopBar(), BorderLayout.NORTH);
        contentShell.add(contentArea,  BorderLayout.CENTER);

        add(sidebar,       BorderLayout.WEST);
        add(contentShell,  BorderLayout.CENTER);
    }

    // ── sidebar ───────────────────────────────────────────────────────────────
    private void buildSidebar() {
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(UITheme.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(0x334155)));

        // Brand area
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(UITheme.SIDEBAR_BG);
        brand.setBorder(new EmptyBorder(24, 20, 20, 20));

        // Circle logo
        JPanel logoCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, 38, 38);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String a = "A";
                g2.drawString(a, (38 - fm.stringWidth(a)) / 2, (38 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        logoCircle.setOpaque(false);
        logoCircle.setPreferredSize(new Dimension(38, 38));
        logoCircle.setMaximumSize(new Dimension(38, 38));
        logoCircle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrand = new JLabel("ACADEXIA");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Result Management");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(new Color(0x64748B));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logoCircle);
        brand.add(Box.createVerticalStrut(12));
        brand.add(lblBrand);
        brand.add(Box.createVerticalStrut(2));
        brand.add(lblSub);

        // User profile area
        JPanel userArea = buildSidebarUserArea();

        // Nav items (populated by subclasses via addNavItem)
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setBackground(UITheme.SIDEBAR_BG);
        navList.setBorder(new EmptyBorder(8, 0, 8, 0));
        navList.setName("navList");

        // Scroll wrapper for nav
        JScrollPane navScroll = new JScrollPane(navList);
        navScroll.setBorder(null);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.getViewport().setBackground(UITheme.SIDEBAR_BG);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        navScroll.setName("navScroll");

        // Logout button at bottom
        JPanel logoutArea = new JPanel(new BorderLayout());
        logoutArea.setBackground(UITheme.SIDEBAR_BG);
        logoutArea.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(0x334155)),
                new EmptyBorder(12, 12, 20, 12)
        ));

        SidebarNavButton btnLogout = new SidebarNavButton("🚪", "Log Out", null);
        btnLogout.setIsLogout(true);
        btnLogout.addActionListener(e -> performLogout());
        logoutArea.add(btnLogout, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.SIDEBAR_BG);
        top.add(brand,    BorderLayout.NORTH);
        top.add(userArea, BorderLayout.SOUTH);

        sidebar.add(top,        BorderLayout.NORTH);
        sidebar.add(navScroll,  BorderLayout.CENTER);
        sidebar.add(logoutArea, BorderLayout.SOUTH);
    }

    private JPanel buildSidebarUserArea() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(0x162032));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        User user = SessionManager.getInstance().getCurrentUser();
        String name = user != null ? user.getFullName()  : "User";
        String role = user != null ? user.getRole().name() : "ROLE";

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, 36, 36);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.setColor(Color.WHITE);
                String initial = name.isEmpty() ? "U" : String.valueOf(name.charAt(0)).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (36 - fm.stringWidth(initial)) / 2, (36 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // Truncate long names
        String displayName = name.length() > 18 ? name.substring(0, 16) + "…" : name;
        JLabel lblName = new JLabel(displayName);
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(new Color(0x64748B));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblName);
        info.add(Box.createVerticalStrut(2));
        info.add(lblRole);

        panel.add(avatar, BorderLayout.WEST);
        panel.add(info,   BorderLayout.CENTER);
        return panel;
    }

    // ── top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.SURFACE);
        bar.setPreferredSize(new Dimension(0, UITheme.TOPBAR_HEIGHT));
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));

        lblPageTitle.setFont(UITheme.FONT_HEADING);
        lblPageTitle.setForeground(UITheme.TEXT);

        // Right side: notification icon + user label
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setOpaque(false);

        User user = SessionManager.getInstance().getCurrentUser();
        String displayUser = user != null ? user.getFullName() : "";

        JLabel lblUser = new JLabel(displayUser);
        lblUser.setFont(UITheme.FONT_REGULAR);
        lblUser.setForeground(UITheme.TEXT_MUTED);

        JLabel roleChip = UIComponents.createBadge(
                user != null ? user.getRole().name() : "",
                UITheme.PRIMARY_LIGHT,
                UITheme.PRIMARY
        );

        rightSide.add(lblUser);
        rightSide.add(roleChip);

        bar.add(lblPageTitle, BorderLayout.WEST);
        bar.add(rightSide,    BorderLayout.EAST);
        return bar;
    }

    // ── protected API for subclasses ──────────────────────────────────────────

    /**
     * Registers a nav item and adds the panel to the card layout.
     *
     * @param icon  Emoji or text icon shown in sidebar
     * @param label Display label in sidebar
     * @param key   CardLayout key — must be unique per frame
     * @param panel The JPanel to show when this nav item is clicked
     */
    protected void addNavItem(String icon, String label, String key, JPanel panel) {
        // Register panel
        contentArea.add(panel, key);

        // Create sidebar button
        SidebarNavButton btn = new SidebarNavButton(icon, label, key);
        btn.addActionListener(e -> showPanel(key));
        navButtons.add(btn);

        // Add to nav list
        JPanel navList = findNavList();
        if (navList != null) {
            navList.add(btn);
            navList.add(Box.createVerticalStrut(2));
        }
    }

    /**
     * Adds a visual section divider with a label in the sidebar.
     */
    protected void addNavSection(String sectionLabel) {
        JPanel navList = findNavList();
        if (navList == null) return;

        JPanel div = new JPanel(new BorderLayout());
        div.setOpaque(false);
        div.setBorder(new EmptyBorder(12, 20, 4, 20));
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        div.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(sectionLabel.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(0x475569));

        div.add(lbl, BorderLayout.CENTER);
        navList.add(div);
    }

    /**
     * Switches the visible content panel and highlights the sidebar button.
     */
    protected void showPanel(String key) {
        if (key == null) return;
        cardLayout.show(contentArea, key);
        activeKey = key;

        // Update button states
        for (SidebarNavButton btn : navButtons) {
            btn.setActive(key.equals(btn.getPanelKey()));
        }

        // Update top bar title
        for (SidebarNavButton btn : navButtons) {
            if (key.equals(btn.getPanelKey())) {
                lblPageTitle.setText(btn.getNavLabel());
                break;
            }
        }
    }

    /**
     * Shows the first registered panel. Call after all addNavItem calls.
     */
    protected void showDefaultPanel() {
        if (!navButtons.isEmpty()) {
            showPanel(navButtons.get(0).getPanelKey());
        }
    }

    // ── logout ────────────────────────────────────────────────────────────────
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            new LandingFrame().setVisible(true);
            dispose();
        }
    }

    // ── utility ───────────────────────────────────────────────────────────────
    private JPanel findNavList() {
        return findNamedPanel(sidebar, "navList");
    }

    private JPanel findNamedPanel(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (c instanceof JScrollPane sp) {
                Component view = sp.getViewport().getView();
                if (view instanceof JPanel p && name.equals(p.getName())) return p;
            }
            if (c instanceof JPanel p) {
                if (name.equals(p.getName())) return p;
                JPanel found = findNamedPanel(p, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    // =========================================================================
    // INNER CLASS: SidebarNavButton
    // =========================================================================

    /**
     * A sidebar navigation button with active/inactive visual state.
     */
    public static class SidebarNavButton extends JButton {

        private final String panelKey;
        private final String navLabel;
        private boolean active   = false;
        private boolean isLogout = false;

        public SidebarNavButton(String icon, String label, String panelKey) {
            super(icon + "  " + label);
            this.panelKey = panelKey;
            this.navLabel = label;

            setFont(UITheme.FONT_NAV);
            setForeground(UITheme.SIDEBAR_TEXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(11, 20, 11, 20));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!active) setForeground(Color.WHITE);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!active) setForeground(isLogout ? UITheme.DANGER : UITheme.SIDEBAR_TEXT);
                }
            });
        }

        public void setIsLogout(boolean logout) {
            this.isLogout = logout;
            setForeground(UITheme.DANGER);
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? UITheme.SIDEBAR_ACTIVE : (isLogout ? UITheme.DANGER : UITheme.SIDEBAR_TEXT));
            repaint();
        }

        public String getPanelKey() { return panelKey; }
        public String getNavLabel() { return navLabel; }

        @Override
        protected void paintComponent(Graphics g) {
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Active background
                g2.setColor(UITheme.SIDEBAR_SEL_BG);
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 8, 8);
                // Left accent bar
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(2, 8, 4, getHeight() - 16, 4, 4);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
