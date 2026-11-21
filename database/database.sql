-- ==============================================================================
-- ONLINE COURSE REGISTRATION AND STUDENT ENROLLMENT ANALYSIS SYSTEM
-- Scenario 17 (Register No.: 17, 47, 77, 107)
-- Database Script: database.sql
-- Compatible with MySQL 8.0+
-- Strict adherence to DBMS concepts: Normalization (1NF, 2NF, 3NF, BCNF),
-- Referential Integrity, Constraints (PK, FK, UNIQUE, NOT NULL, CHECK),
-- Views, Transactions, and Reporting Queries.
-- ==============================================================================

DROP DATABASE IF EXISTS online_course_registration;
CREATE DATABASE online_course_registration
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE online_course_registration;

-- ------------------------------------------------------------------------------
-- 1. TABLE: students
-- Stores basic profile and academic affiliation of students.
-- Primary Key: student_id
-- Unique Key: email
-- ------------------------------------------------------------------------------
CREATE TABLE students (
    student_id VARCHAR(20) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    year_of_study INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_students PRIMARY KEY (student_id),
    CONSTRAINT uk_student_email UNIQUE (email),
    CONSTRAINT chk_student_year CHECK (year_of_study BETWEEN 1 AND 5)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 2. TABLE: courses
-- Stores course offerings, category, duration, and maximum enrollment capacity.
-- Primary Key: course_id
-- Check: category in ('Certificate Course', 'Skill Development', 'Technical Training')
-- Check: max_capacity > 0
-- ------------------------------------------------------------------------------
CREATE TABLE courses (
    course_id VARCHAR(20) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    max_capacity INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_courses PRIMARY KEY (course_id),
    CONSTRAINT chk_course_category CHECK (category IN ('Certificate Course', 'Skill Development', 'Technical Training')),
    CONSTRAINT chk_course_capacity CHECK (max_capacity > 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 3. TABLE: enrollments
-- Maps student registration into courses.
-- Primary Key: registration_id (Auto Increment)
-- Foreign Keys: student_id -> students(student_id), course_id -> courses(course_id)
-- Composite Unique Key: (student_id, course_id) prevents duplicate registration.
-- ------------------------------------------------------------------------------
CREATE TABLE enrollments (
    registration_id INT AUTO_INCREMENT NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    course_id VARCHAR(20) NOT NULL,
    registration_date DATE NOT NULL,
    enrollment_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    CONSTRAINT pk_enrollments PRIMARY KEY (registration_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT uk_student_course UNIQUE (student_id, course_id),
    CONSTRAINT chk_enrollment_status CHECK (enrollment_status IN ('REGISTERED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 4. TABLE: completions
-- Tracks official course completion and certificates.
-- Primary Key: completion_id (Auto Increment)
-- Foreign Key: registration_id -> enrollments(registration_id)
-- Unique Key: registration_id (One completion record per enrollment)
-- ------------------------------------------------------------------------------
CREATE TABLE completions (
    completion_id INT AUTO_INCREMENT NOT NULL,
    registration_id INT NOT NULL,
    completion_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    completion_date DATE NOT NULL,
    CONSTRAINT pk_completions PRIMARY KEY (completion_id),
    CONSTRAINT fk_completions_enrollment FOREIGN KEY (registration_id)
        REFERENCES enrollments(registration_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT uk_completion_registration UNIQUE (registration_id),
    CONSTRAINT chk_completion_status CHECK (completion_status IN ('COMPLETED', 'PENDING', 'WITHDRAWN'))
) ENGINE=InnoDB;

-- ==============================================================================
-- DATABASE VIEWS
-- ==============================================================================

-- 1. View: Course-wise Enrollment and Seat Availability Summary
-- Active registrations (status != 'CANCELLED') consume seats.
CREATE OR REPLACE VIEW vw_course_enrollment_summary AS
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

-- 2. View: Comprehensive Student Enrollment and Completion Details
CREATE OR REPLACE VIEW vw_student_enrollment_details AS
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

-- 3. View: Most Popular Courses (Ranked by active enrollments)
CREATE OR REPLACE VIEW vw_popular_courses AS
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
ORDER BY total_enrolled DESC, utilization_rate DESC;

-- 4. View: Low Enrollment Courses (Active enrollment < 50% capacity)
CREATE OR REPLACE VIEW vw_low_enrollment_courses AS
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS total_enrolled,
    (c.max_capacity - COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END)) AS available_seats,
    ROUND(
        (COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 
        2
    ) AS utilization_rate
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
HAVING total_enrolled < (c.max_capacity * 0.50) OR total_enrolled = 0
ORDER BY total_enrolled ASC;

-- ==============================================================================
-- SAMPLE DATA INSERTION
-- Realistic, coherent academic data representing a college institution.
-- ==============================================================================

-- Insert 10 Sample Students
INSERT INTO students (student_id, student_name, email, phone, department, year_of_study) VALUES
('STU101', 'Aarav Sharma',     'aarav.sharma@institution.edu',   '9876543210', 'Information Technology', 3),
('STU102', 'Bhavna Patel',     'bhavna.patel@institution.edu',   '9876543211', 'Computer Science',        3),
('STU103', 'Chirag Sundaram',  'chirag.s@institution.edu',       '9876543212', 'Electronics & Comm',      2),
('STU104', 'Divya Nair',       'divya.nair@institution.edu',     '9876543213', 'Information Technology', 4),
('STU105', 'Eshwar Reddy',     'eshwar.reddy@institution.edu',   '9876543214', 'Artificial Intelligence', 2),
('STU106', 'Farhan Ali',       'farhan.ali@institution.edu',     '9876543215', 'Computer Science',        4),
('STU107', 'Gayathri Menon',   'gayathri.m@institution.edu',     '9876543216', 'Mechanical Engg',        1),
('STU108', 'Hari Krishnan',    'hari.k@institution.edu',         '9876543217', 'Information Technology', 2),
('STU109', 'Indira Priyadarsh', 'indira.p@institution.edu',      '9876543218', 'Artificial Intelligence', 3),
('STU110', 'Jitendra Varma',   'jiten.varma@institution.edu',    '9876543219', 'Computer Science',        1);

-- Insert 8 Sample Courses across categories
-- Note: CRS104 has max_capacity = 3 and 3 active enrollments below to test Course Full condition!
INSERT INTO courses (course_id, course_name, category, duration, max_capacity, description) VALUES
('CRS101', 'Full Stack Web Development',          'Technical Training',  '60 Hours', 5, 'Comprehensive front-end and back-end web development with modern standards.'),
('CRS102', 'Data Structures & Algorithms in Java', 'Technical Training',  '45 Hours', 5, 'Core data structures, algorithm design, complexity analysis, and coding.'),
('CRS103', 'Foundations of Data Science with Python', 'Certificate Course', '60 Hours', 6, 'Data preprocessing, Pandas wrangling, NumPy, and statistical visualizations.'),
('CRS104', 'Machine Learning & Deep Neural Nets', 'Technical Training',  '50 Hours', 3, 'Supervised learning, deep architectures, loss optimization, and evaluation.'),
('CRS105', 'Professional Communication & Soft Skills', 'Skill Development', '30 Hours', 8, 'Corporate etiquette, presentation techniques, leadership, and interview prep.'),
('CRS106', 'Agile Software Engineering & DevOps', 'Skill Development',   '35 Hours', 6, 'Scrum workflows, continuous integration, version control, and test automation.'),
('CRS107', 'Advanced Database Design & SQL Tuning', 'Certificate Course', '40 Hours', 4, 'Relational schema normalization, complex queries, transactions, and indexing.'),
('CRS108', 'Cyber Security & Network Defense',     'Certificate Course', '40 Hours', 4, 'Cryptographic protocols, penetration testing fundamentals, and security audit.');

-- Insert Enrollments
-- Active counts per course:
-- CRS101: 4 active (STU101, STU102, STU104, STU108) -> Capacity 5 (1 Available)
-- CRS102: 3 active (STU102, STU105, STU106)         -> Capacity 5 (2 Available)
-- CRS103: 5 active (STU101, STU103, STU105, STU108, STU109) -> Capacity 6 (1 Available)
-- CRS104: 3 active (STU104, STU106, STU109)         -> Capacity 3 (0 Available - FULL!)
-- CRS105: 4 active (STU101, STU103, STU107, STU110) -> Capacity 8 (4 Available)
-- CRS106: 2 active (STU102, STU106)                 -> Capacity 6 (4 Available)
-- CRS107: 2 active (STU104, STU108)                 -> Capacity 4 (2 Available)
-- CRS108: 1 active (STU103), 1 CANCELLED (STU107)   -> Active count = 1 -> Capacity 4 (3 Available)
INSERT INTO enrollments (registration_id, student_id, course_id, registration_date, enrollment_status) VALUES
(1,  'STU101', 'CRS101', '2026-01-10', 'COMPLETED'),
(2,  'STU102', 'CRS101', '2026-01-11', 'COMPLETED'),
(3,  'STU104', 'CRS101', '2026-01-12', 'IN_PROGRESS'),
(4,  'STU108', 'CRS101', '2026-01-15', 'REGISTERED'),

(5,  'STU102', 'CRS102', '2026-01-10', 'COMPLETED'),
(6,  'STU105', 'CRS102', '2026-01-14', 'IN_PROGRESS'),
(7,  'STU106', 'CRS102', '2026-01-16', 'REGISTERED'),

(8,  'STU101', 'CRS103', '2026-01-08', 'COMPLETED'),
(9,  'STU103', 'CRS103', '2026-01-12', 'COMPLETED'),
(10, 'STU105', 'CRS103', '2026-01-14', 'IN_PROGRESS'),
(11, 'STU108', 'CRS103', '2026-01-15', 'REGISTERED'),
(12, 'STU109', 'CRS103', '2026-01-16', 'REGISTERED'),

(13, 'STU104', 'CRS104', '2026-01-11', 'COMPLETED'),
(14, 'STU106', 'CRS104', '2026-01-13', 'IN_PROGRESS'),
(15, 'STU109', 'CRS104', '2026-01-15', 'REGISTERED'),

(16, 'STU101', 'CRS105', '2026-01-09', 'COMPLETED'),
(17, 'STU103', 'CRS105', '2026-01-12', 'COMPLETED'),
(18, 'STU107', 'CRS105', '2026-01-14', 'IN_PROGRESS'),
(19, 'STU110', 'CRS105', '2026-01-16', 'REGISTERED'),

(20, 'STU102', 'CRS106', '2026-01-10', 'COMPLETED'),
(21, 'STU106', 'CRS106', '2026-01-14', 'IN_PROGRESS'),

(22, 'STU104', 'CRS107', '2026-01-12', 'COMPLETED'),
(23, 'STU108', 'CRS107', '2026-01-15', 'IN_PROGRESS'),

(24, 'STU103', 'CRS108', '2026-01-13', 'REGISTERED'),
(25, 'STU107', 'CRS108', '2026-01-14', 'CANCELLED'); -- Cancelled enrollment does not consume seat!

-- Insert Course Completions (Only for active enrollments marked as COMPLETED)
INSERT INTO completions (completion_id, registration_id, completion_status, completion_date) VALUES
(1, 1,  'COMPLETED', '2026-02-28'),
(2, 2,  'COMPLETED', '2026-02-28'),
(3, 5,  'COMPLETED', '2026-02-25'),
(4, 8,  'COMPLETED', '2026-03-05'),
(5, 9,  'COMPLETED', '2026-03-05'),
(6, 13, 'COMPLETED', '2026-03-01'),
(7, 16, 'COMPLETED', '2026-02-20'),
(8, 17, 'COMPLETED', '2026-02-20'),
(9, 20, 'COMPLETED', '2026-02-22'),
(10, 22, 'COMPLETED', '2026-03-02');

-- ==============================================================================
-- END OF DATABASE SCRIPT
-- ==============================================================================
