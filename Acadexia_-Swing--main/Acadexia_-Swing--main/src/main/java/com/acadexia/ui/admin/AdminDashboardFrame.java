package com.acadexia.ui.admin;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.dao.*;
import com.acadexia.model.*;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.charts.AcademicChartFactory;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Acadexia System Administration & Academic Governance Portal.
 * Centralized command center providing:
 * - Institutional KPI Analytics & Dashboard Statistics
 * - Student Lifecycle Registration with Official ID Generation & Roll Numbering
 * - Faculty Governance with Auto Faculty ID Generation
 * - Department & HOD Management
 * - Examination Scheduling
 * - Marks Entry, Result Verification & Multi-Tier Publication Tracking
 * - Parent Account Provisioning & Ward Linking
 * - Security & Audit Logging
 * - System Diagnostics
 */
public class AdminDashboardFrame extends JFrame {

    // DAOs
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ParentDAO parentDAO = new ParentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ExamScheduleDAO examScheduleDAO = new ExamScheduleDAO();
    private final MarkDAO markDAO = new MarkDAO();
    private final ResultPublicationDAO publicationDAO = new ResultPublicationDAO();

    // Table Models
    private DefaultTableModel auditTableModel;
    private DefaultTableModel userTableModel;
    private DefaultTableModel parentTableModel;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel facultyTableModel;
    private DefaultTableModel deptTableModel;
    private DefaultTableModel examTableModel;
    private DefaultTableModel marksEntryTableModel;
    private DefaultTableModel publicationTrackerModel;

    // Filter controls & cached data
    private List<Student> cachedStudents;
    private List<Faculty> cachedFaculty;
    private List<Parent> cachedParents;

    private JTextField txtStudentSearch;
    private JComboBox<String> cmbStudentDeptFilter;
    private JTextField txtFacultySearch;
    private JComboBox<String> cmbFacultyDeptFilter;
    private JTextField txtParentSearch;
    private JComboBox<String> cmbSeverity;

    // Marks Entry & Publication controls
    private JComboBox<ClassBatch> cmbMarksClass;
    private JComboBox<Subject> cmbMarksSubject;
    private JComboBox<Mark.ExamType> cmbMarksExamType;
    private JTextField txtMarksAcademicYear;
    private JLabel lblResultStatusBadge;
    private JButton btnVerifyResults;
    private JButton btnPublishResults;

    // Overview Stats & Navigation
    private JPanel overviewStatsCardsPanel;
    private JPanel adminChartsContainer;
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCardsPanel = new JPanel(contentCardLayout);
    private final Map<String, JButton> sidebarButtons = new LinkedHashMap<>();
    private JLabel lblSectionTitle;
    private JLabel lblSectionSubtitle;

