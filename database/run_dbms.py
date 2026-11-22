#!/usr/bin/env python3
"""
===============================================================================
Scenario 17: Online Course Registration and Student Enrollment Analysis System
Student Register Numbers: 17, 47, 77, 107 | Regulations: VCET B.Tech IT R2025
Component: DBMS Module Local Terminal Query Runner
===============================================================================
Executes SQL queries directly on course_registration.db and prints formatted terminal outputs.
Zero external dependencies required (uses built-in sqlite3).
"""

import os
import sys
import sqlite3

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(SCRIPT_DIR, "course_registration.db")
SCHEMA_PATH = os.path.join(SCRIPT_DIR, "schema.sql")
SAMPLE_DATA_PATH = os.path.join(SCRIPT_DIR, "sample_data.sql")

def ensure_database():
    """Initializes the database if not present."""
    if not os.path.exists(DB_PATH):
        print(f"[*] Initializing SQLite Database at: {DB_PATH}")
        conn = sqlite3.connect(DB_PATH)
        cur = conn.cursor()
        with open(SCHEMA_PATH, "r", encoding="utf-8") as f:
            cur.executescript(f.read())
        with open(SAMPLE_DATA_PATH, "r", encoding="utf-8") as f:
            cur.executescript(f.read())
        conn.commit()
        conn.close()
        print("[✓] Database initialized successfully with schema and sample data.")

def print_table(cursor, max_rows=50):
    """Prints cursor result set as a clean formatted ASCII table."""
    headers = [desc[0] for desc in cursor.description]
    rows = cursor.fetchall()
    
    if not rows:
        print("  (0 rows returned)")
        return

    col_widths = [len(h) for h in headers]
    for row in rows[:max_rows]:
        for idx, val in enumerate(row):
            sval = str(val) if val is not None else "NULL"
            if len(sval) > col_widths[idx]:
                col_widths[idx] = len(sval)

    sep = "+-" + "-+-".join("-" * w for w in col_widths) + "-+"
    header_str = "| " + " | ".join(h.ljust(col_widths[i]) for i, h in enumerate(headers)) + " |"

    print(sep)
    print(header_str)
    print(sep)

    for row in rows[:max_rows]:
        row_str = []
        for i, val in enumerate(row):
            sval = str(val) if val is not None else "NULL"
            if isinstance(val, (int, float)):
                row_str.append(sval.rjust(col_widths[i]))
            else:
                row_str.append(sval.ljust(col_widths[i]))
        print("| " + " | ".join(row_str) + " |")

    print(sep)
    print(f" Total Rows: {len(rows)}" + (f" (Showing first {max_rows})" if len(rows) > max_rows else ""))

def run_query(conn, title, sql, params=()):
    print("\n" + "="*90)
    print(f"  📌 {title}")
    print("="*90)
    print(" SQL Query Executed:")
    for line in sql.strip().splitlines():
        print(f"   {line}")
    print("-" * 90)
    
    cur = conn.cursor()
    try:
        cur.execute(sql, params)
        print_table(cur)
    except Exception as e:
        print(f"[X] Query execution error: {e}")
    print("="*90)

