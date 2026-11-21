package src;

/**
 * Entity class representing a Course.
 * Corresponds to MySQL 'courses' table.
 */
public class Course {
    private String courseId;
    private String courseName;
    private String category; // 'Certificate Course', 'Skill Development', 'Technical Training'
    private String duration;
    private int maxCapacity;
    private String description;
    
    // Auxiliary fields computed from joins/views
    private int activeEnrollments;
    private int availableSeats;

    public Course() {
    }

    public Course(String courseId, String courseName, String category, String duration, int maxCapacity, String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.category = category;
        this.duration = duration;
        this.maxCapacity = maxCapacity;
        this.description = description;
    }

    public Course(String courseId, String courseName, String category, String duration, int maxCapacity, String description, int activeEnrollments, int availableSeats) {
        this(courseId, courseName, category, duration, maxCapacity, description);
        this.activeEnrollments = activeEnrollments;
        this.availableSeats = availableSeats;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getActiveEnrollments() {
        return activeEnrollments;
    }

    public void setActiveEnrollments(int activeEnrollments) {
        this.activeEnrollments = activeEnrollments;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getOccupancyRate() {
        if (maxCapacity <= 0) return 0.0;
        return (activeEnrollments * 100.0) / maxCapacity;
    }

    @Override
    public String toString() {
        return String.format("%-10s | %-32s | %-20s | %-10s | Capacity: %2d | Enrolled: %2d | Available: %2d",
                courseId, courseName, category, duration, maxCapacity, activeEnrollments, availableSeats);
    }
}
