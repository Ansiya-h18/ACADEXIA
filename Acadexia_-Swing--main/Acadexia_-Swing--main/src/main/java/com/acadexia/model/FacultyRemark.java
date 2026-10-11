package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing faculty mentoring remarks and progress feedback given to students & parents.
 */
public class FacultyRemark {

    public enum Category {
        ACADEMIC_PROGRESS("Academic Progress & Performance"),
        CLASS_CONDUCT("Classroom Conduct & Discipline"),
        ATTENDANCE_CONCERN("Attendance & Punctuality Concern"),
        EXCELLENCE("Commendation & Academic Excellence"),
        GENERAL("General Advisory & Guidance");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;
    private int studentId;
    private String studentName;
    private String studentRegisterNo;
    private int facultyId;
    private String facultyName;
    private String facultyDesignation;
    private Integer subjectId;
    private String subjectCode;
    private String subjectName;
    private Category category = Category.ACADEMIC_PROGRESS;
    private String title;
    private String remarks;
    private String actionSuggested;
    private Integer rating; // 1 to 5
    private Date datePosted;
    private Timestamp createdAt;

    public FacultyRemark() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentRegisterNo() { return studentRegisterNo; }
    public void setStudentRegisterNo(String studentRegisterNo) { this.studentRegisterNo = studentRegisterNo; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getFacultyDesignation() { return facultyDesignation; }
    public void setFacultyDesignation(String facultyDesignation) { this.facultyDesignation = facultyDesignation; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getActionSuggested() { return actionSuggested; }
    public void setActionSuggested(String actionSuggested) { this.actionSuggested = actionSuggested; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Date getDatePosted() { return datePosted; }
    public void setDatePosted(Date datePosted) { this.datePosted = datePosted; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
