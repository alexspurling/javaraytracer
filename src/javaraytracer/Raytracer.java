package javaraytracer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.cos;
import static java.lang.Math.tan;

public class Raytracer implements CanvasRenderer, MouseMotionListener, MouseListener, KeyListener {

    public static final int WIDTH = 640;
    public static final int WIDTH_2 = WIDTH / 2;
    public static final int HEIGHT = 512;
    public static final int HEIGHT_2 = HEIGHT / 2;

    private final BufferedImage img;
    private final int[] pixels;
    private final List<Object3D> objects;
    private final List<Light> lights;

    // Mouse position
    private Vector2D mousePos;

    // Pressed keys
    private final Set<Integer> keysPressed = new HashSet<>();

    private final Projector projector = new Projector(WIDTH, HEIGHT, 650);

    public Raytracer() {
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        mousePos = new Vector2D(WIDTH / 2, HEIGHT / 2);
        objects = generateObjects();
        lights = generateLights();
    }

    private List<Object3D> generateObjects() {
        return List.of(
                new Quad(new Vector3D(-500, 1000, 300),
                        new Vector3D(-500, 1600, 300),
                        new Vector3D(500, 1600, 300),
                        new Vector3D(500, 1000, 300)),
                new Quad(new Vector3D(-500, 1000, 300),
                        new Vector3D(-500, 1000, 100),
                        new Vector3D(-500, 1600, 100),
                        new Vector3D(-500, 1600, 300)),
                new Quad(new Vector3D(500, 1000, 300),
                        new Vector3D(500, 1600, 300),
                        new Vector3D(500, 1600, 100),
                        new Vector3D(500, 1000, 100)),
                new Quad(new Vector3D(-500, 1600, 300),
                        new Vector3D(-500, 1600, 100),
                        new Vector3D(500, 1600, 100),
                        new Vector3D(500, 1600, 300)),
                new Sphere(new Vector3D(75, 1000, 0), 100),
                new Cube(new Vector3D(-250, 1200, -120), 100, 1.0 / 1000)
        );
    }

    private List<Light> generateLights() {
        return List.of(new Light(new Vector3D(250, 800, -140)));
    }

    private int frameCount = 0;
    private int fps = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private long lastFrameTime = System.nanoTime();

    @Override
    public void render(Graphics g) {

        long curFrameTime = System.nanoTime();
        double dt = ((double)(curFrameTime - lastFrameTime)) / 1e6; // Convert nanoseconds to milliseconds
        lastFrameTime = curFrameTime;

        var g2 = img.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        updateKeys(dt);

        for (Object3D object : objects) {
            object.draw(g2, projector);
            object.update(dt);
        }

        Vector3D rayOrigin = new Vector3D(0, 0, 0);

//        int x = 76 + WIDTH_2;
//        int y = -44 + HEIGHT_2;
//        for (int y = 0, i = 0; y < HEIGHT; y++) {
        IntStream.range(0, HEIGHT).parallel().forEach((y) -> {
            for (int x = 0; x < WIDTH; x++) {

                // Calculate vector from camera to pixel
                Vector3D pixelRay = new Vector3D((double) x - WIDTH_2, projector.getProjectionPlane(), (double) y - HEIGHT_2);

                // Calculate intersections for each object
                Intersection closestIntersection = null;
                double nearestIntersectionDistance = Double.MAX_VALUE;

                for (Object3D object : objects) {
                    Intersection intersection = object.getIntersection(pixelRay, rayOrigin);
                    if (intersection != null) {
                        double intersectionDistance = intersection.point().subtract(rayOrigin).magnitude2();
                        if (intersectionDistance < nearestIntersectionDistance) {
                            closestIntersection = intersection;
                            nearestIntersectionDistance = intersectionDistance;
                        }
                    }
                }
//
                int i = x + y * WIDTH;
                if (closestIntersection != null) {
                    pixels[i] = calculateColour(lights.get(0), closestIntersection);
                }
            }
        });


//        g.setColor(Color.RED);
//        Vector2D circlePos = new Vector2D(300, 300);
//        int radius = 75;
//        drawCircle(g, circlePos, radius);
//
//        g.setColor(Color.YELLOW);
//        g.fillRect((int) (mousePos.x() - 1), (int) (mousePos.y() - 1), 2, 2);

//        List<Vector2D> tangents = getTangents(circlePos, radius);

//        for (Vector2D tangent : tangents) {
//            g.drawLine((int) mousePos.x(), (int) mousePos.y(), (int) tangent.x(), (int) tangent.y());
//        }

        frameCount++;
        long time = System.currentTimeMillis();
        if (time - lastFpsTime > 1000) {
            System.out.println("FPS: " + frameCount);
            fps = frameCount;
            frameCount = 0;
            lastFpsTime = time;
        }
        g2.setColor(Color.WHITE);
        g2.drawString("FPS: " + fps, WIDTH - 100, 40);
        g2.drawString("Plane:" + projector.getProjectionPlane(), WIDTH - 100, 60);

        Quad quad = (Quad) objects.get(0);
        Vector3D pixelRay = new Vector3D((double) mousePos.x() - WIDTH_2, projector.getProjectionPlane(), (double) mousePos.y() - HEIGHT_2);
        quad.getIntersection(pixelRay, rayOrigin);

//        g2.drawString("p2dot:" + quad.p2dot, WIDTH - 200, 80);
//        g2.drawString("p3dot:" + quad.p3dot, WIDTH - 200, 100);
//        g2.drawString("p4dot:" + quad.p4dot, WIDTH - 200, 120);

//        g2.drawString("ux:" + quad.ux, WIDTH - 200, 80);
//        g2.drawString("uP1:" + quad.uP1, WIDTH - 200, 100);
//        g2.drawString("uP2:" + quad.uP2, WIDTH - 200, 120);
//
//        g2.drawString("vx:" + quad.vx, WIDTH - 200, 140);
//        g2.drawString("vP1:" + quad.vP1, WIDTH - 200, 160);
//        g2.drawString("vP4:" + quad.vP4, WIDTH - 200, 180);

//        Cube lastCube = (Cube) objects.get(objects.size() - 1);
//        g.drawString("Cube pos:" + (int) lastCube.getPos().x() + ", " + (int) lastCube.getPos().z(), WIDTH - 100, 80);

        g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);

