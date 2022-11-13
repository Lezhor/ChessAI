package program;

import program.guis.*;
import program.guis.TerminalUI;
import program.players.HumanPlayer;
import program.players.Player;
import program.players.ais.*;

/**
 * <p>The Main-Class contains the Main-Method as well as other methods which start the game by creating a new Object of the game-Class with different players</p>
 */

public class Main {

    /**
     * The game object is saved here
     */
    public static Game game;

    /**
     * The main-method which calls the startGame()-Method.
     * @param args Args
     */
    public static void main(String[] args) {
        startTerminalGame(0, 2);
    }

    private static void startTerminalGame(int whitePlayer, int blackPlayer) {
        startGame(whitePlayer, blackPlayer, new TerminalUI(blackPlayer == 0 && whitePlayer != 0 ? ChessRules.PLAYER_BLACK : ChessRules.PLAYER_WHITE));
    }

    private static void startGame(int whitePlayer, int blackPlayer, Gui gui) {
        Player player1, player2;
        try {
            player1 = getPlayer(true, whitePlayer, gui);
            player2 = getPlayer(false, blackPlayer, gui);
            System.out.println("Starting Game: \"" + player1.PGN_NAME + "\" vs \"" + player2.PGN_NAME + "\"\n");
            game = new Game(player1, player2, gui);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Player getPlayer(boolean whitePlayer, int playerType, Gui gui) {
        int playerColor = whitePlayer ? ChessRules.PLAYER_WHITE : ChessRules.PLAYER_BLACK;
        return switch (playerType) {
            case 0 -> new HumanPlayer(playerColor, gui);
            case 1 -> new AI_MiniMax1(playerColor);
            case 2 -> new AI_MiniMax2(playerColor);
            default -> throw new IllegalArgumentException("Playertype: " + playerType + " not defined!");
        };
    }

}
