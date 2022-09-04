package javaraytracer;

import java.awt.*;
import java.util.Optional;

public class Quad extends Object3D {
    private final Vector3D p1;
    private final Vector3D p2;
    private final Vector3D p3;
    private final Vector3D p4;

    public Quad(Vector3D p1, Vector3D p2, Vector3D p3, Vector3D p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }

    @Override
    void draw(Graphics g, Projector p) {
        Vector2D p1p = p.project(p1);
        Vector2D p2p = p.project(p2);
        Vector2D p3p = p.project(p3);
        Vector2D p4p = p.project(p4);

        g.setColor(Color.MAGENTA.darker().darker());
        g.fillPolygon(new Polygon(new int[] {(int) p1p.x(), (int) p2p.x(), (int) p3p.x(), (int) p4p.x()},
                new int[] {(int) p1p.y(), (int) p2p.y(), (int) p3p.y(), (int) p4p.y()}, 4));
    }

    @Override
    void update(double dt) {

    }

    @Override
    Intersection getIntersection(Vector3D ray, Vector3D rayPos) {
        return null;
    }

    @Override
    Color getColour() {
        return null;
    }
}
