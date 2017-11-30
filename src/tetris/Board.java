package tetris;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a Tetris board -- essentially a 2-d grid of booleans. Supports
 * tetris pieces and row clearing. Has an "undo" feature that allows clients to
 * add and remove pieces efficiently. Does not do any drawing or have any idea
 * of pixels. Instead, just represents the abstract 2-d board.
 */
public class Board {

	private int width;
	private int height;

	private int[] widths;
	private int[] heights;

	protected boolean[][] grid;
	private boolean committed;
	
	/**
	 * Creates an empty board of the given width and height measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;

		this.grid = new boolean[width][height];
		this.committed = true;

		this.widths = new int[width];
		this.heights = new int[height];
	}

    /**
     * Creates a nex instance of borard from an instance by copying its attributes
     * @param oldBoard board to copy
     */
	public Board(Board oldBoard){
        this.width = oldBoard.width;
        this.height = oldBoard.height;

        this.grid = oldBoard.grid.clone();
        this.committed = oldBoard.committed;

        this.widths = oldBoard.widths.clone();
        this.heights = oldBoard.heights.clone();
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
        return height;
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
	    return 0; // YOUR CODE HERE
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
	    if (!this.committed) {
		    throw new RuntimeException("can only place object if the board has been commited");
	    }

	    boolean rowFilled = false;

        if (x + piece.getWidth() > this.width || y + piece.getHeight() > this.height){
	        return PLACE_OUT_BOUNDS;
        }

        for (TPoint point : piece.getBody()){
            boolean state = this.grid[x+point.x][y+point.y];
            if (state){
                return PLACE_BAD;
            } else {
                this.grid[x+point.x][y+point.y] = true;
                this.widths[point.y]++;
                if (this.widths[point.y] == this.width) rowFilled = true;
                this.heights[point.x] = point.y; //QUESTION (CF SUJET)
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

        for (int i = 0; i < this.height; i++) {
            if (this.widths[i] == this.width){
                cleared++;
                for (int j = 0; j < this.width; j++) {
                    this.grid[i][j] = false;
                }
                dropFromRow(i);
            }
        }
        return cleared;
	}

    /**
     * Utility method
     * Performs the dropdown feature from the cleared row
     * @param y clear row
     */
	private void dropFromRow(int y){
        for (int i = 0; i < this.width; i++) {
            for (int j = y; j < this.height; j++) {
                this.grid[i][j] = (j + 1 != this.height) && this.grid[i][j + 1];
            }
        }
    }

	/**
	 * Reverts the board to its state before up to one place and one
	 * clearRows(); If the conditions for undo() are not met, such as calling
	 * undo() twice in a row, then the second undo() does nothing. See the
	 * overview docs.
	 */
	public void undo() {
	    // YOUR CODE HERE
    }

	/**
	 * Puts the board in the committed state.
	 */
	public void commit() {
	    // YOUR CODE HERE
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
		return buff.toString();
	}

	// Only for unit tests
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
