package utils.parameters;

public class DynamicAnalysisParameters {
    private final String cellId;
    private final double minValue;
    private final double maxValue;
    private final double stepSize;
    private final int maxRows;
    private final int maxColumns;

    public DynamicAnalysisParameters(String cellId, double minValue, double maxValue, double stepSize, int maxRows, int maxColumns) {
        this.cellId = cellId.toUpperCase();
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize;
        this.maxRows = maxRows;
        this.maxColumns = maxColumns;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getStepSize() {
        return stepSize;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public String getCellId() {
        return cellId;
    }
}
