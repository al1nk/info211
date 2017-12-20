package tetris;

import java.util.Arrays;
import java.util.Stack;

/**
 * Represents a Tetris board -- essentially a 2-d grid of booleans. Supports
 * tetris pieces and row clearing. Has an "undo" feature that allows clients to
 * add and remove pieces efficiently. Does not do any drawing or have any idea
 * of pixels. Instead, just represents the abstract 2-d board.
 */
public class Board {

	private int width;
	private int height;

	protected int[] widths;
	protected int[] heights;

	protected boolean[][] grid;
	private boolean committed;

	private boolean[][] backupGrid;
	private int[] backupWidths;
	private int[] backupHeights;
	
	/**
	 * Creates an empty board of the given width and height measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;

		this.grid = new boolean[width][height];
		this.committed = true;

		this.widths = new int[height];
		this.heights = new int[width];

        this.backupGrid = new boolean[width][height];
        this.backupWidths = new int[height];
        this.backupHeights = new int[width];

    }

    /**
     * Creates a nex instance of borard from an instance by copying its attributes
     * @param oldBoard board to copy
     */
	public Board(Board oldBoard){
        this.width = oldBoard.width;
        this.height = oldBoard.height;

        //Here, we use Java 8's streams to 'deepcopy' the 2D grid array
        this.grid = Arrays.stream(oldBoard.grid).map(boolean[]::clone).toArray(boolean[][]::new);

        this.committed = oldBoard.committed;

        //Using Arrays' copyOf method to copy widths and heights
        this.widths = Arrays.copyOf(oldBoard.widths, oldBoard.height);
        this.heights = Arrays.copyOf(oldBoard.heights, oldBoard.width);
    }
	
	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns the max column height present in the board. For an empty board
	 * this is 0.
	 */
	public int getMaxHeight() {

        int max = 0;

        for (int height : this.heights){
            if (max < height){
                max = height;
            }
        }
        return max;
	}

	/**
	 * Given a piece and an x, returns the y value where the piece would come to
	 * rest if it were dropped straight down at that x.
	 * 
	 * <p>
	 * Implementation: use the skirt and the col heights to compute this fast --
	 * O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {

	    int y = 0;

        for (int i = 0; i < piece.getSkirt().size(); i++) {
            int delta = this.heights[i] - piece.getSkirt().get(i);
            if (delta > y){
                y = delta;
            }
        }
	    return y;
	}

	/**
	 * Returns the height of the given column -- i.e. the y value of the highest
	 * block + 1. The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
	    return this.heights[x];
	}

	/**
	 * Returns the number of filled blocks in the given row.
	 */
	public int getRowWidth(int y) {
	    return this.widths[y];
	}

	/**
	 * Returns true if the given block is filled in the board. Blocks outside of
	 * the valid width/height area always return true.
	 */
	public boolean getGrid(int x, int y) {
	    return this.grid[x][y];
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 * Attempts to add the body of a piece to the board. Copies the piece blocks
	 * into the board grid. Returns PLACE_OK for a regular placement, or
	 * PLACE_ROW_FILLED for a regular placement that causes at least one row to
	 * be filled.
	 * 
	 * <p>
	 * Error cases: A placement may fail in two ways. First, if part of the
	 * piece may falls out of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 * Or the placement may collide with existing blocks in the grid in which
	 * case PLACE_BAD is returned. In both error cases, the board may be left in
	 * an invalid state. The client can use undo(), to recover the valid,
	 * pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
	    //Managing the committed state
	    if (!this.committed) {
		    throw new RuntimeException("Can only place object if the board has been commited");
	    }
        this.committed = false;

	    //Row fill flag
        boolean rowFilled = false;

	    //Checking if OOB
        if (x + piece.getWidth() > this.width || y + piece.getHeight() > this.height
                || x < 0 || y < 0){
	        return PLACE_OUT_BOUNDS;
        }


        for (TPoint point : piece.getBody()){
            //Current XY values
            int cx = x + point.x, cy = y + point.y;

            //Checking the current state of the board @ (cx, cy) coordinates
            if (this.grid[cx][cy]){
                //There is already something
                return PLACE_BAD;
            } else {
                //Otherwise place the piece aka we flip the value
                this.grid[cx][cy] = true;
                //Updating the widths and heights attributes
                this.widths[cy]++;
                this.heights[cx] = cy + 1;

                //has a row been filled ?
                if (this.widths[cy] == this.width) rowFilled = true;
            }
        }

	    return (rowFilled) ? PLACE_ROW_FILLED : PLACE_OK;
	}

