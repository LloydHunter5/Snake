package Snake;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;



public class Snake extends JPanel {
    //JPanel purely for input handling; rendering is done in Main with a JFrame
    int width, height; // JFrame size
    int distBetweenRects; // Size of each superpixel on the screen, subdivided based on grid size
    int rectSize; // Draw rectangles slightly smaller than the borders of enclosing superpixel for a segmented effect
    int gridSize; // How many superpixels (subdivides the screen based on height)
    Point currentAppleLocation;
    Direction snakeFacing; //Updated every time updateSnake() is called
    Direction bufferedSnakeFacing; //Updated every time there is an input
    LinkedList<Point> snake; // the snake's "head" is the head of the list (as one might expect)
    Point oldTail; // Stores previous tail location so that it can be removed next frame
    public class SnakeBoinkException extends RuntimeException{
        public SnakeBoinkException(String message){
            super(message);
        }
    }

    // Basic data structure to help with easy data storage and equality checking
    private class Point{
        int x;
        int y;
        public Point(int x, int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Point){
                return this.equals((Point) obj);
            }else{
                return false;
            }
        }

        public boolean equals(Point point){
            return point.x == this.x && point.y == this.y;
        }
    }

    public Snake(int w, int h, int gridSize){
        width = w;
        height = h;
        this.gridSize = gridSize;
        distBetweenRects = h/this.gridSize;
        rectSize = (int)(0.9*distBetweenRects);
        snakeFacing = Direction.RIGHT;
        bufferedSnakeFacing = snakeFacing;
        snake = new LinkedList<>();

        int middleOfGrid = gridSize/2;
        //Snake starting point, starts length 2
        snake.add(new Point(middleOfGrid,middleOfGrid));
        snake.add(new Point(middleOfGrid - 2,middleOfGrid));

        // Sets old tail up in the correct initial position
        oldTail = new Point(middleOfGrid - 3,middleOfGrid);

        //Starting apple not randomized
        currentAppleLocation = new Point(middleOfGrid + 3,middleOfGrid);

        //Set up keyboard events
        KeyListener arrows = new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()){
                    case VK_DOWN:  bufferedSnakeFacing = Direction.DOWN;  break;
                    case VK_UP:    bufferedSnakeFacing = Direction.UP;    break;
                    case VK_LEFT:  bufferedSnakeFacing = Direction.LEFT;  break;
                    case VK_RIGHT: bufferedSnakeFacing = Direction.RIGHT; break;
                }
            }

            // No need for these methods, but KeyListener requires them
            @Override
            public void keyReleased(KeyEvent e) {}
            @Override
            public void keyTyped(KeyEvent e) {}

        };

        addKeyListener(arrows);
        setDoubleBuffered(true);
        setFocusable(true);
        requestFocusInWindow();
    }
    public void paintToBufferedImage(BufferedImage b){
        Graphics g = b.getGraphics();
        drawSnake(g);
        drawApple(g);
    }

    //Draws the snake and updates the apple location if it has been eaten
    public void drawSnake(Graphics g){

        // Draws the snake
        for(Point p : snake){
            g.fillRect(p.x * distBetweenRects,p.y * distBetweenRects, rectSize, rectSize);
        }
        // Erases the old tail (unless an apple has been eaten)
        if(!snake.getFirst().equals(currentAppleLocation)) {
            g.clearRect(oldTail.x * distBetweenRects, oldTail.y * distBetweenRects, rectSize, rectSize);
        }else{
            //update apple location (if the point chosen is in the snake, pick a new one)
            while(snake.contains(currentAppleLocation)) {
                currentAppleLocation = new Point((int) (Math.random() * (gridSize - 1)), (int) (Math.random() * (gridSize - 2)) + 1);
            }
        }
    }

    public void drawApple(Graphics g){
        g.setColor(Color.GRAY);
        int x = currentAppleLocation.x * distBetweenRects;
        int y = currentAppleLocation.y * distBetweenRects;
        g.fillRect(x, y,rectSize,rectSize);
    }

    // Used to keep track of the snake's direction
    public enum Direction{
        UP{
            @Override
            public Direction opposite() {
                return DOWN;
            }
        },
        DOWN{
            @Override
            public Direction opposite() {
                return UP;
            }
        },
        LEFT{
            @Override
            public Direction opposite() {
                return RIGHT;
            }
        },
        RIGHT{
            @Override
            public Direction opposite() {
                return LEFT;
            }
        };

        public abstract Direction opposite();
    }

    public void updateSnake(){
        if(snakeFacing != bufferedSnakeFacing.opposite()){
            snakeFacing = bufferedSnakeFacing;
        }

        Point head = snake.getFirst();
        Point newHead = snake.removeLast();

        // Reusing the tail as the head saves a small (very small) amount of memory allocation
        // Absolutely not necessary but should make things slightly faster
        oldTail.x = newHead.x;
        oldTail.y = newHead.y;

        // new head is either left, right, up, or down one superpixel from the head
        newHead.x = head.x;
        newHead.y = head.y;
        switch (snakeFacing){
            case RIGHT:
                newHead.x++;
                break;
            case LEFT:
                newHead.x--;
                break;
            case UP:
                newHead.y--;
                break;
            case DOWN:
                newHead.y++;
                break;
        }
        snake.addFirst(newHead);

        //if the snake eats an apple, increase length by one
        if(newHead.equals(currentAppleLocation)) {
            snake.addLast(new Point(oldTail.x, oldTail.y));
        }

        //Check if snake is hitting itself
        boolean isFirstIteration = true; // Prevents checking the head against itself
        for(Point bodyPart : snake){
            if(isFirstIteration) {
                isFirstIteration = false;
                continue;
            }
            if(bodyPart.equals(newHead)){
                // game over
                throw new SnakeBoinkException("Snake bonked itself");
            }
        }

        // If the snake goes off the screen, game over
        if(newHead.x < 0 || newHead.x > gridSize-1 || newHead.y < 0 || newHead.y > gridSize-1){
            throw new SnakeBoinkException("Snake bonked into a wall!");
        }

    }
}
