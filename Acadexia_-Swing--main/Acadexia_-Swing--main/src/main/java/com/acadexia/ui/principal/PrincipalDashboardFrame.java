package com.acadexia.ui.principal;

import com.acadexia.dao.*;
import com.acadexia.dao.MarkDAO.StudentMarksEntryRow;
import com.acadexia.model.*;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;
import com.acadexia.ui.principal.charts.ModernBarChart;
import com.acadexia.ui.principal.charts.ModernBarChart.BarItem;
import com.acadexia.ui.principal.charts.ModernDonutChart;
import com.acadexia.ui.principal.charts.ModernDonutChart.Segment;
import com.acadexia.util.PdfReportGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Upgraded Executive Command Center for the Office of the Principal.
 * Provides complete academic governance across:
 * - Executive Overview & Real-time KPI Analytics
 * - College-wide Academic Analytics & Student Transcripts
 * - Result Approval & Multi-Tier Examination Publication
 * - Faculty & Department Head (HOD) Governance & Monitoring
 * - Tier-3 Duty Leave Sanction & Validation
 * - Student Grievance Redressal Oversight & Adjudication
 * - Executive Reports Center with Official PDF Export
 * - Institutional Announcements & Policy Circulars
 * - Master Academic Calendar & Term Milestones
 * - Campus-wide Broadcast Notifications
 * - Institutional Security Audit Trail & Activity Logs
 */
public class PrincipalDashboardFrame extends JFrame {

    // DAOs
    private final DutyLeaveDAO dutyLeaveDAO = new DutyLeaveDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final GrievanceDAO grievanceDAO = new GrievanceDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final MarkDAO markDAO = new MarkDAO();
    private final ResultPublicationDAO publicationDAO = new ResultPublicationDAO();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final AcademicCalendarDAO calendarDAO = new AcademicCalendarDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    // Navigation & Layout
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCardsPanel = new JPanel(contentCardLayout);
    private final Map<String, JButton> sidebarButtons = new LinkedHashMap<>();
    private JLabel lblSectionTitle;
    private JLabel lblSectionSubtitle;
    private JLabel lblPendingLeaveBadge;
    private JLabel lblPendingGrievanceBadge;
    private JLabel lblPendingResultBadge;

    // Cached Data
    private List<Student> cachedStudents = new ArrayList<>();
    private List<Faculty> cachedFaculty = new ArrayList<>();
    private List<Department> cachedDepartments = new ArrayList<>();
    private List<ClassBatch> cachedClasses = new ArrayList<>();
    private List<DutyLeave> cachedLeaves = new ArrayList<>();
    private List<Grievance> cachedGrievances = new ArrayList<>();
    private List<ResultPublication> cachedPublications = new ArrayList<>();
    private List<Announcement> cachedAnnouncements = new ArrayList<>();
    private List<AcademicEvent> cachedCalendarEvents = new ArrayList<>();
    private List<Notification> cachedNotifications = new ArrayList<>();
    private List<AuditLog> cachedAuditLogs = new ArrayList<>();

    // Table Models
    private DefaultTableModel analyticsTableModel;
    private DefaultTableModel publicationTableModel;
    private DefaultTableModel deptTableModel;
    private DefaultTableModel facultyTableModel;
    private DefaultTableModel leaveTableModel;
    private DefaultTableModel grievanceTableModel;
    private DefaultTableModel announcementTableModel;
    private DefaultTableModel calendarTableModel;
    private DefaultTableModel notificationTableModel;
    private DefaultTableModel logTableModel;
    private DefaultTableModel reportPreviewTableModel;

    // Charts
    private ModernBarChart overviewDeptBarChart;
    private ModernDonutChart overviewStandingDonutChart;
    private ModernBarChart analyticsCgpaBarChart;
    private ModernDonutChart analyticsAttendanceDonutChart;

    // Overview Stat Value Labels
    private JLabel lblStatEnrollment;
    private JLabel lblStatFaculty;
    private JLabel lblStatDepts;
    private JLabel lblStatPendingLeaves;
    private JLabel lblStatPendingGrievances;
    private JLabel lblStatPendingResults;
    private JLabel lblStatPublishedResults;
    private JLabel lblStatActiveCirculars;

    // Filters
    private JComboBox<String> cmbAnalyticsDept;
    private JComboBox<String> cmbAnalyticsClass;
    private JComboBox<String> cmbAnalyticsRisk;
    private JTextField txtAnalyticsSearch;

    private JComboBox<String> cmbPubStatus;
    private JComboBox<String> cmbPubExamType;
    private JTextField txtPubSearch;

    private JComboBox<String> cmbFacDept;
    private JTextField txtFacSearch;

    private JComboBox<String> cmbLeaveStatus;
    private JTextField txtLeaveSearch;

    private JComboBox<String> cmbGrievanceCat;
    private JComboBox<String> cmbGrievanceStatus;
    private JTextField txtGrievanceSearch;

    private JComboBox<String> cmbAnnAudience;
    private JComboBox<String> cmbAnnCategory;
    private JTextField txtAnnSearch;

    private JComboBox<String> cmbCalType;
    private JTextField txtCalSearch;

    private JComboBox<String> cmbNotifAudience;
    private JComboBox<String> cmbNotifPriority;
    private JTextField txtNotifSearch;

    private JComboBox<String> cmbLogSeverity;
    private JTextField txtLogSearch;

    // Report generation controls
    private JComboBox<String> cmbReportType;
    private JComboBox<ClassBatch> cmbReportClass;
    private JComboBox<Subject> cmbReportSubject;
    private JComboBox<Mark.ExamType> cmbReportExamType;
    private File lastGeneratedPdfFile;
    private JLabel lblReportStatus;

