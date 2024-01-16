package program;

import program.gameManagers.AnalyzeAI3ParamsWithGames;
import program.guis.*;
import program.guis.TerminalUI;
import program.players.HumanPlayer;
import program.players.Player;
import program.players.ais.v1.*;
import program.players.ais.v2.*;

import java.util.ArrayList;
import java.util.List;

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
        //runNoUiGames(8);
        //startTerminalGame(AI2_v3::new, AI2_v3::new);
        switch (5) {
            case 0 -> AnalyzeAI3ParamsWithGames.analyzeWeightPosKnights(); // did
            case 1 -> AnalyzeAI3ParamsWithGames.analyzeWeightPosBishop(); // did
            case 2 -> AnalyzeAI3ParamsWithGames.analyzeWeightPosRook();
            case 3 -> AnalyzeAI3ParamsWithGames.analyzeWeightQueenRook();
            case 4 -> AnalyzeAI3ParamsWithGames.analyzeWeightCastlingBonus();
            //                                                                                      SEARCHDEPTH, PAWN, BIAS, KNIGHT, BISHOP, ROOK, QUEEN, CASTLING
            case 5 -> AnalyzeAI3ParamsWithGames.analyzeAIv3_TestParams("dennis_params", 4, 1, 16, 1.3f, .27f, 1.86f, 1.99f, .12f); // TODO: 16.01.2024 Add Dennis Params here
        }

    }

    private static void startTerminalGame(int whitePlayer, int blackPlayer) {
        startGame(whitePlayer, blackPlayer, new TerminalUI(blackPlayer == 0 && whitePlayer != 0 ? ChessRules.PLAYER_BLACK : ChessRules.PLAYER_WHITE));
    }

    private static void startTerminalGame(PlayerFactory<? extends Player> white, PlayerFactory<? extends Player> black) {
        Player whitePlayer = white.create(ChessRules.PLAYER_WHITE);
        Player blackPlayer = black.create(ChessRules.PLAYER_BLACK);
        game = new Game(whitePlayer, blackPlayer, new TerminalUI((blackPlayer instanceof HumanPlayer) && !(whitePlayer instanceof HumanPlayer) ? ChessRules.PLAYER_BLACK : ChessRules.PLAYER_WHITE));
    }

    @FunctionalInterface
    private interface PlayerFactory<T extends Player> {
        T create(int player);
    }


    private static void startNoGuiGame(int whitePlayer, int blackPlayer) {
        startGame(whitePlayer, blackPlayer, new NoGui());
    }

    private static void runNoUiGames(int amount) {
        List<Integer> halfMovesCount = new ArrayList<>();
        List<Integer> possibleMovesCount = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            System.out.println("Running Game " + i + ":");
            long time = System.currentTimeMillis();
            startNoGuiGame(3, 3);
            time = System.currentTimeMillis() - time;
            System.out.println("Time, the game took: " + (time / 1000f) + "\nTime per move: " + (time / 1000f / game.halfMoves));
            halfMovesCount.add(game.halfMoves);
            possibleMovesCount.addAll(game.possibleMoveCount);
        }
        System.out.println("Average moves per Game:          " + calculateAvarage(halfMovesCount));
        System.out.println("Average possible moves per Turn: " + calculateAvarage(possibleMovesCount));
    }

    private static void startGame(int whitePlayer, int blackPlayer, Gui gui) {
        Player player1, player2;
        try {
            player1 = getPlayer(true, whitePlayer, gui);
            player2 = getPlayer(false, blackPlayer, gui);
            System.out.println("Starting Game: \"" + player1.getPgnName() + "\" vs \"" + player2.getPgnName() + "\"\n");
            game = new Game(player1, player2, gui);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Player getPlayer(boolean whitePlayer, int playerType, Gui gui) {
        int playerColor = whitePlayer ? ChessRules.PLAYER_WHITE : ChessRules.PLAYER_BLACK;
        return switch (playerType) {
            case 0 -> new HumanPlayer(playerColor, gui);
            case 1 -> new AI1_MiniMax(playerColor);
            case 2 -> new AI2_AlphaBeta(playerColor);
            case 3 -> new AI3_PosAna(playerColor);
            case 21 -> new AI2_v1(playerColor);
            case 22 -> new AI2_v2(playerColor);
            case 23 -> new AI2_v3(playerColor);
            default -> throw new IllegalArgumentException("Playertype: " + playerType + " not defined!");
        };
    }

    private static double calculateAvarage(List<Integer> list) {
        double average = 0;
        for (int element : list) {
            average += element;
        }
        return average / list.size();
    }

}
