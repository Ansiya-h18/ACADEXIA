package com.acadexia.model;

import java.sql.Timestamp;

/**
 * Model representing a Parent/Guardian account securely linked to a Student.
 */
public class Parent {

    public enum VerificationStatus {
        PENDING("Pending Verification"),
        VERIFIED("Verified Guardian"),
        REJECTED("Rejected / Invalid");

        private final String label;

        VerificationStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private int id;
    private int userId;
    private int studentId;
    private String relationship; // Father, Mother, Guardian
    private String occupation;
    private String address;
    private String emergencyContact;
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    private Integer verifiedByUserId;
    private Timestamp verificationDate;
    private String verificationNotes;
    private Timestamp createdAt;

    // Joined User Details
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private boolean active = true;

    // Joined Student Details
    private String studentFullName;
    private String studentRegisterNumber;
    private String studentRollNumber;
    private int studentClassId;
    private String studentClassName;
    private String studentDepartmentName;
    private int studentSemester;
    private double studentAttendancePercentage;
    private double studentSgpa;
    private double studentCgpa;
    private String studentAdvisorName;

    public Parent() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public Integer getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(Integer verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }

    public Timestamp getVerificationDate() { return verificationDate; }
    public void setVerificationDate(Timestamp verificationDate) { this.verificationDate = verificationDate; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // User details getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Student details getters/setters
    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getStudentRegisterNumber() { return studentRegisterNumber; }
    public void setStudentRegisterNumber(String studentRegisterNumber) { this.studentRegisterNumber = studentRegisterNumber; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public int getStudentClassId() { return studentClassId; }
    public void setStudentClassId(int studentClassId) { this.studentClassId = studentClassId; }

    public String getStudentClassName() { return studentClassName; }
    public void setStudentClassName(String studentClassName) { this.studentClassName = studentClassName; }

    public String getStudentDepartmentName() { return studentDepartmentName; }
    public void setStudentDepartmentName(String studentDepartmentName) { this.studentDepartmentName = studentDepartmentName; }

    public int getStudentSemester() { return studentSemester; }
    public void setStudentSemester(int studentSemester) { this.studentSemester = studentSemester; }

    public double getStudentAttendancePercentage() { return studentAttendancePercentage; }
    public void setStudentAttendancePercentage(double studentAttendancePercentage) { this.studentAttendancePercentage = studentAttendancePercentage; }

    public double getStudentSgpa() { return studentSgpa; }
    public void setStudentSgpa(double studentSgpa) { this.studentSgpa = studentSgpa; }

    public double getStudentCgpa() { return studentCgpa; }
    public void setStudentCgpa(double studentCgpa) { this.studentCgpa = studentCgpa; }

    public String getStudentAdvisorName() { return studentAdvisorName; }
    public void setStudentAdvisorName(String studentAdvisorName) { this.studentAdvisorName = studentAdvisorName; }

    @Override
    public String toString() {
        return fullName + " (" + relationship + " of " + studentFullName + " - " + studentRegisterNumber + ")";
    }
}
