import javax.swing.*;
import javax.swing.JFrame;

public class Main {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 1200;

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Stephanie's Retro Snake Game");
        frame.setSize(WIDTH, HEIGHT);
        final SnakeGame game = new SnakeGame(WIDTH,HEIGHT );
        frame.add(game);
        frame.setLocationRelativeTo(null); //puts window in center
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.pack();
        game.startGame();
    }
}