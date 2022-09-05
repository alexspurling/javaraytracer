package javaraytracer;

import java.awt.*;

public class Sphere extends Object3D {

    private final Color colour;
    private final double invRadius;
    private final double radius;
    private final double radiusSq;
    private Vector3D position;
    private double totalTime = 0;

    public Sphere(Color colour, Vector3D position, double radius) {
        super("Sphere");
        this.colour = colour;
        this.position = position;
        this.radius = radius;
        this.radiusSq = radius * radius;
        this.invRadius = 1 / radius;
    }

    @Override
    void update(double dt) {
        totalTime += dt;
        position = new Vector3D(position.x(), position.y(), Math.sin(totalTime * 0.001) * 50);
    }

    @Override
    Intersection getIntersection(Vector3D ray, Vector3D rayOrigin) {
        Vector3D rayToCentre = position.subtract(rayOrigin);
        Vector3D unitRay = ray;
        double rayComponent = rayToCentre.dot(unitRay);
        // toCentre.magnitude() = Math.sqrt(rayComponent * rayComponent + rayDistanceFromCentre * rayDistanceFromCentre);
        // toCentre.magnitude() * toCentre.magnitude() = rayComponent * rayComponent + rayDistanceFromCentre * rayDistanceFromCentre;
        // rayDistanceFromCentre * rayDistanceFromCentre = toCentre.magnitude() * toCentre.magnitude() / rayComponent * rayComponent
//        double rayToCentreLength = rayToCentre.magnitude();
        double rayToCentreLength2 = rayToCentre.magnitude2();
        double distanceFromCentreSquared = (rayToCentreLength2) - (rayComponent * rayComponent);

        if (distanceFromCentreSquared > radiusSq) {
            return null;
        }

        double intersectionDistance = Math.sqrt(radiusSq - distanceFromCentreSquared);

//        Vector3D intersectionPoint = rayOrigin.add(unitRay.scaleTo(rayComponent - intersectionDistance));
        // Optimised by assuming rayOrigin is always 0, 0, 0
        Vector3D intersectionPoint = unitRay.scaleTo(rayComponent - intersectionDistance);
        Vector3D normal = intersectionPoint.subtract(position).scale(invRadius);

        return new Intersection(intersectionPoint, normal);
    }

    @Override
    Color getColour() {
        return colour;
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
