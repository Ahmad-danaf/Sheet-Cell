package utils.parameters;

public class GraphParameters {
    String xAxisRange;
    String yAxisRange;
    String chartType;

    public GraphParameters(String xAxisRange, String yAxisRange, String chartType) {
        this.xAxisRange = xAxisRange;
        this.yAxisRange = yAxisRange;
        this.chartType = chartType;
    }

    public String getXAxisRange() {
        return xAxisRange;
    }
    public String getYAxisRange() {
        return yAxisRange;
    }
    public String getChartType() {
        return chartType;
    }
}
