package com.acadexia.ui.faculty;

import com.acadexia.dao.*;
import com.acadexia.model.*;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.common.MainFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Faculty dashboard — extends MainFrame.
 *
 * Sidebar nav (role-adaptive):
 *   Overview · Teaching · [CFA Portal] · [HOD Portal]
 *
 * CFA and HOD tabs appear only when the logged-in faculty holds those roles.
 * All DAO calls are preserved exactly from the original.
 */
public class FacultyDashboardFrame extends MainFrame {

    // ── current faculty ───────────────────────────────────────────────────────
    private final Faculty faculty;

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final SubjectDAO    subjectDAO    = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MarkDAO       markDAO       = new MarkDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final DutyLeaveDAO  dutyLeaveDAO  = new DutyLeaveDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();
    private final FeedbackDAO   feedbackDAO   = new FeedbackDAO();
    private final TimetableDAO  timetableDAO  = new TimetableDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final FacultyDAO    facultyDAO    = new FacultyDAO();

    // ── course selector (shared across teaching sub-panels) ───────────────────
    private JComboBox<SubjectDAO.AssignedCourse> cmbCourses;
    private List<SubjectDAO.AssignedCourse>      assignedCourses;

    // ── table models ──────────────────────────────────────────────────────────
    private DefaultTableModel attendanceModel;
    private DefaultTableModel marksModel;
    private DefaultTableModel assignModel;
    private DefaultTableModel feedbackModel;
    private DefaultTableModel cfaStudentModel;
    private DefaultTableModel cfaLeaveModel;
    private DefaultTableModel hodLeaveModel;

    // ── live data for leave processing ────────────────────────────────────────
    private List<DutyLeave> cfaPendingLeaves;
    private List<DutyLeave> hodPendingLeaves;

    // ── constructor ───────────────────────────────────────────────────────────
    public FacultyDashboardFrame() {
        super("Acadexia – Faculty Portal", 1260, 780);
        this.faculty = SessionManager.getInstance().getCurrentFaculty();

        int facId = faculty != null ? faculty.getId() : 1;
        assignedCourses = subjectDAO.getAssignedCoursesForFaculty(facId);

        // Universal panels
        addNavItem("🏠", "Overview",      "overview",  buildOverviewPanel());
        addNavItem("📚", "My Teaching",   "teaching",  buildTeachingPanel());

        // Role-conditional panels
        if (faculty != null && faculty.isCfa()) {
            addNavItem("📋", "Class Advisor",  "cfa",  buildCfaPanel());
        }
        if (faculty != null && faculty.isHod()) {
            addNavItem("🏛️", "HOD Portal",     "hod",  buildHodPanel());
        }

        showDefaultPanel();
        SwingUtilities.invokeLater(this::loadAllData);
    }

