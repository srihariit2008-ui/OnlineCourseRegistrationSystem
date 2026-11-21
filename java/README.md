# JAVA APPLICATION MODULE
## Online Course Registration and Student Enrollment Analysis System

---

## 1. OVERVIEW
This module implements the core business logic, input validation, transaction management, and interactive console interface for the Online Course Registration System using **pure Core Java (SE 21)** and **JDBC**.

### Key Architectural Characteristics
- **No Heavy Frameworks**: Pure Java standard library and standard JDBC (`java.sql.*`).
- **Object-Oriented Design**: Abstract class `Person`, concrete class `Student` demonstrating inheritance and dynamic method dispatch.
- **Checked Exception Hierarchy**: Domain-specific business exceptions (`CourseFullException`, `DuplicateRegistrationException`, `RecordNotFoundException`, `ValidationException`).
- **ACID Transaction Management**: Multi-step registration and completion workflows executed under `connection.setAutoCommit(false)`, `commit()`, and `rollback()`.
- **Multithreading**: `AsyncAuditLogger` runs a daemon thread to asynchronously write user actions to an audit trail file (`audit_trail.log`) without blocking the console UI.
- **Dual Runtime Support**: Connects to live MySQL database via JDBC, and includes a resilient in-memory fallback store seeded with sample academic data for zero-crash testing when MySQL is offline.

---

## 2. FILE STRUCTURE & CLASS ROLES

| File | Description & Syllabus Concepts |
| :--- | :--- |
| `Person.java` | Abstract superclass with common person attributes (`id`, `name`, `email`, `phone`) and abstract method `getRoleDescription()`. |
| `Student.java` | Subclass extending `Person`. Adds `department` and `yearOfStudy`. Implements polymorphism and method overriding. |
| `Course.java` | Course domain model (`courseId`, `courseName`, `category`, `duration`, `maxCapacity`, `activeEnrollments`, `availableSeats`). |
| `Enrollment.java` | Enrollment mapping model representing student registration with enrollment statuses. |
| `Completion.java` | Represents certified course completion details. |
| `CustomExceptions.java` | Defines custom checked exceptions for course full, duplicate registration, validation, and missing records. |
| `DatabaseConnection.java` | Manages JDBC connections, parses `db.properties` / environment variables, and provides connection diagnostics. |
| `db.properties` | External configuration file for database host, port, user, password, and driver class. |
| `StudentDAO.java` | CRUD operations for students using `PreparedStatement` and email regex validation. |
| `CourseDAO.java` | CRUD operations for courses, category checks, and capacity calculations. |
| `EnrollmentDAO.java` | Transactional course registration, capacity verification with database locks, and cancellation workflows. |
| `CompletionDAO.java` | Atomically updates enrollment status and inserts completion certificates. |
| `ReportService.java` | Generates 8 tabular reports using SQL queries, joins, Java streams, and exports CSV. |
| `AsyncAuditLogger.java` | Concurrent worker thread for logging operations to disk asynchronously. |
| `Main.java` | Interactive menu-driven console user interface with full validation and confirmation prompts. |
| `SystemTester.java` | Automated test runner executing 14+ unit and integration test cases with a pass/fail matrix. |

---

## 3. HOW TO COMPILE AND RUN

### Prerequisites
- JDK 21 or JDK 17 installed and added to `PATH`.

### Step 1: Navigate to the `java` directory
```bash
cd OnlineCourseRegistrationSystem/java
```

### Step 2: Compile all Java source files
```bash
javac -d bin src/*.java
```

### Step 3: Run the Automated Test Suite
```bash
java -cp bin src.SystemTester
```

### Step 4: Run the Interactive Console Application
```bash
java -cp bin src.Main
```

### Optional: Running with MySQL Connector/J Driver (Live Database)
Place `mysql-connector-j-8.x.x.jar` in `java/lib/` or specify its classpath:
```bash
# On Windows PowerShell:
java -cp "bin;lib/*" src.Main

# On Linux / macOS:
java -cp "bin:lib/*" src.Main
```
