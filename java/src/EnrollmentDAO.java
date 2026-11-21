package src;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Data Access Object (DAO) for Enrollment operations.
 * Implements transactional course registration, capacity checking,
 * duplicate prevention, and cancellation workflows.
 * Syllabus Reference: Java Programming Unit 3 & Unit 4; DBMS Unit 4 (Transactions).
 */
public class EnrollmentDAO {

    public static final List<String> VALID_STATUSES = Arrays.asList(
            "REGISTERED",
            "IN_PROGRESS",
            "COMPLETED",
            "CANCELLED"
    );

    // In-memory fallback repository
    private static final Map<Integer, Enrollment> memStore = new ConcurrentHashMap<>();
    private static final AtomicInteger regIdCounter = new AtomicInteger(25);

    static {
        seedDefaultEnrollments();
    }

    private static void seedDefaultEnrollments() {
        if (memStore.isEmpty()) {
            addMem(1,  "STU101", "CRS101", Date.valueOf("2026-01-10"), "COMPLETED");
            addMem(2,  "STU102", "CRS101", Date.valueOf("2026-01-11"), "COMPLETED");
            addMem(3,  "STU104", "CRS101", Date.valueOf("2026-01-12"), "IN_PROGRESS");
            addMem(4,  "STU108", "CRS101", Date.valueOf("2026-01-15"), "REGISTERED");

            addMem(5,  "STU102", "CRS102", Date.valueOf("2026-01-10"), "COMPLETED");
            addMem(6,  "STU105", "CRS102", Date.valueOf("2026-01-14"), "IN_PROGRESS");
            addMem(7,  "STU106", "CRS102", Date.valueOf("2026-01-16"), "REGISTERED");

            addMem(8,  "STU101", "CRS103", Date.valueOf("2026-01-08"), "COMPLETED");
            addMem(9,  "STU103", "CRS103", Date.valueOf("2026-01-12"), "COMPLETED");
            addMem(10, "STU105", "CRS103", Date.valueOf("2026-01-14"), "IN_PROGRESS");
            addMem(11, "STU108", "CRS103", Date.valueOf("2026-01-15"), "REGISTERED");
            addMem(12, "STU109", "CRS103", Date.valueOf("2026-01-16"), "REGISTERED");

            addMem(13, "STU104", "CRS104", Date.valueOf("2026-01-11"), "COMPLETED");
            addMem(14, "STU106", "CRS104", Date.valueOf("2026-01-13"), "IN_PROGRESS");
            addMem(15, "STU109", "CRS104", Date.valueOf("2026-01-15"), "REGISTERED"); // CRS104 reaches capacity 3!

            addMem(16, "STU101", "CRS105", Date.valueOf("2026-01-09"), "COMPLETED");
            addMem(17, "STU103", "CRS105", Date.valueOf("2026-01-12"), "COMPLETED");
            addMem(18, "STU107", "CRS105", Date.valueOf("2026-01-14"), "IN_PROGRESS");
            addMem(19, "STU110", "CRS105", Date.valueOf("2026-01-16"), "REGISTERED");

            addMem(20, "STU102", "CRS106", Date.valueOf("2026-01-10"), "COMPLETED");
            addMem(21, "STU106", "CRS106", Date.valueOf("2026-01-14"), "IN_PROGRESS");

            addMem(22, "STU104", "CRS107", Date.valueOf("2026-01-12"), "COMPLETED");
            addMem(23, "STU108", "CRS107", Date.valueOf("2026-01-15"), "IN_PROGRESS");

            addMem(24, "STU103", "CRS108", Date.valueOf("2026-01-13"), "REGISTERED");
            addMem(25, "STU107", "CRS108", Date.valueOf("2026-01-14"), "CANCELLED"); // Cancelled does not count
        }
    }

    private static void addMem(int id, String sId, String cId, Date d, String status) {
        memStore.put(id, new Enrollment(id, sId, cId, d, status));
    }