    // =========================================================================
    // OVERVIEW
    // =========================================================================
    private JPanel buildOverviewPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 24));

        String facName = faculty != null ? faculty.getFullName()      : "Faculty";
        String facDept = faculty != null ? faculty.getDepartmentName(): "—";
        String facId   = faculty != null ? faculty.getFacultyId()     : "—";

        // Greeting
        JPanel greeting = new JPanel(new BorderLayout(0, 4));
        greeting.setOpaque(false);

        JLabel lblHi = new JLabel("Welcome, " + facName.split(" ")[0] + " 👋");
        lblHi.setFont(UITheme.FONT_TITLE);
        lblHi.setForeground(UITheme.TEXT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setOpaque(false);
        badgeRow.add(UIComponents.createBadge(facId, UITheme.PRIMARY_LIGHT, UITheme.PRIMARY));
        badgeRow.add(UIComponents.createBadge(facDept, new Color(0xF1F5F9), UITheme.TEXT_MUTED));
        if (faculty != null && faculty.isCfa()) {
            badgeRow.add(UIComponents.createBadge("CFA · " + faculty.getAdvisedClassName(),
                    UITheme.SUCCESS_LIGHT, UITheme.SUCCESS));
        }
        if (faculty != null && faculty.isHod()) {
            badgeRow.add(UIComponents.createBadge("HOD · " + facDept,
                    UITheme.WARNING_LIGHT, UITheme.WARNING));
        }

        greeting.add(lblHi,      BorderLayout.NORTH);
        greeting.add(badgeRow,   BorderLayout.SOUTH);

        // Stat cards
        int courseCount = assignedCourses != null ? assignedCourses.size() : 0;
        int pendCfa = 0, pendHod = 0;
        if (faculty != null && faculty.isCfa() && faculty.getAdvisedClassId() != null) {
            pendCfa = dutyLeaveDAO.getPendingLeavesForAdvisor(faculty.getAdvisedClassId()).size();
        }
        if (faculty != null && faculty.isHod()) {
            pendHod = dutyLeaveDAO.getPendingLeavesForHod(faculty.getDepartmentId()).size();
        }

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(UIComponents.createStatCard("Assigned Courses", String.valueOf(courseCount),  "This academic year",  UITheme.PRIMARY));
        cards.add(UIComponents.createStatCard("CFA Pending",      String.valueOf(pendCfa),      "Duty leaves awaiting your review", pendCfa > 0 ? UITheme.DANGER : UITheme.SUCCESS));
        cards.add(UIComponents.createStatCard("HOD Pending",      String.valueOf(pendHod),      "Leaves to endorse",   pendHod > 0 ? UITheme.WARNING : UITheme.SUCCESS));
        cards.add(UIComponents.createStatCard("Designation",
                faculty != null ? faculty.getDesignation() : "—", facDept, UITheme.INFO));

        // Quick actions
        JPanel qaCard = UIComponents.createCardPanel();
        qaCard.setLayout(new BorderLayout(0, 14));
        JPanel qaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qaRow.setOpaque(false);

        JButton btnTeach  = UIComponents.createPrimaryButton("📚  Go to Teaching");
        JButton btnCfa    = UIComponents.createSecondaryButton("📋  Class Advisor");
        JButton btnHod    = UIComponents.createSecondaryButton("🏛️  HOD Portal");

        btnTeach.addActionListener(e -> showPanel("teaching"));
        btnCfa  .addActionListener(e -> showPanel("cfa"));
        btnHod  .addActionListener(e -> showPanel("hod"));

        btnCfa.setVisible(faculty != null && faculty.isCfa());
        btnHod.setVisible(faculty != null && faculty.isHod());

        qaRow.add(btnTeach);
        qaRow.add(btnCfa);
        qaRow.add(btnHod);

        qaCard.add(UIComponents.createSectionLabel("Quick Actions"), BorderLayout.NORTH);
        qaCard.add(qaRow, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(cards);
        body.add(Box.createVerticalStrut(24));
        body.add(qaCard);

        page.add(greeting, BorderLayout.NORTH);
        page.add(body,     BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // TEACHING PANEL
    // =========================================================================
    private JPanel buildTeachingPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("My Teaching",
                "Manage attendance, marks, assignments, and feedback for your assigned courses."),
                BorderLayout.NORTH);

        // Course selector bar
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectorBar.setOpaque(false);
        selectorBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblSel = UIComponents.createFormLabel("Active Course:");
        cmbCourses = UIComponents.createComboBox(
                assignedCourses.toArray(new SubjectDAO.AssignedCourse[0]));
        cmbCourses.setPreferredSize(new Dimension(320, 34));
        cmbCourses.addActionListener(e -> refreshCourseData());

        selectorBar.add(lblSel);
        selectorBar.add(cmbCourses);

        // Sub-tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("📝  Attendance Register",    buildAttendanceSubPanel());
        tabs.addTab("📊  Internal Marks Entry",   buildMarksSubPanel());
        tabs.addTab("📁  Assignments",             buildAssignmentsSubPanel());
        tabs.addTab("💬  Student Feedback",        buildFeedbackSubPanel());
        tabs.addTab("🕒  My Timetable",            buildTimetableSubPanel());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(selectorBar, BorderLayout.NORTH);
        center.add(tabs,        BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);

        // Initial load after frame is shown
        SwingUtilities.invokeLater(this::refreshCourseData);
        return page;
    }

    // ── attendance register ───────────────────────────────────────────────────
    private JPanel buildAttendanceSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        JTextField txtDate = UIComponents.createTextField(LocalDate.now().toString());
        txtDate.setPreferredSize(new Dimension(130, 34));
        JComboBox<Integer> cmbHour = UIComponents.createComboBox(new Integer[]{1,2,3,4,5,6,7});

        JButton btnLoad    = UIComponents.createSecondaryButton("Load Register");
        JButton btnAllPres = UIComponents.createSecondaryButton("✓ Mark All Present");
        JButton btnSave    = UIComponents.createSuccessButton("💾  Save Attendance");

        btnLoad   .addActionListener(e ->
                loadAttendanceRegister(txtDate.getText().trim(), (Integer) cmbHour.getSelectedItem()));
        btnAllPres.addActionListener(e -> {
            for (int r = 0; r < attendanceModel.getRowCount(); r++)
                attendanceModel.setValueAt("PRESENT", r, 4);
        });
        btnSave.addActionListener(e ->
                saveAttendance(txtDate.getText().trim(), (Integer) cmbHour.getSelectedItem()));

        JPanel toolbar = UIComponents.createToolbar(
                new JLabel("Date:"), txtDate,
                new JLabel("Hour:"), cmbHour,
                btnLoad, btnAllPres, btnSave);

        String[] cols = {"Student ID", "Roll", "Reg No", "Name",
                         "Status (PRESENT/ABSENT/DUTY_LEAVE)", "Remarks"};
        attendanceModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4 || c == 5; }
        };
        JTable table = UIComponents.createStyledTable(attendanceModel);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAttendanceRegister(String dateStr, int hour) {
        if (attendanceModel == null) return;
        attendanceModel.setRowCount(0);
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;
        try {
            Date d = Date.valueOf(dateStr);
            List<AttendanceDAO.StudentAttendanceEntry> list =
                    attendanceDAO.getAttendanceEntrySheet(sel.classId(), sel.subjectId(), d, hour);
            for (AttendanceDAO.StudentAttendanceEntry e : list) {
                attendanceModel.addRow(new Object[]{
                    e.studentId(), e.rollNumber(), e.registerNumber(), e.fullName(),
                    e.status().name(), e.remarks() != null ? e.remarks() : ""
                });
            }
        } catch (Exception ex) {
            UIComponents.showError(this, "Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private void saveAttendance(String dateStr, int hour) {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;
        try {
            Date d = Date.valueOf(dateStr);
            int facId = faculty != null ? faculty.getId() : 1;
            int count = 0;
            for (int r = 0; r < attendanceModel.getRowCount(); r++) {
                int    stuId   = (int)    attendanceModel.getValueAt(r, 0);
                String statStr = attendanceModel.getValueAt(r, 4).toString().toUpperCase().trim();
                Attendance.Status status = Attendance.Status.valueOf(statStr);
                String rem = attendanceModel.getValueAt(r, 5) != null
                        ? attendanceModel.getValueAt(r, 5).toString() : "";
                if (attendanceDAO.recordAttendance(stuId, sel.subjectId(),
                        sel.classId(), d, hour, status, facId, rem)) count++;
            }
            UIComponents.showSuccessToast(this, "Attendance saved for " + count + " students.");
        } catch (Exception ex) {
            UIComponents.showError(this, "Error saving attendance: " + ex.getMessage());
        }
    }

    // ── marks entry ───────────────────────────────────────────────────────────
    private JPanel buildMarksSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        JComboBox<Mark.ExamType> cmbExam = UIComponents.createComboBox(Mark.ExamType.values());
        JButton btnLoad = UIComponents.createSecondaryButton("Load Sheet");
        JButton btnSave = UIComponents.createSuccessButton("💾  Save Marks");

        btnLoad.addActionListener(e ->
                loadMarksEntry((Mark.ExamType) cmbExam.getSelectedItem()));
        btnSave.addActionListener(e ->
                saveMarks((Mark.ExamType) cmbExam.getSelectedItem()));

        JPanel toolbar = UIComponents.createToolbar(
                new JLabel("Evaluation Type:"), cmbExam, btnLoad, btnSave);

        String[] cols = {"Student ID", "Roll", "Reg No", "Name", "Marks Obtained", "Max Marks"};
        marksModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        JTable table = UIComponents.createStyledTable(marksModel);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadMarksEntry(Mark.ExamType examType) {
        if (marksModel == null) return;
        marksModel.setRowCount(0);
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;
        List<MarkDAO.StudentMarksEntryRow> rows =
                markDAO.getClassSubjectMarks(sel.classId(), sel.subjectId(), examType);
        for (MarkDAO.StudentMarksEntryRow r : rows) {
            marksModel.addRow(new Object[]{
                r.studentId(), r.rollNumber(), r.registerNumber(), r.fullName(),
                r.marksObtained() != null ? r.marksObtained() : 0.0,
                r.maxMarks()
            });
        }
    }

    private void saveMarks(Mark.ExamType examType) {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;
        int facId = faculty != null ? faculty.getId() : 1;
        int count = 0;
        for (int r = 0; r < marksModel.getRowCount(); r++) {
            int    stuId = (int)    marksModel.getValueAt(r, 0);
            double marks = Double.parseDouble(marksModel.getValueAt(r, 4).toString());
            double max   = Double.parseDouble(marksModel.getValueAt(r, 5).toString());
            if (markDAO.recordMark(stuId, sel.subjectId(), examType, marks, max,
                    Date.valueOf(LocalDate.now()), facId)) count++;
        }
        UIComponents.showSuccessToast(this, "Marks saved for " + count + " students.");
    }

    // ── assignments ───────────────────────────────────────────────────────────
    private JPanel buildAssignmentsSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnNew     = UIComponents.createPrimaryButton("➕  Post Assignment");
        JButton btnGrade   = UIComponents.createSecondaryButton("🏆  Grade Submissions");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");

        btnNew    .addActionListener(e -> showPostAssignmentDialog());
        btnGrade  .addActionListener(e -> showGradeSubmissionsDialog());
        btnRefresh.addActionListener(e -> loadAssignments());

        String[] cols = {"ID", "Title", "Due Date", "Max Marks", "Submissions", "Class"};
        assignModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(assignModel);

        p.add(UIComponents.createToolbar(btnNew, btnGrade, btnRefresh), BorderLayout.NORTH);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAssignments() {
        if (assignModel == null) return;
        assignModel.setRowCount(0);
        int facId = faculty != null ? faculty.getId() : 1;
        List<Assignment> list = assignmentDAO.getAssignmentsByFaculty(facId);
        for (Assignment a : list) {
            assignModel.addRow(new Object[]{
                a.getId(), a.getTitle(), a.getDueDate(), a.getMaxMarks(),
                a.getTotalSubmissions() + " / " + a.getTotalStudents(),
                a.getClassName()
            });
        }
    }

    private void showPostAssignmentDialog() {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) { UIComponents.showError(this, "Please select a course first."); return; }

        JDialog dlg = new JDialog(this, "Post New Assignment", true);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.SURFACE);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        JTextField txtTitle = UIComponents.createTextField("Assignment title");
        JTextArea  txtDesc  = new JTextArea(3, 30);
        txtDesc.setFont(UITheme.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
        JTextField txtDue = UIComponents.createTextField(LocalDate.now().plusDays(7).toString());
        JTextField txtMax = UIComponents.createTextField("10.0");

        addFRow(form, "Title",                  txtTitle);
        form.add(Box.createVerticalStrut(10));
        JLabel lDesc = UIComponents.createFormLabel("Description");
        lDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lDesc);
        form.add(Box.createVerticalStrut(4));
        JScrollPane dSp = new JScrollPane(txtDesc);
        dSp.setAlignmentX(Component.LEFT_ALIGNMENT);
        dSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        form.add(dSp);
        form.add(Box.createVerticalStrut(10));
        addFRow(form, "Due Date (YYYY-MM-DD)",  txtDue);
        form.add(Box.createVerticalStrut(10));
        addFRow(form, "Max Marks",              txtMax);

        JPanel footer = buildDlgFooter(dlg, () -> {
            try {
                Assignment a = new Assignment();
                a.setTitle(txtTitle.getText().trim());
                a.setDescription(txtDesc.getText().trim());
                a.setSubjectId(sel.subjectId());
                a.setClassId(sel.classId());
                a.setFacultyId(faculty != null ? faculty.getId() : 1);
                a.setDueDate(Date.valueOf(txtDue.getText().trim()));
                a.setMaxMarks(Double.parseDouble(txtMax.getText().trim()));
                if (assignmentDAO.createAssignment(a)) {
                    loadAssignments();
                    dlg.dispose();
                    UIComponents.showSuccessToast(this, "Assignment posted successfully.");
                }
            } catch (Exception ex) {
                UIComponents.showError(dlg, "Error: " + ex.getMessage());
            }
        });

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showGradeSubmissionsDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Assignment ID to view submissions:", "Grade Submissions", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int aid  = Integer.parseInt(input.trim());
            List<AssignmentSubmission> subs = assignmentDAO.getSubmissionsForAssignment(aid);
            if (subs.isEmpty()) {
                UIComponents.showError(this, "No submissions found for Assignment #" + aid);
                return;
            }

            JDialog dlg = new JDialog(this, "Submissions — Assignment #" + aid, true);
            dlg.setSize(860, 480);
            dlg.setLocationRelativeTo(this);

            String[] cols = {"ID","Roll","Reg No","Student","Status","Marks","Notes"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (AssignmentSubmission s : subs) {
                model.addRow(new Object[]{
                    s.getId(), s.getRollNumber(), s.getRegisterNumber(), s.getStudentName(),
                    s.getStatus().name(),
                    s.getMarksObtained() != null ? s.getMarksObtained() : "—",
                    s.getSubmissionText()
                });
            }
            JTable tbl = UIComponents.createStyledTable(model);

            JButton btnGrade = UIComponents.createPrimaryButton("Award Marks to Selected Row");
            btnGrade.addActionListener(e -> {
                int row = tbl.getSelectedRow();
                if (row < 0) { UIComponents.showError(dlg, "Select a row first."); return; }
                int subId = (int) model.getValueAt(row, 0);
                String mStr = JOptionPane.showInputDialog(dlg, "Enter marks awarded:");
                String fbTxt = JOptionPane.showInputDialog(dlg, "Enter feedback / remarks:");
                if (mStr != null && !mStr.trim().isEmpty()) {
                    double awarded = Double.parseDouble(mStr.trim());
                    assignmentDAO.gradeSubmission(subId, awarded, fbTxt != null ? fbTxt : "");
                    model.setValueAt(awarded,    row, 5);
                    model.setValueAt("GRADED",   row, 4);
                }
            });

            dlg.setLayout(new BorderLayout());
            dlg.add(UIComponents.createTableScrollPane(tbl), BorderLayout.CENTER);
            dlg.add(btnGrade, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid Assignment ID.");
        }
    }

    // ── student feedback view ─────────────────────────────────────────────────
    private JPanel buildFeedbackSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnRefresh.addActionListener(e -> loadFeedback());

        String[] cols = {"Subject", "Teaching (★)", "Punctuality (★)", "Clarity (★)", "Average", "Comments"};
        feedbackModel = new DefaultTableModel(cols, 0);
        JTable table  = UIComponents.createStyledTable(feedbackModel);

        p.add(UIComponents.createToolbar(btnRefresh), BorderLayout.NORTH);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadFeedback() {
        if (feedbackModel == null) return;
        feedbackModel.setRowCount(0);
        int facId = faculty != null ? faculty.getId() : 1;
        List<Feedback> list = feedbackDAO.getFeedbackForFaculty(facId);
        for (Feedback fb : list) {
            feedbackModel.addRow(new Object[]{
                fb.getSubjectCode() + " · " + fb.getSubjectName(),
                fb.getRatingTeaching()     + " ★",
                fb.getRatingPunctuality()  + " ★",
                fb.getRatingClarity()      + " ★",
                String.format("%.1f ★", fb.getAverageRating()),
                fb.getComments()
            });
        }
    }

    // ── timetable ─────────────────────────────────────────────────────────────
    private JPanel buildTimetableSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 0, 0, 0));

        String[] cols = {"Day","Period","Subject Code","Subject Name","Class / Section","Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        int facId = faculty != null ? faculty.getId() : 1;
        List<TimetableSlot> slots = timetableDAO.getTimetableForFaculty(facId);
        for (TimetableSlot s : slots) {
            model.addRow(new Object[]{
                s.getDayOfWeek().getLabel(), "Period " + s.getPeriodSlot(),
                s.getSubjectCode(), s.getSubjectName(),
                s.getClassName(), s.getRoomNumber()
            });
        }
        JTable table = UIComponents.createStyledTable(model);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshCourseData() {
        SubjectDAO.AssignedCourse sel = cmbCourses != null
                ? (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem() : null;
        if (sel != null) {
            loadAttendanceRegister(LocalDate.now().toString(), 1);
            loadMarksEntry(Mark.ExamType.SERIES_1);
            loadAssignments();
            loadFeedback();
        }
    }

    // =========================================================================
    // CFA PANEL
    // =========================================================================
    private JPanel buildCfaPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));

        String className = faculty != null ? faculty.getAdvisedClassName() : "—";
        page.add(UIComponents.createHeader("Class Advisor Portal",
                "Advised class: " + className + " · Tier-1 duty leave recommendations."),
                BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("👥  Class Students",         buildCfaStudentsSubPanel());
        tabs.addTab("📋  Duty Leave (Tier-1)",    buildCfaDutyLeaveSubPanel());

        page.add(tabs, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildCfaStudentsSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        String[] cols = {"Roll","Reg No","Name","Attendance %","SGPA","CGPA","Phone","Guardian"};
        cfaStudentModel = new DefaultTableModel(cols, 0);

        if (faculty != null && faculty.getAdvisedClassId() != null) {
            List<Student> students = studentDAO.getStudentsByClass(faculty.getAdvisedClassId());
            for (Student s : students) {
                cfaStudentModel.addRow(new Object[]{
                    s.getRollNumber(), s.getRegisterNumber(), s.getFullName(),
                    s.getAttendancePercentage() + "%", s.getCurrentSgpa(), s.getCurrentCgpa(),
                    s.getPhone(), s.getGuardianName()
                });
            }
        }

        JTable table = UIComponents.createStyledTable(cfaStudentModel);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCfaDutyLeaveSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JButton btnRecommend = UIComponents.createSuccessButton("👍  Recommend to HOD");
        JButton btnReject    = UIComponents.createDangerButton("❌  Reject");
        JButton btnRefresh   = UIComponents.createSecondaryButton("↻  Refresh");

        btnRecommend.addActionListener(e -> processCfaDutyLeave(true));
        btnReject   .addActionListener(e -> processCfaDutyLeave(false));
        btnRefresh  .addActionListener(e -> loadCfaDutyLeaves());

        String[] cols = {"App ID","Reg No","Student Name","Reason","Days","Dates","Proof"};
        cfaLeaveModel = new DefaultTableModel(cols, 0);
        JTable table  = UIComponents.createStyledTable(cfaLeaveModel);

        loadCfaDutyLeaves();

        p.add(UIComponents.createToolbar(btnRecommend, btnReject, btnRefresh), BorderLayout.NORTH);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadCfaDutyLeaves() {
        if (cfaLeaveModel == null || faculty == null || faculty.getAdvisedClassId() == null) return;
        cfaLeaveModel.setRowCount(0);
        cfaPendingLeaves = dutyLeaveDAO.getPendingLeavesForAdvisor(faculty.getAdvisedClassId());
        for (DutyLeave dl : cfaPendingLeaves) {
            cfaLeaveModel.addRow(new Object[]{
                dl.getId(), dl.getRegisterNumber(), dl.getStudentName(),
                dl.getReason(), dl.getTotalDays(),
                dl.getStartDate() + " → " + dl.getEndDate(),
                dl.getProofDetails()
            });
        }
    }

    private void processCfaDutyLeave(boolean recommend) {
        String input = JOptionPane.showInputDialog(this,
                "Enter Application ID to " + (recommend ? "RECOMMEND" : "REJECT") + ":",
                recommend ? "Recommend to HOD" : "Reject Application",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int    appId   = Integer.parseInt(input.trim());
            String remarks = JOptionPane.showInputDialog(this, "Enter Advisor Remarks:",
                    recommend ? "Verified and Recommended" : "Application Rejected");
            if (remarks == null) remarks = recommend ? "Recommended" : "Rejected";
            dutyLeaveDAO.updateAdvisorDecision(appId, recommend, remarks, faculty.getFacultyId());
            loadCfaDutyLeaves();
            UIComponents.showSuccessToast(this,
                    "Application " + appId + (recommend ? " recommended to HOD." : " rejected."));
        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid Application ID.");
        }
    }

    // =========================================================================
    // HOD PANEL
    // =========================================================================
    private JPanel buildHodPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));

        String deptName = faculty != null ? faculty.getDepartmentName() : "—";
        page.add(UIComponents.createHeader("HOD Governance Portal",
                "Department: " + deptName + " · Tier-2 endorsement & faculty assignments."),
                BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("📋  Duty Leave (Tier-2)",       buildHodDutyLeaveSubPanel());
        tabs.addTab("👔  Assign Class Advisor (CFA)", buildHodAssignCfaSubPanel());
        tabs.addTab("📖  Assign Subject Teachers",    buildHodAssignTeacherSubPanel());

        page.add(tabs, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildHodDutyLeaveSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JButton btnEndorse = UIComponents.createSuccessButton("✍️  Endorse to Principal");
        JButton btnReject  = UIComponents.createDangerButton("❌  Reject");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");

        btnEndorse.addActionListener(e -> processHodDutyLeave(true));
        btnReject .addActionListener(e -> processHodDutyLeave(false));
        btnRefresh.addActionListener(e -> loadHodDutyLeaves());

        String[] cols = {"App ID","Reg No","Student","Class","Reason","Days","CFA Remarks"};
        hodLeaveModel = new DefaultTableModel(cols, 0);
        JTable table  = UIComponents.createStyledTable(hodLeaveModel);

        loadHodDutyLeaves();

        p.add(UIComponents.createToolbar(btnEndorse, btnReject, btnRefresh), BorderLayout.NORTH);
        p.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadHodDutyLeaves() {
        if (hodLeaveModel == null || faculty == null) return;
        hodLeaveModel.setRowCount(0);
        hodPendingLeaves = dutyLeaveDAO.getPendingLeavesForHod(faculty.getDepartmentId());
        for (DutyLeave dl : hodPendingLeaves) {
            hodLeaveModel.addRow(new Object[]{
                dl.getId(), dl.getRegisterNumber(), dl.getStudentName(),
                dl.getClassName(), dl.getReason(), dl.getTotalDays(),
                dl.getAdvisorRemarks() != null ? dl.getAdvisorRemarks() : "—"
            });
        }
    }

    private void processHodDutyLeave(boolean endorse) {
        String input = JOptionPane.showInputDialog(this,
                "Enter Application ID to " + (endorse ? "ENDORSE" : "REJECT") + ":",
                endorse ? "Endorse to Principal" : "Reject Application",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int    appId   = Integer.parseInt(input.trim());
            String remarks = JOptionPane.showInputDialog(this, "Enter HOD Remarks:",
                    endorse ? "Endorsed" : "Rejected by HOD");
            if (remarks == null) remarks = endorse ? "Endorsed" : "Rejected";
            dutyLeaveDAO.updateHodDecision(appId, endorse, remarks, faculty.getFacultyId());
            loadHodDutyLeaves();
            UIComponents.showSuccessToast(this,
                    "Application " + appId + (endorse ? " endorsed to Principal." : " rejected."));
        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid Application ID.");
        }
    }

    private JPanel buildHodAssignCfaSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        List<com.acadexia.model.ClassBatch> classes =
                new com.acadexia.dao.ClassDAO().getClassesByDepartment(faculty.getDepartmentId());
        List<Faculty> facList = facultyDAO.getFacultyByDepartment(faculty.getDepartmentId());

        JComboBox<com.acadexia.model.ClassBatch> cmbClass =
                UIComponents.createComboBox(classes.toArray(new com.acadexia.model.ClassBatch[0]));
        JComboBox<Faculty> cmbFac = UIComponents.createComboBox(facList.toArray(new Faculty[0]));

        addFRow(card, "Select Class",          cmbClass);
        card.add(Box.createVerticalStrut(12));
        addFRow(card, "Appoint as CFA",        cmbFac);
        card.add(Box.createVerticalStrut(16));

        JButton btnAssign = UIComponents.createPrimaryButton("Assign Class Advisor");
        btnAssign.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAssign.addActionListener(e -> {
            com.acadexia.model.ClassBatch cls = (com.acadexia.model.ClassBatch) cmbClass.getSelectedItem();
            Faculty fac = (Faculty) cmbFac.getSelectedItem();
            if (cls == null || fac == null) return;
            new com.acadexia.dao.ClassDAO().assignAdvisor(cls.getId(), fac.getId());
            UIComponents.showSuccessToast(this, fac.getFullName() + " assigned as CFA for " + cls.getName());
        });
        card.add(btnAssign);

        p.add(card, BorderLayout.NORTH);
        return p;
    }

    private JPanel buildHodAssignTeacherSubPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        List<Subject> subjects =
                new com.acadexia.dao.SubjectDAO().getAllSubjects();
        List<com.acadexia.model.ClassBatch> classes =
                new com.acadexia.dao.ClassDAO().getClassesByDepartment(faculty.getDepartmentId());
        List<Faculty> facList = facultyDAO.getAllFaculty();

        JComboBox<Subject> cmbSubj =
                UIComponents.createComboBox(subjects.toArray(new Subject[0]));
        JComboBox<com.acadexia.model.ClassBatch> cmbClass =
                UIComponents.createComboBox(classes.toArray(new com.acadexia.model.ClassBatch[0]));
        JComboBox<Faculty> cmbFac = UIComponents.createComboBox(facList.toArray(new Faculty[0]));
        JTextField txtYear = UIComponents.createTextField(
                String.valueOf(LocalDate.now().getYear()) + "-" + (LocalDate.now().getYear() + 1));

        addFRow(card, "Subject",         cmbSubj);
        card.add(Box.createVerticalStrut(12));
        addFRow(card, "Class",           cmbClass);
        card.add(Box.createVerticalStrut(12));
        addFRow(card, "Assign Faculty",  cmbFac);
        card.add(Box.createVerticalStrut(12));
        addFRow(card, "Academic Year",   txtYear);
        card.add(Box.createVerticalStrut(16));

        JButton btnAssign = UIComponents.createPrimaryButton("Assign Subject Teacher");
        btnAssign.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAssign.addActionListener(e -> {
            Subject  subj  = (Subject)  cmbSubj.getSelectedItem();
            com.acadexia.model.ClassBatch cls = (com.acadexia.model.ClassBatch) cmbClass.getSelectedItem();
            Faculty  fac   = (Faculty)  cmbFac.getSelectedItem();
            String   year  = txtYear.getText().trim();
            if (subj == null || cls == null || fac == null || year.isEmpty()) {
                UIComponents.showError(this, "All fields are required.");
                return;
            }
            boolean ok = subjectDAO.assignTeacherToSubject(subj.getId(), fac.getId(), cls.getId(), year);
            if (ok) UIComponents.showSuccessToast(this, "Assignment saved successfully.");
            else    UIComponents.showError(this, "Assignment already exists or could not be saved.");
        });
        card.add(btnAssign);

        p.add(card, BorderLayout.NORTH);
        return p;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void addFRow(JPanel panel, String labelText, JComponent field) {
        JLabel lbl = UIComponents.createFormLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
    }

    private JPanel buildDlgFooter(JDialog dlg, Runnable onSubmit) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        footer.setBackground(UITheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton btnCancel = UIComponents.createSecondaryButton("Cancel");
        JButton btnSubmit = UIComponents.createPrimaryButton("Submit");
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSubmit.addActionListener(e -> onSubmit.run());
        footer.add(btnCancel);
        footer.add(btnSubmit);
        return footer;
    }

    private void loadAllData() {
        refreshCourseData();
        loadCfaDutyLeaves();
        loadHodDutyLeaves();
    }
}
