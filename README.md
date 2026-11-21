# ONLINE COURSE REGISTRATION AND STUDENT ENROLLMENT ANALYSIS SYSTEM
**Institutional Academic Project** | **Scenario 17** (Register Numbers: 17, 47, 77, 107)  
**Curriculum Regulations**: VCET B.Tech IT R2025

---

## 1. PROJECT OVERVIEW & PROBLEM STATEMENT

An educational institution offers a variety of certificate courses, skill development programs, and technical training sessions. Managing student enrollments manually through physical registers or disconnected spreadsheets leads to:
- Duplicate student registrations.
- Difficulties in tracking available seats in real-time, resulting in chronic over-enrollment.
- Inabilities to restore capacity when students cancel.
- Delays in identifying popular and under-enrolled courses for curriculum planning.

This project implements a complete, robust, and testable academic software solution developed strictly within the curriculum scope of four core engineering subjects:
1. **Java Programming** (Core Java SE, OOP, Inheritance, Exceptions, Multithreading, JDBC, Collections, Streams)
2. **Database Management Systems** (MySQL, 3NF/BCNF Normalization, Constraints, ACID Transactions, Views)
3. **Foundations of Data Science using Python** (Pandas, NumPy, Matplotlib, Seaborn, Exploratory Data Analysis)
4. **Software Engineering** (SRS IEEE 830, Layered Architecture, DFDs, ERDs, UML Class Models, Test Cases, V&V)

> **Important**: No external web frameworks (Spring Boot, Django, Flask, React, Node.js) or NoSQL databases are used, strictly upholding syllabus boundaries.

---

## 2. DIRECTORY STRUCTURE

```
OnlineCourseRegistrationSystem/
│
├── java/
│   ├── src/
│   │   ├── Person.java                  # Abstract Superclass (Inheritance & Polymorphism)
│   │   ├── Student.java                 # Student Entity Class
│   │   ├── Course.java                  # Course Entity Class
│   │   ├── Enrollment.java              # Registration Entity Class
│   │   ├── Completion.java              # Course Completion Model
│   │   ├── CustomExceptions.java        # Custom Checked Exceptions (CourseFull, DuplicateReg, etc.)
│   │   ├── DatabaseConnection.java      # JDBC Connection Factory & Health Diagnostics
│   │   ├── db.properties                # Externalized Database Configuration
│   │   ├── StudentDAO.java              # Student CRUD & Regex Validation
│   │   ├── CourseDAO.java               # Course CRUD, Category & Capacity Checks
│   │   ├── EnrollmentDAO.java           # Transactional Course Registration & Cancellations
│   │   ├── CompletionDAO.java           # Course Completion & Certificate Management
│   │   ├── ReportService.java           # Tabular SQL Reports & CSV Export Engine
│   │   ├── AsyncAuditLogger.java        # Daemon Thread for Asynchronous Audit Logging
│   │   ├── Main.java                    # Menu-Driven Console Application
│   │   └── SystemTester.java            # Automated Test Suite (14 Core Tests)
│   ├── bin/                             # Compiled Bytecode (.class files)
│   ├── lib/                             # Directory for MySQL Connector/J Driver jar
│   └── README.md                        # Java Module Guide
│
├── database/
│   └── database.sql                     # Full DDL, Relational Schema, Constraints, Views, Sample Data
│
├── python/
│   ├── analysis.py                      # 5-Stage Data Science & EDA Engine (Pandas, NumPy, Visualizations)
│   ├── database_connection.py           # MySQL Connector & Property Parser
│   └── requirements.txt                 # Python Dependency Specification
│
├── reports/
│   ├── charts/                          # Generated Visualizations (SVG & PNG)
│   │   ├── course_wise_enrollment.svg
│   │   ├── category_distribution.svg
│   │   ├── top_5_popular_courses.svg
│   │   ├── available_seats_per_course.svg
│   │   ├── completed_vs_pending.svg
│   │   └── student_course_heatmap.svg
│   ├── enrollment_report.csv            # Processed Dataset Exported from Database
│   └── analysis_summary.txt             # Statistical Summary Report
│
├── documentation/
│   ├── SRS.md                           # Software Requirements Specification (IEEE 830 Standard)
│   ├── SystemDesign.md                  # System Architecture, DFDs (Levels 0, 1), Transaction Workflows
│   ├── ERDiagram.md                     # Entity-Relationship Model, Crow's Foot ERD, Normalization (1NF-BCNF)
│   ├── ClassDiagram.md                  # UML Class Diagram & Syllabus Mappings
│   ├── TestPlan.md                      # Testing Strategy, V&V, White-Box & Black-Box Quality Metrics
│   └── TestCases.md                     # 17 Verified Test Cases & Pass/Fail Matrix
│
└── README.md                            # Comprehensive Master Project Documentation
```

---

## 3. STRICT SYLLABUS ALIGNMENT

### A. Java Programming (Course Code: 25ITT32)
- **Unit 1 (Intro & OOP)**: Encapsulation, default & parameterized constructors, static methods, Scanner console I/O, control flow switch expressions.
- **Unit 2 (Inheritance & Interfaces)**: Abstract class `Person` extended by `Student`; dynamic method dispatch overriding `getRoleDescription()`.
- **Unit 3 (Exceptions & Multithreading)**: Custom checked exceptions (`CourseFullException`, `DuplicateRegistrationException`, `ValidationException`, `RecordNotFoundException`); daemon thread `AsyncAuditLogger` writing operational audit trails concurrently.
- **Unit 4 (Strings & JDBC)**: `PreparedStatement` preventing SQL injection; `StringBuilder` and formatted strings; ACID transaction management (`setAutoCommit(false)`, `commit()`, `rollback()`).
- **Unit 5 (Generics & Collections)**: `List<Student>`, `Map<String, Course>`, generic stream pipelines with Lambda expressions (`.filter()`, `.sorted()`, `.collect()`).

