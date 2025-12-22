package com.ocean.probe.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.util.Objects;

@Entity
public class ProbeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int x;
    private int y;
    @Enumerated(EnumType.STRING)
    private Direction direction;

    public ProbeEntity() {}

    public ProbeEntity(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public Long getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(Direction direction) { this.direction = direction; }

    @Override
    public boolean equals(Object o) {
        System.out.println("Object " + o.getClass().getName());
        if (this == o) return true;
        if (!(o instanceof ProbeEntity)) return false;
        ProbeEntity that = (ProbeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
