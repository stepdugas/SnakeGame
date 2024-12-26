import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

//SnakeGame class has the core logic and graphical interface for the game
public class SnakeGame extends JPanel implements ActionListener {

    private final int width;
    private final int height;
    private final int cellSize;
    private final Random random = new Random();
    private static final int FRAME_RATE = 20;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private int highScore;
    private GamePoint food;
    private Direction direction = Direction.RIGHT;
    private Direction newDirection = Direction.RIGHT;
    private final List<GamePoint> snake = new ArrayList<>();

    //Constructor that initializes the game board
    public SnakeGame(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.cellSize = width / (FRAME_RATE * 2); //cell size is based on screen width & frame rate
        setPreferredSize(new Dimension(width, height)); //Setting preferred size of the JPanel
        setBackground(Color.BLUE); //Background color of the game board
    }

    //startGame is initializing the game by resetting & enabling keyboard input
    //startGame also starts the game loop
    public void startGame() {
        resetGameData(); //resets game for a new round
        setFocusable(true); //allows the panel focusable for capturing keyboard input
        setFocusTraversalKeysEnabled(false); //Needed to disable default focus keys
        requestFocusInWindow();

        //Adds a key listener to capture user inputs for controlling the snake
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                handleKeyEvent(e.getKeyCode()); //This handles when the user presses the arrow keys
            }
        });
        new Timer(1000 / FRAME_RATE, this).start(); //triggers the game loop
    }

    private void handleKeyEvent(final int keyCode) {
        //start the game with space bar
        if (!gameStarted) {
            if (keyCode == KeyEvent.VK_SPACE) {
                gameStarted = true; //starts the game
            }
        } else if (!gameOver) {
            //switch statement while the game is running
            //allow direction changes
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (direction != Direction.DOWN) {
                        newDirection = Direction.UP;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != Direction.UP) {
                        newDirection = Direction.DOWN;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != Direction.LEFT) {
                        newDirection = Direction.RIGHT;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (direction != Direction.RIGHT) {
                        newDirection = Direction.LEFT;
                    }
                    break;
            }
        } else if (keyCode == KeyEvent.VK_SPACE) {
            gameStarted = false;
            gameOver = false;
            resetGameData(); //restarts game for a new round
        }
    }

    private void resetGameData() {
        snake.clear(); //resets snakes body
        snake.add(new GamePoint(width / 2, height / 2)); //adds the start of snake
        generateFood();
    }

    //generates random position for snake food
    private void generateFood() {
        do {
            //randomly generates coordinates for x & y
            //based on grid size
            food = new GamePoint(random.nextInt(width / cellSize) * cellSize,
                    random.nextInt(height / cellSize) * cellSize);
        } while (snake.contains(food)); //this avoids having food spawn on top of snake
    }

    //paintComponent is called every time the game state changes to update visual representation
    @Override
    protected void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);

        if (!gameStarted) {
            printMessage(graphics, "Start Game by Hitting Space Bar"); //display msg to user
        } else {
            //food on game board
            graphics.setColor(Color.WHITE);
            graphics.fillRect(food.x, food.y, cellSize, cellSize);

            //change the color of the snake slightly
            Color snakeColor = Color.GREEN;
            for (final var point : snake) {
                graphics.setColor(snakeColor);
                graphics.fillRect(point.x, point.y, cellSize, cellSize);
                final int newGreen = (int) Math.round(snakeColor.getGreen() * (0.95)); //darken snake color slowly
                snakeColor = new Color(0, newGreen, 0);
            }

            //display score
            if (gameOver) {
                final int currentScore = snake.size();
                if (currentScore > highScore) {
                    highScore = currentScore;
                }
                printMessage(graphics, "Your Current Score: " + currentScore
                        + "\nYour High Score: " + highScore
                        + "\nPress Space Bar to Reset");
            }
        }
    }

    //message onto screen
    private void printMessage(final Graphics graphics, final String message) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(graphics.getFont().deriveFont(40F)); //font size
        int currentHeight = height / 3; //start message at 1/3 of screen
        final var graphics2D = (Graphics2D) graphics;
        final var frc = graphics2D.getFontRenderContext(); //font rendering for the text layout
        for (final var line : message.split("\n")) {
            final var layout = new TextLayout(line, graphics.getFont(), frc);
            final var bounds = layout.getBounds();
            final var targetWidth = (float) (width - bounds.getWidth()) / 2; //center text horizontally
            layout.draw(graphics2D, targetWidth, currentHeight);
            currentHeight += graphics.getFontMetrics().getHeight();
        }
    }

    //updates the snakes position
    private void move() {
        direction = newDirection; //updates the snakes direction

        final GamePoint head = snake.getFirst();
        //takes current head of snake and
        //then creates new head based on direction of movement
        final GamePoint newHead = switch (direction) {
            case UP -> new GamePoint(head.x, head.y - cellSize);
            case DOWN -> new GamePoint(head.x, head.y + cellSize);
            case LEFT -> new GamePoint(head.x - cellSize, head.y);
            case RIGHT -> new GamePoint(head.x + cellSize, head.y);
        };
        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            generateFood(); //if snake eats food, then generate new food
        } else if (isCollision()) {
            gameOver = true; //if a collision occurs, then its game over
            snake.removeFirst();
        } else {
            snake.removeLast();
        }
    }

    //checks if snake collides with a wall and/or itself
    private boolean isCollision() {
        final GamePoint head = snake.getFirst();
        final var invalidWidth = (head.x < 0) || (head.x >= width);
        final var invalidHeight = (head.y < 0) || (head.y >= height);
        if (invalidWidth || invalidHeight) {
            return true; //if collision with a boundary
        }

        //checking for collision with self
        return snake.size() != new HashSet<>(snake).size();
    }

    //called based on time at each frame to update game state
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (gameStarted && !gameOver) {
            move();
        }
        repaint();
    }

    private record GamePoint(int x, int y) {
    }

    private enum Direction {
        UP, DOWN, RIGHT, LEFT
    }
}