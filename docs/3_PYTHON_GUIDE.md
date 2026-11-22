# Part 3: Python Analytics & Visualization Presentation Guide
**Scenario 17: Online Course Registration and Student Enrollment Analysis System**  
**Register Numbers:** 17, 47, 77, 107 | **Curriculum Regulations:** VCET B.Tech IT R2025  
**Course Code & Title:** 25ITC31 – Foundations of Data Science using Python  
**Python Environment:** Python 3.10+ (Standard Library, Pandas, NumPy, Matplotlib, Seaborn)

---

## 1. Objective of the Python Module

To fulfill **Curriculum Unit 5 (Exploratory Data Analysis & Visualization)** and project requirements:
1. **Dual Ingestion Engine**: Connects directly to the MySQL database via `mysql-connector-python` or falls back seamlessly to the generated snapshot `reports/enrollment_report.csv`.
2. **Data Wrangling & Integrity Verification**: Cleans raw dataset records, parses ISO dates, handles missing completion timestamps, and verifies categorical values.
3. **5-Stage Exploratory Statistical Analysis**:
   - Course-wise enrollment breakdown & occupancy percentages.
   - Popular & under-enrolled course identification.
   - Student cross-enrollment pattern analysis.
   - Institutional seat capacity utilization.
   - Course completion rates and progress tracking.
4. **Publication-Grade Data Visualizations**: Generates vector SVG graphics across 6 analytical perspectives for institutional stakeholders.

---

## 2. Analytics Pipeline Architecture

```
[MySQL Database / enrollment_report.csv]
                   │
                   ▼
[Stage 0: Ingestion & Verification] ─────► database_connection.py / CSV Parser
                   │
                   ▼
[Stage 1: Course-wise Enrollment] ───────► Aggregates active seats vs capacity
                   │
                   ▼
[Stage 2: Popularity Ranking] ───────────► Finds max/min demanded offerings
                   │
                   ▼
[Stage 3: Multi-Course Patterns] ────────► Tracks student workload distributions
                   │
                   ▼
[Stage 4: Institutional Capacity] ───────► Total capacity vs vacancy audit
                   │
                   ▼
[Stage 5: Completion Rates] ─────────────► Completed vs in-progress proportions
                   │
                   ▼
[Visualizations & Artifacts] ────────────► 6 SVG Charts in reports/charts/
                                          Summary report in reports/analysis_summary.txt
```

---

## 3. Key Code Highlights to Show the Professor

Located in [`python/analysis.py`](../python/analysis.py):

### 1. Dual Ingestion Mechanism (Database with CSV Fallback)
```python
def load_enrollment_data():
    # Attempt live MySQL database connection
    conn = get_db_connection()
    if conn:
        try:
            query = "SELECT * FROM vw_course_enrollment_summary"
            # Ingest live SQL records...
        finally:
            conn.close()
    
    # Graceful fallback to verified exported CSV snapshot
    print("[INFO] Loading dataset from CSV export fallback...")
    return parse_csv(CSV_FILE_PATH)
```

### 2. Vectorized Occupancy & Capacity Analysis
```python
# Compute course-level occupancy metrics
for course_id, stats in courses.items():
    enrolled = stats["enrolled_count"]
    capacity = stats["max_capacity"]
    available = max(0, capacity - enrolled)
    occupancy_pct = (enrolled / capacity) * 100.0 if capacity > 0 else 0.0
    
    stats["available_seats"] = available
    stats["occupancy_pct"] = round(occupancy_pct, 1)
```

### 3. Student Enrollment Cross-Tabulation Matrix
```python
# Build 2D frequency matrix mapping Students to Registered Courses
matrix = {}
for student in unique_students:
    matrix[student] = {c: 0 for c in unique_courses}

for record in active_records:
    s_name = record["student_name"]
    c_code = record["course_id"]
    matrix[s_name][c_code] = 1
```

### 4. Vector SVG Chart Generation
```python
# Generates high-fidelity standalone SVG visual chart without display server
def generate_bar_chart(data, filename, title, xlabel, ylabel):
    # Pure vector math rendering custom SVG bars, ticks, legends, and styling
    # Completely platform independent
```

---

## 4. Visualizations Generated

All visual assets are stored in the [`reports/charts/`](../reports/charts) directory:

| Chart Filename | Visualization Type | Academic & Institutional Insight Provided |
| :--- | :--- | :--- |
| **`course_wise_enrollment.svg`** | Clustered Bar Chart | Compares actual active enrollments against maximum quota caps for all 8 courses. |
| **`category_distribution.svg`** | Proportional Pie Chart | Illustrates institutional program balance: Technical Training, Skill Development, and Certificate Courses. |
| **`top_5_popular_courses.svg`** | Ranked Bar Chart | Identifies the top 5 most demanded courses (led by Foundations of Data Science, CRS103). |
| **`available_seats_per_course.svg`** | Horizontal Vacancy Bar Chart | Visualizes vacancy margins and alerts administrators to saturated offerings (e.g., CRS104 at 100% capacity). |
| **`completed_vs_pending.svg`** | Status Donut / Pie Chart | Displays overall institutional completion rate (45.83% completed vs. 54.17% in-progress/pending). |
| **`student_course_heatmap.svg`** | Cross-Tabulation Matrix Heatmap | Visualizes multi-course registration density and identifies cross-department interdisciplinary students. |

---

## 5. Live Execution for Professor

### Command Line Execution
```bash
cd python
# Install dependencies (if not already installed)
pip install -r requirements.txt

# Run the 5-Stage Data Science Analytics Engine
python analysis.py
```

### Generated Terminal Output Preview
```text
===========================================================================
 ONLINE COURSE REGISTRATION & STUDENT ENROLLMENT ANALYSIS REPORT
 Data Source: Python Standard CSV Parser
===========================================================================

[ANALYSIS 1: COURSE ENROLLMENT ANALYSIS]
 - Total Unique Courses with Enrollments : 8
 - Total Unique Registered Students     : 10
 - Total Active Course Registrations    : 24

 Course-wise Enrollment Breakdown:
   * CRS103 (Foundations of Data Science ): Enrolled:  5/ 6 | Avail:  1 | Occupancy:  83.3%
   * CRS101 (Full Stack Web Development  ): Enrolled:  4/ 5 | Avail:  1 | Occupancy:  80.0%
   * CRS104 (Machine Learning & Deep Neur): Enrolled:  3/ 3 | Avail:  0 | Occupancy: 100.0%
   * CRS108 (Cyber Security & Network Def): Enrolled:  1/ 4 | Avail:  3 | Occupancy:  25.0%

[ANALYSIS 4: COURSE CAPACITY ANALYSIS]
 - Institutional Total Capacity : 41 seats
 - Institutional Total Enrolled : 24 seats
 - Institutional Total Available: 17 seats
 - Overall Completion Rate     : 45.83%
===========================================================================
```


