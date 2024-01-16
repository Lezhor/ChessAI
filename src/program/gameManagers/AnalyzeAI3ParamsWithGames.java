package program.gameManagers;

import program.ChessRules;
import program.Game;
import program.guis.NoGui;
import program.players.ais.v2.AI2_v3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /**
     * Plays 100 games with DEFAULT_PARAMETERS vs passed params. default params get a 5% spread
     *
     * @param directoryName directory-name, should NOT start with '/', end not end with '/'
     * @param params        parameters to play against
     */
    public static void analyzeAIv3_TestParams(String directoryName, double... params) {
        if (params.length != DEFAULT_PARAMETERS.length) {
            throw new IllegalArgumentException("Wrong number of params");
        }
        AIParams aiParams = new AIParams.Builder(DEFAULT_PARAMETERS).build();

        playGamesAIv3("ai3/" + directoryName + "/", params, aiParams, 1, 0);
    }

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

    public static void analyzeWeightPosBishop() {
        AIParams iteratingParams = new AIParams.Builder(DEFAULT_PARAMETERS)
                .enableIteratingOnParam(4, 0.2, 2.2, 9)
                .build();
        playGamesAIv3("ai3/bishopPos/", DEFAULT_PARAMETERS, iteratingParams, 50, 0);
    }

    public static void analyzeWeightPosRook() {
        AIParams iteratingParams = new AIParams.Builder(DEFAULT_PARAMETERS)
                .enableIteratingOnParam(5, 0.2, 2.2, 10)
                .build();
        playGamesAIv3("ai3/rookPos/", DEFAULT_PARAMETERS, iteratingParams, 50, 0);
    }

    public static void analyzeWeightQueenRook() {
        AIParams iteratingParams = new AIParams.Builder(DEFAULT_PARAMETERS)
                .enableIteratingOnParam(6, 0.2, 2.2, 10)
                .build();
        playGamesAIv3("ai3/queenPos/", DEFAULT_PARAMETERS, iteratingParams, 50, 0);
    }

    public static void analyzeWeightCastlingBonus() {
        AIParams iteratingParams = new AIParams.Builder(DEFAULT_PARAMETERS)
                .enableIteratingOnParam(7, 0, 1.4, 7)
                .build();
        playGamesAIv3("ai3/castlingBonus/", DEFAULT_PARAMETERS, iteratingParams, 50, 0);
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
        try (ExecutorService service = Executors.newFixedThreadPool(10)) {
            do {
                System.out.print("Changed Params: ");
                for (int i = 0; i < samplesPerIteration; i++) {
                    double[] otherParams = iteratingParams.getParams();

                    final int dirCount1 = directoryCount;
                    final int i1 = i + 1;
                    service.submit(() -> {
                        try {
                            new Game(new AI2_v3(ChessRules.PLAYER_WHITE, defaultParams), new AI2_v3(ChessRules.PLAYER_BLACK, otherParams), new NoGui(), directory + dirCount1 + "/iterBlack/", "Game" + i1 + ".pgn");
                            System.out.print("|");
                        } catch (IllegalStateException e) {
                            System.out.print("_");
                        }
                    });

                    final int dirCount2 = directoryCount;
                    final int i2 = i + 1;
                    service.submit(() -> {
                        try {
                            new Game(new AI2_v3(ChessRules.PLAYER_WHITE, otherParams), new AI2_v3(ChessRules.PLAYER_BLACK, defaultParams), new NoGui(), directory + dirCount2 + "/iterWhite/", "Game" + i2 + ".pgn");
                            System.out.print("|");
                        } catch (IllegalStateException e) {
                            System.out.print("_");
                        }
                    });
                }
                iteratingParams.iterateParams();
                System.out.println();
                directoryCount++;

            } while (!iteratingParams.doneIterating());
        }
        System.out.println("\n----------------------------------------\n");
    }


}
