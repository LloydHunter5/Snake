package Snake;


import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    // Global parameters
    static final int width = 800;
    static final int height = 800;
    static final int gridSize = 16;
    public static void main(String[] args){
        JFrame f = new JFrame("Snake");
        f.setSize(width,height);
        Snake s = new Snake(width, height, gridSize);
        f.add(s); // Purely for input purposes
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);

        BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);

        Runnable snakeUpdate = new Runnable() {
            @Override
            public void run() {
                // Get the next frame
                s.updateSnake();
                s.paintToBufferedImage(bufferedImage);
                //Draw the new frame onto the JFrame
                f.getGraphics().drawImage(bufferedImage,0,0,width,height,null);
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // updates the JFrame at 5 frames/second; delays for .75 seconds to allow startup time
        executor.scheduleAtFixedRate(snakeUpdate,750,200, TimeUnit.MILLISECONDS);
    }
}

