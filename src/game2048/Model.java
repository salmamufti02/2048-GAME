package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author salma mufti
 */
public class Model extends Observable {
    /**
     * Current contents of the board.
     */
    private Board board;
    /**
     * Current score.
     */
    private int score;
    /**
     * Maximum score so far.  Updated when game ends.
     */
    private int maxScore;
    /**
     * True iff game is ended.
     */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /**
     * Largest piece value.
     */
    public static final int MAX_PIECE = 2048;

    /**
     * A new 2048 game on a board of size SIZE with no pieces
     * and score 0.
     */
    public Model(int size) {
        board = new Board(size);
        score = 0;
        maxScore = 0;
        gameOver = false;
    }

    /**
     * A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes.
     */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        board = new Board(rawValues);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /**
     * Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     * 0 <= COL < size(). Returns null if there is no tile there.
     * Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }  //use for tilt

    /**
     * Return the number of squares on one side of the board.
     * Used for testing. Should be deprecated and removed.
     */
    public int size() {
        return board.size();
    }

    /**
     * Return true iff the game is over (there are no moves, or
     * there is a tile with value 2048 on the board).
     */
    public boolean gameOver() {
        return maxTileExists(board) || !atLeastOneMoveExists(board);
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        board.clear();
        score = 0;
        gameOver = false;
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
    }


    /** Tilt the board toward SIDE. Return true iff this changes the board.*
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     */
    public boolean tilt(Side side) {
        boolean changed = false;
        board.setViewingPerspective(side);

        for (int i = 0; i < board.size(); i++) {
            Tile[] tiles = new Tile[board.size()];
            int index = 0;

            for (Tile tile : board) {
                if (tile != null) {
                    tiles[index++] = tile;
                }
            }
            index = 0;
            while (index < board.size() - 1 && tiles[index] != null) {
                Tile currentTile = tiles[index];
                Tile nextTile = tiles[index + 1];

                if (nextTile != null && currentTile.value() == nextTile.value()) {
                    int mergedValue = currentTile.value() * 2;
                    score += mergedValue;
                    tiles[index] = Tile.create(mergedValue, currentTile.col(), currentTile.row());
                    tiles[index + 1] = null;
                    index += 2;
                    changed = true;
                } else {
                    index++;
                }
            }
            for (int j = 0; j < board.size(); j++) {
                if (tiles[j] != null) {
                    int newRow = side.row(j, i, board.size());
                    int newCol = side.col(j, i, board.size());
                    changed = changed || board.move(newCol, newRow, tiles[j]);
                }
            }
        }
        board.setViewingPerspective(Side.NORTH);

        checkGameOver();
        setChanged();
        notifyObservers();
        return changed;
    }




    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        if (maxTileExists(board) || !atLeastOneMoveExists(board)) {
            gameOver = true;
        }
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     */
    public static boolean emptySpaceExists(Board b) {
        for (int col = 0; col < b.size(); col++) {
            for (int row = 0; row < b.size(); row++) {
                if (b.tile(col, row) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for (Tile tile : b) {
            if (tile != null && tile.value() == MAX_PIECE) {
                return true;
            }
        }
        return false;
    }

/**
 ~ Tests passed: 8 of 8 tests - 30 ms
 /opt/homebrew/Cellar/openjdk/20.0.1/Libexec/openjdk.jdk/Contents/Home/bin/java)
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        for (int row = 0; row < b.size(); row++) {
            for (int col = 0; col < b.size(); col++) {
                Tile currentTile = b.tile(col, row);
                if (b.tile(col, row) == null) {
                    return true;
                }
                if (col < b.size() - 1) {
                    Tile rightTile = b.tile(col + 1, row);
                    if (rightTile != null && rightTile.value() == currentTile.value()) {
                        return true;
                    }
                }
                if (row < b.size() - 1) {
                    Tile belowTile = b.tile(col, row + 1);
                    if (belowTile != null && belowTile.value() == currentTile.value()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Returns the model as a string, used for debugging. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    /** Returns whether two models are equal. */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    /** Returns hash code of Model’s string. */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
