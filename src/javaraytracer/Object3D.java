package javaraytracer;

import java.awt.*;
import java.util.Optional;

public abstract class Object3D {

    private final String name;

    public Object3D(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract void draw(Graphics g, Projector p);

    abstract void update(double dt);

    abstract Intersection getIntersection(Vector3D ray, Vector3D rayOrigin);

    abstract Color getColour();
}
