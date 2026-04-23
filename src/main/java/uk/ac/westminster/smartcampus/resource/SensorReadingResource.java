package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.data.DataStore;
import uk.ac.westminster.smartcampus.exception.SensorUnavailableException;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Sub-resource class for managing SensorReadings.
 * This class does NOT have a @Path annotation at the class level.
 * It is instantiated and returned by SensorResource's sub-resource locator method.
 * 
 * Handles:
 *   GET  /api/v1/sensors/{sensorId}/readings       - Fetch reading history
 *   POST /api/v1/sensors/{sensorId}/readings       - Append a new reading
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the historical list of readings for this sensor.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> readings = dataStore.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to this sensor's history.
     * 
     * Side Effect: Updates the parent Sensor's currentValue field
     * to ensure data consistency across the API.
     * 
     * Constraint: A sensor in "MAINTENANCE" status cannot accept new readings.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);

        // State constraint: sensors in MAINTENANCE mode cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept new readings. "
                    + "Please wait until the sensor is back ACTIVE before submitting data."
            );
        }

        // Set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // addReading also updates the parent sensor's currentValue (side effect)
        dataStore.addReading(sensorId, reading);

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
