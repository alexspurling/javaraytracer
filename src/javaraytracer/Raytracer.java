package javaraytracer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.*;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.tan;

public class Raytracer implements CanvasRenderer, MouseMotionListener, MouseListener, KeyListener {

    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    public static final int BRUSH_SIZE = 20;

    private final double FOV = (double) 100 / 180 * Math.PI;
    private final double VIEW_PORT_DISTANCE = 20;
    private final double VIEW_PORT_WIDTH = cos(FOV / 2) * VIEW_PORT_DISTANCE * 2;
    private final double VIEW_PORT_HEIGHT = cos(FOV / 2) * VIEW_PORT_DISTANCE * 2;


    private final BufferedImage img;
    private final int[] pixels;
    private final Random r;
    private final List<Object3D> objects;

    private final Vector3D player;
    private final double angle;

    // Mouse position
    private Vector2D mousePos;

    // Pressed keys
    private final Set<Integer> keysPressed = new HashSet<>();

    private final Projector projector = new Projector(WIDTH, HEIGHT, 1000);

    public Raytracer() {
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        r = new Random();

        mousePos = new Vector2D(WIDTH / 2, HEIGHT / 2);

        objects = generateObjects();
        player = new Vector3D(0, 0, 0);
        angle = 0;
    }

    private List<Object3D> generateObjects() {
        return List.of(
                new Sphere(new Vector3D(75, 1200, 0), 100),
                new Quad(new Vector3D(-100, 0, -100), new Vector3D(100, 0, -100),
                        new Vector3D(-100, 200, -100), new Vector3D(100, 200, -100)),
                new Cube(new Vector3D(-500, 1200, -250), 100, 1.0 / 1000),
                new Cube(new Vector3D(-250, 1200, -250), 100, 1.0 / 1000),
                new Cube(new Vector3D(0, 1200, -250), 100, 1.0 / 1000),
                new Cube(new Vector3D(250, 1200, -250), 100, 1.0 / 1000),
                new Cube(new Vector3D(500, 1200, -250), 100, 1.0 / 1000),
                new Cube(new Vector3D(-500, 1200, 0), 100, 1.0 / 1000),
                new Cube(new Vector3D(-250, 1200, 0), 100, 1.0 / 1000),
                new Cube(new Vector3D(0, 1200, 0), 100, 1.0 / 1000),
                new Cube(new Vector3D(250, 1200, 0), 100, 1.0 / 1000),
                new Cube(new Vector3D(500, 1200, 0), 100, 1.0 / 1000),
                new Cube(new Vector3D(-250, 1200, 250), 100, 1.0 / 1000),
                new Cube(new Vector3D(-500, 1200, 250), 100, 1.0 / 1000),
                new Cube(new Vector3D(0, 1200, 250), 100, 1.0 / 1000),
                new Cube(new Vector3D(250, 1200, 250), 100, 1.0 / 1000),
                new Cube(new Vector3D(500, 1200, 250), 100, 1.0 / 1000)
        );
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

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
//        multiplier += 0.001;
//        if (multiplier > 64) {
//            multiplier = 0;
//        }

//        for (int y = 0; y < HEIGHT; y++) {
//            for (int x = 0; x < WIDTH; x++) {
//                // Calculate vector from camera to pixel
//                Vector3D viewPortPoint = new Vector3D((double) x / WIDTH * VIEW_PORT_WIDTH,  VIEW_PORT_DISTANCE, (double) y / HEIGHT * VIEW_PORT_HEIGHT)
//                        .rotateZ(angle)
//                        .add(player);
//                Vector3D ray = viewPortPoint.subtract(player);
//
//                int i = x + y * WIDTH;
//                pixels[i] = (int)(x * y * multiplier); // r.nextInt(0xffffff);
//            }
//        }
//        g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);

        updateKeys(dt);


        for (Object3D object : objects) {
            object.draw(g, projector);
            object.update(dt);
        }

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
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + fps, WIDTH - 100, 40);
        g.drawString("Plane:" + projector.getProjectionPlane(), WIDTH - 100, 60);

        g.dispose();
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
//        if (!keysPressed.isEmpty()) {
//            System.out.println("Keys pressed: " + keysPressed);
//        }
        if (keysPressed.contains(KeyEvent.VK_UP)) {
            projector.setProjectionPlane(projector.getProjectionPlane() + 0.01);
        }
        if (keysPressed.contains(KeyEvent.VK_DOWN)) {
            projector.setProjectionPlane(projector.getProjectionPlane() - 0.01);
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
