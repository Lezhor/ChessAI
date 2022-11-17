package program;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The ChessRules Class contains all needed information to run a game of chess.</p>
 * <p>
 * For example all the different masks which dictate the Protocol of saving a specific piece with all its stats on the board.
 * All Information of one Piece, except its position, is saved in an Integer.
 * </p>
 * <p>
 * A move is also an Integer, whereas the last byte is the old Position and the second to last byte the new position to move to.
 * </p>
 * <p>
 * Furthermore, the rules to play the game are saved here in form of the method 'getLegalMoves()'.
 * AIs use this method to cycle through all the different Game-Configurations.
 * </p>
 */

public class ChessRules {

    /**
     * This Constant is 0. It is used to clear a cell on a board for example if the piece moves away.
     */
    public static final int EMPTY_FIELD = 0;

    /**
     * 0000 0111 - The piece-type is stored in these last three bits
     */
    public static final int MASK_PIECE = 0b0000_0111;

    /**
     * 001 - Resembles Pawn
     */
    public static final int PIECE_PAWN = 0b0000_0001;

    /**
     * 010 - Resembles Knight
     */
    public static final int PIECE_KNIGHT = 0b0000_0010;

    /**
     * 011 - Resembles Bishop
     */
    public static final int PIECE_BISHOP = 0b0000_0011;

    /**
     * 100 - Resembles Rook
     */
    public static final int PIECE_ROOK = 0b0000_0100;

    /**
     * 101 - Resembles Queen
     */
    public static final int PIECE_QUEEN = 0b0000_0101;

    /**
     * 110 - Resembles King
     */
    public static final int PIECE_KING = 0b0000_0110;


    // Players

    /**
     * 0000 1000 - In This Bit the Color of the piece is saved. As shown in PLAYER_WHITE and PLAYER_BLACK.
     */
    public static final int MASK_PLAYER = 0b0000_1000;

    /**
     * 1 on the MASK_PLAYER Bit.
     */
    public static final int PLAYER_WHITE = 0b0000_1000;

    /**
     * 0 on the MASK_PLAYER Bit.
     */
    public static final int PLAYER_BLACK = 0b0000_0000;

    /**
     * 0001 0000 - For each new piece this bit is set to 0. As soon as it moves it becomes one.
     * <p>Useful for Castling-Mechanics.</p>
     */
    public static final int MASK_HAS_MOVED = 0b0001_0000;

    /**
     * 0010 0000 - Only if the pawn just has moved two Fields forward this bit is set to 1. But as soon if it happened more than one turn ago it is set to 0 again.
     * <p>Useful for En Passant</p>
     */
    public static final int MASK_PAWN_DOUBLE_JUMP = 0b0010_0000;

    /**
     * 0100 0000 - The easiest way to check if a Field is occupied or not
     */
    public static final int MASK_SET_FIELD = 0b0100_0000;

    /**
     * Sets specific bit(s) of an integer to 1
     * @param value The input-value that needs to be changed
     * @param mask The bit which needs to be changed in value
     * @return value with the mask-bit(s) set to 1.
     */
    public static int setBit(int value, int mask) {
        return value | mask;
    }

    /**
     * Sets specific bit(s) of an integer to 0
     * @param value The input-value that needs to be changed
     * @param mask The bit which needs to be changed in value param
     * @return value with the mask-bit(s) set to 0.
     */
    public static int unsetBit(int value, int mask) {
        return (value | mask) ^ mask;
    }

    // String Methods:

    /**
     * <p>Turns a cell on the board into a String</p>
     * <p>Empty String if the Cell is not occupied</p>
     * <p>String in UpperCase if the piece's color is white. e.g. "KNIGHT", "ROOK"</p>
     * <p>String in LowerCase if the piece's color is black. e.g. "queen", "pawn"</p>
     * @param boardCell an Item from the board-Array where the piece is saved.
     * @return The String-Value of the piece's name.
     */
    public static String getPieceName(int boardCell) {
        // Returns the Name of a piece
        // In Capslock, if the piece is white, and in lowercase letters if the piece is black
        String piece = switch (boardCell & MASK_PIECE) {
            case PIECE_PAWN -> "pawn";
            case PIECE_ROOK -> "rook";
            case PIECE_KNIGHT -> "knight";
            case PIECE_BISHOP -> "bishop";
            case PIECE_QUEEN -> "queen";
            case PIECE_KING -> "king";
            default -> "";
        };
        return ((boardCell & MASK_PLAYER) > 0) ? piece.toUpperCase() : piece.toLowerCase();
    }

