-- ==============================================================================
-- Scenario 17: Online Course Registration and Student Enrollment Analysis System
-- SQL Analytical and Operational Queries: queries.sql
-- Curriculum Reference: 25ITT31 Database Management Systems (SQL & Views)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- QUERY 1: Student Master Catalog with Department Affiliations
-- ------------------------------------------------------------------------------
SELECT 
    student_id,
    student_name,
    email,
    phone,
    department,
    year_of_study
FROM students
ORDER BY department, year_of_study DESC, student_id;

-- ------------------------------------------------------------------------------
-- QUERY 2: Available Course Offerings & Maximum Capacities
-- ------------------------------------------------------------------------------
SELECT 
    course_id,
    course_name,
    category,
    duration,
    max_capacity
FROM courses
ORDER BY category, course_id;

-- ------------------------------------------------------------------------------
-- QUERY 3: Live Course Occupancy & Remaining Available Seat Count
-- Active registrations (status != 'CANCELLED') consume capacity.
-- ------------------------------------------------------------------------------
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS active_enrollments,
    (c.max_capacity - COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END)) AS available_seats,
    ROUND((COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 2) AS occupancy_pct
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
ORDER BY occupancy_pct DESC;

-- ------------------------------------------------------------------------------
-- QUERY 4: Saturated / Full Course Alert (Available Seats = 0)
-- ------------------------------------------------------------------------------
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(e.registration_id) AS enrolled_count,
    'COURSE FULL - REGISTRATION BLOCKED' AS alert_status
FROM courses c
JOIN enrollments e ON c.course_id = e.course_id
WHERE e.enrollment_status != 'CANCELLED'
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
HAVING COUNT(e.registration_id) >= c.max_capacity;

-- ------------------------------------------------------------------------------
-- QUERY 5: Most Popular Courses Ranked by Student Demand
-- ------------------------------------------------------------------------------
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS total_enrolled,
    ROUND((COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 2) AS utilization_rate
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
ORDER BY total_enrolled DESC, utilization_rate DESC;

-- ------------------------------------------------------------------------------
-- QUERY 6: Under-Enrolled Offerings (< 50% Capacity Alert)
-- ------------------------------------------------------------------------------
SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS enrolled_students,
    (c.max_capacity - COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END)) AS vacant_seats,
    ROUND((COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 2) AS occupancy_rate
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
HAVING enrolled_students < (c.max_capacity * 0.50) OR enrolled_students = 0
ORDER BY enrolled_students ASC;

-- ------------------------------------------------------------------------------
-- QUERY 7: Multi-Course Enrolled Students (Workload Distribution)
-- ------------------------------------------------------------------------------
SELECT 
    s.student_id,
    s.student_name,
    s.department,
    COUNT(e.registration_id) AS total_registered_courses,
    GROUP_CONCAT(e.course_id || ' (' || e.enrollment_status || ')', ', ') AS registered_course_list
FROM students s
JOIN enrollments e ON s.student_id = e.student_id
WHERE e.enrollment_status != 'CANCELLED'
GROUP BY s.student_id, s.student_name, s.department
HAVING COUNT(e.registration_id) >= 2
ORDER BY total_registered_courses DESC, s.student_id;

-- ------------------------------------------------------------------------------
-- QUERY 8: Department-wise Enrollment Distribution
-- ------------------------------------------------------------------------------
SELECT 
    s.department,
    COUNT(DISTINCT s.student_id) AS total_students,
    COUNT(e.registration_id) AS total_course_enrollments,
    ROUND(CAST(COUNT(e.registration_id) AS FLOAT) / COUNT(DISTINCT s.student_id), 2) AS avg_courses_per_student
FROM students s
LEFT JOIN enrollments e ON s.student_id = e.student_id AND e.enrollment_status != 'CANCELLED'
GROUP BY s.department
ORDER BY total_course_enrollments DESC;

-- ------------------------------------------------------------------------------
-- QUERY 9: Verified Course Completion & Certificate Records
-- ------------------------------------------------------------------------------
SELECT 
    comp.completion_id,
    e.registration_id,
    s.student_id,
    s.student_name,
    c.course_id,
    c.course_name,
    comp.completion_date,
    comp.completion_status
FROM completions comp
JOIN enrollments e ON comp.registration_id = e.registration_id
JOIN students s ON e.student_id = s.student_id
JOIN courses c ON e.course_id = c.course_id
ORDER BY comp.completion_date DESC, comp.completion_id;

-- ------------------------------------------------------------------------------
-- QUERY 10: Cancellation Impact & Seat Restoration Audit
-- Demonstrates that cancelled registrations restore seat availability.
-- ------------------------------------------------------------------------------
SELECT 
    e.registration_id,
    e.student_id,
    s.student_name,
    e.course_id,
    c.course_name,
    e.enrollment_status,
    'SEAT RESTORED TO INVENTORY' AS capacity_impact
FROM enrollments e
JOIN students s ON e.student_id = s.student_id
JOIN courses c ON e.course_id = c.course_id
WHERE e.enrollment_status = 'CANCELLED';
