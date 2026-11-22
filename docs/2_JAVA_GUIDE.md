# Part 2: Java Application Presentation & Evaluation Guide
**Scenario 17: Online Course Registration and Student Enrollment Analysis System**  
**Register Numbers:** 17, 47, 77, 107 | **Curriculum Regulations:** VCET B.Tech IT R2025  
**Course Code & Title:** 25ITT32 – Java Programming  
**JDK Version:** Java SE 17 / 21 LTS (Strict adherence to Core Java, No Frameworks)

---

## 1. Objective of the Java Module

To implement an enterprise-grade Object-Oriented Management System that:
1. **Encapsulates Core Academic Entities** (`Person`, `Student`, `Course`, `Enrollment`, `Completion`).
2. **Demonstrates Inheritance & Polymorphism** through an abstract `Person` superclass extended by `Student`.
3. **Provides a Decoupled DAO Layer** with JDBC database connectivity, externalized configuration (`db.properties`), and parameterized `PreparedStatement` execution preventing SQL injection.
4. **Enforces ACID Transaction Management** to handle race conditions during course seat bookings.
5. **Utilizes Multithreaded Asynchronous Logging** using a dedicated daemon thread to write operational audit logs to disk without stalling the user.
6. **Employs Java Collections & Stream Pipelines** for in-memory filtering, sorting, and transformation.

---

## 2. Architecture & Design Patterns

The application is structured into a clean **3-Tier Layered Architecture**:

```
[Main.java (Interactive Console UI)]
         │
         ▼
[Service Layer] ────────► ReportService.java    (Tabular Reports & CSV Data Export)
                ────────► AsyncAuditLogger.java (Background Daemon Logger Thread)
         │
         ▼
[DAO Layer] ────────────► StudentDAO.java, CourseDAO.java, EnrollmentDAO.java, CompletionDAO.java
         │
         ▼
[DatabaseConnection.java] ──► JDBC Driver Manager & db.properties External Config
         │
         ▼
[Domain Model Layer] ───► Person (Abstract), Student, Course, Enrollment, Completion
```

---

## 3. Strict Syllabus Alignment (25ITT32)

| Syllabus Unit | Core Java Concept | Concrete Implementation in Code |
| :--- | :--- | :--- |
| **Unit 1: Introduction & OOP** | Encapsulation, Constructors, Static methods, Console I/O | All entities use private fields with getters/setters; formatted `Scanner` console menus in `Main.java`. |
| **Unit 2: Inheritance & Interfaces** | Abstract Classes, Method Overriding, Dynamic Method Dispatch | Abstract superclass `Person.java` extended by `Student.java`, overriding abstract method `getRoleDescription()`. |
| **Unit 3: Exceptions & Multithreading** | Custom Checked Exceptions, Daemon Threads, Blocking Queues | Custom exception hierarchy in `CustomExceptions.java`; background thread in `AsyncAuditLogger.java`. |
| **Unit 4: Strings & JDBC** | `PreparedStatement`, ACID Transactions, Formatted Strings | Parameterized SQL queries; `setAutoCommit(false)`, `commit()`, and `rollback()` in `EnrollmentDAO.java`. |
| **Unit 5: Generics & Collections** | `List<T>`, `Map<K, V>`, Lambdas, Java Streams API | Functional stream filtering (`.filter()`, `.sorted()`, `.collect()`) in `ReportService.java`. |

---

## 4. Key Classes & Code Highlights to Show the Professor

### 1. Inheritance & Dynamic Polymorphism
In [`Person.java`](../java/src/Person.java) and [`Student.java`](../java/src/Student.java):
```java
// Abstract Superclass
public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;

    public abstract String getRoleDescription(); // Dynamic Polymorphic Hook
}

// Concrete Subclass Overriding Behavior
public class Student extends Person {
    private String department;
    private int yearOfStudy;

    @Override
    public String getRoleDescription() {
        return String.format("Undergraduate Student [Dept: %s, Year: %d]", department, yearOfStudy);
    }
}
```

