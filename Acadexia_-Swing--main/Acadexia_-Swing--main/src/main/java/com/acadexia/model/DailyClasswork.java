package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing daily classwork logs recorded by faculty for classes and subjects.
 */
public class DailyClasswork {

    public enum Status {
        COMPLETED("Completed"),
        IN_PROGRESS("In Progress"),
        POSTPONED("Postponed");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;
    private int classId;
    private String className;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int facultyId;
    private String facultyName;
    private Date date;
    private int periodHour = 1;
    private String topicCovered;
    private String homeworkAssigned;
    private String referenceMaterials;
    private Status status = Status.COMPLETED;
    private Timestamp createdAt;

    public DailyClasswork() {}

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

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getPeriodHour() { return periodHour; }
    public void setPeriodHour(int periodHour) { this.periodHour = periodHour; }

    public String getTopicCovered() { return topicCovered; }
    public void setTopicCovered(String topicCovered) { this.topicCovered = topicCovered; }

    public String getHomeworkAssigned() { return homeworkAssigned; }
    public void setHomeworkAssigned(String homeworkAssigned) { this.homeworkAssigned = homeworkAssigned; }

    public String getReferenceMaterials() { return referenceMaterials; }
    public void setReferenceMaterials(String referenceMaterials) { this.referenceMaterials = referenceMaterials; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
