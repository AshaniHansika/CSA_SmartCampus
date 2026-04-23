package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.data.DataStore;
import uk.ac.westminster.smartcampus.exception.RoomNotEmptyException;
import uk.ac.westminster.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * JAX-RS Resource class for managing Rooms.
 * Handles CRUD operations on the /api/v1/rooms path.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(dataStore.getRooms().values());
        return Response.ok(rooms).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created on success.
     */
    @POST
    public Response createRoom(Room room) {
        // Validate required fields
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required.\"}")
                    .build();
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room name is required.\"}")
                    .build();
        }

        // Check if room already exists
        if (dataStore.getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"A room with ID '\" + room.getId() + \"' already exists.\"}")
                    .build();
        }

        dataStore.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room with ID '\" + roomId + \"' not found.\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. A room CANNOT be deleted if it still has sensors assigned to it.
     * This enforces the business logic constraint to prevent data orphans.
     * 
     * Idempotency: The first DELETE removes the room and returns 204.
     * Subsequent DELETE requests for the same ID return 404 (room not found).
     * This is considered idempotent because repeated calls do not cause
     * additional side effects beyond the initial deletion.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room with ID '\" + roomId + \"' not found.\"}")
                    .build();
        }

        // Business Logic: Block deletion if the room has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has "
                    + room.getSensorIds().size() + " sensor(s) assigned to it. "
                    + "Please reassign or remove all sensors before decommissioning this room."
            );
        }

        dataStore.removeRoom(roomId);
        return Response.noContent().build(); // 204 No Content
    }
}
