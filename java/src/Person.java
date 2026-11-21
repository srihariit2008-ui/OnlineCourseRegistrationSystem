package src;

/**
 * Abstract superclass representing a person in the institution.
 * Demonstrates: OOP Inheritance, Abstract Classes, Encapsulation,
 * and Constructors as per College Syllabus Unit 2.
 */
public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;

    public Person() {
    }

    public Person(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Abstract method demonstrating dynamic method dispatch
    public abstract String getRoleDescription();

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Email: %s | Phone: %s", id, name, email, phone);
    }
}