    public PrincipalDashboardFrame() {
        // Enforce role-based security
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || (currentUser.getRole() != Role.PRINCIPAL && currentUser.getRole() != Role.ADMIN)) {
            JOptionPane.showMessageDialog(null,
                    "Access Denied: The Office of the Principal requires executive leadership credentials.",
                    "Security Access Authorization", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                new LandingFrame().setVisible(true);
                dispose();
            });
            return;
        }

        setTitle("Acadexia — Executive Office of the Principal (Higher Education Governance)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 830);
        setMinimumSize(new Dimension(1160, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
        loadAllDataAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // West: Professional Dark Sidebar
        add(createSidebarPanel(), BorderLayout.WEST);

        // Center: Header Bar + Content Panels CardLayout
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(UITheme.BG_DARK);

        rightContainer.add(createTopBar(), BorderLayout.NORTH);

        // Assemble Content Cards (11 Modules)
        contentCardsPanel.setBackground(UITheme.BG_DARK);
        contentCardsPanel.add(createOverviewPanel(), "OVERVIEW");
        contentCardsPanel.add(createAcademicAnalyticsPanel(), "ANALYTICS");
        contentCardsPanel.add(createResultApprovalPanel(), "RESULTS");
        contentCardsPanel.add(createFacultyMonitoringPanel(), "FACULTY");
        contentCardsPanel.add(createDutyLeavePanel(), "DUTY_LEAVE");
        contentCardsPanel.add(createGrievancePanel(), "GRIEVANCE");
        contentCardsPanel.add(createReportsPanel(), "REPORTS");
        contentCardsPanel.add(createAnnouncementsPanel(), "ANNOUNCEMENTS");
        contentCardsPanel.add(createCalendarPanel(), "CALENDAR");
        contentCardsPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentCardsPanel.add(createActivityLogsPanel(), "LOGS");

        rightContainer.add(contentCardsPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // SIDEBAR NAVIGATION
    // =========================================================================

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.SIDEBAR_BORDER));

        // Sidebar Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(22, 18, 16, 18));

        JLabel lblBrand = new JLabel("ACADEXIA ERP");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setForeground(UITheme.CYAN_ACCENT);

        JLabel lblRole = new JLabel("Office of the Principal");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblBadge = UIComponents.createBadge("Executive Academic Head", new Color(18, 38, 72), UITheme.CYAN_ACCENT);
        lblBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(lblBrand);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(lblRole);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(lblBadge);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        // Sidebar Nav Buttons List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(8, 10, 8, 10));

        addSidebarItem(navList, "OVERVIEW", "👑", "Executive Overview", null);
        addSidebarItem(navList, "ANALYTICS", "📊", "Academic Analytics", null);
        lblPendingResultBadge = createPillBadge("0");
        addSidebarItem(navList, "RESULTS", "📈", "Result Approval", lblPendingResultBadge);
        addSidebarItem(navList, "FACULTY", "👨‍🏫", "Faculty & HODs", null);
        lblPendingLeaveBadge = createPillBadge("0");
        addSidebarItem(navList, "DUTY_LEAVE", "📋", "Duty Leave Sanctions", lblPendingLeaveBadge);
        lblPendingGrievanceBadge = createPillBadge("0");
        addSidebarItem(navList, "GRIEVANCE", "⚖️", "Grievance Redressal", lblPendingGrievanceBadge);
        addSidebarItem(navList, "REPORTS", "📑", "Reports & PDF Export", null);
        addSidebarItem(navList, "ANNOUNCEMENTS", "📢", "Circulars & Notices", null);
        addSidebarItem(navList, "CALENDAR", "📅", "Academic Calendar", null);
        addSidebarItem(navList, "NOTIFICATIONS", "🔔", "Broadcast Alerts", null);
        addSidebarItem(navList, "LOGS", "🛡️", "Security & Activity Logs", null);

        JScrollPane scrollNav = new JScrollPane(navList);
        scrollNav.setBorder(null);
        scrollNav.setOpaque(false);
        scrollNav.getViewport().setOpaque(false);
        scrollNav.getVerticalScrollBar().setUnitIncrement(14);
        sidebar.add(scrollNav, BorderLayout.CENTER);

        // Sidebar Footer User Profile Card
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 16, 16, 16));

        User user = SessionManager.getInstance().getCurrentUser();
        String displayName = user != null ? user.getFullName() : "Dr. K. S. Menon";

        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setOpaque(false);

        JLabel lblU = new JLabel(displayName);
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblU.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Principal & Professor");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        userBox.add(lblU);
        userBox.add(lblSubtitle);

        JButton btnLogout = UIComponents.createDangerButton("🚪 Exit");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you wish to log out from the Principal Portal?",
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

    private void addSidebarItem(JPanel container, String cardKey, String icon, String title, JLabel badge) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(245, 42));
        row.setPreferredSize(new Dimension(240, 40));

        UIComponents.SidebarNavButton btn = new UIComponents.SidebarNavButton(icon, title);
        btn.addActionListener(e -> selectView(cardKey, title));
        sidebarButtons.put(cardKey, btn);

        row.add(btn, BorderLayout.CENTER);
        if (badge != null) {
            row.add(badge, BorderLayout.EAST);
        }

        container.add(row);
        container.add(Box.createVerticalStrut(3));
    }

    private JLabel createPillBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CYAN_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(Color.BLACK);
        badge.setPreferredSize(new Dimension(24, 18));
        badge.setVisible(false);
        return badge;
    }

    private void selectView(String cardKey, String title) {
        contentCardLayout.show(contentCardsPanel, cardKey);
        lblSectionTitle.setText(title.toUpperCase());
        lblSectionSubtitle.setText("Institutional Governance > " + title);

        // Update active sidebar styling with glowing highlights
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

        lblSectionSubtitle = new JLabel("Institutional Governance > Executive Overview");
        lblSectionSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSectionSubtitle.setForeground(UITheme.TEXT_MUTED);

        left.add(lblSectionTitle);
        left.add(lblSectionSubtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel lblSession = UIComponents.createBadge("Academic Session 2026-27 | Odd Sem", new Color(30, 41, 59), new Color(148, 163, 184));
        right.add(lblSession);

        JButton btnQuickReport = UIComponents.createPrimaryButton("📑 Export Dossier");
        btnQuickReport.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnQuickReport.addActionListener(e -> selectView("REPORTS", "Reports & PDF Export"));
        right.add(btnQuickReport);

        JButton btnRefreshAll = UIComponents.createSecondaryButton("🔄 Refresh Data");
        btnRefreshAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefreshAll.addActionListener(e -> loadAllDataAsync());
        right.add(btnRefreshAll);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    // =========================================================================
    // 1. EXECUTIVE OVERVIEW PANEL
    // =========================================================================

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top 8 Stat KPI Cards in 2x4 grid
        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        kpiGrid.setOpaque(false);

        lblStatEnrollment = new JLabel("0");
        lblStatFaculty = new JLabel("0");
        lblStatDepts = new JLabel("0");
        lblStatPendingLeaves = new JLabel("0");
        lblStatPendingGrievances = new JLabel("0");
        lblStatPendingResults = new JLabel("0");
        lblStatPublishedResults = new JLabel("0");
        lblStatActiveCirculars = new JLabel("0");

        kpiGrid.add(createModernKpiCard("Total Students", lblStatEnrollment, "Enrolled Across All Batches", UITheme.PRIMARY));
        kpiGrid.add(createModernKpiCard("Faculty Strength", lblStatFaculty, "Professors & Lecturers", UITheme.INFO));
        kpiGrid.add(createModernKpiCard("Academic Depts", lblStatDepts, "Active Engineering Wings", UITheme.SUCCESS));
        kpiGrid.add(createModernKpiCard("Pending Duty Leaves", lblStatPendingLeaves, "Awaiting Tier-3 Sanction", UITheme.WARNING));
        kpiGrid.add(createModernKpiCard("Active Grievances", lblStatPendingGrievances, "Open Student Cases", UITheme.DANGER));
        kpiGrid.add(createModernKpiCard("Pending Results", lblStatPendingResults, "Verified Awaiting Publication", new Color(168, 85, 247)));
        kpiGrid.add(createModernKpiCard("Published Results", lblStatPublishedResults, "Officially Released Exams", new Color(16, 185, 129)));
        kpiGrid.add(createModernKpiCard("Active Circulars", lblStatActiveCirculars, "Institutional Directives", new Color(245, 158, 11)));

        panel.add(kpiGrid, BorderLayout.NORTH);

        // Center: Dual Charts
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 14, 14));
        chartsContainer.setOpaque(false);

        overviewDeptBarChart = new ModernBarChart("Department Enrollment & Performance");
        overviewStandingDonutChart = new ModernDonutChart("Institutional Academic Standing Distribution", "Students");

        chartsContainer.add(overviewDeptBarChart);
        chartsContainer.add(overviewStandingDonutChart);

        // Bottom: Institutional Directive Card + Quick Actions
        JPanel bottomContainer = new JPanel(new GridLayout(1, 2, 14, 14));
        bottomContainer.setOpaque(false);

        JPanel directiveCard = UIComponents.createCardPanel();
        directiveCard.setLayout(new BoxLayout(directiveCard, BoxLayout.Y_AXIS));

        JLabel lblDTitle = new JLabel("Principal Executive Directives & Academic Health");
        lblDTitle.setFont(UITheme.FONT_TITLE);
        lblDTitle.setForeground(new Color(245, 158, 11));

        JLabel lblDDesc = new JLabel("<html>" +
                "• <b>Tier-3 Statutory Duty Leave Sanctions:</b> Review and issue executive decisions on extracurricular and sports leaves.<br>" +
                "• <b>Multi-Tier Examination Publication:</b> Verify academic marksheets entered by faculty and publish results.<br>" +
                "• <b>Departmental Headship Governance:</b> Appoint or reassign Faculty HODs to ensure smooth administration.<br>" +
                "• <b>Grievance Redressal Adjudication:</b> Review student submissions and provide institutional resolutions.<br>" +
                "• <b>Official PDF Reporting:</b> Export university-grade merit transcripts, attendance warnings, and audit logs.</html>");
        lblDDesc.setFont(UITheme.FONT_REGULAR);
        lblDDesc.setForeground(UITheme.TEXT_MUTED);

        directiveCard.add(lblDTitle);
        directiveCard.add(Box.createVerticalStrut(10));
        directiveCard.add(lblDDesc);

        JPanel quickActionsCard = UIComponents.createCardPanel();
        quickActionsCard.setLayout(new GridLayout(3, 2, 10, 10));

        JButton btnAct1 = UIComponents.createPrimaryButton("📋 Sanction Duty Leaves");
        btnAct1.addActionListener(e -> selectView("DUTY_LEAVE", "Duty Leave Sanctions"));

        JButton btnAct2 = UIComponents.createSuccessButton("📈 Publish Exam Results");
        btnAct2.addActionListener(e -> selectView("RESULTS", "Result Approval"));

        JButton btnAct3 = UIComponents.createSecondaryButton("⚖️ Resolve Grievances");
        btnAct3.addActionListener(e -> selectView("GRIEVANCE", "Grievance Redressal"));

        JButton btnAct4 = UIComponents.createSecondaryButton("📢 Issue New Notice");
        btnAct4.addActionListener(e -> {
            selectView("ANNOUNCEMENTS", "Circulars & Notices");
            showCreateAnnouncementDialog();
        });

        JButton btnAct5 = UIComponents.createSecondaryButton("📅 Schedule Event");
        btnAct5.addActionListener(e -> {
            selectView("CALENDAR", "Academic Calendar");
            showCreateCalendarEventDialog();
        });

        JButton btnAct6 = UIComponents.createPrimaryButton("📑 Export PDF Dossier");
        btnAct6.addActionListener(e -> selectView("REPORTS", "Reports & PDF Export"));

        quickActionsCard.add(btnAct1);
        quickActionsCard.add(btnAct2);
        quickActionsCard.add(btnAct3);
        quickActionsCard.add(btnAct4);
        quickActionsCard.add(btnAct5);
        quickActionsCard.add(btnAct6);

        bottomContainer.add(directiveCard);
        bottomContainer.add(quickActionsCard);

        JPanel centerScrollPanel = new JPanel(new BorderLayout(14, 14));
        centerScrollPanel.setOpaque(false);
        centerScrollPanel.add(chartsContainer, BorderLayout.CENTER);
        centerScrollPanel.add(bottomContainer, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(centerScrollPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createModernKpiCard(String title, JLabel lblVal, String subtitle, Color accentColor) {
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(6, 4));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(accentColor != null ? accentColor : UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(UITheme.TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. ACADEMIC ANALYTICS PANEL
    // =========================================================================

    private JPanel createAcademicAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Filters Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbAnalyticsDept = new JComboBox<>(new String[]{"All Departments"});
        cmbAnalyticsDept.addActionListener(e -> filterAnalyticsTable());

        cmbAnalyticsClass = new JComboBox<>(new String[]{"All Classes"});
        cmbAnalyticsClass.addActionListener(e -> filterAnalyticsTable());

        cmbAnalyticsRisk = new JComboBox<>(new String[]{
                "All Students",
                "Distinction (CGPA >= 8.5)",
                "First Class (CGPA 7.0 - 8.49)",
                "Academic Warning (CGPA < 5.0)",
                "Attendance Shortage (< 75%)"
        });
        cmbAnalyticsRisk.addActionListener(e -> filterAnalyticsTable());

        txtAnalyticsSearch = new JTextField(12);
        txtAnalyticsSearch.putClientProperty("JTextField.placeholderText", "Search student / reg no...");
        txtAnalyticsSearch.addActionListener(e -> filterAnalyticsTable());

        JButton btnApplyFilter = UIComponents.createSecondaryButton("🔍 Filter");
        btnApplyFilter.addActionListener(e -> filterAnalyticsTable());

        JButton btnViewTranscript = UIComponents.createPrimaryButton("📄 View Transcript");
        btnViewTranscript.addActionListener(e -> showStudentTranscriptDialog());

        JButton btnExportPdf = UIComponents.createSuccessButton("📑 Export Analytics PDF");
        btnExportPdf.addActionListener(e -> exportAcademicAnalyticsPdf());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadAcademicAnalytics());

        toolbar.add(new JLabel("Dept:"));
        toolbar.add(cmbAnalyticsDept);
        toolbar.add(new JLabel("Class:"));
        toolbar.add(cmbAnalyticsClass);
        toolbar.add(new JLabel("Standing:"));
        toolbar.add(cmbAnalyticsRisk);
        toolbar.add(txtAnalyticsSearch);
        toolbar.add(btnApplyFilter);
        toolbar.add(btnViewTranscript);
        toolbar.add(btnExportPdf);
        toolbar.add(btnRefresh);

        // Center split: Top charts (Height 170) + Bottom Table
        JPanel chartsBox = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsBox.setOpaque(false);
        chartsBox.setPreferredSize(new Dimension(0, 175));

        analyticsCgpaBarChart = new ModernBarChart("Department Average CGPA Standing");
        analyticsAttendanceDonutChart = new ModernDonutChart("Attendance Health Ratio", "Students");
        chartsBox.add(analyticsCgpaBarChart);
        chartsBox.add(analyticsAttendanceDonutChart);

        String[] cols = {"Register No", "Roll", "Student Name", "Class", "Department", "Sem", "Attendance %", "SGPA", "CGPA", "Academic Standing"};
        analyticsTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(analyticsTableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showStudentTranscriptDialog();
                }
            }
        });

        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(chartsBox, BorderLayout.NORTH);
        centerContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    private void filterAnalyticsTable() {
        if (cachedStudents == null) return;
        String selDept = (String) cmbAnalyticsDept.getSelectedItem();
        String selClass = (String) cmbAnalyticsClass.getSelectedItem();
        String selStanding = (String) cmbAnalyticsRisk.getSelectedItem();
        String query = txtAnalyticsSearch.getText().trim().toLowerCase();

        analyticsTableModel.setRowCount(0);
        for (Student s : cachedStudents) {
            if (selDept != null && !selDept.equals("All Departments") && !selDept.equalsIgnoreCase(s.getDepartmentName())) {
                continue;
            }
            if (selClass != null && !selClass.equals("All Classes") && !selClass.equalsIgnoreCase(s.getClassName())) {
                continue;
            }
            if (selStanding != null) {
                if (selStanding.startsWith("Distinction") && s.getCurrentCgpa() < 8.5) continue;
                if (selStanding.startsWith("First Class") && (s.getCurrentCgpa() < 7.0 || s.getCurrentCgpa() >= 8.5)) continue;
                if (selStanding.startsWith("Academic Warning") && s.getCurrentCgpa() >= 5.0) continue;
                if (selStanding.startsWith("Attendance Shortage") && s.getAttendancePercentage() >= 75.0) continue;
            }
            if (!query.isEmpty()) {
                boolean matchName = s.getFullName() != null && s.getFullName().toLowerCase().contains(query);
                boolean matchReg = s.getRegisterNumber() != null && s.getRegisterNumber().toLowerCase().contains(query);
                if (!matchName && !matchReg) continue;
            }

            String standing = "Good Standing";
            if (s.getCurrentCgpa() >= 8.5) standing = "🌟 Distinction";
            else if (s.getCurrentCgpa() >= 7.0) standing = "✅ First Class";
            else if (s.getCurrentCgpa() < 5.0 || s.getAttendancePercentage() < 75.0) standing = "⚠️ At-Risk / Warning";

            analyticsTableModel.addRow(new Object[]{
                    s.getRegisterNumber(),
                    s.getRollNumber(),
                    s.getFullName(),
                    s.getClassName(),
                    s.getDepartmentName(),
                    s.getSemester(),
                    String.format("%.1f%%", s.getAttendancePercentage()),
                    String.format("%.2f", s.getCurrentSgpa()),
                    String.format("%.2f", s.getCurrentCgpa()),
                    standing
            });
        }
    }

    private void showStudentTranscriptDialog() {
        int row = analyticsTableModel.getRowCount() > 0 ? 0 : -1;
        // Check if user selected row in table
        Component comp = ((JPanel) contentCardsPanel.getComponent(1)).getComponent(1);
        if (comp instanceof JPanel p && p.getComponent(1) instanceof JScrollPane sp) {
            JTable tbl = (JTable) sp.getViewport().getView();
            int sel = tbl.getSelectedRow();
            if (sel >= 0) row = sel;
        }

        if (row < 0 || cachedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to view transcript.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String regNo = (String) analyticsTableModel.getValueAt(row, 0);
        Student s = cachedStudents.stream().filter(st -> regNo.equals(st.getRegisterNumber())).findFirst().orElse(null);
        if (s == null) return;

        JDialog dlg = new JDialog(this, "Academic Dossier & Transcript: " + s.getFullName(), true);
        dlg.setSize(740, 520);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG_DARK);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header info
        JPanel infoBox = UIComponents.createCardPanel();
        infoBox.setLayout(new GridLayout(2, 3, 10, 6));
        infoBox.add(new JLabel("Name: " + s.getFullName()));
        infoBox.add(new JLabel("Reg No: " + s.getRegisterNumber()));
        infoBox.add(new JLabel("Class: " + s.getClassName()));
        infoBox.add(new JLabel("Department: " + s.getDepartmentName()));
        infoBox.add(new JLabel("Attendance: " + String.format("%.1f%%", s.getAttendancePercentage())));
        infoBox.add(new JLabel("CGPA: " + String.format("%.2f", s.getCurrentCgpa())));
        p.add(infoBox, BorderLayout.NORTH);

        // Marks Table
        List<Mark> marks = markDAO.getMarksForStudent(s.getId());
        String[] markCols = {"Subject Code", "Subject Name", "Semester", "Exam Type", "Marks Obtained", "Max Marks", "Percentage", "Grade"};
        DefaultTableModel marksModel = new DefaultTableModel(markCols, 0);
        for (Mark m : marks) {
            double pct = (m.getMarksObtained() / m.getMaxMarks()) * 100.0;
            String grade = pct >= 90 ? "S" : pct >= 80 ? "A" : pct >= 70 ? "B" : pct >= 60 ? "C" : pct >= 50 ? "D" : pct >= 40 ? "E" : "F";
            marksModel.addRow(new Object[]{
                    m.getSubjectCode(),
                    m.getSubjectName(),
                    m.getSemester(),
                    m.getExamType().name(),
                    m.getMarksObtained(),
                    m.getMaxMarks(),
                    String.format("%.1f%%", pct),
                    grade
            });
        }
        JTable markTable = UIComponents.createStyledTable(marksModel);
        p.add(new JScrollPane(markTable), BorderLayout.CENTER);

        JButton btnClose = UIComponents.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.add(btnClose);
        p.add(bot, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void exportAcademicAnalyticsPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Institutional_Academic_Analytics_Dossier.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                PdfReportGenerator.generateInstitutionalPerformanceReport(f, cachedStudents, cachedDepartments);
                lastGeneratedPdfFile = f;
                int opt = JOptionPane.showConfirmDialog(this,
                        "Institutional Analytics Report successfully generated at:\n" + f.getAbsolutePath() + "\n\nWould you like to open it now?",
                        "PDF Generated Successfully", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(f);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating PDF report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 3. RESULT APPROVAL AND PUBLICATION PANEL
    // =========================================================================

    private JPanel createResultApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbPubStatus = new JComboBox<>(new String[]{"All Statuses", "ENTERED", "VERIFIED", "PUBLISHED"});
        cmbPubStatus.addActionListener(e -> filterPublications());

        cmbPubExamType = new JComboBox<>(new String[]{"All Exams", "SERIES_1", "SERIES_2", "ASSIGNMENT", "INTERNAL", "SEMESTER_EXAM"});
        cmbPubExamType.addActionListener(e -> filterPublications());

        txtPubSearch = new JTextField(12);
        txtPubSearch.putClientProperty("JTextField.placeholderText", "Search class / subject...");
        txtPubSearch.addActionListener(e -> filterPublications());

        JButton btnInspectMarks = UIComponents.createPrimaryButton("👁️ Inspect Marks Sheet");
        btnInspectMarks.addActionListener(e -> showInspectMarksDialog());

        JButton btnApprovePublish = UIComponents.createSuccessButton("✅ Sanction & Publish");
        btnApprovePublish.addActionListener(e -> processPublishDecision());

        JButton btnRequestRevision = UIComponents.createDangerButton("↩️ Request Revision");
        btnRequestRevision.addActionListener(e -> processRevisionRequest());

        JButton btnBulkPublish = UIComponents.createPrimaryButton("🚀 Bulk Publish Verified");
        btnBulkPublish.addActionListener(e -> processBulkPublishVerified());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadResultPublications());

        toolbar.add(new JLabel("Status:"));
        toolbar.add(cmbPubStatus);
        toolbar.add(new JLabel("Exam:"));
        toolbar.add(cmbPubExamType);
        toolbar.add(txtPubSearch);
        toolbar.add(btnInspectMarks);
        toolbar.add(btnApprovePublish);
        toolbar.add(btnRequestRevision);
        toolbar.add(btnBulkPublish);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Class", "Subject Code", "Subject Name", "Exam Type", "Year", "Status", "Verified By", "Published By", "Published At", "Remarks"};
        publicationTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(publicationTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterPublications() {
        if (cachedPublications == null) return;
        String statusFilter = (String) cmbPubStatus.getSelectedItem();
        String examFilter = (String) cmbPubExamType.getSelectedItem();
        String query = txtPubSearch.getText().trim().toLowerCase();

        publicationTableModel.setRowCount(0);
        for (ResultPublication rp : cachedPublications) {
            if (statusFilter != null && !statusFilter.equals("All Statuses") && !statusFilter.equalsIgnoreCase(rp.getStatus().name())) {
                continue;
            }
            if (examFilter != null && !examFilter.equals("All Exams") && !examFilter.equalsIgnoreCase(rp.getExamType().name())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchClass = rp.getClassName() != null && rp.getClassName().toLowerCase().contains(query);
                boolean matchSub = rp.getSubjectName() != null && rp.getSubjectName().toLowerCase().contains(query);
                boolean matchCode = rp.getSubjectCode() != null && rp.getSubjectCode().toLowerCase().contains(query);
                if (!matchClass && !matchSub && !matchCode) continue;
            }

            publicationTableModel.addRow(new Object[]{
                    rp.getId(),
                    rp.getClassName(),
                    rp.getSubjectCode(),
                    rp.getSubjectName(),
                    rp.getExamType().name(),
                    rp.getAcademicYear(),
                    rp.getStatus().name(),
                    rp.getVerifiedByName() != null ? rp.getVerifiedByName() : "Pending",
                    rp.getPublishedByName() != null ? rp.getPublishedByName() : "-",
                    rp.getPublishedAt() != null ? rp.getPublishedAt().toString() : "-",
                    rp.getRemarks() != null ? rp.getRemarks() : "-"
            });
        }
    }

    private ResultPublication getSelectedPublication() {
        Component comp = ((JPanel) contentCardsPanel.getComponent(2)).getComponent(1);
        if (comp instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTable tbl) {
            int sel = tbl.getSelectedRow();
            if (sel >= 0) {
                int id = (int) publicationTableModel.getValueAt(sel, 0);
                return cachedPublications.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
            }
        }
        return null;
    }

    private void showInspectMarksDialog() {
        ResultPublication rp = getSelectedPublication();
        if (rp == null) {
            JOptionPane.showMessageDialog(this, "Please select an exam publication batch from the table.", "Select Batch", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<StudentMarksEntryRow> rows = markDAO.getClassSubjectMarks(rp.getClassId(), rp.getSubjectId(), rp.getExamType());

        JDialog dlg = new JDialog(this, "Class Grade Sheet: " + rp.getClassName() + " — " + rp.getSubjectName(), true);
        dlg.setSize(780, 520);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG_DARK);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header info
        JPanel topInfo = UIComponents.createCardPanel();
        topInfo.setLayout(new GridLayout(2, 3, 10, 6));
        topInfo.add(new JLabel("Class: " + rp.getClassName()));
        topInfo.add(new JLabel("Course: " + rp.getSubjectCode() + " - " + rp.getSubjectName()));
        topInfo.add(new JLabel("Exam Type: " + rp.getExamType().name()));
        topInfo.add(new JLabel("Academic Year: " + rp.getAcademicYear()));
        topInfo.add(new JLabel("Status: " + rp.getStatus().name()));
        topInfo.add(new JLabel("Evaluated: " + rows.size() + " students"));
        p.add(topInfo, BorderLayout.NORTH);

        String[] cols = {"Roll", "Register No", "Student Name", "Marks Obtained", "Max Marks", "Percentage", "Grade", "Status"};
        DefaultTableModel mModel = new DefaultTableModel(cols, 0);

        int passCount = 0;
        double sumMarks = 0;
        int evaluatedCount = 0;
        double maxMarks = 50.0;

        for (StudentMarksEntryRow r : rows) {
            if (r.marksObtained() != null) {
                double m = r.marksObtained();
                double mx = r.maxMarks() != null ? r.maxMarks() : 50.0;
                maxMarks = mx;
                double pct = (m / mx) * 100.0;
                sumMarks += m;
                evaluatedCount++;
                boolean pass = pct >= 40.0;
                if (pass) passCount++;
                String grade = pct >= 90 ? "S" : pct >= 80 ? "A" : pct >= 70 ? "B" : pct >= 60 ? "C" : pct >= 50 ? "D" : pct >= 40 ? "E" : "F";

                mModel.addRow(new Object[]{
                        r.rollNumber(),
                        r.registerNumber(),
                        r.fullName(),
                        m,
                        mx,
                        String.format("%.1f%%", pct),
                        grade,
                        pass ? "PASS" : "FAIL"
                });
            } else {
                mModel.addRow(new Object[]{
                        r.rollNumber(),
                        r.registerNumber(),
                        r.fullName(),
                        "ABSENT",
                        50.0,
                        "0.0%",
                        "F",
                        "ABSENT"
                });
            }
        }

        JTable tbl = UIComponents.createStyledTable(mModel);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // Bottom Controls
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);

        double passRate = evaluatedCount > 0 ? ((double) passCount / evaluatedCount) * 100.0 : 0.0;
        double avgMark = evaluatedCount > 0 ? (sumMarks / evaluatedCount) : 0.0;
        JLabel lblSummary = new JLabel(String.format("Class Pass Rate: %.1f%% | Class Average: %.1f / %.1f", passRate, avgMark, maxMarks));
        lblSummary.setFont(UITheme.FONT_BOLD);
        lblSummary.setForeground(UITheme.SUCCESS);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        double finalMax = maxMarks;
        JButton btnExport = UIComponents.createSuccessButton("📄 Export Grade Sheet PDF");
        btnExport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(rp.getClassName().replace(" ", "_") + "_" + rp.getSubjectCode() + "_GradeSheet.pdf"));
            if (chooser.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                try {
                    PdfReportGenerator.generateClassResultReport(chooser.getSelectedFile(), rp.getClassName(),
                            rp.getSubjectCode() + " - " + rp.getSubjectName(), rp.getExamType().name(), rows, finalMax);
                    JOptionPane.showMessageDialog(dlg, "Class grade sheet successfully saved!");
                    Desktop.getDesktop().open(chooser.getSelectedFile());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Error exporting grade sheet: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnClose = UIComponents.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        btnBar.add(btnExport);
        btnBar.add(btnClose);

        bot.add(lblSummary, BorderLayout.WEST);
        bot.add(btnBar, BorderLayout.EAST);
        p.add(bot, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void processPublishDecision() {
        ResultPublication rp = getSelectedPublication();
        if (rp == null) {
            JOptionPane.showMessageDialog(this, "Please select an exam publication from the table to publish.", "Select Exam Batch", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (rp.getStatus() == ResultPublication.PublicationStatus.PUBLISHED) {
            JOptionPane.showMessageDialog(this, "This examination result is already officially published.", "Already Published", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String remarks = JOptionPane.showInputDialog(this,
                "Enter Principal Release Remarks for " + rp.getClassName() + " (" + rp.getSubjectName() + "):",
                "Sanction & Publish Exam Results", JOptionPane.QUESTION_MESSAGE);
        if (remarks == null) return;
        if (remarks.trim().isEmpty()) remarks = "Officially published by the Executive Office of the Principal";

        User u = SessionManager.getInstance().getCurrentUser();
        boolean ok = publicationDAO.publishResults(rp.getClassId(), rp.getSubjectId(), rp.getExamType(), rp.getAcademicYear(), u.getId());
        if (ok) {
            AuditLogDAO.log(u.getId(), u.getUsername(), "PRINCIPAL",
                    "RESULTS_OFFICIALLY_PUBLISHED", "RESULT_PUBLICATION", String.valueOf(rp.getId()),
                    rp.getClassName() + " | " + rp.getSubjectCode() + " | " + rp.getExamType().name() + " | " + remarks,
                    AuditLog.Severity.INFO);

            loadResultPublications();
            JOptionPane.showMessageDialog(this, "Results for " + rp.getClassName() + " - " + rp.getSubjectName() + " have been officially released and published!");
        } else {
            JOptionPane.showMessageDialog(this, "Error publishing results in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processRevisionRequest() {
        ResultPublication rp = getSelectedPublication();
        if (rp == null) {
            JOptionPane.showMessageDialog(this, "Please select an exam publication batch from the table.", "Select Exam Batch", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String notes = JOptionPane.showInputDialog(this,
                "Enter Revision Instructions / Reason to Send Back to Faculty/HOD:",
                "Request Result Revision", JOptionPane.WARNING_MESSAGE);
        if (notes == null || notes.trim().isEmpty()) return;

        User u = SessionManager.getInstance().getCurrentUser();
        boolean ok = publicationDAO.recordOrUpdateStatus(rp.getClassId(), rp.getSubjectId(), rp.getExamType(), rp.getAcademicYear(),
                ResultPublication.PublicationStatus.ENTERED, u.getId(), "Revision Requested by Principal: " + notes);

        if (ok) {
            AuditLogDAO.log(u.getId(), u.getUsername(), "PRINCIPAL",
                    "RESULTS_REVISION_REQUESTED", "RESULT_PUBLICATION", String.valueOf(rp.getId()),
                    rp.getClassName() + " | " + rp.getSubjectCode() + " | Notes: " + notes, AuditLog.Severity.WARNING);
            loadResultPublications();
            JOptionPane.showMessageDialog(this, "Result batch sent back for correction with revision notes.");
        }
    }

    private void processBulkPublishVerified() {
        long verifiedCount = cachedPublications.stream().filter(p -> p.getStatus() == ResultPublication.PublicationStatus.VERIFIED).count();
        if (verifiedCount == 0) {
            JOptionPane.showMessageDialog(this, "No examination result batches currently in 'VERIFIED' status awaiting publication.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to officially sanction and publish all " + verifiedCount + " verified examination batches across all departments?",
                "Confirm Bulk Publication", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            User u = SessionManager.getInstance().getCurrentUser();
            int updated = publicationDAO.bulkPublishVerifiedResults(u.getId(), "Executive bulk publication by Principal");
            if (updated > 0) {
                AuditLogDAO.log(u.getId(), u.getUsername(), "PRINCIPAL",
                        "BULK_RESULTS_PUBLISHED", "RESULT_PUBLICATION", "ALL_VERIFIED",
                        "Bulk published " + updated + " examination batches.", AuditLog.Severity.INFO);
                loadResultPublications();
                JOptionPane.showMessageDialog(this, "Successfully published " + updated + " verified examination batches!");
            }
        }
    }

    // =========================================================================
    // 4. FACULTY / HOD MONITORING PANEL
    // =========================================================================

    private JPanel createFacultyMonitoringPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Subtabs: Tab 1 Department Leadership & HODs, Tab 2 Faculty Roster & Feedback
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(UITheme.FONT_BOLD);

        // Tab 1: Department Governance & HODs
        JPanel deptPanel = new JPanel(new BorderLayout(10, 10));
        deptPanel.setOpaque(false);

        JPanel deptToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        deptToolbar.setOpaque(false);
        JButton btnAssignHod = UIComponents.createPrimaryButton("👔 Appoint / Reassign Department Head (HOD)");
        btnAssignHod.addActionListener(e -> showAssignHodDialog());
        JButton btnRefreshDept = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefreshDept.addActionListener(e -> loadDepartments());
        deptToolbar.add(btnAssignHod);
        deptToolbar.add(btnRefreshDept);

        String[] deptCols = {"Dept ID", "Code", "Department Name", "Current HOD", "HOD Qualification", "Faculty Strength", "Enrolled Students"};
        deptTableModel = new DefaultTableModel(deptCols, 0);
        JTable deptTable = UIComponents.createStyledTable(deptTableModel);

        deptPanel.add(deptToolbar, BorderLayout.NORTH);
        deptPanel.add(new JScrollPane(deptTable), BorderLayout.CENTER);

        // Tab 2: Faculty Roster & Performance Monitor
        JPanel facPanel = new JPanel(new BorderLayout(10, 10));
        facPanel.setOpaque(false);

        JPanel facToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        facToolbar.setOpaque(false);

        cmbFacDept = new JComboBox<>(new String[]{"All Departments"});
        cmbFacDept.addActionListener(e -> filterFacultyTable());

        txtFacSearch = new JTextField(12);
        txtFacSearch.putClientProperty("JTextField.placeholderText", "Search faculty name / ID...");
        txtFacSearch.addActionListener(e -> filterFacultyTable());

        JButton btnViewDossier = UIComponents.createPrimaryButton("🔍 View Faculty Dossier & Feedback");
        btnViewDossier.addActionListener(e -> showFacultyDossierDialog());

        JButton btnExportFacReport = UIComponents.createSuccessButton("📑 Export Faculty Report PDF");
        btnExportFacReport.addActionListener(e -> exportFacultyReportPdf());

        JButton btnRefreshFac = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefreshFac.addActionListener(e -> loadFaculty());

        facToolbar.add(new JLabel("Dept:"));
        facToolbar.add(cmbFacDept);
        facToolbar.add(txtFacSearch);
        facToolbar.add(btnViewDossier);
        facToolbar.add(btnExportFacReport);
        facToolbar.add(btnRefreshFac);

        String[] facCols = {"Faculty ID", "Full Name", "Department", "Designation", "Qualification", "Joining Date", "Is HOD?", "Is CFA Advisor?", "Student Feedback Avg"};
        facultyTableModel = new DefaultTableModel(facCols, 0);
        JTable facTable = UIComponents.createStyledTable(facultyTableModel);

        facPanel.add(facToolbar, BorderLayout.NORTH);
        facPanel.add(new JScrollPane(facTable), BorderLayout.CENTER);

        subTabs.addTab("🏛️ Department Leadership & HOD Appointments", deptPanel);
        subTabs.addTab("👨‍🏫 Faculty Directory & Student Feedback Oversight", facPanel);

        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    private void filterFacultyTable() {
        if (cachedFaculty == null) return;
        String deptFilter = (String) cmbFacDept.getSelectedItem();
        String query = txtFacSearch.getText().trim().toLowerCase();

        facultyTableModel.setRowCount(0);
        for (Faculty f : cachedFaculty) {
            if (deptFilter != null && !deptFilter.equals("All Departments") && !deptFilter.equalsIgnoreCase(f.getDepartmentName())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchName = f.getFullName() != null && f.getFullName().toLowerCase().contains(query);
                boolean matchId = f.getFacultyId() != null && f.getFacultyId().toLowerCase().contains(query);
                if (!matchName && !matchId) continue;
            }

            List<Feedback> fbs = feedbackDAO.getFeedbackForFaculty(f.getId());
            double avgRating = 0.0;
            if (!fbs.isEmpty()) {
                double teach = fbs.stream().mapToInt(Feedback::getRatingTeaching).average().orElse(0.0);
                double punc = fbs.stream().mapToInt(Feedback::getRatingPunctuality).average().orElse(0.0);
                double clar = fbs.stream().mapToInt(Feedback::getRatingClarity).average().orElse(0.0);
                avgRating = (teach + punc + clar) / 3.0;
            }

            facultyTableModel.addRow(new Object[]{
                    f.getFacultyId(),
                    f.getFullName(),
                    f.getDepartmentName(),
                    f.getDesignation(),
                    f.getQualification() != null ? f.getQualification() : "-",
                    f.getJoiningDate() != null ? f.getJoiningDate().toString() : "-",
                    f.isHod() ? "⭐ YES (HOD)" : "No",
                    f.isCfa() ? "📋 YES (CFA)" : "No",
                    avgRating > 0 ? String.format("%.2f / 5.0 (%d reviews)", avgRating, fbs.size()) : "No feedback recorded"
            });
        }
    }

    private void showAssignHodDialog() {
        List<Department> depts = departmentDAO.getAllDepartments();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();

        if (depts.isEmpty() || facultyList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No departments or faculty available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<Department> cmbDept = new JComboBox<>(depts.toArray(new Department[0]));
        JComboBox<Faculty> cmbFac = new JComboBox<>(facultyList.toArray(new Faculty[0]));

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Department:"));
        p.add(cmbDept);
        p.add(new JLabel("Appoint Faculty as Head of Dept:"));
        p.add(cmbFac);

        int result = JOptionPane.showConfirmDialog(this, p, "Appoint / Reassign Department Head (HOD)", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Department selDept = (Department) cmbDept.getSelectedItem();
            Faculty selFac = (Faculty) cmbFac.getSelectedItem();
            if (selDept != null && selFac != null) {
                departmentDAO.updateHod(selDept.getId(), selFac.getId());
                User u = SessionManager.getInstance().getCurrentUser();
                AuditLogDAO.log(u.getId(), u.getUsername(), "PRINCIPAL",
                        "HOD_APPOINTED", "DEPARTMENT", String.valueOf(selDept.getId()),
                        "Appointed " + selFac.getFullName() + " as HOD of " + selDept.getName(), AuditLog.Severity.INFO);

                loadDepartments();
                loadFaculty();
                JOptionPane.showMessageDialog(this, "Successfully appointed " + selFac.getFullName() + " as Head of " + selDept.getName() + "!");
            }
        }
    }

    private void showFacultyDossierDialog() {
        int row = facultyTableModel.getRowCount() > 0 ? 0 : -1;
        // Try getting selected row in faculty table
        Component comp = ((JPanel) contentCardsPanel.getComponent(3)).getComponent(0);
        if (comp instanceof JTabbedPane tp && tp.getComponent(1) instanceof JPanel p && p.getComponent(1) instanceof JScrollPane sp) {
            JTable tbl = (JTable) sp.getViewport().getView();
            int sel = tbl.getSelectedRow();
            if (sel >= 0) row = sel;
        }

        if (row < 0 || cachedFaculty.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String facId = (String) facultyTableModel.getValueAt(row, 0);
        Faculty f = cachedFaculty.stream().filter(fac -> facId.equals(fac.getFacultyId())).findFirst().orElse(null);
        if (f == null) return;

        JDialog dlg = new JDialog(this, "Faculty Dossier & Performance Profile: " + f.getFullName(), true);
        dlg.setSize(740, 520);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG_DARK);

        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Profile summary card
        JPanel profileBox = UIComponents.createCardPanel();
        profileBox.setLayout(new GridLayout(2, 3, 10, 6));
        profileBox.add(new JLabel("Faculty ID: " + f.getFacultyId()));
        profileBox.add(new JLabel("Name: " + f.getFullName()));
        profileBox.add(new JLabel("Designation: " + f.getDesignation()));
        profileBox.add(new JLabel("Department: " + f.getDepartmentName()));
        profileBox.add(new JLabel("Qualification: " + (f.getQualification() != null ? f.getQualification() : "-")));
        profileBox.add(new JLabel("Role: " + (f.isHod() ? "HOD | " : "") + (f.isCfa() ? "CFA Advisor" : "Faculty Member")));
        p.add(profileBox, BorderLayout.NORTH);

        // Center: Feedback list & Assigned Courses
        List<Feedback> fbs = feedbackDAO.getFeedbackForFaculty(f.getId());
        String[] fbCols = {"Subject Code", "Subject Name", "Teaching (1-5)", "Punctuality (1-5)", "Clarity (1-5)", "Student Comments", "Date"};
        DefaultTableModel fbModel = new DefaultTableModel(fbCols, 0);

        for (Feedback fb : fbs) {
            fbModel.addRow(new Object[]{
                    fb.getSubjectCode(),
                    fb.getSubjectName(),
                    fb.getRatingTeaching() + " / 5",
                    fb.getRatingPunctuality() + " / 5",
                    fb.getRatingClarity() + " / 5",
                    fb.getComments() != null ? fb.getComments() : "-",
                    fb.getCreatedAt() != null ? fb.getCreatedAt().toString() : "-"
            });
        }
        JTable fbTable = UIComponents.createStyledTable(fbModel);
        p.add(new JScrollPane(fbTable), BorderLayout.CENTER);

        JButton btnClose = UIComponents.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.add(btnClose);
        p.add(bot, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void exportFacultyReportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Institutional_Faculty_Governance_Report.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PdfReportGenerator.generateFacultyWorkloadReport(chooser.getSelectedFile(), cachedFaculty);
                lastGeneratedPdfFile = chooser.getSelectedFile();
                JOptionPane.showMessageDialog(this, "Faculty report successfully generated!");
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating faculty report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 5. TIER-3 DUTY LEAVE SANCTIONS PANEL
    // =========================================================================

    private JPanel createDutyLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbLeaveStatus = new JComboBox<>(new String[]{"Pending Principal Sanction", "Approved", "Rejected", "All Applications"});
        cmbLeaveStatus.addActionListener(e -> filterDutyLeaves());

        txtLeaveSearch = new JTextField(12);
        txtLeaveSearch.putClientProperty("JTextField.placeholderText", "Search student / reg no...");
        txtLeaveSearch.addActionListener(e -> filterDutyLeaves());

        JButton btnApprove = UIComponents.createSuccessButton("✅ Final Sanction (Approve)");
        btnApprove.addActionListener(e -> processDutyLeaveDecision(true));

        JButton btnReject = UIComponents.createDangerButton("❌ Deny / Reject");
        btnReject.addActionListener(e -> processDutyLeaveDecision(false));

        JButton btnViewProof = UIComponents.createPrimaryButton("📄 View Proof Details");
        btnViewProof.addActionListener(e -> showDutyLeaveProofDialog());

        JButton btnExportPdf = UIComponents.createSecondaryButton("📑 Export Sanctions PDF");
        btnExportPdf.addActionListener(e -> exportDutyLeavesPdf());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadDutyLeaves());

        toolbar.add(new JLabel("Filter:"));
        toolbar.add(cmbLeaveStatus);
        toolbar.add(txtLeaveSearch);
        toolbar.add(btnApprove);
        toolbar.add(btnReject);
        toolbar.add(btnViewProof);
        toolbar.add(btnExportPdf);
        toolbar.add(btnRefresh);

        String[] cols = {"App ID", "Student Name", "Register No", "Class", "Department", "Days", "Dates", "Reason", "Proof Details", "CFA Endorse", "HOD Endorse", "Status"};
        leaveTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(leaveTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterDutyLeaves() {
        if (cachedLeaves == null) return;
        String filter = (String) cmbLeaveStatus.getSelectedItem();
        String query = txtLeaveSearch.getText().trim().toLowerCase();

        leaveTableModel.setRowCount(0);
        for (DutyLeave dl : cachedLeaves) {
            if ("Pending Principal Sanction".equals(filter)) {
                if (dl.getStatus() != DutyLeave.Status.ENDORSED_BY_HOD) continue;
            } else if ("Approved".equals(filter)) {
                if (dl.getStatus() != DutyLeave.Status.APPROVED) continue;
            } else if ("Rejected".equals(filter)) {
                if (dl.getStatus() != DutyLeave.Status.REJECTED && dl.getStatus() != DutyLeave.Status.REJECTED_BY_HOD && dl.getStatus() != DutyLeave.Status.REJECTED_BY_ADVISOR) continue;
            }

            if (!query.isEmpty()) {
                boolean matchName = dl.getStudentName() != null && dl.getStudentName().toLowerCase().contains(query);
                boolean matchReg = dl.getRegisterNumber() != null && dl.getRegisterNumber().toLowerCase().contains(query);
                if (!matchName && !matchReg) continue;
            }

            leaveTableModel.addRow(new Object[]{
                    dl.getId(),
                    dl.getStudentName(),
                    dl.getRegisterNumber(),
                    dl.getClassName(),
                    dl.getDepartmentName(),
                    dl.getTotalDays(),
                    dl.getStartDate() + " to " + dl.getEndDate(),
                    dl.getReason(),
                    dl.getProofDetails() != null ? dl.getProofDetails() : "-",
                    dl.getAdvisorRemarks() != null ? dl.getAdvisorRemarks() : "-",
                    dl.getHodRemarks() != null ? dl.getHodRemarks() : "-",
                    dl.getStatus().getLabel()
            });
        }
    }

    private DutyLeave getSelectedDutyLeave() {
        Component comp = ((JPanel) contentCardsPanel.getComponent(4)).getComponent(1);
        if (comp instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTable tbl) {
            int sel = tbl.getSelectedRow();
            if (sel >= 0) {
                int id = (int) leaveTableModel.getValueAt(sel, 0);
                return cachedLeaves.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
            }
        }
        return null;
    }

    private void processDutyLeaveDecision(boolean approve) {
        DutyLeave dl = getSelectedDutyLeave();
        if (dl == null) {
            JOptionPane.showMessageDialog(this, "Please select a duty leave application from the table.", "Select Application", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String remarks = JOptionPane.showInputDialog(this,
                "Enter Principal Official " + (approve ? "Approval Sanction" : "Rejection Reason") + " Remarks:",
                approve ? "Sanction Duty Leave Application" : "Deny Duty Leave Application",
                JOptionPane.QUESTION_MESSAGE);
        if (remarks == null) return;
        if (remarks.trim().isEmpty()) remarks = approve ? "Sanctioned by Principal" : "Denied by Principal";

        User u = SessionManager.getInstance().getCurrentUser();
        boolean ok = dutyLeaveDAO.updatePrincipalDecision(dl.getId(), approve, remarks, u.getUsername());
        if (ok) {
            loadDutyLeaves();
            JOptionPane.showMessageDialog(this, "Duty leave application #" + dl.getId() + " successfully " + (approve ? "SANCTIONED & APPROVED!" : "REJECTED."));
        } else {
            JOptionPane.showMessageDialog(this, "Error updating application in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDutyLeaveProofDialog() {
        DutyLeave dl = getSelectedDutyLeave();
        if (dl == null) {
            JOptionPane.showMessageDialog(this, "Please select an application from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String msg = String.format("<html><b>Application ID:</b> %d<br>" +
                "<b>Student:</b> %s (%s)<br>" +
                "<b>Class:</b> %s | <b>Department:</b> %s<br>" +
                "<b>Duration:</b> %d Days (%s to %s)<br><br>" +
                "<b>Reason:</b><br>%s<br><br>" +
                "<b>Supporting Proof & Certificates:</b><br>%s<br><br>" +
                "<b>CFA Advisor Recommendation:</b><br>%s<br><br>" +
                "<b>HOD Endorsement:</b><br>%s<br><br>" +
                "<b>Current Status:</b> %s</html>",
                dl.getId(), dl.getStudentName(), dl.getRegisterNumber(), dl.getClassName(), dl.getDepartmentName(),
                dl.getTotalDays(), dl.getStartDate(), dl.getEndDate(), dl.getReason(),
                dl.getProofDetails() != null ? dl.getProofDetails() : "None attached",
                dl.getAdvisorRemarks() != null ? dl.getAdvisorRemarks() : "Pending",
                dl.getHodRemarks() != null ? dl.getHodRemarks() : "Pending",
                dl.getStatus().getLabel());

        JOptionPane.showMessageDialog(this, msg, "Duty Leave Dossier #" + dl.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportDutyLeavesPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Institutional_Duty_Leave_Sanctions_Register.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PdfReportGenerator.generateDutyLeaveAuditReport(chooser.getSelectedFile(), cachedLeaves);
                lastGeneratedPdfFile = chooser.getSelectedFile();
                JOptionPane.showMessageDialog(this, "Duty leave register PDF generated!");
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 6. GRIEVANCE MANAGEMENT PANEL
    // =========================================================================

    private JPanel createGrievancePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbGrievanceCat = new JComboBox<>(new String[]{"All Categories", "ACADEMIC", "EXAMINATION", "ATTENDANCE", "FACILITY", "HARASSMENT", "OTHER"});
        cmbGrievanceCat.addActionListener(e -> filterGrievances());

        cmbGrievanceStatus = new JComboBox<>(new String[]{"All Statuses", "SUBMITTED", "UNDER_REVIEW", "RESOLVED", "DISMISSED"});
        cmbGrievanceStatus.addActionListener(e -> filterGrievances());

        txtGrievanceSearch = new JTextField(12);
        txtGrievanceSearch.putClientProperty("JTextField.placeholderText", "Search grievance...");
        txtGrievanceSearch.addActionListener(e -> filterGrievances());

        JButton btnResolve = UIComponents.createPrimaryButton("⚖️ Adjudicate / Issue Resolution");
        btnResolve.addActionListener(e -> showResolveGrievanceDialog());

        JButton btnViewDossier = UIComponents.createSecondaryButton("📋 View Case Dossier");
        btnViewDossier.addActionListener(e -> showGrievanceDossierDialog());

        JButton btnExportPdf = UIComponents.createSuccessButton("📑 Export Grievances PDF");
        btnExportPdf.addActionListener(e -> exportGrievancesPdf());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadGrievances());

        toolbar.add(new JLabel("Cat:"));
        toolbar.add(cmbGrievanceCat);
        toolbar.add(new JLabel("Status:"));
        toolbar.add(cmbGrievanceStatus);
        toolbar.add(txtGrievanceSearch);
        toolbar.add(btnResolve);
        toolbar.add(btnViewDossier);
        toolbar.add(btnExportPdf);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Student Name", "Register No", "Class", "Category", "Title", "Status", "Resolution Notes", "Adjudicated By"};
        grievanceTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(grievanceTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterGrievances() {
        if (cachedGrievances == null) return;
        String cat = (String) cmbGrievanceCat.getSelectedItem();
        String st = (String) cmbGrievanceStatus.getSelectedItem();
        String query = txtGrievanceSearch.getText().trim().toLowerCase();

        grievanceTableModel.setRowCount(0);
        for (Grievance g : cachedGrievances) {
            if (cat != null && !cat.equals("All Categories") && !cat.equalsIgnoreCase(g.getCategory().name())) {
                continue;
            }
            if (st != null && !st.equals("All Statuses") && !st.equalsIgnoreCase(g.getStatus().name())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchT = g.getTitle() != null && g.getTitle().toLowerCase().contains(query);
                boolean matchN = g.getStudentName() != null && g.getStudentName().toLowerCase().contains(query);
                boolean matchR = g.getRegisterNumber() != null && g.getRegisterNumber().toLowerCase().contains(query);
                if (!matchT && !matchN && !matchR) continue;
            }

            grievanceTableModel.addRow(new Object[]{
                    g.getId(),
                    g.getStudentName(),
                    g.getRegisterNumber(),
                    g.getClassName(),
                    g.getCategory().getLabel(),
                    g.getTitle(),
                    g.getStatus().getLabel(),
                    g.getResolution() != null ? g.getResolution() : "Pending review",
                    g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "-"
            });
        }
    }

    private Grievance getSelectedGrievance() {
        Component comp = ((JPanel) contentCardsPanel.getComponent(5)).getComponent(1);
        if (comp instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTable tbl) {
            int sel = tbl.getSelectedRow();
            if (sel >= 0) {
                int id = (int) grievanceTableModel.getValueAt(sel, 0);
                return cachedGrievances.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
            }
        }
        return null;
    }

    private void showResolveGrievanceDialog() {
        Grievance g = getSelectedGrievance();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "Please select a grievance case from the table to adjudicate.", "Select Case", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<Grievance.Status> cmbStatus = new JComboBox<>(new Grievance.Status[]{
                Grievance.Status.RESOLVED,
                Grievance.Status.UNDER_REVIEW,
                Grievance.Status.DISMISSED
        });
        cmbStatus.setSelectedItem(g.getStatus());

        JTextArea txtResolution = new JTextArea(g.getResolution() != null ? g.getResolution() : "", 5, 30);
        txtResolution.setLineWrap(true);
        txtResolution.setWrapStyleWord(true);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(3, 1, 4, 4));
        top.add(new JLabel("Case: #" + g.getId() + " - " + g.getTitle()));
        top.add(new JLabel("Student: " + g.getStudentName() + " (" + g.getRegisterNumber() + ")"));
        top.add(cmbStatus);

        p.add(top, BorderLayout.NORTH);
        p.add(new JLabel("Principal Official Verdict & Resolution Directives:"), BorderLayout.CENTER);
        p.add(new JScrollPane(txtResolution), BorderLayout.SOUTH);

        int opt = JOptionPane.showConfirmDialog(this, p, "Adjudicate Grievance Case #" + g.getId(), JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String resText = txtResolution.getText().trim();
            if (resText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Resolution statement cannot be blank.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User u = SessionManager.getInstance().getCurrentUser();
            boolean ok = grievanceDAO.updateResolution(g.getId(), (Grievance.Status) cmbStatus.getSelectedItem(), resText, u.getId(), u.getUsername());
            if (ok) {
                loadGrievances();
                JOptionPane.showMessageDialog(this, "Grievance status successfully updated and recorded in compliance register.");
            }
        }
    }

    private void showGrievanceDossierDialog() {
        Grievance g = getSelectedGrievance();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "Please select a grievance case from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String msg = String.format("<html><b>Grievance Case ID:</b> #%d<br>" +
                "<b>Student:</b> %s (%s)<br>" +
                "<b>Class:</b> %s<br>" +
                "<b>Category:</b> %s<br>" +
                "<b>Title:</b> %s<br><br>" +
                "<b>Statement of Grievance:</b><br>%s<br><br>" +
                "<b>Current Status:</b> %s<br>" +
                "<b>Official Resolution:</b><br>%s<br><br>" +
                "<b>Adjudicated By:</b> %s</html>",
                g.getId(), g.getStudentName(), g.getRegisterNumber(), g.getClassName(),
                g.getCategory().getLabel(), g.getTitle(), g.getDescription(),
                g.getStatus().getLabel(),
                g.getResolution() != null ? g.getResolution() : "Under Review / Not yet resolved",
                g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "-");

        JOptionPane.showMessageDialog(this, msg, "Grievance Dossier #" + g.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportGrievancesPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Student_Grievance_Redressal_Audit_Register.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PdfReportGenerator.generateGrievanceAuditReport(chooser.getSelectedFile(), cachedGrievances);
                lastGeneratedPdfFile = chooser.getSelectedFile();
                JOptionPane.showMessageDialog(this, "Grievance register PDF generated!");
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 7. REPORTS WITH PDF EXPORT PANEL
    // =========================================================================

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Configuration Card
        JPanel configCard = UIComponents.createCardPanel();
        configCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        cmbReportType = new JComboBox<>(new String[]{
                "Institutional Academic Analytics & Performance Dossier",
                "Department Summary & HOD Governance Register",
                "Class Examination Result & Grade Sheet Master Report",
                "Tier-3 Duty Leave Sanction Audit Register",
                "Student Grievance Redressal Oversight Register",
                "Faculty Workload & Governance Register",
                "System Security & Audit Trail Register"
        });
        cmbReportType.addActionListener(e -> updateReportControlsState());

        cmbReportClass = new JComboBox<>();
        cmbReportSubject = new JComboBox<>();
        cmbReportExamType = new JComboBox<>(Mark.ExamType.values());

        cmbReportClass.addActionListener(e -> {
            ClassBatch cb = (ClassBatch) cmbReportClass.getSelectedItem();
            if (cb != null) {
                List<Subject> subs = subjectDAO.getSubjectsByClass(cb.getId());
                cmbReportSubject.removeAllItems();
                for (Subject s : subs) cmbReportSubject.addItem(s);
            }
        });

        JButton btnPreviewReport = UIComponents.createPrimaryButton("👁️ Preview Report Data");
        btnPreviewReport.addActionListener(e -> previewSelectedReport());

        JButton btnGeneratePdf = UIComponents.createSuccessButton("📄 Export Official PDF");
        btnGeneratePdf.addActionListener(e -> generateAndExportSelectedReportPdf());

        JButton btnOpenLastPdf = UIComponents.createSecondaryButton("🌐 Open Generated PDF");
        btnOpenLastPdf.addActionListener(e -> {
            if (lastGeneratedPdfFile != null && lastGeneratedPdfFile.exists()) {
                try {
                    Desktop.getDesktop().open(lastGeneratedPdfFile);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "No PDF has been generated yet in this session.");
            }
        });

        configCard.add(new JLabel("Report Type:"));
        configCard.add(cmbReportType);
        configCard.add(new JLabel("Class:"));
        configCard.add(cmbReportClass);
        configCard.add(new JLabel("Course:"));
        configCard.add(cmbReportSubject);
        configCard.add(new JLabel("Exam:"));
        configCard.add(cmbReportExamType);
        configCard.add(btnPreviewReport);
        configCard.add(btnGeneratePdf);
        configCard.add(btnOpenLastPdf);

        // Center Live Preview Table
        String[] cols = {"Field 1", "Field 2", "Field 3", "Field 4", "Field 5", "Field 6", "Field 7", "Field 8"};
        reportPreviewTableModel = new DefaultTableModel(cols, 0);
        JTable previewTable = UIComponents.createStyledTable(reportPreviewTableModel);

        // Status Footer
        lblReportStatus = new JLabel("Select a report and click 'Preview' or 'Export Official PDF'.");
        lblReportStatus.setFont(UITheme.FONT_SMALL);
        lblReportStatus.setForeground(UITheme.TEXT_MUTED);

        panel.add(configCard, BorderLayout.NORTH);
        panel.add(new JScrollPane(previewTable), BorderLayout.CENTER);
        panel.add(lblReportStatus, BorderLayout.SOUTH);

        return panel;
    }

    private void updateReportControlsState() {
        String type = (String) cmbReportType.getSelectedItem();
        boolean isClassReport = type != null && type.startsWith("Class Examination Result");
        cmbReportClass.setEnabled(isClassReport);
        cmbReportSubject.setEnabled(isClassReport);
        cmbReportExamType.setEnabled(isClassReport);
    }

    private void previewSelectedReport() {
        String type = (String) cmbReportType.getSelectedItem();
        if (type == null) return;

        reportPreviewTableModel.setRowCount(0);

        if (type.startsWith("Institutional Academic Analytics")) {
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"Register No", "Student Name", "Class", "Department", "Semester", "Att %", "SGPA", "CGPA"});
            for (Student s : cachedStudents) {
                reportPreviewTableModel.addRow(new Object[]{
                        s.getRegisterNumber(), s.getFullName(), s.getClassName(), s.getDepartmentName(),
                        s.getSemester(), String.format("%.1f%%", s.getAttendancePercentage()), s.getCurrentSgpa(), s.getCurrentCgpa()
                });
            }
            lblReportStatus.setText("Previewing " + cachedStudents.size() + " enrolled students in Institutional Academic Analytics Dossier.");

        } else if (type.startsWith("Department Summary")) {
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"Code", "Department Name", "Current HOD", "Enrolled Students", "Faculty Strength"});
            for (Department d : cachedDepartments) {
                long stuCount = cachedStudents.stream().filter(s -> d.getName().equalsIgnoreCase(s.getDepartmentName())).count();
                long facCount = cachedFaculty.stream().filter(f -> d.getName().equalsIgnoreCase(f.getDepartmentName())).count();
                reportPreviewTableModel.addRow(new Object[]{
                        d.getCode(), d.getName(), d.getHodName() != null ? d.getHodName() : "Unassigned", stuCount, facCount
                });
            }
            lblReportStatus.setText("Previewing " + cachedDepartments.size() + " academic departments.");

        } else if (type.startsWith("Class Examination Result")) {
            ClassBatch cb = (ClassBatch) cmbReportClass.getSelectedItem();
            Subject sub = (Subject) cmbReportSubject.getSelectedItem();
            Mark.ExamType et = (Mark.ExamType) cmbReportExamType.getSelectedItem();
            if (cb == null || sub == null || et == null) {
                JOptionPane.showMessageDialog(this, "Please select Class, Course, and Exam Type to preview class results.", "Select Options", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<StudentMarksEntryRow> rows = markDAO.getClassSubjectMarks(cb.getId(), sub.getId(), et);
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"Roll", "Register No", "Student Name", "Marks Obtained", "Max Marks", "Percentage", "Grade", "Status"});
            for (StudentMarksEntryRow r : rows) {
                double marks = r.marksObtained() != null ? r.marksObtained() : 0.0;
                double max = r.maxMarks() != null ? r.maxMarks() : 50.0;
                double pct = (marks / max) * 100.0;
                String grade = pct >= 90 ? "S" : pct >= 80 ? "A" : pct >= 70 ? "B" : pct >= 60 ? "C" : pct >= 50 ? "D" : pct >= 40 ? "E" : "F";
                reportPreviewTableModel.addRow(new Object[]{
                        r.rollNumber(), r.registerNumber(), r.fullName(),
                        r.marksObtained() != null ? marks : "ABSENT", max, String.format("%.1f%%", pct), grade, pct >= 40 ? "PASS" : "FAIL"
                });
            }
            lblReportStatus.setText("Previewing " + rows.size() + " student marks in " + cb.getName() + " - " + sub.getName());

        } else if (type.startsWith("Tier-3 Duty Leave")) {
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"ID", "Student", "Register No", "Class", "Total Days", "Reason", "HOD Endorsement", "Status"});
            for (DutyLeave dl : cachedLeaves) {
                reportPreviewTableModel.addRow(new Object[]{
                        dl.getId(), dl.getStudentName(), dl.getRegisterNumber(), dl.getClassName(),
                        dl.getTotalDays(), dl.getReason(), dl.getHodRemarks() != null ? dl.getHodRemarks() : "-", dl.getStatus().getLabel()
                });
            }
            lblReportStatus.setText("Previewing " + cachedLeaves.size() + " duty leave applications in register.");

        } else if (type.startsWith("Student Grievance")) {
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"ID", "Student", "Register No", "Class", "Category", "Title", "Status", "Resolution"});
            for (Grievance g : cachedGrievances) {
                reportPreviewTableModel.addRow(new Object[]{
                        g.getId(), g.getStudentName(), g.getRegisterNumber(), g.getClassName(),
                        g.getCategory().getLabel(), g.getTitle(), g.getStatus().getLabel(), g.getResolution() != null ? g.getResolution() : "Pending"
                });
            }
            lblReportStatus.setText("Previewing " + cachedGrievances.size() + " grievances in institutional register.");

        } else if (type.startsWith("Faculty Workload")) {
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"Faculty ID", "Name", "Department", "Designation", "Qualification", "HOD?", "CFA?"});
            for (Faculty f : cachedFaculty) {
                reportPreviewTableModel.addRow(new Object[]{
                        f.getFacultyId(), f.getFullName(), f.getDepartmentName(), f.getDesignation(),
                        f.getQualification() != null ? f.getQualification() : "-", f.isHod() ? "YES" : "NO", f.isCfa() ? "YES" : "NO"
                });
            }
            lblReportStatus.setText("Previewing " + cachedFaculty.size() + " faculty members in roster.");

        } else if (type.startsWith("System Security")) {
            reportPreviewTableModel.setColumnIdentifiers(new String[]{"Timestamp", "User", "Role", "Action", "Severity", "Details"});
            for (AuditLog l : cachedAuditLogs) {
                reportPreviewTableModel.addRow(new Object[]{
                        l.getTimestamp(), l.getUsername(), l.getRole(), l.getAction(), l.getSeverity().name(), l.getDetails()
                });
            }
            lblReportStatus.setText("Previewing " + cachedAuditLogs.size() + " system audit logs.");
        }
    }

    private void generateAndExportSelectedReportPdf() {
        String type = (String) cmbReportType.getSelectedItem();
        if (type == null) return;

        JFileChooser chooser = new JFileChooser();
        String safeName = type.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        chooser.setSelectedFile(new File(safeName));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                if (type.startsWith("Institutional Academic Analytics")) {
                    PdfReportGenerator.generateInstitutionalPerformanceReport(target, cachedStudents, cachedDepartments);
                } else if (type.startsWith("Department Summary")) {
                    PdfReportGenerator.generateInstitutionalPerformanceReport(target, cachedStudents, cachedDepartments);
                } else if (type.startsWith("Class Examination Result")) {
                    ClassBatch cb = (ClassBatch) cmbReportClass.getSelectedItem();
                    Subject sub = (Subject) cmbReportSubject.getSelectedItem();
                    Mark.ExamType et = (Mark.ExamType) cmbReportExamType.getSelectedItem();
                    if (cb == null || sub == null || et == null) {
                        JOptionPane.showMessageDialog(this, "Please select Class, Course, and Exam Type.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    List<StudentMarksEntryRow> rows = markDAO.getClassSubjectMarks(cb.getId(), sub.getId(), et);
                    PdfReportGenerator.generateClassResultReport(target, cb.getName(), sub.getCode() + " - " + sub.getName(), et.name(), rows, 50.0);
                } else if (type.startsWith("Tier-3 Duty Leave")) {
                    PdfReportGenerator.generateDutyLeaveAuditReport(target, cachedLeaves);
                } else if (type.startsWith("Student Grievance")) {
                    PdfReportGenerator.generateGrievanceAuditReport(target, cachedGrievances);
                } else if (type.startsWith("Faculty Workload")) {
                    PdfReportGenerator.generateFacultyWorkloadReport(target, cachedFaculty);
                } else if (type.startsWith("System Security")) {
                    PdfReportGenerator.generateAuditLogsReport(target, cachedAuditLogs);
                }

                lastGeneratedPdfFile = target;
                lblReportStatus.setText("PDF successfully generated: " + target.getAbsolutePath());

                int opt = JOptionPane.showConfirmDialog(this,
                        "Official Executive Report generated successfully!\nPath: " + target.getAbsolutePath() + "\n\nWould you like to open it now?",
                        "Export Successful", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(target);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating report PDF: " + ex.getMessage(), "Report Generation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 8. ANNOUNCEMENTS & OFFICIAL CIRCULARS PANEL
    // =========================================================================

    private JPanel createAnnouncementsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbAnnAudience = new JComboBox<>(new String[]{"All Audiences", "ALL", "FACULTY", "STUDENTS", "PARENTS"});
        cmbAnnAudience.addActionListener(e -> filterAnnouncements());

        cmbAnnCategory = new JComboBox<>(new String[]{"All Categories", "GENERAL", "ACADEMIC", "EXAMINATION", "EVENT", "URGENT", "CIRCULAR"});
        cmbAnnCategory.addActionListener(e -> filterAnnouncements());

        txtAnnSearch = new JTextField(12);
        txtAnnSearch.putClientProperty("JTextField.placeholderText", "Search circulars...");
        txtAnnSearch.addActionListener(e -> filterAnnouncements());

        JButton btnNewAnn = UIComponents.createPrimaryButton("📢 Issue New Circular");
        btnNewAnn.addActionListener(e -> showCreateAnnouncementDialog());

        JButton btnEditAnn = UIComponents.createSecondaryButton("✏️ Edit Circular");
        btnEditAnn.addActionListener(e -> showEditAnnouncementDialog());

        JButton btnTogglePin = UIComponents.createSecondaryButton("📌 Toggle Pin");
        btnTogglePin.addActionListener(e -> toggleAnnouncementPin());

        JButton btnDeleteAnn = UIComponents.createDangerButton("🗑️ Delete");
        btnDeleteAnn.addActionListener(e -> deleteSelectedAnnouncement());

        JButton btnViewAnn = UIComponents.createSecondaryButton("👁️ Read Full Circular");
        btnViewAnn.addActionListener(e -> showReadAnnouncementDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadAnnouncements());

        toolbar.add(new JLabel("Audience:"));
        toolbar.add(cmbAnnAudience);
        toolbar.add(new JLabel("Category:"));
        toolbar.add(cmbAnnCategory);
        toolbar.add(txtAnnSearch);
        toolbar.add(btnNewAnn);
        toolbar.add(btnEditAnn);
        toolbar.add(btnTogglePin);
        toolbar.add(btnDeleteAnn);
        toolbar.add(btnViewAnn);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Pinned", "Title", "Category", "Audience", "Posted Date", "Expiry Date", "Issued By", "Reference / Directive"};
        announcementTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(announcementTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterAnnouncements() {
        if (cachedAnnouncements == null) return;
        String aud = (String) cmbAnnAudience.getSelectedItem();
        String cat = (String) cmbAnnCategory.getSelectedItem();
        String query = txtAnnSearch.getText().trim().toLowerCase();

        announcementTableModel.setRowCount(0);
        for (Announcement a : cachedAnnouncements) {
            if (aud != null && !aud.equals("All Audiences") && !aud.equalsIgnoreCase(a.getTargetAudience())) {
                continue;
            }
            if (cat != null && !cat.equals("All Categories") && !cat.equalsIgnoreCase(a.getCategory().name())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchT = a.getTitle() != null && a.getTitle().toLowerCase().contains(query);
                boolean matchC = a.getContent() != null && a.getContent().toLowerCase().contains(query);
                if (!matchT && !matchC) continue;
            }

            announcementTableModel.addRow(new Object[]{
                    a.getId(),
                    a.isPinned() ? "📌 Pinned" : "-",
                    a.getTitle(),
                    a.getCategory().getLabel(),
                    a.getTargetAudience(),
                    a.getPostedDate() != null ? a.getPostedDate().toString() : "-",
                    a.getExpiryDate() != null ? a.getExpiryDate().toString() : "-",
                    a.getPostedByUserName() != null ? a.getPostedByUserName() : "Principal",
                    a.getAttachmentNote() != null ? a.getAttachmentNote() : "-"
            });
        }
    }

    private Announcement getSelectedAnnouncement() {
        Component comp = ((JPanel) contentCardsPanel.getComponent(7)).getComponent(1);
        if (comp instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTable tbl) {
            int sel = tbl.getSelectedRow();
            if (sel >= 0) {
                int id = (int) announcementTableModel.getValueAt(sel, 0);
                return cachedAnnouncements.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
            }
        }
        return null;
    }

    private void showCreateAnnouncementDialog() {
        JTextField txtTitle = new JTextField(25);
        JComboBox<Announcement.Category> cmbCat = new JComboBox<>(Announcement.Category.values());
        JComboBox<String> cmbAud = new JComboBox<>(new String[]{"ALL", "FACULTY", "STUDENTS", "PARENTS"});
        JCheckBox chkPin = new JCheckBox("Pin to Top of Notice Board", true);
        JTextField txtExpiry = new JTextField(LocalDate.now().plusMonths(1).toString(), 10);
        JTextField txtRef = new JTextField("Office of the Principal Directive", 20);
        JTextArea txtContent = new JTextArea(6, 25);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.add(new JLabel("Circular Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Category:"));
        p.add(cmbCat);
        p.add(new JLabel("Target Audience:"));
        p.add(cmbAud);
        p.add(new JLabel("Pin Status:"));
        p.add(chkPin);
        p.add(new JLabel("Expiry Date (yyyy-MM-dd):"));
        p.add(txtExpiry);
        p.add(new JLabel("Reference Note:"));
        p.add(txtRef);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.add(new JLabel("Official Circular Body:"), BorderLayout.CENTER);
        wrapper.add(new JScrollPane(txtContent), BorderLayout.SOUTH);

        int opt = JOptionPane.showConfirmDialog(this, wrapper, "Issue Institutional Circular", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String title = txtTitle.getText().trim();
            String content = txtContent.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and circular body cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Announcement a = new Announcement();
            a.setTitle(title);
            a.setContent(content);
            a.setCategory((Announcement.Category) cmbCat.getSelectedItem());
            a.setTargetAudience((String) cmbAud.getSelectedItem());
            a.setPinned(chkPin.isSelected());
            a.setPostedDate(Date.valueOf(LocalDate.now()));
            try {
                a.setExpiryDate(Date.valueOf(txtExpiry.getText().trim()));
            } catch (Exception ex) {
                a.setExpiryDate(Date.valueOf(LocalDate.now().plusMonths(1)));
            }
            a.setAttachmentNote(txtRef.getText().trim());
            User u = SessionManager.getInstance().getCurrentUser();
            a.setPostedByUserId(u != null ? u.getId() : 1);

            boolean ok = announcementDAO.createAnnouncement(a, u != null ? u.getUsername() : "principal");
            if (ok) {
                loadAnnouncements();
                JOptionPane.showMessageDialog(this, "Official circular published successfully!");
            }
        }
    }

    private void showEditAnnouncementDialog() {
        Announcement a = getSelectedAnnouncement();
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Please select a circular to edit.", "Select Circular", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField txtTitle = new JTextField(a.getTitle(), 25);
        JComboBox<Announcement.Category> cmbCat = new JComboBox<>(Announcement.Category.values());
        cmbCat.setSelectedItem(a.getCategory());
        JComboBox<String> cmbAud = new JComboBox<>(new String[]{"ALL", "FACULTY", "STUDENTS", "PARENTS"});
        cmbAud.setSelectedItem(a.getTargetAudience());
        JCheckBox chkPin = new JCheckBox("Pin to Top of Notice Board", a.isPinned());
        JTextField txtExpiry = new JTextField(a.getExpiryDate() != null ? a.getExpiryDate().toString() : LocalDate.now().plusMonths(1).toString(), 10);
        JTextField txtRef = new JTextField(a.getAttachmentNote() != null ? a.getAttachmentNote() : "", 20);
        JTextArea txtContent = new JTextArea(a.getContent(), 6, 25);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.add(new JLabel("Circular Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Category:"));
        p.add(cmbCat);
        p.add(new JLabel("Target Audience:"));
        p.add(cmbAud);
        p.add(new JLabel("Pin Status:"));
        p.add(chkPin);
        p.add(new JLabel("Expiry Date:"));
        p.add(txtExpiry);
        p.add(new JLabel("Reference Note:"));
        p.add(txtRef);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.add(new JLabel("Official Circular Body:"), BorderLayout.CENTER);
        wrapper.add(new JScrollPane(txtContent), BorderLayout.SOUTH);

        int opt = JOptionPane.showConfirmDialog(this, wrapper, "Edit Institutional Circular #" + a.getId(), JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            a.setTitle(txtTitle.getText().trim());
            a.setContent(txtContent.getText().trim());
            a.setCategory((Announcement.Category) cmbCat.getSelectedItem());
            a.setTargetAudience((String) cmbAud.getSelectedItem());
            a.setPinned(chkPin.isSelected());
            try {
                a.setExpiryDate(Date.valueOf(txtExpiry.getText().trim()));
            } catch (Exception ex) {
                // keep previous
            }
            a.setAttachmentNote(txtRef.getText().trim());

            User u = SessionManager.getInstance().getCurrentUser();
            boolean ok = announcementDAO.updateAnnouncement(a, u != null ? u.getUsername() : "principal");
            if (ok) {
                loadAnnouncements();
                JOptionPane.showMessageDialog(this, "Circular successfully updated!");
            }
        }
    }

    private void toggleAnnouncementPin() {
        Announcement a = getSelectedAnnouncement();
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Please select a circular to toggle pin status.", "Select Circular", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean ok = announcementDAO.togglePin(a.getId(), !a.isPinned());
        if (ok) {
            loadAnnouncements();
        }
    }

    private void deleteSelectedAnnouncement() {
        Announcement a = getSelectedAnnouncement();
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Please select a circular to delete.", "Select Circular", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you wish to delete circular #" + a.getId() + " (" + a.getTitle() + ")?",
                "Confirm Delete Circular", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            User u = SessionManager.getInstance().getCurrentUser();
            boolean ok = announcementDAO.deleteAnnouncement(a.getId(), u.getUsername(), u.getId());
            if (ok) {
                loadAnnouncements();
                JOptionPane.showMessageDialog(this, "Circular successfully deleted.");
            }
        }
    }

    private void showReadAnnouncementDialog() {
        Announcement a = getSelectedAnnouncement();
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Please select a circular from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String msg = String.format("<html><h2>%s</h2>" +
                "<b>Category:</b> %s | <b>Audience:</b> %s<br>" +
                "<b>Issued Date:</b> %s | <b>Expires:</b> %s<br>" +
                "<b>Reference Note:</b> %s<br><hr><br>" +
                "<p style='width:360px;'>%s</p></html>",
                a.getTitle(), a.getCategory().getLabel(), a.getTargetAudience(),
                a.getPostedDate(), a.getExpiryDate() != null ? a.getExpiryDate() : "Permanent",
                a.getAttachmentNote() != null ? a.getAttachmentNote() : "-",
                a.getContent().replace("\n", "<br>"));

        JOptionPane.showMessageDialog(this, msg, "Official Circular #" + a.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // 9. ACADEMIC CALENDAR PANEL
    // =========================================================================

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbCalType = new JComboBox<>(new String[]{"All Event Types", "EXAM", "HOLIDAY", "ACADEMIC_MILESTONE", "MEETING", "CULTURAL_SPORTS", "DEADLINE"});
        cmbCalType.addActionListener(e -> filterCalendarEvents());

        txtCalSearch = new JTextField(12);
        txtCalSearch.putClientProperty("JTextField.placeholderText", "Search event...");
        txtCalSearch.addActionListener(e -> filterCalendarEvents());

        JButton btnNewEvent = UIComponents.createPrimaryButton("📅 Schedule Academic Event");
        btnNewEvent.addActionListener(e -> showCreateCalendarEventDialog());

        JButton btnEditEvent = UIComponents.createSecondaryButton("✏️ Edit Event");
        btnEditEvent.addActionListener(e -> showEditCalendarEventDialog());

        JButton btnDeleteEvent = UIComponents.createDangerButton("🗑️ Remove Event");
        btnDeleteEvent.addActionListener(e -> deleteSelectedCalendarEvent());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadCalendarEvents());

        toolbar.add(new JLabel("Type:"));
        toolbar.add(cmbCalType);
        toolbar.add(txtCalSearch);
        toolbar.add(btnNewEvent);
        toolbar.add(btnEditEvent);
        toolbar.add(btnDeleteEvent);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Event Title", "Type", "Start Date", "End Date", "Applicable Semesters", "Audience", "Description", "Scheduled By"};
        calendarTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(calendarTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterCalendarEvents() {
        if (cachedCalendarEvents == null) return;
        String typeFilter = (String) cmbCalType.getSelectedItem();
        String query = txtCalSearch.getText().trim().toLowerCase();

        calendarTableModel.setRowCount(0);
        for (AcademicEvent ae : cachedCalendarEvents) {
            if (typeFilter != null && !typeFilter.equals("All Event Types") && !typeFilter.equalsIgnoreCase(ae.getEventType().name())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchT = ae.getEventTitle() != null && ae.getEventTitle().toLowerCase().contains(query);
                boolean matchD = ae.getDescription() != null && ae.getDescription().toLowerCase().contains(query);
                if (!matchT && !matchD) continue;
            }

            calendarTableModel.addRow(new Object[]{
                    ae.getId(),
                    ae.getEventTitle(),
                    ae.getEventType().getLabel(),
                    ae.getStartDate() != null ? ae.getStartDate().toString() : "-",
                    ae.getEndDate() != null ? ae.getEndDate().toString() : "-",
                    ae.getSemesterApplicable() != null ? ae.getSemesterApplicable() : "All",
                    ae.getTargetAudience(),
                    ae.getDescription() != null ? ae.getDescription() : "-",
                    ae.getCreatedByUserName() != null ? ae.getCreatedByUserName() : "Principal"
            });
        }
    }

    private AcademicEvent getSelectedCalendarEvent() {
        Component comp = ((JPanel) contentCardsPanel.getComponent(8)).getComponent(1);
        if (comp instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTable tbl) {
            int sel = tbl.getSelectedRow();
            if (sel >= 0) {
                int id = (int) calendarTableModel.getValueAt(sel, 0);
                return cachedCalendarEvents.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
            }
        }
        return null;
    }

    private void showCreateCalendarEventDialog() {
        JTextField txtTitle = new JTextField(25);
        JComboBox<AcademicEvent.EventType> cmbType = new JComboBox<>(AcademicEvent.EventType.values());
        JTextField txtStart = new JTextField(LocalDate.now().plusDays(7).toString(), 10);
        JTextField txtEnd = new JTextField(LocalDate.now().plusDays(8).toString(), 10);
        JTextField txtSem = new JTextField("All Semesters", 15);
        JComboBox<String> cmbAud = new JComboBox<>(new String[]{"ALL", "FACULTY", "STUDENTS", "PARENTS"});
        JTextArea txtDesc = new JTextArea(4, 25);
        txtDesc.setLineWrap(true);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.add(new JLabel("Event / Milestone Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Event Type:"));
        p.add(cmbType);
        p.add(new JLabel("Start Date (yyyy-MM-dd):"));
        p.add(txtStart);
        p.add(new JLabel("End Date (yyyy-MM-dd):"));
        p.add(txtEnd);
        p.add(new JLabel("Applicable Semesters:"));
        p.add(txtSem);
        p.add(new JLabel("Target Audience:"));
        p.add(cmbAud);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.add(new JLabel("Event Description:"), BorderLayout.CENTER);
        wrapper.add(new JScrollPane(txtDesc), BorderLayout.SOUTH);

        int opt = JOptionPane.showConfirmDialog(this, wrapper, "Schedule Master Academic Calendar Event", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String title = txtTitle.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Event title cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AcademicEvent e = new AcademicEvent();
            e.setEventTitle(title);
            e.setEventType((AcademicEvent.EventType) cmbType.getSelectedItem());
            try {
                e.setStartDate(Date.valueOf(txtStart.getText().trim()));
            } catch (Exception ex) {
                e.setStartDate(Date.valueOf(LocalDate.now()));
            }
            try {
                e.setEndDate(Date.valueOf(txtEnd.getText().trim()));
            } catch (Exception ex) {
                e.setEndDate(e.getStartDate());
            }
            e.setSemesterApplicable(txtSem.getText().trim());
            e.setTargetAudience((String) cmbAud.getSelectedItem());
            e.setDescription(txtDesc.getText().trim());
            User u = SessionManager.getInstance().getCurrentUser();
            e.setCreatedByUserId(u != null ? u.getId() : 1);

            boolean ok = calendarDAO.createEvent(e, u != null ? u.getUsername() : "principal");
            if (ok) {
                loadCalendarEvents();
                JOptionPane.showMessageDialog(this, "Academic calendar event successfully scheduled!");
            }
        }
    }

    private void showEditCalendarEventDialog() {
        AcademicEvent ae = getSelectedCalendarEvent();
        if (ae == null) {
            JOptionPane.showMessageDialog(this, "Please select an event to edit.", "Select Event", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField txtTitle = new JTextField(ae.getEventTitle(), 25);
        JComboBox<AcademicEvent.EventType> cmbType = new JComboBox<>(AcademicEvent.EventType.values());
        cmbType.setSelectedItem(ae.getEventType());
        JTextField txtStart = new JTextField(ae.getStartDate() != null ? ae.getStartDate().toString() : "", 10);
        JTextField txtEnd = new JTextField(ae.getEndDate() != null ? ae.getEndDate().toString() : "", 10);
        JTextField txtSem = new JTextField(ae.getSemesterApplicable() != null ? ae.getSemesterApplicable() : "All", 15);
        JComboBox<String> cmbAud = new JComboBox<>(new String[]{"ALL", "FACULTY", "STUDENTS", "PARENTS"});
        cmbAud.setSelectedItem(ae.getTargetAudience());
        JTextArea txtDesc = new JTextArea(ae.getDescription() != null ? ae.getDescription() : "", 4, 25);
        txtDesc.setLineWrap(true);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.add(new JLabel("Event Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Event Type:"));
        p.add(cmbType);
        p.add(new JLabel("Start Date:"));
        p.add(txtStart);
        p.add(new JLabel("End Date:"));
        p.add(txtEnd);
        p.add(new JLabel("Applicable Semesters:"));
        p.add(txtSem);
        p.add(new JLabel("Target Audience:"));
        p.add(cmbAud);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.add(new JLabel("Event Description:"), BorderLayout.CENTER);
        wrapper.add(new JScrollPane(txtDesc), BorderLayout.SOUTH);

        int opt = JOptionPane.showConfirmDialog(this, wrapper, "Edit Event #" + ae.getId(), JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            ae.setEventTitle(txtTitle.getText().trim());
            ae.setEventType((AcademicEvent.EventType) cmbType.getSelectedItem());
            try {
                ae.setStartDate(Date.valueOf(txtStart.getText().trim()));
            } catch (Exception ex) {}
            try {
                ae.setEndDate(Date.valueOf(txtEnd.getText().trim()));
            } catch (Exception ex) {}
            ae.setSemesterApplicable(txtSem.getText().trim());
            ae.setTargetAudience((String) cmbAud.getSelectedItem());
            ae.setDescription(txtDesc.getText().trim());

            User u = SessionManager.getInstance().getCurrentUser();
            boolean ok = calendarDAO.updateEvent(ae, u != null ? u.getUsername() : "principal");
            if (ok) {
                loadCalendarEvents();
                JOptionPane.showMessageDialog(this, "Academic event successfully updated!");
            }
        }
    }

    private void deleteSelectedCalendarEvent() {
        AcademicEvent ae = getSelectedCalendarEvent();
        if (ae == null) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Select Event", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you wish to delete event #" + ae.getId() + " (" + ae.getEventTitle() + ")?",
                "Confirm Remove Event", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            User u = SessionManager.getInstance().getCurrentUser();
            boolean ok = calendarDAO.deleteEvent(ae.getId(), u.getUsername(), u.getId());
            if (ok) {
                loadCalendarEvents();
                JOptionPane.showMessageDialog(this, "Event successfully removed from academic calendar.");
            }
        }
    }

    // =========================================================================
    // 10. INSTITUTIONAL NOTIFICATIONS PANEL
    // =========================================================================

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbNotifAudience = new JComboBox<>(new String[]{"All Audiences", "ALL", "PARENTS", "STUDENTS", "FACULTY"});
        cmbNotifAudience.addActionListener(e -> filterNotifications());

        cmbNotifPriority = new JComboBox<>(new String[]{"All Priorities", "LOW", "NORMAL", "HIGH", "URGENT"});
        cmbNotifPriority.addActionListener(e -> filterNotifications());

        txtNotifSearch = new JTextField(12);
        txtNotifSearch.putClientProperty("JTextField.placeholderText", "Search alerts...");
        txtNotifSearch.addActionListener(e -> filterNotifications());

        JButton btnNewNotif = UIComponents.createPrimaryButton("🔔 Broadcast Institutional Alert");
        btnNewNotif.addActionListener(e -> showCreateNotificationDialog());

        JButton btnToggleStatus = UIComponents.createSecondaryButton("🔄 Toggle Active/Inactive");
        btnToggleStatus.addActionListener(e -> toggleNotificationStatus());

        JButton btnDeleteNotif = UIComponents.createDangerButton("🗑️ Delete");
        btnDeleteNotif.addActionListener(e -> deleteSelectedNotification());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadNotifications());

        toolbar.add(new JLabel("Audience:"));
        toolbar.add(cmbNotifAudience);
        toolbar.add(new JLabel("Priority:"));
        toolbar.add(cmbNotifPriority);
        toolbar.add(txtNotifSearch);
        toolbar.add(btnNewNotif);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnDeleteNotif);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Title", "Message Snippet", "Audience", "Priority", "Category", "Publish Date", "Status", "Broadcast By"};
        notificationTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(notificationTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterNotifications() {
        if (cachedNotifications == null) return;
        String aud = (String) cmbNotifAudience.getSelectedItem();
        String pri = (String) cmbNotifPriority.getSelectedItem();
        String query = txtNotifSearch.getText().trim().toLowerCase();

        notificationTableModel.setRowCount(0);
        for (Notification n : cachedNotifications) {
            if (aud != null && !aud.equals("All Audiences") && !aud.equalsIgnoreCase(n.getTargetAudience())) {
                continue;
            }
            if (pri != null && !pri.equals("All Priorities") && !pri.equalsIgnoreCase(n.getPriority().name())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchT = n.getTitle() != null && n.getTitle().toLowerCase().contains(query);
                boolean matchM = n.getMessage() != null && n.getMessage().toLowerCase().contains(query);
                if (!matchT && !matchM) continue;
            }

            notificationTableModel.addRow(new Object[]{
                    n.getId(),
                    n.getTitle(),
                    n.getMessage() != null && n.getMessage().length() > 50 ? n.getMessage().substring(0, 48) + ".." : n.getMessage(),
                    n.getTargetAudience(),
                    n.getPriority().getLabel(),
                    n.getCategory().getLabel(),
                    n.getPublishDate() != null ? n.getPublishDate().toString() : "-",
                    n.isActive() ? "Active" : "Archived",
                    n.getPostedByUserName() != null ? n.getPostedByUserName() : "Principal"
            });
        }
    }

    private Notification getSelectedNotification() {
        Component comp = ((JPanel) contentCardsPanel.getComponent(9)).getComponent(1);
        if (comp instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTable tbl) {
            int sel = tbl.getSelectedRow();
            if (sel >= 0) {
                int id = (int) notificationTableModel.getValueAt(sel, 0);
                return cachedNotifications.stream().filter(n -> n.getId() == id).findFirst().orElse(null);
            }
        }
        return null;
    }

    private void showCreateNotificationDialog() {
        JTextField txtTitle = new JTextField(25);
        JComboBox<Notification.Priority> cmbPri = new JComboBox<>(Notification.Priority.values());
        JComboBox<Notification.Category> cmbCat = new JComboBox<>(Notification.Category.values());
        JComboBox<String> cmbAud = new JComboBox<>(new String[]{"ALL", "PARENTS", "STUDENTS", "FACULTY"});
        JTextArea txtMsg = new JTextArea(5, 25);
        txtMsg.setLineWrap(true);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Alert Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Target Audience:"));
        p.add(cmbAud);
        p.add(new JLabel("Priority Level:"));
        p.add(cmbPri);
        p.add(new JLabel("Category:"));
        p.add(cmbCat);

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.add(new JLabel("Notification Message Body:"), BorderLayout.CENTER);
        wrapper.add(new JScrollPane(txtMsg), BorderLayout.SOUTH);

        int opt = JOptionPane.showConfirmDialog(this, wrapper, "Broadcast Institutional Notification", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String title = txtTitle.getText().trim();
            String msg = txtMsg.getText().trim();
            if (title.isEmpty() || msg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and message cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Notification n = new Notification();
            n.setTitle(title);
            n.setMessage(msg);
            n.setTargetAudience((String) cmbAud.getSelectedItem());
            n.setPriority((Notification.Priority) cmbPri.getSelectedItem());
            n.setCategory((Notification.Category) cmbCat.getSelectedItem());
            n.setPublishDate(Date.valueOf(LocalDate.now()));
            n.setActive(true);
            User u = SessionManager.getInstance().getCurrentUser();
            n.setPostedByUserId(u != null ? u.getId() : 1);

            boolean ok = notificationDAO.createNotification(n);
            if (ok) {
                AuditLogDAO.log(u != null ? u.getId() : 1, u != null ? u.getUsername() : "principal", "PRINCIPAL",
                        "NOTIFICATION_BROADCAST", "NOTIFICATION", title, "Priority: " + n.getPriority().name(), AuditLog.Severity.INFO);
                loadNotifications();
                JOptionPane.showMessageDialog(this, "Institutional alert successfully broadcasted across portals!");
            }
        }
    }

    private void toggleNotificationStatus() {
        Notification n = getSelectedNotification();
        if (n == null) {
            JOptionPane.showMessageDialog(this, "Please select an alert from the table.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean ok = notificationDAO.toggleNotificationStatus(n.getId(), !n.isActive());
        if (ok) {
            loadNotifications();
        }
    }

    private void deleteSelectedNotification() {
        Notification n = getSelectedNotification();
        if (n == null) {
            JOptionPane.showMessageDialog(this, "Please select an alert to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Delete notification #" + n.getId() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = notificationDAO.deleteNotification(n.getId());
            if (ok) {
                loadNotifications();
            }
        }
    }

    // =========================================================================
    // 11. SYSTEM SECURITY & ACTIVITY LOGS PANEL
    // =========================================================================

    private JPanel createActivityLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        cmbLogSeverity = new JComboBox<>(new String[]{"All Severities", "INFO", "WARNING", "SECURITY_ALERT", "ERROR"});
        cmbLogSeverity.addActionListener(e -> filterActivityLogs());

        txtLogSearch = new JTextField(12);
        txtLogSearch.putClientProperty("JTextField.placeholderText", "Search action / user...");
        txtLogSearch.addActionListener(e -> filterActivityLogs());

        JButton btnExportPdf = UIComponents.createSuccessButton("📑 Export Audit Trail PDF");
        btnExportPdf.addActionListener(e -> exportAuditLogsPdf());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Logs");
        btnRefresh.addActionListener(e -> loadActivityLogs());

        toolbar.add(new JLabel("Severity:"));
        toolbar.add(cmbLogSeverity);
        toolbar.add(txtLogSearch);
        toolbar.add(btnExportPdf);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Timestamp", "User", "Role", "Action Performed", "Entity", "Entity ID", "IP Address", "Severity", "Audit Details"};
        logTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(logTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void filterActivityLogs() {
        if (cachedAuditLogs == null) return;
        String sev = (String) cmbLogSeverity.getSelectedItem();
        String query = txtLogSearch.getText().trim().toLowerCase();

        logTableModel.setRowCount(0);
        for (AuditLog l : cachedAuditLogs) {
            if (sev != null && !sev.equals("All Severities") && !sev.equalsIgnoreCase(l.getSeverity().name())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchU = l.getUsername() != null && l.getUsername().toLowerCase().contains(query);
                boolean matchA = l.getAction() != null && l.getAction().toLowerCase().contains(query);
                boolean matchD = l.getDetails() != null && l.getDetails().toLowerCase().contains(query);
                if (!matchU && !matchA && !matchD) continue;
            }

            logTableModel.addRow(new Object[]{
                    l.getId(),
                    l.getTimestamp() != null ? l.getTimestamp().toString() : "-",
                    l.getUsername(),
                    l.getRole(),
                    l.getAction(),
                    l.getEntityType() != null ? l.getEntityType() : "-",
                    l.getEntityId() != null ? l.getEntityId() : "-",
                    l.getIpAddress() != null ? l.getIpAddress() : "127.0.0.1",
                    l.getSeverity().name(),
                    l.getDetails() != null ? l.getDetails() : "-"
            });
        }
    }

    private void exportAuditLogsPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Institutional_Audit_Trail_Register.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PdfReportGenerator.generateAuditLogsReport(chooser.getSelectedFile(), cachedAuditLogs);
                lastGeneratedPdfFile = chooser.getSelectedFile();
                JOptionPane.showMessageDialog(this, "Audit logs PDF successfully exported!");
                Desktop.getDesktop().open(chooser.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting audit logs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // ASYNCHRONOUS DATA LOADER & REFRESH LOGIC
    // =========================================================================

    private void loadAllDataAsync() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    cachedStudents = studentDAO.getAllStudents();
                    cachedFaculty = facultyDAO.getAllFaculty();
                    cachedDepartments = departmentDAO.getAllDepartments();
                    cachedClasses = classDAO.getAllClasses();
                    cachedLeaves = dutyLeaveDAO.getAllLeaves();
                    cachedGrievances = grievanceDAO.getAllGrievances();
                    cachedPublications = publicationDAO.getAllPublications();
                    cachedAnnouncements = announcementDAO.getAllAnnouncements();
                    cachedCalendarEvents = calendarDAO.getAllEvents();
                    cachedNotifications = notificationDAO.getAllNotifications();
                    cachedAuditLogs = auditDAO.getRecentLogs(250, "ALL");
                } catch (Exception e) {
                    System.err.println("Error loading background portal data: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                updateOverviewKpisAndCharts();
                populateDropdownFilters();
                filterAnalyticsTable();
                filterPublications();
                loadDepartments();
                filterFacultyTable();
                filterDutyLeaves();
                filterGrievances();
                filterAnnouncements();
                filterCalendarEvents();
                filterNotifications();
                filterActivityLogs();
            }
        }.execute();
    }

    private void updateOverviewKpisAndCharts() {
        if (cachedStudents == null) return;

        // KPI Counts
        lblStatEnrollment.setText(String.valueOf(cachedStudents.size()));
        lblStatFaculty.setText(String.valueOf(cachedFaculty.size()));
        lblStatDepts.setText(String.valueOf(cachedDepartments.size()));

        long pendingLeaves = cachedLeaves.stream().filter(l -> l.getStatus() == DutyLeave.Status.ENDORSED_BY_HOD).count();
        lblStatPendingLeaves.setText(String.valueOf(pendingLeaves));

        long pendingGrievances = cachedGrievances.stream().filter(g -> g.getStatus() == Grievance.Status.SUBMITTED || g.getStatus() == Grievance.Status.UNDER_REVIEW).count();
        lblStatPendingGrievances.setText(String.valueOf(pendingGrievances));

        long pendingResults = cachedPublications.stream().filter(p -> p.getStatus() == ResultPublication.PublicationStatus.VERIFIED).count();
        lblStatPendingResults.setText(String.valueOf(pendingResults));

        long publishedResults = cachedPublications.stream().filter(p -> p.getStatus() == ResultPublication.PublicationStatus.PUBLISHED).count();
        lblStatPublishedResults.setText(String.valueOf(publishedResults));

        lblStatActiveCirculars.setText(String.valueOf(cachedAnnouncements.size()));

        // Update badge counters on sidebar
        if (pendingLeaves > 0) {
            lblPendingLeaveBadge.setText(String.valueOf(pendingLeaves));
            lblPendingLeaveBadge.setVisible(true);
        } else {
            lblPendingLeaveBadge.setVisible(false);
        }

        if (pendingGrievances > 0) {
            lblPendingGrievanceBadge.setText(String.valueOf(pendingGrievances));
            lblPendingGrievanceBadge.setVisible(true);
        } else {
            lblPendingGrievanceBadge.setVisible(false);
        }

        if (pendingResults > 0) {
            lblPendingResultBadge.setText(String.valueOf(pendingResults));
            lblPendingResultBadge.setVisible(true);
        } else {
            lblPendingResultBadge.setVisible(false);
        }

        // Overview Department Bar Chart
        List<BarItem> deptBars = new ArrayList<>();
        double maxDeptCgpa = 10.0;
        for (Department d : cachedDepartments) {
            List<Student> dStus = cachedStudents.stream().filter(s -> d.getName().equalsIgnoreCase(s.getDepartmentName())).toList();
            double avgCgpa = dStus.stream().mapToDouble(Student::getCurrentCgpa).average().orElse(0.0);
            deptBars.add(new BarItem(
                    d.getCode(),
                    avgCgpa,
                    String.format("%.2f CGPA (%d stus)", avgCgpa, dStus.size()),
                    new Color(99, 102, 241),
                    new Color(139, 92, 246)
            ));
        }
        overviewDeptBarChart.setData(deptBars, maxDeptCgpa);

        // Overview Standing Donut Chart
        long distinction = cachedStudents.stream().filter(s -> s.getCurrentCgpa() >= 8.5).count();
        long firstClass = cachedStudents.stream().filter(s -> s.getCurrentCgpa() >= 7.0 && s.getCurrentCgpa() < 8.5).count();
        long secondClass = cachedStudents.stream().filter(s -> s.getCurrentCgpa() >= 5.0 && s.getCurrentCgpa() < 7.0).count();
        long atRisk = cachedStudents.stream().filter(s -> s.getCurrentCgpa() < 5.0 || s.getAttendancePercentage() < 75.0).count();

        List<Segment> segments = new ArrayList<>();
        if (distinction > 0) segments.add(new Segment("Distinction (>= 8.5)", distinction, new Color(245, 158, 11)));
        if (firstClass > 0) segments.add(new Segment("First Class (7.0 - 8.49)", firstClass, new Color(16, 185, 129)));
        if (secondClass > 0) segments.add(new Segment("Second Class (5.0 - 6.99)", secondClass, new Color(59, 130, 246)));
        if (atRisk > 0) segments.add(new Segment("Academic Warning / Low Att", atRisk, new Color(239, 68, 68)));
        overviewStandingDonutChart.setData(segments);

        // Analytics Tab Charts
        analyticsCgpaBarChart.setData(deptBars, maxDeptCgpa);

        long attSafe = cachedStudents.stream().filter(s -> s.getAttendancePercentage() >= 85.0).count();
        long attModerate = cachedStudents.stream().filter(s -> s.getAttendancePercentage() >= 75.0 && s.getAttendancePercentage() < 85.0).count();
        long attCritical = cachedStudents.stream().filter(s -> s.getAttendancePercentage() < 75.0).count();

        List<Segment> attSegments = new ArrayList<>();
        if (attSafe > 0) attSegments.add(new Segment("Safe Attendance (>= 85%)", attSafe, new Color(16, 185, 129)));
        if (attModerate > 0) attSegments.add(new Segment("Moderate (75% - 84%)", attModerate, new Color(245, 158, 11)));
        if (attCritical > 0) attSegments.add(new Segment("Critical Shortage (< 75%)", attCritical, new Color(239, 68, 68)));
        analyticsAttendanceDonutChart.setData(attSegments);
    }

    private void populateDropdownFilters() {
        // Analytics Department Filter
        if (cmbAnalyticsDept != null && cmbAnalyticsDept.getItemCount() <= 1) {
            for (Department d : cachedDepartments) {
                cmbAnalyticsDept.addItem(d.getName());
            }
        }

        // Analytics Class Filter
        if (cmbAnalyticsClass != null && cmbAnalyticsClass.getItemCount() <= 1) {
            for (ClassBatch c : cachedClasses) {
                cmbAnalyticsClass.addItem(c.getName());
            }
        }

        // Faculty Department Filter
        if (cmbFacDept != null && cmbFacDept.getItemCount() <= 1) {
            for (Department d : cachedDepartments) {
                cmbFacDept.addItem(d.getName());
            }
        }

        // Reports Class Filter
        if (cmbReportClass != null && cmbReportClass.getItemCount() == 0) {
            for (ClassBatch cb : cachedClasses) {
                cmbReportClass.addItem(cb);
            }
        }
    }

    private void loadAcademicAnalytics() {
        new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() {
                return studentDAO.getAllStudents();
            }

            @Override
            protected void done() {
                try {
                    cachedStudents = get();
                    filterAnalyticsTable();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadResultPublications() {
        new SwingWorker<List<ResultPublication>, Void>() {
            @Override
            protected List<ResultPublication> doInBackground() {
                return publicationDAO.getAllPublications();
            }

            @Override
            protected void done() {
                try {
                    cachedPublications = get();
                    filterPublications();
                    updateOverviewKpisAndCharts();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadDepartments() {
        deptTableModel.setRowCount(0);
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            long stuCount = cachedStudents.stream().filter(s -> d.getName().equalsIgnoreCase(s.getDepartmentName())).count();
            long facCount = cachedFaculty.stream().filter(f -> d.getName().equalsIgnoreCase(f.getDepartmentName())).count();
            deptTableModel.addRow(new Object[]{
                    d.getId(),
                    d.getCode(),
                    d.getName(),
                    d.getHodName() != null ? d.getHodName() : "⚠️ Unassigned",
                    "Ph.D. / Senior Academic",
                    facCount + " Faculty",
                    stuCount + " Students"
            });
        }
    }

    private void loadFaculty() {
        new SwingWorker<List<Faculty>, Void>() {
            @Override
            protected List<Faculty> doInBackground() {
                return facultyDAO.getAllFaculty();
            }

            @Override
            protected void done() {
                try {
                    cachedFaculty = get();
                    filterFacultyTable();
                    updateOverviewKpisAndCharts();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadDutyLeaves() {
        new SwingWorker<List<DutyLeave>, Void>() {
            @Override
            protected List<DutyLeave> doInBackground() {
                return dutyLeaveDAO.getAllLeaves();
            }

            @Override
            protected void done() {
                try {
                    cachedLeaves = get();
                    filterDutyLeaves();
                    updateOverviewKpisAndCharts();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadGrievances() {
        new SwingWorker<List<Grievance>, Void>() {
            @Override
            protected List<Grievance> doInBackground() {
                return grievanceDAO.getAllGrievances();
            }

            @Override
            protected void done() {
                try {
                    cachedGrievances = get();
                    filterGrievances();
                    updateOverviewKpisAndCharts();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadAnnouncements() {
        new SwingWorker<List<Announcement>, Void>() {
            @Override
            protected List<Announcement> doInBackground() {
                return announcementDAO.getAllAnnouncements();
            }

            @Override
            protected void done() {
                try {
                    cachedAnnouncements = get();
                    filterAnnouncements();
                    updateOverviewKpisAndCharts();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadCalendarEvents() {
        new SwingWorker<List<AcademicEvent>, Void>() {
            @Override
            protected List<AcademicEvent> doInBackground() {
                return calendarDAO.getAllEvents();
            }

            @Override
            protected void done() {
                try {
                    cachedCalendarEvents = get();
                    filterCalendarEvents();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadNotifications() {
        new SwingWorker<List<Notification>, Void>() {
            @Override
            protected List<Notification> doInBackground() {
                return notificationDAO.getAllNotifications();
            }

            @Override
            protected void done() {
                try {
                    cachedNotifications = get();
                    filterNotifications();
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadActivityLogs() {
        new SwingWorker<List<AuditLog>, Void>() {
            @Override
            protected List<AuditLog> doInBackground() {
                return auditDAO.getRecentLogs(250, "ALL");
            }

            @Override
            protected void done() {
                try {
                    cachedAuditLogs = get();
                    filterActivityLogs();
                } catch (Exception ex) {}
            }
        }.execute();
    }
}
