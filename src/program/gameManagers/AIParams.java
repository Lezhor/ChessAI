package program.gameManagers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AIParams {

    private final ParamRange[] params;
    private boolean noIterParams_DoneIterating = false;

    private AIParams() {
        params = new ParamRange[0];
    }

    private AIParams(List<ParamRange> paramList) {
        this.params = paramList.toArray(ParamRange[]::new);
        resetParams();
    }

    public double[] getParams() {
        Random r = new Random();
        return Arrays.stream(params).mapToDouble(ParamRange::getValue).map(d -> d * ( .975 + (r.nextDouble() / 20))).toArray();
    }

    public void iterateParams() {
        if (doneIterating()) {
            throw new IllegalStateException("Done iterating...");
        }
        boolean noneIterating = true;
        for (ParamRange param : params) {
            if (param.isIterating()) {
                noneIterating = false;
                if (param.reachedEnd()) {
                    param.resetValue();
                } else {
                    param.increaseValue();
                    break;
                }
            }
        }
        if (noneIterating) {
            noIterParams_DoneIterating = true;
        }
    }

    public boolean doneIterating() {
        boolean noneIterating = true;
        for (ParamRange param : params) {
            if (param.isIterating()) {
                noneIterating = false;
                if (param.reachedEnd()) {
                    return true;
                }
            }
        }
        if (noneIterating) {
            return noIterParams_DoneIterating;
        }
        return false;
    }

    public void resetParams() {
        noIterParams_DoneIterating = false;
        Arrays.stream(params).forEach(ParamRange::resetValue);
    }

    @Override
    public String toString() {
        return Arrays.stream(params).map(Object::toString).reduce("AIParams{ ", (a, b) -> a + b + "; ") + "}";
    }

    public static class Builder {

        private final List<ParamRange> params;

        public Builder(double... values) {
            params = new LinkedList<>();
            for (double v : values) {
                addParam(v);
            }
        }

        public Builder addParam(ParamRange param) {
            params.add(param);
            return this;
        }

        public Builder addParam(double defaultValue) {
            params.add(new ParamRange(defaultValue));
            return this;
        }

        public Builder addIteratingParam(double defaultValue, double min, double max, int divisions) {
            params.add(new ParamRange(defaultValue, min, max, divisions));
            return this;
        }

        public Builder enableIteratingOnParam(int paramIndex, double min, double max, int divisions) {
            params.get(paramIndex).enableIterating(divisions, min, max);
            return this;
        }

        public AIParams build() {
            return new AIParams(params);
        }

    }

}
