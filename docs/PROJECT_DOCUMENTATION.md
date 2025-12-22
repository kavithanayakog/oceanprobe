# probe — Project Documentation

## Quick receipt & plan
I'll provide an end-to-end project documentation for the probe application: overview, architecture diagram (PlantUML), setup & dependencies, how to populate H2, common fixes for JPA/Entity issues, combining move methods, a line-by-line explanation of `ProbeController`, run & test steps, troubleshooting FAQ, and an appendix with the PlantUML source and quick commands.

Checklist
- [x] Project overview and goals
- [x] Architecture diagram (PlantUML) and explanation
- [x] Setup & Gradle dependency snippets for Lombok & H2
- [x] How to populate H2 (controller-based and SQL-based)
- [x] Common fixes for `ProbeRepository` and `ProbeEntity` issues (Lombok example)
- [x] Combine move forward/back into one method (suggested controller API + tests)
- [x] Line-by-line explanation of `ProbeController` (imports, fields, constructor, endpoints, helpers)
- [x] Run/test/verify commands and curl examples
- [x] Troubleshooting & FAQ
- [x] Appendix: PlantUML source + quick commands

---

## 1. Project overview
The `probe` project exposes a small REST API for controlling probes on a grid. A probe has an (x,y) position and a facing `Direction` (NORTH, SOUTH, EAST, WEST). The API supports creating probes, listing/fetching/deleting probes, sending command sequences (F/B/L/R) and single-step movement or turns. The project persists probe state using Spring Data JPA and an embedded H2 database.

Goals for this documentation
- Help new developers run and test the app locally.
- Explain system architecture and data flow.
- Describe how to seed data into H2 and common JPA/Entity fixes.
- Show how to unify forward/back movement into a single API and provide tests.
- Explain `ProbeController` code line-by-line.

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

Rendered diagram: see `docs/images/architecture.png` if generated; PlantUML source is in the Appendix.


## 3. Setup & prerequisites
Minimum requirements
- JDK 17 (project uses Java toolchain configured for 17)
- Gradle Wrapper (use provided `gradlew.bat` on Windows)
- Lombok (IDE annotation processing enabled)
- No external DB is required (H2 in-memory used by default)

Relevant Gradle snippets (from `build.gradle`)
- Spring Boot + JPA + H2 are already included in the project:

```
implementation 'org.springframework.boot:spring-boot-starter'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'com.h2database:h2'
```

- Lombok (already present as compileOnly and annotationProcessor):

```
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

Enable H2 console (add to `src/main/resources/application.yaml` or `application.properties`):

YAML example:

```
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:mem:probe;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: update
```

Notes:
- `DB_CLOSE_DELAY=-1` keeps the in-memory DB alive until the JVM stops (useful for debugging sessions).
- `ddl-auto: update` keeps schema in sync but for production prefer explicit migrations.


## 4. Populating H2 with sample data
Option A — Programmatic via controller endpoint
- Add a small endpoint (or a `@Component` runner) that persists sample `ProbeEntity` rows. Example controller method you can add to `ProbeController`:

```java
@PostMapping("/sample-data")
public ResponseEntity<List<ProbeEntity>> createSampleData() {
    List<ProbeEntity> list = List.of(
        new ProbeEntity(0, 0, Direction.NORTH),
        new ProbeEntity(2, 2, Direction.EAST)
    );
    List<ProbeEntity> saved = repo.saveAll(list);
    return ResponseEntity.ok(saved);
}
```

- Call: `curl -X POST http://localhost:8080/api/probe/sample-data`

Option B — SQL-based on startup
- Create `src/main/resources/data.sql` with content:

```
INSERT INTO PROBE_ENTITY (id, x, y, direction) VALUES (1, 0, 0, 'NORTH');
INSERT INTO PROBE_ENTITY (id, x, y, direction) VALUES (2, 2, 2, 'EAST');
```

Spring Boot will execute `data.sql` at startup when using an embedded DB.


## 5. Common `ProbeRepository` (JpaRepository) issues and fixes
Symptoms & fixes
- Symptom: "No qualifying bean of type 'ProbeRepository'" — Fix: ensure package is scanned by Spring Boot. The main application class must be in a parent package above `com.ocean.probe.repository`. If not, add `@EnableJpaRepositories(basePackages = "com.ocean.probe.repository")` in a configuration class.

- Symptom: Generic mismatch / compile error — Fix: check `ProbeRepository` signature matches entity and ID type exactly:

```java
public interface ProbeRepository extends JpaRepository<ProbeEntity, Long> { }
```

- Symptom: Wrong import (e.g., importing an entity from `test` package) — Fix: use your main `ProbeEntity` import: `com.ocean.probe.model.ProbeEntity`.

- Tip: `@Repository` annotation is optional for Spring Data interfaces but safe to add if you need custom behavior.


