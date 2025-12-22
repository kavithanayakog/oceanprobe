package com.ocean.probe.controller;

import com.ocean.probe.model.Point;
import com.ocean.probe.model.Probe;
import com.ocean.probe.model.ProbeEntity;
import com.ocean.probe.repository.ProbeRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/probe")
public class ProbeController {

    private final ProbeRepository repo;
    private final Grid grid; // injected Grid bean

    public ProbeController(ProbeRepository repo, Grid grid) {
        this.repo = repo;
        this.grid = grid;
    }

    // create and persist a new probe
    @PostMapping
    public ResponseEntity<ProbeEntity> createProbe(@RequestBody ProbeEntity request) {
        ProbeEntity saved = repo.save(new ProbeEntity(request.getX(), request.getY(), request.getDirection()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // list all probes
    @GetMapping
    public List<ProbeEntity> listProbes() {
        return repo.findAll();
    }

    // get probe by id
    @GetMapping("/{id}")
    public ResponseEntity<ProbeEntity> getProbe(@PathVariable Long id) {
        Optional<ProbeEntity> op = repo.findById(id);
        return op.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProbe(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Execute a sequence of commands on stored probe
    // Commands: F - Move Forward, B - Move Backward, L - Turn Left, R - Turn Right
    @PostMapping("/{id}/commands")
    public ResponseEntity<?> executeCommands(@PathVariable Long id, @RequestBody CommandRequest request) {
        Optional<ProbeEntity> op = repo.findById(id);
        if (op.isEmpty()) return ResponseEntity.notFound().build();

        ProbeEntity entity = op.get();
        Probe probe = toProbe(entity);

        for (char command : request.getCommands().toCharArray()) {
            boolean ok = true;
            switch (command) {
                case 'F': ok = probe.moveForward(); break;
                case 'B': ok = probe.moveBackward(); break;
                case 'L': probe.turnLeft(); break;
                case 'R': probe.turnRight(); break;
                default: // ignore
            }
            if (!ok) {
                // blocked or out of bounds - write current state back and return bad request
                writeBack(entity, probe);
                repo.save(entity);
                return ResponseEntity.badRequest().body("Blocked or out of bounds at command: " + command);
            }
        }
        writeBack(entity, probe);
        repo.save(entity);
        return ResponseEntity.ok(new ProbeStatusResponse(probe));
    }

    // Get current probe status
    @GetMapping("/{id}/status")
    public ResponseEntity<ProbeStatusResponse> getStatus(@PathVariable Long id) {
        Optional<ProbeEntity> op = repo.findById(id);
        if (op.isEmpty()) return ResponseEntity.notFound().build();
        Probe probe = toProbe(op.get());
        return ResponseEntity.ok(new ProbeStatusResponse(probe));
    }

    // movement endpoints
    // single movement endpoint (forward or backward via query param)
    @PostMapping("/{id}/move")
    public ResponseEntity<?> move(@PathVariable Long id, @RequestParam(name = "forward", defaultValue = "true") boolean forward) {
        return moveInternal(id, forward);
    }

    private ResponseEntity<?> moveInternal(Long id, boolean forward) {
        Optional<ProbeEntity> op = repo.findById(id);
        if (op.isEmpty()) return ResponseEntity.notFound().build();

        ProbeEntity entity = op.get();
        Probe probe = toProbe(entity);
        boolean ok = forward ? probe.moveForward() : probe.moveBackward();
        if (ok) {
            writeBack(entity, probe);
            repo.save(entity);
            return ResponseEntity.ok(entity);
        }
        return ResponseEntity.badRequest().body("Blocked or out of bounds");
    }

    @PostMapping("/{id}/turnLeft")
    public ResponseEntity<?> turnLeft(@PathVariable Long id) {
        return turn(id, true);
    }

    @PostMapping("/{id}/turnRight")
    public ResponseEntity<?> turnRight(@PathVariable Long id) {
        return turn(id, false);
    }

    private ResponseEntity<?> turn(Long id, boolean left) {
        Optional<ProbeEntity> op = repo.findById(id);
        if (op.isEmpty()) return ResponseEntity.notFound().build();

        ProbeEntity entity = op.get();
        Probe probe = toProbe(entity);
        if (left) probe.turnLeft(); else probe.turnRight();
        writeBack(entity, probe);
        repo.save(entity);
        return ResponseEntity.ok(entity);
    }

    private Probe toProbe(ProbeEntity e) {
        return new Probe(e.getX(), e.getY(), e.getDirection(), grid);
    }

    private void writeBack(ProbeEntity e, Probe p) {
        e.setX(p.getX());
        e.setY(p.getY());
        e.setDirection(p.getDirection());
    }

    // DTOs for request/response
    public static class CommandRequest {
        private String commands;
        public String getCommands() { return commands; }
        public void setCommands(String commands) { this.commands = commands; }
    }

    public static class ProbeStatusResponse {
        private final int x;
        private final int y;
        private final String direction;
        private final Set<Point> visited;
        public ProbeStatusResponse(Probe probe) {
            this.x = probe.getX();
            this.y = probe.getY();
            this.direction = probe.getDirection().name();
            this.visited = probe.getVisited();
        }
        public int getX() { return x; }
        public int getY() { return y; }
        public String getDirection() { return direction; }
        public Set<Point> getVisited() { return visited; }
    }

    public static class Grid {
        private final int width;
        private final int height;
        private final Set<Point> obstacles = new HashSet<>();

        public Grid(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public boolean isWithinBounds(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        public void addObstacle(int x, int y) {
            obstacles.add(new Point(x, y));
        }

        public boolean isObstacle(int x, int y) {
            System.out.println("Checking obstacle at:"+ x + "," +y);
            System.out.println("Checking obstacle :"+obstacles.contains(new Point(x, y)));
            return obstacles.contains(new Point(x, y));
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
}
