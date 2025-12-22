package com.ocean.probe;

import com.ocean.probe.controller.ProbeController;
import com.ocean.probe.model.Direction;
import com.ocean.probe.model.Probe;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProbeControllerTest {
    @Test
    void testExecuteCommandsSequence() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(1, 1, Direction.NORTH, grid);
        CommandProbeController controller = new CommandProbeController(probe);
        controller.executeCommands("FFRFF");
        assertEquals(3, probe.getX());
        assertEquals(3, probe.getY());
        assertEquals(Direction.EAST, probe.getDirection());
    }

    @Test
    void testCommandsWithObstaclesAndBoundaries() {
        ProbeController.Grid grid = new ProbeController.Grid(3, 3);
        grid.addObstacle(1, 2);
        Probe probe = new Probe(1, 1, Direction.NORTH, grid);
        CommandProbeController controller = new CommandProbeController(probe);
        controller.executeCommands("FFF"); // Should stop at (1,1) -> (1,2) (obstacle)
        assertEquals(1, probe.getX());
        assertEquals(1, probe.getY());
        assertEquals(Direction.NORTH, probe.getDirection());
    }

    @Test
    void testInvalidCommandsAreIgnored() {
        ProbeController.Grid grid = new ProbeController.Grid(5, 5);
        Probe probe = new Probe(2, 2, Direction.NORTH, grid);
        CommandProbeController controller = new CommandProbeController(probe);
        controller.executeCommands("FXBZL"); // X and Z are invalid
        assertEquals(2, probe.getX());
        assertEquals(2, probe.getY()); // Fixed: should be 2, not 1
        assertEquals(Direction.WEST, probe.getDirection());
    }
}
