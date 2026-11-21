package src;

/**
 * Entity class representing a Student.
 * Inherits from Person to demonstrate Inheritance and Super Constructor usage.
 * Corresponds to MySQL 'students' table.
 */
public class Student extends Person {
    private String department;
    private int yearOfStudy;

    public Student() {
        super();
    }

    public Student(String studentId, String studentName, String email, String phone, String department, int yearOfStudy) {
        super(studentId, studentName, email, phone);
        this.department = department;
        this.yearOfStudy = yearOfStudy;
    }

    @Override
    public String getRoleDescription() {
        return "Student enrolled in " + department + " (Year " + yearOfStudy + ")";
    }

    public String getStudentId() {
        return getId();
    }

    public void setStudentId(String studentId) {
        setId(studentId);
    }

    public String getStudentName() {
        return getName();
    }

    public void setStudentName(String studentName) {
        setName(studentName);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    @Override
    public String toString() {
        return String.format("%-10s | %-20s | %-30s | %-12s | %-22s | Year %d",
                id, name, email, phone, department, yearOfStudy);
    }
}
