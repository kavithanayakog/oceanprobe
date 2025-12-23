# Probe — Project Documentation
## 1. Project overview
The `probe` project exposes a small REST API for controlling probes on a grid. A probe has an (x,y) position and a facing `Direction` (NORTH, SOUTH, EAST, WEST). The API supports creating probes, listing/fetching/deleting probes, sending command sequences (F/B/L/R) and single-step movement or turns. The project persists probe state using Spring Data JPA and an embedded H2 database.

Functional Requirements
Core Capabilities
  •	Initialize probe with:
    o	Grid size
    o	Starting position (x, y)
    o	Initial direction
  •	Execute commands:
    o	Move forward
    o	Move backward
    o	Turn left
    o	Turn right
  •	Prevent:
    o	Moving outside grid boundaries
    o	Colliding with obstacles
  •	Track and print:
    o	All visited coordinates in order

Key files
- `src/main/java/com/ocean/probe/controller/ProbeController.java`
- `src/main/java/com/ocean/probe/model/ProbeEntity.java`
- `src/main/java/com/ocean/probe/model/Probe.java`
- `src/main/java/com/ocean/probe/repository/ProbeRepository.java`


## 2. Architecture (summary)
Components
- Client (curl/Postman/UI) — sends HTTP requests.
- `ProbeController` (REST controller) — handles HTTP requests and coordinates operations.
- `Probe` (in-memory model) — performs movement logic and visited-tracking.
- `ProbeEntity` (JPA entity) — persisted representation of a probe.
- `ProbeRepository` (Spring Data JPA) — repository for CRUD operations.
- H2 (embedded DB) — stores probe rows.

Data flow (simplified)
Client -> HTTP -> ProbeController -> (toProbe) -> Probe (in-memory) -> mutate -> writeBack -> ProbeRepository.save() -> H2

## 3. Setup & prerequisites
Minimum requirements
- JDK 17 
- Gradle Wrapper
- Lombok (IDE annotation processing enabled)
- No external DB is required (H2 in-memory used by default)

## 4. Class Diagram

|   Grid      |
|-------------|
| width       |
| height      |

| Position    |
|-------------|
| x           |
| y           |

| Direction   |
|-------------|
| NORTH       |
| SOUTH       |
| EAST        |
| WEST        |

| Probe       |
|-------------|
| position    |
| direction   |

| Navigator   |
|-------------|
| move()      |
| turn()      |

## 6. Movement Logic: Forward / Backward Movement
Facing	Forward	Backward		
NORTH	y + 1	y - 1
SOUTH	y - 1	y + 1
EAST	x + 1	x - 1
WEST	x - 1	x + 1

## 5. Clean Code Practices Used
- Single Responsibility Principle
- Meaningful class and method names
- No hard-coded values
- Immutability where possible
- Centralized validation logic
  
## 6. Conclusion
This project demonstrates:
  - Strong Object-Oriented Design
  - Proper TDD workflow
  - Robust edge case handling
  - Clean and extensible architecture



