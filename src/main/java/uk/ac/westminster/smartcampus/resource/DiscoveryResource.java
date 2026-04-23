package uk.ac.westminster.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint that provides API metadata, versioning info,
 * and a map of available resource collections (HATEOAS-style links).
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        Map<String, Object> apiInfo = new LinkedHashMap<>();
        apiInfo.put("name", "Smart Campus Sensor & Room Management API");
        apiInfo.put("version", "1.0");
        apiInfo.put("description", "RESTful API for managing campus rooms, sensors, and sensor readings.");
        apiInfo.put("contact", "admin@westminster.ac.uk");

        // Hypermedia links to primary resource collections
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        apiInfo.put("resources", resources);

        return Response.ok(apiInfo).build();
    }
}