    // Move Methods:

    /**
     * <p>Splits the move-integer into two positions and returns the old one (saved in the last byte)</p>
     * @param move The move-integer.
     * @return the oldPosition of move
     */
    public static int getMoveOldPos(int move) {
        // Last Byte
        return (move & 0x00FF);
    }

    /**
     * <p>Splits the move-integer into two positions and returns the new one (saved in the second to last byte)</p>
     * @param move The move-integer.
     * @return the oldPosition of move.
     */
    public static int getMoveNewPos(int move) {
        // Second to last byte
        return ((move >> 8) & 0xFF);
    }

    /**
     * <p>Unites two input-positions into a move-integer.</p>
     * <p>The Last byte holds the old position and the second to last byte the new position</p>
     * @param oldPos The position of the piece which needs to move.
     * @param newPos The position the piece needs to move to. It can be occupied by an enemy piece.
     * @return The new move-integer containing both positions
     */
    public static int getMove(int oldPos, int newPos) {
        return (newPos << 8) | oldPos;
    }

    /**
     * <p>Changes a board-Array by playing a move</p>
     * <p>The move-integer gets split into the two position, the new position gets overwritten by the new one</p>
     * <p>Also other game mechanics are hardcoded here. e.g. Castling if the king moves more than one field, En passant, or pawns becoming a queen when reaching the final line</p>
     * <p>!!Alert: makeMove() does NOT check if a move is legal or not! It is assumed that the move is legal already! So technically makeMove can play illegal moves, even capturing own pieces</p>
     * @param board The board-array which needs to be altered.
     * @param move The move-integer containing newPos and oldPos.
     */
    public static void makeMove(int[] board, int move) {
        // Move contains two Bytes
        //      - Last byte: Old Pos
        //      - Second to last Byte: New Pos

        // En Passant:
        if ((board[getMoveOldPos(move)] & MASK_PIECE) == PIECE_PAWN && Math.abs(getMoveNewPos(move) - getMoveOldPos(move)) % 8 != 0) {
            if ((board[getMoveNewPos(move)] & MASK_SET_FIELD) == 0) {
                board[getMoveOldPos(move) + ((getMoveNewPos(move) % 8) - (getMoveOldPos(move) % 8))] = EMPTY_FIELD;
            }
        }

        board[getMoveNewPos(move)] = setBit(board[getMoveOldPos(move)], MASK_HAS_MOVED);
        board[getMoveOldPos(move)] = EMPTY_FIELD;


        // Remove DoubleMoveBit
        for (int i = 0; i < board.length; i++) {
            board[i] = unsetBit(board[i], MASK_PAWN_DOUBLE_JUMP);
        }
        // Castling:
        if ((board[getMoveNewPos(move)] & MASK_PIECE) == PIECE_KING) {
            if (getMoveNewPos(move) - getMoveOldPos(move) == 2) {
                int rookPos = ((int) Math.ceil(getMoveOldPos(move) / 8f)) * 8 - 1;
                board[getMoveNewPos(move) - 1] = setBit(board[rookPos], MASK_HAS_MOVED);
                board[rookPos] = EMPTY_FIELD;
            } else if (getMoveOldPos(move) - getMoveNewPos(move) == 2) {
                int rookPos = ((int) Math.floor(getMoveOldPos(move) / 8f)) * 8;
                board[getMoveNewPos(move) + 1] = setBit(board[rookPos], MASK_HAS_MOVED);
                board[rookPos] = EMPTY_FIELD;
            }
        }
        if ((board[getMoveNewPos(move)] & MASK_PIECE) == PIECE_PAWN) {
            if (getMoveNewPos(move) < 8 || getMoveNewPos(move) >= 56) {
                // Pawn becomes Queen
                board[getMoveNewPos(move)] = setBit(board[getMoveNewPos(move)], PIECE_QUEEN);
            } else if (Math.abs(getMoveNewPos(move) - getMoveOldPos(move)) == 16) {
                // Pawn gets DoubleMoveBit Set (For En passant)
                board[getMoveNewPos(move)] = setBit(board[getMoveNewPos(move)], MASK_PAWN_DOUBLE_JUMP);
            }

        }
    }

