package src;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application Class providing an interactive Console Menu Interface.
 * Implements all 7 functional modules and system exit.
 * Demonstrates: OOP Design, Switch Expressions, Exception Handling,
 * Formatted Console Output, and Input Validation.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final CourseDAO courseDAO = new CourseDAO();
    private static final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private static final CompletionDAO completionDAO = new CompletionDAO();
    private static final ReportService reportService = new ReportService();

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("   ONLINE COURSE REGISTRATION & ENROLLMENT ANALYSIS SYSTEM");
        System.out.println("      Institutional Academic Management Platform (v1.0)");
        System.out.println("=".repeat(60));

        // Initial database connectivity check
        checkDatabaseStatus();

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = readInt("Enter choice (1-8): ", 1, 8);
            System.out.println();
            switch (choice) {
                case 1 -> handleStudentManagement();
                case 2 -> handleCourseManagement();
                case 3 -> handleCourseRegistration();
                case 4 -> handleEnrollmentManagement();
                case 5 -> handleCourseCompletionManagement();
                case 6 -> handleViewReports();
                case 7 -> handleSearchRecords();
                case 8 -> {
                    System.out.println("Exiting system. Saving audit trails...");
                    AsyncAuditLogger.log("SYSTEM_SHUTDOWN", "Application exited gracefully by user.");
                    AsyncAuditLogger.stopLogger();
                    System.out.println("Thank you for using Online Course Registration System. Goodbye!");
                    running = false;
                }
            }
        }
    }

    private static void checkDatabaseStatus() {
        System.out.print("[INFO] Checking MySQL Database connection... ");
        if (DatabaseConnection.isDatabaseAvailable()) {
            System.out.println("CONNECTED (Live MySQL Mode).");
            AsyncAuditLogger.log("DB_CHECK", "Connected to MySQL at " + DatabaseConnection.getDbUrl());
        } else {
            System.out.println("OFFLINE / STANDALONE MODE.");
            System.out.println("       Running in resilient in-memory mode with full seed data.");
            System.out.println("       To connect to MySQL, ensure MySQL is running on port 3306 and execute database/database.sql.");
            AsyncAuditLogger.log("DB_CHECK", "MySQL offline. Active in resilient in-memory fallback mode.");
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("========== ONLINE COURSE REGISTRATION SYSTEM ==========");
        System.out.println("=".repeat(50));
        System.out.println(" 1. Student Management");
        System.out.println(" 2. Course Management");
        System.out.println(" 3. Course Registration");
        System.out.println(" 4. Enrollment Management");
        System.out.println(" 5. Course Completion Management");
        System.out.println(" 6. View Reports");
        System.out.println(" 7. Search Records");
        System.out.println(" 8. Exit");
        System.out.println("=".repeat(50));
    }

    // =========================================================================
    // MODULE 1: STUDENT MANAGEMENT
    // =========================================================================
    private static void handleStudentManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 1: STUDENT MANAGEMENT] ---");
            System.out.println("1. Add a new student");
            System.out.println("2. View all students");
            System.out.println("3. Search student using Student ID");
            System.out.println("4. Update student details");
            System.out.println("5. Delete student details");
            System.out.println("6. Return to Main Menu");
            int choice = readInt("Enter choice (1-6): ", 1, 6);

            switch (choice) {
                case 1 -> {
                    System.out.println("\n-- Add New Student --");
                    String id = readNonEmptyString("Enter Student ID (e.g. STU111): ");
                    String name = readNonEmptyString("Enter Student Name: ");
                    String email = readNonEmptyString("Enter Email Address: ");
                    String phone = readNonEmptyString("Enter Phone Number: ");
                    String dept = readNonEmptyString("Enter Department (e.g. Information Technology): ");
                    int year = readInt("Enter Year of Study (1-5): ", 1, 5);

                    try {
                        Student s = new Student(id, name, email, phone, dept, year);
                        boolean added = studentDAO.addStudent(s);
                        if (added) {
                            System.out.println("[SUCCESS] Student " + id + " (" + name + ") successfully registered!");
                            AsyncAuditLogger.log("ADD_STUDENT", "Added student " + id);
                        }
                    } catch (CustomExceptions.ValidationException | CustomExceptions.DuplicateRegistrationException e) {
                        System.out.println("[VALIDATION ERROR] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[ERROR] Failed to add student: " + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        reportService.printCompleteStudentReport();
                    } catch (Exception e) {
                        System.out.println("[ERROR] Could not fetch student list: " + e.getMessage());
                    }
                }
                case 3 -> {
                    String id = readNonEmptyString("Enter Student ID to search: ");
                    try {
                        Student s = studentDAO.getStudentById(id);
                        if (s != null) {
                            System.out.println("\n[STUDENT FOUND]:");
                            System.out.println("ID         : " + s.getStudentId());
                            System.out.println("Name       : " + s.getStudentName());
                            System.out.println("Email      : " + s.getEmail());
                            System.out.println("Phone      : " + s.getPhone());
                            System.out.println("Department : " + s.getDepartment());
                            System.out.println("Year       : " + s.getYearOfStudy());
                            System.out.println("Role Info  : " + s.getRoleDescription());
                        } else {
                            System.out.println("[NOT FOUND] No student found with ID: " + id);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Error searching student: " + e.getMessage());
                    }
                }
                case 4 -> {
                    String id = readNonEmptyString("Enter Student ID to update: ");
                    try {
                        Student s = studentDAO.getStudentById(id);
                        if (s == null) {
                            System.out.println("[NOT FOUND] No student found with ID: " + id);
                            break;
                        }
                        System.out.println("Updating Student: " + s.getStudentName());
                        String name = readOptionalString("Enter new Name (leave blank to keep '" + s.getStudentName() + "'): ", s.getStudentName());
                        String email = readOptionalString("Enter new Email (leave blank to keep '" + s.getEmail() + "'): ", s.getEmail());
                        String phone = readOptionalString("Enter new Phone (leave blank to keep '" + s.getPhone() + "'): ", s.getPhone());
                        String dept = readOptionalString("Enter new Department (leave blank to keep '" + s.getDepartment() + "'): ", s.getDepartment());
                        System.out.print("Enter new Year (1-5, leave blank to keep " + s.getYearOfStudy() + "): ");
                        String yStr = scanner.nextLine().trim();
                        int year = yStr.isEmpty() ? s.getYearOfStudy() : Integer.parseInt(yStr);

                        Student updated = new Student(id, name, email, phone, dept, year);
                        boolean ok = studentDAO.updateStudent(updated);
                        if (ok) {
                            System.out.println("[SUCCESS] Student details updated successfully!");
                            AsyncAuditLogger.log("UPDATE_STUDENT", "Updated student " + id);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Update failed: " + e.getMessage());
                    }
                }
                case 5 -> {
                    String id = readNonEmptyString("Enter Student ID to delete: ");
                    if (confirmAction("Are you sure you want to delete Student '" + id + "'? (y/n): ")) {
                        try {
                            boolean deleted = studentDAO.deleteStudent(id);
                            if (deleted) {
                                System.out.println("[SUCCESS] Student " + id + " deleted successfully.");
                                AsyncAuditLogger.log("DELETE_STUDENT", "Deleted student " + id);
                            } else {
                                System.out.println("[NOT FOUND] Student " + id + " does not exist.");
                            }
                        } catch (CustomExceptions.ValidationException e) {
                            System.out.println("[SAFEGUARD ACTIVE] " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("[ERROR] Deletion failed: " + e.getMessage());
                        }
                    } else {
                        System.out.println("[CANCELLED] Deletion aborted.");
                    }
                }
                case 6 -> back = true;
            }
        }
    }

    // =========================================================================
    // MODULE 2: COURSE MANAGEMENT
    // =========================================================================
    private static void handleCourseManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 2: COURSE MANAGEMENT] ---");
            System.out.println("1. Add a new course");
            System.out.println("2. View all courses");
            System.out.println("3. Search courses");
            System.out.println("4. Update course information");
            System.out.println("5. Delete courses");
            System.out.println("6. Return to Main Menu");
            int choice = readInt("Enter choice (1-6): ", 1, 6);

            switch (choice) {
                case 1 -> {
                    System.out.println("\n-- Add New Course --");
                    String id = readNonEmptyString("Enter Course ID (e.g. CRS109): ");
                    String name = readNonEmptyString("Enter Course Name: ");
                    System.out.println("Select Category:");
                    System.out.println(" 1. Certificate Course");
                    System.out.println(" 2. Skill Development");
                    System.out.println(" 3. Technical Training");
                    int catChoice = readInt("Category choice (1-3): ", 1, 3);
                    String category = CourseDAO.VALID_CATEGORIES.get(catChoice - 1);
                    String duration = readNonEmptyString("Enter Duration (e.g. 40 Hours): ");
                    int capacity = readInt("Enter Maximum Capacity (> 0): ", 1, 500);
                    String desc = readNonEmptyString("Enter Description: ");

                    try {
                        Course c = new Course(id, name, category, duration, capacity, desc);
                        boolean added = courseDAO.addCourse(c);
                        if (added) {
                            System.out.println("[SUCCESS] Course " + id + " (" + name + ") successfully added!");
                            AsyncAuditLogger.log("ADD_COURSE", "Added course " + id);
                        }
                    } catch (CustomExceptions.ValidationException | CustomExceptions.DuplicateRegistrationException e) {
                        System.out.println("[VALIDATION ERROR] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[ERROR] Failed to add course: " + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        reportService.printCompleteCourseReport();
                    } catch (Exception e) {
                        System.out.println("[ERROR] Could not fetch courses: " + e.getMessage());
                    }
                }
                case 3 -> {
                    String query = readNonEmptyString("Enter search keyword (Course ID, Name, or Category): ");
                    try {
                        List<Course> results = courseDAO.searchCourses(query);
                        if (results.isEmpty()) {
                            System.out.println("[NOT FOUND] No courses matching: " + query);
                        } else {
                            System.out.println("\n[SEARCH RESULTS - " + results.size() + " Course(s)]:");
                            results.forEach(System.out::println);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Search error: " + e.getMessage());
                    }
                }
                case 4 -> {
                    String id = readNonEmptyString("Enter Course ID to update: ");
                    try {
                        Course c = courseDAO.getCourseById(id);
                        if (c == null) {
                            System.out.println("[NOT FOUND] No course found with ID: " + id);
                            break;
                        }
                        String name = readOptionalString("Enter new Name (leave blank to keep '" + c.getCourseName() + "'): ", c.getCourseName());
                        System.out.println("Current Category: " + c.getCategory());
                        System.out.print("Update category? (y/n): ");
                        String changeCat = scanner.nextLine().trim();
                        String category = c.getCategory();
                        if (changeCat.equalsIgnoreCase("y")) {
                            System.out.println(" 1. Certificate Course\n 2. Skill Development\n 3. Technical Training");
                            int cat = readInt("Select (1-3): ", 1, 3);
                            category = CourseDAO.VALID_CATEGORIES.get(cat - 1);
                        }
                        String duration = readOptionalString("Enter new Duration (leave blank to keep '" + c.getDuration() + "'): ", c.getDuration());
                        System.out.print("Enter new Capacity (leave blank to keep " + c.getMaxCapacity() + "): ");
                        String capStr = scanner.nextLine().trim();
                        int cap = capStr.isEmpty() ? c.getMaxCapacity() : Integer.parseInt(capStr);
                        String desc = readOptionalString("Enter new Description: ", c.getDescription());

                        Course updated = new Course(id, name, category, duration, cap, desc);
                        boolean ok = courseDAO.updateCourse(updated);
                        if (ok) {
                            System.out.println("[SUCCESS] Course updated successfully!");
                            AsyncAuditLogger.log("UPDATE_COURSE", "Updated course " + id);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Update failed: " + e.getMessage());
                    }
                }
                case 5 -> {
                    String id = readNonEmptyString("Enter Course ID to delete: ");
                    if (confirmAction("Are you sure you want to delete Course '" + id + "'? (y/n): ")) {
                        try {
                            boolean deleted = courseDAO.deleteCourse(id);
                            if (deleted) {
                                System.out.println("[SUCCESS] Course " + id + " deleted successfully.");
                                AsyncAuditLogger.log("DELETE_COURSE", "Deleted course " + id);
                            } else {
                                System.out.println("[NOT FOUND] Course " + id + " does not exist.");
                            }
                        } catch (CustomExceptions.ValidationException e) {
                            System.out.println("[SAFEGUARD ACTIVE] " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("[ERROR] Deletion failed: " + e.getMessage());
                        }
                    } else {
                        System.out.println("[CANCELLED] Deletion aborted.");
                    }
                }
                case 6 -> back = true;
            }
        }
    }

    // =========================================================================
    // MODULE 3: COURSE REGISTRATION
    // =========================================================================
    private static void handleCourseRegistration() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 3: COURSE REGISTRATION] ---");
            System.out.println("1. Register a student for a course");
            System.out.println("2. View available seats for all courses");
            System.out.println("3. Return to Main Menu");
            int choice = readInt("Enter choice (1-3): ", 1, 3);

            switch (choice) {
                case 1 -> {
                    System.out.println("\n-- Register Student for Course --");
                    String studentId = readNonEmptyString("Enter Student ID: ");
                    String courseId = readNonEmptyString("Enter Course ID: ");

                    try {
                        int regId = enrollmentDAO.registerStudentForCourse(studentId, courseId);
                        System.out.println("\n" + "*".repeat(60));
                        System.out.println("       REGISTRATION CONFIRMED SUCCESSFULLY!");
                        System.out.println("*".repeat(60));
                        System.out.println(" Registration ID : #" + regId);
                        System.out.println(" Student ID      : " + studentId);
                        System.out.println(" Course ID       : " + courseId);
                        System.out.println(" Status          : REGISTERED");
                        System.out.println(" Date            : " + new Date(System.currentTimeMillis()));
                        System.out.println("*".repeat(60));
                        AsyncAuditLogger.log("REGISTER_COURSE", "Student " + studentId + " registered for " + courseId + " (Reg #" + regId + ")");
                    } catch (CustomExceptions.RecordNotFoundException | CustomExceptions.DuplicateRegistrationException | CustomExceptions.CourseFullException e) {
                        System.out.println("\n[REGISTRATION REJECTED] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("\n[SYSTEM ERROR] Registration failed: " + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        reportService.printAvailableSeatReport();
                    } catch (Exception e) {
                        System.out.println("[ERROR] Could not load available seats: " + e.getMessage());
                    }
                }
                case 3 -> back = true;
            }
        }
    }

    // =========================================================================
    // MODULE 4: ENROLLMENT MANAGEMENT
    // =========================================================================
    private static void handleEnrollmentManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 4: ENROLLMENT MANAGEMENT] ---");
            System.out.println("1. View all enrollments");
            System.out.println("2. View enrollments for a particular student");
            System.out.println("3. View enrollments for a particular course");
            System.out.println("4. Update enrollment status");
            System.out.println("5. Cancel a registration");
            System.out.println("6. Return to Main Menu");
            int choice = readInt("Enter choice (1-6): ", 1, 6);

            switch (choice) {
                case 1 -> {
                    try {
                        List<Enrollment> list = enrollmentDAO.getAllEnrollments();
                        System.out.println("\n[ALL ENROLLMENTS - Total: " + list.size() + "]:");
                        list.forEach(System.out::println);
                    } catch (Exception e) {
                        System.out.println("[ERROR] Could not fetch enrollments: " + e.getMessage());
                    }
                }
                case 2 -> {
                    String sId = readNonEmptyString("Enter Student ID: ");
                    try {
                        List<Enrollment> list = enrollmentDAO.getEnrollmentsByStudent(sId);
                        if (list.isEmpty()) {
                            System.out.println("[INFO] No enrollments found for student: " + sId);
                        } else {
                            System.out.println("\n[ENROLLMENTS FOR STUDENT " + sId + "]:");
                            list.forEach(System.out::println);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Fetch failed: " + e.getMessage());
                    }
                }
                case 3 -> {
                    String cId = readNonEmptyString("Enter Course ID: ");
                    try {
                        List<Enrollment> list = enrollmentDAO.getEnrollmentsByCourse(cId);
                        if (list.isEmpty()) {
                            System.out.println("[INFO] No enrollments found for course: " + cId);
                        } else {
                            System.out.println("\n[ENROLLMENTS FOR COURSE " + cId + "]:");
                            list.forEach(System.out::println);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Fetch failed: " + e.getMessage());
                    }
                }
                case 4 -> {
                    int regId = readInt("Enter Registration ID to update: ", 1, Integer.MAX_VALUE);
                    System.out.println("Choose new status:");
                    System.out.println(" 1. REGISTERED\n 2. IN_PROGRESS\n 3. COMPLETED\n 4. CANCELLED");
                    int st = readInt("Choice (1-4): ", 1, 4);
                    String newStatus = EnrollmentDAO.VALID_STATUSES.get(st - 1);

                    try {
                        boolean ok = enrollmentDAO.updateEnrollmentStatus(regId, newStatus);
                        if (ok) {
                            System.out.println("[SUCCESS] Registration #" + regId + " status updated to " + newStatus);
                            AsyncAuditLogger.log("UPDATE_STATUS", "Reg #" + regId + " updated to " + newStatus);
                        } else {
                            System.out.println("[NOT FOUND] Registration #" + regId + " not found.");
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Status update failed: " + e.getMessage());
                    }
                }
                case 5 -> {
                    int regId = readInt("Enter Registration ID to cancel: ", 1, Integer.MAX_VALUE);
                    if (confirmAction("Are you sure you want to cancel Registration #" + regId + "? (y/n): ")) {
                        try {
                            boolean cancelled = enrollmentDAO.cancelRegistration(regId);
                            if (cancelled) {
                                System.out.println("[SUCCESS] Registration #" + regId + " CANCELLED successfully.");
                                System.out.println("          The available seat count has been automatically restored!");
                                AsyncAuditLogger.log("CANCEL_REGISTRATION", "Cancelled Reg #" + regId);
                            }
                        } catch (CustomExceptions.RecordNotFoundException | CustomExceptions.ValidationException e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("[ERROR] Cancellation failed: " + e.getMessage());
                        }
                    } else {
                        System.out.println("[ABORTED] Registration was not cancelled.");
                    }
                }
                case 6 -> back = true;
            }
        }
    }

    // =========================================================================
    // MODULE 5: COURSE COMPLETION MANAGEMENT
    // =========================================================================
    private static void handleCourseCompletionManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 5: COURSE COMPLETION MANAGEMENT] ---");
            System.out.println("1. Mark a course as completed");
            System.out.println("2. View completed courses");
            System.out.println("3. View pending courses");
            System.out.println("4. View student completion history");
            System.out.println("5. Return to Main Menu");
            int choice = readInt("Enter choice (1-5): ", 1, 5);

            switch (choice) {
                case 1 -> {
                    int regId = readInt("Enter Registration ID to mark completed: ", 1, Integer.MAX_VALUE);
                    System.out.print("Enter completion date (YYYY-MM-DD) or leave blank for today: ");
                    String dateStr = scanner.nextLine().trim();
                    Date date = dateStr.isEmpty() ? new Date(System.currentTimeMillis()) : Date.valueOf(dateStr);

                    try {
                        boolean ok = completionDAO.markCourseCompleted(regId, date);
                        if (ok) {
                            System.out.println("[SUCCESS] Course marked COMPLETED for Registration #" + regId + " on " + date);
                            AsyncAuditLogger.log("MARK_COMPLETED", "Reg #" + regId + " marked completed on " + date);
                        }
                    } catch (CustomExceptions.RecordNotFoundException | CustomExceptions.ValidationException e) {
                        System.out.println("[VALIDATION ERROR] " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[ERROR] Completion failed: " + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        reportService.printCourseCompletionReport();
                    } catch (Exception e) {
                        System.out.println("[ERROR] Could not fetch completions: " + e.getMessage());
                    }
                }
                case 3 -> {
                    try {
                        List<Enrollment> pending = completionDAO.getPendingCourses();
                        System.out.println("\n" + "=".repeat(90));
                        System.out.println("                      PENDING / IN-PROGRESS COURSES");
                        System.out.println("=".repeat(90));
                        pending.forEach(System.out::println);
                        System.out.println("=".repeat(90));
                        System.out.println("Total Pending Enrollments: " + pending.size());
                    } catch (Exception e) {
                        System.out.println("[ERROR] Could not fetch pending courses: " + e.getMessage());
                    }
                }
                case 4 -> {
                    String sId = readNonEmptyString("Enter Student ID to check completion history: ");
                    try {
                        List<Completion> list = completionDAO.getCompletionsByStudent(sId);
                        if (list.isEmpty()) {
                            System.out.println("[INFO] No completed courses found for student: " + sId);
                        } else {
                            System.out.println("\n[COMPLETION HISTORY FOR " + sId + " - " + list.size() + " Certificate(s)]:");
                            list.forEach(System.out::println);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] Query failed: " + e.getMessage());
                    }
                }
                case 5 -> back = true;
            }
        }
    }

    // =========================================================================
    // MODULE 6: VIEW REPORTS
    // =========================================================================
    private static void handleViewReports() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 6: VIEW REPORTS] ---");
            System.out.println(" 1. Complete student report");
            System.out.println(" 2. Complete course report");
            System.out.println(" 3. Course-wise enrollment report");
            System.out.println(" 4. Student-wise registration report");
            System.out.println(" 5. Available seat report");
            System.out.println(" 6. Course completion report");
            System.out.println(" 7. Most popular courses");
            System.out.println(" 8. Courses with low enrollment");
            System.out.println(" 9. Export enrollment report to CSV (for Python analysis)");
            System.out.println("10. Return to Main Menu");
            int choice = readInt("Enter choice (1-10): ", 1, 10);

            try {
                switch (choice) {
                    case 1 -> reportService.printCompleteStudentReport();
                    case 2 -> reportService.printCompleteCourseReport();
                    case 3 -> reportService.printCourseWiseEnrollmentReport();
                    case 4 -> reportService.printStudentWiseRegistrationReport();
                    case 5 -> reportService.printAvailableSeatReport();
                    case 6 -> reportService.printCourseCompletionReport();
                    case 7 -> reportService.printMostPopularCourses();
                    case 8 -> reportService.printLowEnrollmentCourses();
                    case 9 -> {
                        String path = "../reports/enrollment_report.csv";
                        reportService.exportEnrollmentReportToCsv(path);
                    }
                    case 10 -> back = true;
                }
            } catch (Exception e) {
                System.out.println("[ERROR] Report generation failed: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // MODULE 7: SEARCH RECORDS
    // =========================================================================
    private static void handleSearchRecords() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [MODULE 7: SEARCH RECORDS] ---");
            System.out.println("1. Search by Student ID");
            System.out.println("2. Search by Student Name");
            System.out.println("3. Search by Course ID");
            System.out.println("4. Search by Course Name");
            System.out.println("5. Search by Registration ID");
            System.out.println("6. Return to Main Menu");
            int choice = readInt("Enter choice (1-6): ", 1, 6);

            try {
                switch (choice) {
                    case 1 -> {
                        String id = readNonEmptyString("Enter Student ID: ");
                        Student s = studentDAO.getStudentById(id);
                        if (s != null) {
                            System.out.println("[RECORD FOUND]: " + s);
                        } else {
                            System.out.println("[NOT FOUND] No student found with ID: " + id);
                        }
                    }
                    case 2 -> {
                        String name = readNonEmptyString("Enter Student Name to search: ");
                        List<Student> list = studentDAO.searchStudentsByName(name);
                        if (list.isEmpty()) {
                            System.out.println("[NOT FOUND] No students matching name: " + name);
                        } else {
                            System.out.println("\nFound " + list.size() + " student(s):");
                            list.forEach(System.out::println);
                        }
                    }
                    case 3 -> {
                        String id = readNonEmptyString("Enter Course ID: ");
                        Course c = courseDAO.getCourseById(id);
                        if (c != null) {
                            System.out.println("[RECORD FOUND]: " + c);
                        } else {
                            System.out.println("[NOT FOUND] No course found with ID: " + id);
                        }
                    }
                    case 4 -> {
                        String name = readNonEmptyString("Enter Course Name keyword: ");
                        List<Course> list = courseDAO.searchCourses(name);
                        if (list.isEmpty()) {
                            System.out.println("[NOT FOUND] No courses matching: " + name);
                        } else {
                            System.out.println("\nFound " + list.size() + " course(s):");
                            list.forEach(System.out::println);
                        }
                    }
                    case 5 -> {
                        int regId = readInt("Enter Registration ID: ", 1, Integer.MAX_VALUE);
                        Enrollment e = enrollmentDAO.getEnrollmentById(regId);
                        if (e != null) {
                            System.out.println("[RECORD FOUND]: " + e);
                        } else {
                            System.out.println("[NOT FOUND] No registration found with ID: " + regId);
                        }
                    }
                    case 6 -> back = true;
                }
            } catch (Exception e) {
                System.out.println("[ERROR] Search error: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // CONSOLE INPUT UTILITIES
    // =========================================================================
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please try again.");
            }
        }
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty. Please enter a valid value.");
        }
    }

    private static String readOptionalString(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private static boolean confirmAction(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }
}
