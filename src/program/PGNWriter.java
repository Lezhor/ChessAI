package program;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PGNWriter {

    private final String DIRECTORY_PATH = "src/data/pgn2/";
    private String filePath = "";

    private String event = "?", site = "?", date = "?", white = "?", black = "?", result = "?";

    private String fileBody = "";
    private int moveCounter = 0;

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
     * @param fileName  The Filename, without the directory-path.
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
     * Converts the move into a SAN-Move (Standard Algebraic Notation)
     *
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
     * @return SAN-String of move.
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

}
