package src;

import java.sql.Date;

/**
 * Entity class representing an Enrollment / Course Registration.
 * Corresponds to MySQL 'enrollments' table.
 */
public class Enrollment {
    private int registrationId;
    private String studentId;
    private String courseId;
    private Date registrationDate;
    private String enrollmentStatus; // 'REGISTERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'

    // Additional fields populated via SQL Joins for display
    private String studentName;
    private String courseName;
    private String department;

    public Enrollment() {
    }

    public Enrollment(int registrationId, String studentId, String courseId, Date registrationDate, String enrollmentStatus) {
        this.registrationId = registrationId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.registrationDate = registrationDate;
        this.enrollmentStatus = enrollmentStatus;
    }

    public Enrollment(int registrationId, String studentId, String studentName, String courseId, String courseName, Date registrationDate, String enrollmentStatus) {
        this(registrationId, studentId, courseId, registrationDate, enrollmentStatus);
        this.studentName = studentName;
        this.courseName = courseName;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return String.format("Reg #%-4d | Student: %-8s (%-18s) | Course: %-8s (%-26s) | Date: %s | Status: %-11s",
                registrationId, studentId, (studentName != null ? studentName : ""),
                courseId, (courseName != null ? courseName : ""),
                registrationDate != null ? registrationDate.toString() : "N/A",
                enrollmentStatus);
    }
}
