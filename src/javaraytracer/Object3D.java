package javaraytracer;

import java.awt.*;

public abstract class Object3D {

    abstract void draw(Graphics g, Projector p);

    abstract void update(double dt);
}
