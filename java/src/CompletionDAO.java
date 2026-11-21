package src;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Data Access Object (DAO) for Course Completion operations.
 * Implements transaction management to update enrollment status to 'COMPLETED'
 * and create corresponding completion certificate records.
 * Syllabus Reference: Java Programming Unit 3 & Unit 4; DBMS Unit 4.
 */
public class CompletionDAO {

    // In-memory fallback repository
    private static final Map<Integer, Completion> memStore = new ConcurrentHashMap<>();
    private static final AtomicInteger compIdCounter = new AtomicInteger(10);

    static {
        seedDefaultCompletions();
    }

    private static void seedDefaultCompletions() {
        if (memStore.isEmpty()) {
            addMem(1, 1, "COMPLETED", Date.valueOf("2026-02-28"));
            addMem(2, 2, "COMPLETED", Date.valueOf("2026-02-28"));
            addMem(3, 5, "COMPLETED", Date.valueOf("2026-02-25"));
            addMem(4, 8, "COMPLETED", Date.valueOf("2026-03-05"));
            addMem(5, 9, "COMPLETED", Date.valueOf("2026-03-05"));
            addMem(6, 13, "COMPLETED", Date.valueOf("2026-03-01"));
            addMem(7, 16, "COMPLETED", Date.valueOf("2026-02-20"));
            addMem(8, 17, "COMPLETED", Date.valueOf("2026-02-20"));
            addMem(9, 20, "COMPLETED", Date.valueOf("2026-02-22"));
            addMem(10, 22, "COMPLETED", Date.valueOf("2026-03-02"));
        }
    }

    private static void addMem(int cId, int rId, String status, Date d) {
        memStore.put(cId, new Completion(cId, rId, status, d));
    }

    /**
     * Marks an active registration as completed.
     * Uses database transaction to update enrollments and insert completions atomically.
     */
    public synchronized boolean markCourseCompleted(int registrationId, Date completionDate)
            throws CustomExceptions.RecordNotFoundException,
                   CustomExceptions.ValidationException,
                   SQLException {

        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        Enrollment enrollment = enrollmentDAO.getEnrollmentById(registrationId);
        if (enrollment == null) {
            throw new CustomExceptions.RecordNotFoundException("Registration ID " + registrationId + " does not exist.");
        }

        if (enrollment.getEnrollmentStatus().equalsIgnoreCase("CANCELLED")) {
            throw new CustomExceptions.ValidationException("Cannot complete a CANCELLED registration.");
        }

        if (completionDate == null) {
            completionDate = new Date(System.currentTimeMillis());
        }

        if (DatabaseConnection.isDatabaseAvailable()) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false); // BEGIN TRANSACTION

                // Check if already completed
                String checkSql = "SELECT completion_id FROM completions WHERE registration_id = ?";
                try (PreparedStatement chkStmt = conn.prepareStatement(checkSql)) {
                    chkStmt.setInt(1, registrationId);
                    try (ResultSet rs = chkStmt.executeQuery()) {
                        if (rs.next()) {
                            conn.rollback();
                            throw new CustomExceptions.ValidationException("Registration ID " + registrationId + " is already marked as completed.");
                        }
                    }
                }

                // 1. Update enrollment status
                String updateEnrollSql = "UPDATE enrollments SET enrollment_status = 'COMPLETED' WHERE registration_id = ?";
                try (PreparedStatement updStmt = conn.prepareStatement(updateEnrollSql)) {
                    updStmt.setInt(1, registrationId);
                    updStmt.executeUpdate();
                }

