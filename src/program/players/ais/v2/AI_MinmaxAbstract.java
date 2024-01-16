package program.players.ais.v2;

import program.ChessRules;
import program.players.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Version 3.0 of Chess-AI</p>
 * <p>MinMax and AlphaBeta-Pruning is implemented. The Search-Depth is 5 Layers</p>
 * <p>The board-Analyzer works only by means of the Board-Score, determined by the score of the pieces.
 * Also the position of the pawns and the king is calculated in (needed in the endgame)</p>
 */
public abstract class AI_MinmaxAbstract extends Player {

    public static final int DEFAULT_SEARCH_DEPTH = 6;

    /**
     * The Search-Depth of the MiniMax-Algorithm. 5 Works well for this AI.
     */

    protected final int searchDepth;
    protected final List<Double> ai_parameters;
    private final ScoreBonusInMinmaxFunction scoreBonusInMinmaxFunction;
    private final double[] scoreBonusInMinmaxFunctionParams;

    /**
     * Constructor which initializes the player in the Superclass.
     *
     * @param player Player which this AI will play.
     */
    public AI_MinmaxAbstract(int player, String aiName, int depth, ScoreBonusInMinmaxFunction scoreBonusInMinmaxFunction, double... scoreBonusInMinmaxFunctionParams) {
        super(player, aiName);
        this.searchDepth = depth;
        this.scoreBonusInMinmaxFunction = scoreBonusInMinmaxFunction;
        this.scoreBonusInMinmaxFunctionParams = scoreBonusInMinmaxFunctionParams;
        ai_parameters = new LinkedList<>();
        ai_parameters.add(searchDepth + 0.0);
        setPgnName(generatePgnName(aiName, ai_parameters));
    }

    /**
     * Default constructer - sets SEARCH_DEPTH to 6
     *
     * @param player player which this AI will play
     */
    public AI_MinmaxAbstract(int player, String aiName) {
        this(player, aiName, DEFAULT_SEARCH_DEPTH);
    }

    /**
     * Default constructer - no bonus in mid the minmax
     *
     * @param player player which this AI will play
     */
    public AI_MinmaxAbstract(int player, String aiName, int depth) {
        this(player, aiName, depth, AI_MinmaxAbstract::calculateScoreBonusInMinmax_NoBonus);
    }

    public AI_MinmaxAbstract(int player, String aiName, int depth, double castlingBonus) {
        this(player, aiName, depth, AI_MinmaxAbstract::calculateScoreBonusInMinmax_Castling, castlingBonus);
    }


