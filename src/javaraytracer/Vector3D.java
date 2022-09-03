package javaraytracer;

import java.util.List;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public record Vector3D(double x, double y, double z) {

    public Vector3D add(Vector3D b) {
        return new Vector3D(x + b.x, y + b.y, z + b.z);
    }

    public Vector3D subtract(Vector3D b) {
        return new Vector3D(x - b.x, y - b.y, z - b.z);
    }

//    /**
//     * Returns a new vector representing the current vector rotated anti-clockwise by angle radians
//     * @param angle the angle to rotate in radians
//     * @return the new rotated vector
//     */
//    public Vector3D rotate(double angle) {
//        return new Vector3D(Math.cos(angle) * x - Math.sin(angle) * y, Math.sin(angle) * x + Math.cos(angle) * y);
//    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

//    public Vector3D scaleTo(double scale) {
//        double magnitude = magnitude();
//        return new Vector3D(scale * x / magnitude, scale * y / magnitude);
//    }

    public double dot(Vector3D b) {
        return x * b.x + y * b.y + z * b.z;
    }

    public Vector3D rotate(Vector3D to) {
        return null;
    }

    /* Rotate about the Z axis */
    public Vector3D rotateZ(double angle) {
        double c = cos(angle);
        double s = sin(angle);

        double x2 = c * x + -s * y;
        double y2 = s * x + c * y;
        double z2 = z;
        return new Vector3D(x2, y2, z2);
    }
}
