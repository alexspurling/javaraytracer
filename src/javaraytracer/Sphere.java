package javaraytracer;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class Sphere extends Object3D {

    private Vector3D position;
    private final double radius;
    private final double radiusSq;
    private double totalTime = 0;

    public Sphere(Vector3D position, double radius) {
        this.position = position;
        this.radius = radius;
        this.radiusSq = radius * radius;
    }

    @Override
    void update(double dt) {
        totalTime += dt;
        position = new Vector3D(Math.sin(totalTime * 0.001) * 500, position.y(), Math.sin(totalTime * 0.002) * 200);
    }

    @Override
    Intersection getIntersection(Vector3D ray, Vector3D rayPos) {
        Vector3D rayToCentre = position.subtract(rayPos);
        Vector3D unitRay = ray.unit();
        double rayComponent = rayToCentre.dot(unitRay);
        // toCentre.magnitude() = Math.sqrt(rayComponent * rayComponent + rayDistanceFromCentre * rayDistanceFromCentre);
        // toCentre.magnitude() * toCentre.magnitude() = rayComponent * rayComponent + rayDistanceFromCentre * rayDistanceFromCentre;
        // rayDistanceFromCentre * rayDistanceFromCentre = toCentre.magnitude() * toCentre.magnitude() / rayComponent * rayComponent
        double distanceFromCentreSquared = (rayToCentre.magnitude() * rayToCentre.magnitude()) - (rayComponent * rayComponent);
//        double rayDistanceFromCentre = Math.sqrt(distanceFromCentreSquared);

        if (distanceFromCentreSquared > radiusSq) {
            return null;
        }

        double intersectionDistance = Math.sqrt(radiusSq - distanceFromCentreSquared);

        Vector3D intersectionPoint = rayPos.add(unitRay.scaleTo(rayComponent - intersectionDistance));
        Vector3D normal = intersectionPoint.subtract(position).scaleTo(1);

        return new Intersection(this, intersectionPoint, normal);
    }

    @Override
    Color getColour() {
        return Color.YELLOW;
    }

    @Override
    void draw(Graphics g, Projector p) {
//        Vector2D left = p.project(position.add(new Vector3D(-radius, 0, 0)));
//        Vector2D right = p.project(position.add(new Vector3D(radius, 0, 0)));
//        Vector2D top = p.project(position.add(new Vector3D(0, 0, -radius)));
//        Vector2D bottom = p.project(position.add(new Vector3D(0, 0, radius)));
//
//        g.setColor(Color.ORANGE);
//        g.drawOval((int) left.x(), (int) top.y(), (int) (right.x() - left.x()), (int) (bottom.y() - top.y()));
    }
}
