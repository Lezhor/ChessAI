package program.players.ais.v1;

import program.ChessRules;
import program.players.Player;

import java.util.List;
import java.util.Random;

/**
 * Version 0 of the AI. It plays random moves only.
 */
public class AI_Random extends Player {

    /**
     * Constructor which inits the player in the Superclass
     * @param player player of this AI
     */
    public AI_Random(int player) {
        super(player, "AI_0.0");
    }

    /**
     * Decides on a Move randomly.
     * @param board The current board-Array
     * @return The chosen Move-Integer
     */
    @Override
    public int decideOnMove(int[] board) {
        Random r = new Random();
        List<Integer> moves = ChessRules.getLegalMoves(board, player);
        if (moves.size() == 0)
            return 0;
        return moves.get(r.nextInt(moves.size()));
    }
}
