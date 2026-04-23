package uk.ac.westminster.smartcampus.exception;

/**
 * Custom exception thrown when attempting to delete a Room
 * that still has Sensors assigned to it.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
