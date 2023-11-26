package program.gameManagers;

import program.ChessRules;
import program.Game;
import program.guis.NoGui;
import program.players.ais.v2.AI2_v3;

public class AnalyzeAI3ParamsWithGames {

    private final static double[] DEFAULT_PARAMETERS = new double[]{
            4,      // Search Depth
            1,      // WeightPawnPos
            16,     // BiasPawnPosPieceCount
            1.3f,   // WeightKnightsPos
            .8f,    // WeightBishopPos
            1f,     // WeightRooksPos
            1.1f,   // WeightQueenPos
            .4f     // WeightCastlingBonus
    };
    public static void analyzeAIv3_WeightPosPawns() {
        AIParams iteratingParams = new AIParams.Builder(DEFAULT_PARAMETERS)
                .enableIteratingOnParam(1, 0, 3, 9)
                .build();


        playGamesAIv3("ai3/pawnPos/", DEFAULT_PARAMETERS, iteratingParams, 30, 3);
    }

    public static void analyzeWeightPosKnights() {
        AIParams iteratingParams = new AIParams.Builder(DEFAULT_PARAMETERS)
                .enableIteratingOnParam(3, 0.3, 3.8, 7)
                .build();
        playGamesAIv3("ai3/knightPos/", DEFAULT_PARAMETERS, iteratingParams, 30, 0);
    }

    private static void playGamesAIv3(String directory, double[] defaultParams, AIParams iteratingParams, int samplesPerIteration) {
        playGamesAIv3(directory, defaultParams, iteratingParams, samplesPerIteration, 0);
    }

    private static void playGamesAIv3(String directory, double[] defaultParams, AIParams iteratingParams, int samplesPerIteration, int skip) {
        iteratingParams.resetParams();
        System.out.println("Testing Params: " + iteratingParams + "\n");
        for (int i = 0; i < skip; i++) {
            iteratingParams.iterateParams();
            System.out.println("Skipping one param set...");
        }
        int directoryCount = 1 + skip;
        do {
            System.out.print("Changed Params: ");
            for (int i = 0; i < samplesPerIteration; i++) {
                try {
                    double[] otherParams = iteratingParams.getParams();
                    //System.out.println(otherParams[3]);
                    new Game(new AI2_v3(ChessRules.PLAYER_WHITE, defaultParams), new AI2_v3(ChessRules.PLAYER_BLACK, otherParams), new NoGui(), directory + directoryCount + "/");
                    System.out.print("|");
                    i++;
                    new Game(new AI2_v3(ChessRules.PLAYER_WHITE, otherParams), new AI2_v3(ChessRules.PLAYER_BLACK, defaultParams), new NoGui(), directory + directoryCount + "/");
                    System.out.print("|");
                } catch (IllegalStateException e) {
                    System.out.print("_");
                    i--;
                }
            }
            iteratingParams.iterateParams();
            System.out.println();
            directoryCount++;

        } while (!iteratingParams.doneIterating());
        System.out.println("\n----------------------------------------\n");
    }


}
