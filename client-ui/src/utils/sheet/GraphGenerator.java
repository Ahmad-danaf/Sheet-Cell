package utils.sheet;

import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.utils.RangeValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.cell.CellWrapper;
import utils.parameters.GraphParameters;

import java.util.ArrayList;
import java.util.List;

public class GraphGenerator {

    private final TableView<ObservableList<CellWrapper>> spreadsheetTableView;
    private final int maxRows;
    private final int maxColumns;

    public GraphGenerator(TableView<ObservableList<CellWrapper>> spreadsheetTableView, int maxRows, int maxColumns) {
        this.spreadsheetTableView = spreadsheetTableView;
        this.maxRows = maxRows;
        this.maxColumns = maxColumns;
    }

    // Method to create the graph configuration dialog
    public static Dialog<GraphParameters> createGraphConfigDialog() {
        Dialog<GraphParameters> dialog = new Dialog<>();
        dialog.setTitle("Create Graph");

        // Set the dialog buttons
        ButtonType createGraphButtonType = new ButtonType("Create Graph", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createGraphButtonType, ButtonType.CANCEL);

        // Create layout for the dialog
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        // Input for selecting X-axis range
        TextField xAxisRangeField = new TextField();
        xAxisRangeField.setPromptText("Enter X-axis range (e.g., A1..A10)");

        // Input for selecting Y-axis range
        TextField yAxisRangeField = new TextField();
        yAxisRangeField.setPromptText("Enter Y-axis range (e.g., B1..B10)");

        // Choice box for graph type (Bar or Line chart)
        ChoiceBox<String> chartTypeChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Line Chart", "Bar Chart"));
        chartTypeChoiceBox.setValue("Line Chart");

        dialogContent.getChildren().addAll(
                new Label("X-axis Range:"),
                xAxisRangeField,
                new Label("Y-axis Range:"),
                yAxisRangeField,
                new Label("Graph Type:"),
                chartTypeChoiceBox
        );

        dialog.getDialogPane().setContent(dialogContent);

        // Return the result when user clicks "Create Graph"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createGraphButtonType) {
                String xAxisRange = xAxisRangeField.getText();
                String yAxisRange = yAxisRangeField.getText();
                String chartType = chartTypeChoiceBox.getValue();
                return new GraphParameters(xAxisRange, yAxisRange, chartType);
            }
            return null;
        });

        return dialog;
    }

    public void generateGraph(GraphParameters params) {
        // Parse the ranges
        RangeValidator rangeValidator = new RangeValidator(maxRows, maxColumns);

        try {
            // Parse and validate X-axis range
            Coordinate[] xRange = rangeValidator.parseRange(params.getXAxisRange());
            validateRangeInOneColumn(xRange, "X");

            // Parse and validate Y-axis range
            Coordinate[] yRange = rangeValidator.parseRange(params.getYAxisRange());
            validateRangeInOneColumn(yRange, "Y");

            // Get X and Y values
            List<Double> xValues = getNumericValuesFromRange(xRange);
            List<Double> yValues = getNumericValuesFromRange(yRange);

            // Ensure X and Y ranges have the same number of values
            if (xValues.size() != yValues.size()) {
                showErrorPopup("X and Y ranges must have the same number of values.");
                return;
            }

            // Check if values are available for both axes
            if (xValues.isEmpty() || yValues.isEmpty()) {
                showErrorPopup("Both X and Y ranges must contain numeric values.");
                return;
            }

            // Create a chart based on the user's selection
            if (params.getChartType().equals("Line Chart")) {
                showLineChart(xValues, yValues);
            } else if (params.getChartType().equals("Bar Chart")) {
                showBarChart(xValues, yValues);
            }
        } catch (Exception e) {
            showErrorPopup("Invalid input: " + e.getMessage());
        }

    }

    // Helper to extract numeric values from a range
    public List<Double> getNumericValuesFromRange(Coordinate[] range) {
        List<Double> values = new ArrayList<>();
        for (int row = range[0].getRow(); row <= range[1].getRow(); row++) {
            for (int col = range[0].getColumn(); col <= range[1].getColumn(); col++) {
                CellWrapper cellWrapper = spreadsheetTableView.getItems().get(row).get(col);
                if (cellWrapper != null && cellWrapper.getCell() != null) {
                    EffectiveValue value = cellWrapper.getCell().getEffectiveValue();
                    if (value != null && value.getValue() instanceof Number) {
                        values.add(((Number) value.getValue()).doubleValue());
                    }
                }
            }
        }
        return values;
    }

    private void showLineChart(List<Double> xValues, List<Double> yValues) {
        // Create a LineChart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X-Axis");
        yAxis.setLabel("Y-Axis");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Line Chart");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < xValues.size(); i++) {
            series.getData().add(new XYChart.Data<>(xValues.get(i), yValues.get(i)));
        }
        lineChart.getData().add(series);

        displayChartInPopup(lineChart);
    }

    private void showBarChart(List<Double> xValues, List<Double> yValues) {
        // Create a BarChart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X-Axis");
        yAxis.setLabel("Y-Axis");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Bar Chart");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < xValues.size(); i++) {
            series.getData().add(new XYChart.Data<>(xValues.get(i).toString(), yValues.get(i)));
        }
        barChart.getData().add(series);

        displayChartInPopup(barChart);
    }

    // Utility method to display any chart in a popup
    private void displayChartInPopup(Chart chart) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Graph");

        VBox vbox = new VBox(chart);
        Scene scene = new Scene(vbox, 800, 600);

        popupStage.setScene(scene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    private void validateRangeInOneColumn(Coordinate[] range, String axisName) {
        if (range[0].getColumn() != range[1].getColumn()) {
            throw new IllegalArgumentException(axisName + "-axis range must be from a single column.");
        }
    }
    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