## 6. Typical `ProbeEntity` issues and Lombok example
Common pitfalls
- Missing `@Id` / wrong ID type — JPA needs an `@Id` field whose Java type matches the repository generic.
- No-args constructor — JPA requires a no-arg constructor (can be protected or public).
- Lombok not working — ensure `annotationProcessor 'org.projectlombok:lombok'` in Gradle and IDE annotation processing enabled.

Recommended `ProbeEntity` using Lombok (example you can adopt):

```java
package com.ocean.probe.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "probe_entity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProbeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;
    private int y;

    @Enumerated(EnumType.STRING)
    private Direction direction;
}
```

Notes on Lombok usage
- Prefer `@Getter`/`@Setter` over `@Data` for entities if you want to control equals/hashCode.
- If you provide equals/hashCode, avoid including mutable fields; usually base equals/hashCode on the `id` alone.


## 7. Combine moveForward and moveBackward into a single method
Current status in code
- `Probe` already implements a private `move(int step)` and exposes `moveForward()` and `moveBackward()` that call `move(1)` and `move(-1)` respectively. That is a good internal unification.

Suggested single-step API on controller
- Instead of two endpoints or a boolean `forward` query param, provide a single endpoint that accepts an `int delta` where positive = forward, negative = backward, and 0 = no-op.

Example controller endpoint (replace or add to `ProbeController`):

```java
@PostMapping("/{id}/moveBy")
public ResponseEntity<?> moveBy(@PathVariable Long id, @RequestParam int delta) {
    Optional<ProbeEntity> op = repo.findById(id);
    if (op.isEmpty()) return ResponseEntity.notFound().build();
    ProbeEntity e = op.get();
    Probe p = toProbe(e);
    boolean ok = true;
    if (delta > 0) {
        for (int i = 0; i < delta; i++) { if (!p.moveForward()) { ok = false; break; } }
    } else if (delta < 0) {
        for (int i = 0; i < -delta; i++) { if (!p.moveBackward()) { ok = false; break; } }
    }
    writeBack(e, p);
    repo.save(e);
    if (!ok) return ResponseEntity.badRequest().body("Blocked or out of bounds");
    return ResponseEntity.ok(e);
}
```

Tests to add (high-level)
- moveBy_positiveSteps_shouldAdvancePosition
- moveBy_negativeSteps_shouldRetreatPosition
- moveBy_blocked_shouldReturnBadRequest

Use `@WebMvcTest` with mocked `ProbeRepository` or `@SpringBootTest` for integration tests.


## 8. Line-by-line explanation: `ProbeController.java`
Below is an annotated explanation of `ProbeController`'s key parts. For full file location see `src/main/java/com/ocean/probe/controller/ProbeController.java`.

1) Package and imports
- `package com.ocean.probe.controller;` — places class in the controller package; important for component scanning.
- Imports bring in models, repository, Spring MVC annotations, HTTP types, and collection classes.

2) Class-level annotations
- `@RestController` — shorthand for `@Controller` + `@ResponseBody`. Spring creates a bean and methods return objects serialized to JSON.
- `@RequestMapping("/api/probe")` — base path for all endpoints in this controller.

3) Fields & constructor
- `private final ProbeRepository repo;` — the JPA repository used for CRUD operations; injected via constructor.
- `private final Grid grid;` — an injected `Grid` bean (inner static class) that holds bounds and obstacles.
- Constructor `public ProbeController(ProbeRepository repo, Grid grid)` — Spring injects beans here (constructor injection is recommended, avoids field reflection and is test-friendly).

4) `createProbe` (POST /api/probe)
- `@PostMapping` — maps HTTP POST to this method at `/api/probe`.
- Accepts `@RequestBody ProbeEntity request` — JSON request body is mapped to `ProbeEntity` fields using Jackson.
- Creates a new `ProbeEntity` using only `x,y,direction` from the request (this avoids clients setting `id`).
- Saves via `repo.save(...)` and returns `201 Created` with saved entity.

5) `listProbes` (GET /api/probe)
- `@GetMapping` — returns all probes using `repo.findAll()`.

6) `getProbe` (GET /api/probe/{id})
- Looks up by `id` with `repo.findById(id)`, returns `200 OK` with the entity or `404 Not Found`.

7) `deleteProbe` (DELETE /api/probe/{id})
- Checks existence via `repo.existsById(id)`, deletes via `repo.deleteById(id)` then returns `204 No Content`.

8) `executeCommands` (POST /api/probe/{id}/commands)
- Accepts a `CommandRequest` with a `commands` string (e.g., "FFRBL").
- Loads the entity, converts to an in-memory `Probe` via `toProbe(entity)` which wires the shared grid.
- Iterates command characters; uses `probe.moveForward()` / `moveBackward()` / `turnLeft()` / `turnRight()`.
- If any move fails (`ok == false`), writes current state back to the entity with `writeBack(...)`, persists with `repo.save(entity)`, and returns `400 Bad Request` with a message identifying the failing command.
- If all commands succeed, final state is written back and saved, and a `ProbeStatusResponse` is returned with the current probe state.

