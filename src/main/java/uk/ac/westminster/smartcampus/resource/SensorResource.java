package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.data.DataStore;
import uk.ac.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import uk.ac.westminster.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS Resource class for managing Sensors.
 * Handles CRUD operations on the /api/v1/sensors path.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Returns all sensors. Supports optional filtering by type via query parameter.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(dataStore.getSensors().values());

        // If the 'type' query parameter is provided, filter the results
        if (type != null && !type.trim().isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. The roomId in the request body must reference
     * an existing room, otherwise a LinkedResourceNotFoundException is thrown.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Validate required fields
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Sensor ID is required.\"}")
                    .build();
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Sensor type is required.\"}")
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID (roomId) is required.\"}")
                    .build();
        }

        // Check if sensor ID already exists
        if (dataStore.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"A sensor with ID '\" + sensor.getId() + \"' already exists.\"}")
                    .build();
        }

        // Dependency validation: verify that the referenced room exists
        if (dataStore.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor '" + sensor.getId() + "': the specified roomId '"
                    + sensor.getRoomId() + "' does not exist in the system. "
                    + "Please create the room first or provide a valid roomId."
            );
        }

        // Set default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        dataStore.addSensor(sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns detailed information for a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor with ID '\" + sensorId + \"' not found.\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for sensor readings.
     * Delegates /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        // Verify the sensor exists before delegating
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
