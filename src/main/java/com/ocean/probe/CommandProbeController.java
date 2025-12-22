package com.ocean.probe;

import com.ocean.probe.model.Probe;

public class CommandProbeController {
    private final Probe probe;

    public CommandProbeController(Probe probe) {
        this.probe = probe;
    }

    public void executeCommands(String commands) {
        for (char command : commands.toCharArray()) {
            switch (command) {
                case 'F': probe.moveForward(); break;
                case 'B': probe.moveBackward(); break;
                case 'L': probe.turnLeft(); break;
                case 'R': probe.turnRight(); break;
                default: // Ignore invalid
            }
        }
    }

    public Probe getProbe() {
        return probe;
    }
}

