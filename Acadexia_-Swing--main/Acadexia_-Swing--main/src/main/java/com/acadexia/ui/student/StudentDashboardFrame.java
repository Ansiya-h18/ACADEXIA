package com.acadexia.ui.student;

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
 * Student dashboard — extends MainFrame.
 *
 * Sidebar navigation:
 *   Overview · Results · Attendance · Duty Leave · Timetable · Assignments · Grievances · Feedback
 *
 * All DAO calls and business logic are preserved exactly from the original.
 */
public class StudentDashboardFrame extends MainFrame {

    // ── current student ───────────────────────────────────────────────────────
    private final Student student;

    // ── DAOs (all unchanged) ──────────────────────────────────────────────────
    private final AttendanceDAO  attendanceDAO  = new AttendanceDAO();
    private final MarkDAO        markDAO        = new MarkDAO();
    private final DutyLeaveDAO   dutyLeaveDAO   = new DutyLeaveDAO();
    private final TimetableDAO   timetableDAO   = new TimetableDAO();
    private final AssignmentDAO  assignmentDAO  = new AssignmentDAO();
    private final GrievanceDAO   grievanceDAO   = new GrievanceDAO();
    private final FeedbackDAO    feedbackDAO    = new FeedbackDAO();
    private final SubjectDAO     subjectDAO     = new SubjectDAO();

    // ── table models ──────────────────────────────────────────────────────────
    private DefaultTableModel marksModel;
    private DefaultTableModel attModel;
    private DefaultTableModel leaveModel;
    private DefaultTableModel assignModel;
    private DefaultTableModel grievModel;
    private DefaultTableModel feedbackModel;

    // ── constructor ───────────────────────────────────────────────────────────
    public StudentDashboardFrame() {
        super("Acadexia – Student Portal", 1200, 760);
        this.student = SessionManager.getInstance().getCurrentStudent();

        addNavItem("🏠", "Overview",     "overview",    buildOverviewPanel());
        addNavItem("📊", "Results",      "results",     buildResultsPanel());
        addNavItem("🕒", "Attendance",   "attendance",  buildAttendancePanel());
        addNavItem("✈️", "Duty Leave",   "dutyleave",   buildDutyLeavePanel());
        addNavItem("📅", "Timetable",    "timetable",   buildTimetablePanel());
        addNavItem("📁", "Assignments",  "assignments", buildAssignmentsPanel());
        addNavItem("⚖️", "Grievances",   "grievances",  buildGrievancesPanel());
        addNavItem("💬", "Feedback",     "feedback",    buildFeedbackPanel());

        showDefaultPanel();
        SwingUtilities.invokeLater(this::loadAllData);
    }

