package javaraytracer;

import java.awt.*;
import java.util.Optional;

public abstract class Object3D {

    abstract void draw(Graphics g, Projector p);

    abstract void update(double dt);

    abstract Intersection getIntersection(Vector3D ray, Vector3D rayOrigin);

    abstract Color getColour();
}
