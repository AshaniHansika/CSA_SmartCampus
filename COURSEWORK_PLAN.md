# Smart Campus Coursework - 1-Day Implementation Plan

This plan breaks down the coursework into a structured 1-day workflow. Each phase focuses on a specific part of the assignment and includes a logical **Git Commit** point to help you build your repository history incrementally.

## Phase 1: Project Setup & Core Models (1-2 hours)
*Goal: Bootstrap the project and define the data structures.*
1. Initialize a Maven web project (`war` packaging).
2. Add dependencies for JAX-RS (Jersey) and JSON binding (Jackson).
3. Create the core POJO models: `Room`, `Sensor`, `SensorReading` with proper getters/setters.
4. Create a thread-safe singleton `DataStore` to hold data in memory using Maps/Lists.
- **Git Commit:** `Setup Maven project, Jersey dependencies, and core models`

## Phase 2: Service Architecture & Discovery (Part 1) (1 hour)
*Goal: Establish the API entry point and basic routing.*
1. Create `SmartCampusApplication` extending `javax.ws.rs.core.Application` with `@ApplicationPath("/api/v1")`.
2. Create `DiscoveryResource` to handle `GET /api/v1` and return API metadata and hypermedia links.
3. Configure `web.xml` (if needed) for Tomcat deployment.
4. Test the deployment and Discovery endpoint via Postman.
- **Git Commit:** `Implement JAX-RS application configuration and Discovery endpoint`

## Phase 3: Room Management (Part 2) (2 hours)
*Goal: Build the Room endpoints and enforce deletion constraints.*
1. Create `RoomResource` mapped to `/api/v1/rooms`.
2. Implement `GET /` (list all rooms) and `POST /` (create new room).
3. Implement `GET /{roomId}` (fetch single room).
4. Implement `DELETE /{roomId}`.
5. Add logic to prevent deletion if the room contains active sensors (will integrate custom exception later).
- **Git Commit:** `Implement RoomResource CRUD operations and safety logic`

## Phase 4: Sensor Operations & Filtering (Part 3) (2 hours)
*Goal: Manage sensors and link them to rooms.*
1. Create `SensorResource` mapped to `/api/v1/sensors`.
2. Implement `POST /` to register a sensor. Include logic to verify that the `roomId` exists in the `DataStore`.
3. Implement `GET /` and add support for the `@QueryParam("type")` to filter sensors.
- **Git Commit:** `Implement SensorResource with registration and query filtering`

## Phase 5: Deep Nesting with Sub-Resources (Part 4) (2 hours)
*Goal: Handle historical readings using the sub-resource locator pattern.*
1. Create `SensorReadingResource` (without a `@Path` annotation at the class level).
2. In `SensorResource`, add a sub-resource locator method for `{sensorId}/readings` that returns an instance of `SensorReadingResource`.
3. In `SensorReadingResource`, implement `GET /` to fetch history.
4. Implement `POST /` to add a reading, ensuring it also updates the `currentValue` of the parent `Sensor` object.
- **Git Commit:** `Implement Sub-Resource Locator pattern for sensor readings`

## Phase 6: Advanced Error Handling & Logging (Part 5) (2 hours)
*Goal: Make the API resilient and observable.*
1. Create custom exceptions: `RoomNotEmptyException`, `LinkedResourceNotFoundException`, `SensorUnavailableException`.
2. Create `ExceptionMapper` classes for these exceptions to return 409, 422, and 403 HTTP status codes respectively.
3. Create a global `ThrowableMapper` to catch unexpected errors and return 500 without leaking stack traces.
4. Implement `ApiLoggingFilter` implementing `ContainerRequestFilter` and `ContainerResponseFilter` using `java.util.logging.Logger`.
5. Register all providers in the application.
- **Git Commit:** `Implement custom ExceptionMappers and API logging filters`

## Phase 7: Documentation (1-2 hours)
*Goal: Complete the required report and repository instructions.*
1. Update `README.md` in the GitHub repo to include:
   - API Design Overview
   - Instructions to build and run the server
   - 5 sample `curl` commands
   - The answers to the 8 theoretical questions posed in the coursework brief.
- **Git Commit:** `Complete README with API documentation and coursework report`

## Phase 8: Final Review & Video Recording (1 hour)
*Goal: Prepare the deliverables.*
1. Perform end-to-end testing of the complete API in Postman.
2. Record the maximum 10-minute Postman video demonstration.
3. Submit the Video link/file via Blackboard.
