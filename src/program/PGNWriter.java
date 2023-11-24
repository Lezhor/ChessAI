package program;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class PGNWriter {

    /**
     * Ends with a '/'.
     */
    private final String DIRECTORY_PATH = "src/data/pgnv2/ai2/";

    /**
     * Gets set to DIRECTORY_PATH + fileName specified in Constructor
     */
    private String filePath = "";

    /**
     * Data in the beginning of the PGN-File
     */
    private String event = "?", site = "?", date = "?", white = "?", black = "?", result = "?";

    /**
     * The part of the file which contains the actual gameplay.
     */
    private String fileBody = "";

    /**
     * Used for printing the Turn-Numbers in the fileBody
     */
    private int moveCounter = 0;

    /**
     * Calls the PGNWriter(String)-Constructor
     * @throws FileAlreadyExistsException
     */
    public PGNWriter() throws FileAlreadyExistsException {
        String fileName = "";
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        int i = 0;
        do {
            i++;
            try {
                fileName = "Game" + (Objects.requireNonNull(new File(DIRECTORY_PATH).list()).length + i) + ".pgn";
            } catch (NullPointerException e) {
                System.out.println("Directory is Empty");
                fileName = "Game1.pgn";
            }
            filePath = DIRECTORY_PATH + fileName;
        } while (new File(filePath).exists());
        setDate();
    }

    /**
     * Initializes the Writer
     * @param fileName The Filename, without the directory-path.
     */
    public PGNWriter(String fileName) throws FileAlreadyExistsException {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!fileName.endsWith(".pgn")) {
            fileName = fileName + ".pgn";
        }
        filePath = DIRECTORY_PATH + fileName;
        if (new File(filePath).exists()) {
            throw new FileAlreadyExistsException("File '" + filePath + "' already exists!!!");
        }
        setDate();
    }

    /**
     * Creates new file specified in filePath (if it already exists it deletes it first!) And then writes all data it already got to the file. Even not finished games. Indeed it gets called every move.
     */
    public void writeDataToFile() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                if (file.exists())
                    throw new IOException("File could not be deleted");
            }
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write("[Event \"" + event + "\"]\n");
            writer.write("[Site \"" + site + "\"]\n");
            writer.write("[Date \"" + date + "\"]\n");
            writer.write("[White \"" + white + "\"]\n");
            writer.write("[Black \"" + black + "\"]\n");
            writer.write("[Result \"" + result + "\"]\n\n");
            writer.write(fileBody + (result.equalsIgnoreCase("?") ? "" : " " + result));
            writer.close();
        } catch (IOException e) {
            System.out.println("PGN-File-Init failed with Filepath: \"" + filePath + "\"");
        }
    }

    public PGNWriter setBlack(String black) {
        this.black = black;
        return this;
    }

    public PGNWriter setWhite(String white) {
        this.white = white;
        return this;
    }

    private void setDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDateTime today = LocalDateTime.now();
        date = dtf.format(today);
    }

    public PGNWriter setEvent(String event) {
        this.event = event;
        return this;
    }

    public PGNWriter setSite(String site) {
        this.site = site;
        return this;
    }

    public void setResultStaleMate() {
        result = "1/2-1/2";
    }

    public void setResultWinner(int winner) {
        if (winner == ChessRules.PLAYER_WHITE) {
            result = "1-0";
        } else {
            result = "0-1";
        }
    }

    /**
     * Converts the move into the SAN-Notation using the getSAN()-Method and afterwards saves the result in the fileBody-String
     * @param board The board-Array, the one BEFORE the move was taken. It is necessary in order to determine if the move is a capture or not.
     * @param move  The move-integer. ChessRules.getMoveOldPos(move) and ChessRules.getMoveNewPos(move) can be used on it.
     */
    public void addMoveToFile(int[] board, int move) {
        if (moveCounter % 12 == 0) {
            fileBody += "\n";
        } else {
            fileBody += " ";
        }
        if (moveCounter == 0) {
            fileBody = "1.";
        } else if (moveCounter % 2 == 0) {
            fileBody += ((moveCounter / 2) + 1) + ".";
        }
        String san = getSAN(board, move);
        fileBody += san;
        moveCounter++;
        writeDataToFile();
    }

    /**
     * Converts a move from the integer to the SAN-String.
     * @param board The board-Array before the move was played.
     * @param move The move-integer. ChessRules.getMoveOldPos(move) and ChessRules.getMoveNewPos(move) can be used on it.
     * @return SAN-Notation of move.
     */
    private String getSAN(int[] board, int move) {
        String san = "";
        int oldPos = ChessRules.getMoveOldPos(move);
        int newPos = ChessRules.getMoveNewPos(move);
        if ((board[oldPos] & ChessRules.MASK_PIECE) == ChessRules.PIECE_KING) {
            if (newPos - oldPos == 2) {
                return "O-O";
            } else if (oldPos - newPos == 2) {
                return "O-O-O";
            }
        }

        san += getPieceLetter(board[oldPos]);
        san += posToString(oldPos);
        if ((board[newPos] & ChessRules.MASK_SET_FIELD) > 0) {
            san += "x";
        }
        san += posToString(newPos);
        if ((board[oldPos] & ChessRules.MASK_PIECE) == ChessRules.PIECE_PAWN && ((int) Math.floor(newPos / 8f)) % 7 == 0) {
            san += "=Q";
        }
        return san;
    }

    /**
     * Converts a given Piece into the letter from SAN-Notation e.g. "B" for Bishop or "" for Pawn.
     * @param piece The Piece specified in ChessRules.MASK_PIECE
     * @return The SAN-Letter
     */
    private String getPieceLetter(int piece) {
        return switch (piece & ChessRules.MASK_PIECE) {
            default -> "";
            case ChessRules.PIECE_KNIGHT -> "N";
            case ChessRules.PIECE_BISHOP -> "B";
            case ChessRules.PIECE_ROOK -> "R";
            case ChessRules.PIECE_QUEEN -> "Q";
            case ChessRules.PIECE_KING -> "K";
        };
    }

    private String posToString(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException("Position negative");
        } else if (pos > 63) {
            throw new IndexOutOfBoundsException("Position greater than 63");
        }
        return switch (pos % 8) {
            case 0 -> "a";
            case 1 -> "b";
            case 2 -> "c";
            case 3 -> "d";
            case 4 -> "e";
            case 5 -> "f";
            case 6 -> "g";
            case 7 -> "h";
            default -> "";
        } + (8 - ((int) Math.floor(pos / 8f)));
    }

    public static int[] getBoardFromFen(String fen) throws IllegalArgumentException{
        int[] board = new int[64];
        String[] fenParts = fen.split(" ");
        if (fenParts.length != 6)
            throw new IllegalArgumentException("FEN-Notation has to consist of 6 Strings split by a space");
        String[] rows = fenParts[0].split("/");
        if (rows.length != 8)
            throw new IllegalArgumentException("Wrong number of Rows");
        int pos = 0;
        for (String row : rows) {
            for (String fenLetter : row.split("")) {
                int piece = getPieceFEN(fenLetter);
                if (piece == -1) {
                    try {
                        pos += Integer.parseInt(fenLetter);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                } else {
                    board[pos] = piece;
                    pos++;
                }
            }
        }
        return board;
    }
    public static int getPieceFEN(String fenLetter) {
        int ret =  switch (fenLetter.toUpperCase()) {
            case "P" -> ChessRules.PIECE_PAWN;
            case "N" -> ChessRules.PIECE_KNIGHT;
            case "B" -> ChessRules.PIECE_BISHOP;
            case "R" -> ChessRules.PIECE_ROOK;
            case "Q" -> ChessRules.PIECE_QUEEN;
            case "K" -> ChessRules.PIECE_KING;
            default -> -1;
        };
        if (ret == -1)
            return ret;
        if (fenLetter.equals(fenLetter.toUpperCase())) {
            ret = ret | ChessRules.PLAYER_WHITE;
        }
        return ret | ChessRules.MASK_SET_FIELD | ChessRules.MASK_HAS_MOVED;
    }

}
