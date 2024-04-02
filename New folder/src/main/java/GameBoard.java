import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Phaser; // Import Phaser class

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GameBoard extends JPanel {
    private final int CELL_SIZE = 50;
    private final int NUM_ROWS = 10;
    private final int NUM_COLS = 10;
    private char[][] map;
    private int playerRow;
    private int playerCol;
    private int movesLeft;
    private Phaser phaser; // Add Phaser field

    public GameBoard(char[][] map, int playerRow, int playerCol) {
        this.map = map;
        this.playerRow = playerRow;
        this.playerCol = playerCol;
        this.movesLeft = calculateShortestPath();
        this.phaser = new Phaser(1); // Initialize Phaser with initial party count of 1

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        movePlayer(-1, 0);
                        break;
                    case KeyEvent.VK_DOWN:
                        movePlayer(1, 0);
                        break;
                    case KeyEvent.VK_LEFT:
                        movePlayer(0, -1);
                        break;
                    case KeyEvent.VK_RIGHT:
                        movePlayer(0, 1);
                        break;
                }
                repaint();
            }
        });
    }

    private void movePlayer(int rowOffset, int colOffset) {
        int newRow = playerRow + rowOffset;
        int newCol = playerCol + colOffset;
        if (isValidMove(newRow, newCol)) {
            playerRow = newRow;
            playerCol = newCol;
            movesLeft--;
            if (map[newRow][newCol] == 'F') {
                endGame(true);
                return;
            }
            if (movesLeft == 0) {
                endGame(false);
                return;
            }
        } else {
            endGame(false); // End game with loss if user hits a wall
            return;
        }
        phaser.arriveAndAwaitAdvance(); // Synchronize player movement
    }

    private boolean isValidMove(int newRow, int newCol) {
        return newRow >= 0 && newRow < NUM_ROWS && newCol >= 0 && newCol < NUM_COLS &&
                map[newRow][newCol] != '0'; // Check if the target cell is not a wall
    }

    private void endGame(boolean win) {
        if (win) {
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
        } else {
            JOptionPane.showMessageDialog(this, "Game over! You hit a wall or ran out of moves!");
        }
        // Reset game
        playerRow = findStartRow();
        playerCol = findStartCol();
        movesLeft = calculateShortestPath();
        phaser.arriveAndAwaitAdvance(); // Synchronize end of game
        repaint();
    }

    private int findStartRow() {
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (map[i][j] == 'S') {
                    return i;
                }
            }
        }
        return 0; // Default start row
    }

    private int findStartCol() {
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (map[i][j] == 'S') {
                    return j;
                }
            }
        }
        return 0; // Default start column
    }

    private int calculateShortestPath() {
        boolean[][] visited = new boolean[NUM_ROWS][NUM_COLS];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{playerRow, playerCol, 0});

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];
            int distance = current[2];

            if (map[row][col] == 'F') {
                return distance;
            }

            visited[row][col] = true;

            // Check all valid neighboring cells
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                    queue.add(new int[]{newRow, newCol, distance + 1});
                }
            }
        }

        return -1; // No path found
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int startX = (getWidth() - NUM_COLS * CELL_SIZE) / 2;
        int startY = (getHeight() - NUM_ROWS * CELL_SIZE) / 2;

        // Draw grid lines
        for (int row = 0; row <= NUM_ROWS; row++) {
            int y = startY + row * CELL_SIZE;
            g.drawLine(startX, y, startX + NUM_COLS * CELL_SIZE, y);
        }
        for (int col = 0; col <= NUM_COLS; col++) {
            int x = startX + col * CELL_SIZE;
            g.drawLine(x, startY, x, startY + NUM_ROWS * CELL_SIZE);
        }

        // Draw cells and objects
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                int x = startX + col * CELL_SIZE;
                int y = startY + row * CELL_SIZE;
                if (map[row][col] == '0') {
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                } else if (map[row][col] == 'S') {
                    g.setColor(Color.GREEN);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawString("S", x + CELL_SIZE / 2, y + CELL_SIZE / 2);
                } else if (map[row][col] == 'F') {
                    g.setColor(Color.RED);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawString("F", x + CELL_SIZE / 2, y + CELL_SIZE / 2);
                }
            }
        }

        // Draw player
        int playerX = startX + playerCol * CELL_SIZE;
        int playerY = startY + playerRow * CELL_SIZE;
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, CELL_SIZE, CELL_SIZE);

        // Draw moves left
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Moves Left: " + movesLeft, 20, 30);

        // Draw final movement if the game is won
        if (movesLeft == 0 || map[playerRow][playerCol] == 'F') {
            g.setColor(Color.YELLOW); // Set color for final movement
            g.fillOval(playerX, playerY, CELL_SIZE, CELL_SIZE);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    public static void main(String[] args) {
        char[][] map = {
                {'.', '.', '.', '.', '0', '.', '.', '.', '.', 'S'},
                {'.', '.', '.', '.', '0', '.', '.', '.', '.', '.'},
                {'0', '.', '.', '.', '.', '.', '0', '.', '.', '0'},
                {'.', '.', '.', '0', '.', '.', '.', '.', '0', '.'},
                {'.', 'F', '.', '.', '.', '.', '.', '.', '0', '.'},
                {'.', '0', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '0', '.'},
                {'0', '.', '0', '.', '0', '.', '.', '.', '0', '0'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.', '0'},
                {'.', '0', '0', '.', '.', '.', '.', '.', '.', '.'}
        };

        // Find start position
        int startRow = -1;
        int startCol = -1;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 'S') {
                    startRow = i;
                    startCol = j;
                    break;
                }
            }
        }

        JFrame frame = new JFrame("Sliding Puzzle Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GameBoard gameBoard = new GameBoard(map, startRow, startCol);
        frame.add(gameBoard);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gameBoard.requestFocusInWindow();
    }
}