	/**
	 * Deletes rows that are filled all the way across, moving things above
	 * down. Returns the number of rows cleared.
	 */
	public int clearRows() {
	    int cleared = 0;

	    //Here, we browse through the grid (bottom to top) to see if there are rows to be cleared
        //If there are, we store the index in a stack ...
        Stack<Integer> rowsToClear = new Stack<>();

        for (int i = 0; i < this.height; i++) {
            if (this.widths[i] == this.width){
                rowsToClear.push(i);
            }
        }

        //...so that we can pop the indexes in the right order so
        //this code can work with multiple lines to clear
        while (!rowsToClear.empty()){
            int row = rowsToClear.pop();
            clearOne(row);
            dropFromRow(row);
            cleared++;
        }

        return cleared;
	}

    /**
     * Utility method
     * Clears one row at given index
     * @param y clear row
     */
    public void clearOne(int y){

        for (int i = 0; i < this.width; i++) {
            this.grid[i][y] = false;
            this.heights[i]--;
        }

        this.widths[y] = 0;
    }

    /**
     * Utility method
     * Performs the dropdown feature from the cleared row
     * @param y clear row
     */
	private void dropFromRow(int y){

        for (int i = 0; i < this.width; i++) {
            //Rather than using Arrays' copyOf method, we use System's arraycopy
            //Because we don't want to allocate new memory and this way, we can
            //truncate the right portion of data
            System.arraycopy(this.grid[i], y + 1, this.grid[i], y, this.height - 1 - y);
        }
        System.arraycopy(this.widths, y + 1, this.widths, y, this.height - 1 - y);
    }

	/**
	 * Reverts the board to its state before up to one place and one
	 * clearRows(); If the conditions for undo() are not met, such as calling
	 * undo() twice in a row, then the second undo() does nothing. See the
	 * overview docs.
	 */
	public void undo() {

	    //Using the same methods as the copy constructor, we revert to the backed uip state
        //by cpopying the backup arrays
        this.grid = Arrays.stream(this.backupGrid).map(boolean[]::clone).toArray(boolean[][]::new);
        this.heights = Arrays.copyOf(this.backupHeights, this.width);
        this.widths = Arrays.copyOf(this.backupWidths, this.height);

        this.committed = true;
    }

	/**
	 * Puts the board in the committed state.
	 */
	public void commit() {

	    //We create a copy instance of the board and assign the right variables
        Board backup = new Board(this);

        this.backupGrid = backup.grid;
        this.backupHeights = backup.heights;
        this.backupWidths = backup.widths;

        this.committed = true;
	}

	/*
	 * Renders the board state as a big String, suitable for printing. This is
	 * the sort of print-obj-state utility that can help see complex state
	 * change over time. (provided debugging utility)
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = this.height - 1; y >= 0; y--) {
			buff.append('|');
			for (int x = 0; x < this.width; x++) {
				if (getGrid(x, y))
					buff.append('+');
				else
					buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x = 0; x < this.width + 2; x++)
			buff.append('-');
		buff.append('\n');
		return buff.toString();
	}

	// Only for unit tetris.tests
	protected void updateWidthsHeights() {
		Arrays.fill(this.widths, 0);

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				if (this.grid[i][j]) {
					this.widths[j] += 1;
					this.heights[i] = Math.max(j + 1, this.heights[i]);
				}
			}
		}
	}
}
