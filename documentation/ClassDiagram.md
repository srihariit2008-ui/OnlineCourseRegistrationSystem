# CLASS-BASED MODELING & CLASS DIAGRAM
## Online Course Registration and Student Enrollment Analysis System
**Academic Scenario 17** | Curriculum Reference: 25ITT32 Java Programming & 25ITT33 Software Engineering

---

## 1. UML CLASS DIAGRAM

```mermaid
classDiagram
    direction TB

    class Person {
        <<abstract>>
        #String id
        #String name
        #String email
        #String phone
        +Person()
        +Person(id, name, email, phone)
        +getRoleDescription()* String
        +getId() String
        +setId(String id) void
        +getName() String
        +setName(String name) void
        +getEmail() String
        +setEmail(String email) void
        +getPhone() String
        +setPhone(String phone) void
        +toString() String
    }

    class Student {
        -String department
        -int yearOfStudy
        +Student()
        +Student(id, name, email, phone, department, yearOfStudy)
        +getRoleDescription() String
        +getDepartment() String
        +setDepartment(String dept) void
        +getYearOfStudy() int
        +setYearOfStudy(int year) void
        +toString() String
    }

    class Course {
        -String courseId
        -String courseName
        -String category
        -String duration
        -int maxCapacity
        -String description
        -int activeEnrollments
        -int availableSeats
        +Course()
        +Course(id, name, cat, dur, cap, desc)
        +getCourseId() String
        +getCourseName() String
        +getCategory() String
        +getMaxCapacity() int
        +getActiveEnrollments() int
        +getAvailableSeats() int
        +getOccupancyRate() double
        +toString() String
    }

    class Enrollment {
        -int registrationId
        -String studentId
        -String courseId
        -Date registrationDate
        -String enrollmentStatus
        -String studentName
        -String courseName
        -String department
        +Enrollment()
        +Enrollment(regId, sId, cId, date, status)
        +getRegistrationId() int
        +getStudentId() String
        +getCourseId() String
        +getEnrollmentStatus() String
        +setEnrollmentStatus(String status) void
        +toString() String
    }

    class Completion {
        -int completionId
        -int registrationId
        -String completionStatus
        -Date completionDate
        -String studentId
        -String studentName
        -String courseId
        -String courseName
        +Completion()
        +Completion(compId, regId, status, date)
        +getCompletionId() int
        +getRegistrationId() int
        +getCompletionStatus() String
        +getCompletionDate() Date
        +toString() String
    }

    class DatabaseConnection {
        -String dbUrl$
        -String dbUser$
        -String dbPassword$
        -String dbDriver$
        +loadConfiguration()$ void
        +getConnection()$ Connection
        +isDatabaseAvailable()$ boolean
        +closeQuietly(AutoCloseable)$ void
    }

    class StudentDAO {
        -Map~String,Student~ memStore$
        +validateStudent(Student) void
        +addStudent(Student) boolean
        +getAllStudents() List~Student~
        +getStudentById(String) Student
        +updateStudent(Student) boolean
        +deleteStudent(String) boolean
        +searchStudentsByName(String) List~Student~
    }

    class CourseDAO {
        -Map~String,Course~ memStore$
        +validateCourse(Course) void
        +addCourse(Course) boolean
        +getAllCourses() List~Course~
        +getCourseById(String) Course
        +updateCourse(Course) boolean
        +deleteCourse(String) boolean
        +searchCourses(String) List~Course~
    }

    class EnrollmentDAO {
        -Map~Integer,Enrollment~ memStore$
        +registerStudentForCourse(sId, cId) int
        +getActiveEnrollmentsForCourse(cId) int
        +getAllEnrollments() List~Enrollment~
        +getEnrollmentById(int) Enrollment
        +getEnrollmentsByStudent(String) List~Enrollment~
        +getEnrollmentsByCourse(String) List~Enrollment~
        +updateEnrollmentStatus(int, String) boolean
        +cancelRegistration(int) boolean
    }

    class CompletionDAO {
        -Map~Integer,Completion~ memStore$
        +markCourseCompleted(int, Date) boolean
        +getAllCompletions() List~Completion~
        +getPendingCourses() List~Enrollment~
        +getCompletionsByStudent(String) List~Completion~
    }

    class ReportService {
        -StudentDAO studentDAO
        -CourseDAO courseDAO
        -EnrollmentDAO enrollmentDAO
        -CompletionDAO completionDAO
        +printCompleteStudentReport() void
        +printCompleteCourseReport() void
        +printCourseWiseEnrollmentReport() void
        +printStudentWiseRegistrationReport() void
        +printAvailableSeatReport() void
        +printCourseCompletionReport() void
        +printMostPopularCourses() void
        +printLowEnrollmentCourses() void
        +exportEnrollmentReportToCsv(String) void
    }

    class AsyncAuditLogger {
        -BlockingQueue~String~ logQueue$
        -boolean running$
        +log(String, String)$ void
        +run() void
        +stopLogger()$ void
    }

    Person <|-- Student : Inherits
    Enrollment "1" o-- "1" Student : Associated With
    Enrollment "1" o-- "1" Course : Enrolls Into
    Completion "1" o-- "1" Enrollment : Completes
    StudentDAO ..> DatabaseConnection : Uses
    CourseDAO ..> DatabaseConnection : Uses
    EnrollmentDAO ..> DatabaseConnection : Uses
    CompletionDAO ..> DatabaseConnection : Uses
    ReportService --> StudentDAO : Delegates
    ReportService --> CourseDAO : Delegates
    ReportService --> EnrollmentDAO : Delegates
    ReportService --> CompletionDAO : Delegates
```

---

## 2. SYLLABUS CONCEPT MAPPINGS

1. **Inheritance & Polymorphism (Java Unit 2)**:
   - `Person` defines common state and behaviour (`id`, `name`, `email`, `phone`).
   - `Student` extends `Person`, invoking `super(...)` and overriding `getRoleDescription()`. Dynamic method dispatch occurs when polymorphic references invoke overridden methods.
2. **Encapsulation & Constructors (Java Unit 1)**:
   - Private attributes with getter/setter accessors.
   - Parameterized and default constructors for entity initialization.
3. **Exception Handling Hierarchy (Java Unit 3)**:
   - `CustomExceptions.CourseFullException`
   - `CustomExceptions.DuplicateRegistrationException`
   - `CustomExceptions.ValidationException`
   - `CustomExceptions.RecordNotFoundException`
4. **Collections, Generics & Lambdas (Java Unit 5)**:
   - Collections: `List<Student>`, `List<Course>`, `Map<String, Course>`.
   - Lambdas and Streams: Filter, sort, and map operations: e.g. `list.stream().filter(e -> ...).sorted(...).collect(Collectors.toList())`.
5. **Multithreaded Background Processing (Java Unit 3)**:
   - `AsyncAuditLogger` implements `Runnable` and runs as a daemon thread executing concurrent log persistence without blocking main UI threads.
