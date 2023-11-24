package program.guis;

import program.ChessRules;
import program.players.ais.v1.AI2_AlphaBeta;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A UI which uses the Console only to communicate with the User.
 */
public class TerminalUI extends Gui {

    public TerminalUI(int bottomPlayer) {
        super(bottomPlayer);
    }

    /**
     * Prints Board to the console
     * @param board Board-Array
     */
    @Override
    public void printBoard(int[] board) {
        for (int i = 0; i < board.length; i++) {
            if (i % 8 == 0) {
                System.out.print("\n   ");
                for (int j = 0; j < 15 * 8 + 1; j++) {
                    System.out.print("-");
                }
                System.out.println();
                if (bottomPlayer == ChessRules.PLAYER_WHITE) {
                    System.out.print((8 - (i / 8)) + "  | ");
                } else {
                    System.out.print(((i / 8) + 1) + "  | ");
                }
            }
            String pieceName = ChessRules.getPieceName(board[bottomPlayer == ChessRules.PLAYER_WHITE ? i : 63 - i]);
            for (int j = 0; j < Math.ceil((12 - pieceName.length()) / 2f); j++) {
                System.out.print(" ");
            }
            System.out.print(pieceName);
            for (int j = 0; j < Math.floor((12 - pieceName.length()) / 2f); j++) {
                System.out.print(" ");
            }
            System.out.print(" | ");
        }
        System.out.print("\n   ");
        for (int j = 0; j < 15 * 8 + 1; j++) {
            System.out.print("-");
        }
        String s = "              ";
        if (bottomPlayer == ChessRules.PLAYER_WHITE) {
            System.out.println("\n           " + "A" + s + "B" + s + "C" + s + "D" + s + "E" + s + "F" + s + "G" + s + "H");
        } else {
            System.out.println("\n           " + "H" + s + "G" + s + "F" + s + "E" + s + "D" + s + "C" + s + "B" + s + "A");
        }

        float score = 0f;
        for (int cell : board) {
            score += AI2_AlphaBeta.analyzeBoard(board);
        }
        System.out.println("\nScore: " + score);
    }

    /**
     * Prints winner to the console
     * @param player Player who won. As Specified in ChessRules.MASK_PLAYER
     */
    @Override
    public void printWinner(int player) {
        System.out.println("Game Finished - Player \"" + (player == ChessRules.PLAYER_WHITE ? "White" : "Black") + "\" won!");
    }

    /**
     * Prints to the console that the game has concluded in a draw.
     */
    @Override
    public void printStalemate() {
        System.out.println("Stalemate - The game is finished!!!");
    }

    /**
     * Gets the Input-Move used for the Human-Player
     * @param board Board-Array
     * @param player Player which needs to take a move
     * @return The chosen Move
     */
    @Override
    public int getInputMove(int[] board, int player) {
        int move = 0;
        boolean loop = true;
        while (loop) {
            System.out.print("Input Move: ");
            try {
                String input = getUserInput();
                if (input.equalsIgnoreCase("moves")) {
                    ChessRules.printMoves(ChessRules.getLegalMoves(board, player));
                    continue;
                }
                move = getMove(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Illegal Argument!");
                continue;
            }
            System.out.println("Move: " + Integer.toBinaryString(move));
            if ((board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_SET_FIELD) > 0 && (board[ChessRules.getMoveOldPos(move)] & ChessRules.MASK_PLAYER) != player) {
                System.out.println("Move is not legal!");
            } else if (ChessRules.moveIsLegal(board, move)) {
                loop = false;
            } else {
                System.out.println("Move is not legal!");
            }
        }
        return move;
    }

    /**
     * Used in getInputMove()
     * @return returns a String (e.g. "E2 E4") which needs to be than converted to the move-integer
     */
    private String getUserInput() {
        Scanner sc = new Scanner(System.in);
        String string = "";
        while (string.equalsIgnoreCase("")) {
            try {
                string = sc.nextLine();
            } catch (NoSuchElementException ignored) {
            }
        }
        System.out.println("String: '" + string + "'");

        return string;

    }

    // Getters

    /**
     * Converts a Move-String inputted by the Human-Player to a move-integer
     * @param input The String (e.g. "E2 E4")
     * @return The move-integer
     * @throws IllegalArgumentException In case the User made a wrong input e.g. a typo
     */
    public int getMove(String input) throws IllegalArgumentException {
        if (input.length() < 5)
            throw new IllegalArgumentException();
        String pos1 = input.substring(0, 2);
        String pos2 = input.substring(3, 5);
        return ChessRules.getMove(getPos(pos1), getPos(pos2));
    }

    /**
     * Converts an Input-String consisting of a letter followed by a number to an int-position on the board-array.
     * @param stringPos Input-String e.g. E2
     * @return The int-position
     * @throws IllegalArgumentException In case the Letters are not between A-H, the number is not between 1-8 or the string has not a length of two.
     */
    public int getPos(String stringPos) throws IllegalArgumentException {
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
}
