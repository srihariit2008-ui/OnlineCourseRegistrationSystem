package src;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Report Service implementing Module 6: Report Generation.
 * Executes analytical queries with joins, aggregate functions, grouping,
 * sorting, and exports processed data to CSV.
 * Syllabus Reference: DBMS Unit 2 (SQL Joins & Aggregates); Java Unit 5 (Streams & Lambdas).
 */
public class ReportService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CompletionDAO completionDAO = new CompletionDAO();

    // 1. Complete Student Report
    public void printCompleteStudentReport() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        System.out.println("\n" + "=".repeat(105));
        System.out.printf("%-10s | %-22s | %-32s | %-12s | %-20s | %s\n",
                "STUDENT ID", "STUDENT NAME", "EMAIL ADDRESS", "PHONE", "DEPARTMENT", "YEAR");
        System.out.println("-".repeat(105));
        for (Student s : students) {
            System.out.printf("%-10s | %-22s | %-32s | %-12s | %-20s | Year %d\n",
                    s.getStudentId(), s.getStudentName(), s.getEmail(), s.getPhone(), s.getDepartment(), s.getYearOfStudy());
        }
        System.out.println("=".repeat(105));
        System.out.println("Total Registered Students: " + students.size());
    }

    // 2. Complete Course Report
    public void printCompleteCourseReport() throws SQLException {
        List<Course> courses = courseDAO.getAllCourses();
        System.out.println("\n" + "=".repeat(120));
        System.out.printf("%-10s | %-36s | %-22s | %-10s | %-8s | %-8s | %-10s\n",
                "COURSE ID", "COURSE NAME", "CATEGORY", "DURATION", "CAPACITY", "ENROLLED", "AVAILABLE");
        System.out.println("-".repeat(120));
        for (Course c : courses) {
            System.out.printf("%-10s | %-36s | %-22s | %-10s | %-8d | %-8d | %-10d\n",
                    c.getCourseId(), c.getCourseName(), c.getCategory(), c.getDuration(),
                    c.getMaxCapacity(), c.getActiveEnrollments(), c.getAvailableSeats());
        }
        System.out.println("=".repeat(120));
        System.out.println("Total Courses Offered: " + courses.size());
    }

    // 3. Course-Wise Enrollment Report
    public void printCourseWiseEnrollmentReport() throws SQLException {
        List<Course> courses = courseDAO.getAllCourses();
        System.out.println("\n" + "=".repeat(110));
        System.out.printf("%-10s | %-36s | %-20s | %-9s | %-9s | %s\n",
                "COURSE ID", "COURSE NAME", "CATEGORY", "CAPACITY", "ENROLLED", "OCCUPANCY RATE");
        System.out.println("-".repeat(110));
        for (Course c : courses) {
            System.out.printf("%-10s | %-36s | %-20s | %-9d | %-9d | %6.2f%%\n",
                    c.getCourseId(), c.getCourseName(), c.getCategory(),
                    c.getMaxCapacity(), c.getActiveEnrollments(), c.getOccupancyRate());
        }
        System.out.println("=".repeat(110));
    }

    // 4. Student-Wise Registration Report
    public void printStudentWiseRegistrationReport() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();

        System.out.println("\n" + "=".repeat(115));
        System.out.printf("%-10s | %-20s | %-20s | %-15s | %s\n",
                "STUDENT ID", "STUDENT NAME", "DEPARTMENT", "TOTAL ENROLLED", "REGISTERED COURSES (STATUS)");
        System.out.println("-".repeat(115));

        for (Student s : students) {
            List<Enrollment> studentEnrolls = enrollments.stream()
                    .filter(e -> e.getStudentId().equalsIgnoreCase(s.getStudentId()))
                    .collect(Collectors.toList());

            String courseSummary = studentEnrolls.stream()
                    .map(e -> e.getCourseId() + " (" + e.getEnrollmentStatus() + ")")
                    .collect(Collectors.joining(", "));
            if (courseSummary.isEmpty()) courseSummary = "None";

            System.out.printf("%-10s | %-20s | %-20s | %-15d | %s\n",
                    s.getStudentId(), s.getStudentName(), s.getDepartment(), studentEnrolls.size(), courseSummary);
        }
        System.out.println("=".repeat(115));
    }

    // 5. Available Seat Report
    public void printAvailableSeatReport() throws SQLException {
        List<Course> courses = courseDAO.getAllCourses();
        System.out.println("\n" + "=".repeat(100));
        System.out.printf("%-10s | %-38s | %-10s | %-10s | %s\n",
                "COURSE ID", "COURSE NAME", "CAPACITY", "ENROLLED", "AVAILABLE SEATS STATUS");
        System.out.println("-".repeat(100));
        for (Course c : courses) {
            String status = c.getAvailableSeats() == 0 ? "FULL (0 Seats)" : c.getAvailableSeats() + " Seats Available";
            System.out.printf("%-10s | %-38s | %-10d | %-10d | %s\n",
                    c.getCourseId(), c.getCourseName(), c.getMaxCapacity(), c.getActiveEnrollments(), status);
        }
        System.out.println("=".repeat(100));
    }

    // 6. Course Completion Report
    public void printCourseCompletionReport() throws SQLException {
        List<Completion> completions = completionDAO.getAllCompletions();
        System.out.println("\n" + "=".repeat(115));
        System.out.printf("%-8s | %-8s | %-10s | %-20s | %-10s | %-30s | %s\n",
                "CERT #", "REG #", "STUDENT ID", "STUDENT NAME", "COURSE ID", "COURSE NAME", "COMPLETION DATE");
        System.out.println("-".repeat(115));
        for (Completion c : completions) {
            System.out.printf("%-8d | %-8d | %-10s | %-20s | %-10s | %-30s | %s\n",
                    c.getCompletionId(), c.getRegistrationId(), c.getStudentId(), c.getStudentName(),
                    c.getCourseId(), c.getCourseName(), c.getCompletionDate());
        }
        System.out.println("=".repeat(115));
        System.out.println("Total Certified Course Completions: " + completions.size());
    }

    // 7. Most Popular Courses (Ranked descending)
    public void printMostPopularCourses() throws SQLException {
        List<Course> courses = courseDAO.getAllCourses().stream()
                .sorted(Comparator.comparingInt(Course::getActiveEnrollments).reversed())
                .collect(Collectors.toList());

        System.out.println("\n" + "=".repeat(95));
        System.out.println("                         MOST POPULAR COURSES (RANKED BY ENROLLMENT)");
        System.out.println("=".repeat(95));
        System.out.printf("%-5s | %-10s | %-36s | %-10s | %-10s | %s\n",
                "RANK", "COURSE ID", "COURSE NAME", "CAPACITY", "ENROLLED", "UTILIZATION");
        System.out.println("-".repeat(95));
        int rank = 1;
        for (Course c : courses) {
            System.out.printf("%-5d | %-10s | %-36s | %-10d | %-10d | %6.2f%%\n",
                    rank++, c.getCourseId(), c.getCourseName(), c.getMaxCapacity(), c.getActiveEnrollments(), c.getOccupancyRate());
        }
        System.out.println("=".repeat(95));
    }

    // 8. Courses with Low Enrollment (< 50% capacity)
    public void printLowEnrollmentCourses() throws SQLException {
        List<Course> lowEnrollment = courseDAO.getAllCourses().stream()
                .filter(c -> c.getActiveEnrollments() < (c.getMaxCapacity() * 0.50))
                .sorted(Comparator.comparingInt(Course::getActiveEnrollments))
                .collect(Collectors.toList());

        System.out.println("\n" + "=".repeat(95));
        System.out.println("               COURSES WITH LOW ENROLLMENT (BELOW 50% CAPACITY)");
        System.out.println("=".repeat(95));
        System.out.printf("%-10s | %-36s | %-10s | %-10s | %-12s | %s\n",
                "COURSE ID", "COURSE NAME", "CAPACITY", "ENROLLED", "AVAILABLE", "OCCUPANCY RATE");
        System.out.println("-".repeat(95));
        for (Course c : lowEnrollment) {
            System.out.printf("%-10s | %-36s | %-10d | %-10d | %-12d | %6.2f%%\n",
                    c.getCourseId(), c.getCourseName(), c.getMaxCapacity(), c.getActiveEnrollments(),
                    c.getAvailableSeats(), c.getOccupancyRate());
        }
        System.out.println("=".repeat(95));
        if (lowEnrollment.isEmpty()) {
            System.out.println("All offered courses currently meet or exceed 50% capacity!");
        }
    }

    /**
     * Exports processed enrollment summary to CSV for Python Data Science analysis.
     */
    public void exportEnrollmentReportToCsv(String filePath) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();

            List<Enrollment> enrollments = enrollmentDAO.getAllEnrollments();
            List<Course> courses = courseDAO.getAllCourses();
            List<Completion> completions = completionDAO.getAllCompletions();

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("registration_id,student_id,student_name,department,course_id,course_name,category,duration,max_capacity,registration_date,enrollment_status,completion_status,completion_date");
                for (Enrollment e : enrollments) {
                    Course c = courses.stream().filter(crs -> crs.getCourseId().equalsIgnoreCase(e.getCourseId())).findFirst().orElse(null);
                    Completion comp = completions.stream().filter(cmp -> cmp.getRegistrationId() == e.getRegistrationId()).findFirst().orElse(null);

                    String category = c != null ? c.getCategory() : "N/A";
                    String duration = c != null ? c.getDuration() : "N/A";
                    int maxCap = c != null ? c.getMaxCapacity() : 0;
                    String compStatus = comp != null ? comp.getCompletionStatus() : "PENDING";
                    String compDate = comp != null && comp.getCompletionDate() != null ? comp.getCompletionDate().toString() : "";

                    pw.printf("%d,%s,\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",%d,%s,%s,%s,%s\n",
                            e.getRegistrationId(),
                            e.getStudentId(),
                            e.getStudentName() != null ? e.getStudentName() : "",
                            e.getDepartment() != null ? e.getDepartment() : "",
                            e.getCourseId(),
                            e.getCourseName() != null ? e.getCourseName() : "",
                            category,
                            duration,
                            maxCap,
                            e.getRegistrationDate() != null ? e.getRegistrationDate().toString() : "",
                            e.getEnrollmentStatus(),
                            compStatus,
                            compDate
                    );
                }
            }
            System.out.println("[INFO] Enrollment data successfully exported to: " + filePath);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export CSV: " + e.getMessage());
        }
    }
}
