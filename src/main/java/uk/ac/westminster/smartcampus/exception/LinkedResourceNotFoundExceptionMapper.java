package uk.ac.westminster.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception Mapper for LinkedResourceNotFoundException.
 * Returns HTTP 422 Unprocessable Entity when a referenced resource
 * (e.g., roomId) does not exist in the system.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", exception.getMessage());
        error.put("resolution", "Ensure the referenced resource exists before creating dependent resources.");

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
