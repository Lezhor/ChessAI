package program.players.ais.v2;

import org.junit.jupiter.api.Test;
import program.ChessRules;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AI_MinmaxAbstractTest {

    @Test
    public void generatePgnName() {
        AI_MinmaxAbstract ai = new AI_MinmaxAbstract(ChessRules.PLAYER_BLACK, "AI_Name") {
            @Override
            public double analyzeBoard(int[] board) {
                return 0;
            }
        };

        double[] params = new double[]{6, 0.4, 0.3, 0.8, 1.4};

        String result = ai.generatePgnName("AI_Name", Arrays.stream(params).boxed().toList());
        System.out.println("PgnName: " + result);

    }
}