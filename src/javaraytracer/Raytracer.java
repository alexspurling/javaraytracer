package javaraytracer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Raytracer implements CanvasRenderer, MouseMotionListener, MouseListener, KeyListener {

    public static final int WIDTH = 640;
    public static final int WIDTH_2 = WIDTH / 2;
    public static final int HEIGHT = 480;
    public static final int HEIGHT_2 = HEIGHT / 2;

    private static final Vector3D RAY_ORIGIN = new Vector3D(0, 0, 0);

    private final BufferedImage img;
    private final int[] pixels;
    private final int[] pixelObjects;
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
        pixelObjects = new int[pixels.length];
        mousePos = new Vector2D(WIDTH / 2, HEIGHT / 2);
        objects = generateObjects();
        lights = generateLights();
    }

    private List<Object3D> generateObjects() {
        return List.of(
                new Quad("Background", new Color(0xffffff),
                        new Vector3D(-2000, 2000, 2000),
                        new Vector3D(-2000, -2000, 2000),
                        new Vector3D(2000, -2000, 2000),
                        new Vector3D(2000, 2000, 2000)),
                new Quad("Floor", new Color(0x77E1F1),
                        new Vector3D(-500, -300, 1600),
                        new Vector3D(-500, -300, 600),
                        new Vector3D(500, -300, 600),
                        new Vector3D(500, -300, 1600)),
                new Quad("Left wall", new Color(0x77E1F1),
                        new Vector3D(-500, -100, 1000),
                        new Vector3D(-500, -300, 1000),
                        new Vector3D(-500, -300, 1600),
                        new Vector3D(-500, -100, 1600)),
                new Quad("Right wall", new Color(0x77E1F1),
                        new Vector3D(500, -100, 1000),
                        new Vector3D(500, -100, 1600),
                        new Vector3D(500, -300, 1600),
                        new Vector3D(500, -300, 1000)),
                new Quad("Back wall", new Color(0x77E1F1),
                        new Vector3D(500, -100, 1600),
                        new Vector3D(-500, -100, 1600),
                        new Vector3D(-500, -300, 1600),
                        new Vector3D(500, -300, 1600)),
//                new Quad("Square", new Color(0xE0C49E),
//                        new Vector3D(50, -290, 850),
//                        new Vector3D(-50, -290, 850),
//                        new Vector3D(-50, -290, 750),
//                        new Vector3D(50, -290, 750)),
                new Sphere(new Color(0x5AA184), new Vector3D(100, -125, 1150), 75),
                new Cube("Cube 1", new Color(0xE0C49E), new Vector3D(-250, -200, 1100), 100, 0.001),
                new Cube("Cube 2", new Color(0xE0C49E), new Vector3D(250, -200, 1100), 100, 0.01)
        );
    }

    private List<Light> generateLights() {
        return List.of(new Light(new Vector3D(250, 140, 800)));
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
        render(g, dt, 4);
    }

    @Override
    public void render(Graphics g, double dt, int samplesPerPixel) {

        var g2 = img.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        updateKeys(dt);

        for (Object3D object : objects) {
            object.draw(g2, projector);
            object.update(dt);
        }

//        int x = 76 + WIDTH_2;
//        int y = -44 + HEIGHT_2;
//        for (int y = 0, i = 0; y < HEIGHT; y++) {
        IntStream.range(0, HEIGHT).parallel().forEach((y) -> {

            int[] pixelSamples = new int[3];

            for (int x = 0; x < WIDTH; x++) {

                double xScreenCoord = x - WIDTH_2;
                double yScreenCoord = HEIGHT_2 - y;

                int i = x + y * WIDTH;
                pixels[i] = projectRay(xScreenCoord, yScreenCoord);
            }
        });

        // Draw lights
        for (Light light : lights) {
            g2.setColor(Color.RED);
            Vector2D circlePos = projector.project(light.getPos());
            int radius = 10;
            drawCircle(g2, circlePos, radius);
        }
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

        double xScreenCoord = mousePos.x() - WIDTH_2;
        double yScreenCoord = HEIGHT_2 - mousePos.y();
        g2.drawString("Mouse:" + xScreenCoord + ", " + yScreenCoord, WIDTH - 200, 80);

//        Quad quad = (Quad) objects.get(0);
//        Vector3D pixelRay = new Vector3D((double) mousePos.x() - WIDTH_2, projector.getProjectionPlane(), (double) mousePos.y() - HEIGHT_2);
//        quad.getIntersection(pixelRay, rayOrigin);

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

    private int projectRay(double xScreenCoord, double yScreenCoord) {

        // Calculate vector from camera to pixel
        Vector3D pixelRay = new Vector3D(xScreenCoord, yScreenCoord, projector.getProjectionPlane());

        // Calculate intersections for each object
        Intersection closestIntersection = null;
        double nearestIntersectionDistance = Double.MAX_VALUE;
        Object3D intersectionObject = null;

        Vector3D unitRay = pixelRay.unit();

        for (Object3D object : objects) {
            Intersection intersection = object.getIntersection(unitRay, RAY_ORIGIN);
            if (intersection != null) {
                double intersectionDistance = intersection.point().subtract(RAY_ORIGIN).magnitude2();
                if (intersectionDistance < nearestIntersectionDistance) {
                    closestIntersection = intersection;
                    nearestIntersectionDistance = intersectionDistance;
                    intersectionObject = object;
                }
            }
        }
        if (closestIntersection != null) {
//            if (xScreenCoord == -25 && yScreenCoord == -160) {
//                System.out.println(calculateColour(lights.get(0), intersectionObject, closestIntersection));
//            }
            return calculateColour(lights.get(0), intersectionObject, closestIntersection);
        }
        return 0;
    }

    private int calculateColour(Light light, Object3D litObject, Intersection litPoint) {
        Vector3D fromLight = litPoint.point().subtract(light.getPos());
        double fromLightDistance = fromLight.magnitude();
        Vector3D fromLightUnit = fromLight.unit();
        Vector3D toLight = light.getPos().subtract(litPoint.point()).unit();
        // Because both vectors are unit vectors, we should receive a value between 0 and 1
        double amountLit = litPoint.normal().dot(toLight);
        if (amountLit < 0) {
            // we're facing away from the light so should be in shadow
            return 0;
        }

        // Check if there are any intersections with other objects that are blocking the light
        for (Object3D object : objects) {
            if (object != litObject) {
                Intersection intersection = object.getIntersection(fromLightUnit, light.getPos());
                // If there is an intersection from the light to the lit point AND that intersection
                // happens before the lit point then we must actually be in shadow
                if (intersection != null && intersection.point().subtract(light.getPos()).magnitude() < fromLightDistance) {
                    return 0;
                }
            }
        }

        Color objColour = litObject.getColour();
        int red = (int) (objColour.getRed() * amountLit);
        int green = (int) (objColour.getGreen() * amountLit);
        int blue = (int) (objColour.getBlue() * amountLit);
//        return new int[] {red, green, blue};
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
            Object3D lastCube = objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(0, 0, 0.1 * dt)));
        }
        if (keysPressed.contains(KeyEvent.VK_S)) {
            Object3D lastCube = objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(0, 0, -0.1 * dt)));
        }
        if (keysPressed.contains(KeyEvent.VK_A)) {
            Object3D lastCube = objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(-0.1 * dt, 0, 0)));
        }
        if (keysPressed.contains(KeyEvent.VK_D)) {
            Object3D lastCube = objects.get(objects.size() - 1);
            lastCube.setPos(lastCube.getPos().add(new Vector3D(0.1 * dt, 0, 0)));
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


    public static void main(String[] args) throws IOException {
        Raytracer raytracer = new Raytracer();

        RaytracerCanvas main = new RaytracerCanvas(raytracer, WIDTH, HEIGHT);

        main.addMouseMotionListener(raytracer);
        main.addMouseListener(raytracer);
        main.addKeyListener(raytracer);

        if (args.length > 0 && args[0].equals("record")) {

            double frameTime = (double) 1000 / 25; // 1 25th of a second
            int frameCount = (int) (1000 * 2 * Math.PI / frameTime) + 1;
            for (int frame = 0; frame < frameCount; frame++) {
                BufferedImage myImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = myImage.createGraphics();

                String fileName = String.format("frame%04d.png", frame);
                System.out.println("Rendering " + fileName);
                raytracer.render(g2, frameTime, 8);

                File outputfile = new File("output", fileName);
                ImageIO.write(myImage, "png", outputfile);
            }
            main.stop();
            System.out.println("Done");
        } else {
            main.start();
        }
    }

}
