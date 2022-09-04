package javaraytracer;

import java.util.List;
import java.util.stream.Collectors;

public class Projector {

    private final int width;
    private final int height;
    private double projectionPlane;

    public Projector(int width, int height, double projectionPlane) {
        this.projectionPlane = projectionPlane;
        this.width = width;
        this.height = height;
    }

    public List<Vector2D> project(List<Vector3D> points) {
        return points.stream().map(this::project).collect(Collectors.toList());
    }

    public Vector2D project(Vector3D pos) {
        double mapX = projectionPlane * pos.x() / pos.y();
        double mapY = projectionPlane * pos.z() / pos.y();

        int width_2 = width / 2;
        int height_2 = height / 2;

        return new Vector2D(width_2 + mapX, height_2 + mapY);
    }

    public double getProjectionPlane() {
        return projectionPlane;
    }

    public void setProjectionPlane(double projectionPlane) {
        this.projectionPlane = projectionPlane;
    }
}
