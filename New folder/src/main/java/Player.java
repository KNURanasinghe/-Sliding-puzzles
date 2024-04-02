public class Player {
    private int row;
    private int col;

    public Player(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void moveUp() {
        if (row > 0) {
            row--;
        }
    }

    public void moveDown(int numRows) {
        if (row < numRows - 1) {
            row++;
        }
    }

    public void moveLeft() {
        if (col > 0) {
            col--;
        }
    }

    public void moveRight(int numCols) {
        if (col < numCols - 1) {
            col++;
        }
    }
}
