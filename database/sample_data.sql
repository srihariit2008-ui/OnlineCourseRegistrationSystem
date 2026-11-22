-- ==============================================================================
-- Scenario 17: Online Course Registration and Student Enrollment Analysis System
-- Database Seed Data: sample_data.sql
-- Coherent Academic Master Dataset
-- ==============================================================================

-- 1. Insert 10 Sample Students
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

-- 2. Insert 8 Sample Courses across 3 Academic Categories
-- Note: CRS104 max_capacity = 3 and 3 active enrollments below to test Course Full condition!
INSERT INTO courses (course_id, course_name, category, duration, max_capacity, description) VALUES
('CRS101', 'Full Stack Web Development',          'Technical Training',  '60 Hours', 5, 'Comprehensive front-end and back-end web development with modern standards.'),
('CRS102', 'Data Structures & Algorithms in Java', 'Technical Training',  '45 Hours', 5, 'Core data structures, algorithm design, complexity analysis, and coding.'),
('CRS103', 'Foundations of Data Science with Python', 'Certificate Course', '60 Hours', 6, 'Data preprocessing, Pandas wrangling, NumPy, and statistical visualizations.'),
('CRS104', 'Machine Learning & Deep Neural Nets', 'Technical Training',  '50 Hours', 3, 'Supervised learning, deep architectures, loss optimization, and evaluation.'),
('CRS105', 'Professional Communication & Soft Skills', 'Skill Development', '30 Hours', 8, 'Corporate etiquette, presentation techniques, leadership, and interview prep.'),
('CRS106', 'Agile Software Engineering & DevOps', 'Skill Development',   '35 Hours', 6, 'Scrum workflows, continuous integration, version control, and test automation.'),
('CRS107', 'Advanced Database Design & SQL Tuning', 'Certificate Course', '40 Hours', 4, 'Relational schema normalization, complex queries, transactions, and indexing.'),
('CRS108', 'Cyber Security & Network Defense',     'Certificate Course', '40 Hours', 4, 'Cryptographic protocols, penetration testing fundamentals, and security audit.');

-- 3. Insert Enrollments
-- Active counts per course:
-- CRS101: 4 active -> Capacity 5 (1 Available)
-- CRS102: 3 active -> Capacity 5 (2 Available)
-- CRS103: 5 active -> Capacity 6 (1 Available)
-- CRS104: 3 active -> Capacity 3 (0 Available - FULL!)
-- CRS105: 4 active -> Capacity 8 (4 Available)
-- CRS106: 2 active -> Capacity 6 (4 Available)
-- CRS107: 2 active -> Capacity 4 (2 Available)
-- CRS108: 1 active (STU103), 1 CANCELLED (STU107) -> Active count = 1 -> Capacity 4 (3 Available)
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
(25, 'STU107', 'CRS108', '2026-01-14', 'CANCELLED');

-- 4. Insert Course Completions
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
