package javaraytracer;

public class Light {
    private final Vector3D pos;
    private Vector3D newPos;
    private double totalTime;

    public Light(Vector3D pos) {
        this.pos = pos;
        this.newPos = pos;
    }

    public Vector3D getPos() {
        return newPos;
    }

    public void update(double dt) {
        totalTime += dt;
        newPos = pos.add(new Vector3D(Math.sin(totalTime * 0.001) * 250, Math.cos(totalTime * 0.001) * 50, 0));
    }
}
