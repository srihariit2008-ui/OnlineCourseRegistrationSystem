# SRS Requirements Traceability & Compliance Matrix

## Project Title: Online Course Registration and Student Enrollment Analysis System
**Academic Scenario 17** (Register Numbers: 17, 47, 77, 107)  
**Curriculum Regulations:** VCET B.Tech IT R2025 | **Course Code:** 25ITT33 Software Engineering

This document provides a formal, comprehensive mapping between each requirement in the **Software Requirements Specification (SRS)** and the implementation artifacts across the **Java (Core SE, JDBC, OOP)**, **Database (MySQL 8.0)**, **Python (Data Science, EDA)**, and **Software Engineering** modules.

---

## 1. Functional Requirements Traceability Matrix

| Requirement ID | SRS Functional Requirement Description | Implementation Artifacts | Verification Method | Status |
| :--- | :--- | :--- | :--- | :---: |
| **FR-1.1 to FR-1.5**<br>*Student Management* | Register student, validate 10-digit phone and email regex, enforce unique student ID, view/search profiles, and restrict deletion of enrolled students. | - `database.sql` (`students` table, `uk_student_email`, `chk_student_year`)<br>- `Student.java`, `Person.java`<br>- `StudentDAO.java` (`insertStudent`, `getStudentById`, `getAllStudents`)<br>- `Main.java` (Options 1, 2, 3) | Tested via Console UI: Add student, test invalid email format, test duplicate ID insertion. Verified via `SystemTester.java` (Test 1). | **COMPLIANT** |
| **FR-2.1 to FR-2.4**<br>*Course Management* | Create courses, enforce allowable categories (`Certificate Course`, `Skill Development`, `Technical Training`), enforce positive capacity (`max_capacity > 0`), track active seats. | - `database.sql` (`courses` table, `chk_course_category`, `chk_course_capacity`)<br>- `Course.java`, `CourseDAO.java`<br>- `Main.java` (Option 4 & 5)<br>- `vw_course_enrollment_summary` | Tested via Console UI: Add course with valid/invalid categories. Database rejects non-positive capacities. | **COMPLIANT** |
| **FR-3.1 to FR-3.6**<br>*Course Registration & ACID Transactions* | Register student into course, verify student & course existence, prevent duplicate registration, check capacity in transaction, and throw `CourseFullException` when capacity is 0. | - `database.sql` (`enrollments` table, `uk_student_course`)<br>- `Enrollment.java`, `EnrollmentDAO.java` (`registerStudentForCourse`)<br>- `CustomExceptions.java` (`CourseFullException`, `DuplicateRegistrationException`)<br>- `Main.java` (Option 6) | Tested with student registering for valid course, duplicate attempt blocked with exception, and full course (`CRS104`) blocked. Verified via `SystemTester.java` (Tests 2, 3, 4). | **COMPLIANT** |
| **FR-4.1 to FR-4.3**<br>*Enrollment Status & Cancellation* | Track status (`REGISTERED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), allow cancellation with user confirmation, immediately restore seat capacity. | - `database.sql` (`chk_enrollment_status`)<br>- `EnrollmentDAO.java` (`cancelEnrollment`)<br>- `Main.java` (Option 7)<br>- `AsyncAuditLogger.java` | Cancelled record marked as `CANCELLED`. Query verified seat count increases by 1 instantly. Verified via `SystemTester.java` (Test 5). | **COMPLIANT** |
| **FR-5.1 to FR-5.3**<br>*Course Completion & Certification* | Mark active enrollment as completed, record certificate entry in `completions` table, prevent completing cancelled registrations. | - `database.sql` (`completions` table, `uk_completion_registration`)<br>- `Completion.java`, `CompletionDAO.java` (`recordCompletion`)<br>- `Main.java` (Option 8) | Atomically marks enrollment `COMPLETED` and inserts into `completions`. Cancelled registrations rejected. Verified via `SystemTester.java` (Test 6). | **COMPLIANT** |
| **FR-6.1 to FR-6.4**<br>*Reporting & SQL Analytics* | Generate tabular reports for course enrollments, available seats, completions, identify most/least popular courses, export consolidated CSV dataset. | - `ReportService.java` (`exportEnrollmentDataToCSV`, `generateSummaryReport`)<br>- `database.sql` (`vw_course_enrollment_summary`, `vw_popular_courses`)<br>- `Main.java` (Option 9) | Generates `reports/enrollment_report.csv` containing joined dataset across 4 relational tables. | **COMPLIANT** |
| **FR-7.1 to FR-7.3**<br>*Data Science Analytics & Visualizations* | Ingest database or CSV snapshot, execute 5-stage statistical exploratory data analysis, generate 6 visual charts in SVG/PNG. | - `python/analysis.py`<br>- `python/database_connection.py`<br>- `reports/analysis_summary.txt`<br>- `reports/charts/*.svg` | Python script computes course breakdown, occupancy %, and outputs 6 standalone SVG charts. | **COMPLIANT** |

---

## 2. Business Rules Compliance Matrix

| Rule ID | SRS Business Rule | Implementation & Enforcement Point | Enforcement Mechanism | Status |
| :---: | :--- | :--- | :--- | :---: |
| **BR-1** | Every student must possess a unique institutional email and student ID. | - `students.student_id` PRIMARY KEY<br>- `students.uk_student_email` UNIQUE<br>- `StudentDAO.java` pre-validation check | Enforced by MySQL unique constraints and Java regex validator. | **COMPLIANT** |
| **BR-2** | A course's active enrollment cannot exceed its defined `max_capacity`. | - `EnrollmentDAO.java` transactional check<br>- `CustomExceptions.CourseFullException`<br>- `vw_course_enrollment_summary` | Evaluated atomically under `conn.setAutoCommit(false)`. Rolled back if capacity is 0. | **COMPLIANT** |
| **BR-3** | A student cannot register for the same course more than once. | - `enrollments.uk_student_course` UNIQUE(student_id, course_id)<br>- `CustomExceptions.DuplicateRegistrationException` | Composite UNIQUE key in MySQL and DAO duplicate check. | **COMPLIANT** |
| **BR-4** | Cancelled registrations must immediately release seat vacancy. | - `database.sql` view condition (`enrollment_status != 'CANCELLED'`)<br>- `EnrollmentDAO.java` status update | Calculated dynamically so vacancy is restored without deleting historical audit rows. | **COMPLIANT** |
| **BR-5** | Only non-cancelled active registrations can be marked as completed. | - `CompletionDAO.java` verification check<br>- `completions.uk_completion_registration` | Rejects completion if status is `CANCELLED`; enforces 1:1 certificate constraint. | **COMPLIANT** |
| **BR-6** | All significant operational actions must be logged asynchronously. | - `AsyncAuditLogger.java` daemon thread<br>- Output to `audit_trail.log` | Thread-safe `LinkedBlockingQueue` decouples user I/O from disk writes. | **COMPLIANT** |

---

## 3. Technology Stack & Syllabus Compliance Matrix

| Syllabus Requirement | Regulation Syllabus Component | Implemented Artifact in Project | Status |
| :--- | :--- | :--- | :---: |
| **Java Programming (25ITT32)** | Object-Oriented Programming (OOP) | `Person.java` (abstract superclass) and `Student.java` (subclass) with method overriding. | **COMPLIANT** |
| **Java Programming (25ITT32)** | Exception Handling | 4 custom checked exceptions in `CustomExceptions.java`. | **COMPLIANT** |
| **Java Programming (25ITT32)** | Multithreaded Programming | Daemon thread with blocking queue in `AsyncAuditLogger.java`. | **COMPLIANT** |
| **Java Programming (25ITT32)** | JDBC & Transaction Control | Parameterized queries and ACID rollback/commit in `EnrollmentDAO.java`. | **COMPLIANT** |
| **Java Programming (25ITT32)** | Collections & Streams API | Generic collections and stream filters in `ReportService.java`. | **COMPLIANT** |
| **DBMS (25ITT31)** | Relational Schema & Constraints | Normalized tables in `database/database.sql` with PK, FK, UNIQUE, NOT NULL, CHECK. | **COMPLIANT** |
| **DBMS (25ITT31)** | Database Views | `vw_course_enrollment_summary` and `vw_popular_courses`. | **COMPLIANT** |
| **Data Science (25ITC31)** | Data Ingestion & Preprocessing | `python/analysis.py` with MySQL Connector and CSV fallback engine. | **COMPLIANT** |
| **Data Science (25ITC31)** | Statistical Data Visualization | 6 vector SVG charts generated in `reports/charts/`. | **COMPLIANT** |
| **Software Engineering (25ITT33)** | Verification & Testing | 14 automated unit and integration tests in `SystemTester.java`. | **COMPLIANT** |

---

## 4. Input & Output Verification Summary

### Inputs (SRS Section 8):
- **Student Profile**: ID, Name, Institutional Email, 10-digit Phone, Department, Year of Study.
- **Course Offering**: ID, Name, Category, Duration, Maximum Capacity, Description.
- **Registration**: Student ID, Course ID, Registration Date.
- **Completion**: Registration ID, Completion Date.

### Outputs (SRS Section 9):
- **Console Tables**: Tabular listings of students, courses, enrollments, completions, and vacancies.
- **Data Export**: `reports/enrollment_report.csv` containing joined relational data.
- **Statistical Report**: `reports/analysis_summary.txt` with course-wise occupancy and metrics.
- **Visual Analytics**: 6 SVG charts in `reports/charts/` covering distributions, trends, and matrices.
- **Audit Logs**: `audit_trail.log` recording all registration events with timestamps.
