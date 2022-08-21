package javaraycaster;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class RaycasterCanvas extends Canvas implements Runnable {

    private final CanvasRenderer renderer;

    private Thread thread;

    private boolean isRunning = false;

    public RaycasterCanvas(CanvasRenderer renderer, int width, int height) {

        this.renderer = renderer;

        JFrame frame = new JFrame();
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        while (isRunning) {
            BufferStrategy bs = this.getBufferStrategy();
            if (bs == null) {
                createBufferStrategy(2);
                continue;
            }
            Graphics g = bs.getDrawGraphics();
            if (g != null) {
                renderer.render(g);
                bs.show();
            }
        }
    }

    public void start() {
        if (isRunning)
            return;
        isRunning = true;

        thread = new Thread(this);
        thread.start();
    }

    private void stop() {
        if (!isRunning)
            return;
        isRunning = false;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
