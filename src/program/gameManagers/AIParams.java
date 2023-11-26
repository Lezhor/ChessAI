package program.gameManagers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AIParams {

    private final ParamRange[] params;

    private AIParams() {
        params = new ParamRange[0];
    }

    private AIParams(List<ParamRange> paramList) {
        this.params = paramList.toArray(ParamRange[]::new);
        resetParams();
    }

    public double[] getNextParams() {
        if (doneIterating()) {
            throw new IllegalStateException("Done iterating...");
        }
        iterateParams();
        return Arrays.stream(params).mapToDouble(ParamRange::getValue).toArray();
    }

    private void iterateParams() {
        for (ParamRange param : params) {
            if (param.isIterating()) {
                if (param.reachedEnd()) {
                    param.resetValue();
                } else {
                    param.increaseValue();
                    break;
                }
            }
        }

    }

    public boolean doneIterating() {
        for (ParamRange param : params) {
            if (param.isIterating() && !param.reachedEnd()) {
                return true;
            }
        }
        return false;
    }

    public void resetParams() {
        Arrays.stream(params).forEach(ParamRange::resetValue);
    }

    public static class Builder {

        private final List<ParamRange> params;

        public Builder() {
            params = new LinkedList<>();
        }

        public Builder addParam(ParamRange param) {
            params.add(param);
            return this;
        }

        public Builder addParam(double defaultValue) {
            params.add(new ParamRange(defaultValue, 1));
            return this;
        }

        public Builder addIteratingParam(double defaultValue, double spread, int divisions) {
            params.add(new ParamRange(defaultValue, spread, divisions));
            return this;
        }

        public AIParams build() {
            return new AIParams(params);
        }

    }

}
