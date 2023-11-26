package program.gameManagers;

public class ParamRange {

    private double min, max;
    private final double defaultValue;
    private double currentValue;
    private int divisions;
    private int currentDivision;
    private boolean iterating;

    public ParamRange(double defaultValue, double spread) {
        this(defaultValue, spread, 0);
    }

    public ParamRange(double defaultValue, double spread, int divisions) {
        this.defaultValue = defaultValue;
        min = defaultValue - spread;
        max = defaultValue + spread;
        this.currentValue = defaultValue;
        if (divisions == 0) {
            disableIterating();
        } else {
            enableIterating(divisions);
        }
    }

    public void increaseValue() {
        if (!iterating) {
            return;
        }
        currentDivision++;
        if (currentDivision >= divisions) {
            currentValue = max;
        } else {
            currentValue = min + ((max - min) * (currentDivision / (double) divisions));
        }
    }

    public void resetValue() {
        if (iterating) {
            currentDivision = -1;
            increaseValue();
        }
    }

    public boolean reachedEnd() {
        return (iterating && currentDivision >= divisions);
    }

    public double getValue() {
        return currentValue;
    }

    public ParamRange enableIterating(int divisions) {
        if (!iterating) {
            this.divisions = divisions;
            iterating = true;
            resetValue();
        }
        return this;
    }

    public ParamRange disableIterating() {
        if (iterating) {
            divisions = 0;
            currentDivision = 0;
            currentValue = defaultValue;
            iterating = false;
        }
        return this;
    }

    public boolean isIterating() {
        return iterating;
    }
}