                // 2. Insert into completions table
                String insertCompSql = "INSERT INTO completions (registration_id, completion_status, completion_date) VALUES (?, 'COMPLETED', ?)";
                int generatedId = -1;
                try (PreparedStatement insStmt = conn.prepareStatement(insertCompSql, Statement.RETURN_GENERATED_KEYS)) {
                    insStmt.setInt(1, registrationId);
                    insStmt.setDate(2, completionDate);
                    insStmt.executeUpdate();
                    try (ResultSet keys = insStmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedId = keys.getInt(1);
                        }
                    }
                }

                conn.commit(); // COMMIT TRANSACTION

                // Sync memory store
                enrollment.setEnrollmentStatus("COMPLETED");
                Completion c = new Completion(generatedId, registrationId, "COMPLETED", completionDate);
                c.setStudentId(enrollment.getStudentId());
                c.setStudentName(enrollment.getStudentName());
                c.setCourseId(enrollment.getCourseId());
                c.setCourseName(enrollment.getCourseName());
                memStore.put(generatedId, c);

                return true;
            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
                throw e;
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
                }
            }
        } else {
            // Standalone Fallback
            boolean already = memStore.values().stream().anyMatch(c -> c.getRegistrationId() == registrationId);
            if (already) {
                throw new CustomExceptions.ValidationException("Registration ID " + registrationId + " is already marked as completed.");
            }

            enrollmentDAO.updateEnrollmentStatus(registrationId, "COMPLETED");
            int newId = compIdCounter.incrementAndGet();
            Completion c = new Completion(newId, registrationId, "COMPLETED", completionDate);
            c.setStudentId(enrollment.getStudentId());
            c.setStudentName(enrollment.getStudentName());
            c.setCourseId(enrollment.getCourseId());
            c.setCourseName(enrollment.getCourseName());
            memStore.put(newId, c);
            return true;
        }
    }

    public List<Completion> getAllCompletions() throws SQLException {
        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT c.completion_id, c.registration_id, c.completion_status, c.completion_date, " +
                         "s.student_id, s.student_name, crs.course_id, crs.course_name " +
                         "FROM completions c " +
                         "JOIN enrollments e ON c.registration_id = e.registration_id " +
                         "JOIN students s ON e.student_id = s.student_id " +
                         "JOIN courses crs ON e.course_id = crs.course_id " +
                         "ORDER BY c.completion_id";

            List<Completion> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Completion c = new Completion(
                            rs.getInt("completion_id"),
                            rs.getInt("registration_id"),
                            rs.getString("completion_status"),
                            rs.getDate("completion_date")
                    );
                    c.setStudentId(rs.getString("student_id"));
                    c.setStudentName(rs.getString("student_name"));
                    c.setCourseId(rs.getString("course_id"));
                    c.setCourseName(rs.getString("course_name"));
                    list.add(c);
                    memStore.put(c.getCompletionId(), c);
                }
            }
            return list;
        } else {
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            List<Completion> list = new ArrayList<>();
            for (Completion c : memStore.values()) {
                Enrollment e = enrollmentDAO.getEnrollmentById(c.getRegistrationId());
                if (e != null) {
                    c.setStudentId(e.getStudentId());
                    c.setStudentName(e.getStudentName());
                    c.setCourseId(e.getCourseId());
                    c.setCourseName(e.getCourseName());
                }
                list.add(c);
            }
            return list.stream().sorted((c1, c2) -> Integer.compare(c1.getCompletionId(), c2.getCompletionId())).collect(Collectors.toList());
        }
    }

    public List<Enrollment> getPendingCourses() throws SQLException {
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        List<Enrollment> all = enrollmentDAO.getAllEnrollments();
        return all.stream()
                .filter(e -> e.getEnrollmentStatus().equalsIgnoreCase("REGISTERED") ||
                             e.getEnrollmentStatus().equalsIgnoreCase("IN_PROGRESS"))
                .collect(Collectors.toList());
    }

    public List<Completion> getCompletionsByStudent(String studentId) throws SQLException {
        if (studentId == null) return new ArrayList<>();
        String trimmed = studentId.trim();

        return getAllCompletions().stream()
                .filter(c -> c.getStudentId() != null && c.getStudentId().equalsIgnoreCase(trimmed))
                .collect(Collectors.toList());
    }
}
