package javaraytracer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Cube extends Object3D {

    private final double angularVelocity;
    private final Color colour;

    private double cubeSize_2;
    // These base shapes don't change
    private final List<Quad> baseFaces;

    // Mutable state
    private Vector3D pos;
    private double angle = 0;

    // These faces change based on the current position and angle
    private List<Quad> faces;

    public Cube(String name, Color colour, Vector3D pos, double size, double angularVelocity) {
        super(name);

        this.colour = colour;
        this.pos = pos;
        this.cubeSize_2 = size / 2;
        this.angularVelocity = angularVelocity;

        var front = new Quad(
                "Front face", colour,
                new Vector3D(-cubeSize_2, -cubeSize_2, -cubeSize_2),
                new Vector3D(+cubeSize_2, -cubeSize_2, -cubeSize_2),
                new Vector3D(+cubeSize_2, -cubeSize_2, +cubeSize_2),
                new Vector3D(-cubeSize_2, -cubeSize_2, +cubeSize_2)
        );

        // left
        var left = new Quad(
                "Left face", colour,
                new Vector3D(-cubeSize_2, +cubeSize_2, -cubeSize_2),
                new Vector3D(-cubeSize_2, -cubeSize_2, -cubeSize_2),
                new Vector3D(-cubeSize_2, -cubeSize_2, +cubeSize_2),
                new Vector3D(-cubeSize_2, +cubeSize_2, +cubeSize_2)
        );

        // back
        var back = new Quad(
                "Back face", colour,
                new Vector3D(+cubeSize_2, +cubeSize_2, -cubeSize_2),
                new Vector3D(-cubeSize_2, +cubeSize_2, -cubeSize_2),
                new Vector3D(-cubeSize_2, +cubeSize_2, +cubeSize_2),
                new Vector3D(+cubeSize_2, +cubeSize_2, +cubeSize_2)
        );

        // right
        var right = new Quad(
                "Right face", colour,
                new Vector3D(+cubeSize_2, -cubeSize_2, -cubeSize_2),
                new Vector3D(+cubeSize_2, +cubeSize_2, -cubeSize_2),
                new Vector3D(+cubeSize_2, +cubeSize_2, +cubeSize_2),
                new Vector3D(+cubeSize_2, -cubeSize_2, +cubeSize_2)
        );

        // top
        var top = new Quad(
                "Top face", colour,
                new Vector3D(-cubeSize_2, -cubeSize_2, -cubeSize_2),
                new Vector3D(-cubeSize_2, +cubeSize_2, -cubeSize_2),
                new Vector3D(+cubeSize_2, +cubeSize_2, -cubeSize_2),
                new Vector3D(+cubeSize_2, -cubeSize_2, -cubeSize_2)
        );

        // bottom
        var bottom = new Quad(
                "Bottom face", colour,
                new Vector3D(-cubeSize_2, -cubeSize_2, +cubeSize_2),
                new Vector3D(+cubeSize_2, -cubeSize_2, +cubeSize_2),
                new Vector3D(+cubeSize_2, +cubeSize_2, +cubeSize_2),
                new Vector3D(-cubeSize_2, +cubeSize_2, +cubeSize_2)
        );

        baseFaces = List.of(front, left, back, right, top, bottom);
        updateFaces();
    }

    @Override
    public void update(double dt) {
        angle += angularVelocity * dt;
//        totalTime += dt;

        // Rotate all the faces
        updateFaces();

//        pos = new Vector3D(Math.sin(totalTime * 0.001) * 50, pos.y(), Math.sin(totalTime * 0.002) * 15);
//        System.out.println(pos.x());
    }

    private void updateFaces() {
        List<Quad> newFaces = new ArrayList<>();
        // Rotate the original faces and then translate their position
        for (Quad face : baseFaces) {
            newFaces.add(new Quad(face.getName(), face.getColour(),
                    face.p1.rotateZ(angle).add(pos),
                    face.p2.rotateZ(angle).add(pos),
                    face.p3.rotateZ(angle).add(pos),
                    face.p4.rotateZ(angle).add(pos)));
        }
        faces = newFaces;
    }

    @Override
    public void draw(Graphics g, Projector p) {

        Vector2D c1 = p.project(new Vector3D(-cubeSize_2, -cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c2 = p.project(new Vector3D(cubeSize_2, -cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c3 = p.project(new Vector3D(-cubeSize_2, -cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c4 = p.project(new Vector3D(cubeSize_2, -cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));

        Vector2D c5 = p.project(new Vector3D(-cubeSize_2, cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c6 = p.project(new Vector3D(cubeSize_2, cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c7 = p.project(new Vector3D(-cubeSize_2, cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c8 = p.project(new Vector3D(cubeSize_2, cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));

        g.setColor(Color.RED);
        drawLine(g, c1, c2);
        drawLine(g, c5, c6);
        drawLine(g, c1, c5);

        g.setColor(Color.GREEN);
        drawLine(g, c2, c4);
        drawLine(g, c6, c8);
        drawLine(g, c2, c6);

        g.setColor(Color.BLUE);
        drawLine(g, c4, c3);
        drawLine(g, c8, c7);
        drawLine(g, c4, c8);

        g.setColor(Color.WHITE);
        drawLine(g, c3, c1);
        drawLine(g, c7, c5);
        drawLine(g, c3, c7);
    }

    private void drawLine(Graphics g, Vector2D p1, Vector2D p2) {
//        g.drawLine((int) p1.x(), (int) p1.y(), (int) p2.x(), (int) p2.y());
    }

    @Override
    Intersection getIntersection(Vector3D ray, Vector3D rayOrigin) {

        // Calculate intersections for each object
        Intersection closestIntersection = null;
        double nearestIntersectionDistance = Double.MAX_VALUE;

        for (Quad face : faces) {
            Intersection intersection = face.getIntersection(ray, rayOrigin);
            if (intersection != null) {
                double intersectionDistance = intersection.point().subtract(rayOrigin).magnitude2();
                if (intersectionDistance < nearestIntersectionDistance) {
                    closestIntersection = intersection;
                    nearestIntersectionDistance = intersectionDistance;
                }
            }
        }
        if (closestIntersection != null) {
            return closestIntersection;
        }
//        for (Quad face : faces) {
//            Intersection intersection = face.getIntersection(ray, rayOrigin);
//            if (intersection != null) {
//                return intersection;
//            }
//        }
        return null;
    }

    @Override
    Color getColour() {
        return colour;
    }

    public Vector3D getPos() {
        return pos;
    }

    public void setPos(Vector3D pos) {
        this.pos = pos;
    }
}
