package src;

import java.sql.Date;

/**
 * Entity class representing a Course Completion.
 * Corresponds to MySQL 'completions' table.
 */
public class Completion {
    private int completionId;
    private int registrationId;
    private String completionStatus; // 'COMPLETED', 'PENDING', 'WITHDRAWN'
    private Date completionDate;

    // Join details
    private String studentId;
    private String studentName;
    private String courseId;
    private String courseName;

    public Completion() {
    }

    public Completion(int completionId, int registrationId, String completionStatus, Date completionDate) {
        this.completionId = completionId;
        this.registrationId = registrationId;
        this.completionStatus = completionStatus;
        this.completionDate = completionDate;
    }

    public int getCompletionId() {
        return completionId;
    }

    public void setCompletionId(int completionId) {
        this.completionId = completionId;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    @Override
    public String toString() {
        return String.format("Comp #%-4d | Reg #%-4d | Student: %-8s (%-16s) | Course: %-8s (%-24s) | Date: %s | Status: %s",
                completionId, registrationId, studentId, (studentName != null ? studentName : ""),
                courseId, (courseName != null ? courseName : ""),
                completionDate != null ? completionDate.toString() : "N/A",
                completionStatus);
    }
}
