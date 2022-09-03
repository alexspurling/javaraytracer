package javaraytracer;

import java.awt.*;

public class Cube {

    private Vector3D pos;
    private double cubeSize_2;
    private double angle = 0;

    public Cube(Vector3D pos, double size) {
        this.pos = pos;
        setSize(size);
    }

    public Vector3D getPos() {
        return pos;
    }

    public void setPos(Vector3D pos) {
        this.pos = pos;
    }

    public void setSize(double size) {
        this.cubeSize_2 = size / 2;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void draw(Graphics g, Projector projector) {

        Vector2D c1 = projector.project(new Vector3D(-cubeSize_2, -cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c2 = projector.project(new Vector3D(cubeSize_2, -cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c3 = projector.project(new Vector3D(-cubeSize_2, -cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c4 = projector.project(new Vector3D(cubeSize_2, -cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));

        Vector2D c5 = projector.project(new Vector3D(-cubeSize_2, cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c6 = projector.project(new Vector3D(cubeSize_2, cubeSize_2, -cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c7 = projector.project(new Vector3D(-cubeSize_2, cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));
        Vector2D c8 = projector.project(new Vector3D(cubeSize_2, cubeSize_2, cubeSize_2).rotateZ(angle).add(pos));

        g.setColor(Color.RED);
        drawLine(g, c1, c2);
        g.setColor(Color.GREEN);
        drawLine(g, c2, c4);
        g.setColor(Color.BLUE);
        drawLine(g, c4, c3);
        g.setColor(Color.WHITE);
        drawLine(g, c3, c1);

        g.setColor(Color.RED);
        drawLine(g, c5, c6);
        g.setColor(Color.GREEN);
        drawLine(g, c6, c8);
        g.setColor(Color.BLUE);
        drawLine(g, c8, c7);
        g.setColor(Color.WHITE);
        drawLine(g, c7, c5);

        g.setColor(Color.RED);
        drawLine(g, c1, c5);
        g.setColor(Color.GREEN);
        drawLine(g, c2, c6);
        g.setColor(Color.BLUE);
        drawLine(g, c4, c8);
        g.setColor(Color.WHITE);
        drawLine(g, c3, c7);
    }

    private void drawLine(Graphics g, Vector2D p1, Vector2D p2) {
        g.drawLine((int) p1.x(), (int) p1.y(), (int) p2.x(), (int) p2.y());
    }
}
