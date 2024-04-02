import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Create a map graph with dimensions 10x10
        MapGraph mapGraph = new MapGraph(10, 10);
        
        // Define the map (you can read this from a file in your actual implementation)
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
        
        // Set nodes in the map graph based on the map
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                mapGraph.setNode(i, j, map[i][j]);
            }
        }
        
        // Find the shortest path from start to finish
        List<Node> shortestPath = mapGraph.findShortestPath();
        
        // Print the shortest path
        if (shortestPath.isEmpty()) {
            System.out.println("No path found!");
        } else {
            System.out.println("Shortest path from start to finish:");
            for (int i = shortestPath.size() - 1; i >= 0; i--) {
                Node node = shortestPath.get(i);
                System.out.println("(" + node.getRow() + ", " + node.getCol() + ")");
            }
        }
    }
}
