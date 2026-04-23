package uk.ac.westminster.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" Exception Mapper that intercepts any unexpected
 * runtime errors (e.g., NullPointerException, IndexOutOfBoundsException).
 * 
 * Returns a generic HTTP 500 Internal Server Error without exposing
 * internal Java stack traces to external API consumers.
 * 
 * Security: Exposing stack traces could reveal package names, class structures,
 * library versions, and internal logic — information an attacker could exploit
 * to find known vulnerabilities or craft targeted attacks.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace internally for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper", exception);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
