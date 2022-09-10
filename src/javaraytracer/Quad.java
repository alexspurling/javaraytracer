package javaraytracer;

import java.awt.*;

public class Quad extends Object3D {

    private static final double PI_2 = Math.PI / 2;

    public final Vector3D p1;
    public final Vector3D p2;
    public final Vector3D p3;
    public final Vector3D p4;
    private final Vector3D normal;
    private final Color colour;
    private final Vector3D u;
    private final Vector3D v;

    private final double uP1;
    private final double uP2;
    private final double vP1;
    private final double vP4;

    public Quad(String name, Color colour, Vector3D p1, Vector3D p2, Vector3D p3, Vector3D p4) {
        super(name);
        this.colour = colour;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.normal = p2.subtract(p1).cross(p3.subtract(p1)).unit();
        this.u = p1.subtract(p2);
        this.v = p1.subtract(p4);
        this.uP1 = u.dot(p1);
        this.uP2 = u.dot(p2);
        this.vP1 = v.dot(p1);
        this.vP4 = v.dot(p3);
    }

    @Override
    void draw(Graphics g, Projector p) {
//        Vector2D p1p = p.project(p1);
//        Vector2D p2p = p.project(p2);
//        Vector2D p3p = p.project(p3);
//        Vector2D p4p = p.project(p4);

//        g.setColor(Color.MAGENTA.darker().darker());
//        g.fillPolygon(new Polygon(new int[]{(int) p1p.x(), (int) p2p.x(), (int) p3p.x(), (int) p4p.x()},
//                new int[]{(int) p1p.y(), (int) p2p.y(), (int) p3p.y(), (int) p4p.y()}, 4));

//        g.setColor(Color.RED);
//        g.drawLine((int) p1p.x(), (int) p1p.y(), (int) p2p.x(), (int) p2p.y());
//
//        g.setColor(Color.GREEN);
//        g.drawLine((int) p2p.x(), (int) p2p.y(), (int) p3p.x(), (int) p3p.y());

    }

    @Override
    void update(double dt) {

    }

    @Override
    Intersection getIntersection(Vector3D ray, Vector3D rayOrigin) {
        var Pv = rayOrigin;
        var Pp = p1;
        var Vv = ray;
        var Vp = normal;

        // Formula for intersection point on a plane:
        // https://math.stackexchange.com/a/3412215/785030
        double rayComponent = Vv.dot(Vp);

        // If ray is pointing in the same direction as the normal of the plane then it will never intersect
        if (rayComponent >= 0) {
            return null;
        }

        double rayComponent2 = Pp.subtract(Pv).dot(Vp);

        Vector3D inter = Vv.scale(rayComponent2 / rayComponent);
        Vector3D planeIntersection = Pv.add(inter);

        // Optimised by assuming ray origin is always 0, 0, 0
//        double scale = Pp.dot(Vp) / rayComponent;
//        Vector3D planeIntersection = new Vector3D(Vv.x() * scale, Vv.y() * scale, Vv.z() * scale);

        // Check if the intersection lies within the quad

        // Formula to check if point lies within x, y, z bounds:
        // https://math.stackexchange.com/a/1472080/785030

        double ux = u.dot(planeIntersection);
        double vx = v.dot(planeIntersection);

        if (ux > uP2 && ux < uP1 && vx > vP4 && vx < vP1) {
            return new Intersection(this, planeIntersection, normal);
        }
        return null;
    }


    @Override
    Color getColour() {
        return colour;
    }
}
