package com.ocean.probe;

import com.ocean.probe.controller.ProbeController;
import com.ocean.probe.model.Direction;
import com.ocean.probe.model.Point;
import com.ocean.probe.model.Probe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProbeTest {
    @Test
    void testMoveForwardWithinBounds() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(2, 2, Direction.NORTH, grid);
        assertTrue(probe.moveForward());
        assertEquals(2, probe.getX());
        assertEquals(3, probe.getY());
    }

    @Test
    void testMoveBackwardWithinBounds() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(2, 2, Direction.NORTH, grid);
        assertTrue(probe.moveBackward());
        assertEquals(2, probe.getX());
        assertEquals(1, probe.getY());
    }

    @Test
    void testTurnLeftAndRight() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(2, 2, Direction.NORTH, grid);
        probe.turnLeft();
        assertEquals(Direction.WEST, probe.getDirection());
        probe.turnRight();
        assertEquals(Direction.NORTH, probe.getDirection());
    }

    @Test
    void testObstacleAvoidance() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        grid.addObstacle(2, 3);
        Probe probe = new Probe(2, 2, Direction.NORTH, grid);
        assertFalse(probe.moveForward());
        assertEquals(2, probe.getX());
        assertEquals(2, probe.getY());
    }

    @Test
    void testBoundary() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(0, 0, Direction.SOUTH, grid);
        assertFalse(probe.moveForward());
        assertEquals(0, probe.getX());
        assertEquals(0, probe.getY());
    }

    @Test
    void testVisitedCoordinates() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(2, 2, Direction.NORTH, grid);
        probe.moveForward();
        probe.turnRight();
        probe.moveForward();
        assertTrue(probe.getVisited().contains(new Point(2,2)));
        assertTrue(probe.getVisited().contains(new Point(2,3)));
        assertTrue(probe.getVisited().contains(new Point(3,3)));
        assertEquals(3, probe.getVisited().size());
    }
}
