package javaraytracer;

import java.awt.*;
import java.util.Optional;

public class Cube extends Object3D {

    private Vector3D pos;
    private double cubeSize_2;
    private final double angularVelocity;
    private double angle = 0;
    private double totalTime = 0;

    public Cube(Vector3D pos, double size, double angularVelocity) {
        this.pos = pos;
        this.cubeSize_2 = size / 2;
        this.angularVelocity = angularVelocity;
    }

    @Override
    public void update(double dt) {
        angle += angularVelocity * dt;
        totalTime += dt;
//        pos = new Vector3D(Math.sin(totalTime * 0.001) * 50, pos.y(), Math.sin(totalTime * 0.002) * 15);
//        System.out.println(pos.x());
    }

    @Override
    Intersection getIntersection(Vector3D ray, Vector3D rayPos) {
        return null;
    }

    @Override
    Color getColour() {
        return null;
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
        g.drawLine((int) p1.x(), (int) p1.y(), (int) p2.x(), (int) p2.y());
    }

    public Vector3D getPos() {
        return pos;
    }

    public void setPos(Vector3D pos) {
        this.pos = pos;
    }
}
