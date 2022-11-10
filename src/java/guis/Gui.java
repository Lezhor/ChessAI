package java.guis;

/**
 * <p>This abstract class contains all methods needed for a GUI. Such as printing the Board and getting Input from the Human-Player</p>
 */
public abstract class Gui {

    /**
     * Draws the board. For example on the terminal.
     * @param board Board-Array
     */
    public abstract void printBoard(int[] board);

    /**
     * Prints a Win-Screen.
     * @param player Player who won. As Specified in ChessRules.MASK_PLAYER
     */
    public abstract void printWinner(int player);

    /**
     * Prints that the game ended in a draw.
     */
    public abstract void printStalemate();

    /**
     * Gets an Input Move from the Human-Player
     * @param board Board-Array
     * @param player Player which needs to take a move
     * @return the Move-Integer containing the old-pos and the newPos
     */
    public abstract int getInputMove(int[] board, int player);
}
