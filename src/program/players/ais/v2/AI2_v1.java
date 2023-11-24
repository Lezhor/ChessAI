package program.players.ais.v2;

/**
 * Generation 2 of Chess AIs<br>
 * Version 1: Random
 *
 * @author Daniil
 */
public class AI2_v1 extends AI_MinmaxAbstract {
    public static String AI_NAME = "AIv2.1-Random";

    public AI2_v1(int player) {
        super(player, AI_NAME);
    }

    @Override
    public double analyzeBoard(int[] board) {
        return 0;
    }
}