    // =========================================================================
    // OVERVIEW
    // =========================================================================
    private JPanel buildOverviewPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 24));

        // Personalised greeting
        String sName = student != null ? student.getFullName()   : "Student";
        String sReg  = student != null ? student.getRegisterNumber() : "—";
        String sDept = student != null ? student.getDepartmentName() : "—";
        String sClass= student != null ? student.getClassName()   : "—";

        JPanel greeting = new JPanel(new BorderLayout(0, 4));
        greeting.setOpaque(false);

        JLabel lblHi = new JLabel("Welcome back, " + sName.split(" ")[0] + " 👋");
        lblHi.setFont(UITheme.FONT_TITLE);
        lblHi.setForeground(UITheme.TEXT);

        JLabel lblInfo = new JLabel(sReg + "  ·  " + sDept + "  ·  " + sClass);
        lblInfo.setFont(UITheme.FONT_REGULAR);
        lblInfo.setForeground(UITheme.TEXT_MUTED);

        greeting.add(lblHi,   BorderLayout.NORTH);
        greeting.add(lblInfo, BorderLayout.SOUTH);

        // KPI cards
        double attPct = student != null ? student.getAttendancePercentage() : 0.0;
        double sgpa   = student != null ? student.getCurrentSgpa()          : 0.0;
        double cgpa   = student != null ? student.getCurrentCgpa()          : 0.0;
        String roll   = student != null ? student.getRollNumber()            : "—";

        Color attColor = attPct >= 75.0 ? UITheme.SUCCESS : UITheme.DANGER;
        String attSub  = attPct >= 75.0 ? "Eligible for exams" : "⚠ Below 75% — shortage";

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(UIComponents.createStatCard("Attendance",      String.format("%.1f%%", attPct), attSub,                   attColor));
        cards.add(UIComponents.createStatCard("Current SGPA",    String.format("%.2f",   sgpa),   "This semester GPA",      UITheme.PRIMARY));
        cards.add(UIComponents.createStatCard("Cumulative CGPA", String.format("%.2f",   cgpa),   "Overall academic GPA",   UITheme.INFO));
        cards.add(UIComponents.createStatCard("Roll Number",     roll,                            sDept,                    UITheme.WARNING));

        // Quick actions
        JPanel qaCard = UIComponents.createCardPanel();
        qaCard.setLayout(new BorderLayout(0, 14));

        JLabel qaTitle = UIComponents.createSectionLabel("Quick Actions");
        JPanel qaRow   = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qaRow.setOpaque(false);

        JButton btnResults  = UIComponents.createPrimaryButton("📊  View Results");
        JButton btnAtt      = UIComponents.createSecondaryButton("🕒  Attendance");
        JButton btnLeave    = UIComponents.createSecondaryButton("✈️  Apply Duty Leave");
        JButton btnAssign   = UIComponents.createSecondaryButton("📁  Assignments");

        btnResults.addActionListener(e -> showPanel("results"));
        btnAtt    .addActionListener(e -> showPanel("attendance"));
        btnLeave  .addActionListener(e -> showPanel("dutyleave"));
        btnAssign .addActionListener(e -> showPanel("assignments"));

        qaRow.add(btnResults);
        qaRow.add(btnAtt);
        qaRow.add(btnLeave);
        qaRow.add(btnAssign);

        qaCard.add(qaTitle, BorderLayout.NORTH);
        qaCard.add(qaRow,   BorderLayout.CENTER);

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
    // RESULTS / MARKSHEET
    // =========================================================================
    private JPanel buildResultsPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Results & Marksheet",
                "Consolidated academic marks, grades, and grade points."), BorderLayout.NORTH);

        String[] cols = {"Subject Code", "Subject Name", "Exam Type",
                         "Marks", "Max Marks", "Percentage", "Grade", "Grade Point"};
        marksModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(marksModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnRefresh.addActionListener(e -> loadMarks());

        JPanel toolbar = UIComponents.createToolbar(btnRefresh);
        JPanel center  = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolbar,                                     BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table),   BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadMarks() {
        if (marksModel == null || student == null) return;
        marksModel.setRowCount(0);
        List<Mark> list = markDAO.getMarksForStudent(student.getId());
        for (Mark m : list) {
            marksModel.addRow(new Object[]{
                m.getSubjectCode(),
                m.getSubjectName(),
                m.getExamType().getLabel(),
                m.getMarksObtained(),
                m.getMaxMarks(),
                String.format("%.1f%%", m.getPercentage()),
                m.getGrade(),
                m.getGradePoint()
            });
        }
    }

    // =========================================================================
    // ATTENDANCE
    // =========================================================================
    private JPanel buildAttendancePanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Attendance",
                "Subject-wise attendance and shortage check."), BorderLayout.NORTH);

        String[] cols = {"Subject Code", "Subject Name", "Total Hours",
                         "Present", "Duty Leave", "Absent", "Attendance %", "Status"};
        attModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(attModel);

        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnRefresh.addActionListener(e -> loadAttendance());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnRefresh),        BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table),     BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadAttendance() {
        if (attModel == null || student == null) return;
        attModel.setRowCount(0);
        List<AttendanceDAO.SubjectAttendanceSummary> list =
                attendanceDAO.getStudentSubjectWiseAttendance(student.getId());
        for (AttendanceDAO.SubjectAttendanceSummary s : list) {
            String status = s.percentage() >= 75.0 ? "✓ Satisfactory" : "⚠ SHORTAGE";
            attModel.addRow(new Object[]{
                s.subjectCode(), s.subjectName(), s.totalHours(),
                s.presentHours(), s.dutyLeaveHours(), s.absentHours(),
                s.percentage() + "%", status
            });
        }
    }

    // =========================================================================
    // DUTY LEAVE
    // =========================================================================
    private JPanel buildDutyLeavePanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Duty Leave",
                "Apply for duty leave and track its approval pipeline."), BorderLayout.NORTH);

        String[] cols = {"App ID", "Reason", "Days", "Dates",
                         "Proof", "Status", "Advisor Remarks", "HOD Remarks", "Principal"};
        leaveModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(leaveModel);

        JButton btnApply   = UIComponents.createPrimaryButton("➕  Apply for Duty Leave");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnApply  .addActionListener(e -> showApplyDutyLeaveDialog());
        btnRefresh.addActionListener(e -> loadDutyLeaves());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnApply, btnRefresh), BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table),        BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadDutyLeaves() {
        if (leaveModel == null || student == null) return;
        leaveModel.setRowCount(0);
        List<DutyLeave> list = dutyLeaveDAO.getLeavesForStudent(student.getId());
        for (DutyLeave dl : list) {
            leaveModel.addRow(new Object[]{
                dl.getId(),
                dl.getReason(),
                dl.getTotalDays(),
                dl.getStartDate() + " → " + dl.getEndDate(),
                dl.getProofDetails(),
                dl.getStatus().getLabel(),
                dl.getAdvisorRemarks()   != null ? dl.getAdvisorRemarks()   : "Pending",
                dl.getHodRemarks()       != null ? dl.getHodRemarks()       : "Pending",
                dl.getPrincipalRemarks() != null ? dl.getPrincipalRemarks() : "Pending"
            });
        }
    }

    private void showApplyDutyLeaveDialog() {
        JDialog dlg = new JDialog(this, "Apply for Duty Leave", true);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.SURFACE);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        JTextField txtReason = UIComponents.createTextField("Reason for leave");
        JTextField txtStart  = UIComponents.createTextField(LocalDate.now().toString());
        JTextField txtEnd    = UIComponents.createTextField(LocalDate.now().plusDays(1).toString());
        JTextField txtDays   = UIComponents.createTextField("Number of days");
        JTextField txtProof  = UIComponents.createTextField("Reference / proof document");

        addDlgRow(form, "Reason",              txtReason);
        form.add(Box.createVerticalStrut(10));
        addDlgRow(form, "Start Date (YYYY-MM-DD)", txtStart);
        form.add(Box.createVerticalStrut(10));
        addDlgRow(form, "End Date (YYYY-MM-DD)",   txtEnd);
        form.add(Box.createVerticalStrut(10));
        addDlgRow(form, "Total Days",          txtDays);
        form.add(Box.createVerticalStrut(10));
        addDlgRow(form, "Proof / Reference",   txtProof);

        JPanel footer = buildDlgFooter(dlg, () -> {
            try {
                DutyLeave dl = new DutyLeave();
                dl.setStudentId(student.getId());
                dl.setRegisterNumber(student.getRegisterNumber());
                dl.setReason(txtReason.getText().trim());
                dl.setStartDate(Date.valueOf(txtStart.getText().trim()));
                dl.setEndDate(Date.valueOf(txtEnd.getText().trim()));
                dl.setTotalDays(Integer.parseInt(txtDays.getText().trim()));
                dl.setProofDetails(txtProof.getText().trim());
                if (dutyLeaveDAO.applyDutyLeave(dl)) {
                    loadDutyLeaves();
                    dlg.dispose();
                    UIComponents.showSuccessToast(this, "Duty leave submitted — forwarded to your Class Advisor.");
                }
            } catch (Exception ex) {
                UIComponents.showError(dlg, "Error: " + ex.getMessage());
            }
        });

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // TIMETABLE
    // =========================================================================
    private JPanel buildTimetablePanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Timetable",
                "Weekly class schedule for your current semester."), BorderLayout.NORTH);

        String[] cols = {"Day", "Period", "Subject Code", "Subject Name", "Faculty", "Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        if (student != null) {
            List<TimetableSlot> slots = timetableDAO.getTimetableForClass(student.getClassId());
            for (TimetableSlot s : slots) {
                model.addRow(new Object[]{
                    s.getDayOfWeek().getLabel(), "Period " + s.getPeriodSlot(),
                    s.getSubjectCode(), s.getSubjectName(),
                    s.getFacultyName(), s.getRoomNumber()
                });
            }
        }

        JTable table = UIComponents.createStyledTable(model);
        page.add(UIComponents.createTableScrollPane(table), BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // ASSIGNMENTS
    // =========================================================================
    private JPanel buildAssignmentsPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Assignments",
                "View assignments and submit your work."), BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Subject", "Faculty", "Due Date", "Max Marks", "Status", "Marks Awarded"};
        assignModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(assignModel);

        JButton btnSubmit  = UIComponents.createPrimaryButton("📝  Submit Work");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnSubmit .addActionListener(e -> showSubmitAssignmentDialog());
        btnRefresh.addActionListener(e -> loadAssignments());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnSubmit, btnRefresh), BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table),         BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadAssignments() {
        if (assignModel == null || student == null) return;
        assignModel.setRowCount(0);
        List<Assignment> list = assignmentDAO.getAssignmentsForClass(student.getClassId(), student.getId());
        for (Assignment a : list) {
            assignModel.addRow(new Object[]{
                a.getId(), a.getTitle(),
                a.getSubjectCode() + " · " + a.getSubjectName(),
                a.getFacultyName(), a.getDueDate(), a.getMaxMarks(),
                a.isSubmittedByCurrentStudent() ? "Submitted" : "Pending",
                a.getMarksAwarded() != null ? a.getMarksAwarded() : "—"
            });
        }
    }

    private void showSubmitAssignmentDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter Assignment ID to submit:", "Submit Assignment", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int aid = Integer.parseInt(input.trim());
            JTextArea txtWork = new JTextArea(6, 35);
            txtWork.setLineWrap(true);
            txtWork.setFont(UITheme.FONT_REGULAR);

            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.add(new JLabel("Paste your solution / notes:"), BorderLayout.NORTH);
            p.add(new JScrollPane(txtWork), BorderLayout.CENTER);

            int opt = JOptionPane.showConfirmDialog(this, p,
                    "Assignment Submission #" + aid, JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                if (assignmentDAO.submitAssignment(aid, student.getId(),
                        txtWork.getText().trim(), "solution.txt")) {
                    loadAssignments();
                    UIComponents.showSuccessToast(this, "Assignment submitted successfully.");
                }
            }
        } catch (NumberFormatException ex) {
            UIComponents.showError(this, "Invalid Assignment ID.");
        }
    }

    // =========================================================================
    // GRIEVANCES
    // =========================================================================
    private JPanel buildGrievancesPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Grievances",
                "File and track grievances with the administration."), BorderLayout.NORTH);

        String[] cols = {"ID", "Category", "Title", "Status", "Resolution", "Resolved By"};
        grievModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(grievModel);

        JButton btnNew     = UIComponents.createPrimaryButton("➕  File Grievance");
        JButton btnRefresh = UIComponents.createSecondaryButton("↻  Refresh");
        btnNew    .addActionListener(e -> showFileGrievanceDialog());
        btnRefresh.addActionListener(e -> loadGrievances());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(UIComponents.createToolbar(btnNew, btnRefresh), BorderLayout.NORTH);
        center.add(UIComponents.createTableScrollPane(table),      BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void loadGrievances() {
        if (grievModel == null || student == null) return;
        grievModel.setRowCount(0);
        List<Grievance> list = grievanceDAO.getGrievancesByStudent(student.getId());
        for (Grievance g : list) {
            grievModel.addRow(new Object[]{
                g.getId(), g.getCategory().getLabel(), g.getTitle(),
                g.getStatus().getLabel(),
                g.getResolution()        != null ? g.getResolution()        : "Under Review",
                g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "—"
            });
        }
    }

    private void showFileGrievanceDialog() {
        JDialog dlg = new JDialog(this, "File a Grievance", true);
        dlg.setSize(460, 360);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.SURFACE);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        JComboBox<Grievance.Category> cmbCat = UIComponents.createComboBox(Grievance.Category.values());
        JTextField txtTitle = UIComponents.createTextField("Brief title of the grievance");
        JTextArea  txtDesc  = new JTextArea(4, 30);
        txtDesc.setFont(UITheme.FONT_REGULAR);
        txtDesc.setLineWrap(true);
        txtDesc.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)));

        addDlgRow(form, "Category",    cmbCat);
        form.add(Box.createVerticalStrut(10));
        addDlgRow(form, "Title",       txtTitle);
        form.add(Box.createVerticalStrut(10));

        JLabel lblDesc = UIComponents.createFormLabel("Description");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblDesc);
        form.add(Box.createVerticalStrut(4));
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        form.add(descScroll);

        JPanel footer = buildDlgFooter(dlg, () -> {
            Grievance g = new Grievance();
            g.setStudentId(student.getId());
            g.setRegisterNumber(student.getRegisterNumber());
            g.setCategory((Grievance.Category) cmbCat.getSelectedItem());
            g.setTitle(txtTitle.getText().trim());
            g.setDescription(txtDesc.getText().trim());
            if (grievanceDAO.submitGrievance(g)) {
                loadGrievances();
                dlg.dispose();
                UIComponents.showSuccessToast(this, "Grievance submitted and forwarded to the Principal.");
            }
        });

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    // FEEDBACK
    // =========================================================================
    private JPanel buildFeedbackPanel() {
        JPanel page = UIComponents.createPagePanel();
        page.setLayout(new BorderLayout(0, 16));
        page.add(UIComponents.createHeader("Faculty Feedback",
                "Submit anonymous feedback for your course instructors."), BorderLayout.NORTH);

        if (student == null) return page;

        List<SubjectDAO.ClassTeacherMapping> teachers =
                subjectDAO.getFacultyAssignedToClass(student.getClassId());

        JComboBox<SubjectDAO.ClassTeacherMapping> cmbTeacher =
                UIComponents.createComboBox(teachers.toArray(new SubjectDAO.ClassTeacherMapping[0]));

        JComboBox<Integer> cmbTeach   = UIComponents.createComboBox(new Integer[]{5, 4, 3, 2, 1});
        JComboBox<Integer> cmbPunct   = UIComponents.createComboBox(new Integer[]{5, 4, 3, 2, 1});
        JComboBox<Integer> cmbClarity = UIComponents.createComboBox(new Integer[]{5, 4, 3, 2, 1});
        JTextArea          txtComment = new JTextArea(4, 30);
        txtComment.setFont(UITheme.FONT_REGULAR);
        txtComment.setLineWrap(true);
        txtComment.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)));

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        addDlgRow(card, "Faculty & Subject",              cmbTeacher);
        card.add(Box.createVerticalStrut(12));
        addDlgRow(card, "Teaching Effectiveness (1–5)",   cmbTeach);
        card.add(Box.createVerticalStrut(12));
        addDlgRow(card, "Punctuality & Regularity (1–5)", cmbPunct);
        card.add(Box.createVerticalStrut(12));
        addDlgRow(card, "Concept Clarity (1–5)",          cmbClarity);
        card.add(Box.createVerticalStrut(12));

        JLabel lblCmt = UIComponents.createFormLabel("Anonymous Comments");
        lblCmt.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCmt);
        card.add(Box.createVerticalStrut(4));
        JScrollPane cmtScroll = new JScrollPane(txtComment);
        cmtScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmtScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.add(cmtScroll);
        card.add(Box.createVerticalStrut(16));

        JButton btnSubmit = UIComponents.createPrimaryButton("Submit Feedback Anonymously");
        btnSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubmit.addActionListener(e -> {
            SubjectDAO.ClassTeacherMapping sel =
                    (SubjectDAO.ClassTeacherMapping) cmbTeacher.getSelectedItem();
            if (sel == null) { UIComponents.showError(this, "Please select a faculty member."); return; }
            Feedback fb = new Feedback();
            fb.setStudentId(student.getId());
            fb.setFacultyId(sel.facultyId());
            fb.setSubjectId(1);
            fb.setRatingTeaching((Integer)   cmbTeach.getSelectedItem());
            fb.setRatingPunctuality((Integer) cmbPunct.getSelectedItem());
            fb.setRatingClarity((Integer)     cmbClarity.getSelectedItem());
            fb.setComments(txtComment.getText().trim());
            if (feedbackDAO.submitFeedback(fb)) {
                txtComment.setText("");
                UIComponents.showSuccessToast(this, "Feedback submitted anonymously. Thank you!");
            } else {
                UIComponents.showError(this, "Could not submit feedback. You may have already submitted for this subject.");
            }
        });
        card.add(btnSubmit);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void addDlgRow(JPanel panel, String labelText, JComponent field) {
        JLabel lbl = UIComponents.createFormLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
    }

    /**
     * Builds a standard dialog footer with Cancel + Submit buttons.
     * @param dlg      The dialog to close on cancel
     * @param onSubmit Runnable to run on Submit click
     */
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
        loadMarks();
        loadAttendance();
        loadDutyLeaves();
        loadAssignments();
        loadGrievances();
    }
}
