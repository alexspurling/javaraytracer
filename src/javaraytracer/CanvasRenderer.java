package javaraytracer;

import java.awt.*;

public interface CanvasRenderer {

    void render(Graphics g);
    void render(Graphics g, double dt, int samplesPerPixel);
}
