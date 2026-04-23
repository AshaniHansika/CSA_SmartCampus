package uk.ac.westminster.smartcampus.exception;

/**
 * Custom exception thrown when a client attempts to POST a reading
 * to a sensor that is currently in "MAINTENANCE" status.
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
