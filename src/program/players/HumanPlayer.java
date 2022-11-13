package program.players;

import program.guis.Gui;

/**
 * The Class which is used in order to make a human play. In the Main-Method even two HumanPlayers can be assigned to the Game or no human at all.
 */
public class HumanPlayer extends Player {

    /**
     * The gui for the game, which the class needs in order to get a human input from.
     */
    private final Gui gui;

    /**
     * Constructor which inits the player in the superclass and the gui.
     * @param player Player according to ChessRules.MASK_PLAYER.
     * @param gui Gui Object - The SAME one which is being used in the game.
     */
    public HumanPlayer(int player, Gui gui) {
        super(player, "Human");
        this.gui = gui;
    }

    /**
     * Overwritten method from Superclass, which is being called from the Game-Class in order to get the next move from the Human-Player.
     * The actual decision is being made in the GUI-Class.
     * @param board The current board-Array
     * @return The Move-Integer the Human has decided to make.
     */
    @Override
    public int decideOnMove(int[] board) {
        return gui.getInputMove(board, player);
    }

}
