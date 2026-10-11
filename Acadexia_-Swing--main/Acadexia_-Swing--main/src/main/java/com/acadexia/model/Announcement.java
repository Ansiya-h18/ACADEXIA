package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing institutional circulars and official announcements published by Principal/Authorities.
 */
public class Announcement {

    public enum Category {
        GENERAL("General Institutional Circular"),
        ACADEMIC("Academic Directive"),
        EXAMINATION("Examination Notice"),
        EVENT("Campus Event & Symposium"),
        URGENT("Urgent Institutional Notice"),
        CIRCULAR("Executive Office Circular");

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
    private String content;
    private Category category = Category.GENERAL;
    private String targetAudience = "ALL"; // ALL, FACULTY, STUDENTS, PARENTS
    private boolean pinned = false;
    private Integer postedByUserId;
    private String postedByUserName;
    private Date postedDate;
    private Date expiryDate;
    private String attachmentNote;
    private Timestamp createdAt;

    public Announcement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public Integer getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(Integer postedByUserId) { this.postedByUserId = postedByUserId; }

    public String getPostedByUserName() { return postedByUserName; }
    public void setPostedByUserName(String postedByUserName) { this.postedByUserName = postedByUserName; }

    public Date getPostedDate() { return postedDate; }
    public void setPostedDate(Date postedDate) { this.postedDate = postedDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getAttachmentNote() { return attachmentNote; }
    public void setAttachmentNote(String attachmentNote) { this.attachmentNote = attachmentNote; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
