package javaraytracer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Raytracer implements CanvasRenderer, MouseMotionListener, MouseListener, KeyListener {

    public static final int WIDTH = 640;
    public static final int WIDTH_2 = WIDTH / 2;
    public static final int HEIGHT = 480;
    public static final int HEIGHT_2 = HEIGHT / 2;

    private static final Vector3D RAY_ORIGIN = new Vector3D(0, 0, 0);

    private final BufferedImage img;
    private final int[] imgPixels;
    private final Pixel[] pixels;
    private final List<Object3D> objects;
    private Light light;

    // Mouse position
    private Vector2D mousePos;
    private Vector3D mousePos3D;
    private Vector3D mouseNormal;
    private Vector3D mouseReflect;
    private Vector3D mouseToLight;

    // Pressed keys
    private final Set<Integer> keysPressed = new HashSet<>();

    private final Projector projector = new Projector(WIDTH, HEIGHT, 650);
    private boolean specularOn = true;

    public Raytracer() {
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        imgPixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        pixels = new Pixel[imgPixels.length];
        mousePos = new Vector2D(WIDTH / 2, HEIGHT / 2);
        objects = generateObjects();
        light = new Light(new Vector3D(0, 100, 800));
    }

    private List<Object3D> generateObjects() {
        return List.of(
                new Quad("Background", new Color(0x555555),
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
                new Sphere(new Color(0x5AA184), new Vector3D(100, -125, 1150), 75),
                new Cube("Cube 1", new Color(0xE0C49E), new Vector3D(-250, -200, 1100), 100, 0.001),
                new Cube("Cube 2", new Color(0xE0C49E), new Vector3D(250, -200, 1100), 100, 0.005)
        );
    }

    private int frameCount = 0;
    private int fps = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private long lastFrameTime = System.nanoTime();

    @Override
    public void render(Graphics g) {
        long curFrameTime = System.nanoTime();
        double dt = (double)(curFrameTime - lastFrameTime) / 1e6; // Convert nanoseconds to milliseconds
        lastFrameTime = curFrameTime;
        render(g, dt, false);
    }

    @Override
    public void render(Graphics g, double dt, boolean recordMode) {

        var g2 = img.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        updateKeys(dt);

        for (Object3D object : objects) {
            object.draw(g2, projector);
            object.update(dt);
        }

        this.light.update(dt);

        final Light light = this.light;

        IntStream.range(0, HEIGHT).parallel().forEach(y -> {

            for (int x = 0; x < WIDTH; x++) {

                double xScreenCoord = x - WIDTH_2;
                double yScreenCoord = HEIGHT_2 - y;

                int i = x + y * WIDTH;
                pixels[i] = projectRay(light, xScreenCoord, yScreenCoord);
                imgPixels[i] = pixels[i].colour;
            }
        });

        AtomicInteger numSubSampled = new AtomicInteger();

        IntStream.range(1, HEIGHT - 1).parallel().forEach(y -> {

            int numSubSampledThread = 0;

            for (int x = 1; x < WIDTH - 1; x++) {

                double xScreenCoord = x - WIDTH_2;
                double yScreenCoord = HEIGHT_2 - y;

                int i = x + y * WIDTH;

                Object3D thisPixelObject = pixels[i].object;
                if (thisPixelObject != pixels[i - 1].object ||
                        thisPixelObject != pixels[i + 1].object ||
                        thisPixelObject != pixels[i - WIDTH].object ||
                        thisPixelObject != pixels[i + WIDTH].object
                        ) {

                    // Supersample this pixel
                    Pixel[] samples = new Pixel[4];
                    samples[0] = projectRay(light, xScreenCoord - 0.25, yScreenCoord - 0.25);
                    samples[1] = projectRay(light, xScreenCoord + 0.25, yScreenCoord - 0.25);
                    samples[2] = projectRay(light, xScreenCoord - 0.25, yScreenCoord + 0.25);
                    samples[3] = projectRay(light, xScreenCoord + 0.25, yScreenCoord + 0.25);

                    imgPixels[i] = averageColour(samples);

                    numSubSampledThread++;
                }
            }

            numSubSampled.addAndGet(numSubSampledThread);
        });

        frameCount++;
        long time = System.currentTimeMillis();
        if (time - lastFpsTime > 1000) {
            System.out.println("FPS: " + frameCount);
            fps = frameCount;
            frameCount = 0;
            lastFpsTime = time;
        }

        if (!recordMode) {
            // Draw lights
            g2.setColor(Color.YELLOW);
            Vector2D circlePos = projector.project(light.getPos());
            int radius = 5;
            drawCircle(g2, circlePos, radius);

            g2.setColor(Color.WHITE);
            g2.drawString("FPS: " + fps, WIDTH - 100, 40);
            g2.drawString("Plane:" + projector.getProjectionPlane(), WIDTH - 100, 60);

            double xScreenCoord = mousePos.x() - WIDTH_2;
            double yScreenCoord = HEIGHT_2 - mousePos.y();
            g2.drawString("Mouse:" + xScreenCoord + ", " + yScreenCoord, WIDTH - 200, 80);

//            var percentage = numSubSampled.get() * 100 / (HEIGHT * WIDTH);
//            g2.drawString("Samples:" + numSubSampled.get() + " / " + HEIGHT * WIDTH + " (" + percentage + "%)", WIDTH - 200, 10);

            drawVec(g2, projector, mousePos3D, mouseNormal, Color.GREEN.darker());
            drawVec(g2, projector, mousePos3D, mouseReflect, Color.RED.darker());
            drawVec(g2, projector, mousePos3D, mouseToLight, Color.YELLOW.darker());

//            Quad quad = (Quad) objects.get(0);
//            Vector3D pixelRay = new Vector3D((double) mousePos.x() - WIDTH_2, projector.getProjectionPlane(), (double) mousePos.y() - HEIGHT_2);
//            quad.getIntersection(pixelRay, rayOrigin);
//
//            g2.drawString("p2dot:" + quad.p2dot, WIDTH - 200, 80);
//            g2.drawString("p3dot:" + quad.p3dot, WIDTH - 200, 100);
//            g2.drawString("p4dot:" + quad.p4dot, WIDTH - 200, 120);
//
//            g2.drawString("ux:" + quad.ux, WIDTH - 200, 80);
//            g2.drawString("uP1:" + quad.uP1, WIDTH - 200, 100);
//            g2.drawString("uP2:" + quad.uP2, WIDTH - 200, 120);
//
//            g2.drawString("vx:" + quad.vx, WIDTH - 200, 140);
//            g2.drawString("vP1:" + quad.vP1, WIDTH - 200, 160);
//            g2.drawString("vP4:" + quad.vP4, WIDTH - 200, 180);
//
//            Cube lastCube = (Cube) objects.get(objects.size() - 1);
//            g.drawString("Cube pos:" + (int) lastCube.getPos().x() + ", " + (int) lastCube.getPos().z(), WIDTH - 100, 80);

        }

        g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);

        g2.dispose();
        g.dispose();
    }

    private Pixel projectRay(Light light, double xScreenCoord, double yScreenCoord) {

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

            return calculateColour(xScreenCoord, yScreenCoord, light, intersectionObject, closestIntersection);
        }
        return new Pixel(0, null);
    }

    private Pixel calculateColour(double x, double y, Light light, Object3D litObject, Intersection litPoint) {
        Vector3D fromLight = litPoint.point().subtract(light.getPos());
        double fromLightDistance = fromLight.magnitude();
        Vector3D fromLightUnit = fromLight.unit();
        Vector3D toLight = light.getPos().subtract(litPoint.point()).unit();
        // Because both vectors are unit vectors, we should receive a value between 0 and 1
        double amountLit = litPoint.normal().dot(toLight);
        if (amountLit < 0) {
            // we're facing away from the light so should be in shadow
            return new Pixel(0, litPoint.object());
        }

        if (specularOn) {
            Vector3D reflected = litPoint.normal().scale(2 * litPoint.normal().dot(toLight)).subtract(toLight);
            Vector3D toCamera = new Vector3D(0, 0, 0).subtract(litPoint.point()).unit();
            double specularAmount = Math.pow(Math.max(toCamera.dot(reflected), 0), 32);
            double specularStrength = 0.5;

            amountLit = amountLit + specularAmount * specularStrength;

            double mouseScreenCoordX = mousePos.x() - WIDTH_2;
            double mouseScreenCoordY = HEIGHT_2 - mousePos.y();
            if (x == mouseScreenCoordX && y == mouseScreenCoordY) {
                mousePos3D = litPoint.point();
                mouseNormal = litPoint.normal();
                mouseReflect = reflected;
                mouseToLight = toLight;
            }
        }

        // Check if there are any intersections with other objects that are blocking the light
        for (Object3D object : objects) {
            if (object != litObject) {
                Intersection intersection = object.getIntersection(fromLightUnit, light.getPos());
                // If there is an intersection from the light to the lit point AND that intersection
                // happens before the lit point then we must actually be in shadow
                if (intersection != null && intersection.point().subtract(light.getPos()).magnitude() < fromLightDistance) {
                    return new Pixel(0, intersection.object());
                }
            }
        }

        Color objColour = litObject.getColour();
        int red = Math.min((int) (objColour.getRed() * amountLit), 255);
        int green = Math.min((int) (objColour.getGreen() * amountLit), 255);
        int blue = Math.min((int) (objColour.getBlue() * amountLit), 255);
//        return new int[] {red, green, blue};
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return new Pixel(rgb, litPoint.object());
    }

    private int averageColour(Pixel[] colours) {

        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;

        for (Pixel colour : colours) {
            Color c = new Color(colour.colour);
            totalRed += c.getRed();
            totalGreen += c.getGreen();
            totalBlue += c.getBlue();
        }

        int red = totalRed / colours.length;
        int green = totalGreen / colours.length;
        int blue = totalBlue / colours.length;
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }

    private void drawVec(Graphics g, Vector2D pos, Vector2D vec, Color color) {
        g.setColor(color);
        g.drawLine((int) pos.x(), (int) pos.y(), (int) (pos.x() + vec.x()), (int) (pos.y() + vec.y()));
    }

    double degToRad(double a) { return a * Math.PI / 180; }

    private void drawUnitVec(Graphics g, Vector2D pos, Vector2D vec, Color color) {
        g.setColor(color);
//        Vector2D scaledUnitVec = vec.scaleTo(40);
        drawVec(g, pos, vec, color);
        // Left arrow mark
        drawVec(g, pos.add(vec), vec.rotate(degToRad(145)).scaleTo(10), color);
        // Right arrow mark
        drawVec(g, pos.add(vec), vec.rotate(degToRad(215)).scaleTo(10), color);
    }

    private void drawVec(Graphics g, Projector projector, Vector3D pos, Vector3D vec, Color color) {
        if (pos != null && vec != null) {
            Vector2D projectedPos = projector.project(pos);
            Vector2D projectedVec = projector.project(pos.add(vec.scaleTo(100))).subtract(projectedPos);
            drawUnitVec(g, projectedPos, projectedVec, color);
        }
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
        if (keysPressed.contains(KeyEvent.VK_C)) {
            specularOn = !specularOn;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
//        System.out.println(e.getModifiersEx() + " left mouse: " + (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) + " right mouse: " + (e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK));
        if (SwingUtilities.isLeftMouseButton(e)) {
            double mouseScreenCoordX = e.getX() - WIDTH_2;
            double mouseScreenCoordY = HEIGHT_2 - e.getY();
            light = new Light(new Vector3D(mouseScreenCoordX, mouseScreenCoordY, 800));
        } else if (SwingUtilities.isRightMouseButton(e)) {
            mousePos = new Vector2D(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            mousePos = new Vector2D(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            mousePos = new Vector2D(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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

            double frameTime = (double) 1000 / 50; // 1 50th of a second
            int frameCount = (int) (1000 * 2 * Math.PI / frameTime) + 1;
            for (int frame = 0; frame < frameCount; frame++) {
                BufferedImage myImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = myImage.createGraphics();

                String fileName = String.format("frame%04d.png", frame);
                System.out.println("Rendering " + fileName);
                raytracer.render(g2, frameTime, true);

                File outputfile = new File("output", fileName);
                ImageIO.write(myImage, "png", outputfile);
            }
            main.stop();
            System.out.println("Done");
        } else {
            main.start();
        }
    }

    private record Pixel(int colour, Object3D object) {

    }

}
