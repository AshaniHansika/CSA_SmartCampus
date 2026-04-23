package uk.ac.westminster.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception Mapper for RoomNotEmptyException.
 * Returns HTTP 409 Conflict with a JSON body explaining
 * that the room is currently occupied by active hardware.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 409);
        error.put("error", "Conflict");
        error.put("message", exception.getMessage());
        error.put("resolution", "Remove or reassign all sensors from this room before attempting deletion.");

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
