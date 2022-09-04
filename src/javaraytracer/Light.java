package javaraytracer;

public class Light {
    private final Vector3D pos;

    public Light(Vector3D pos) {
        this.pos = pos;
    }

    public Vector3D getPos() {
        return pos;
    }
}
