package javaraytracer;

import java.awt.*;
import java.util.List;

public class Sphere extends Object3D {

    private Vector3D position;
    private final double radius;
    private double totalTime = 0;

    public Sphere(Vector3D position, double radius) {
        this.position = position;
        this.radius = radius;
    }

    @Override
    void update(double dt) {
        totalTime += dt;
        position = new Vector3D(Math.sin(totalTime * 0.001) * 500, position.y(), Math.sin(totalTime * 0.002) * 200);
    }

    @Override
    void draw(Graphics g, Projector p) {
        // Find intersection of vector from camera origin to sphere centre with image plane

//        Vector3D toPlane = getToPlane(position);
//
//        List<Vector2D> xTangents = getTangents(new Vector2D(position.x(), position.y()), radius, new Vector2D(0, 0));
//        List<Vector2D> yTangents = getTangents(new Vector2D(position.z(), position.y()), radius, new Vector2D(0, 0));
//
//        List<Vector3D> xTangentPlaneIntersect = getToPlane()

        Vector2D left = p.project(position.add(new Vector3D(-radius, 0, 0)));
        Vector2D right = p.project(position.add(new Vector3D(radius, 0, 0)));
        Vector2D top = p.project(position.add(new Vector3D(0, 0, -radius)));
        Vector2D bottom = p.project(position.add(new Vector3D(0, 0, radius)));

        g.setColor(Color.ORANGE);
        g.drawOval((int) left.x(), (int) top.y(), (int) (right.x() - left.x()), (int) (bottom.y() - top.y()));
    }

    private java.util.List<Vector2D> getTangents(Vector2D circlePos, double radius, Vector2D point) {
        Vector2D p = point.subtract(circlePos);

        double distanceToCircle = p.magnitude();

        if (distanceToCircle <= radius) {
            return java.util.List.of();
        }

        double a = radius * radius / distanceToCircle;
        double q = radius * Math.sqrt((distanceToCircle * distanceToCircle) - (radius * radius)) / distanceToCircle;

        Vector2D pN = p.scale(1 / distanceToCircle);

        Vector2D pNP = new Vector2D(-pN.y(), pN.x());

        Vector2D va = pN.scale(a);

        Vector2D tangentA = circlePos.add(va.add(pNP.scale(q)));
        Vector2D tangentB = circlePos.add(va.subtract(pNP.scale(q)));

        return List.of(tangentA, tangentB);
    }
}
