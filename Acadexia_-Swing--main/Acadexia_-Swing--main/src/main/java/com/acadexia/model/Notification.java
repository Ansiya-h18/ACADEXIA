package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing institutional notifications, circulars, and announcements.
 */
public class Notification {

    public enum Priority {
        LOW("Low Priority"),
        NORMAL("Normal"),
        HIGH("High Priority"),
        URGENT("Urgent Alert");

        private final String label;

        Priority(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Category {
        EXAMINATION("Examination & Results"),
        ATTENDANCE_ALERT("Attendance & Punctuality"),
        PARENT_MEETING("Parent-Teacher Conference"),
        ACADEMIC("Academic Calendar & Syllabus"),
        FEES("Fee Deadlines & Scholarships"),
        GENERAL("General Institutional Circular");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;
    private String title;
    private String message;
    private String targetAudience = "ALL"; // ALL, PARENTS, STUDENTS, FACULTY
    private Integer targetClassId;
    private String targetClassName;
    private Priority priority = Priority.NORMAL;
    private Category category = Category.GENERAL;
    private Integer postedByUserId;
    private String postedByUserName;
    private Date publishDate;
    private boolean active = true;
    private Timestamp createdAt;

    public Notification() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public Integer getTargetClassId() { return targetClassId; }
    public void setTargetClassId(Integer targetClassId) { this.targetClassId = targetClassId; }

    public String getTargetClassName() { return targetClassName; }
    public void setTargetClassName(String targetClassName) { this.targetClassName = targetClassName; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Integer getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(Integer postedByUserId) { this.postedByUserId = postedByUserId; }

    public String getPostedByUserName() { return postedByUserName; }
    public void setPostedByUserName(String postedByUserName) { this.postedByUserName = postedByUserName; }

    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
