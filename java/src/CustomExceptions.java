package src;

/**
 * Custom exceptions representing business rule violations in the system.
 * Demonstrates: Java Exception Handling, Checked Exceptions, Subclassing Exception
 * as per College Syllabus Unit 3.
 */
public class CustomExceptions {

    public static class CourseFullException extends Exception {
        public CourseFullException(String message) {
            super(message);
        }
    }

    public static class DuplicateRegistrationException extends Exception {
        public DuplicateRegistrationException(String message) {
            super(message);
        }
    }

    public static class RecordNotFoundException extends Exception {
        public RecordNotFoundException(String message) {
            super(message);
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class DatabaseOperationException extends RuntimeException {
        public DatabaseOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