### 2. ACID Transactional Enrollment with Seat Capacity Check
In [`EnrollmentDAO.java`](../java/src/EnrollmentDAO.java):
```java
public synchronized boolean registerStudentForCourse(String studentId, String courseId)
        throws CourseFullException, DuplicateRegistrationException, RecordNotFoundException, DatabaseOperationException {
    
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // Begin ACID Transaction

        // 1. Verify existence of student & course
        // 2. Enforce duplicate check
        if (isAlreadyRegistered(conn, studentId, courseId)) {
            throw new DuplicateRegistrationException("Student already enrolled in this course.");
        }

        // 3. Atomically check remaining capacity
        int availableSeats = getAvailableSeats(conn, courseId);
        if (availableSeats <= 0) {
            conn.rollback(); // Abort transaction
            throw new CourseFullException("Registration rejected: Course is at maximum capacity.");
        }

        // 4. Insert enrollment record
        String sql = "INSERT INTO enrollments (student_id, course_id, registration_date, enrollment_status) VALUES (?, ?, ?, 'REGISTERED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseId);
            ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
        }

        conn.commit(); // Commit transaction atomically
        AsyncAuditLogger.log("COURSE_REGISTRATION", "Student " + studentId + " registered for " + courseId);
        return true;
    } catch (SQLException e) {
        if (conn != null) conn.rollback();
        throw new DatabaseOperationException("Transaction failed: " + e.getMessage());
    }
}
```

### 3. Custom Checked Exceptions Hierarchy
In [`CustomExceptions.java`](../java/src/CustomExceptions.java):
```java
public class CustomExceptions {
    public static class CourseFullException extends Exception {
        public CourseFullException(String message) { super(message); }
    }
    public static class DuplicateRegistrationException extends Exception {
        public DuplicateRegistrationException(String message) { super(message); }
    }
    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }
    public static class RecordNotFoundException extends Exception {
        public RecordNotFoundException(String message) { super(message); }
    }
}
```

### 4. Multithreaded Asynchronous Audit Logger
In [`AsyncAuditLogger.java`](../java/src/AsyncAuditLogger.java):
```java
public class AsyncAuditLogger implements Runnable {
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static volatile boolean running = true;

    static {
        Thread daemon = new Thread(new AsyncAuditLogger(), "AuditLoggerDaemon");
        daemon.setDaemon(true); // Daemon thread exits when JVM stops
        daemon.start();
    }

    public static void log(String action, String details) {
        String logEntry = String.format("[%s] [%s] %s", LocalDateTime.now(), action, details);
        logQueue.offer(logEntry); // Non-blocking producer
    }

    @Override
    public void run() {
        while (running || !logQueue.isEmpty()) {
            try {
                String entry = logQueue.take(); // Consumer waits on queue
                try (PrintWriter pw = new PrintWriter(new FileWriter("audit_trail.log", true))) {
                    pw.println(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

---

## 5. How to Run the Live Demo for the Professor

### Step 1: Open Terminal in the `java` folder
```powershell
cd java
# Compile all source files
javac -d bin -cp ".;lib/*" src/*.java
# Run the Main Application
java -cp "bin;lib/*" src.Main
```

### Step 2: Live Demo Sequence

1. **Option 1 (Register New Student)**:
   - Enter ID `STU111`, Name `Kavya Ramesh`, Email `kavya@vcet.edu.in`, Phone `9876543210`, Dept `IT`, Year `3`.
   - Show regex validation on email and phone.
2. **Option 4 (Enroll in Course - Success)**:
   - Enroll `STU111` into `CRS103` (Foundations of Data Science).
   - Shows live confirmation and instantaneous audit trail logging.
3. **Option 4 (Duplicate Registration Prevention Test)**:
   - Attempt to enroll `STU111` into `CRS103` again.
   - Show that the system immediately catches `DuplicateRegistrationException`.
4. **Option 4 (Capacity Exceeded Test)**:
   - Attempt to enroll into `CRS104` (already 3/3 full).
   - Show that the transaction rolls back and displays `CourseFullException: Course is at maximum capacity`.
5. **Option 6 (Cancel Registration & Seat Restoration)**:
   - Cancel an enrollment and show that available seats are instantly restored.
6. **Option 8 (Export Consolidated CSV Report)**:
   - Generates `reports/enrollment_report.csv` for data science processing.


