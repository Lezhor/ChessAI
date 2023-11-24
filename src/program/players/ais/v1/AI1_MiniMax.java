package program.players.ais.v1;

import program.ChessRules;
import program.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>Version 1.0 of Chess-AI</p>
 * <p>MinMax is implemented, however no Alpha-Beta-Pruning yet. The Search-Depth is only 4 Layers</p>
 * <p>The board-Analyzer works only by means of the Board-Score, determined by the score of the pieces.</p>
 */
public class AI1_MiniMax extends Player {

    /**
     * The Search-Depth of the MiniMax-Algorithm. 4 Works well for this AI.
     */
    private final int SEARCH_DEPTH;

    /**
     * Constructor which initializes the player in the Superclass.
     * @param player Player which this AI will play.
     */
    public AI1_MiniMax(int player) {
        super(player, "AI MiniMax 1");
        SEARCH_DEPTH = 4;
    }

    /**
     * The Method which is being called by the Game-Class. It decides which Move the AI will play using the minMax Algorithm.
     * @param board The current board-Array
     * @return The chosen Move.
     */
    @Override
    public int decideOnMove(int[] board) {
        List<Integer> moves = ChessRules.getLegalMoves(board, player);

        Random r = new Random();

        float bestScore = (player == ChessRules.PLAYER_WHITE) ? -1000000000 : 1000000000;
        List<Integer> bestMoves = new ArrayList<>();
        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            float score = minimax(backUpBoard, SEARCH_DEPTH, player ^ ChessRules.MASK_PLAYER);
            if (score == bestScore && r.nextInt(7) == 0) {
                bestMoves.add(move);
            } else if (player == ChessRules.PLAYER_WHITE && score > bestScore || player == ChessRules.PLAYER_BLACK && score < bestScore) {
                bestScore = score;
                bestMoves = new ArrayList<>();
                bestMoves.add(move);
            }

        }
        //System.out.println("Possible best moves: " + bestMoves.size());
        return bestMoves.get(r.nextInt(bestMoves.size()));
    }

    /**
     * <p>Recursive MiniMax-Algorithm-Implementation</p>
     * <p>Decides recursively for each board-position which is the best one (alternating between both players as the turn-order determines)</p>
     * <p>At the end of each branch the analyzeBoard-Method is being called</p>
     * @param board The current board-array. In order to be able to alter the board without messing up the main-one, it gets cloned before calling this method again.
     * @param depth The Depth the Algorithm will go recursively. It counts down by one every layer.
     * @param player The player which takes the current turn. (Alternates every layer)
     * @return The Score which the algorithm assigns to this board.
     */
    public float minimax(int[] board, int depth, int player) {
        // White is max / Black is min


        if (depth == 0) {
            return analyzeBoard(board);
        }

        float bestScore = (player == ChessRules.PLAYER_WHITE ? -10000000 - depth : 10000000 + depth);
        List<Integer> moves = ChessRules.getLegalMoves(board, player);

        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            float score = minimax(backUpBoard, depth - 1, player ^ ChessRules.MASK_PLAYER);
            if (player == ChessRules.PLAYER_WHITE && score > bestScore) {
                bestScore = score;
            } else if (player == ChessRules.PLAYER_BLACK && score < bestScore) {
                bestScore = score;
            }
        }
        if (moves.size() == 0) {
            if (!ChessRules.pieceInCheck(board, ChessRules.findPiecePos(board, player, ChessRules.PIECE_KING), player)) {
                bestScore = 0;
            }
        }

        return bestScore;
    }

    /**
     * <p>Analyzes the board based on the score for each piece left on it.</p>
     * <p>A positive score is in favor of the white player, a negative one for th black player.</p>
     * @param board the board which needs to be analyzed.
     * @return A score for the board.
     */
    public static float analyzeBoard(int[] board) {
        float score = 0;
        for (int cell : board) {
            score += ChessRules.getCost(cell);
        }
        return score;
    }


}
