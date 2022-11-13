package program;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class PGNWriter {

    private final String directoryPath = "src/data/testpgn/";
    private String filePath = "";

    // Variables for PGN-File

    private String event = "?", site = "?", date = "?",
            round = "?", white = "?", black = "?", result = "?";

    public PGNWriter(String blackName, String whiteName) {
        String fileName = "Test";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            fileName = "Game" + (Objects.requireNonNull(new File(directoryPath).list()).length + 1);
        } catch (NullPointerException e) {
            System.out.println("Directory is Empty");
            fileName = "Game1";
        }
        new PGNWriter(fileName, blackName, whiteName);
    }

    /**
     * Initializes the Writer
     *
     * @param fileName  The Filename, without the directory-path.
     * @param blackName The name of the black player.
     * @param whiteName The name of the white player
     */
    public PGNWriter(String fileName, String blackName, String whiteName) {
        if (!fileName.endsWith(".pgn")) {
            fileName = fileName + ".pgn";
        }
        setFilePath(fileName);
        black = blackName;
        white = whiteName;
        beginPGNFile();
    }

    public void beginPGNFile() {
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
            writer.write("[Round \"" + round + "\"]\n");
            writer.write("[White \"" + white + "\"]\n");
            writer.write("[Black \"" + black + "\"]\n");
            writer.write("[Result \"" + result + "\"]\n\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("PGN-File-Init failed with Filepath: \"" + filePath + "\"");
        }
    }

    /**
     * Converts the move into a SAN-Move (Standard Algebraic Notation)
     *
     * @param board The board-Array, the one BEFORE the move was taken. It is necessary in order to determine if the move is a capture or not.
     * @param move  The move-integer. ChessRules.getMoveOldPos(move) and ChessRules.getMoveNewPos(move) can be used on it.
     */
    public void addMoveToFile(int[] board, int move) {
        String san = getSAN(board, move);
        // Append the san-move to the file
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

        //The SAN (Standard Algebraic Notation) of the move needs to be calculated here

        return san;
    }

    /**
     * Sets the filePath to directoryPath + filename
     * @param fileName The Name of the file with .pgn ending.
     */
    private void setFilePath(String fileName) {
        filePath = directoryPath + fileName;
    }

}
