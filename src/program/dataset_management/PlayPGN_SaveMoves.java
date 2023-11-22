package program.dataset_management;

import program.ChessRules;
import program.guis.TerminalUI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayPGN_SaveMoves {

    private final String DIRECTORY_PATH = "src/data/dataset/";
    public final String PGN_FILE_NAME = "Database9mil.pgn";
    private final String FEN_FILE_NAME = "fenToMoves.txt";

    private BufferedReader pgnReader;

    private ArrayList<String> fenMoves;

    private File pgnFile, fenFile;

    public PlayPGN_SaveMoves() {
        try {
            initFiles();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            playGames(24251);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFiles() throws FileNotFoundException {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        pgnFile = new File(DIRECTORY_PATH + PGN_FILE_NAME);
        if (!pgnFile.exists()) {
            throw new FileNotFoundException("PGN File not found with path: '" + pgnFile.getPath() + "'");
        }
        pgnReader = new BufferedReader(new FileReader(pgnFile));
        fenFile = new File(DIRECTORY_PATH + FEN_FILE_NAME);
        fenMoves = new ArrayList<>();
    }

    private void playGames(int amount) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fenFile));
        for (int i = 0; i < amount; i++) {
            ArrayList<String> newFens = gameLoop(7);
            for (String fen : newFens) {
                if (!fenMoves.contains(fen)) {
                    fenMoves.add(fen);
                    writer.write(fen+"\n");
                }
            }
        }
        writer.close();
    }

    /**
     * Plays game to a specific Turn-Number.
     *
     * @param turnAmount
     * @return String with all FEN-Boards, followed by the move the player chose.
     */
    private ArrayList<String> gameLoop(int turnAmount) throws IOException {
        int[] board = new int[64];
        initBottomRowPieces(board);
        initUpperRowPieces(board);
        ArrayList<String> fenString = new ArrayList<>();

        String currentLine;
        do {
            currentLine = pgnReader.readLine();
        } while (!currentLine.startsWith("1. "));
        String[] splitLine = currentLine.split(" ");
        int i = 0;
        int turn = 0;
        int currentPlayer = ChessRules.PLAYER_WHITE;
        while (turn < turnAmount) {
            if (i >= splitLine.length) {
                splitLine = pgnReader.readLine().split(" ");
                i = 0;
            } else if (splitLine[i].startsWith("{")) {
                return fenString;
            } else if (!splitLine[i].endsWith(".")) {
                try {
                    int move = getMoveFromPGN(splitLine[i], board, currentPlayer);
                    fenString.add(toFEN(board, currentPlayer) + " -> " + move);
                    ChessRules.makeMove(board, move);
                    currentPlayer = currentPlayer ^ ChessRules.MASK_PLAYER;
                    turn++;
                } catch (IllegalArgumentException e) {
                    return new ArrayList<String>();
                }
            }
            i++;
        }

        return fenString;
    }

    private String toFEN(int[] board, int currentPlayer) {
        String fen = "";
        int fieldsWithNoPieces = 0;
        for (int i = 0; i < board.length; i++) {
            if ((board[i] & ChessRules.MASK_SET_FIELD) > 0) {
                if (fieldsWithNoPieces != 0) {
                    fen += fieldsWithNoPieces;
                    fieldsWithNoPieces = 0;
                }
                String pieceLetter = switch (board[i] & ChessRules.MASK_PIECE) {
                    case ChessRules.PIECE_PAWN -> "p";
                    case ChessRules.PIECE_KNIGHT -> "n";
                    case ChessRules.PIECE_BISHOP -> "b";
                    case ChessRules.PIECE_ROOK -> "r";
                    case ChessRules.PIECE_QUEEN -> "q";
                    case ChessRules.PIECE_KING -> "k";
                    default -> "";
                };
                fen += (board[i] & ChessRules.MASK_PLAYER) == ChessRules.PLAYER_WHITE ? pieceLetter.toUpperCase() : pieceLetter.toLowerCase();
            } else {
                fieldsWithNoPieces++;
            }
            if (i % 8 == 7) {
                if (fieldsWithNoPieces != 0) {
                    fen += fieldsWithNoPieces;
                    fieldsWithNoPieces = 0;
                }
                if (i != board.length - 1) {
                    fen += "/";
                }
            }
        }
        fen += " " + (currentPlayer == ChessRules.PLAYER_WHITE ? "w" : "b");
        fen += " - - 0 0";
        return fen;
    }

    private int getMoveFromPGN(String pgnMove, int[] board, int currentPlayer) {
        if (pgnMove.equalsIgnoreCase("O-O") || pgnMove.equalsIgnoreCase("O-O-O")) {
            List<Integer> moves = ChessRules.getLegalMoves(board, currentPlayer);
            for (int move : moves) {
                if (ChessRules.isCastlingMove(board, move)) {
                    return move;
                }
            }
        } else {
            int destinationPos = pgn_getDestination(pgnMove);
            int row = pgn_getStartRow(pgnMove);
            int column = pgn_getStartColumn(pgnMove);
            int piece = pgn_getPiece(pgnMove);
            List<Integer> moves = ChessRules.getLegalMoves(board, currentPlayer);
            for (int move : moves) {
                if (ChessRules.getMoveNewPos(move) == destinationPos) {
                    if (piece == (board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_PIECE)
                            && (row == -1 || row == Math.floor(ChessRules.getMoveOldPos(move) / 8f))
                            && (column == -1 || column == ChessRules.getMoveOldPos(move) % 8)) {
                        return move;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Could not convert move to integer");
    }

    private int pgn_getDestination(String pgnMove) {
        return getPosFromString((pgnMove.endsWith("+")) ? pgnMove.substring(pgnMove.length() - 3, pgnMove.length() - 1) : pgnMove.substring(pgnMove.length() - 2));
    }

    private int pgn_getStartRow(String pgnMove) {
        String pgn = ((pgnMove.endsWith("+")) ? pgnMove.substring(0, pgnMove.length() - 3) : pgnMove.substring(0, pgnMove.length() - 2));
        pgn = pgn.replace("x", "");
        pgn = (!pgn.toUpperCase().equals(pgn)) ? pgn.substring(1) : pgn;
        pgn = pgn.replaceAll("\\D", "");
        if (pgn.equals("")) {
            return -1;
        }
        return 8 - Integer.parseInt(pgn);
    }

    private int pgn_getStartColumn(String pgnMove) {
        String pgn = ((pgnMove.endsWith("+")) ? pgnMove.substring(0, pgnMove.length() - 3) : pgnMove.substring(0, pgnMove.length() - 2));
        pgn = pgn.replace("x", "");
        pgn = (!pgn.toUpperCase().equals(pgn)) ? pgn.substring(1) : pgn;
        pgn = pgn.replaceAll("\\d", "");
        return switch (pgn) {
            case "a" -> 0;
            case "b" -> 1;
            case "c" -> 2;
            case "d" -> 3;
            case "e" -> 4;
            case "f" -> 5;
            case "g" -> 6;
            case "h" -> 7;
            default -> -1;
        };
    }

    private int pgn_getPiece(String pgnMove) {
        return switch (pgnMove.charAt(0)) {
            case 'N' -> ChessRules.PIECE_KNIGHT;
            case 'B' -> ChessRules.PIECE_BISHOP;
            case 'R' -> ChessRules.PIECE_ROOK;
            case 'Q' -> ChessRules.PIECE_QUEEN;
            case 'K' -> ChessRules.PIECE_KING;
            default -> ChessRules.PIECE_PAWN;
        };
    }

    public int getPosFromString(String stringPos) throws IllegalArgumentException {
        if (stringPos.length() != 2) {
            throw new IllegalArgumentException();
        }
        char char1 = stringPos.toLowerCase().charAt(0);
        int pos = switch (char1) {
            case 'a' -> 0;
            case 'b' -> 1;
            case 'c' -> 2;
            case 'd' -> 3;
            case 'e' -> 4;
            case 'f' -> 5;
            case 'g' -> 6;
            case 'h' -> 7;
            default -> -1;
        };
        if (pos == -1)
            throw new IllegalArgumentException();
        try {
            pos += (8 - Integer.parseInt(stringPos.substring(1))) * 8;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
        return pos;
    }

    private void initUpperRowPieces(int[] board) {
        int pawn = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_PAWN);
        int rook = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_ROOK);
        int knight = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KNIGHT);
        int bishop = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_BISHOP);
        int queen = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_QUEEN);
        int king = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_BLACK | ChessRules.PIECE_KING);
        board[0] = rook;
        board[1] = knight;
        board[2] = bishop;
        board[3] = queen;
        board[4] = king;
        board[5] = bishop;
        board[6] = knight;
        board[7] = rook;
        for (int i = 8; i < 16; i++) {
            board[i] = pawn;
        }
    }

    private void initBottomRowPieces(int[] board) {
        int pawn = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_PAWN);
        int rook = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_ROOK);
        int knight = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KNIGHT);
        int bishop = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_BISHOP);
        int queen = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_QUEEN);
        int king = (ChessRules.MASK_SET_FIELD | ChessRules.PLAYER_WHITE | ChessRules.PIECE_KING);
        board[56] = rook;
        board[57] = knight;
        board[58] = bishop;
        board[59] = queen;
        board[60] = king;
        board[61] = bishop;
        board[62] = knight;
        board[63] = rook;
        for (int i = 48; i < 56; i++) {
            board[i] = pawn;
        }
    }

}
