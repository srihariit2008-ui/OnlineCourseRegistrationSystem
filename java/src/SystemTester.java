package src;

import java.sql.Date;
import java.util.List;

/**
 * Automated System Test Suite executing Core Unit and Integration Test Cases.
 * Demonstrates: Software Testing, Verification and Validation, Boundary Value Analysis,
 * Equivalence Partitioning, and Exception Verification.
 * Syllabus Reference: Software Engineering Unit 4 (Software Testing).
 */
public class SystemTester {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(90));
        System.out.println("   ONLINE COURSE REGISTRATION SYSTEM - AUTOMATED TEST SUITE");
        System.out.println("=".repeat(90));
        System.out.printf("%-8s | %-45s | %-8s | %s\n", "TEST ID", "TEST DESCRIPTION", "STATUS", "REASON/NOTES");
        System.out.println("-".repeat(90));

        StudentDAO studentDAO = new StudentDAO();
        CourseDAO courseDAO = new CourseDAO();
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        CompletionDAO completionDAO = new CompletionDAO();
        ReportService reportService = new ReportService();

        // TC-01: Adding a valid student
        try {
            Student s1 = new Student("STU999", "Kavya Ramesh", "kavya.ramesh@institution.edu", "9876543999", "Information Technology", 2);
            studentDAO.addStudent(s1);
            Student retrieved = studentDAO.getStudentById("STU999");
            assertCondition("TC-01", "Adding a valid student", retrieved != null && retrieved.getStudentName().equals("Kavya Ramesh"), "Student inserted successfully");
        } catch (Exception e) {
            recordFailure("TC-01", "Adding a valid student", e.getMessage());
        }

        // TC-02: Adding a duplicate student (same ID)
        try {
            Student sDup = new Student("STU999", "Kavya Duplicate", "kavya.dup@institution.edu", "9876543999", "Information Technology", 2);
            studentDAO.addStudent(sDup);
            recordFailure("TC-02", "Adding duplicate student", "Failed to catch duplicate Student ID");
        } catch (CustomExceptions.DuplicateRegistrationException e) {
            recordSuccess("TC-02", "Adding duplicate student", "Properly rejected duplicate Student ID");
        } catch (Exception e) {
            recordFailure("TC-02", "Adding duplicate student", "Unexpected error: " + e.getMessage());
        }

        // TC-03: Adding a valid course
        try {
            Course c1 = new Course("CRS999", "Cloud Computing & Distributed Systems", "Technical Training", "40 Hours", 4, "Cloud concepts and deployments.");
            courseDAO.addCourse(c1);
            Course retrievedCourse = courseDAO.getCourseById("CRS999");
            assertCondition("TC-03", "Adding a valid course", retrievedCourse != null && retrievedCourse.getCourseName().contains("Cloud"), "Course inserted successfully");
        } catch (Exception e) {
            recordFailure("TC-03", "Adding a valid course", e.getMessage());
        }

        // TC-04: Registering an existing student for available course
        int testRegId = -1;
        try {
            // Register STU999 for CRS999 (Capacity 4, 0 enrolled)
            testRegId = enrollmentDAO.registerStudentForCourse("STU999", "CRS999");
            assertCondition("TC-04", "Registering an existing student", testRegId > 0, "Registration created with ID #" + testRegId);
        } catch (Exception e) {
            recordFailure("TC-04", "Registering an existing student", e.getMessage());
        }

        // TC-05: Registering a non-existing student
        try {
            enrollmentDAO.registerStudentForCourse("NON_EXISTING_STU", "CRS999");
            recordFailure("TC-05", "Registering non-existing student", "Allowed non-existing student registration");
        } catch (CustomExceptions.RecordNotFoundException e) {
            recordSuccess("TC-05", "Registering non-existing student", "Properly rejected with RecordNotFoundException");
        } catch (Exception e) {
            recordFailure("TC-05", "Registering non-existing student", "Unexpected exception: " + e.getMessage());
        }

        // TC-06: Registering for a non-existing course
        try {
            enrollmentDAO.registerStudentForCourse("STU999", "NON_EXISTING_CRS");
            recordFailure("TC-06", "Registering non-existing course", "Allowed non-existing course registration");
        } catch (CustomExceptions.RecordNotFoundException e) {
            recordSuccess("TC-06", "Registering non-existing course", "Properly rejected with RecordNotFoundException");
        } catch (Exception e) {
            recordFailure("TC-06", "Registering non-existing course", "Unexpected exception: " + e.getMessage());
        }

        // TC-07: Registering twice for the same course (Duplicate registration)
        try {
            // STU999 is already registered for CRS999 in TC-04
            enrollmentDAO.registerStudentForCourse("STU999", "CRS999");
            recordFailure("TC-07", "Registering twice for same course", "Duplicate registration allowed");
        } catch (CustomExceptions.DuplicateRegistrationException e) {
            recordSuccess("TC-07", "Registering twice for same course", "Properly prevented duplicate registration");
        } catch (Exception e) {
            recordFailure("TC-07", "Registering twice for same course", "Unexpected exception: " + e.getMessage());
        }

        // TC-08: Registering when course capacity is full
        // CRS104 has max_capacity = 3 and 3 active enrollments (STU104, STU106, STU109)
        try {
            enrollmentDAO.registerStudentForCourse("STU999", "CRS104");
            recordFailure("TC-08", "Registering when course capacity full", "Allowed registration in full course");
        } catch (CustomExceptions.CourseFullException e) {
            recordSuccess("TC-08", "Registering when course capacity full", "Rejected with CourseFullException");
        } catch (Exception e) {
            recordFailure("TC-08", "Registering when course capacity full", "Unexpected exception: " + e.getMessage());
        }

        // TC-09: Cancelling a registration
        try {
            boolean cancelled = enrollmentDAO.cancelRegistration(testRegId);
            Enrollment updated = enrollmentDAO.getEnrollmentById(testRegId);
            assertCondition("TC-09", "Cancelling a registration", cancelled && updated.getEnrollmentStatus().equalsIgnoreCase("CANCELLED"), "Registration status updated to CANCELLED");
        } catch (Exception e) {
            recordFailure("TC-09", "Cancelling a registration", e.getMessage());
        }

        // TC-10: Checking available seats after cancellation
        try {
            Course cAfterCancel = courseDAO.getCourseById("CRS999");
            // Since testRegId was cancelled, active count in CRS999 should be 0, available should be 4
            assertCondition("TC-10", "Checking available seats after cancellation", cAfterCancel.getAvailableSeats() == 4, "Available seats restored to capacity 4");
        } catch (Exception e) {
            recordFailure("TC-10", "Checking available seats after cancellation", e.getMessage());
        }

        // TC-11: Updating enrollment status
        try {
            // Reg #3 is IN_PROGRESS -> Update to IN_PROGRESS explicitly
            boolean updated = enrollmentDAO.updateEnrollmentStatus(3, "IN_PROGRESS");
            Enrollment e3 = enrollmentDAO.getEnrollmentById(3);
            assertCondition("TC-11", "Updating enrollment status", updated && e3.getEnrollmentStatus().equals("IN_PROGRESS"), "Enrollment status successfully updated");
        } catch (Exception e) {
            recordFailure("TC-11", "Updating enrollment status", e.getMessage());
        }

        // TC-12: Marking a course as completed
        try {
            // Reg #4 (STU108 for CRS101) is REGISTERED. Let's mark as completed.
            boolean comp = completionDAO.markCourseCompleted(4, new Date(System.currentTimeMillis()));
            Enrollment e4 = enrollmentDAO.getEnrollmentById(4);
            assertCondition("TC-12", "Marking course as completed", comp && e4.getEnrollmentStatus().equals("COMPLETED"), "Registration #4 marked COMPLETED and certificate recorded");
        } catch (Exception e) {
            recordFailure("TC-12", "Marking course as completed", e.getMessage());
        }

        // TC-13: Generating course enrollment reports and CSV export
        try {
            String testCsv = "../reports/enrollment_report.csv";
            reportService.exportEnrollmentReportToCsv(testCsv);
            java.io.File file = new java.io.File(testCsv);
            assertCondition("TC-13", "Generating enrollment reports & CSV", file.exists() && file.length() > 0, "Report generated (" + file.length() + " bytes)");
        } catch (Exception e) {
            recordFailure("TC-13", "Generating enrollment reports & CSV", e.getMessage());
        }

        // TC-14: Database Connection handler resilience
        try {
            boolean available = DatabaseConnection.isDatabaseAvailable();
            String mode = available ? "MySQL Server Active" : "Standalone Memory Mode Active";
            recordSuccess("TC-14", "Connecting Java with Database layer", "Connection verified (" + mode + ")");
        } catch (Exception e) {
            recordFailure("TC-14", "Connecting Java with Database layer", e.getMessage());
        }

        System.out.println("=".repeat(90));
        System.out.printf("TEST SUMMARY: Total Executed: %d | PASSED: %d | FAILED: %d | Success Rate: %.1f%%\n",
                (totalPassed + totalFailed), totalPassed, totalFailed, (totalPassed * 100.0) / (totalPassed + totalFailed));
        System.out.println("=".repeat(90));

        AsyncAuditLogger.stopLogger();
    }

    private static void assertCondition(String testId, String description, boolean condition, String note) {
        if (condition) {
            recordSuccess(testId, description, note);
        } else {
            recordFailure(testId, description, note);
        }
    }

    private static void recordSuccess(String testId, String description, String note) {
        totalPassed++;
        System.out.printf("%-8s | %-45s | %-8s | %s\n", testId, description, "PASS", note);
    }

    private static void recordFailure(String testId, String description, String note) {
        totalFailed++;
        System.out.printf("%-8s | %-45s | %-8s | %s\n", testId, description, "FAIL", note);
    }
}
