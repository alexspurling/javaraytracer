package javaraytracer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.Random;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Raytracer implements CanvasRenderer, MouseMotionListener, MouseListener {

    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;

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
    private int xPos;
    private int yPos;

    private final Projector projector = new Projector(WIDTH, HEIGHT, 300);
    private final Cube cube = new Cube(new Vector3D(0, 50, 0), 20);
    private double cubeAngle = 0;

    public Raytracer() {
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        r = new Random();

        xPos = WIDTH / 2;
        yPos = HEIGHT / 2;

        objects = generateObjects();
        player = new Vector3D(0, 0, 0);
        angle = 0;
    }

    private List<Object3D> generateObjects() {
        return List.of(
                new Sphere(new Vector3D(0, 50, 0), 10),
                new Quad(new Vector3D(-100, 0, -100), new Vector3D(100, 0, -100),
                        new Vector3D(-100, 200, -100), new Vector3D(100, 200, -100))
        );
    }

    private int frameCount = 0;
    private int fps = 0;

    private long lastTime = System.currentTimeMillis();

    double multiplier = 64;

    @Override
    public void render(Graphics g) {

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

        cube.draw(g, projector);
//        cube.setPos(cube.getPos().add(new Vector3D(0, 0.001, 0)));
        cubeAngle += 0.0001;
        cube.setAngle(cubeAngle);


        frameCount++;
        long time = System.currentTimeMillis();
        if (time - lastTime > 1000) {
            System.out.println("FPS: " + frameCount);
            fps = frameCount;
            frameCount = 0;
            lastTime = time;
        }
        String fpsString = "FPS: " + fps;
        g.setColor(Color.WHITE);
        g.drawString(fpsString, WIDTH - 100, 40);

        g.dispose();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        xPos = e.getX();
        yPos = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        xPos = e.getX();
        yPos = e.getY();

    }

    @Override
    public void mousePressed(MouseEvent e) {
        xPos = e.getX();
        yPos = e.getY();

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        xPos = e.getX();
        yPos = e.getY();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public static void main(String[] args) {
        Raytracer raytracer = new Raytracer();

        RaytracerCanvas main = new RaytracerCanvas(raytracer, WIDTH, HEIGHT);

        main.addMouseMotionListener(raytracer);
        main.addMouseListener(raytracer);

        main.start();
    }

}