### B. Database Management Systems (Course Code: 25ITT31)
- **Unit 1 (System Design & ER)**: Entities, attributes, primary keys, foreign keys, mapping cardinalities ($1:N$, $1:1$).
- **Unit 2 (SQL & Views)**: Parameterized queries, aggregate functions (`COUNT`, `ROUND`, `SUM`), joins (`LEFT JOIN`, `INNER JOIN`), and database views (`vw_course_enrollment_summary`, `vw_popular_courses`).
- **Unit 3 (Normalization)**: Relational schema verified in 1NF, 2NF, 3NF, and BCNF to eliminate insertion, deletion, and update anomalies.
- **Unit 4 (Transactions & Concurrency)**: Atomicity and consistency during enrollment capacity checks; exclusive row-locking simulation (`FOR UPDATE`).

### C. Foundations of Data Science using Python (Course Code: 25ITC31)
- **Unit 1 & 2 (Data Description & Python Basics)**: Statistical measures (mean, max, sums), data structures (lists, dictionaries), CSV file processing.
- **Unit 3 & 4 (Pandas & NumPy Wrangling)**: Data cleaning, missing value checks, duplicate filtering, boolean masking, grouping (`.groupby()`), and pivot tables (`pd.crosstab()`).
- **Unit 5 (EDA & Visualization)**: Matplotlib and Seaborn charts:
  1. Course-wise enrollment bar chart
  2. Course category distribution pie chart
  3. Top 5 popular courses bar chart
  4. Available seats per course horizontal bar chart
  5. Completed versus pending enrollments pie chart
  6. Student-course cross-tabulation heatmap

### D. Software Engineering (Course Code: 25ITT33)
- **Unit 1 & 2 (Process & Requirements)**: Waterfall-Iterative lifecycle model, IEEE 830 standard SRS document, functional and non-functional requirements.
- **Unit 3 (System Design)**: Layered architectural style, Level 0 & Level 1 Data Flow Diagrams (DFDs), UML Class Diagram.
- **Unit 4 (Testing & Quality)**: Verification & Validation (V&V), Equivalence Partitioning (EP), Boundary Value Analysis (BVA), Basis Path Testing, and a 17-point test execution matrix.

---

## 4. SETUP AND EXECUTION GUIDE

### Step 1: Database Setup (MySQL)
1. Start your local MySQL Server (Default Port: `3306`).
2. Log into MySQL client or MySQL Workbench:
   ```bash
   mysql -u root -p
   ```
3. Execute the database script:
   ```sql
   source /path/to/OnlineCourseRegistrationSystem/database/database.sql;
   ```
4. Verify database creation:
   ```sql
   USE online_course_registration;
   SHOW TABLES;
   SELECT * FROM vw_course_enrollment_summary;
   ```

### Step 2: Configure Database Credentials
Edit `OnlineCourseRegistrationSystem/java/src/db.properties` if your MySQL username or password differs:
```properties
db.url=jdbc:mysql://localhost:3306/online_course_registration?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
db.driver=com.mysql.cj.jdbc.Driver
```

### Step 3: Compile and Run the Java Application
1. Open PowerShell or Command Prompt and navigate to the `java` directory:
   ```bash
   cd OnlineCourseRegistrationSystem/java
   ```
2. Compile all source code into the `bin/` directory:
   ```bash
   javac -d bin src/*.java
   ```
3. Run the automated test suite:
   ```bash
   java -cp bin src.SystemTester
   ```
4. Launch the interactive console application:
   ```bash
   java -cp bin src.Main
   ```
   *(Note: To connect to live MySQL, include the MySQL Connector/J driver: `java -cp "bin;lib/*" src.Main`)*

### Step 4: Run the Python Data Science Analytics Module
1. Navigate to the `python` directory:
   ```bash
   cd OnlineCourseRegistrationSystem/python
   ```
2. Install Python dependencies (optional for standard Python runtime, required for 300 DPI Matplotlib PNGs):
   ```bash
   pip install -r requirements.txt
   ```
3. Run the analysis engine:
   ```bash
   python analysis.py
   ```
4. Inspect the generated outputs:
   - Summary Report: `reports/analysis_summary.txt`
   - Processed Dataset: `reports/enrollment_report.csv`
   - Visual Charts: `reports/charts/*.svg` and `reports/charts/*.png`

---

## 5. AUTOMATED TEST RESULTS SUMMARY

All 17 required academic test scenarios were executed with 100% success rate:
- **Total Test Cases**: 17
- **Passed**: 17
- **Failed**: 0
- **Pass Rate**: 100%

Key validated edge cases include:
- Rejection of duplicate student IDs (`DuplicateRegistrationException`).
- Prevention of duplicate registrations for the same student and course.
- Rejection of registration when course capacity is full (`CourseFullException`).
- Automatic restoration of course available seats when a registration is cancelled.
- Atomicity of marking active enrollments completed and recording completion certificates.
- Resilient fallback execution when database is offline.
