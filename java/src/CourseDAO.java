package src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data Access Object (DAO) for Course entities.
 * Enforces course categories, positive capacities, active registration counts,
 * and available seat calculations.
 * Syllabus Reference: Java Programming Unit 4 & Unit 5.
 */
public class CourseDAO {

    public static final List<String> VALID_CATEGORIES = Arrays.asList(
            "Certificate Course",
            "Skill Development",
            "Technical Training"
    );

    // In-memory fallback repository
    private static final Map<String, Course> memStore = new ConcurrentHashMap<>();

    static {
        seedDefaultCourses();
    }

    private static void seedDefaultCourses() {
        if (memStore.isEmpty()) {
            memStore.put("CRS101", new Course("CRS101", "Full Stack Web Development", "Technical Training", "60 Hours", 5, "Comprehensive front-end and back-end web development with modern standards."));
            memStore.put("CRS102", new Course("CRS102", "Data Structures & Algorithms in Java", "Technical Training", "45 Hours", 5, "Core data structures, algorithm design, complexity analysis, and coding."));
            memStore.put("CRS103", new Course("CRS103", "Foundations of Data Science with Python", "Certificate Course", "60 Hours", 6, "Data preprocessing, Pandas wrangling, NumPy, and statistical visualizations."));
            memStore.put("CRS104", new Course("CRS104", "Machine Learning & Deep Neural Nets", "Technical Training", "50 Hours", 3, "Supervised learning, deep architectures, loss optimization, and evaluation."));
            memStore.put("CRS105", new Course("CRS105", "Professional Communication & Soft Skills", "Skill Development", "30 Hours", 8, "Corporate etiquette, presentation techniques, leadership, and interview prep."));
            memStore.put("CRS106", new Course("CRS106", "Agile Software Engineering & DevOps", "Skill Development", "35 Hours", 6, "Scrum workflows, continuous integration, version control, and test automation."));
            memStore.put("CRS107", new Course("CRS107", "Advanced Database Design & SQL Tuning", "Certificate Course", "40 Hours", 4, "Relational schema normalization, complex queries, transactions, and indexing."));
            memStore.put("CRS108", new Course("CRS108", "Cyber Security & Network Defense", "Certificate Course", "40 Hours", 4, "Cryptographic protocols, penetration testing fundamentals, and security audit."));
        }
    }

