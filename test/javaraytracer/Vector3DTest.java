package javaraytracer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vector3DTest {

    private static final double delta = 1e-15;

    @Test
    public void testRotationZ() {
        var pos = new Vector3D(1, 0, 0);
        var rotated = pos.rotateZ(Math.PI / 2);

        assertEquals(0, rotated.x(), delta);
        assertEquals(1, rotated.y(), delta);
        assertEquals(0, rotated.z(), delta);
    }

    @Test
    public void testRotationZAlongAxis() {
        var pos = new Vector3D(0, 0, 3);
        var rotated = pos.rotateZ(Math.PI / 2);

        assertEquals(pos, rotated);
    }
}