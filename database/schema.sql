-- ==============================================================================
-- Scenario 17: Online Course Registration and Student Enrollment Analysis System
-- Database Schema Definition: schema.sql
-- Compatible with MySQL 8.0+ and SQLite 3+
-- Normalization: 1NF, 2NF, 3NF, BCNF Compliant
-- ==============================================================================

DROP VIEW IF EXISTS vw_low_enrollment_courses;
DROP VIEW IF EXISTS vw_popular_courses;
DROP VIEW IF EXISTS vw_course_enrollment_summary;
DROP VIEW IF EXISTS vw_student_enrollment_details;

DROP TABLE IF EXISTS completions;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;

-- ------------------------------------------------------------------------------
-- 1. TABLE: students
-- Primary Key: student_id
-- Unique Key: email
-- ------------------------------------------------------------------------------
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    year_of_study INTEGER NOT NULL CHECK (year_of_study BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 2. TABLE: courses
-- Primary Key: course_id
-- Check: category IN ('Certificate Course', 'Skill Development', 'Technical Training')
-- Check: max_capacity > 0
-- ------------------------------------------------------------------------------
CREATE TABLE courses (
    course_id VARCHAR(20) PRIMARY KEY NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('Certificate Course', 'Skill Development', 'Technical Training')),
    duration VARCHAR(50) NOT NULL,
    max_capacity INTEGER NOT NULL CHECK (max_capacity > 0),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 3. TABLE: enrollments
-- Primary Key: registration_id
-- Composite Unique: (student_id, course_id) prevents duplicate registrations
-- Status Check: REGISTERED, IN_PROGRESS, COMPLETED, CANCELLED
-- ------------------------------------------------------------------------------
CREATE TABLE enrollments (
    registration_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    course_id VARCHAR(20) NOT NULL,
    registration_date DATE NOT NULL,
    enrollment_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' CHECK (enrollment_status IN ('REGISTERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (student_id, course_id)
);

-- ------------------------------------------------------------------------------
-- 4. TABLE: completions
-- Primary Key: completion_id
-- Foreign Key & Unique: registration_id (1:1 Relationship)
-- ------------------------------------------------------------------------------
CREATE TABLE completions (
    completion_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    registration_id INTEGER UNIQUE NOT NULL,
    completion_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (completion_status IN ('COMPLETED', 'PENDING', 'WITHDRAWN')),
    completion_date DATE NOT NULL,
    FOREIGN KEY (registration_id) REFERENCES enrollments(registration_id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ==============================================================================
-- DATABASE VIEWS
-- ==============================================================================

-- View 1: Real-time Course Enrollment and Seat Vacancy Summary
CREATE VIEW vw_course_enrollment_summary AS
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.duration,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS active_enrollments,
    (c.max_capacity - COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END)) AS available_seats,
    ROUND(
        (COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 
        2
    ) AS occupancy_percentage
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.duration, c.max_capacity;

-- View 2: Student Enrollment and Completion Details
CREATE VIEW vw_student_enrollment_details AS
SELECT 
    e.registration_id,
    s.student_id,
    s.student_name,
    s.email,
    s.department,
    s.year_of_study,
    c.course_id,
    c.course_name,
    c.category,
    c.duration,
    e.registration_date,
    e.enrollment_status,
    comp.completion_id,
    comp.completion_status,
    comp.completion_date
FROM enrollments e
JOIN students s ON e.student_id = s.student_id
JOIN courses c ON e.course_id = c.course_id
LEFT JOIN completions comp ON e.registration_id = comp.registration_id;

-- View 3: Most Popular Courses Ranked by Enrollment
CREATE VIEW vw_popular_courses AS
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS total_enrolled,
    ROUND(
        (COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 
        2
    ) AS utilization_rate
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
ORDER BY total_enrolled DESC;
