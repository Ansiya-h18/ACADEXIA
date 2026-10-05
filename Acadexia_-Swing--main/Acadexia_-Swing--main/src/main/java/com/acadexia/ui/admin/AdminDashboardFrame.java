package com.acadexia.ui.admin;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.dao.AuditLogDAO;
import com.acadexia.dao.UserDAO;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.common.MainFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin dashboard — extends MainFrame for sidebar navigation shell.
 *
 * Panels:
 *   • Overview      — stat cards + quick actions
 *   • User Accounts — user management table
 *   • Audit Logs    — security & audit trail
 *   • System Status — DB + environment info
 */
public class AdminDashboardFrame extends MainFrame {

    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final UserDAO     userDAO  = new UserDAO();

    private DefaultTableModel auditTableModel;
    private DefaultTableModel userTableModel;
    private JComboBox<String> cmbSeverity;

    public AdminDashboardFrame() {
        super("Acadexia – System Administration", 1200, 760);

        // Register nav panels
        addNavItem("🏠", "Overview",       "overview",  buildOverviewPanel());
        addNavItem("👥", "User Accounts",  "users",     buildUserPanel());
        addNavItem("🛡️", "Audit Logs",     "audit",     buildAuditPanel());
        addNavItem("⚙️", "System Status",  "system",    buildSystemPanel());

        showDefaultPanel();

        // Initial data load
        SwingUtilities.invokeLater(() -> {
            loadUsers();
            loadAuditLogs();
        });
    }

