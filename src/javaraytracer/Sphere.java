package javaraytracer;

public class Sphere extends Object3D {

    private final Vector3D position;
    private final double radius;

    public Sphere(Vector3D position, double radius) {
        this.position = position;
        this.radius = radius;
    }
}
