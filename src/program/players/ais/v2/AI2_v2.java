package program.players.ais.v2;

import program.ChessRules;

import java.util.Arrays;

/**
 * Generation 2 of Chess AIs<br>
 * Version 1: Random
 *
 * @author Daniil
 */
public class AI2_v2 extends AI_MinmaxAbstract {
    public static String AI_NAME = "AIv2.2-PieceCounting";

    public AI2_v2(int player) {
        super(player, AI_NAME);
    }

    @Override
    public double analyzeBoard(int[] board) {
        return Arrays.stream(board)
                .boxed()
                .map(ChessRules::getCost)
                .reduce(0f, Float::sum);
    }
}
