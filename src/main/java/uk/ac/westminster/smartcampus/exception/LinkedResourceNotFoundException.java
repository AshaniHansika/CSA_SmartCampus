package uk.ac.westminster.smartcampus.exception;

/**
 * Custom exception thrown when a client attempts to create a resource
 * (e.g., a Sensor) that references another resource (e.g., a Room via roomId)
 * that does not exist in the system.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