    /**
     * The Method which is being called by the Game-Class. It decides which Move the AI will play using the minMax Algorithm. If multiple moves have the same score a random one is chosen.
     *
     * @param board The current board-Array
     * @return The chosen Move.
     */
    @Override
    public int decideOnMove(int[] board) {
        List<Integer> moves = ChessRules.getLegalMovesSorted(board, player);

        Random r = new Random();

        double bestScore = (player == ChessRules.PLAYER_WHITE) ? -1000000000 : 1000000000;
        List<Integer> bestMoves = new ArrayList<>();
        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            double score = minimax(backUpBoard, (player == ChessRules.PLAYER_WHITE) ? bestScore : -1000000000, (player == ChessRules.PLAYER_WHITE) ? 1000000000 : bestScore, searchDepth - 1, player ^ ChessRules.MASK_PLAYER);
            score += scoreBonusInMinmaxFunction.calculate(backUpBoard, move, player, scoreBonusInMinmaxFunctionParams);
            if (bestScore == score) {
                bestMoves.add(move);
            } else if (player == ChessRules.PLAYER_WHITE && score > bestScore || player == ChessRules.PLAYER_BLACK && score < bestScore) {
                bestScore = score;
                bestMoves = new ArrayList<>();
                bestMoves.add(move);
            }

        }
        //System.out.println("Possible best moves: " + bestMoves.size());
        return getBestMoveFromEqualScored(board, bestMoves);
    }

    /**
     * <p>Recursive MiniMax-Algorithm-Implementation with Alpha-Beta-Pruning</p>
     * <p>Decides recursively for each board-position which is the best one (alternating between both players as the turn-order determines)</p>
     * <p>At the end of each branch the analyzeBoard-Method is called</p>
     * <p>Do to Alpha-Beta-Pruning many branches can be pruned away, so that it is not necessary to analyze them</p>
     *
     * @param board  The current board-array. In order to be able to alter the board without messing up the main-one, it gets cloned before calling this method again.
     * @param a      Alpha-Value (For AlphaBetaPruning)
     * @param b      Beta-Value (For AlphaBetaPruning)
     * @param depth  The Depth the Algorithm will go recursively. It counts down by one every layer.
     * @param player The player which takes the current turn. (Alternates every layer)
     * @return The Score which the algorithm assigns to this board.
     */
    private double minimax(int[] board, double a, double b, int depth, int player) {
        // White is max / Black is min


        if (depth <= 0) {
            return analyzeBoard(board);
        }

        double bestScore = (player == ChessRules.PLAYER_WHITE ? -10000000 - depth : 10000000 + depth);
        List<Integer> moves = ChessRules.getLegalMovesSorted(board, player);

        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            double score = minimax(backUpBoard, a, b, depth - 1, player ^ ChessRules.MASK_PLAYER);
            score += scoreBonusInMinmaxFunction.calculate(backUpBoard, move, player, scoreBonusInMinmaxFunctionParams);
            if (player == ChessRules.PLAYER_WHITE) {
                bestScore = Math.max(bestScore, score);
                a = Math.max(bestScore, a);
                if (a > b) {
                    break;
                }
            } else if (player == ChessRules.PLAYER_BLACK) {
                bestScore = Math.min(bestScore, score);
                b = Math.min(bestScore, b);
                if (b < a) {
                    break;
                }
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
     * @param board
     * @param moves
     * @return
     */
    private int getBestMoveFromEqualScored(int[] board, List<Integer> moves) {
        if (moves.size() == 1)
            return moves.get(0);
        List<Integer> bestMoves = new ArrayList<>();
        double bestScore = (player == ChessRules.PLAYER_WHITE) ? -1000000000 : 1000000000;
        for (int move : moves) {
            int[] boardCopy = board.clone();
            ChessRules.makeMove(boardCopy, move);
            double score = analyzeBoard(boardCopy);
            if (score == bestScore) {
                bestMoves.add(move);
            } else if (player == ChessRules.PLAYER_WHITE && score > bestScore || player == ChessRules.PLAYER_BLACK && score < bestScore) {
                bestScore = score;
                bestMoves = new ArrayList<>();
                bestMoves.add(move);
            }
        }
        return bestMoves.get((new Random()).nextInt(bestMoves.size()));
    }
    
    protected String generatePgnName(String aiName, List<Double> params) {
        return params.stream()
                .map(d -> String.format("%.2f", d))
                .map(s -> s.replace(',', '.'))
                .reduce(aiName, (a, b) -> a + "||" + b);
    }

    /**
     * <p>Analyzes the board based on the score for each piece left on it.</p>
     * <p>A positive score is in favor of the white player, a negative one for th black player.</p>
     *
     * @param board the board which needs to be analyzed.
     * @return A score for the board.
     */
    public abstract double analyzeBoard(int[] board);


    public static double calculateScoreBonusInMinmax_Castling(int[] board, int lastMove, int player, double... params) {
        if (params.length < 1) {
            return 0f;
        }
        double castlingBonus = params[0];
        if ((board[ChessRules.getMoveOldPos(lastMove)] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KING && (board[ChessRules.getMoveOldPos(lastMove)] & ChessRules.MASK_HAS_MOVED) == 0) {
            return ChessRules.isCastlingMove(board, lastMove) ^ (player == ChessRules.PLAYER_BLACK) ? castlingBonus : -castlingBonus;
        }
        return 0f;
    }

    public static double calculateScoreBonusInMinmax_NoBonus(int[] board, int lastMove, int player, double... params) {
        return 0f;
    }

    @FunctionalInterface
    public interface ScoreBonusInMinmaxFunction {

        double calculate(int[] board, int lastMove, int player, double... params);

    }


}
