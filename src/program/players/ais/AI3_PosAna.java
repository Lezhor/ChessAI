package program.players.ais;

import program.ChessRules;
import program.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>Version 3.0 of Chess-AI</p>
 * <p>MinMax and AlphaBeta-Pruning is implemented. The Search-Depth is 5 Layers</p>
 * <p>The board-Analyzer works only by means of the Board-Score, determined by the score of the pieces.
 * Also the position of the pawns and the king is calculated in (needed in the endgame)</p>
 */
public class AI3_PosAna extends Player {

    /**
     * The Search-Depth of the MiniMax-Algorithm. 5 Works well for this AI.
     */

    private final float[] params;
    private final int SEARCH_DEPTH = 6;

    private final float WEIGHT_POS_PAWNS = 1f;
    private final int BIAS_PAWN_POS_PIECE_COUNT = 16;
    private final float WEIGHT_POS_KNIGHTS = 1.3f;
    private final float WEIGHT_POS_BISHOPS = .8f;
    private final float WEIGHT_POS_ROOKS = 1f;
    private final float WEIGHT_POS_QUEENS = 1.1f;
    private final float CASTLING_BONUS = .4f;

    /**
     * Constructor which initializes the player in the Superclass.
     *
     * @param player Player which this AI will play.
     */
    public AI3_PosAna(int player) {
        this(player, new float[]{6, 1, 16, 1.3f, .8f, 1f, 1.1f, .4f});
    }

    public AI3_PosAna(int player, float[] params) {
        super(player, "AI3 PiecePos-Analyzing");
        this.params = params;
        if (params.length != 8) {
            new IllegalArgumentException("Number of params wrong!");
        }
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

        float bestScore = (player == ChessRules.PLAYER_WHITE) ? -1000000000 : 1000000000;
        List<Integer> bestMoves = new ArrayList<>();
        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            float score = minimax(backUpBoard, (player == ChessRules.PLAYER_WHITE) ? bestScore : -1000000000, (player == ChessRules.PLAYER_WHITE) ? 1000000000 : bestScore, SEARCH_DEPTH, player ^ ChessRules.MASK_PLAYER);
            if (player == ChessRules.PLAYER_WHITE) {
                if ((board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KING && (board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_HAS_MOVED) == 0) {
                    score += ChessRules.isCastlingMove(board, move) ? CASTLING_BONUS : -CASTLING_BONUS;
                }
            } else {
                if ((board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KING && (board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_HAS_MOVED) == 0) {
                    score -= ChessRules.isCastlingMove(board, move) ? CASTLING_BONUS : -CASTLING_BONUS;
                }
            }
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
    public float minimax(int[] board, float a, float b, int depth, int player) {
        // White is max / Black is min


        if (depth == 0) {
            return analyzeBoard(board);
        }

        float bestScore = (player == ChessRules.PLAYER_WHITE ? -10000000 - depth : 10000000 + depth);
        List<Integer> moves = ChessRules.getLegalMovesSorted(board, player);

        int[] backUpBoard;

        for (int move : moves) {
            backUpBoard = board.clone();
            ChessRules.makeMove(backUpBoard, move);
            float score = minimax(backUpBoard, a, b, depth - 1, player ^ ChessRules.MASK_PLAYER);
            if (player == ChessRules.PLAYER_WHITE) {
                if ((board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KING && (board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_HAS_MOVED) == 0) {
                    score += ChessRules.isCastlingMove(board, move) ? CASTLING_BONUS : -CASTLING_BONUS;
                }
                bestScore = Math.max(bestScore, score);
                a = Math.max(bestScore, a);
                if (a > b) {
                    break;
                }
            } else if (player == ChessRules.PLAYER_BLACK) {
                if ((board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KING && (board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_HAS_MOVED) == 0) {
                    score -= ChessRules.isCastlingMove(board, move) ? CASTLING_BONUS : -CASTLING_BONUS;
                }
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
    public int getBestMoveFromEqualScored(int[] board, List<Integer> moves) {
        if (moves.size() == 1)
            return moves.get(0);
        List<Integer> bestMoves = new ArrayList<>();
        float bestScore = (player == ChessRules.PLAYER_WHITE) ? -1000000000 : 1000000000;
        for (int move : moves) {
            int[] boardCopy = board.clone();
            ChessRules.makeMove(boardCopy, move);
            float score = analyzeBoard(boardCopy);
            if (score == bestScore) {
                bestMoves.add(move);
            } else if (player == ChessRules.PLAYER_WHITE && score > bestScore || player == ChessRules.PLAYER_BLACK && score < bestScore) {
                bestScore = score;
                bestMoves = new ArrayList<>();
                bestMoves.add(move);
            }
        }
        return moves.get((new Random()).nextInt(moves.size()));
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
        score += getScoreModifier_PieceCosts(board);
        score += getScoreModifier_PawnPos(board);
        score += getScoreModifier_KnightPos(board);
        score += getScoreModifier_BishopPos(board);
        score += getScoreModifier_RookPos(board);
        score += getScoreModifier_QueenPos(board);
        return score;
    }

    private float getScoreModifier_PieceCosts(int[] board) {
        float score = 0;
        for (int cell : board) {
            score += ChessRules.getCost(cell);
        }
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
    private float getScoreModifier_PawnPos(int[] board) {
        float scoreModifier = 0;
        if (ChessRules.countPieces(board) <= BIAS_PAWN_POS_PIECE_COUNT) {
            for (int i = 0; i < board.length; i++) {
                if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && (board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_PAWN) {
                    int player = board[i] & ChessRules.MASK_PLAYER;
                    // The scoreModifier needs to be updated here based on the Position of the pawn
                    scoreModifier += ((player == ChessRules.PLAYER_WHITE) ? 1 : -1)
                            * switch (player == ChessRules.PLAYER_WHITE ? ((int) Math.floor(i / 8f)) : 8 - ((int) Math.floor(i / 8f))) {
                        case 0 -> 1;
                        case 1 -> .4f;
                        case 2 -> .2f;
                        case 3 -> .1f;
                        case 4 -> .03f;
                        case 5 -> -.01f;
                        case 6 -> -.03f;
                        case 7 -> -.04f;
                        default -> 0;
                    };
                }
            }
        }
        return scoreModifier * WEIGHT_POS_PAWNS;
    }

    private float getScoreModifier_KnightPos(int[] board) {
        float modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KNIGHT)) {
                float positionModifier = ChessRules.getKnightMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_KNIGHTS;
    }

    private float getScoreModifier_BishopPos(int[] board) {
        float modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_BISHOP)) {
                float positionModifier = ChessRules.getBishopMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_BISHOPS;
    }

    private float getScoreModifier_RookPos(int[] board) {
        float modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_ROOK)) {
                float positionModifier = ChessRules.getRookMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_ROOKS;
    }

    private float getScoreModifier_QueenPos(int[] board) {
        float modifier = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0 && ((board[i] & ChessRules.MASK_PIECE) == ChessRules.PIECE_QUEEN)) {
                float positionModifier = ChessRules.getQueenMoves(board, i).size() / 100f;
                if ((board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE) {
                    modifier += positionModifier;
                } else {
                    modifier -= positionModifier;
                }
            }
        }
        return modifier * WEIGHT_POS_QUEENS;
    }


}
