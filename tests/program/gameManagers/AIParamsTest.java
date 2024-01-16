package program.gameManagers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AIParamsTest {

    @Test
    public void testIter1DivisionOnly() {
        double[] paramArray = new double[]{10, 5, 32};
        AIParams params = new AIParams.Builder(paramArray)
                .enableIteratingOnParam(1, 5, 5, 1)
                .build();

        assertFalse(params.doneIterating());
        assertArrayEquals(paramArray, params.getParams(), 1);

        params.iterateParams();
        assertTrue(params.doneIterating());
    }

    @Test
    public void testIterParamsNoIterating() {
        double[] paramArray = new double[]{10, 5, 32};
        AIParams params = new AIParams.Builder(paramArray)
                .build();

        assertFalse(params.doneIterating());
        assertArrayEquals(paramArray, params.getParams(), 1);

        params.iterateParams();
        assertTrue(params.doneIterating());
    }

}