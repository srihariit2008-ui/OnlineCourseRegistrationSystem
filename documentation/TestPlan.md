# SOFTWARE TEST PLAN
## Online Course Registration and Student Enrollment Analysis System
**Academic Scenario 17** | Curriculum Reference: 25ITT33 Software Engineering (Unit 4: Testing)

---

## 1. TEST PLAN OVERVIEW

### 1.1 Objectives
The primary objective of the testing phase is to verify and validate that all functional and non-functional requirements are satisfied, ensure data integrity across transactions, and confirm that boundary conditions (such as full course capacity and duplicate enrollments) are handled gracefully without software failures.

### 1.2 Scope of Testing
- **Unit Testing**: Testing individual entity methods, DAO validations, and calculations.
- **Integration Testing**: Testing multi-table interactions, JDBC connections, and foreign key cascades.
- **System Testing**: Validating end-to-end user workflows from console menu invocation to report generation.
- **Data Science Validation**: Verifying that Python analysis queries real database records and accurately computes statistical metrics.

---

## 2. TESTING STRATEGY & METHODOLOGIES

### 2.1 Black-Box Testing Techniques
- **Equivalence Partitioning (EP)**:
  - Year of study divided into valid partitions $[1, 5]$ and invalid partitions ($<1$ or $>5$).
  - Email addresses partitioned into valid RFC format and invalid non-standard formats.
  - Course capacity partitioned into valid integers ($>0$) and invalid inputs ($\le 0$).
- **Boundary Value Analysis (BVA)**:
  - Testing capacity saturation at exact capacity limit ($N$) and one above limit ($N+1$).
  - Course enrollment count transitions when registrations are added, completed, or cancelled.

### 2.2 White-Box Testing Techniques
- **Basis Path Testing & Cyclomatic Complexity**:
  - Evaluation of independent decision paths in `registerStudentForCourse()`:
    1. Path 1: Non-existing student $\rightarrow$ Throw `RecordNotFoundException`.
    2. Path 2: Non-existing course $\rightarrow$ Throw `RecordNotFoundException`.
    3. Path 3: Duplicate student-course record $\rightarrow$ Throw `DuplicateRegistrationException`.
    4. Path 4: Active enrollments $\ge$ capacity $\rightarrow$ Throw `CourseFullException`.
    5. Path 5: Valid registration $\rightarrow$ Insert record and commit.
  - Calculated Cyclomatic Complexity $V(G) = E - N + 2P = 5$, indicating high testability and clean maintainability.

---

## 3. VERIFICATION AND VALIDATION (V&V)

- **Verification**: "Are we building the product right?"
  - Code reviews adhering to OOP encapsulation, naming conventions, and resource closing.
  - SQL schema validation against 3NF/BCNF normalization rules.
- **Validation**: "Are we building the right product?"
  - Functional testing verifying all college academic requirements from Scenario 17.
  - End-to-end execution of automated test cases in `SystemTester.java`.

---

## 4. SOFTWARE QUALITY METRICS

1. **Test Pass Rate**: $100\%$ ($17/17$ executed test cases passed).
2. **Defect Density**: $0$ known open defects in released build.
3. **Coupling and Cohesion**:
   - High Cohesion: Each DAO owns operations strictly for one entity.
   - Low Coupling: Interaction mediated through well-defined interfaces and configuration files.
