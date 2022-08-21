package javaraycaster;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

public class Raycaster implements CanvasRenderer, MouseMotionListener, MouseListener {

    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;

    public static final int BRUSH_SIZE = 20;

    private final BufferedImage img;
    private final int[] pixels;
    private final Random r;

    // Mouse position
    private int xPos;
    private int yPos;

    public Raycaster() {
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        r = new Random();

        xPos = WIDTH / 2;
        yPos = HEIGHT / 2;
    }

    private int frameCount = 0;
    private int fps = 0;

    private long lastTime = System.currentTimeMillis();

    @Override
    public void render(Graphics g) {

        int xstart = xPos - BRUSH_SIZE;
        int ystart = yPos - BRUSH_SIZE;
        int xend = xPos + BRUSH_SIZE;
        int yend = yPos + BRUSH_SIZE;

        if (xstart < 0) xstart = 0;
        if (xstart > WIDTH) xstart = WIDTH;
        if (ystart < 0) ystart = 0;
        if (ystart > HEIGHT) ystart = HEIGHT;
        if (xend < 0) xend = 0;
        if (xend > WIDTH) xend = WIDTH;
        if (yend < 0) yend = 0;
        if (yend > HEIGHT) yend = HEIGHT;

        for (int i = 0; i < WIDTH * HEIGHT; i++) {
            pixels[i] *= 0.99;
            if (pixels[i] <= 0)
                pixels[i] = 0;
        }

        for (int y = ystart; y < yend; y++) {
            for (int x = xstart; x < xend; x++) {
                int i = x + y * WIDTH;
                pixels[i] = r.nextInt(0xffffff);
            }
        }

        g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);

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
        Raycaster raycaster = new Raycaster();

        RaycasterCanvas main = new RaycasterCanvas(raycaster, WIDTH, HEIGHT);

        main.addMouseMotionListener(raycaster);
        main.addMouseListener(raycaster);

        main.start();
    }
}