9) `getStatus` (GET /api/probe/{id}/status)
- Returns the current in-memory status by mapping stored entity to a `Probe` then to `ProbeStatusResponse`.

10) Movement endpoints and `moveInternal`
- There's a single `move` endpoint (`POST /{id}/move`) that accepts a `forward` boolean query parameter (defaults to true). This delegates to `moveInternal` which loads the entity, moves forward/back using the `Probe` methods, writes back on success and persists; otherwise returns `400`.

11) Turning endpoints and `turn` method
- `turnLeft` and `turnRight` endpoints call the `turn` helper which loads entity, calls `probe.turnLeft()` or `turnRight()`, writes back and saves.

12) Helpers: `toProbe` and `writeBack`
- `toProbe(ProbeEntity e)` creates a `Probe` model from the stored entity's x,y,direction and injects the controller's `grid` so the `Probe` movement logic can validate bounds and obstacles.
- `writeBack(ProbeEntity e, Probe p)` copies probe's in-memory x,y,direction back to the entity object before persisting.

13) DTOs `CommandRequest` and `ProbeStatusResponse`
- `CommandRequest` is a minimal request type exposing a `commands` field and getter/setter so Jackson can serialize/deserialize.
- `ProbeStatusResponse` is an immutable view of the `Probe` used for API responses (x,y,direction,visited set).

14) Inner class `Grid`
- `Grid` stores `width` and `height` and a set of obstacles (as `Point`). It exposes `isWithinBounds`, `addObstacle` and `isObstacle`. `Probe` uses `grid` to prevent invalid moves.


## 9. Run, test, verify
Build & run (Windows PowerShell)

```
.
\> .\gradlew.bat clean build
.
\> .\gradlew.bat bootRun
```

Access H2 console (if enabled)
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:probe
- User: sa (password blank unless configured)

Curl examples
- Create probe:

```
curl -X POST -H "Content-Type: application/json" -d '{"x":0,"y":0,"direction":"NORTH"}' http://localhost:8080/api/probe
```

- Move forward via single-step endpoint:

```
curl -X POST http://localhost:8080/api/probe/1/move?forward=true
```

- Send command sequence:

```
curl -X POST -H "Content-Type: application/json" -d '{"commands":"FFRBL"}' http://localhost:8080/api/probe/1/commands
```

Run tests
- `.
\> .\gradlew.bat test`


## 10. Troubleshooting & FAQs
- Q: App doesn't start due to JPA mapping errors
  - A: Check entity annotations and datasource URL in `application.yaml`. Ensure `hibernate.ddl-auto` is configured appropriately.

- Q: Repository bean not found
  - A: Confirm main application class package is the parent of `com.ocean.probe.*`. If not, explicitly configure `@EnableJpaRepositories(basePackages = "com.ocean.probe.repository")`.

- Q: Lombok not generating constructors/getters
  - A: Ensure `annotationProcessor 'org.projectlombok:lombok'` is present in `build.gradle` and enable annotation processing in your IDE.

- Q: H2 console unreachable
  - A: Ensure `spring.h2.console.enabled` is true and `server.port` is the port you're connecting to. Check logs for bind errors.


## 11. Appendix
PlantUML source (copy to `docs/architecture.puml` or paste into an online PlantUML editor):

```
@startuml
actor Client
node "CommandProbeController / ProbeController" as Controller
component "ProbeService (optional)" as Service
component "ProbeRepository (JpaRepository)" as Repo
entity "ProbeEntity" as Entity
database "H2 (in-memory)" as H2

Client -> Controller : HTTP (create / list / commands / move)
Controller -> Repo : findById(id) / save(entity) / findAll()
Controller -> Entity : toProbe() -> create Probe
Controller -> Service : (if present) call business logic
Repo --> H2 : SQL
Controller -> Client : HTTP response (200/201/400/404)

@enduml
```

Quick commands
- Build: `./gradlew.bat clean build`
- Run: `./gradlew.bat bootRun`
- Tests: `./gradlew.bat test`
- H2 Console (if enabled): `http://localhost:8080/h2-console`


---

If you'd like, I can:
- Generate a rendered PNG of the PlantUML diagram and add it to `docs/images/architecture.png`.
- Add an example `data.sql` and/or an actual `@PostMapping("/sample-data")` method into the controller source (I won't change source files without your confirmation).
- Convert the long `ProbeController` explanation into inline commented source code if you prefer per-line comments.

Requirements coverage
- Project overview: Done
- Architecture diagram: PlantUML included in Appendix
- Setup with Gradle snippets: Done (snippets match `build.gradle`)
- Populate H2: Programmatic & SQL approaches included
- Repository & Entity issues: Common issues and fixes included; Lombok entity example provided
- Combine move methods: Example API + test suggestions included
- Line-by-line `ProbeController` explanation: Done
- Run/test/verify: Commands and curl examples included


