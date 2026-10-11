package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing institutional academic calendar events, term milestones, exams, and holidays.
 */
public class AcademicEvent {

    public enum EventType {
        EXAM("Examination Schedule"),
        HOLIDAY("Public / Institutional Holiday"),
        ACADEMIC_MILESTONE("Academic Milestone / Term Start/End"),
        MEETING("Senate / Faculty / Parent Council Meeting"),
        CULTURAL_SPORTS("Cultural & Sports Meet"),
        DEADLINE("Marks Entry / Fee Submission Deadline");

        private final String label;

        EventType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;
    private String eventTitle;
    private EventType eventType = EventType.ACADEMIC_MILESTONE;
    private Date startDate;
    private Date endDate;
    private String description;
    private String targetAudience = "ALL"; // ALL, FACULTY, STUDENTS, PARENTS
    private String semesterApplicable;
    private Integer createdByUserId;
    private String createdByUserName;
    private Timestamp createdAt;

    public AcademicEvent() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getSemesterApplicable() { return semesterApplicable; }
    public void setSemesterApplicable(String semesterApplicable) { this.semesterApplicable = semesterApplicable; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByUserName() { return createdByUserName; }
    public void setCreatedByUserName(String createdByUserName) { this.createdByUserName = createdByUserName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
