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

## How to Build & Run

You can run this API using any of the following three methods. Regardless of the method, the API will be available at the root context: `http://localhost:8080/api/v1`.

### Option 1: Terminal Run (Fastest)
Use the embedded Jetty server to run directly from your terminal.
1. Open your terminal in the project root.
2. Run the following command:
   ```bash
   mvn jetty:run
   ```
3. The API is now live at: `http://localhost:8080/api/v1`

---

### Option 2: Manual Deployment to Tomcat
Build a standard WAR file and deploy it to a standalone Tomcat server.
1. Build the project:
   ```bash
   mvn clean package
   ```
2. Locate the generated file: `target/ROOT.war`.
3. Copy `ROOT.war` to your Tomcat's `webapps/` directory.
   - *Note: Naming it `ROOT.war` ensures it is accessible at `/api/v1` instead of `/smartcampus/api/v1`.*
4. Start Tomcat using `bin/startup.bat` (Windows) or `bin/startup.sh` (Linux).
5. The API will be available at: `http://localhost:8080/api/v1`

---

### Option 3: NetBeans Integration (IDE)
Use NetBeans to manage the lifecycle and link Tomcat automatically.
1. Open NetBeans and select **File > Open Project**.
2. Select the `CSA_SmartCampus` folder.
3. **Link Tomcat:**
   - Right-click the project > **Properties** > **Run**.
   - Select **Server**: Apache Tomcat.
   - Set **Context Path** to `/`.
4. Click **Run** (Green arrow). The API will be at: `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a New Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "SCI-201", "name": "Science Lab B", "capacity": 40}'
```

### 3. Register a New Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 0.0, "roomId": "SCI-201"}'
```

### 4. Get All Sensors (Filtered by Type)
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.5}'
```

---

## Coursework Report — Question Answers

**Q1: Default lifecycle of a JAX-RS Resource class — per-request or singleton?**
By default, JAX-RS uses a **per-request** lifecycle. This means Jersey creates a new object for every single HTTP request and destroys it right after. Because of this, you can't store data in normal variables in the resource class—it would get lost. That's why we use a singleton `DataStore` with `ConcurrentHashMap` to keep our data persistent and thread-safe across all requests.

**Q2: Why is HATEOAS considered a hallmark of advanced RESTful design?**
HATEOAS makes an API "self-navigating." Instead of the client having to guess or hardcode every URL, the server sends back links telling the client what they can do next. It’s better because if we change a URL path later, the client doesn't break as long as it follows the links. It makes the API more flexible and easier to use without constantly checking documentation.

**Q3: Implications of returning only IDs versus full room objects when listing rooms?**
If we only return IDs, the response is tiny and fast, but the client has to make a bunch of extra calls to get the actual details (the "N+1" problem). If we return full objects, the response is bigger, but the client gets everything in one go. For this project, since our room data is small, returning full objects is better because it saves the client from making unnecessary extra network requests.

**Q4: Is the DELETE operation idempotent in your implementation?**
Yes, it is. If you delete a room once, it’s removed (204 No Content). If you try to delete it again, it stays gone (404 Not Found). Even though the status code changes, the end result on the server is exactly the same—the room is no longer there. Idempotency means that doing the same thing many times has the same final effect as doing it once.

**Q5: Consequences of sending data in a format other than JSON when @Consumes(APPLICATION_JSON) is specified?**
If a client sends something like XML or plain text instead of JSON, the server will immediately reject the request and return a **415 Unsupported Media Type** error. The code inside our resource method won't even execute. This is a built-in safety check in Jersey to ensure the server only tries to process data it actually knows how to handle.

**Q6: Why is the @QueryParam approach generally superior to path-based filtering?**
Using `@QueryParam` (like `?type=CO2`) is better because it’s specifically meant for **filtering** a collection, whereas path segments are meant for identifying specific unique resources. It's also much easier to combine multiple filters (like `?type=CO2&status=ACTIVE`). Path-based filtering for everything makes URLs messy and confusing very quickly.

**Q7: Architectural benefits of the Sub-Resource Locator pattern?**
It helps keep the code clean and follows the "Single Responsibility" principle. Instead of having one giant class that handles every single endpoint, we split them up—`SensorResource` handles basic sensor tasks, and it "hands off" to `SensorReadingResource` specifically for reading history. This makes the code much easier to read and maintain.

**Q8: Why is HTTP 422 often more accurate than 404 for a missing reference inside a valid JSON payload?**
404 usually means the URL itself doesn't exist. But if you're POSTing to `/sensors` and that endpoint is correct, a 404 is misleading. 422 (Unprocessable Entity) is better because it says: "I understand your JSON and the URL is correct, but the data inside (like a Room ID that doesn't exist) is invalid." It’s much more helpful for debugging.

**Q9: Security risks of exposing internal Java stack traces to external consumers?**
Stack traces are a gift to hackers. They reveal internal package names, class structures, and even specific library versions you are using. If an attacker knows you're using an old version of Jersey or Tomcat, they can look up known vulnerabilities to attack your server. Our `GenericExceptionMapper` hides these details to keep the server secure.

**Q10: Why use JAX-RS filters for logging instead of manual Logger.info() statements?**
Filters are "set it and forget it." Instead of manually adding a log line to every single method (which is easy to forget when you add new ones), a filter automatically catches every single request and response. It keeps the code clean and ensures that our logs are always consistent across the entire API without any duplicate code.
