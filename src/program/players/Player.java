package program.players;

/**
 * Abstract Player class for the Human-Player as well as all AIs
 */
public abstract class Player {

    /**
     * The player Color (specified in ChessRules.MASK_PLAYER)
     */
    public final int player;

    /**
     * Used for storing the game in pgn.
     */
    private String pgnName;

    /**
     * Constructor which sets the final integer 'player'
     * @param player New Value for Player
     */
    public Player(int player, String pgnName) {
        this.player = player;
        this.pgnName = pgnName;
    }

    /**
     * <p>The Method which actually is being called by the Game-Loop in order to get the next move to play</p>
     * <p>For a Human player, this method asks the human to input a move. And an AI needs to decide on a move based on the board.</p>
     * <p>The output of this method needs to be a legal move already! It is not being checked later on!</p>
     * @param board The current board-Array
     * @return The move-Integer the player has decided on.
     */
    public abstract int decideOnMove(int[] board);

    public String getPgnName() {
        return pgnName;
    }

    protected void setPgnName(String pgnName) {
        this.pgnName = pgnName;
    }
}
