package program.players.ais;

import program.ChessRules;
import program.players.Player;

import java.util.List;
import java.util.Random;

/**
 * <p>Version 2.0 of Chess-AI</p>
 * <p>MinMax is implemented, however no Alpha-Beta-Pruning yet. The Search-Depth is only 4 Layers</p>
 * <p>The board-Analyzer works only by means of the Board-Score, determined by the score of the pieces.
 * Also the position of the pawns and the king is calculated in (needed in the endgame)</p>
 */
public class AI_MiniMax2 extends Player {

    /**
     * The Search-Depth of the MiniMax-Algorithm. 4 Works well for this AI.
     */
    private final int SEARCH_DEPTH;

    /**
     * The Weight of how much the position of pawns determines the total score.
     */
    private static final float WEIGHT_PAWN_POS = 1;

    /**
     * A Threshold for piece-count. More than that and the PawnPos will not be calculated at all.
     */
    private final int PIECES_LEFT_PAWN_POS_MATTER = 12;

    /**
     * Constructor which initializes the player in the Superclass.
     *
     * @param player Player which this AI will play.
     */
    public AI_MiniMax2(int player) {
        super(player, "AI MiniMax 2");
        SEARCH_DEPTH = 4;
    }

    /**
     * The Method which is being called by the Game-Class. It decides which Move the AI will play using the minMax Algorithm.
     *
     * @param board The current board-Array
     * @return The chosen Move.
     */
    @Override
    public int decideOnMove(int[] board) {
        List<Integer> moves = ChessRules.getLegalMoves(board, player);

        Random r = new Random();

        float bestScore = (player == ChessRules.PLAYER_WHITE) ? -1000000000 : 1000000000;
        int bestMove = 0;
        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            float score = minimax(backUpBoard, SEARCH_DEPTH, player ^ ChessRules.MASK_PLAYER);
            if (score == bestScore && r.nextInt(7) == 0) {
                bestMove = move;
            } else if (player == ChessRules.PLAYER_WHITE && score > bestScore || player == ChessRules.PLAYER_BLACK && score < bestScore) {
                bestScore = score;
                bestMove = move;
            }

        }

        return bestMove;
    }

    /**
     * <p>Recursive MiniMax-Algorithm-Implementation</p>
     * <p>Decides recursively for each board-position which is the best one (alternating between both players as the turn-order determines)</p>
     * <p>At the end of each branch the analyzeBoard-Method is being called</p>
     *
     * @param board  The current board-array. In order to be able to alter the board without messing up the main-one, it gets cloned before calling this method again.
     * @param depth  The Depth the Algorithm will go recursively. It counts down by one every layer.
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
     *
     * @param board the board which needs to be analyzed.
     * @return A score for the board.
     */
    public float analyzeBoard(int[] board) {
        float score = 0;
        for (int cell : board) {
            score += ChessRules.getCost(cell);
        }
        score += getScoreModifier_PawnPos(board);
        return score;
    }

    /**
     * This method is called in the analyzeBoard()-Method and modifies the boards total score based on the pawns position.
     * <p>
     * The further the pawn is to the end of the board, the higher the score.
     * In the middle of the board it needs to be approximately 0, and on the start-pos it needs to be negative.
     *
     * @param board                 An int-array which stores all the piece-information. Item 0 is in the top left corner, Item 7 in the top right, Item 63 in the bottom right.
     * @return The Score-Modifier
     */
    private float getScoreModifier_PawnPos(int[] board) {
        float scoreModifier = 0;
        if (ChessRules.countPieces(board) > PIECES_LEFT_PAWN_POS_MATTER) {
            return 0;
        }
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && (board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_PAWN) {
                int player = board[i] & ChessRules.MASK_PLAYER;
                // The scoreModifier needs to be updated here based on the Position of the pawn
                if (player == ChessRules.PLAYER_WHITE) {
                    scoreModifier += switch ((int) Math.floor(i / 8f)) {
                        case 0 -> 5;
                        case 1 -> 1;
                        case 2 -> 0.4;
                        case 3 -> 0;
                        case 4 -> -0.3;
                        case 5 -> -0.5;
                        default -> -0.6;
                    };
                } else {
                    scoreModifier += switch ((int) Math.floor(i / 8f)) {
                        case 7 -> 5;
                        case 6 -> 1;
                        case 5 -> 0.4;
                        case 4 -> 0;
                        case 3 -> -0.3;
                        case 2 -> -0.5;
                        default -> -0.6;
                    };
                }
            }
        }
        return scoreModifier * WEIGHT_PAWN_POS * (2f - ChessRules.countPieces(board) / 12f);
    }

}