    // Get Possible Moves:

    /**
     * <p>Gets the list of all the possible moves to play for one player. It is achieved this by calling the specific getPieceMoves()-Methods e.g. getKnightMoves() and adding all the Moves together to one List</p>
     * <p>After this list is completed, it is once again run through and filtered: All the moves which result in the player's king getting in check are eliminated</p>
     * @param board Current Board-Array.
     * @param player Current Player which has to take a move
     * @return A list of all the possible moves for the player
     */
    public static List<Integer> getLegalMoves(int[] board, int player) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & MASK_SET_FIELD) > 0 && (board[i] & MASK_PLAYER) == player) {
                switch (board[i] & MASK_PIECE) {
                    case PIECE_PAWN -> moves.addAll(getPawnMoves(board, i));
                    case PIECE_KNIGHT -> moves.addAll(getKnightMoves(board, i));
                    case PIECE_BISHOP -> moves.addAll(getBishopMoves(board, i));
                    case PIECE_ROOK -> moves.addAll(getRookMoves(board, i));
                    case PIECE_QUEEN -> moves.addAll(getQueenMoves(board, i));
                    case PIECE_KING -> moves.addAll(getKingMoves(board, i));
                }
            }
        }

        for (int i = 0; i < moves.size(); i++) {
            if (moveIllegalDueToSelfCheck(board, moves.get(i))) {
                moves.remove(i);
                i--;
            }
        }

        return moves;
    }

    /**
     *
     * @param board
     * @param player
     * @return
     */
    public static List<Integer> getLegalMovesSorted(int[] board, int player) {
        List<Integer> moves = getLegalMoves(board, player);
        List<Integer> sortedMoves = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            if ((board[getMoveNewPos(moves.get(i))] & MASK_SET_FIELD) > 0) {
                sortedMoves.add(moves.remove(i));
                i--;
            }
        }
        sortedMoves.addAll(moves);
        return sortedMoves;
    }

    /**
     * Gets All Knight-Moves from a specific position. Also gets the color of the Knight needed for this on its own.
     * In this method it is not yet checked, if as the result of the move the players king gets in check.
     * @param board Current Board
     * @param pos The Position of the Knight
     * @return A List of all possible moves.
     */
    private static List<Integer> getKnightMoves(int[] board, int pos) {
        List<Integer> moves = new ArrayList<>();
        int player = board[pos] & MASK_PLAYER;
        if (pos % 8 < 7) {
            if (pos - 15 >= 0 && ((board[pos - 15] & MASK_SET_FIELD) == 0 || (board[pos - 15] & MASK_PLAYER) != player))
                moves.add(getMove(pos, pos - 15));
            if (pos + 17 < board.length && ((board[pos + 17] & MASK_SET_FIELD) == 0 || (board[pos + 17] & MASK_PLAYER) != player))
                moves.add(getMove(pos, pos + 17));
            if (pos % 8 < 6) {
                if (pos - 6 >= 0 && ((board[pos - 6] & MASK_SET_FIELD) == 0 || (board[pos - 6] & MASK_PLAYER) != player))
                    moves.add(getMove(pos, pos - 6));
                if (pos + 10 < board.length && ((board[pos + 10] & MASK_SET_FIELD) == 0 || (board[pos + 10] & MASK_PLAYER) != player))
                    moves.add(getMove(pos, pos + 10));
            }
        }
        if (pos % 8 > 0) {
            if (pos - 17 >= 0 && ((board[pos - 17] & MASK_SET_FIELD) == 0 || (board[pos - 17] & MASK_PLAYER) != player))
                moves.add(getMove(pos, pos - 17));
            if (pos + 15 < board.length && ((board[pos + 15] & MASK_SET_FIELD) == 0 || (board[pos + 15] & MASK_PLAYER) != player))
                moves.add(getMove(pos, pos + 15));
            if (pos % 8 > 1) {
                if (pos - 10 >= 0 && ((board[pos - 10] & MASK_SET_FIELD) == 0 || (board[pos - 10] & MASK_PLAYER) != player))
                    moves.add(getMove(pos, pos - 10));
                if (pos + 6 < board.length && ((board[pos + 6] & MASK_SET_FIELD) == 0 || (board[pos + 6] & MASK_PLAYER) != player))
                    moves.add(getMove(pos, pos + 6));
            }
        }
        return moves;
    }

    /**
     * Gets All Bishop-Moves from a specific position. Also gets the color of the Bishop needed for this on its own.
     * In this method it is not yet checked, if as the result of the move the players king gets in check.
     * @param board Current Board
     * @param pos The Position of the Bishop.
     * @return A List of all possible moves.
     */
    private static List<Integer> getBishopMoves(int[] board, int pos) {
        List<Integer> moves = new ArrayList<>();
        int player = board[pos] & MASK_PLAYER;
        // TopRight
        for (int i = pos - 7; i % 8 > 0; i -= 7) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        // TopLeft
        for (int i = pos - 9; i % 8 < 7 && i >= 0; i -= 9) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        // BottomRight
        for (int i = pos + 9; i % 8 > 0 && i < board.length; i += 9) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        // BottomLeft
        for (int i = pos + 7; i % 8 < 7 && i < board.length; i += 7) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        return moves;
    }

    /**
     * Gets All Rook-Moves from a specific position. Also gets the color of the Rook needed for this on its own.
     * In this method it is not yet checked, if as the result of the move the players king gets in check.
     * @param board Current Board
     * @param pos The Position of the Rook.
     * @return A List of all possible moves.
     */
    private static List<Integer> getRookMoves(int[] board, int pos) {
        List<Integer> moves = new ArrayList<>();
        int player = board[pos] & MASK_PLAYER;
        for (int i = pos + 1; i % 8 > 0; i++) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        for (int i = pos - 1; i % 8 < 7 && i >= 0; i--) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        for (int i = pos - 8; i >= 0; i -= 8) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        for (int i = pos + 8; i < board.length; i += 8) {
            if ((board[i] & MASK_PLAYER) != player || (board[i] & MASK_SET_FIELD) == 0)
                moves.add(getMove(pos, i));
            if ((board[i] & MASK_SET_FIELD) > 0)
                break;
        }
        return moves;
    }

    /**
     * Gets All Pawn-Moves from a specific position. Also gets the color of the Pawn needed for this on its own.
     * In this method it is not yet checked, if as the result of the move the players king gets in check.
     * @param board Current Board
     * @param pos The Position of the Pawn
     * @return A List of all possible moves.
     */
    private static List<Integer> getPawnMoves(int[] board, int pos) {
        List<Integer> moves = new ArrayList<>();
        int player = board[pos] & MASK_PLAYER;
        if (player == PLAYER_WHITE /* Bottom Player */) {
            if (pos - 8 >= 0) {
                if (pos % 8 > 0) {
                    if ((board[pos - 9] & MASK_SET_FIELD) > 0 && (board[pos - 9] & MASK_PLAYER) != player) {
                        moves.add(getMove(pos, pos - 9));
                    } else if ((board[pos - 1] & MASK_SET_FIELD) > 0 && (board[pos - 1] & MASK_PLAYER) != player && (board[pos - 1] & MASK_PAWN_DOUBLE_JUMP) > 0) {
                        moves.add(getMove(pos, pos - 9));
                    }
                }
                if (pos % 8 < 7) {
                    if ((board[pos - 7] & MASK_SET_FIELD) > 0 && (board[pos - 7] & MASK_PLAYER) != player) {
                        moves.add(getMove(pos, pos - 7));
                    } else if ((board[pos + 1] & MASK_SET_FIELD) > 0 && (board[pos + 1] & MASK_PLAYER) != player && (board[pos + 1] & MASK_PAWN_DOUBLE_JUMP) > 0) {
                        moves.add(getMove(pos, pos - 7));
                    }
                }
                if ((board[pos - 8] & MASK_SET_FIELD) == 0) {
                    if ((board[pos] & MASK_HAS_MOVED) == 0 && pos - 16 >= 0 && (board[pos - 16] & MASK_SET_FIELD) == 0)
                        moves.add(getMove(pos, pos - 16));
                    moves.add(getMove(pos, pos - 8));
                }
            }
        } else {
            if (pos + 8 < board.length) {
                if (pos % 8 > 0) {
                    if ((board[pos + 7] & MASK_SET_FIELD) > 0 && (board[pos + 7] & MASK_PLAYER) != player) {
                        moves.add(getMove(pos, pos + 7));
                    } else if ((board[pos - 1] & MASK_SET_FIELD) > 0 && (board[pos - 1] & MASK_PLAYER) != player && (board[pos - 1] & MASK_PAWN_DOUBLE_JUMP) > 0) {
                        moves.add(getMove(pos, pos + 7));
                    }
                }
                if (pos % 8 < 7) {
                    if ((board[pos + 9] & MASK_SET_FIELD) > 0 && (board[pos + 9] & MASK_PLAYER) != player) {
                        moves.add(getMove(pos, pos + 9));
                    }else if ((board[pos + 1] & MASK_SET_FIELD) > 0 && (board[pos + 1] & MASK_PLAYER) != player && (board[pos + 1] & MASK_PAWN_DOUBLE_JUMP) > 0) {
                        moves.add(getMove(pos, pos + 9));
                    }
                }
                if ((board[pos + 8] & MASK_SET_FIELD) == 0) {
                    if ((board[pos] & MASK_HAS_MOVED) == 0 && pos + 16 < board.length && (board[pos + 16] & MASK_SET_FIELD) == 0)
                        moves.add(getMove(pos, pos + 16));
                    moves.add(getMove(pos, pos + 8));
                }
            }
        }
        return moves;
    }

    /**
     * Gets All Queen-Moves from a specific position. Also gets the color of the Queen needed for this on its own.
     * In this method it is not yet checked, if as the result of the move the players king gets in check.
     * @param board Current Board
     * @param pos The Position of the Queen.
     * @return A List of all possible moves.
     */
    private static List<Integer> getQueenMoves(int[] board, int pos) {
        List<Integer> moves = new ArrayList<>();
        moves.addAll(getRookMoves(board, pos));
        moves.addAll(getBishopMoves(board, pos));
        return moves;
    }

    /**
     * Gets All King-Moves from a specific position. Also gets the color of the King needed for this on its own.
     * In this method it is not yet checked, if as the result of the move the players king gets in check. Also, it is not checked if the king is near the other players king as a result.
     * @param board Current Board
     * @param pos The Position of the Knight
     * @return A List of all possible moves.
     */
    private static List<Integer> getKingMoves(int[] board, int pos) {
        List<Integer> moves = new ArrayList<>();
        int player = board[pos] & MASK_PLAYER;
        boolean inCheck = false;
        boolean checkedForCheck = false;
        // Castling
        if ((board[pos] & MASK_HAS_MOVED) == 0) {
            int leftRookPos = ((int) Math.floor(pos / 8f)) * 8;
            int rightRookPos = ((int) Math.ceil(pos / 8f)) * 8 - 1;
            if ((board[leftRookPos] & MASK_SET_FIELD) > 0 && (board[leftRookPos] & MASK_HAS_MOVED) == 0) {
                // Castling with Left Rook
                boolean castlingAllowed = true;
                for (int i = pos - 1; i > leftRookPos; i--) {
                    if ((board[i] & MASK_SET_FIELD) > 0) {
                        castlingAllowed = false;
                        break;
                    }
                }
                if (castlingAllowed) {
                    checkedForCheck = true;
                    if (pieceInCheck(board, pos, player)) {
                        castlingAllowed = false;
                        inCheck = true;
                    }
                    if (castlingAllowed) {
                        for (int i = pos - 1; i > leftRookPos; i--) {
                            if (pieceInCheck(board, i, player)) {
                                castlingAllowed = false;
                                break;
                            }
                        }
                    }
                    if (castlingAllowed)
                        moves.add(getMove(pos, pos - 2));
                }
            }
            if (!inCheck && (board[rightRookPos] & MASK_SET_FIELD) > 0 && (board[rightRookPos] & MASK_HAS_MOVED) == 0) {
                // Castling with Right Rook
                boolean castlingAllowed = true;
                for (int i = pos + 1; i < rightRookPos; i++) {
                    if ((board[i] & MASK_SET_FIELD) > 0) {
                        castlingAllowed = false;
                        break;
                    }
                }
                if (castlingAllowed) {
                    if (!checkedForCheck && pieceInCheck(board, pos, player)) {
                        castlingAllowed = false;
                        //inCheck = true;
                    }
                    if (castlingAllowed) {
                        for (int i = pos + 1; i < rightRookPos; i++) {
                            if (pieceInCheck(board, i, player)) {
                                castlingAllowed = false;
                                break;
                            }
                        }
                    }
                    if (castlingAllowed)
                        moves.add(getMove(pos, pos + 2));
                }
            }
        }

        // Normal King Moves
        for (int i = pos - 8; i <= pos + 8; i += 8) {
            if (i < 0 || i >= board.length)
                continue;
            for (int j = -1; j <= 1; j++) {
                if ((j == -1 && pos % 8 > 0) || (j == 1 && pos % 8 < 7) || j == 0) {
                    if ((board[i + j] & MASK_SET_FIELD) == 0 || (board[i + j] & MASK_PLAYER) != player)
                        moves.add(getMove(pos, i + j));
                }
            }
        }
        return moves;
    }

    // Check for Moves Methods:

    /**
     * Searches through the board-Array and finds the specific index, where the needed piece is located.
     * Commonly used to find the players king on the board.
     * @param board Current Board-Array
     * @param player The color of the needed piece
     * @param piece The piece-type (specified in MASK_PIECE)
     * @return The position of the searched piece. If there are multiple, the method returns the first one it finds. Returns -1 if no Piece is found.
     */
    public static int findPiecePos(int[] board, int player, int piece) {
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & MASK_SET_FIELD) > 0
                    && (board[i] & MASK_PLAYER) == player
                    && (board[i] & MASK_PIECE) == piece) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if a field is under attack. Commonly used for king but can be used for all other pieces as well.
     * @param board The current board which needs to be checked.
     * @param pos The position of the piece (mostly king).
     * @param player The pieces color. Is used to get the enemy-player.
     * @return Whether the piece is being attacked by the enemy or not.
     */
    public static boolean pieceInCheck(int[] board, int pos, int player) {

        // Check Horizontal Line (Rooks or Queens)
        for (int i = pos + 1; i % 8 > 0; i++) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_ROOK || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }
        for (int i = pos - 1; i % 8 < 7 && i >= 0; i--) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_ROOK || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }

        // Check Vertical Line (Rooks or Queens)
        for (int i = pos + 8; i < board.length; i += 8) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_ROOK || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }
        for (int i = pos - 8; i >= 0; i -= 8) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_ROOK || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }

        // Check Diagonals          (Bishops or Queens)
        // Check DiagonalTopRight
        for (int i = pos - 7; i % 8 > 0; i -= 7) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_BISHOP || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }
        // Check DiagonalTopLeft
        for (int i = pos - 9; i % 8 < 7 && i >= 0; i -= 9) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_BISHOP || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }
        // Check DiagonalBottomRight
        for (int i = pos + 9; i % 8 > 0 && i < board.length; i += 9) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_BISHOP || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }
        // Check DiagonalBottomLeft
        for (int i = pos + 7; i % 8 < 7 && i < board.length; i += 7) {
            if ((board[i] & MASK_SET_FIELD) > 0) {
                if ((board[i] & MASK_PLAYER) != player && ((board[i] & MASK_PIECE) == PIECE_BISHOP || (board[i] & MASK_PIECE) == PIECE_QUEEN)) {
                    return true;
                } else {
                    break;
                }
            }
        }

        // Check for Knights
        if (pos % 8 < 7) {
            // Piece not at Right Side of Board
            if (pos - 15 >= 0 && (board[pos - 15] & MASK_PLAYER) != player && (board[pos - 15] & MASK_PIECE) == PIECE_KNIGHT)
                return true;
            if (pos + 17 < board.length && (board[pos + 17] & MASK_PLAYER) != player && (board[pos + 17] & MASK_PIECE) == PIECE_KNIGHT)
                return true;
            if (pos % 8 < 6) {
                // Two Columns to Right Side of Board
                if (pos - 6 >= 0 && (board[pos - 6] & MASK_PLAYER) != player && (board[pos - 6] & MASK_PIECE) == PIECE_KNIGHT)
                    return true;
                if (pos + 10 < board.length && (board[pos + 10] & MASK_PLAYER) != player && (board[pos + 10] & MASK_PIECE) == PIECE_KNIGHT)
                    return true;
            }
        }
        if (pos % 8 > 0) {
            // Piece not at Left Side of Board
            if (pos - 17 >= 0 && (board[pos - 17] & MASK_PLAYER) != player && (board[pos - 17] & MASK_PIECE) == PIECE_KNIGHT)
                return true;
            if (pos + 15 < board.length && (board[pos + 15] & MASK_PLAYER) != player && (board[pos + 15] & MASK_PIECE) == PIECE_KNIGHT)
                return true;
            if (pos % 8 > 1) {
                // Two Columns to Right Side of Board
                if (pos - 10 >= 0 && (board[pos - 10] & MASK_PLAYER) != player && (board[pos - 10] & MASK_PIECE) == PIECE_KNIGHT)
                    return true;
                if (pos + 6 < board.length && (board[pos + 6] & MASK_PLAYER) != player && (board[pos + 6] & MASK_PIECE) == PIECE_KNIGHT)
                    return true;
            }
        }

        // Check for Pawns
        if (player == PLAYER_WHITE) {
            if (pos - 8 >= 0) {
                if (pos % 8 > 0 && (board[pos - 9] & MASK_PLAYER) != player && (board[pos - 9] & MASK_PIECE) == PIECE_PAWN)
                    return true;
                if (pos % 8 < 7 && (board[pos - 7] & MASK_PLAYER) != player && (board[pos - 7] & MASK_PIECE) == PIECE_PAWN)
                    return true;
            }
        } else {
            if (pos + 8 < board.length) {
                if (pos % 8 > 0 && (board[pos + 7] & MASK_PLAYER) != player && (board[pos + 7] & MASK_PIECE) == PIECE_PAWN)
                    return true;
                if (pos % 8 < 7 && (board[pos + 9] & MASK_PLAYER) != player && (board[pos + 9] & MASK_PIECE) == PIECE_PAWN)
                    return true;
            }
        }

        // Check for Opponent-King
        for (int i = pos - 8; i <= pos + 8; i += 8) {
            if (i < 0 || i >= board.length)
                continue;
            for (int j = -1; j <= 1; j++) {
                if ((j == -1 && pos % 8 > 0) || (j == 1 && pos % 8 < 7) || j == 0) {
                    if ((board[i + j] & MASK_SET_FIELD) > 0 && (board[i + j] & MASK_PIECE) == PIECE_KING && (board[i + j] & MASK_PLAYER) != player)
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * <p>Checks if the move results in the king being in check, making it illegal to play.</p>
     * <p>Used in getLegalMoves() in order to filter certain moves.</p>
     * @param board Board-Array
     * @param move The moves which needs to be checked.
     * @return Whether this move is legal to play or not.
     */
    public static boolean moveIllegalDueToSelfCheck(int[] board, int move) {
        /*
            Tests if taking the move,  lets the players king in check

            Current player is known due to which piece moves in 'move'
            board is NOT Copied!!! It is being altered and then changed back!!!
        */

        int player = board[getMoveOldPos(move)] & MASK_PLAYER;

        int[] boardCopy = board.clone();
        makeMove(boardCopy, move);
        int playersKingPos = findPiecePos(boardCopy, player, PIECE_KING);
        return pieceInCheck(boardCopy, playersKingPos, player);
    }


    /**
     * Checks whether a move is legal. Player parameter not needed. Current player can be concluded from 'move'-parameter.
     * Is used to check if a player-Input is a legal move.
     * @param board Current Board
     * @param move The Move that needs to be checked
     * @return current Move is legal to play
     */
    public static boolean moveIsLegal(int[] board, int move) {
        if ((board[getMoveOldPos(move)] & MASK_SET_FIELD) == 0)
            return false;
        int player = board[getMoveOldPos(move)] & MASK_PLAYER;
        List<Integer> moves = getLegalMoves(board, player);
        return moves.contains(move);
    }

    /**
     * Checks if there are no legal moves left. Used in Game-Loop in order to check for Game-Over.
     * @param board Current Board.
     * @param player Player who needs to take next move.
     * @return Returns true if there are no more moves the player could play.
     */
    public static boolean noLegalMovesLeft(int[] board, int player) {
        List<Integer> moves = getLegalMoves(board, player);
        return moves.size() == 0;
    }

    /**
     * Used in Game-Loop after noLegalMovesLeft() in order to differentiate between a checkmate and a stalemate.
     * @param board Current Board
     * @param player Player
     * @return Whether the player is in check or not.
     */
    public static boolean playerInCheck(int[] board, int player) {
        int king = findPiecePos(board, player, PIECE_KING);
        return pieceInCheck(board, king, player);
    }

    /**
     * <p>Returns cost of a specific piece, for example 10 for Queen, 1 for Pawn etc.</p>
     * <p>Also the value is negative if the pieces color is black</p>
     * @param piece The input needs to be the cell-value from the board array. NOT masked with MASK_PIECE.
     * @return Cost of the piece.
     */
    public static float getCost(int piece) {
        if ((piece & MASK_SET_FIELD) == 0)
            return 0;
        float cost = 0;
        switch (piece & MASK_PIECE) {
            case PIECE_PAWN -> cost = 1;
            case PIECE_KNIGHT -> cost = 2.5f;
            case PIECE_BISHOP -> cost = 3;
            case PIECE_ROOK -> cost = 5;
            case PIECE_QUEEN -> cost = 10;
            case PIECE_KING -> cost = 200;
        }
        return (piece & MASK_PLAYER) == PLAYER_WHITE ? cost : -cost;
    }

    /**
     * <p>This is a debugging-Method only. It prints all the moves inputted to it in the Form:</p>
     * <p>"pos1 -> pos2"</p>
     * @param moves List of moves to be printed
     */
    public static void printMoves(List<Integer> moves) {
        System.out.println("Moves: ");
        for (int i = 0; i < moves.size(); i++) {
            System.out.println((i + 1) + ")  " + getMoveOldPos(moves.get(i)) + " -> " + getMoveNewPos(moves.get(i)));
        }
    }

    /**
     * Counts the total number of pieces on the board (both Players)
     * @param board The current board-Array
     * @return Total number of pieces on the board
     */
    public static int countPieces(int[] board) {
        int counter = 0;
        for (int cell : board) {
            if ((cell & MASK_SET_FIELD) > 0 )
                counter++;
        }
        return counter;
    }

    /**
     * Counts the number of the left pieces of one side.
     * @param board The current board-Array
     * @param player The player
     * @return The number of pieces
     */
    public static int countPieces(int[] board, int player) {
        int counter = 0;
        for (int cell : board) {
            if ((cell & MASK_SET_FIELD) > 0 && (cell & MASK_PLAYER) == player)
                counter++;
        }
        return counter;
    }

}
