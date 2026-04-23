package uk.ac.westminster.smartcampus.data;

import uk.ac.westminster.smartcampus.model.Room;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton class that acts as the in-memory data store
 * for all Rooms, Sensors, and SensorReadings.
 */
public class DataStore {

    private static DataStore instance;

    // Room ID -> Room object
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Sensor ID -> Sensor object
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Sensor ID -> List of SensorReadings
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        // Pre-populate with some sample data for demonstration
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("ENG-101", "Engineering Lab A", 30);
        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // ---- Room Operations ----

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    // ---- Sensor Operations ----

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Also link the sensor to its room
        Room room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().add(sensor.getId());
        }
        // Initialize an empty readings list for this sensor
        sensorReadings.put(sensor.getId(), new ArrayList<>());
    }

    public Sensor removeSensor(String id) {
        Sensor sensor = sensors.remove(id);
        if (sensor != null) {
            // Unlink sensor from its room
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().remove(sensor.getId());
            }
            // Remove readings for this sensor
            sensorReadings.remove(id);
        }
        return sensor;
    }

    // ---- Sensor Reading Operations ----

    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        // Side effect: update the parent sensor's currentValue
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }
}
