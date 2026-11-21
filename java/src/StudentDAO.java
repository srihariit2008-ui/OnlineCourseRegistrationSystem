package src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Data Access Object (DAO) for Student entities.
 * Implements CRUD, Search, and Validation rules using PreparedStatement.
 * Supports fallback in-memory cache when standalone/testing without MySQL server.
 * Syllabus Reference: Java Programming Unit 4 & Unit 5.
 */
public class StudentDAO {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // In-memory fallback repository for standalone testing and environment resilience
    private static final Map<String, Student> memStore = new ConcurrentHashMap<>();

    static {
        // Seed default sample students in memory
        seedDefaultMemory();
    }

    private static void seedDefaultMemory() {
        if (memStore.isEmpty()) {
            memStore.put("STU101", new Student("STU101", "Aarav Sharma", "aarav.sharma@institution.edu", "9876543210", "Information Technology", 3));
            memStore.put("STU102", new Student("STU102", "Bhavna Patel", "bhavna.patel@institution.edu", "9876543211", "Computer Science", 3));
            memStore.put("STU103", new Student("STU103", "Chirag Sundaram", "chirag.s@institution.edu", "9876543212", "Electronics & Comm", 2));
            memStore.put("STU104", new Student("STU104", "Divya Nair", "divya.nair@institution.edu", "9876543213", "Information Technology", 4));
            memStore.put("STU105", new Student("STU105", "Eshwar Reddy", "eshwar.reddy@institution.edu", "9876543214", "Artificial Intelligence", 2));
            memStore.put("STU106", new Student("STU106", "Farhan Ali", "farhan.ali@institution.edu", "9876543215", "Computer Science", 4));
            memStore.put("STU107", new Student("STU107", "Gayathri Menon", "gayathri.m@institution.edu", "9876543216", "Mechanical Engg", 1));
            memStore.put("STU108", new Student("STU108", "Hari Krishnan", "hari.k@institution.edu", "9876543217", "Information Technology", 2));
            memStore.put("STU109", new Student("STU109", "Indira Priyadarsh", "indira.p@institution.edu", "9876543218", "Artificial Intelligence", 3));
            memStore.put("STU110", new Student("STU110", "Jitendra Varma", "jiten.varma@institution.edu", "9876543219", "Computer Science", 1));
        }
    }