    // =========================================================================
    // OVERVIEW PANEL
    // =========================================================================
    private JPanel buildOverviewPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 24));

        // Header
        page.add(UIComponents.createHeader("System Administration",
                "Manage users, review audit logs, and monitor system health."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<User> allUsers = userDAO.getAllUsers();
        long students  = allUsers.stream().filter(u -> u.getRole() == Role.STUDENT).count();
        long faculty   = allUsers.stream().filter(u -> u.getRole() == Role.FACULTY).count();
        long admins    = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.PRINCIPAL).count();
        long active    = allUsers.stream().filter(User::isActive).count();

        cards.add(UIComponents.createStatCard("Total Users",   String.valueOf(allUsers.size()), "All accounts", UITheme.PRIMARY));
        cards.add(UIComponents.createStatCard("Students",       String.valueOf(students),        "Registered students", UITheme.INFO));
        cards.add(UIComponents.createStatCard("Faculty",        String.valueOf(faculty),         "Teaching staff", UITheme.SUCCESS));
        cards.add(UIComponents.createStatCard("Active Accounts",String.valueOf(active),          "Currently active", UITheme.WARNING));

        // Quick Actions
        JPanel qaCard = UIComponents.createCardPanel();
        qaCard.setLayout(new BorderLayout(0, 14));
        qaCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel qaTitle = UIComponents.createSectionLabel("Quick Actions");
        JPanel qaButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qaButtons.setOpaque(false);

        JButton btnNewUser  = UIComponents.createPrimaryButton("➕  New User");
        JButton btnAudit    = UIComponents.createSecondaryButton("🛡️  View Audit Logs");
        JButton btnSystem   = UIComponents.createSecondaryButton("⚙️  System Status");

        btnNewUser.addActionListener(e -> showCreateUserDialog());
        btnAudit.addActionListener(e  -> showPanel("audit"));
        btnSystem.addActionListener(e -> showPanel("system"));

        qaButtons.add(btnNewUser);
        qaButtons.add(btnAudit);
        qaButtons.add(btnSystem);

        qaCard.add(qaTitle,    BorderLayout.NORTH);
        qaCard.add(qaButtons,  BorderLayout.CENTER);

        body.add(cards);
        body.add(Box.createVerticalStrut(24));
        body.add(qaCard);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // USER MANAGEMENT PANEL
    // =========================================================================
    private JPanel buildUserPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("User Accounts", "Manage all system user accounts."), BorderLayout.NORTH);

        // Toolbar
        JButton btnNew    = UIComponents.createPrimaryButton("➕  New User");
        JButton btnToggle = UIComponents.createSecondaryButton("Toggle Active/Inactive");
        JButton btnRefresh= UIComponents.createSecondaryButton("↻  Refresh");
        btnNew.addActionListener(e    -> showCreateUserDialog());
        btnToggle.addActionListener(e -> toggleSelectedUser());
        btnRefresh.addActionListener(e-> loadUsers());

        JPanel toolbar = UIComponents.createToolbar(btnNew, btnToggle, btnRefresh);

        // Table
        String[] cols = {"ID", "Username", "Full Name", "Role", "Email", "Phone", "Status", "Created"};
        userTableModel = new DefaultTableModel(cols, 0);
        JTable table   = UIComponents.createStyledTable(userTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(140);

        JScrollPane sp = UIComponents.createTableScrollPane(table);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(sp,      BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadUsers() {
        if (userTableModel == null) return;
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getRole().name(),
                u.getEmail()  != null ? u.getEmail()  : "—",
                u.getPhone()  != null ? u.getPhone()  : "—",
                u.isActive()  ? "Active" : "Inactive",
                u.getCreatedAt()
            });
        }
    }

    private void toggleSelectedUser() {
        String input = JOptionPane.showInputDialog(this,
                "Enter User ID to toggle status:", "Toggle User Status", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int uid   = Integer.parseInt(input.trim());
            User u    = userDAO.getById(uid);
            if (u == null) { UIComponents.showError(this, "User ID not found."); return; }
            boolean ns = !u.isActive();
            userDAO.updateUserStatus(uid, ns);
            AuditLogDAO.log(uid, u.getUsername(), "ADMIN", "USER_STATUS_TOGGLED", "USER",
                    String.valueOf(uid), "Status → " + (ns ? "ACTIVE" : "INACTIVE"), AuditLog.Severity.WARNING);
            loadUsers();
            UIComponents.showSuccess(this, "Status updated to: " + (ns ? "Active" : "Inactive"));
        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid User ID.");
        }
    }

    private void showCreateUserDialog() {
        JDialog dlg = new JDialog(this, "Create New User", true);
        dlg.setSize(460, 500);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.SURFACE);
        form.setBorder(new EmptyBorder(28, 32, 20, 32));

        JTextField   txtU     = UIComponents.createTextField("Username");
        JPasswordField txtP   = UIComponents.createPasswordField("Password");
        JTextField   txtName  = UIComponents.createTextField("Full name");
        JTextField   txtEmail = UIComponents.createTextField("Email address");
        JTextField   txtPhone = UIComponents.createTextField("Phone number");
        JComboBox<Role> cmbRole = UIComponents.createComboBox(Role.values());

        InputFilterFactory.applyIdentifierFilter(txtU, 30);
        InputFilterFactory.applyPasswordFilter(txtP, 50);
        InputFilterFactory.applySqlSanitizer(txtName, 80, true);
        InputFilterFactory.applySqlSanitizer(txtEmail, 80, false);
        InputFilterFactory.applyNumericFilter(txtPhone, 15, false);

        addFormRow(form, "Username",    txtU);
        form.add(Box.createVerticalStrut(12));
        addFormRow(form, "Password",    txtP);
        form.add(Box.createVerticalStrut(12));
        addFormRow(form, "Full Name",   txtName);
        form.add(Box.createVerticalStrut(12));
        addFormRow(form, "Role",        cmbRole);
        form.add(Box.createVerticalStrut(12));
        addFormRow(form, "Email",       txtEmail);
        form.add(Box.createVerticalStrut(12));
        addFormRow(form, "Phone",       txtPhone);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton btnCancel = UIComponents.createSecondaryButton("Cancel");
        JButton btnSubmit = UIComponents.createPrimaryButton("Create Account");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSubmit.addActionListener(e -> {
            String u = txtU.getText().trim();
            String p = new String(txtP.getPassword());
            String n = txtName.getText().trim();
            if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                UIComponents.showError(dlg, "Username, password, and name are required.");
                return;
            }
            User newUser = new User();
            newUser.setUsername(u);
            newUser.setFullName(n);
            newUser.setRole((Role) cmbRole.getSelectedItem());
            newUser.setEmail(txtEmail.getText().trim());
            newUser.setPhone(txtPhone.getText().trim());
            newUser.setActive(true);
            try {
                int id = userDAO.createUser(newUser, p);
                if (id > 0) {
                    AuditLogDAO.log(id, u, "ADMIN", "USER_PROVISIONED", "USER",
                            String.valueOf(id), "Created " + newUser.getRole() + " account", AuditLog.Severity.INFO);
                    loadUsers();
                    dlg.dispose();
                    UIComponents.showSuccessToast(this, "User '" + u + "' created successfully.");
                }
            } catch (Exception ex) {
                UIComponents.showError(dlg, "Error: " + ex.getMessage());
            }
        });

        footer.add(btnCancel);
        footer.add(btnSubmit);

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addFormRow(JPanel panel, String label, JComponent field) {
        JLabel lbl = UIComponents.createFormLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
    }

    // =========================================================================
    // AUDIT LOGS PANEL
    // =========================================================================
    private JPanel buildAuditPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Security & Audit Logs",
                "Monitor all system events, security alerts, and user activity."), BorderLayout.NORTH);

        // Toolbar
        JLabel lblFilter = UIComponents.createFormLabel("Severity:");
        cmbSeverity = UIComponents.createComboBox(
                new String[]{"ALL", "SECURITY_ALERT", "WARNING", "ERROR", "INFO"});
        cmbSeverity.setPreferredSize(new Dimension(160, 34));
        cmbSeverity.addActionListener(e -> loadAuditLogs());

        JButton btnRefresh   = UIComponents.createSecondaryButton("↻  Refresh");
        JButton btnTestExpl  = UIComponents.createDangerButton("⚡  Test SQL Exploit Detection");
        btnRefresh.addActionListener(e  -> loadAuditLogs());
        btnTestExpl.addActionListener(e -> {
            AuditLogDAO.logSecurityAlert("TEST_ATTACKER", "SQL_INJECTION_INTERCEPTED",
                    "Blocked: ' OR '1'='1'; DROP TABLE users; --");
            loadAuditLogs();
            UIComponents.showInfoToast(this, "Security event logged to audit trail.");
        });

        JPanel toolbar = UIComponents.createToolbar(lblFilter, cmbSeverity, btnRefresh, btnTestExpl);

        // Table
        String[] cols = {"ID", "Timestamp", "User", "Role", "Action", "Target", "Severity", "Details"};
        auditTableModel  = new DefaultTableModel(cols, 0);
        JTable table     = UIComponents.createStyledTable(auditTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(340);

        JScrollPane sp = UIComponents.createTableScrollPane(table);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(sp,      BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadAuditLogs() {
        if (auditTableModel == null) return;
        auditTableModel.setRowCount(0);
        String severity = cmbSeverity != null ? (String) cmbSeverity.getSelectedItem() : "ALL";
        List<AuditLog> logs = auditDAO.getRecentLogs(100, severity);
        for (AuditLog l : logs) {
            auditTableModel.addRow(new Object[]{
                l.getId(),
                l.getTimestamp(),
                l.getUsername(),
                l.getRole(),
                l.getAction(),
                l.getEntityType() != null ? l.getEntityType() : "—",
                l.getSeverity().name(),
                l.getDetails()
            });
        }
    }

    // =========================================================================
    // SYSTEM STATUS PANEL
    // =========================================================================
    private JPanel buildSystemPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 24));
        page.add(UIComponents.createHeader("System Status",
                "Database connectivity and application environment information."), BorderLayout.NORTH);

        boolean dbOk = DatabaseConnection.testConnection();

        JPanel cards = new JPanel(new GridLayout(2, 2, 16, 16));
        cards.setOpaque(false);

        cards.add(UIComponents.createStatCard("Database Engine",   "MySQL",
                "Port 3306 · utf8mb4 · InnoDB", UITheme.PRIMARY));
        cards.add(UIComponents.createStatCard("Connection",
                dbOk ? "ONLINE" : "OFFLINE",
                dbOk ? "Connection healthy" : "Check db.properties",
                dbOk ? UITheme.SUCCESS : UITheme.DANGER));
        cards.add(UIComponents.createStatCard("Security Filter",   "ACTIVE",
                "DocumentFilter + PreparedStatement", UITheme.SUCCESS));
        cards.add(UIComponents.createStatCard("Application Stack", "Java Swing",
                "FlatLaf · JDBC · BCrypt", UITheme.INFO));

        page.add(cards, BorderLayout.CENTER);
        return page;
    }
}