QUERIES = [
    ("QUERY 1: Student Master Catalog (FR-1)",
     "SELECT student_id, student_name, email, phone, department, year_of_study FROM students ORDER BY department, year_of_study DESC, student_id;"),

    ("QUERY 2: Available Course Offerings Catalog (FR-2)",
     "SELECT course_id, course_name, category, duration, max_capacity FROM courses ORDER BY category, course_id;"),

    ("QUERY 3: Live Course Occupancy & Remaining Seats (vw_course_enrollment_summary)",
     """SELECT 
    c.course_id,
    c.course_name,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS active_enrolled,
    (c.max_capacity - COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END)) AS available_seats,
    ROUND((COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 2) AS occupancy_pct
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.max_capacity
ORDER BY occupancy_pct DESC;"""),

    ("QUERY 4: Saturated / Full Course Alert (0 Available Seats)",
     """SELECT 
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
HAVING COUNT(e.registration_id) >= c.max_capacity;"""),

    ("QUERY 5: Most Popular Courses Ranked by Student Demand (vw_popular_courses)",
     """SELECT 
    c.course_id,
    c.course_name,
    c.category,
    c.max_capacity,
    COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) AS total_enrolled,
    ROUND((COUNT(CASE WHEN e.enrollment_status != 'CANCELLED' THEN e.registration_id END) * 100.0) / c.max_capacity, 2) AS utilization_rate
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name, c.category, c.max_capacity
ORDER BY total_enrolled DESC;"""),

    ("QUERY 6: Under-Enrolled Offerings (< 50% Capacity Alert)",
     """SELECT 
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
ORDER BY enrolled_students ASC;"""),

    ("QUERY 7: Multi-Course Enrolled Students (Workload Distribution)",
     """SELECT 
    s.student_id,
    s.student_name,
    s.department,
    COUNT(e.registration_id) AS total_courses,
    GROUP_CONCAT(e.course_id || ' (' || e.enrollment_status || ')', ', ') AS course_list
FROM students s
JOIN enrollments e ON s.student_id = e.student_id
WHERE e.enrollment_status != 'CANCELLED'
GROUP BY s.student_id, s.student_name, s.department
HAVING COUNT(e.registration_id) >= 2
ORDER BY total_courses DESC;"""),

    ("QUERY 8: Department-wise Enrollment Breakdown",
     """SELECT 
    s.department,
    COUNT(DISTINCT s.student_id) AS total_students,
    COUNT(e.registration_id) AS total_course_enrollments,
    ROUND(CAST(COUNT(e.registration_id) AS FLOAT) / COUNT(DISTINCT s.student_id), 2) AS avg_courses_per_student
FROM students s
LEFT JOIN enrollments e ON s.student_id = e.student_id AND e.enrollment_status != 'CANCELLED'
GROUP BY s.department
ORDER BY total_course_enrollments DESC;"""),

    ("QUERY 9: Verified Course Completion & Certificate Records",
     """SELECT 
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
ORDER BY comp.completion_date DESC;"""),

    ("QUERY 10: Cancellation Impact & Immediate Seat Restoration Audit",
     """SELECT 
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
WHERE e.enrollment_status = 'CANCELLED';""")
]

def main():
    ensure_database()
    conn = sqlite3.connect(DB_PATH)

    # Check for direct execution flag
    if len(sys.argv) > 1 and sys.argv[1] == "--all":
        print("\n" + "="*90)
        print("  ONLINE COURSE REGISTRATION - COMPLETE DBMS EVALUATION & QUERY RUNNER")
        print("  Database: course_registration.db | Engine: SQLite3 (Zero Setup)")
        print("="*90)
        for title, sql in QUERIES:
            run_query(conn, title, sql)
        conn.close()
        return

    while True:
        print("\n" + "="*90)
        print("  🎓 DBMS MODULE: COURSE REGISTRATION SQL QUERY RUNNER & EVALUATION TERMINAL")
        print("         Scenario 17 | Register Numbers: 17, 47, 77, 107 | VCET IT R2025")
        print("==========================================================================================")
        print(" 1. Query 1: View Student Master Catalog                            (FR-1)")
        print(" 2. Query 2: View Course Offerings Catalog & Capacities             (FR-2)")
        print(" 3. Query 3: Live Course Occupancy & Seat Availability Tracking     (FR-2, FR-6)")
        print(" 4. Query 4: Saturated / Full Course Alert (0 Available Seats)      (FR-3)")
        print(" 5. Query 5: Most Popular Courses Ranked by Demand                  (FR-6)")
        print(" 6. Query 6: Under-Enrolled Offerings (< 50% Capacity Alert)        (FR-6)")
        print(" 7. Query 7: Multi-Course Enrolled Students (Workload Matrix)       (FR-6)")
        print(" 8. Query 8: Department-wise Enrollment Breakdown                   (FR-6)")
        print(" 9. Query 9: Verified Course Completion & Certificate Records       (FR-5)")
        print(" 10. Query 10: Cancellation Impact & Seat Restoration Audit         (FR-4)")
        print(" 11. Run All Queries Sequentially (Complete Professor Evaluation Mode)")
        print(" 0. Exit DBMS Runner")
        print("------------------------------------------------------------------------------------------")
        choice = input(" Enter your choice (0-11): ").strip()

        if choice == "0":
            print("\nExiting DBMS Query Runner. Goodbye!")
            conn.close()
            break
        elif choice == "11":
            for title, sql in QUERIES:
                run_query(conn, title, sql)
        elif choice.isdigit() and 1 <= int(choice) <= 10:
            idx = int(choice) - 1
            title, sql = QUERIES[idx]
            run_query(conn, title, sql)
        else:
            print("[X] Invalid option. Please enter a number between 0 and 11.")

if __name__ == "__main__":
    main()