    /**
     * Validates student data integrity before persistence.
     */
    public void validateStudent(Student student) throws CustomExceptions.ValidationException {
        if (student == null) {
            throw new CustomExceptions.ValidationException("Student details cannot be null.");
        }
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Student ID cannot be empty.");
        }
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Student Name cannot be empty.");
        }
        if (student.getEmail() == null || !EMAIL_PATTERN.matcher(student.getEmail().trim()).matches()) {
            throw new CustomExceptions.ValidationException("Invalid email format: " + student.getEmail());
        }
        if (student.getPhone() == null || student.getPhone().trim().length() < 7) {
            throw new CustomExceptions.ValidationException("Phone number must have at least 7 digits.");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new CustomExceptions.ValidationException("Department cannot be empty.");
        }
        if (student.getYearOfStudy() < 1 || student.getYearOfStudy() > 5) {
            throw new CustomExceptions.ValidationException("Year of study must be between 1 and 5.");
        }
    }

    public boolean addStudent(Student student) throws CustomExceptions.ValidationException, CustomExceptions.DuplicateRegistrationException, SQLException {
        validateStudent(student);

        if (DatabaseConnection.isDatabaseAvailable()) {
            String checkSql = "SELECT COUNT(*) FROM students WHERE student_id = ? OR email = ?";
            String insertSql = "INSERT INTO students (student_id, student_name, email, phone, department, year_of_study) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                
                checkStmt.setString(1, student.getStudentId().trim());
                checkStmt.setString(2, student.getEmail().trim());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new CustomExceptions.DuplicateRegistrationException("Student with ID " + student.getStudentId() + " or Email " + student.getEmail() + " already exists.");
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, student.getStudentId().trim());
                    insertStmt.setString(2, student.getStudentName().trim());
                    insertStmt.setString(3, student.getEmail().trim());
                    insertStmt.setString(4, student.getPhone().trim());
                    insertStmt.setString(5, student.getDepartment().trim());
                    insertStmt.setInt(6, student.getYearOfStudy());
                    
                    int rows = insertStmt.executeUpdate();
                    memStore.put(student.getStudentId().trim(), student);
                    return rows > 0;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // MySQL Duplicate Key error code
                    throw new CustomExceptions.DuplicateRegistrationException("Duplicate entry detected in database: " + e.getMessage());
                }
                throw e;
            }
        } else {
            // Standalone Fallback
            if (memStore.containsKey(student.getStudentId().trim())) {
                throw new CustomExceptions.DuplicateRegistrationException("Student ID " + student.getStudentId() + " already exists.");
            }
            boolean emailExists = memStore.values().stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(student.getEmail().trim()));
            if (emailExists) {
                throw new CustomExceptions.DuplicateRegistrationException("Student with email " + student.getEmail() + " already exists.");
            }
            memStore.put(student.getStudentId().trim(), student);
            return true;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT student_id, student_name, email, phone, department, year_of_study FROM students ORDER BY student_id";
            List<Student> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("department"),
                            rs.getInt("year_of_study")
                    ));
                }
                // sync memory cache
                list.forEach(s -> memStore.put(s.getStudentId(), s));
                return list;
            }
        } else {
            return new ArrayList<>(memStore.values()).stream()
                    .sorted((s1, s2) -> s1.getStudentId().compareTo(s2.getStudentId()))
                    .collect(Collectors.toList());
        }
    }

    public Student getStudentById(String studentId) throws SQLException {
        if (studentId == null || studentId.trim().isEmpty()) return null;
        String trimmedId = studentId.trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT student_id, student_name, email, phone, department, year_of_study FROM students WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, trimmedId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Student(
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("department"),
                                rs.getInt("year_of_study")
                        );
                    }
                }
            }
        }
        return memStore.get(trimmedId);
    }

    public boolean updateStudent(Student student) throws CustomExceptions.ValidationException, SQLException {
        validateStudent(student);
        String trimmedId = student.getStudentId().trim();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "UPDATE students SET student_name = ?, email = ?, phone = ?, department = ?, year_of_study = ? WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student.getStudentName().trim());
                stmt.setString(2, student.getEmail().trim());
                stmt.setString(3, student.getPhone().trim());
                stmt.setString(4, student.getDepartment().trim());
                stmt.setInt(5, student.getYearOfStudy());
                stmt.setString(6, trimmedId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    memStore.put(trimmedId, student);
                    return true;
                }
                return false;
            }
        } else {
            if (!memStore.containsKey(trimmedId)) return false;
            memStore.put(trimmedId, student);
            return true;
        }
    }

    public boolean deleteStudent(String studentId) throws CustomExceptions.ValidationException, SQLException {
        if (studentId == null || studentId.trim().isEmpty()) return false;
        String trimmedId = studentId.trim();

        // Check if student has active registrations
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(trimmedId);
        boolean hasActive = enrollments.stream().anyMatch(e -> !e.getEnrollmentStatus().equalsIgnoreCase("CANCELLED"));
        if (hasActive) {
            throw new CustomExceptions.ValidationException("Cannot delete Student " + trimmedId + ": Active course enrollments exist.");
        }

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "DELETE FROM students WHERE student_id = ?";
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

    public List<Student> searchStudentsByName(String namePattern) throws SQLException {
        if (namePattern == null) namePattern = "";
        String pattern = namePattern.trim().toLowerCase();

        if (DatabaseConnection.isDatabaseAvailable()) {
            String sql = "SELECT student_id, student_name, email, phone, department, year_of_study FROM students WHERE LOWER(student_name) LIKE ? ORDER BY student_name";
            List<Student> list = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + pattern + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Student(
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("department"),
                                rs.getInt("year_of_study")
                        ));
                    }
                }
            }
            return list;
        } else {
            return memStore.values().stream()
                    .filter(s -> s.getStudentName().toLowerCase().contains(pattern) || s.getStudentId().toLowerCase().contains(pattern))
                    .sorted((s1, s2) -> s1.getStudentName().compareToIgnoreCase(s2.getStudentName()))
                    .collect(Collectors.toList());
        }
    }
}
