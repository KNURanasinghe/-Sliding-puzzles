import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MapGraph {
    private Node[][] grid;
    private Node startNode;
    private Node finishNode;
    
    public MapGraph(int numRows, int numCols) {
        grid = new Node[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                grid[i][j] = new Node(i, j, '.');
            }
        }
    }
    
    public void setNode(int row, int col, char type) {
        grid[row][col] = new Node(row, col, type);
        if (type == 'S') {
            startNode = grid[row][col];
        } else if (type == 'F') {
            finishNode = grid[row][col];
        }
    }
    
    public List<Node> findShortestPath() {
        List<Node> shortestPath = new ArrayList<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(startNode);
        startNode.setVisited(true);
        boolean pathFound = false;
        
        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            if (currentNode == finishNode) {
                pathFound = true;
                break;
            }
            
            List<Node> neighbors = getNeighbors(currentNode);
            for (Node neighbor : neighbors) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(currentNode);
                    queue.add(neighbor);
                }
            }
        }
        
        if (pathFound) {
            Node current = finishNode;
            while (current != null) {
                shortestPath.add(current);
                current = current.getParent();
            }
        }
        
        // Reset visited flags for next pathfinding
        resetVisitedFlags();
        
        return shortestPath;
    }
    
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, down, left, right
        
        for (int[] dir : directions) {
            int newRow = node.getRow() + dir[0];
            int newCol = node.getCol() + dir[1];
            if (isValid(newRow, newCol) && grid[newRow][newCol].getType() != '0') {
                neighbors.add(grid[newRow][newCol]);
            }
        }
        
        return neighbors;
    }
    
    private boolean isValid(int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }
    
    private void resetVisitedFlags() {
        for (Node[] row : grid) {
            for (Node node : row) {
                node.setVisited(false);
            }
        }
    }
}
