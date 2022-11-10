package java;

import java.guis.*;
import java.players.HumanPlayer;
import java.players.ais.*;

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
        startGameAIvHuman();
    }

    /**
     * Starts an AIvsAI game. Both AIs are Random-Players.
     */
    public static void startGameRANDOMvRANDOM() {
        game = new Game(new AI_Random(ChessRules.PLAYER_WHITE), new AI_Random(ChessRules.PLAYER_BLACK), new TerminalUI());
    }

    /**
     * Starts an AIvsAI game. Both AIs are AIs of Version 1.0.
     */
    public static void startGameMINMAX1vMINMAX1() {
        game = new Game(new AI_MiniMax1(ChessRules.PLAYER_WHITE), new AI_MiniMax1(ChessRules.PLAYER_BLACK), new TerminalUI());
    }

    /**
     * Starts a HUMANvsAI game. (Human is White Player)
     */
    public static void startGameHumanvAI() {
        Gui gui = new TerminalUI();
        game = new Game(new HumanPlayer(ChessRules.PLAYER_WHITE, gui), new AI_MiniMax1(ChessRules.PLAYER_BLACK), gui);
    }

    /**
     * Starts a HUMANvAI game. (Human is Black Player)
     */
    public static void startGameAIvHuman() {
        Gui gui = new TerminalUI();
        game = new Game(new AI_MiniMax1(ChessRules.PLAYER_WHITE), new HumanPlayer(ChessRules.PLAYER_BLACK, gui), gui);
    }

    /**
     * Starts HUMANvsHUMAN game
     */
    public static void startGameHumanvHuman() {
        Gui gui = new TerminalUI();
        game =  new Game(new HumanPlayer(ChessRules.PLAYER_WHITE, gui), new HumanPlayer(ChessRules.PLAYER_BLACK, gui), gui);
    }

}
