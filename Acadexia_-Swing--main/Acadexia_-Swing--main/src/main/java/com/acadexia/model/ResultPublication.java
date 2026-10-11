package com.acadexia.model;

import java.sql.Timestamp;

/**
 * Entity model representing result verification and official publication status
 * for an examination conducted for a class and subject.
 */
public class ResultPublication {

    public enum PublicationStatus {
        ENTERED("Marks Entered (Draft)"),
        VERIFIED("Verified by Exam Authority"),
        PUBLISHED("Officially Published");

        private final String label;
        PublicationStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int id;
    private int classId;
    private String className;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private Mark.ExamType examType;
    private String academicYear;
    private PublicationStatus status = PublicationStatus.ENTERED;
    private Integer verifiedByUserId;
    private String verifiedByName;
    private Integer publishedByUserId;
    private String publishedByName;
    private Timestamp publishedAt;
    private String remarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ResultPublication() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Mark.ExamType getExamType() { return examType; }
    public void setExamType(Mark.ExamType examType) { this.examType = examType; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }

    public Integer getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(Integer verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }

    public String getVerifiedByName() { return verifiedByName; }
    public void setVerifiedByName(String verifiedByName) { this.verifiedByName = verifiedByName; }

    public Integer getPublishedByUserId() { return publishedByUserId; }
    public void setPublishedByUserId(Integer publishedByUserId) { this.publishedByUserId = publishedByUserId; }

    public String getPublishedByName() { return publishedByName; }
    public void setPublishedByName(String publishedByName) { this.publishedByName = publishedByName; }

    public Timestamp getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Timestamp publishedAt) { this.publishedAt = publishedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
