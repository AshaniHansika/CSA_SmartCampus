package uk.ac.westminster.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API observability filter that logs every incoming request and outgoing response.
 * 
 * Implements both ContainerRequestFilter and ContainerResponseFilter to provide
 * cross-cutting logging without polluting individual resource methods.
 * 
 * Using JAX-RS filters for logging is advantageous because it applies uniformly
 * to all endpoints, follows the Single Responsibility Principle, and can be
 * added or removed without modifying any resource class code.
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Logs the HTTP method and URI for every incoming request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("Incoming Request: " + requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri());
    }

    /**
     * Logs the final HTTP status code for every outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("Outgoing Response: " + requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri()
                + " -> Status: " + responseContext.getStatus());
    }
}
