package com.ocean.probe.model;

import com.ocean.probe.controller.ProbeController;

import java.util.HashSet;
import java.util.Set;

public class Probe {
    private int x;
    private int y;
    private Direction direction;
    private final ProbeController.Grid grid;
    private final Set<Point> visited = new HashSet<>();

    public Probe(int x, int y, Direction direction, ProbeController.Grid grid) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.grid = grid;
        visited.add(new Point(x, y));
    }

    public boolean moveForward() {
        return move(1);
    }

    public boolean moveBackward() {
        return move(-1);
    }

    private boolean move(int step) {
        int newX = x, newY = y;
        switch (direction) {
            case NORTH: newY += step; break;
            case SOUTH: newY -= step; break;
            case EAST: newX += step; break;
            case WEST: newX -= step; break;
        }
        if (grid.isWithinBounds(newX, newY) && !grid.isObstacle(newX, newY)) {
            x = newX;
            y = newY;
            visited.add(new Point(x, y));
            return true;
        }
        return false;
    }

    public void turnLeft() {
        direction = direction.turnLeft();
    }

    public void turnRight() {
        direction = direction.turnRight();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    public Set<Point> getVisited() { return new HashSet<>(visited); }
}
