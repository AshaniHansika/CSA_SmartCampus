# Smart Campus — Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures  
**University of Westminster — School of Computer Science and Engineering**  
**Student:** [Your Name] — [Your Student ID]

---

## Table of Contents
1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [How to Build & Run](#how-to-build--run)
4. [API Endpoints](#api-endpoints)
5. [Sample curl Commands](#sample-curl-commands)
6. [Coursework Report — Question Answers](#coursework-report--question-answers)

---

## API Design Overview

This project implements a RESTful API for the university's **"Smart Campus"** initiative. The API manages three core resources:

- **Rooms** — Physical campus rooms with capacity and linked sensors.
- **Sensors** — Devices (temperature, CO2, occupancy) deployed in rooms.
- **Sensor Readings** — Historical measurement data from each sensor.

The API follows RESTful principles with:
- Resource-based URL design (`/api/v1/rooms`, `/api/v1/sensors`)
- Proper HTTP methods (GET, POST, DELETE)
- Meaningful HTTP status codes (200, 201, 204, 400, 403, 404, 409, 422, 500)
- JSON request/response bodies
- Sub-resource locator pattern for nested readings
- Custom exception mappers for resilient error handling
- Request/response logging via JAX-RS filters

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | JAX-RS (Jersey 2.35) |
| JSON Binding | Jackson (via jersey-media-json-jackson) |
| Servlet Container | Apache Tomcat 9.x |
| Build Tool | Maven |
| Data Storage | In-memory (ConcurrentHashMap) |
| Java Version | Java 11+ |

---

## How to Build & Run

### Prerequisites
- Java 11 or higher installed
- Apache Maven installed
- Apache Tomcat 9.x installed

### Step 1: Clone the Repository
```bash
git clone https://github.com/AshaniHansika/CSA_SmartCampus.git
cd CSA_SmartCampus
```

### Step 2: Build the WAR File
```bash
mvn clean package
```
This produces `target/smartcampus.war`.

### Step 3: Deploy to Tomcat
1. Copy the `smartcampus.war` file to Tomcat's `webapps/` directory.
2. Start Tomcat (e.g., `bin/startup.bat` on Windows or `bin/startup.sh` on Linux).
3. The API will be available at: `http://localhost:8080/smartcampus/api/v1`

### Step 4: Verify
Open your browser or Postman and navigate to:
```
http://localhost:8080/smartcampus/api/v1
```
You should see the Discovery endpoint JSON response.

---

## API Endpoints

### Discovery
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1` | API metadata and HATEOAS links |

### Rooms
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors are assigned) |

### Sensors
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sensors` | List all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId exists) |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |

### Sensor Readings (Sub-Resource)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (updates sensor's currentValue) |

### Error Responses
| Status Code | Scenario |
|-------------|----------|
| 400 | Missing required fields |
| 403 | Posting reading to a sensor in MAINTENANCE mode |
| 404 | Resource not found |
| 409 | Deleting a room that still has sensors assigned |
| 422 | Creating a sensor with a non-existent roomId |
| 500 | Unexpected server error (stack traces hidden) |

---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/smartcampus/api/v1
```

### 2. Create a New Room
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "SCI-201", "name": "Science Lab B", "capacity": 40}'
```

### 3. Register a New Sensor
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 0.0, "roomId": "LIB-301"}'
```

### 4. Get All Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/smartcampus/api/v1/sensors?type=Temperature"
```

### 5. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/smartcampus/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.5}'
```

---

## Coursework Report — Question Answers

### Part 1: Service Architecture & Setup

**Q1: Default lifecycle of a JAX-RS Resource class — per-request or singleton?**

By default, JAX-RS resource classes follow a **per-request lifecycle**. This means the JAX-RS runtime (Jersey) creates a **new instance** of the resource class for every incoming HTTP request. Once the request is processed and the response is sent, the instance is discarded and eligible for garbage collection.

This architectural decision has significant implications for managing in-memory data. Since each request gets its own resource instance, any data stored as instance variables would be lost between requests. To persist data across requests, we use a **singleton `DataStore` class** that employs the `synchronized` singleton pattern and stores data in `ConcurrentHashMap` collections. `ConcurrentHashMap` is chosen because it provides thread-safe read and write operations without requiring explicit synchronization blocks, which prevents race conditions when multiple requests arrive simultaneously. This approach ensures data consistency while maintaining high performance under concurrent load.

**Q2: Why is the provision of "Hypermedia" (HATEOAS) considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia As The Engine Of Application State) is considered the highest level of REST maturity (Level 3 in the Richardson Maturity Model). It transforms a REST API from a static collection of endpoints into a **self-describing, navigable service**.

Benefits for client developers compared to static documentation:
- **Discoverability**: Clients can explore the entire API by following links from the root endpoint, rather than hardcoding URLs from documentation that may become outdated.
- **Decoupling**: Clients depend on link relations (e.g., "rooms", "sensors") rather than specific URL structures. The server can change its URL patterns without breaking clients.
- **Reduced errors**: Clients don't need to construct URLs manually, reducing the risk of typos or incorrect path construction.
- **Self-documentation**: The API describes its own capabilities at runtime, meaning clients always have up-to-date navigation information.

---

### Part 2: Room Management

**Q3: Implications of returning only IDs versus full room objects when listing rooms?**

Returning **only IDs** has the advantage of minimal network bandwidth usage and faster response times, especially when the room collection grows large. However, this forces clients to make additional HTTP requests (N+1 problem) to fetch the details of each room they are interested in, which increases overall latency and server load.

Returning **full room objects** consumes more bandwidth per response but gives clients all the information they need in a single request. This reduces the total number of HTTP round-trips and simplifies client-side processing since the client does not need to manage multiple asynchronous fetches. For our Smart Campus API, we return full objects because the room data is relatively small (a few fields per room), and the convenience of a single request outweighs the marginal bandwidth cost.

**Q4: Is the DELETE operation idempotent in your implementation?**

Yes, the DELETE operation is **idempotent** in our implementation. Idempotency means that making the same request multiple times produces the same server-side state as making it once.

- **First DELETE request**: The room is found and removed from the data store. The server returns `204 No Content`.
- **Second (and subsequent) DELETE requests** for the same room ID: The room no longer exists in the data store. The server returns `404 Not Found`.

The key point is that after the first successful deletion, the server state does not change regardless of how many times the same DELETE request is sent. The room remains deleted. While the HTTP status code changes from 204 to 404, the resulting server state is identical, which satisfies the definition of idempotency. This is safe for clients because retrying a failed or timed-out DELETE request will never cause unintended side effects.

---

### Part 3: Sensor Operations & Linking

**Q5: Technical consequences of sending data in a format other than application/json when @Consumes(APPLICATION_JSON) is specified?**

When `@Consumes(MediaType.APPLICATION_JSON)` is annotated on a POST method, JAX-RS uses content negotiation to determine whether it can process the incoming request. If a client sends a request with a `Content-Type` header of `text/plain` or `application/xml`, the JAX-RS runtime will **reject the request before it reaches the resource method** and automatically return an **HTTP 415 Unsupported Media Type** response.

This happens because:
1. JAX-RS matches incoming requests not only by URL path and HTTP method but also by the `Content-Type` header against the `@Consumes` annotation.
2. If no matching resource method is found for the given content type, Jersey responds with 415.
3. The request body is never deserialized, and the resource method code never executes.

This is a built-in protection mechanism that enforces data format contracts between client and server.

**Q6: Why is the @QueryParam approach generally superior to path-based filtering?**

Using `@QueryParam` (e.g., `GET /sensors?type=CO2`) is generally superior to path-based filtering (e.g., `GET /sensors/type/CO2`) for several reasons:

1. **Semantics**: Query parameters express **optional filters** on a collection, while path segments represent **hierarchical resource identifiers**. A sensor type is not a sub-resource of sensors; it is a filter criterion.
2. **Composability**: Multiple query parameters can be combined easily (e.g., `?type=CO2&status=ACTIVE`), whereas path-based filtering becomes unwieldy with multiple filters (`/sensors/type/CO2/status/ACTIVE`).
3. **Optionality**: Query parameters are inherently optional — omitting them returns the unfiltered collection. Path segments are mandatory; omitting them results in a different URL altogether.
4. **Caching**: Query parameters work naturally with HTTP caching mechanisms, where `GET /sensors` and `GET /sensors?type=CO2` are recognized as different cacheable resources.
5. **Convention**: The query string approach aligns with REST conventions and how most modern APIs (Google, GitHub, Twitter) implement filtering.

---

### Part 4: Deep Nesting with Sub-Resources

**Q7: Architectural benefits of the Sub-Resource Locator pattern?**

The Sub-Resource Locator pattern provides significant architectural benefits for managing complex, nested APIs:

1. **Separation of Concerns**: Each resource class is responsible for a single entity type. `SensorResource` handles sensor CRUD, while `SensorReadingResource` handles reading operations. This follows the Single Responsibility Principle.

2. **Reduced Complexity**: Without sub-resource locators, all paths like `/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, and `/sensors/{id}/readings/{rid}` would need to be defined in a single, massive controller class. This leads to bloated, difficult-to-maintain code.

3. **Reusability**: The `SensorReadingResource` class can potentially be reused in different contexts if needed, since it is decoupled from the parent resource's path structure.

4. **Contextual Delegation**: The parent resource (SensorResource) can validate that the sensor exists before delegating to the sub-resource. This creates a natural validation chain where context (the sensor ID) flows from parent to child.

5. **Testability**: Smaller, focused classes are easier to unit test in isolation.

6. **Team Scalability**: Different developers can work on different resource classes simultaneously without merge conflicts.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q8: Why is HTTP 422 often more semantically accurate than 404 for a missing reference inside a valid JSON payload?**

HTTP 404 means "the resource identified by the request URL was not found." When a client POSTs a new sensor to `/api/v1/sensors`, the URL `/api/v1/sensors` **does exist** — the request reaches the correct endpoint, and the JSON payload is syntactically valid.

HTTP 422 (Unprocessable Entity) means "the server understands the content type and the syntax of the request entity is correct, but it was unable to process the contained instructions." This accurately describes the situation: the JSON is well-formed, but the **semantic content** (a `roomId` referencing a non-existent room) makes it impossible to complete the operation.

Using 404 would be misleading because it would suggest that the `/api/v1/sensors` endpoint itself does not exist, confusing client developers. Using 422 clearly communicates that the request was understood but the **data within it** failed validation — a crucial distinction for API consumers debugging their integrations.

**Q9: Security risks of exposing internal Java stack traces to external API consumers?**

Exposing raw Java stack traces to external users poses severe security risks:

1. **Package and Class Name Exposure**: Stack traces reveal the full package hierarchy (e.g., `uk.ac.westminster.smartcampus.resource.SensorResource`), giving attackers a map of the application's internal structure.

2. **Library and Version Discovery**: Traces often include framework class names and versions (e.g., `jersey-container-servlet-core-2.35`), allowing attackers to search for known CVEs (Common Vulnerabilities and Exposures) targeting those specific versions.

3. **Logic and Flow Revelation**: The method call chain in a trace reveals business logic flow, database interaction patterns, and error handling weaknesses.

4. **File Path Disclosure**: Stack traces may expose server file system paths, helping attackers understand the deployment environment.

5. **SQL/Query Exposure**: If database-related exceptions leak, they may expose table names, column names, or query structures, enabling SQL injection attacks.

Our `GenericExceptionMapper` prevents all of this by catching any unhandled `Throwable`, logging the full details server-side for debugging, and returning only a generic error message to the client.

**Q10: Why is it advantageous to use JAX-RS filters for logging rather than manual Logger.info() statements?**

Using JAX-RS filters (`ContainerRequestFilter` / `ContainerResponseFilter`) for cross-cutting concerns like logging offers several advantages over manually inserting `Logger.info()` in every resource method:

1. **DRY (Don't Repeat Yourself)**: A single filter class handles logging for all endpoints. Manual logging requires duplicating code in every resource method.

2. **Consistency**: Filters guarantee that every request and response is logged in the same format, eliminating the risk of developers forgetting to add logging in new endpoints.

3. **Separation of Concerns**: Logging logic is decoupled from business logic, keeping resource classes clean and focused.

4. **Maintainability**: Changing the log format or adding new logged fields requires modifying only one class, not every resource method.

5. **Non-invasive**: Filters can be added or removed via the `@Provider` annotation without modifying any resource class code, supporting the Open/Closed Principle.

6. **Completeness**: Filters execute even when exceptions occur before reaching the resource method (e.g., 415 Unsupported Media Type), ensuring comprehensive observability.
