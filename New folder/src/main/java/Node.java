public class Node {
    private int row;
    private int col;
    private char type; // '.' for empty, '0' for rock, 'S' for start, 'F' for finish
    private boolean visited;
    private Node parent; // Parent node in the path
    // You can add more properties as needed
    
    public Node(int row, int col, char type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.visited = false;
        this.parent = null; // Initialize parent as null
    }
    
    // Getters and setters for properties
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public char getType() {
        return type;
    }
    
    public boolean isVisited() {
        return visited;
    }
    
    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    
    public Node getParent() {
        return parent;
    }
    
    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    // You can add more methods as needed
}
