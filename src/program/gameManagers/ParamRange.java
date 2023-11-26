package program.gameManagers;

public class ParamRange {

    private double min, max;
    private final double defaultValue;
    private double currentValue;
    private int divisions;
    private int currentDivision;
    private boolean iterating;

    public ParamRange(double defaultValue) {
        this(defaultValue, defaultValue, defaultValue, 0);
    }

    public ParamRange(double defaultValue, double min, double max, int divisions) {
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.currentValue = defaultValue;
        if (divisions == 0) {
            disableIterating();
        } else {
            enableIterating(divisions, min, max);
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

    public void enableIterating(int divisions, double min, double max) {
        this.divisions = divisions;
        this.min = min;
        this.max = max;
        iterating = true;
        resetValue();
    }

    public void disableIterating() {
        divisions = 0;
        currentDivision = 0;
        currentValue = defaultValue;
        iterating = false;
    }

    public boolean isIterating() {
        return iterating;
    }

    @Override
    public String toString() {
        if (iterating) {
            return String.format("%.2f", min) + ".." + String.format("%.2f", max);
        } else {
            return String.format("%.2f", currentValue);
        }
    }
}
