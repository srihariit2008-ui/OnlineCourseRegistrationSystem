# SYSTEM DESIGN & ARCHITECTURE DOCUMENT
## Online Course Registration and Student Enrollment Analysis System
**Academic Scenario 17** | Curriculum Reference: 25ITT33 Software Engineering (Unit 3: Design)

---

## 1. ARCHITECTURAL DESIGN & PATTERNS

The application follows the classic **Layered Architecture Style** and **Data Access Object (DAO) Pattern**, separating user interface, domain logic, data persistence, and data science analysis into decoupled tiers.

```mermaid
graph TD
    subgraph Presentation Tier
        UI["Main Console Interface (Main.java)"]
    end

    subgraph Business Logic & Service Tier
        RS["ReportService.java"]
        AL["AsyncAuditLogger.java (Multithreaded Daemon)"]
        EX["Custom Exceptions (CourseFull, DuplicateReg)"]
    end

    subgraph Data Access Layer [DAO Pattern]
        SDAO["StudentDAO.java"]
        CDAO["CourseDAO.java"]
        EDAO["EnrollmentDAO.java (Transaction Manager)"]
        CMPDAO["CompletionDAO.java"]
    end

    subgraph Database Connectivity
        DBC["DatabaseConnection.java (JDBC Factory)"]
        PROP["db.properties Config File"]
    end

    subgraph Database Tier
        DB[("MySQL Database: online_course_registration")]
        V1["vw_course_enrollment_summary"]
        V2["vw_student_enrollment_details"]
        V3["vw_popular_courses"]
    end

    subgraph Analytics Tier
        PY["Python Data Science Engine (analysis.py)"]
        PD["Pandas & NumPy Data Wrangling"]
        PLT["Matplotlib & Seaborn Visualizations"]
    end

    UI --> RS
    UI --> SDAO
    UI --> CDAO
    UI --> EDAO
    UI --> CMPDAO
    UI --> AL
    
    RS --> SDAO
    RS --> CDAO
    RS --> EDAO
    RS --> CMPDAO

    SDAO --> DBC
    CDAO --> DBC
    EDAO --> DBC
    CMPDAO --> DBC
    DBC --> PROP
    DBC --> DB

    DB --> V1
    DB --> V2
    DB --> V3

    V2 --> PY
    PY --> PD
    PD --> PLT
```

---

## 2. DATA FLOW DIAGRAMS (DFD)

### 2.1 Level 0: Context Analysis Diagram
The Context Diagram defines the external entity interactions with the core system boundary.

```mermaid
flowchart LR
    StudentAdmin["Student / Academic Admin"]
    System["Online Course Registration & Enrollment Analysis System"]
    MySQL[("MySQL Database")]
    AnalyticsEngine["Python Data Science Engine"]

    StudentAdmin -- "Student / Course Details, Registration Requests" --> System
    System -- "Registration Confirmation, Seat Status, Reports" --> StudentAdmin
    System -- "Parameterized CRUD & Transaction Queries" --> MySQL
    MySQL -- "ResultSets & Relational Records" --> System
    MySQL -- "Live Relational Data / Views" --> AnalyticsEngine
    AnalyticsEngine -- "Statistical Reports & Visual Charts" --> StudentAdmin
```

### 2.2 Level 1: System Decomposition DFD
Breaks down the system into its 5 primary functional processes:

```mermaid
flowchart TD
    User["Institutional User"]
    
    P1["1.0 Manage Students"]
    P2["2.0 Manage Courses"]
    P3["3.0 Process Registration & Check Capacity"]
    P4["4.0 Manage Enrollments & Cancellations"]
    P5["5.0 Track Completions & Certifications"]
    P6["6.0 Perform Python Data Science Analysis"]

    DS1[("students table")]
    DS2[("courses table")]
    DS3[("enrollments table")]
    DS4[("completions table")]

    User -->|Student Info| P1
    P1 -->|Write/Update| DS1

    User -->|Course Specs| P2
    P2 -->|Write/Update| DS2

    User -->|Student ID + Course ID| P3
    P3 -->|Validate Student| DS1
    P3 -->|Check & Lock Capacity| DS2
    P3 -->|Record Registration| DS3

    User -->|Cancel / Status Update| P4
    P4 -->|Update Status| DS3

    User -->|Mark Complete| P5
    P5 -->|Update Status| DS3
    P5 -->|Insert Completion Record| DS4

    DS1 & DS2 & DS3 & DS4 -->|Extracted Records| P6
    P6 -->|Charts & CSV| User
```

---

## 3. ALGORITHMS AND WORKFLOWS

### 3.1 Course Registration Algorithm (Transaction Management)
This algorithm prevents over-enrollment and duplicate registrations using ACID transactions as covered in DBMS Unit 4.

```text
ALGORITHM: RegisterStudentForCourse(student_id, course_id)
INPUT: student_id (VARCHAR), course_id (VARCHAR)
OUTPUT: registration_id (INT) or Exception

1. Verify student exists in 'students' table.
   IF not found THEN THROW RecordNotFoundException("Student does not exist").

2. Verify course exists in 'courses' table.
   IF not found THEN THROW RecordNotFoundException("Course does not exist").

3. BEGIN TRANSACTION:
   Set connection.setAutoCommit(false).

4. Check duplicate registration:
   SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?;
   IF count > 0 THEN
      ROLLBACK TRANSACTION;
      THROW DuplicateRegistrationException("Student already registered for this course").

5. Lock and evaluate course capacity:
   SELECT max_capacity FROM courses WHERE course_id = ? FOR UPDATE;
   SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND enrollment_status != 'CANCELLED';
   
   IF active_count >= max_capacity THEN
      ROLLBACK TRANSACTION;
      THROW CourseFullException("Course is full. Available seats: 0").

6. Insert registration record:
   INSERT INTO enrollments (student_id, course_id, registration_date, enrollment_status)
   VALUES (student_id, course_id, CURRENT_DATE, 'REGISTERED');
   Retrieve generated registration_id.

7. COMMIT TRANSACTION:
   connection.commit();
   connection.setAutoCommit(true);

8. Log action asynchronously to audit trail.
9. RETURN registration_id.
```

### 3.2 Seat Availability Calculation Formula
$$\text{Available Seats} = \max\left(0, \text{max\_capacity} - \sum_{\text{status} \neq \text{'CANCELLED'}} 1\right)$$

$$\text{Occupancy Rate (\%)} = \left(\frac{\text{Active Enrollments}}{\text{max\_capacity}}\right) \times 100$$
