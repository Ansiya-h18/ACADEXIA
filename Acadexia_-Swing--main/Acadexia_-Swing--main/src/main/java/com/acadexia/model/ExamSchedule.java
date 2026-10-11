package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing institutional examination schedules.
 */
public class ExamSchedule {

    private int id;
    private int classId;
    private String className;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private Mark.ExamType examType;
    private String examName;
    private Date examDate;
    private String startTime;
    private String endTime;
    private String roomHall;
    private double maxMarks;
    private String portionsSyllabus;
    private Timestamp createdAt;

    public ExamSchedule() {}

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

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRoomHall() { return roomHall; }
    public void setRoomHall(String roomHall) { this.roomHall = roomHall; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public String getPortionsSyllabus() { return portionsSyllabus; }
    public void setPortionsSyllabus(String portionsSyllabus) { this.portionsSyllabus = portionsSyllabus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
