package uk.ac.westminster.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class.
 * Establishes the versioned API entry point at /api/v1.
 * 
 * JAX-RS uses a per-request lifecycle by default — a new instance of each
 * resource class is created for every incoming HTTP request. This means
 * that instance variables in resource classes are NOT shared across requests.
 * To share state, we use a thread-safe singleton DataStore (with ConcurrentHashMaps).
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Jersey will auto-scan and register all resource classes and providers
    // in the same package and sub-packages.
}