    public void validateCourse(Course course) throws CustomExceptions.ValidationException {
        if (course == null) {
            throw new CustomExceptions.ValidationException("Course details cannot be null.");
        }
        if (course.getCourseId() == null || course.getCourseId().trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Course ID cannot be empty.");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Course Name cannot be empty.");
        }
        if (course.getCategory() == null || !VALID_CATEGORIES.contains(course.getCategory().trim())) {
            throw new CustomExceptions.ValidationException("Invalid Category: '" + course.getCategory() + "'. Allowed: " + VALID_CATEGORIES);
        }
        if (course.getDuration() == null || course.getDuration().trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Course duration cannot be empty.");
        }
        if (course.getMaxCapacity() <= 0) {
            throw new CustomExceptions.ValidationException("Maximum enrollment capacity must be a positive number (> 0).");
        }
    }

    public boolean addCourse(Course course) throws CustomExceptions.ValidationException, CustomExceptions.DuplicateRegistrationException, SQLException {
        validateCourse(course);
        String trimmedId = course.getCourseId().trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String checkSql = "SELECT COUNT(*) FROM courses WHERE course_id = ?";
            String insertSql = "INSERT INTO courses (course_id, course_name, category, duration, max_capacity, description) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, trimmedId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new CustomExceptions.DuplicateRegistrationException("Course ID " + trimmedId + " already exists.");
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, trimmedId);
                    insertStmt.setString(2, course.getCourseName().trim());
                    insertStmt.setString(3, course.getCategory().trim());
                    insertStmt.setString(4, course.getDuration().trim());
                    insertStmt.setInt(5, course.getMaxCapacity());
                    insertStmt.setString(6, course.getDescription() != null ? course.getDescription().trim() : "");
                    int rows = insertStmt.executeUpdate();
                    memStore.put(trimmedId, course);
                    return rows > 0;
                }
            }
        } else {
            if (memStore.containsKey(trimmedId)) {
                throw new CustomExceptions.DuplicateRegistrationException("Course ID " + trimmedId + " already exists.");
            }
            memStore.put(trimmedId, course);
            return true;
        }
    }

    public List<Course> getAllCourses() throws SQLException {
        if (DatabaseConnection.isDatabaseAvailable()) {
            // Join with enrollments view to get live active enrollments and available seats
            String sql = "SELECT c.course_id, c.course_name, c.category, c.duration, c.max_capacity, c.description, " +
                         "COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS active_enrolled " +
                         "FROM courses c " +
                         "LEFT JOIN enrollments e ON c.course_id = e.course_id " +
                         "GROUP BY c.course_id, c.course_name, c.category, c.duration, c.max_capacity, c.description " +
                         "ORDER BY c.course_id";

            List<Course> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int cap = rs.getInt("max_capacity");
                    int active = rs.getInt("active_enrolled");
                    int available = Math.max(0, cap - active);
                    Course c = new Course(
                            rs.getString("course_id"),
                            rs.getString("course_name"),
                            rs.getString("category"),
                            rs.getString("duration"),
                            cap,
                            rs.getString("description"),
                            active,
                            available
                    );
                    list.add(c);
                    memStore.put(c.getCourseId(), c);
                }
            }
            return list;
        } else {
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            List<Course> list = new ArrayList<>();
            for (Course c : memStore.values()) {
                int active = enrollmentDAO.getActiveEnrollmentsForCourse(c.getCourseId());
                int avail = Math.max(0, c.getMaxCapacity() - active);
                Course copy = new Course(c.getCourseId(), c.getCourseName(), c.getCategory(), c.getDuration(), c.getMaxCapacity(), c.getDescription(), active, avail);
                list.add(copy);
            }
            return list.stream().sorted((c1, c2) -> c1.getCourseId().compareTo(c2.getCourseId())).collect(Collectors.toList());
        }
    }

    public Course getCourseById(String courseId) throws SQLException {
        if (courseId == null || courseId.trim().isEmpty()) return null;
        String trimmedId = courseId.trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT c.course_id, c.course_name, c.category, c.duration, c.max_capacity, c.description, " +
                         "COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS active_enrolled " +
                         "FROM courses c " +
                         "LEFT JOIN enrollments e ON c.course_id = e.course_id " +
                         "WHERE c.course_id = ? " +
                         "GROUP BY c.course_id, c.course_name, c.category, c.duration, c.max_capacity, c.description";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, trimmedId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int cap = rs.getInt("max_capacity");
                        int active = rs.getInt("active_enrolled");
                        return new Course(
                                rs.getString("course_id"),
                                rs.getString("course_name"),
                                rs.getString("category"),
                                rs.getString("duration"),
                                cap,
                                rs.getString("description"),
                                active,
                                Math.max(0, cap - active)
                        );
                    }
                }
            }
        }
        Course c = memStore.get(trimmedId);
        if (c != null) {
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            int active = enrollmentDAO.getActiveEnrollmentsForCourse(c.getCourseId());
            c.setActiveEnrollments(active);
            c.setAvailableSeats(Math.max(0, c.getMaxCapacity() - active));
        }
        return c;
    }

    public boolean updateCourse(Course course) throws CustomExceptions.ValidationException, SQLException {
        validateCourse(course);
        String trimmedId = course.getCourseId().trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "UPDATE courses SET course_name = ?, category = ?, duration = ?, max_capacity = ?, description = ? WHERE course_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, course.getCourseName().trim());
                stmt.setString(2, course.getCategory().trim());
                stmt.setString(3, course.getDuration().trim());
                stmt.setInt(4, course.getMaxCapacity());
                stmt.setString(5, course.getDescription() != null ? course.getDescription().trim() : "");
                stmt.setString(6, trimmedId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    memStore.put(trimmedId, course);
                    return true;
                }
                return false;
            }
        } else {
            if (!memStore.containsKey(trimmedId)) return false;
            memStore.put(trimmedId, course);
            return true;
        }
    }

    public boolean deleteCourse(String courseId) throws CustomExceptions.ValidationException, SQLException {
        if (courseId == null || courseId.trim().isEmpty()) return false;
        String trimmedId = courseId.trim();

        // Safeguard: Check if course has active registrations
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        int activeEnrollments = enrollmentDAO.getActiveEnrollmentsForCourse(trimmedId);
        if (activeEnrollments > 0) {
            throw new CustomExceptions.ValidationException("Cannot delete Course " + trimmedId + ": It has " + activeEnrollments + " active student registrations.");
        }

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "DELETE FROM courses WHERE course_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, trimmedId);
                int rows = stmt.executeUpdate();
                memStore.remove(trimmedId);
                return rows > 0;
            }
        } else {
            if (memStore.containsKey(trimmedId)) {
                memStore.remove(trimmedId);
                return true;
            }
            return false;
        }
    }

    public List<Course> searchCourses(String query) throws SQLException {
        if (query == null) query = "";
        String pattern = query.trim().toLowerCase();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT c.course_id, c.course_name, c.category, c.duration, c.max_capacity, c.description, " +
                         "COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS active_enrolled " +
                         "FROM courses c " +
                         "LEFT JOIN enrollments e ON c.course_id = e.course_id " +
                         "WHERE LOWER(c.course_id) LIKE ? OR LOWER(c.course_name) LIKE ? OR LOWER(c.category) LIKE ? " +
                         "GROUP BY c.course_id, c.course_name, c.category, c.duration, c.max_capacity, c.description " +
                         "ORDER BY c.course_id";
            List<Course> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + pattern + "%");
                stmt.setString(2, "%" + pattern + "%");
                stmt.setString(3, "%" + pattern + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int cap = rs.getInt("max_capacity");
                        int active = rs.getInt("active_enrolled");
                        list.add(new Course(
                                rs.getString("course_id"),
                                rs.getString("course_name"),
                                rs.getString("category"),
                                rs.getString("duration"),
                                cap,
                                rs.getString("description"),
                                active,
                                Math.max(0, cap - active)
                        ));
                    }
                }
            }
            return list;
        } else {
            return getAllCourses().stream()
                    .filter(c -> c.getCourseId().toLowerCase().contains(pattern) ||
                                 c.getCourseName().toLowerCase().contains(pattern) ||
                                 c.getCategory().toLowerCase().contains(pattern))
                    .collect(Collectors.toList());
        }
    }
}