    public AdminDashboardFrame() {
        setTitle("Acadexia — System Administration & Result Governance Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
        loadAllInitialData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Left Modern Sidebar
        add(createSidebarPanel(), BorderLayout.WEST);

        // Right Content Area
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);
        rightContainer.add(createTopBar(), BorderLayout.NORTH);

        // Content Cards Panel
        contentCardsPanel.setBackground(UITheme.BG_DARK);
        contentCardsPanel.add(createOverviewPanel(), "OVERVIEW");
        contentCardsPanel.add(createStudentManagementPanel(), "STUDENTS");
        contentCardsPanel.add(createFacultyManagementPanel(), "FACULTY");
        contentCardsPanel.add(createDepartmentManagementPanel(), "DEPTS");
        contentCardsPanel.add(createExamSchedulingPanel(), "EXAMS");
        contentCardsPanel.add(createMarksAndResultsPanel(), "RESULTS");
        contentCardsPanel.add(createParentManagementPanel(), "PARENTS");
        contentCardsPanel.add(createUserManagementPanel(), "USERS");
        contentCardsPanel.add(createAuditLogsPanel(), "AUDIT");
        contentCardsPanel.add(createSystemStatusPanel(), "SYSTEM");

        rightContainer.add(contentCardsPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.CENTER);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.SIDEBAR_BORDER));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(22, 18, 16, 18));

        JLabel lblBrand = new JLabel("ACADEXIA ERP");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setForeground(UITheme.CYAN_ACCENT);

        JLabel lblRole = new JLabel("Central Administration");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblBadge = UIComponents.createBadge("System Governor & Results Authority", new Color(18, 38, 72), UITheme.CYAN_ACCENT);
        lblBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(lblBrand);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(lblRole);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(lblBadge);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        // Nav List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(8, 10, 8, 10));

        addSidebarItem(navList, "OVERVIEW", "📊", "Executive Overview");
        addSidebarItem(navList, "STUDENTS", "🎓", "Student Management");
        addSidebarItem(navList, "FACULTY", "👨‍🏫", "Faculty Management");
        addSidebarItem(navList, "DEPTS", "🏛️", "Departments & HODs");
        addSidebarItem(navList, "EXAMS", "📝", "Exam Scheduling");
        addSidebarItem(navList, "RESULTS", "📈", "Marks Entry & Results");
        addSidebarItem(navList, "PARENTS", "👨‍👩‍👧", "Parent Accounts");
        addSidebarItem(navList, "USERS", "👥", "User Governance");
        addSidebarItem(navList, "AUDIT", "🛡️", "Security & Audit Logs");
        addSidebarItem(navList, "SYSTEM", "⚙️", "System Status");

        JScrollPane scrollNav = new JScrollPane(navList);
        scrollNav.setBorder(null);
        scrollNav.setOpaque(false);
        scrollNav.getViewport().setOpaque(false);
        scrollNav.getVerticalScrollBar().setUnitIncrement(14);
        sidebar.add(scrollNav, BorderLayout.CENTER);

        // Footer User Profile
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 16, 16, 16));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        String displayName = currentUser != null ? currentUser.getFullName() : "Administrator";

        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setOpaque(false);

        JLabel lblU = new JLabel(displayName);
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblU.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("System Administrator");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(UITheme.TEXT_MUTED);

        userBox.add(lblU);
        userBox.add(lblSub);

        JButton btnLogout = UIComponents.createDangerButton("🚪 Exit");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to log out from the Central Administration Portal?",
                    "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.getInstance().logout();
                new LandingFrame().setVisible(true);
                dispose();
            }
        });

        footer.add(userBox, BorderLayout.CENTER);
        footer.add(btnLogout, BorderLayout.EAST);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addSidebarItem(JPanel container, String cardKey, String icon, String title) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(245, 42));
        row.setPreferredSize(new Dimension(240, 40));

        UIComponents.SidebarNavButton btn = new UIComponents.SidebarNavButton(icon, title);
        btn.addActionListener(e -> selectView(cardKey, title));
        sidebarButtons.put(cardKey, btn);

        row.add(btn, BorderLayout.CENTER);
        container.add(row);
        container.add(Box.createVerticalStrut(3));
    }

    private void selectView(String cardKey, String title) {
        contentCardLayout.show(contentCardsPanel, cardKey);
        if (lblSectionTitle != null) lblSectionTitle.setText(title.toUpperCase());
        if (lblSectionSubtitle != null) lblSectionSubtitle.setText("Administration > " + title);

        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            boolean active = entry.getKey().equals(cardKey);
            JButton b = entry.getValue();
            if (b instanceof UIComponents.SidebarNavButton sb) {
                sb.setActive(active);
            }
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.TOPBAR_BG);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CARD_BORDER),
                new EmptyBorder(12, 22, 12, 22)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        lblSectionTitle = new JLabel("EXECUTIVE OVERVIEW");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSectionTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblSectionSubtitle = new JLabel("Administration > Executive Overview");
        lblSectionSubtitle.setFont(UITheme.FONT_SMALL);
        lblSectionSubtitle.setForeground(UITheme.TEXT_MUTED);

        left.add(lblSectionTitle);
        left.add(lblSectionSubtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnRefreshAll = UIComponents.createSecondaryButton("🔄 Refresh Data");
        btnRefreshAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefreshAll.addActionListener(e -> loadAllInitialData());
        right.add(btnRefreshAll);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private void loadAllInitialData() {
        refreshDashboardStatistics();
        loadStudents();
        loadFaculty();
        loadDepartments();
        loadExamSchedules();
        loadMarksClasses();
        loadParents();
        loadUsers();
        loadAuditLogs();
        loadPublicationTracker();
    }

    // =========================================================================
    // 1. EXECUTIVE OVERVIEW & DASHBOARD STATISTICS
    // =========================================================================

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Top KPIs Grid
        overviewStatsCardsPanel = new JPanel(new GridLayout(2, 3, 14, 14));
        overviewStatsCardsPanel.setOpaque(false);

        // Center: Dual Interactive Charts
        adminChartsContainer = new JPanel(new GridLayout(1, 2, 14, 14));
        adminChartsContainer.setOpaque(false);
        adminChartsContainer.setPreferredSize(new Dimension(0, 240));

        // Center Quick Insights & Status Cards
        JPanel centerCard = UIComponents.createCardPanel();
        centerCard.setLayout(new BorderLayout(12, 12));

        JLabel lblHeader = new JLabel("Institutional Operations & Academic Status Directives");
        lblHeader.setFont(UITheme.FONT_SECTION);
        lblHeader.setForeground(UITheme.CYAN_ACCENT);
        centerCard.add(lblHeader, BorderLayout.NORTH);

        JTextArea txtSummary = new JTextArea("""
            Welcome to the Acadexia Central Administration Command Center.
            
            Key Administrative Controls:
            • Student Registration: Generates official University/College registration numbers (e.g. REG2026CS001) and automatically suggests class roll numbers.
            • Faculty Management: Provisions official Faculty IDs (e.g. FAC-CS-001) and manages departmental appointments.
            • Department Governance: Direct oversight of academic departments and appointment of Faculty Heads (HODs).
            • Examination Management: Publish institutional examination schedules and room allocations.
            • Result Publication Pipeline: Complete three-step lifecycle — Entry (Draft) ➔ Verification ➔ Official Publication.
            • Parent Portal Integration: Provision verified guardian accounts linked directly to students' academic records.
            • IT Security & Auditing: Dual-layer parameterized SQL protection with real-time audit event logging.
            """);
        txtSummary.setFont(UITheme.FONT_REGULAR);
        txtSummary.setForeground(UITheme.TEXT_MUTED);
        txtSummary.setOpaque(false);
        txtSummary.setEditable(false);
        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        centerCard.add(new JScrollPane(txtSummary), BorderLayout.CENTER);

        JPanel middleSection = new JPanel(new BorderLayout(0, 14));
        middleSection.setOpaque(false);
        middleSection.add(adminChartsContainer, BorderLayout.NORTH);
        middleSection.add(centerCard, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);
        JButton btnRefreshStats = UIComponents.createSecondaryButton("🔄 Refresh Statistics");
        btnRefreshStats.addActionListener(e -> refreshDashboardStatistics());
        bottomBar.add(btnRefreshStats);

        panel.add(overviewStatsCardsPanel, BorderLayout.NORTH);
        panel.add(middleSection, BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshDashboardStatistics() {
        if (overviewStatsCardsPanel == null) return;
        overviewStatsCardsPanel.removeAll();

        int stuCount = studentDAO.getTotalStudentCount();
        int facCount = facultyDAO.getTotalFacultyCount();
        int deptCount = departmentDAO.getTotalDepartmentCount();
        int classCount = classDAO.getTotalClassCount();
        Map<String, Integer> pubStats = publicationDAO.getPublicationStats();
        int publishedCount = pubStats.getOrDefault("PUBLISHED", 0);
        int verifiedCount = pubStats.getOrDefault("VERIFIED", 0);
        boolean dbOk = DatabaseConnection.testConnection();

        overviewStatsCardsPanel.add(UIComponents.createStatCard(
                "Enrolled Students", String.valueOf(stuCount), "Across all active batches", UITheme.CYAN_ACCENT, "🎓"));
        overviewStatsCardsPanel.add(UIComponents.createStatCard(
                "Teaching Faculty", String.valueOf(facCount), "Professors & Instructors", UITheme.SUCCESS, "👨‍🏫"));
        overviewStatsCardsPanel.add(UIComponents.createStatCard(
                "Academic Departments", String.valueOf(deptCount), "Engineering & Sciences", UITheme.INFO, "🏛️"));
        overviewStatsCardsPanel.add(UIComponents.createStatCard(
                "Active Class Batches", String.valueOf(classCount), "Current Semesters", UITheme.WARNING, "📅"));
        overviewStatsCardsPanel.add(UIComponents.createStatCard(
                "Results Published", String.valueOf(publishedCount), verifiedCount + " awaiting publishing", new Color(168, 85, 247), "📈"));
        overviewStatsCardsPanel.add(UIComponents.createStatCard(
                "Database Engine", dbOk ? "ONLINE" : "OFFLINE", "MySQL 8.0+ / utf8mb4", dbOk ? UITheme.SUCCESS : UITheme.DANGER, "🛡️"));

        // Render JFreeCharts in Overview
        if (adminChartsContainer != null) {
            adminChartsContainer.removeAll();

            // 1. Department Enrollment & Capacity Bar Chart
            Map<String, Double> deptData = new LinkedHashMap<>();
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) {
                long count = studentDAO.getStudentsByDepartment(d.getId()).size();
                deptData.put(d.getCode() != null ? d.getCode() : d.getName(), (double) count);
            }
            if (deptData.isEmpty()) {
                deptData.put("CSE", (double) stuCount);
            }
            JFreeChart deptChart = AcademicChartFactory.createDepartmentResultsBarChart("Department Student Enrollment", deptData, "Students Enrolled");
            ChartPanel pnlDept = AcademicChartFactory.createChartPanel(deptChart);

            // 2. Exam Publication & Status Donut Chart
            JFreeChart pubChart = AcademicChartFactory.createPassFailDonutChart("Result Publications & Lifecycle Status", (double) publishedCount, (double) verifiedCount, 1.0);
            ChartPanel pnlPub = AcademicChartFactory.createChartPanel(pubChart);

            adminChartsContainer.add(pnlDept);
            adminChartsContainer.add(pnlPub);
            adminChartsContainer.revalidate();
            adminChartsContainer.repaint();
        }

    }

    // =========================================================================
    // 2. STUDENT REGISTRATION & MANAGEMENT (WITH AUTO ID GENERATION)
    // =========================================================================

    private JPanel createStudentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewStudent = UIComponents.createPrimaryButton("➕ Register New Student");
        btnNewStudent.addActionListener(e -> showCreateStudentDialog());

        JButton btnEditStudent = UIComponents.createSecondaryButton("✏️ Edit Student");
        btnEditStudent.addActionListener(e -> showEditStudentDialog());

        JButton btnToggleStatus = UIComponents.createSecondaryButton("Toggle Active / Inactive");
        btnToggleStatus.addActionListener(e -> toggleSelectedStudent());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadStudents());

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_MUTED);

        txtStudentSearch = new JTextField(12);
        txtStudentSearch.putClientProperty("JTextField.placeholderText", "Name or Reg No...");
        txtStudentSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterStudents();
            }
        });

        cmbStudentDeptFilter = new JComboBox<>();
        cmbStudentDeptFilter.addItem("All Departments");
        cmbStudentDeptFilter.addActionListener(e -> filterStudents());

        toolbar.add(btnNewStudent);
        toolbar.add(btnEditStudent);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnRefresh);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(lblSearch);
        toolbar.add(txtStudentSearch);
        toolbar.add(cmbStudentDeptFilter);

        // Student Table
        String[] cols = {
                "Student ID", "Register No", "Roll No", "Full Name",
                "Class", "Department", "Adm Year", "CGPA", "Attendance",
                "Guardian Name", "Contact Phone"
        };
        studentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIComponents.createStyledTable(studentTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(8).setPreferredWidth(80);
        table.getColumnModel().getColumn(9).setPreferredWidth(120);
        table.getColumnModel().getColumn(10).setPreferredWidth(100);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadStudents() {
        cachedStudents = studentDAO.getAllStudents();

        // Refresh dept filter options
        if (cmbStudentDeptFilter != null) {
            String selected = (String) cmbStudentDeptFilter.getSelectedItem();
            cmbStudentDeptFilter.removeAllItems();
            cmbStudentDeptFilter.addItem("All Departments");
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) {
                cmbStudentDeptFilter.addItem(d.getName());
            }
            if (selected != null) cmbStudentDeptFilter.setSelectedItem(selected);
        }

        filterStudents();
        refreshDashboardStatistics();
    }

    private void filterStudents() {
        if (studentTableModel == null || cachedStudents == null) return;
        studentTableModel.setRowCount(0);

        String query = txtStudentSearch != null ? txtStudentSearch.getText().trim().toLowerCase() : "";
        String deptFilter = cmbStudentDeptFilter != null ? (String) cmbStudentDeptFilter.getSelectedItem() : "All Departments";

        for (Student s : cachedStudents) {
            if (deptFilter != null && !deptFilter.equals("All Departments") && !deptFilter.equalsIgnoreCase(s.getDepartmentName())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean match = (s.getFullName() != null && s.getFullName().toLowerCase().contains(query))
                        || (s.getRegisterNumber() != null && s.getRegisterNumber().toLowerCase().contains(query))
                        || (s.getRollNumber() != null && s.getRollNumber().contains(query));
                if (!match) continue;
            }

            studentTableModel.addRow(new Object[]{
                    s.getId(),
                    s.getRegisterNumber(),
                    s.getRollNumber(),
                    s.getFullName(),
                    s.getClassName(),
                    s.getDepartmentName(),
                    s.getAdmissionYear(),
                    String.format("%.2f", s.getCurrentCgpa()),
                    String.format("%.1f%%", s.getAttendancePercentage()),
                    s.getGuardianName() != null ? s.getGuardianName() : "-",
                    s.getPhone() != null ? s.getPhone() : "-"
            });
        }
    }

    private void showCreateStudentDialog() {
        JDialog dlg = new JDialog(this, "Register New Student (Official ID Generation)", true);
        dlg.setSize(540, 680);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(12, 2, 8, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtName = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<Integer> cmbAdmYear = new JComboBox<>(new Integer[]{2026, 2025, 2024, 2023});

        // Load all departments dynamically from existing MySQL departments table using JDBC
        List<Department> depts = departmentDAO.getAllDepartments();
        JComboBox<Department> cmbDept = new JComboBox<>();
        for (Department d : depts) cmbDept.addItem(d);

        JComboBox<ClassBatch> cmbClass = new JComboBox<>();

        JTextField txtOfficialRegNo = new JTextField();
        txtOfficialRegNo.setEditable(false);
        txtOfficialRegNo.setFont(UITheme.FONT_BOLD);
        txtOfficialRegNo.setForeground(UITheme.PRIMARY);

        JTextField txtRollNo = new JTextField();
        JTextField txtGuardianName = new JTextField();
        JTextField txtGuardianPhone = new JTextField();

        // Security filters
        InputFilterFactory.applySqlSanitizer(txtName, 60, true);
        InputFilterFactory.applyIdentifierFilter(txtUsername, 30);
        InputFilterFactory.applyPasswordFilter(txtPass, 30);
        InputFilterFactory.applySqlSanitizer(txtEmail, 60, false);
        InputFilterFactory.applyNumericFilter(txtPhone, 15, false);
        InputFilterFactory.applyNumericFilter(txtRollNo, 5, false);

        // Auto ID generation & class loader listener based on selected department
        Runnable updateClassesAndAutoIds = () -> {
            Department selDept = (Department) cmbDept.getSelectedItem();
            if (selDept == null) return;
            int admYear = (Integer) cmbAdmYear.getSelectedItem();

            // Load classes dynamically for selected department
            cmbClass.removeAllItems();
            List<ClassBatch> deptClasses = classDAO.getClassesByDepartment(selDept.getId());
            if (deptClasses.isEmpty()) {
                // Auto-provision a starter class batch for this department if none exists
                ClassBatch defaultBatch = new ClassBatch();
                defaultBatch.setName("S1 " + selDept.getCode());
                defaultBatch.setDepartmentId(selDept.getId());
                defaultBatch.setSemester(1);
                defaultBatch.setAcademicYear(admYear + "-" + (admYear + 1));
                defaultBatch.setSection("A");
                try {
                    int newClassId = classDAO.createClass(defaultBatch);
                    if (newClassId > 0) {
                        defaultBatch.setId(newClassId);
                        defaultBatch.setDepartmentName(selDept.getName());
                        deptClasses.add(defaultBatch);
                    }
                } catch (Exception ex) {
                    System.err.println("Could not auto-create starter class for dept " + selDept.getCode() + ": " + ex.getMessage());
                }
            }
            for (ClassBatch cb : deptClasses) {
                cmbClass.addItem(cb);
            }

            // Update registration number and roll number
            String deptCode = selDept.getCode();
            String nextReg = studentDAO.generateNextRegisterNumber(admYear, deptCode);
            txtOfficialRegNo.setText(nextReg);

            ClassBatch selClass = (ClassBatch) cmbClass.getSelectedItem();
            String nextRoll = (selClass != null) ? studentDAO.getNextRollNumber(selClass.getId()) : "01";
            txtRollNo.setText(nextRoll);

            if (txtUsername.getText().trim().isEmpty() && !txtName.getText().trim().isEmpty()) {
                String clean = txtName.getText().trim().toLowerCase().replaceAll("[^a-z0-9]", ".");
                txtUsername.setText(clean);
                if (txtEmail.getText().trim().isEmpty()) {
                    txtEmail.setText(clean + "@acadexia.edu");
                }
            }
        };

        cmbDept.addActionListener(e -> updateClassesAndAutoIds.run());
        cmbAdmYear.addActionListener(e -> updateClassesAndAutoIds.run());
        cmbClass.addActionListener(e -> {
            ClassBatch selClass = (ClassBatch) cmbClass.getSelectedItem();
            if (selClass != null) {
                String nextRoll = studentDAO.getNextRollNumber(selClass.getId());
                txtRollNo.setText(nextRoll);
            }
        });

        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtUsername.getText().trim().isEmpty() && !txtName.getText().trim().isEmpty()) {
                    String clean = txtName.getText().trim().toLowerCase().replaceAll("\\s+", ".");
                    txtUsername.setText(clean);
                    if (txtEmail.getText().trim().isEmpty()) {
                        txtEmail.setText(clean + "@acadexia.edu");
                    }
                }
            }
        });

        // Trigger initial preview
        updateClassesAndAutoIds.run();

        p.add(new JLabel("Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Department:"));
        p.add(cmbDept);
        p.add(new JLabel("Admission Year:"));
        p.add(cmbAdmYear);
        p.add(new JLabel("Class Batch:"));
        p.add(cmbClass);
        p.add(new JLabel("Official Register No (Auto):"));
        p.add(txtOfficialRegNo);
        p.add(new JLabel("Class Roll Number:"));
        p.add(txtRollNo);
        p.add(new JLabel("Account Username:"));
        p.add(txtUsername);
        p.add(new JLabel("Initial Password:"));
        p.add(txtPass);
        p.add(new JLabel("Email Address:"));
        p.add(txtEmail);
        p.add(new JLabel("Phone Number:"));
        p.add(txtPhone);
        p.add(new JLabel("Guardian / Parent Name:"));
        p.add(txtGuardianName);
        p.add(new JLabel("Guardian Phone:"));
        p.add(txtGuardianPhone);

        JButton btnSubmit = UIComponents.createPrimaryButton("Register Student & Generate Official ID");
        btnSubmit.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()
                    || txtPass.getPassword().length == 0 || txtOfficialRegNo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name, username, password, department, and class are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Department selDept = (Department) cmbDept.getSelectedItem();
            ClassBatch selClass = (ClassBatch) cmbClass.getSelectedItem();
            if (selDept == null || selClass == null) {
                JOptionPane.showMessageDialog(dlg, "Department and Class Batch are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User u = new User();
            u.setUsername(txtUsername.getText().trim());
            u.setFullName(txtName.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setPhone(txtPhone.getText().trim());
            u.setDepartmentId(selDept.getId());
            u.setRole(Role.STUDENT);
            u.setActive(true);

            try {
                int userId = userDAO.createUser(u, new String(txtPass.getPassword()));
                if (userId > 0) {
                    Student s = new Student();
                    s.setUserId(userId);
                    s.setRegisterNumber(txtOfficialRegNo.getText().trim());
                    s.setRollNumber(txtRollNo.getText().trim().isEmpty() ? "01" : txtRollNo.getText().trim());
                    s.setClassId(selClass.getId());
                    s.setAdmissionYear((Integer) cmbAdmYear.getSelectedItem());
                    s.setGuardianName(txtGuardianName.getText().trim());
                    s.setGuardianPhone(txtGuardianPhone.getText().trim());

                    int studentId = studentDAO.createStudent(s);
                    if (studentId > 0) {
                        AuditLogDAO.log(userId, u.getUsername(), "ADMIN", "STUDENT_REGISTERED", "STUDENT",
                                String.valueOf(studentId), "Official Register No: " + s.getRegisterNumber() + ", Dept: " + selDept.getName(), AuditLog.Severity.INFO);

                        loadStudents();
                        dlg.dispose();
                        JOptionPane.showMessageDialog(this,
                                "Student registered successfully!\nOfficial Register Number: " + s.getRegisterNumber()
                                        + "\nDepartment: " + selDept.getName()
                                        + "\nClass Roll Number: " + s.getRollNumber(),
                                "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditStudentDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Student ID to Edit Details:", "Edit Student", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int sid = Integer.parseInt(input.trim());
            Student s = studentDAO.getById(sid);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Student record with ID " + sid + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Edit Student #" + s.getId() + " (" + s.getRegisterNumber() + ")", true);
            dlg.setSize(480, 520);
            dlg.setLocationRelativeTo(this);

            JPanel p = new JPanel(new GridLayout(8, 2, 8, 8));
            p.setBackground(UITheme.CARD_BG);
            p.setBorder(new EmptyBorder(20, 24, 20, 24));

            JTextField txtName = new JTextField(s.getFullName());
            JTextField txtRoll = new JTextField(s.getRollNumber());
            JTextField txtEmail = new JTextField(s.getEmail() != null ? s.getEmail() : "");
            JTextField txtPhone = new JTextField(s.getPhone() != null ? s.getPhone() : "");
            JTextField txtGName = new JTextField(s.getGuardianName() != null ? s.getGuardianName() : "");
            JTextField txtGPhone = new JTextField(s.getGuardianPhone() != null ? s.getGuardianPhone() : "");

            List<Department> depts = departmentDAO.getAllDepartments();
            JComboBox<Department> cmbDept = new JComboBox<>();
            for (Department d : depts) {
                cmbDept.addItem(d);
                if (d.getId() == s.getDepartmentId()) cmbDept.setSelectedItem(d);
            }

            JComboBox<ClassBatch> cmbClass = new JComboBox<>();
            Runnable refreshEditClasses = () -> {
                cmbClass.removeAllItems();
                Department selDept = (Department) cmbDept.getSelectedItem();
                if (selDept != null) {
                    List<ClassBatch> deptClasses = classDAO.getClassesByDepartment(selDept.getId());
                    for (ClassBatch cb : deptClasses) {
                        cmbClass.addItem(cb);
                        if (cb.getId() == s.getClassId()) cmbClass.setSelectedItem(cb);
                    }
                }
            };
            cmbDept.addActionListener(e -> refreshEditClasses.run());
            refreshEditClasses.run();

            p.add(new JLabel("Full Name:"));
            p.add(txtName);
            p.add(new JLabel("Department:"));
            p.add(cmbDept);
            p.add(new JLabel("Assigned Class:"));
            p.add(cmbClass);
            p.add(new JLabel("Class Roll Number:"));
            p.add(txtRoll);
            p.add(new JLabel("Email Address:"));
            p.add(txtEmail);
            p.add(new JLabel("Contact Phone:"));
            p.add(txtPhone);
            p.add(new JLabel("Guardian Name:"));
            p.add(txtGName);
            p.add(new JLabel("Guardian Phone:"));
            p.add(txtGPhone);

            JButton btnSave = UIComponents.createSuccessButton("Save Updated Details");
            btnSave.addActionListener(e -> {
                Department selDept = (Department) cmbDept.getSelectedItem();
                ClassBatch sel = (ClassBatch) cmbClass.getSelectedItem();
                if (sel != null) s.setClassId(sel.getId());
                if (selDept != null) s.setDepartmentId(selDept.getId());
                s.setFullName(txtName.getText().trim());
                s.setRollNumber(txtRoll.getText().trim());
                s.setEmail(txtEmail.getText().trim());
                s.setPhone(txtPhone.getText().trim());
                s.setGuardianName(txtGName.getText().trim());
                s.setGuardianPhone(txtGPhone.getText().trim());

                if (studentDAO.updateStudent(s)) {
                    loadStudents();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Student record updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error updating student record.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dlg.setLayout(new BorderLayout());
            dlg.add(p, BorderLayout.CENTER);
            dlg.add(btnSave, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Student ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleSelectedStudent() {
        String input = JOptionPane.showInputDialog(this, "Enter Student ID to toggle active status:", "Toggle Student Status", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int sid = Integer.parseInt(input.trim());
                Student s = studentDAO.getById(sid);
                if (s != null) {
                    User u = userDAO.getById(s.getUserId());
                    if (u != null) {
                        boolean newStatus = !u.isActive();
                        userDAO.updateUserStatus(u.getId(), newStatus);
                        loadStudents();
                        JOptionPane.showMessageDialog(this, "Student status changed to: " + (newStatus ? "ACTIVE" : "INACTIVE"));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 3. FACULTY MANAGEMENT (WITH AUTO FACULTY ID GENERATION)
    // =========================================================================

    private JPanel createFacultyManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewFaculty = UIComponents.createPrimaryButton("➕ Provision New Faculty");
        btnNewFaculty.addActionListener(e -> showCreateFacultyDialog());

        JButton btnEditFaculty = UIComponents.createSecondaryButton("✏️ Edit Faculty");
        btnEditFaculty.addActionListener(e -> showEditFacultyDialog());

        JButton btnToggleStatus = UIComponents.createSecondaryButton("Toggle Active / Inactive");
        btnToggleStatus.addActionListener(e -> toggleSelectedFaculty());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadFaculty());

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_MUTED);

        txtFacultySearch = new JTextField(12);
        txtFacultySearch.putClientProperty("JTextField.placeholderText", "Name or Fac ID...");
        txtFacultySearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterFaculty();
            }
        });

        cmbFacultyDeptFilter = new JComboBox<>();
        cmbFacultyDeptFilter.addItem("All Departments");
        cmbFacultyDeptFilter.addActionListener(e -> filterFaculty());

        toolbar.add(btnNewFaculty);
        toolbar.add(btnEditFaculty);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnRefresh);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(lblSearch);
        toolbar.add(txtFacultySearch);
        toolbar.add(cmbFacultyDeptFilter);

        // Faculty Table
        String[] cols = {
                "ID", "Faculty ID", "Full Name", "Designation",
                "Department", "Qualification", "Joining Date", "Contact Email", "Phone", "Roles"
        };
        facultyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIComponents.createStyledTable(facultyTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);
        table.getColumnModel().getColumn(9).setPreferredWidth(110);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadFaculty() {
        cachedFaculty = facultyDAO.getAllFaculty();

        if (cmbFacultyDeptFilter != null) {
            String selected = (String) cmbFacultyDeptFilter.getSelectedItem();
            cmbFacultyDeptFilter.removeAllItems();
            cmbFacultyDeptFilter.addItem("All Departments");
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) {
                cmbFacultyDeptFilter.addItem(d.getName());
            }
            if (selected != null) cmbFacultyDeptFilter.setSelectedItem(selected);
        }

        filterFaculty();
        refreshDashboardStatistics();
    }

    private void filterFaculty() {
        if (facultyTableModel == null || cachedFaculty == null) return;
        facultyTableModel.setRowCount(0);

        String query = txtFacultySearch != null ? txtFacultySearch.getText().trim().toLowerCase() : "";
        String deptFilter = cmbFacultyDeptFilter != null ? (String) cmbFacultyDeptFilter.getSelectedItem() : "All Departments";

        for (Faculty f : cachedFaculty) {
            if (deptFilter != null && !deptFilter.equals("All Departments") && !deptFilter.equalsIgnoreCase(f.getDepartmentName())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean match = (f.getFullName() != null && f.getFullName().toLowerCase().contains(query))
                        || (f.getFacultyId() != null && f.getFacultyId().toLowerCase().contains(query))
                        || (f.getDesignation() != null && f.getDesignation().toLowerCase().contains(query));
                if (!match) continue;
            }

            String roles = (f.isHod() ? "HOD " : "") + (f.isCfa() ? "CFA " : "") + (!f.isHod() && !f.isCfa() ? "Teacher" : "");

            facultyTableModel.addRow(new Object[]{
                    f.getId(),
                    f.getFacultyId(),
                    f.getFullName(),
                    f.getDesignation(),
                    f.getDepartmentName(),
                    f.getQualification() != null ? f.getQualification() : "-",
                    f.getJoiningDate() != null ? f.getJoiningDate().toString() : "-",
                    f.getEmail() != null ? f.getEmail() : "-",
                    f.getPhone() != null ? f.getPhone() : "-",
                    roles.trim()
            });
        }
    }

    private void showCreateFacultyDialog() {
        JDialog dlg = new JDialog(this, "Provision Faculty Member (Auto Faculty ID)", true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(10, 2, 8, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtName = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();

        List<Department> depts = departmentDAO.getAllDepartments();
        JComboBox<Department> cmbDept = new JComboBox<>();
        for (Department d : depts) cmbDept.addItem(d);

        JTextField txtAutoFacId = new JTextField();
        txtAutoFacId.setEditable(false);
        txtAutoFacId.setFont(UITheme.FONT_BOLD);
        txtAutoFacId.setForeground(UITheme.PRIMARY);

        JComboBox<String> cmbDesignation = new JComboBox<>(new String[]{
                "Assistant Professor", "Associate Professor", "Professor", "Head of Department", "Adjunct Faculty"
        });
        JTextField txtQual = new JTextField("M.Tech / Ph.D");
        JTextField txtJoiningDate = new JTextField(LocalDate.now().toString());

        Runnable updateFacId = () -> {
            Department selDept = (Department) cmbDept.getSelectedItem();
            if (selDept != null) {
                String nextId = facultyDAO.generateNextFacultyId(selDept.getCode());
                txtAutoFacId.setText(nextId);
            }
        };
        cmbDept.addActionListener(e -> updateFacId.run());
        updateFacId.run();

        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtUsername.getText().trim().isEmpty() && !txtName.getText().trim().isEmpty()) {
                    String clean = txtName.getText().trim().toLowerCase().replaceAll("[^a-z0-9]", ".");
                    txtUsername.setText(clean);
                    if (txtEmail.getText().trim().isEmpty()) {
                        txtEmail.setText(clean + "@acadexia.edu");
                    }
                }
            }
        });

        p.add(new JLabel("Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Department:"));
        p.add(cmbDept);
        p.add(new JLabel("Official Faculty ID (Auto):"));
        p.add(txtAutoFacId);
        p.add(new JLabel("Designation:"));
        p.add(cmbDesignation);
        p.add(new JLabel("Academic Qualification:"));
        p.add(txtQual);
        p.add(new JLabel("Joining Date (YYYY-MM-DD):"));
        p.add(txtJoiningDate);
        p.add(new JLabel("Account Username:"));
        p.add(txtUsername);
        p.add(new JLabel("Initial Password:"));
        p.add(txtPass);
        p.add(new JLabel("Email Address:"));
        p.add(txtEmail);
        p.add(new JLabel("Contact Phone:"));
        p.add(txtPhone);

        JButton btnSubmit = UIComponents.createPrimaryButton("Provision Faculty Member");
        btnSubmit.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()
                    || txtPass.getPassword().length == 0 || txtAutoFacId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name, username, password, and department are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Department selDept = (Department) cmbDept.getSelectedItem();
            if (selDept == null) return;

            User u = new User();
            u.setUsername(txtUsername.getText().trim());
            u.setFullName(txtName.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setPhone(txtPhone.getText().trim());
            u.setDepartmentId(selDept.getId());
            u.setRole(Role.FACULTY);
            u.setActive(true);

            try {
                int userId = userDAO.createUser(u, new String(txtPass.getPassword()));
                if (userId > 0) {
                    Faculty f = new Faculty();
                    f.setUserId(userId);
                    f.setFacultyId(txtAutoFacId.getText().trim());
                    f.setDesignation((String) cmbDesignation.getSelectedItem());
                    f.setQualification(txtQual.getText().trim());
                    f.setDepartmentId(selDept.getId());
                    try {
                        f.setJoiningDate(Date.valueOf(txtJoiningDate.getText().trim()));
                    } catch (Exception ignored) {
                        f.setJoiningDate(Date.valueOf(LocalDate.now()));
                    }

                    int facId = facultyDAO.createFaculty(f);
                    if (facId > 0) {
                        AuditLogDAO.log(userId, u.getUsername(), "ADMIN", "FACULTY_PROVISIONED", "FACULTY",
                                String.valueOf(facId), "Official Faculty ID: " + f.getFacultyId(), AuditLog.Severity.INFO);

                        loadFaculty();
                        dlg.dispose();
                        JOptionPane.showMessageDialog(this,
                                "Faculty member provisioned successfully!\nOfficial Faculty ID: " + f.getFacultyId(),
                                "Provisioning Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditFacultyDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Faculty ID to Edit Details:", "Edit Faculty", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int fid = Integer.parseInt(input.trim());
            Faculty f = facultyDAO.getById(fid);
            if (f == null) {
                JOptionPane.showMessageDialog(this, "Faculty record with ID " + fid + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Edit Faculty #" + f.getId() + " (" + f.getFacultyId() + ")", true);
            dlg.setSize(480, 440);
            dlg.setLocationRelativeTo(this);

            JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
            p.setBackground(UITheme.CARD_BG);
            p.setBorder(new EmptyBorder(20, 24, 20, 24));

            JTextField txtName = new JTextField(f.getFullName());
            JComboBox<String> cmbDesignation = new JComboBox<>(new String[]{
                    "Assistant Professor", "Associate Professor", "Professor", "Head of Department", "Adjunct Faculty"
            });
            cmbDesignation.setSelectedItem(f.getDesignation());
            JTextField txtQual = new JTextField(f.getQualification() != null ? f.getQualification() : "");
            JTextField txtEmail = new JTextField(f.getEmail() != null ? f.getEmail() : "");
            JTextField txtPhone = new JTextField(f.getPhone() != null ? f.getPhone() : "");

            List<Department> depts = departmentDAO.getAllDepartments();
            JComboBox<Department> cmbDept = new JComboBox<>();
            for (Department d : depts) {
                cmbDept.addItem(d);
                if (d.getId() == f.getDepartmentId()) cmbDept.setSelectedItem(d);
            }

            p.add(new JLabel("Full Name:"));
            p.add(txtName);
            p.add(new JLabel("Department:"));
            p.add(cmbDept);
            p.add(new JLabel("Designation:"));
            p.add(cmbDesignation);
            p.add(new JLabel("Qualification:"));
            p.add(txtQual);
            p.add(new JLabel("Email Address:"));
            p.add(txtEmail);
            p.add(new JLabel("Contact Phone:"));
            p.add(txtPhone);

            JButton btnSave = UIComponents.createSuccessButton("Save Updated Details");
            btnSave.addActionListener(e -> {
                Department selDept = (Department) cmbDept.getSelectedItem();
                if (selDept != null) f.setDepartmentId(selDept.getId());
                f.setFullName(txtName.getText().trim());
                f.setDesignation((String) cmbDesignation.getSelectedItem());
                f.setQualification(txtQual.getText().trim());
                f.setEmail(txtEmail.getText().trim());
                f.setPhone(txtPhone.getText().trim());

                if (facultyDAO.updateFaculty(f)) {
                    loadFaculty();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Faculty record updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error updating faculty record.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dlg.setLayout(new BorderLayout());
            dlg.add(p, BorderLayout.CENTER);
            dlg.add(btnSave, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Faculty ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleSelectedFaculty() {
        String input = JOptionPane.showInputDialog(this, "Enter Faculty ID to toggle status:", "Toggle Faculty Status", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int fid = Integer.parseInt(input.trim());
                Faculty f = facultyDAO.getById(fid);
                if (f != null) {
                    User u = userDAO.getById(f.getUserId());
                    if (u != null) {
                        boolean newStatus = !u.isActive();
                        userDAO.updateUserStatus(u.getId(), newStatus);
                        loadFaculty();
                        JOptionPane.showMessageDialog(this, "Faculty status changed to: " + (newStatus ? "ACTIVE" : "INACTIVE"));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Faculty record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 4. DEPARTMENT & HOD GOVERNANCE
    // =========================================================================

    private JPanel createDepartmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewDept = UIComponents.createPrimaryButton("➕ Add New Department");
        btnNewDept.addActionListener(e -> showCreateDepartmentDialog());

        JButton btnAssignHod = UIComponents.createSuccessButton("👑 Appoint / Change HOD");
        btnAssignHod.addActionListener(e -> showAssignHodDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadDepartments());

        toolbar.add(btnNewDept);
        toolbar.add(btnAssignHod);
        toolbar.add(btnRefresh);

        // Department Table
        String[] cols = {"Dept ID", "Code", "Department Name", "Current Head (HOD)", "Faculty Count", "Students", "Active Classes"};
        deptTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIComponents.createStyledTable(deptTableModel);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadDepartments() {
        if (deptTableModel == null) return;
        deptTableModel.setRowCount(0);

        List<DepartmentDAO.DepartmentStat> stats = departmentDAO.getDepartmentStats();
        for (DepartmentDAO.DepartmentStat s : stats) {
            deptTableModel.addRow(new Object[]{
                    s.id(),
                    s.code(),
                    s.name(),
                    s.hodName(),
                    s.facultyCount(),
                    s.studentCount(),
                    s.classCount()
            });
        }
        refreshDashboardStatistics();
    }

    private void showCreateDepartmentDialog() {
        JDialog dlg = new JDialog(this, "Create Academic Department", true);
        dlg.setSize(420, 240);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();

        p.add(new JLabel("Department Code (e.g. MECH, IT):"));
        p.add(txtCode);
        p.add(new JLabel("Full Department Name:"));
        p.add(txtName);

        JButton btnSubmit = UIComponents.createPrimaryButton("Create Department");
        btnSubmit.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Code and name are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (departmentDAO.createDepartment(txtCode.getText().trim(), txtName.getText().trim())) {
                loadDepartments();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Department created successfully!");
            } else {
                JOptionPane.showMessageDialog(dlg, "Error creating department. Check if code already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAssignHodDialog() {
        JDialog dlg = new JDialog(this, "Appoint / Change Department Head (HOD)", true);
        dlg.setSize(480, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        List<Department> depts = departmentDAO.getAllDepartments();
        JComboBox<Department> cmbDept = new JComboBox<>();
        for (Department d : depts) cmbDept.addItem(d);

        JComboBox<Faculty> cmbFac = new JComboBox<>();

        Runnable updateFacultyForDept = () -> {
            cmbFac.removeAllItems();
            Department selDept = (Department) cmbDept.getSelectedItem();
            if (selDept != null) {
                List<Faculty> facList = facultyDAO.getFacultyByDepartment(selDept.getId());
                for (Faculty f : facList) {
                    cmbFac.addItem(f);
                }
            }
        };

        cmbDept.addActionListener(e -> updateFacultyForDept.run());
        updateFacultyForDept.run();

        p.add(new JLabel("Select Department:"));
        p.add(cmbDept);
        p.add(new JLabel("Appoint HOD from Faculty:"));
        p.add(cmbFac);

        JButton btnSave = UIComponents.createSuccessButton("Appoint Head of Department");
        btnSave.addActionListener(e -> {
            Department selDept = (Department) cmbDept.getSelectedItem();
            Faculty selFac = (Faculty) cmbFac.getSelectedItem();
            if (selDept != null && selFac != null) {
                if (departmentDAO.updateHod(selDept.getId(), selFac.getId())) {
                    loadDepartments();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "HOD appointed: " + selFac.getFullName() + " for " + selDept.getName());
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error appointing HOD.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSave, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // 5. EXAMINATION SCHEDULING MANAGEMENT
    // =========================================================================

    private JPanel createExamSchedulingPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewExam = UIComponents.createPrimaryButton("➕ Schedule New Exam");
        btnNewExam.addActionListener(e -> showCreateExamScheduleDialog());

        JButton btnDeleteExam = UIComponents.createDangerButton("🗑️ Cancel / Delete Schedule");
        btnDeleteExam.addActionListener(e -> deleteSelectedExamSchedule());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadExamSchedules());

        toolbar.add(btnNewExam);
        toolbar.add(btnDeleteExam);
        toolbar.add(btnRefresh);

        // Exam Table
        String[] cols = {
                "Schedule ID", "Class", "Subject", "Exam Type", "Exam Name",
                "Exam Date", "Start Time", "End Time", "Room / Hall", "Max Marks"
        };
        examTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIComponents.createStyledTable(examTableModel);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadExamSchedules() {
        if (examTableModel == null) return;
        examTableModel.setRowCount(0);

        List<ExamSchedule> schedules = examScheduleDAO.getAllExamSchedules();
        for (ExamSchedule es : schedules) {
            examTableModel.addRow(new Object[]{
                    es.getId(),
                    es.getClassName(),
                    es.getSubjectCode() + " - " + es.getSubjectName(),
                    es.getExamType().name(),
                    es.getExamName(),
                    es.getExamDate().toString(),
                    es.getStartTime(),
                    es.getEndTime(),
                    es.getRoomHall(),
                    es.getMaxMarks()
            });
        }
    }

    private void showCreateExamScheduleDialog() {
        JDialog dlg = new JDialog(this, "Schedule Institutional Examination", true);
        dlg.setSize(520, 520);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(9, 2, 8, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        List<ClassBatch> classes = classDAO.getAllClasses();
        JComboBox<ClassBatch> cmbClass = new JComboBox<>();
        for (ClassBatch cb : classes) cmbClass.addItem(cb);

        JComboBox<Subject> cmbSub = new JComboBox<>();

        Runnable updateSubjects = () -> {
            cmbSub.removeAllItems();
            ClassBatch cb = (ClassBatch) cmbClass.getSelectedItem();
            if (cb != null) {
                List<Subject> subs = subjectDAO.getSubjectsByClass(cb.getId());
                for (Subject s : subs) cmbSub.addItem(s);
            }
        };
        cmbClass.addActionListener(e -> updateSubjects.run());
        updateSubjects.run();

        JComboBox<Mark.ExamType> cmbType = new JComboBox<>(Mark.ExamType.values());
        JTextField txtExamName = new JTextField("Semester Internal Assessment");
        JTextField txtDate = new JTextField(LocalDate.now().plusDays(7).toString());
        JTextField txtStart = new JTextField("10:00 AM");
        JTextField txtEnd = new JTextField("12:00 PM");
        JTextField txtRoom = new JTextField("Exam Hall A-101");
        JTextField txtMax = new JTextField("50.0");

        p.add(new JLabel("Target Class Batch:"));
        p.add(cmbClass);
        p.add(new JLabel("Course / Subject:"));
        p.add(cmbSub);
        p.add(new JLabel("Examination Type:"));
        p.add(cmbType);
        p.add(new JLabel("Exam Name / Title:"));
        p.add(txtExamName);
        p.add(new JLabel("Date (YYYY-MM-DD):"));
        p.add(txtDate);
        p.add(new JLabel("Start Time:"));
        p.add(txtStart);
        p.add(new JLabel("End Time:"));
        p.add(txtEnd);
        p.add(new JLabel("Room / Hall Allocated:"));
        p.add(txtRoom);
        p.add(new JLabel("Maximum Marks:"));
        p.add(txtMax);

        JButton btnSubmit = UIComponents.createPrimaryButton("Schedule & Announce Examination");
        btnSubmit.addActionListener(e -> {
            ClassBatch cb = (ClassBatch) cmbClass.getSelectedItem();
            Subject s = (Subject) cmbSub.getSelectedItem();
            if (cb == null || s == null || txtDate.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please select class, subject, and valid date.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                ExamSchedule es = new ExamSchedule();
                es.setClassId(cb.getId());
                es.setSubjectId(s.getId());
                es.setExamType((Mark.ExamType) cmbType.getSelectedItem());
                es.setExamName(txtExamName.getText().trim());
                es.setExamDate(Date.valueOf(txtDate.getText().trim()));
                es.setStartTime(txtStart.getText().trim());
                es.setEndTime(txtEnd.getText().trim());
                es.setRoomHall(txtRoom.getText().trim());
                es.setMaxMarks(Double.parseDouble(txtMax.getText().trim()));
                es.setPortionsSyllabus("Modules 1 - 3");

                if (examScheduleDAO.createExamSchedule(es)) {
                    loadExamSchedules();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Examination schedule created successfully!");
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error creating schedule.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteSelectedExamSchedule() {
        String input = JOptionPane.showInputDialog(this, "Enter Schedule ID to delete:", "Cancel Exam Schedule", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(input.trim());
                if (examScheduleDAO.deleteExamSchedule(id)) {
                    loadExamSchedules();
                    JOptionPane.showMessageDialog(this, "Exam schedule removed.");
                } else {
                    JOptionPane.showMessageDialog(this, "Schedule ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 6. MARKS ENTRY, RESULT VERIFICATION & PUBLICATION MANAGEMENT
    // =========================================================================

    private JPanel createMarksAndResultsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Sub tabbed pane for:
        // Tab A: Interactive Marks Entry & Verification for a Class
        // Tab B: Institutional Result Publication Tracker
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(UITheme.FONT_REGULAR);

        subTabs.addTab("📝 Class Marks Entry & Official Publication", createMarksBatchPanel());
        subTabs.addTab("📢 Institution-Wide Publication Tracker", createPublicationTrackerPanel());

        mainPanel.add(subTabs, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createMarksBatchPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        // Filter / Selection Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setOpaque(false);

        cmbMarksClass = new JComboBox<>();
        cmbMarksSubject = new JComboBox<>();
        cmbMarksExamType = new JComboBox<>(Mark.ExamType.values());
        txtMarksAcademicYear = new JTextField("2024-2025", 8);

        cmbMarksClass.addActionListener(e -> updateMarksSubjects());
        cmbMarksSubject.addActionListener(e -> loadMarksEntryRows());
        cmbMarksExamType.addActionListener(e -> loadMarksEntryRows());

        JButton btnLoad = UIComponents.createSecondaryButton("🔄 Load Marks");
        btnLoad.addActionListener(e -> loadMarksEntryRows());

        filterBar.add(new JLabel("Class Batch:"));
        filterBar.add(cmbMarksClass);
        filterBar.add(new JLabel("Subject:"));
        filterBar.add(cmbMarksSubject);
        filterBar.add(new JLabel("Exam Type:"));
        filterBar.add(cmbMarksExamType);
        filterBar.add(new JLabel("Year:"));
        filterBar.add(txtMarksAcademicYear);
        filterBar.add(btnLoad);

        // Action Toolbar with Status Badge
        JPanel actionBar = new JPanel(new BorderLayout(10, 0));
        actionBar.setOpaque(false);

        JPanel statusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusLeft.setOpaque(false);
        lblResultStatusBadge = UIComponents.createBadge("STATUS: DRAFT / ENTERED", new Color(75, 85, 99), Color.WHITE);
        statusLeft.add(new JLabel("Current Result Status:"));
        statusLeft.add(lblResultStatusBadge);

        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRight.setOpaque(false);

        JButton btnSaveMarks = UIComponents.createPrimaryButton("💾 Save Marks Batch");
        btnSaveMarks.addActionListener(e -> saveBatchMarks());

        btnVerifyResults = UIComponents.createSuccessButton("✅ Verify Results");
        btnVerifyResults.addActionListener(e -> verifyBatchResults());

        btnPublishResults = UIComponents.createPrimaryButton("📢 Publish Officially");
        btnPublishResults.setBackground(new Color(147, 51, 234)); // Regal Purple
        btnPublishResults.addActionListener(e -> publishBatchResults());

        actionsRight.add(btnSaveMarks);
        actionsRight.add(btnVerifyResults);
        actionsRight.add(btnPublishResults);

        actionBar.add(statusLeft, BorderLayout.WEST);
        actionBar.add(actionsRight, BorderLayout.EAST);

        // Combined Top Controls
        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        topContainer.setOpaque(false);
        topContainer.add(filterBar, BorderLayout.NORTH);
        topContainer.add(actionBar, BorderLayout.SOUTH);

        // Editable Marks Table
        String[] cols = {"Student ID", "Roll No", "Register Number", "Student Full Name", "Max Marks", "Marks Obtained"};
        marksEntryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4 || c == 5; // Editable max marks and marks obtained
            }
        };

        JTable table = UIComponents.createStyledTable(marksEntryTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadMarksClasses() {
        if (cmbMarksClass == null) return;
        cmbMarksClass.removeAllItems();
        List<ClassBatch> classes = classDAO.getAllClasses();
        for (ClassBatch cb : classes) {
            cmbMarksClass.addItem(cb);
        }
        updateMarksSubjects();
    }

    private void updateMarksSubjects() {
        if (cmbMarksSubject == null || cmbMarksClass == null) return;
        cmbMarksSubject.removeAllItems();
        ClassBatch cb = (ClassBatch) cmbMarksClass.getSelectedItem();
        if (cb != null) {
            List<Subject> subs = subjectDAO.getSubjectsByClass(cb.getId());
            for (Subject s : subs) {
                cmbMarksSubject.addItem(s);
            }
        }
        loadMarksEntryRows();
    }

    private void loadMarksEntryRows() {
        if (marksEntryTableModel == null || cmbMarksClass == null || cmbMarksSubject == null) return;
        marksEntryTableModel.setRowCount(0);

        ClassBatch cb = (ClassBatch) cmbMarksClass.getSelectedItem();
        Subject sub = (Subject) cmbMarksSubject.getSelectedItem();
        Mark.ExamType examType = (Mark.ExamType) cmbMarksExamType.getSelectedItem();
        String academicYear = txtMarksAcademicYear.getText().trim();

        if (cb == null || sub == null || examType == null) return;

        List<MarkDAO.StudentMarksEntryRow> rows = markDAO.getClassSubjectMarks(cb.getId(), sub.getId(), examType);
        for (MarkDAO.StudentMarksEntryRow r : rows) {
            marksEntryTableModel.addRow(new Object[]{
                    r.studentId(),
                    r.rollNumber(),
                    r.registerNumber(),
                    r.fullName(),
                    r.maxMarks() != null ? r.maxMarks() : 50.0,
                    r.marksObtained() != null ? r.marksObtained() : ""
            });
        }

        // Fetch current publication status
        ResultPublication pub = publicationDAO.getPublication(cb.getId(), sub.getId(), examType, academicYear);
        if (pub != null) {
            if (pub.getStatus() == ResultPublication.PublicationStatus.PUBLISHED) {
                lblResultStatusBadge.setText("STATUS: PUBLISHED (" + (pub.getPublishedByName() != null ? pub.getPublishedByName() : "Admin") + ")");
                lblResultStatusBadge.setBackground(new Color(16, 185, 129)); // Green
            } else if (pub.getStatus() == ResultPublication.PublicationStatus.VERIFIED) {
                lblResultStatusBadge.setText("STATUS: VERIFIED (" + (pub.getVerifiedByName() != null ? pub.getVerifiedByName() : "Admin") + ")");
                lblResultStatusBadge.setBackground(new Color(59, 130, 246)); // Blue
            } else {
                lblResultStatusBadge.setText("STATUS: DRAFT / ENTERED");
                lblResultStatusBadge.setBackground(new Color(107, 114, 128)); // Slate Gray
            }
        } else {
            lblResultStatusBadge.setText("STATUS: DRAFT (Unverified)");
            lblResultStatusBadge.setBackground(new Color(107, 114, 128));
        }
    }

    private void saveBatchMarks() {
        if (marksEntryTableModel == null) return;

        ClassBatch cb = (ClassBatch) cmbMarksClass.getSelectedItem();
        Subject sub = (Subject) cmbMarksSubject.getSelectedItem();
        Mark.ExamType examType = (Mark.ExamType) cmbMarksExamType.getSelectedItem();
        String academicYear = txtMarksAcademicYear.getText().trim();

        if (cb == null || sub == null || examType == null) {
            JOptionPane.showMessageDialog(this, "Please select class, subject, and exam type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        int adminUserId = (currentUser != null) ? currentUser.getId() : 2;

        int savedCount = 0;
        for (int i = 0; i < marksEntryTableModel.getRowCount(); i++) {
            int studentId = (Integer) marksEntryTableModel.getValueAt(i, 0);
            Object maxObj = marksEntryTableModel.getValueAt(i, 4);
            Object obtainedObj = marksEntryTableModel.getValueAt(i, 5);

            if (obtainedObj != null && !obtainedObj.toString().trim().isEmpty()) {
                try {
                    double maxMarks = Double.parseDouble(maxObj.toString().trim());
                    double obtained = Double.parseDouble(obtainedObj.toString().trim());
                    if (obtained < 0 || obtained > maxMarks) {
                        JOptionPane.showMessageDialog(this, "Invalid marks for row " + (i + 1) + ". Obtained marks must be between 0 and " + maxMarks);
                        return;
                    }

                    if (markDAO.recordMark(studentId, sub.getId(), examType, obtained, maxMarks, Date.valueOf(LocalDate.now()), adminUserId)) {
                        savedCount++;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number format in row " + (i + 1));
                    return;
                }
            }
        }

        // Record draft status in publication registry
        publicationDAO.recordOrUpdateStatus(cb.getId(), sub.getId(), examType, academicYear,
                ResultPublication.PublicationStatus.ENTERED, adminUserId, "Draft marks recorded by Admin");

        loadMarksEntryRows();
        loadPublicationTracker();
        refreshDashboardStatistics();
        JOptionPane.showMessageDialog(this, "Successfully saved marks for " + savedCount + " students!");
    }

    private void verifyBatchResults() {
        ClassBatch cb = (ClassBatch) cmbMarksClass.getSelectedItem();
        Subject sub = (Subject) cmbMarksSubject.getSelectedItem();
        Mark.ExamType examType = (Mark.ExamType) cmbMarksExamType.getSelectedItem();
        String academicYear = txtMarksAcademicYear.getText().trim();

        if (cb == null || sub == null || examType == null) return;

        User currentUser = SessionManager.getInstance().getCurrentUser();
        int adminUserId = (currentUser != null) ? currentUser.getId() : 2;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Verify and approve all marks for:\nClass: " + cb.getName() + "\nSubject: " + sub.getName()
                        + "\nExam: " + examType.name() + "\n\nProceed to mark results as VERIFIED?",
                "Confirm Result Verification", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (publicationDAO.verifyResults(cb.getId(), sub.getId(), examType, academicYear, adminUserId)) {
                AuditLogDAO.log(adminUserId, (currentUser != null ? currentUser.getUsername() : "ADMIN"), "ADMIN",
                        "RESULTS_VERIFIED", "EXAM", String.valueOf(sub.getId()),
                        "Verified marks for " + cb.getName() + " - " + sub.getCode() + " " + examType.name(), AuditLog.Severity.INFO);

                loadMarksEntryRows();
                loadPublicationTracker();
                refreshDashboardStatistics();
                JOptionPane.showMessageDialog(this, "Results successfully verified! They can now be officially published.");
            } else {
                JOptionPane.showMessageDialog(this, "Error updating verification status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void publishBatchResults() {
        ClassBatch cb = (ClassBatch) cmbMarksClass.getSelectedItem();
        Subject sub = (Subject) cmbMarksSubject.getSelectedItem();
        Mark.ExamType examType = (Mark.ExamType) cmbMarksExamType.getSelectedItem();
        String academicYear = txtMarksAcademicYear.getText().trim();

        if (cb == null || sub == null || examType == null) return;

        User currentUser = SessionManager.getInstance().getCurrentUser();
        int adminUserId = (currentUser != null) ? currentUser.getId() : 2;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Official Publication Notice:\n\nPublishing makes marks immediately visible on Student marksheets and Parent oversight portals.\n\nProceed to officially PUBLISH results?",
                "Confirm Official Result Publication", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (publicationDAO.publishResults(cb.getId(), sub.getId(), examType, academicYear, adminUserId)) {
                AuditLogDAO.log(adminUserId, (currentUser != null ? currentUser.getUsername() : "ADMIN"), "ADMIN",
                        "RESULTS_PUBLISHED", "EXAM", String.valueOf(sub.getId()),
                        "Officially published results for " + cb.getName() + " - " + sub.getCode() + " " + examType.name(), AuditLog.Severity.INFO);

                loadMarksEntryRows();
                loadPublicationTracker();
                refreshDashboardStatistics();
                JOptionPane.showMessageDialog(this,
                        "Results officially PUBLISHED!\nStudents and parents can now view these certified marks.",
                        "Publication Live", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error publishing results.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createPublicationTrackerPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        JButton btnRefreshTracker = UIComponents.createSecondaryButton("🔄 Refresh Status Tracker");
        btnRefreshTracker.addActionListener(e -> loadPublicationTracker());
        toolbar.add(btnRefreshTracker);

        String[] cols = {
                "ID", "Class Batch", "Subject Code", "Subject Name", "Exam Type",
                "Academic Year", "Publication Status", "Verified By", "Published By", "Published Date", "Remarks"
        };
        publicationTrackerModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIComponents.createStyledTable(publicationTrackerModel);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadPublicationTracker() {
        if (publicationTrackerModel == null) return;
        publicationTrackerModel.setRowCount(0);

        List<ResultPublication> list = publicationDAO.getAllPublications();
        for (ResultPublication rp : list) {
            publicationTrackerModel.addRow(new Object[]{
                    rp.getId(),
                    rp.getClassName(),
                    rp.getSubjectCode(),
                    rp.getSubjectName(),
                    rp.getExamType().name(),
                    rp.getAcademicYear(),
                    rp.getStatus().getLabel(),
                    rp.getVerifiedByName() != null ? rp.getVerifiedByName() : "-",
                    rp.getPublishedByName() != null ? rp.getPublishedByName() : "-",
                    rp.getPublishedAt() != null ? rp.getPublishedAt().toString() : "-",
                    rp.getRemarks() != null ? rp.getRemarks() : "-"
            });
        }
    }

    // =========================================================================
    // 7. PARENT ACCOUNTS & STUDENT LINKING GOVERNANCE
    // =========================================================================

    private JPanel createParentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewParent = UIComponents.createPrimaryButton("➕ Provision Parent & Link Student");
        btnNewParent.addActionListener(e -> showCreateParentDialog());

        JButton btnVerify = UIComponents.createSuccessButton("✅ Verify Parent Details");
        btnVerify.addActionListener(e -> showVerifyParentDialog());

        JButton btnRelink = UIComponents.createSecondaryButton("🔗 Re-link Student");
        btnRelink.addActionListener(e -> showRelinkStudentDialog());

        JButton btnToggleStatus = UIComponents.createSecondaryButton("Toggle Active / Deactivate");
        btnToggleStatus.addActionListener(e -> toggleSelectedParent());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadParents());

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_MUTED);

        txtParentSearch = new JTextField(12);
        txtParentSearch.putClientProperty("JTextField.placeholderText", "Guardian / Ward / Reg...");
        txtParentSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterParents();
            }
        });

        toolbar.add(btnNewParent);
        toolbar.add(btnVerify);
        toolbar.add(btnRelink);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnRefresh);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(lblSearch);
        toolbar.add(txtParentSearch);

        // Parent Table
        String[] cols = {
                "Parent ID", "Username", "Guardian Name", "Relationship",
                "Linked Student", "Register No", "Class",
                "Phone", "Email", "Verification Status", "Account Status", "Created At"
        };
        parentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = UIComponents.createStyledTable(parentTableModel);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadParents() {
        if (parentTableModel == null) return;
        cachedParents = parentDAO.getAllParents();
        filterParents();
    }

    private void filterParents() {
        if (parentTableModel == null || cachedParents == null) return;
        parentTableModel.setRowCount(0);

        String query = (txtParentSearch != null) ? txtParentSearch.getText().trim().toLowerCase() : "";

        for (Parent p : cachedParents) {
            if (!query.isEmpty()) {
                boolean match = (p.getFullName() != null && p.getFullName().toLowerCase().contains(query))
                        || (p.getUsername() != null && p.getUsername().toLowerCase().contains(query))
                        || (p.getStudentFullName() != null && p.getStudentFullName().toLowerCase().contains(query))
                        || (p.getStudentRegisterNumber() != null && p.getStudentRegisterNumber().toLowerCase().contains(query));
                if (!match) continue;
            }

            parentTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getUsername(),
                    p.getFullName(),
                    p.getRelationship(),
                    p.getStudentFullName() != null ? p.getStudentFullName() : "-",
                    p.getStudentRegisterNumber() != null ? p.getStudentRegisterNumber() : "-",
                    p.getStudentClassName() != null ? p.getStudentClassName() : "-",
                    p.getPhone() != null ? p.getPhone() : "-",
                    p.getEmail() != null ? p.getEmail() : "-",
                    p.getVerificationStatus().getLabel(),
                    p.isActive() ? "ACTIVE" : "INACTIVE",
                    p.getCreatedAt()
            });
        }
    }

    private void showCreateParentDialog() {
        JDialog dlg = new JDialog(this, "Provision Parent Account & Link Student", true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(10, 2, 8, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtU = new JTextField();
        JPasswordField txtP = new JPasswordField();
        JTextField txtName = new JTextField();
        JComboBox<String> cmbRel = new JComboBox<>(new String[]{"Father", "Mother", "Guardian", "Foster Parent"});
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtOccupation = new JTextField();
        JTextField txtAddress = new JTextField();
        JComboBox<Parent.VerificationStatus> cmbVer = new JComboBox<>(Parent.VerificationStatus.values());

        List<Student> students = studentDAO.getAllStudents();
        JComboBox<Student> cmbStudents = new JComboBox<>();
        for (Student s : students) cmbStudents.addItem(s);

        InputFilterFactory.applyIdentifierFilter(txtU, 30);
        InputFilterFactory.applyPasswordFilter(txtP, 30);
        InputFilterFactory.applySqlSanitizer(txtName, 60, true);
        InputFilterFactory.applySqlSanitizer(txtEmail, 60, false);
        InputFilterFactory.applySqlSanitizer(txtPhone, 25, true);

        p.add(new JLabel("Parent Username:"));
        p.add(txtU);
        p.add(new JLabel("Initial Password:"));
        p.add(txtP);
        p.add(new JLabel("Guardian Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Relationship to Child:"));
        p.add(cmbRel);
        p.add(new JLabel("Link to Student (Ward):"));
        p.add(cmbStudents);
        p.add(new JLabel("Contact Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Primary Phone:"));
        p.add(txtPhone);
        p.add(new JLabel("Occupation:"));
        p.add(txtOccupation);
        p.add(new JLabel("Address:"));
        p.add(txtAddress);
        p.add(new JLabel("Initial Verification Status:"));
        p.add(cmbVer);

        JButton btnSubmit = UIComponents.createPrimaryButton("Create & Securely Link Account");
        btnSubmit.addActionListener(e -> {
            if (txtU.getText().trim().isEmpty() || txtP.getPassword().length == 0 || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Username, password, and guardian name are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student selStu = (Student) cmbStudents.getSelectedItem();
            if (selStu == null) {
                JOptionPane.showMessageDialog(dlg, "Please select a student to link.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Parent parent = new Parent();
            parent.setUsername(txtU.getText().trim());
            parent.setFullName(txtName.getText().trim());
            parent.setRelationship((String) cmbRel.getSelectedItem());
            parent.setStudentId(selStu.getId());
            parent.setEmail(txtEmail.getText().trim());
            parent.setPhone(txtPhone.getText().trim());
            parent.setOccupation(txtOccupation.getText().trim());
            parent.setAddress(txtAddress.getText().trim());
            parent.setEmergencyContact(txtPhone.getText().trim());
            parent.setVerificationStatus((Parent.VerificationStatus) cmbVer.getSelectedItem());

            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (parent.getVerificationStatus() == Parent.VerificationStatus.VERIFIED && currentUser != null) {
                parent.setVerifiedByUserId(currentUser.getId());
                parent.setVerificationNotes("Verified by Admin at account provisioning.");
            }
            parent.setActive(true);

            try {
                int id = parentDAO.createParent(parent, new String(txtP.getPassword()));
                if (id > 0) {
                    loadParents();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Parent account created and linked successfully!\nParent ID: " + id + "\nLinked Student: " + selStu.getFullName() + " (" + selStu.getRegisterNumber() + ")",
                            "Account Provisioned", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error creating parent account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showVerifyParentDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Parent ID to verify details:", "Verify Parent Identity", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int pid = Integer.parseInt(input.trim());
            Parent parent = parentDAO.getParentById(pid);
            if (parent == null) {
                JOptionPane.showMessageDialog(this, "Parent record with ID " + pid + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Verify Parent Record #" + pid, true);
            dlg.setSize(520, 420);
            dlg.setLocationRelativeTo(this);

            JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
            p.setBackground(UITheme.CARD_BG);
            p.setBorder(new EmptyBorder(20, 24, 20, 24));

            p.add(new JLabel("Guardian Name:"));
            p.add(new JLabel(parent.getFullName()));
            p.add(new JLabel("Relationship & Contact:"));
            p.add(new JLabel(parent.getRelationship() + " • " + parent.getPhone()));
            p.add(new JLabel("Linked Ward (Student):"));
            p.add(new JLabel(parent.getStudentFullName() + " (" + parent.getStudentRegisterNumber() + ")"));
            p.add(new JLabel("Current Status:"));
            p.add(new JLabel(parent.getVerificationStatus().getLabel()));

            JComboBox<Parent.VerificationStatus> cmbStatus = new JComboBox<>(Parent.VerificationStatus.values());
            cmbStatus.setSelectedItem(parent.getVerificationStatus());
            p.add(new JLabel("Set Verification Status:"));
            p.add(cmbStatus);

            JTextField txtNotes = new JTextField(parent.getVerificationNotes() != null ? parent.getVerificationNotes() : "Identity & student relation verified with institutional records.");
            p.add(new JLabel("Verification Notes / ID Ref:"));
            p.add(txtNotes);

            JButton btnSave = UIComponents.createSuccessButton("Save Verification Status");
            btnSave.addActionListener(e -> {
                User admin = SessionManager.getInstance().getCurrentUser();
                int adminId = (admin != null) ? admin.getId() : 2;
                Parent.VerificationStatus newStatus = (Parent.VerificationStatus) cmbStatus.getSelectedItem();
                String notes = txtNotes.getText().trim();

                if (parentDAO.updateVerificationStatus(pid, newStatus, adminId, notes)) {
                    loadParents();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Parent verification status updated to " + newStatus.getLabel() + "!");
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error updating verification status.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dlg.setLayout(new BorderLayout());
            dlg.add(p, BorderLayout.CENTER);
            dlg.add(btnSave, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Parent ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRelinkStudentDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Parent ID to modify student linkage:", "Re-link Student to Parent", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int pid = Integer.parseInt(input.trim());
            Parent parent = parentDAO.getParentById(pid);
            if (parent == null) {
                JOptionPane.showMessageDialog(this, "Parent record with ID " + pid + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Re-link Ward for " + parent.getFullName(), true);
            dlg.setSize(480, 260);
            dlg.setLocationRelativeTo(this);

            JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
            p.setBackground(UITheme.CARD_BG);
            p.setBorder(new EmptyBorder(20, 24, 20, 24));

            p.add(new JLabel("Parent:"));
            p.add(new JLabel(parent.getFullName() + " (" + parent.getUsername() + ")"));

            List<Student> students = studentDAO.getAllStudents();
            JComboBox<Student> cmbStudents = new JComboBox<>();
            for (Student s : students) {
                cmbStudents.addItem(s);
                if (s.getId() == parent.getStudentId()) {
                    cmbStudents.setSelectedItem(s);
                }
            }
            p.add(new JLabel("Select New Linked Student:"));
            p.add(cmbStudents);

            JComboBox<String> cmbRel = new JComboBox<>(new String[]{"Father", "Mother", "Guardian", "Foster Parent"});
            cmbRel.setSelectedItem(parent.getRelationship());
            p.add(new JLabel("Relationship:"));
            p.add(cmbRel);

            JButton btnUpdate = UIComponents.createPrimaryButton("Update Student Link");
            btnUpdate.addActionListener(e -> {
                Student sel = (Student) cmbStudents.getSelectedItem();
                if (sel != null) {
                    if (parentDAO.linkParentToStudent(pid, sel.getId(), (String) cmbRel.getSelectedItem())) {
                        loadParents();
                        dlg.dispose();
                        JOptionPane.showMessageDialog(this, "Parent successfully linked to " + sel.getFullName() + " (" + sel.getRegisterNumber() + ")!");
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Error updating student linkage.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            dlg.setLayout(new BorderLayout());
            dlg.add(p, BorderLayout.CENTER);
            dlg.add(btnUpdate, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Parent ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleSelectedParent() {
        String input = JOptionPane.showInputDialog(this, "Enter Parent ID to toggle status:", "Toggle Parent Status", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int pid = Integer.parseInt(input.trim());
                Parent p = parentDAO.getParentById(pid);
                if (p != null) {
                    boolean newStatus = !p.isActive();
                    parentDAO.toggleParentStatus(p.getUserId(), newStatus);
                    AuditLogDAO.log(p.getUserId(), p.getUsername(), "ADMIN", "PARENT_STATUS_TOGGLED", "PARENT",
                            String.valueOf(pid), "Status changed to: " + (newStatus ? "ACTIVE" : "INACTIVE"), AuditLog.Severity.WARNING);
                    loadParents();
                    JOptionPane.showMessageDialog(this, "Parent account status updated to: " + (newStatus ? "ACTIVE" : "INACTIVE"));
                } else {
                    JOptionPane.showMessageDialog(this, "Parent ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Parent ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 8. USER ACCOUNTS ADMINISTRATION
    // =========================================================================

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewUser = UIComponents.createPrimaryButton("➕ Provision New User");
        btnNewUser.addActionListener(e -> showCreateUserDialog());

        JButton btnToggleStatus = UIComponents.createSecondaryButton("Toggle Active / Deactivate");
        btnToggleStatus.addActionListener(e -> toggleSelectedUser());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadUsers());

        toolbar.add(btnNewUser);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnRefresh);

        // User Table
        String[] cols = {"User ID", "Username", "Full Name", "Role", "Email", "Phone", "Status", "Created At"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIComponents.createStyledTable(userTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
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
                    u.getEmail() != null ? u.getEmail() : "-",
                    u.getPhone() != null ? u.getPhone() : "-",
                    u.isActive() ? "ACTIVE" : "INACTIVE",
                    u.getCreatedAt()
            });
        }
    }

    private void toggleSelectedUser() {
        String input = JOptionPane.showInputDialog(this, "Enter User ID to toggle status:", "Toggle User Status", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int uid = Integer.parseInt(input.trim());
                User u = userDAO.getById(uid);
                if (u != null) {
                    boolean newStatus = !u.isActive();
                    userDAO.updateUserStatus(uid, newStatus);
                    AuditLogDAO.log(uid, u.getUsername(), "ADMIN", "USER_STATUS_TOGGLED", "USER",
                            String.valueOf(uid), "Status changed to: " + (newStatus ? "ACTIVE" : "INACTIVE"), AuditLog.Severity.WARNING);
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "User status updated to: " + (newStatus ? "ACTIVE" : "INACTIVE"));
                } else {
                    JOptionPane.showMessageDialog(this, "User ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid User ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCreateUserDialog() {
        JDialog dlg = new JDialog(this, "Provision New User Account", true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 10, 12));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtU = new JTextField();
        JPasswordField txtP = new JPasswordField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<Role> cmbRole = new JComboBox<>(Role.values());

        InputFilterFactory.applyIdentifierFilter(txtU, 30);
        InputFilterFactory.applyPasswordFilter(txtP, 30);
        InputFilterFactory.applySqlSanitizer(txtName, 60, true);
        InputFilterFactory.applySqlSanitizer(txtEmail, 60, false);
        InputFilterFactory.applyNumericFilter(txtPhone, 15, false);

        p.add(new JLabel("Username:"));
        p.add(txtU);
        p.add(new JLabel("Password:"));
        p.add(txtP);
        p.add(new JLabel("Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Role:"));
        p.add(cmbRole);
        p.add(new JLabel("Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Phone:"));
        p.add(txtPhone);

        JButton btnSubmit = UIComponents.createPrimaryButton("Create Account");
        btnSubmit.addActionListener(e -> {
            if (txtU.getText().trim().isEmpty() || txtP.getPassword().length == 0 || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Username, password, and name are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User u = new User();
            u.setUsername(txtU.getText().trim());
            u.setFullName(txtName.getText().trim());
            u.setRole((Role) cmbRole.getSelectedItem());
            u.setEmail(txtEmail.getText().trim());
            u.setPhone(txtPhone.getText().trim());
            u.setActive(true);

            try {
                int id = userDAO.createUser(u, new String(txtP.getPassword()));
                if (id > 0) {
                    AuditLogDAO.log(id, u.getUsername(), "ADMIN", "USER_PROVISIONED", "USER",
                            String.valueOf(id), "Created new " + u.getRole() + " account", AuditLog.Severity.INFO);
                    loadUsers();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "User created successfully with ID: " + id);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error creating user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // 9. SECURITY & AUDIT LOGS
    // =========================================================================

    private JPanel createAuditLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Severity:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(UITheme.TEXT_PRIMARY);

        cmbSeverity = new JComboBox<>(new String[]{"ALL", "SECURITY_ALERT", "WARNING", "INFO", "ERROR"});
        cmbSeverity.addActionListener(e -> loadAuditLogs());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Logs");
        btnRefresh.addActionListener(e -> loadAuditLogs());

        JButton btnTestExploit = UIComponents.createDangerButton("⚡ Test SQL Exploit Detection");
        btnTestExploit.setToolTipText("Simulate a blocked SQL injection attack to verify real-time security alerting");
        btnTestExploit.addActionListener(e -> {
            AuditLogDAO.logSecurityAlert("TEST_ATTACKER", "SQL_INJECTION_INTERCEPTED",
                    "Blocked input: ' OR '1'='1'; DROP TABLE users; -- on authentication gateway.");
            loadAuditLogs();
            JOptionPane.showMessageDialog(this,
                    "Simulated malicious payload intercepted and logged to security audit trail!",
                    "Security Event Logged", JOptionPane.WARNING_MESSAGE);
        });

        toolbar.add(lblFilter);
        toolbar.add(cmbSeverity);
        toolbar.add(btnRefresh);
        toolbar.add(btnTestExploit);

        // Audit Table
        String[] cols = {"ID", "Timestamp", "User", "Role", "Action", "Target", "Severity", "Details"};
        auditTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIComponents.createStyledTable(auditTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(320);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadAuditLogs() {
        if (auditTableModel == null) return;
        auditTableModel.setRowCount(0);
        String severity = (cmbSeverity != null) ? (String) cmbSeverity.getSelectedItem() : "ALL";
        List<AuditLog> logs = auditDAO.getRecentLogs(100, severity);
        for (AuditLog l : logs) {
            auditTableModel.addRow(new Object[]{
                    l.getId(),
                    l.getTimestamp(),
                    l.getUsername(),
                    l.getRole(),
                    l.getAction(),
                    l.getEntityType() != null ? l.getEntityType() : "-",
                    l.getSeverity().name(),
                    l.getDetails()
            });
        }
    }

    // =========================================================================
    // 10. SYSTEM & DATABASE STATUS
    // =========================================================================

    private JPanel createSystemStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        boolean dbOk = DatabaseConnection.testConnection();

        panel.add(UIComponents.createStatCard("Database Engine", "MySQL 8 / 9", "Port 3306 | utf8mb4", UITheme.PRIMARY));
        panel.add(UIComponents.createStatCard("Connection Status", dbOk ? "ONLINE" : "OFFLINE", dbOk ? "Active and healthy" : "Check credentials", dbOk ? UITheme.SUCCESS : UITheme.DANGER));
        panel.add(UIComponents.createStatCard("Security Filter", "ACTIVE", "Swing DocumentFilter + Parameterized Queries", UITheme.SUCCESS));
        panel.add(UIComponents.createStatCard("Architecture", "Java Swing + FlatLaf", "Strictly Native Desktop UI", UITheme.INFO));

        return panel;
    }
}
