package javaraytracer;

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
}
