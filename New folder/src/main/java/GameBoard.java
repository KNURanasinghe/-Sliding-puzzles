import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

public class GameBoard extends JPanel implements KeyListener {
    private final int CELL_SIZE = 50;
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
        addKeyListener(this);
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
        return newRow >= 0 && newRow < map.length && newCol >= 0 && newCol < map[0].length &&
                map[newRow][newCol] != '0'; // Check if the target cell is not a wall
    }

    private void endGame(boolean win) {
        String[] options = {"Play Again", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(this,
                win ? "Congratulations! You won!" : "Game over! You hit a wall or ran out of moves!",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) { // Play Again
            // Reset game
            playerRow = findStartRow();
            playerCol = findStartCol();
            movesLeft = calculateShortestPath();
            phaser.arriveAndAwaitAdvance(); // Synchronize end of game
            repaint();
        } else { // Main Menu
            // Close current game window
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.dispose();

            // Show game type selection menu again
            showGameTypeSelectionMenu();
        }
    }

    private int findStartRow() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 'S') {
                    return i;
                }
            }
        }
        return 0; // Default start row
    }

    private int findStartCol() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 'S') {
                    return j;
                }
            }
        }
        return 0; // Default start column
    }

    private int calculateShortestPath() {
        boolean[][] visited = new boolean[map.length][map[0].length];
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
    
        // Calculate cell size based on JFrame dimensions
        int cellSize = Math.min(getWidth() / map[0].length, getHeight() / map.length);
    
        int startX = (getWidth() - map[0].length * cellSize) / 2;
        int startY = (getHeight() - map.length * cellSize) / 2;
    
        // Draw grid lines
        for (int row = 0; row <= map.length; row++) {
            int y = startY + row * cellSize;
            g.drawLine(startX, y, startX + map[0].length * cellSize, y);
        }
        for (int col = 0; col <= map[0].length; col++) {
            int x = startX + col * cellSize;
            g.drawLine(x, startY, x, startY + map.length * cellSize);
        }
    
        // Draw cells and objects
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                int x = startX + col * cellSize;
                int y = startY + row * cellSize;
                if (map[row][col] == '0') {
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, cellSize, cellSize);
                } else if (map[row][col] == 'S') {
                    g.setColor(Color.GREEN);
                    g.fillRect(x, y, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                    g.drawString("S", x + cellSize / 2, y + cellSize / 2);
                } else if (map[row][col] == 'F') {
                    g.setColor(Color.RED);
                    g.fillRect(x, y, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                    g.drawString("F", x + cellSize / 2, y + cellSize / 2);
                }
            }
        }
    
        // Draw player
        int playerX = startX + playerCol * cellSize;
        int playerY = startY + playerRow * cellSize;
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, cellSize, cellSize);
    
        // Calculate position for "Moves Left" text based on frame size
        int textX = getWidth() / 20; // Adjust this value to change the horizontal position
        int textY = getHeight() / 18; // Adjust this value to change the vertical position
    
        // Draw moves left with dynamic position
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Moves Left: " + movesLeft, textX, textY);
    
        // Draw final movement if the game is won
        if (movesLeft == 0 || map[playerRow][playerCol] == 'F') {
            g.setColor(Color.YELLOW); // Set color for final movement
            g.fillOval(playerX, playerY, cellSize, cellSize);
        }
    }
    

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1600, 900);
    }

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

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        showGameTypeSelectionMenu();
    }

    private static void showGameTypeSelectionMenu() {
        String gameType = (String) JOptionPane.showInputDialog(null,
                "Select game type:",
                "Game Type Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"maze", "puzzle"},
                "maze");

        if (gameType != null) {
            // Proceed with map selection based on the chosen game type
            String directory = "";
            if (gameType.equals("maze")) {
                directory = "F:/Projects/-Sliding-puzzles/New folder/src/main/java/maps/maze";
            } else if (gameType.equals("puzzle")) {
                directory = "F:/Projects/-Sliding-puzzles/New folder/src/main/java/maps/puzzel";
            } else {
                JOptionPane.showMessageDialog(null, "Invalid game type.");
                System.exit(1);
            }

            System.out.println("Selected Directory: " + directory); // Debug statement

            File directoryFile = new File(directory);
            if (!directoryFile.exists()) {
                JOptionPane.showMessageDialog(null, "The selected directory does not exist.");
                System.exit(1);
            }

            File[] mapFiles = directoryFile.listFiles();
            if (mapFiles == null || mapFiles.length == 0) {
                JOptionPane.showMessageDialog(null, "No map files found in the selected directory.");
                System.exit(1);
            }

            for (File file : mapFiles) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                }
            }

            String[] mapNames = new String[mapFiles.length];
            for (int i = 0; i < mapFiles.length; i++) {
                mapNames[i] = mapFiles[i].getName();
            }

            String selectedMap = (String) JOptionPane.showInputDialog(null,
                    "Select a map:",
                    "Map Selection",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    mapNames,
                    mapNames[0]);

            try {
                char[][] map = loadMap(directory + "/" + selectedMap);
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

                JFrame frame = new JFrame("Game Board");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                GameBoard gameBoard = new GameBoard(map, startRow, startCol);
                frame.add(gameBoard);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                gameBoard.requestFocusInWindow();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading map file: " + e.getMessage());
            }
        } else {
            // If the user cancels the game type selection, exit the application
            System.exit(0);
        }
    }

    private static char[][] loadMap(String fileName) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            System.out.println(content);
            String[] lines = content.split("\n");
            char[][] map = new char[lines.length][lines[0].length()];
            for (int i = 0; i < lines.length; i++) {
                for (int j = 0; j < lines[i].length(); j++) {
                    map[i][j] = lines[i].charAt(j);
                }
            }
            return map;
        }
    }
}