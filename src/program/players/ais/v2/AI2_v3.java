package program.players.ais.v2;

import program.ChessRules;
import program.players.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * <p>Version 3.0 of Chess-AI</p>
 * <p>MinMax and AlphaBeta-Pruning is implemented. The Search-Depth is 5 Layers</p>
 * <p>The board-Analyzer works only by means of the Board-Score, determined by the score of the pieces.
 * Also the position of the pawns and the king is calculated in (needed in the endgame)</p>
 */
public class AI2_v3 extends AI_MinmaxAbstract {

    public static final String AI_NAME = "AI3 PiecePos-Analyzing";

    /**
     * The Search-Depth of the MiniMax-Algorithm. 5 Works well for this AI.
     */

    private final double[] params;
    private final double WEIGHT_POS_PAWNS;
    private final int BIAS_PAWN_POS_PIECE_COUNT;
    private final Function<Integer, Double> pawnRowToScore = i -> Math.exp(-0.5 * i)-0.1;
    private final double WEIGHT_POS_KNIGHTS;
    private final double WEIGHT_POS_BISHOPS;
    private final double WEIGHT_POS_ROOKS;
    private final double WEIGHT_POS_QUEENS;
    private final double CASTLING_BONUS;

    /**
     * Constructor which initializes the player in the Superclass.
     *
     * @param player Player which this AI will play.
     */
    public AI2_v3(int player) {
        this(player, new double[]{6, 1, 16, 1.3f, .8f, 1f, 1.1f, .4f});
    }

    public AI2_v3(int player, double[] params) {
        super(player, AI_NAME, (int) params[0], params[7]);
        this.params = params;
        WEIGHT_POS_PAWNS = params[1];
        BIAS_PAWN_POS_PIECE_COUNT = (int) params[2];
        WEIGHT_POS_KNIGHTS = params[3];
        WEIGHT_POS_BISHOPS = params[4];
        WEIGHT_POS_ROOKS = params[5];
        WEIGHT_POS_QUEENS = params[6];
        CASTLING_BONUS = params[7];
        setPgnName(generatePgnName(AI_NAME, Arrays.stream(params).boxed().toList()));
    }

    /**
     * <p>Analyzes the board based on the score for each piece left on it.</p>
     * <p>A positive score is in favor of the white player, a negative one for th black player.</p>
     *
     * @param board the board which needs to be analyzed.
     * @return A score for the board.
     */
    public double analyzeBoard(int[] board) {
        double score = ChessRules.getScoreByPieceCost(board);
        score += getScoreModifier_PawnPos(board, pawnRowToScore);
        score += getScoreModifier_KnightPos(board);
        score += getScoreModifier_BishopPos(board);
        score += getScoreModifier_RookPos(board);
        score += getScoreModifier_QueenPos(board);
        return score;
    }

    /**
     * This method is called in the analyzeBoard()-Method and modifies the boards total score based on the pawns position.
     * <p>
     * The further the pawn is to the end of the board, the higher the score.
     * In the middle of the board it needs to be approximately 0, and on the start-pos it needs to be negative.
     *
     * @param board An int-array which stores all the piece-information. Item 0 is in the top left corner, Item 7 in the top right, Item 63 in the bottom right.
     * @return The Score-Modifier
     */
    private double getScoreModifier_PawnPos(int[] board, Function<Integer, Double> rowToScore) {
        double scoreModifier = 0;
        if (ChessRules.countPieces(board) <= BIAS_PAWN_POS_PIECE_COUNT) {
            for (int i = 0; i < board.length; i++) {
                if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && (board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_PAWN) {
                    int player = board[i] & ChessRules.MASK_PLAYER;
                    // The scoreModifier needs to be updated here based on the Position of the pawn
                    int row = player == ChessRules.PLAYER_WHITE ? ((int) Math.floor(i / 8f)) : 8 - ((int) Math.floor(i / 8f));
                    scoreModifier += ((player == ChessRules.PLAYER_WHITE) ? 1 : -1) * rowToScore.apply(row);
                }
            }
        }
        return scoreModifier * WEIGHT_POS_PAWNS;
    }

    private double getScoreModifier_KnightPos(int[] board) {
        double modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KNIGHT)) {
                double positionModifier = ChessRules.getKnightMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_KNIGHTS;
    }

    private double getScoreModifier_BishopPos(int[] board) {
        double modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_BISHOP)) {
                double positionModifier = ChessRules.getBishopMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_BISHOPS;
    }

    private double getScoreModifier_RookPos(int[] board) {
        double modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_ROOK)) {
                double positionModifier = ChessRules.getRookMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_ROOKS;
    }

    private double getScoreModifier_QueenPos(int[] board) {
        double modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_QUEEN)) {
                double positionModifier = ChessRules.getQueenMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_QUEENS;
    }

    // TODO: 28.01.2024 score has to take the square root of the number of fields a piece can go to - so that it is less difference between 10 and 11 then between 0 and 1
    // TODO: 28.01.2024 Score-Modifier King - There should be a penalty for if it is not covered up (Check for to how many places a queen could move in the kings position)


}