    /**
     * Helper to get active enrollment count for a course (excluding CANCELLED).
     */
    public int getActiveEnrollmentsForCourse(String courseId) {
        if (courseId == null) return 0;
        String trimmed = courseId.trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND enrollment_status != 'CANCELLED'";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, trimmed);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException ignored) {
            }
        }
        return (int) memStore.values().stream()
                .filter(e -> e.getCourseId().equalsIgnoreCase(trimmed) && !e.getEnrollmentStatus().equalsIgnoreCase("CANCELLED"))
                .count();
    }

    /**
     * Executes course registration with strict validation and ACID transaction management.
     * Prevents duplicate registration and rejects registration if course is full.
     */
    public synchronized int registerStudentForCourse(String studentId, String courseId)
            throws CustomExceptions.ValidationException,
                   CustomExceptions.RecordNotFoundException,
                   CustomExceptions.DuplicateRegistrationException,
                   CustomExceptions.CourseFullException,
                   SQLException {

        if (studentId == null || studentId.trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Student ID cannot be empty.");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Course ID cannot be empty.");
        }

        String trimmedStudentId = studentId.trim();
        String trimmedCourseId = courseId.trim();

        // Check if student exists
        StudentDAO studentDAO = new StudentDAO();
        Student student = studentDAO.getStudentById(trimmedStudentId);
        if (student == null) {
            throw new CustomExceptions.RecordNotFoundException("Student with ID '" + trimmedStudentId + "' does not exist.");
        }

        // Check if course exists
        CourseDAO courseDAO = new CourseDAO();
        Course course = courseDAO.getCourseById(trimmedCourseId);
        if (course == null) {
            throw new CustomExceptions.RecordNotFoundException("Course with ID '" + trimmedCourseId + "' does not exist.");
        }

        if (DatabaseConnection.isDatabaseAvailable()) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                // BEGIN TRANSACTION
                conn.setAutoCommit(false);

                // 1. Check duplicate registration
                String dupSql = "SELECT enrollment_status FROM enrollments WHERE student_id = ? AND course_id = ?";
                try (PreparedStatement dupStmt = conn.prepareStatement(dupSql)) {
                    dupStmt.setString(1, trimmedStudentId);
                    dupStmt.setString(2, trimmedCourseId);
                    try (ResultSet rs = dupStmt.executeQuery()) {
                        if (rs.next()) {
                            String existingStatus = rs.getString("enrollment_status");
                            conn.rollback();
                            throw new CustomExceptions.DuplicateRegistrationException(
                                    "Student " + trimmedStudentId + " has already registered for course " + trimmedCourseId + " (Status: " + existingStatus + ").");
                        }
                    }
                }

                // 2. Lock course capacity and calculate active enrollments
                int maxCap = course.getMaxCapacity();
                String capSql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND enrollment_status != 'CANCELLED' FOR UPDATE";
                int activeCount = 0;
                try (PreparedStatement capStmt = conn.prepareStatement(capSql)) {
                    capStmt.setString(1, trimmedCourseId);
                    try (ResultSet rs = capStmt.executeQuery()) {
                        if (rs.next()) {
                            activeCount = rs.getInt(1);
                        }
                    }
                }

                if (activeCount >= maxCap) {
                    conn.rollback();
                    throw new CustomExceptions.CourseFullException(
                            "Cannot register: Course '" + course.getCourseName() + "' (" + trimmedCourseId + ") is FULL. " +
                            "Capacity: " + maxCap + ", Active Enrollments: " + activeCount + ".");
                }

                // 3. Insert registration record
                String insertSql = "INSERT INTO enrollments (student_id, course_id, registration_date, enrollment_status) VALUES (?, ?, CURRENT_DATE, 'REGISTERED')";
                int generatedId = -1;
                try (PreparedStatement insStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insStmt.setString(1, trimmedStudentId);
                    insStmt.setString(2, trimmedCourseId);
                    insStmt.executeUpdate();
                    try (ResultSet keys = insStmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedId = keys.getInt(1);
                        }
                    }
                }

                // COMMIT TRANSACTION
                conn.commit();

                // Sync memory store
                Enrollment newEnrollment = new Enrollment(generatedId, trimmedStudentId, student.getStudentName(),
                        trimmedCourseId, course.getCourseName(), new Date(System.currentTimeMillis()), "REGISTERED");
                memStore.put(generatedId, newEnrollment);

                return generatedId;
            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
                if (e.getErrorCode() == 1062) {
                    throw new CustomExceptions.DuplicateRegistrationException("Duplicate registration detected by database unique constraint.");
                }
                throw e;
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
                }
            }
        } else {
            // Standalone Fallback Transaction Logic
            boolean alreadyRegistered = memStore.values().stream()
                    .anyMatch(e -> e.getStudentId().equalsIgnoreCase(trimmedStudentId) &&
                                   e.getCourseId().equalsIgnoreCase(trimmedCourseId));
            if (alreadyRegistered) {
                throw new CustomExceptions.DuplicateRegistrationException(
                        "Student " + trimmedStudentId + " is already registered for course " + trimmedCourseId + ".");
            }

            int activeCount = (int) memStore.values().stream()
                    .filter(e -> e.getCourseId().equalsIgnoreCase(trimmedCourseId) &&
                                 !e.getEnrollmentStatus().equalsIgnoreCase("CANCELLED"))
                    .count();

            if (activeCount >= course.getMaxCapacity()) {
                throw new CustomExceptions.CourseFullException(
                        "Cannot register: Course '" + course.getCourseName() + "' (" + trimmedCourseId + ") is FULL. " +
                        "Capacity: " + course.getMaxCapacity() + ", Active Enrollments: " + activeCount + ".");
            }

            int newId = regIdCounter.incrementAndGet();
            Enrollment newEnrollment = new Enrollment(newId, trimmedStudentId, student.getStudentName(),
                    trimmedCourseId, course.getCourseName(), new Date(System.currentTimeMillis()), "REGISTERED");
            memStore.put(newId, newEnrollment);
            return newId;
        }
    }

    public List<Enrollment> getAllEnrollments() throws SQLException {
        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT e.registration_id, e.student_id, s.student_name, s.department, " +
                         "e.course_id, c.course_name, e.registration_date, e.enrollment_status " +
                         "FROM enrollments e " +
                         "JOIN students s ON e.student_id = s.student_id " +
                         "JOIN courses c ON e.course_id = c.course_id " +
                         "ORDER BY e.registration_id";

            List<Enrollment> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment(
                            rs.getInt("registration_id"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("course_id"),
                            rs.getString("course_name"),
                            rs.getDate("registration_date"),
                            rs.getString("enrollment_status")
                    );
                    e.setDepartment(rs.getString("department"));
                    list.add(e);
                    memStore.put(e.getRegistrationId(), e);
                }
            }
            return list;
        } else {
            StudentDAO sDao = new StudentDAO();
            CourseDAO cDao = new CourseDAO();
            List<Enrollment> list = new ArrayList<>();
            for (Enrollment e : memStore.values()) {
                Student s = sDao.getStudentById(e.getStudentId());
                Course c = cDao.getCourseById(e.getCourseId());
                Enrollment copy = new Enrollment(e.getRegistrationId(), e.getStudentId(),
                        (s != null ? s.getStudentName() : "Unknown"),
                        e.getCourseId(), (c != null ? c.getCourseName() : "Unknown"),
                        e.getRegistrationDate(), e.getEnrollmentStatus());
                if (s != null) copy.setDepartment(s.getDepartment());
                list.add(copy);
            }
            return list.stream().sorted((e1, e2) -> Integer.compare(e1.getRegistrationId(), e2.getRegistrationId())).collect(Collectors.toList());
        }
    }

    public Enrollment getEnrollmentById(int registrationId) throws SQLException {
        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT e.registration_id, e.student_id, s.student_name, s.department, " +
                         "e.course_id, c.course_name, e.registration_date, e.enrollment_status " +
                         "FROM enrollments e " +
                         "JOIN students s ON e.student_id = s.student_id " +
                         "JOIN courses c ON e.course_id = c.course_id " +
                         "WHERE e.registration_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, registrationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Enrollment e = new Enrollment(
                                rs.getInt("registration_id"),
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("course_id"),
                                rs.getString("course_name"),
                                rs.getDate("registration_date"),
                                rs.getString("enrollment_status")
                        );
                        e.setDepartment(rs.getString("department"));
                        return e;
                    }
                }
            }
        }
        Enrollment e = memStore.get(registrationId);
        if (e != null) {
            StudentDAO sDao = new StudentDAO();
            CourseDAO cDao = new CourseDAO();
            Student s = sDao.getStudentById(e.getStudentId());
            Course c = cDao.getCourseById(e.getCourseId());
            if (s != null) e.setStudentName(s.getStudentName());
            if (c != null) e.setCourseName(c.getCourseName());
        }
        return e;
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentId) throws SQLException {
        if (studentId == null) return new ArrayList<>();
        String trimmed = studentId.trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT e.registration_id, e.student_id, s.student_name, s.department, " +
                         "e.course_id, c.course_name, e.registration_date, e.enrollment_status " +
                         "FROM enrollments e " +
                         "JOIN students s ON e.student_id = s.student_id " +
                         "JOIN courses c ON e.course_id = c.course_id " +
                         "WHERE e.student_id = ? " +
                         "ORDER BY e.registration_id";
            List<Enrollment> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, trimmed);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Enrollment(
                                rs.getInt("registration_id"),
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("course_id"),
                                rs.getString("course_name"),
                                rs.getDate("registration_date"),
                                rs.getString("enrollment_status")
                        ));
                    }
                }
            }
            return list;
        } else {
            return getAllEnrollments().stream()
                    .filter(e -> e.getStudentId().equalsIgnoreCase(trimmed))
                    .collect(Collectors.toList());
        }
    }

    public List<Enrollment> getEnrollmentsByCourse(String courseId) throws SQLException {
        if (courseId == null) return new ArrayList<>();
        String trimmed = courseId.trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT e.registration_id, e.student_id, s.student_name, s.department, " +
                         "e.course_id, c.course_name, e.registration_date, e.enrollment_status " +
                         "FROM enrollments e " +
                         "JOIN students s ON e.student_id = s.student_id " +
                         "JOIN courses c ON e.course_id = c.course_id " +
                         "WHERE e.course_id = ? " +
                         "ORDER BY e.registration_id";
            List<Enrollment> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, trimmed);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Enrollment(
                                rs.getInt("registration_id"),
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("course_id"),
                                rs.getString("course_name"),
                                rs.getDate("registration_date"),
                                rs.getString("enrollment_status")
                        ));
                    }
                }
            }
            return list;
        } else {
            return getAllEnrollments().stream()
                    .filter(e -> e.getCourseId().equalsIgnoreCase(trimmed))
                    .collect(Collectors.toList());
        }
    }

    public boolean updateEnrollmentStatus(int registrationId, String newStatus) throws CustomExceptions.ValidationException, SQLException {
        if (newStatus == null || !VALID_STATUSES.contains(newStatus.trim().toUpperCase())) {
            throw new CustomExceptions.ValidationException("Invalid enrollment status: '" + newStatus + "'. Allowed: " + VALID_STATUSES);
        }
        String statusUpper = newStatus.trim().toUpperCase();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "UPDATE enrollments SET enrollment_status = ? WHERE registration_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, statusUpper);
                stmt.setInt(2, registrationId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Enrollment e = memStore.get(registrationId);
                    if (e != null) e.setEnrollmentStatus(statusUpper);
                    return true;
                }
                return false;
            }
        } else {
            Enrollment e = memStore.get(registrationId);
            if (e != null) {
                e.setEnrollmentStatus(statusUpper);
                return true;
            }
            return false;
        }
    }

    public boolean cancelRegistration(int registrationId) throws CustomExceptions.RecordNotFoundException, CustomExceptions.ValidationException, SQLException {
        Enrollment e = getEnrollmentById(registrationId);
        if (e == null) {
            throw new CustomExceptions.RecordNotFoundException("Registration ID " + registrationId + " does not exist.");
        }
        if (e.getEnrollmentStatus().equalsIgnoreCase("CANCELLED")) {
            throw new CustomExceptions.ValidationException("Registration ID " + registrationId + " is already CANCELLED.");
        }
        if (e.getEnrollmentStatus().equalsIgnoreCase("COMPLETED")) {
            throw new CustomExceptions.ValidationException("Cannot cancel a registration that has already been COMPLETED.");
        }

        return updateEnrollmentStatus(registrationId, "CANCELLED");
    }
}