        g2.dispose();
        g.dispose();
    }

    private int calculateColour(Light light, Intersection litPoint) {
        Vector3D toLight = light.getPos().subtract(litPoint.point()).unit();
        // Because both vectors are unit vectors, we should receive a value between 0 and 1
        double amountLit = litPoint.normal().dot(toLight);
        if (amountLit < 0) {
            // we're facing away from the light so should be in shadow
            return 0;
        }

        // Check if there are any intersections with other objects that are blocking the light
        for (Object3D object : objects) {
            if (object != litPoint.object()) {
                Intersection intersection = object.getIntersection(toLight, litPoint.point());
                if (intersection != null) {
                    return 0;
                }
            }
        }

        Color objColour = litPoint.object().getColour();
        int red = (int) (objColour.getRed() * amountLit);
        int green = (int) (objColour.getGreen() * amountLit);
        int blue = (int) (objColour.getBlue() * amountLit);
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }

    private void drawVec(Graphics g, Vector2D pos, Vector2D vec, Color color) {
        g.setColor(color);
        g.drawLine((int) pos.x(), (int) pos.y(), (int) (pos.x() + vec.x()), (int) (pos.y() + vec.y()));
    }

    double degToRad(double a) { return a * Math.PI / 180.0; }

    private void drawUnitVec(Graphics g, Vector2D pos, Vector2D vec, Color color) {
        g.setColor(color);
        Vector2D scaledUnitVec = vec.scaleTo(40);
        drawVec(g, pos, scaledUnitVec, color);
        // Left arrow mark
        drawVec(g, pos.add(scaledUnitVec), vec.rotate(degToRad(145)).scaleTo(10), color);
        // Right arrow mark
        drawVec(g, pos.add(scaledUnitVec), vec.rotate(degToRad(215)).scaleTo(10), color);
    }

    private static void drawCircle(Graphics g, Vector2D pos, int radius) {
        g.drawOval((int) (pos.x() - radius), (int) (pos.y() - radius), radius * 2, radius * 2);
    }

    private static void fillCircle(Graphics g, Vector2D pos, int size) {
        int size_2 = size / 2;
        g.fillOval((int) (pos.x() - size_2), (int) (pos.y() - size_2), size, size);
    }

    private void updateKeys(double dt) {
        if (keysPressed.contains(KeyEvent.VK_UP)) {
            projector.setProjectionPlane(projector.getProjectionPlane() + 0.1 * dt);
        }
        if (keysPressed.contains(KeyEvent.VK_DOWN)) {
            projector.setProjectionPlane(projector.getProjectionPlane() - 0.1 * dt);
        }
        if (keysPressed.contains(KeyEvent.VK_W)) {
            Cube lastCube = (Cube) objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(0, 0, -0.01)));
        }
        if (keysPressed.contains(KeyEvent.VK_S)) {
            Cube lastCube = (Cube) objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(0, 0, 0.01)));
        }
        if (keysPressed.contains(KeyEvent.VK_A)) {
            Cube lastCube = (Cube) objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(-0.01, 0, 0)));
        }
        if (keysPressed.contains(KeyEvent.VK_D)) {
            Cube lastCube = (Cube) objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(0.01, 0, 0)));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePos = new Vector2D(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mousePos = new Vector2D(e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePos = new Vector2D(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePos = new Vector2D(e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keysPressed.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysPressed.remove(e.getKeyCode());
    }


    public static void main(String[] args) {
        Raytracer raytracer = new Raytracer();

        RaytracerCanvas main = new RaytracerCanvas(raytracer, WIDTH, HEIGHT);

        main.addMouseMotionListener(raytracer);
        main.addMouseListener(raytracer);
        main.addKeyListener(raytracer);

        main.start();
    }

}
