package utils.sheet;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.cell.CellRange;
import utils.cell.CellWrapper;
import utils.parameters.DynamicAnalysisParameters;

import java.util.List;
import java.util.Map;

public class SheetPopupUtils {

    public static void configureCellFactoryForPopup(TableColumn<ObservableList<CellWrapper>, CellWrapper> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CellWrapper cellWrapper, boolean empty) {
                super.updateItem(cellWrapper, empty);
                if (empty || cellWrapper == null || cellWrapper.getEffectiveValue() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    String value = cellWrapper.getEffectiveValue();
                    String displayText = value != null ? value : "";

                    // Handle boolean values
                    if (displayText.equals("true") || displayText.equals("false")) {
                        displayText = displayText.toUpperCase();
                    }

                    setText(displayText);
                    setGraphic(null);

                    // Apply cell styling
                    String style = cellWrapper.getStyle();
                    setStyle(style);
                }
            }
        });
    }

    public static void displaySortedDataInPopup(List<ObservableList<CellWrapper>> sortedData, CellRange range) {
        // Create a new TableView to display the sorted data
        TableView<ObservableList<CellWrapper>> sortedTableView = new TableView<>();
        sortedTableView.setEditable(false);

        // Add row number column
        TableColumn<ObservableList<CellWrapper>, Number> rowNumberCol = new TableColumn<>("#");
        rowNumberCol.setCellValueFactory(cellData -> {
            int originalRowIndex = cellData.getValue().get(0).getOriginalRow();
            return new ReadOnlyObjectWrapper<>(originalRowIndex + 1);
        });
        rowNumberCol.setSortable(false);
        rowNumberCol.setPrefWidth(50);
        sortedTableView.getColumns().add(rowNumberCol);

        // Create columns based on the range
        for (int colIndex = range.startCol; colIndex <= range.endCol; colIndex++) {
            String columnName = CoordinateFactory.convertIndexToColumnLabel(colIndex);
            TableColumn<ObservableList<CellWrapper>, CellWrapper> column = new TableColumn<>(columnName);

            final int col = colIndex - range.startCol;

            column.setCellValueFactory(cellData -> {
                ObservableList<CellWrapper> row = cellData.getValue();
                CellWrapper cellWrapper = row.get(col);
                return new ReadOnlyObjectWrapper<>(cellWrapper);
            });

            // Configure cell factory
            configureCellFactoryForPopup(column);

            // Disable sorting on the columns in the pop-up
            column.setSortable(false);

            sortedTableView.getColumns().add(column);
        }

        // Set the sorted data to the TableView
        sortedTableView.setItems(FXCollections.observableArrayList(sortedData));

        // Create a new Stage (window) to display the sorted TableView
        Stage popupStage = new Stage();
        popupStage.setTitle("Sorted Data");
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Add a close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popupStage.close());

        VBox vbox = new VBox(sortedTableView, closeButton);
        Scene scene = new Scene(vbox);

        popupStage.setScene(scene);
        popupStage.show();
    }


    public static void displayFilteredDataInPopup(List<ObservableList<CellWrapper>> filteredData, CellRange range) {
        // Create a new TableView to display the filtered data
        TableView<ObservableList<CellWrapper>> filteredTableView = new TableView<>();
        filteredTableView.setEditable(false);

        // Add row number column
        TableColumn<ObservableList<CellWrapper>, Number> rowNumberCol = new TableColumn<>("#");
        rowNumberCol.setCellValueFactory(cellData -> {
            int originalRowIndex = cellData.getValue().get(0).getOriginalRow();
            return new ReadOnlyObjectWrapper<>(originalRowIndex + 1);
        });
        rowNumberCol.setSortable(false);
        rowNumberCol.setPrefWidth(50);
        filteredTableView.getColumns().add(rowNumberCol);

        // Create columns based on the range
        for (int colIndex = range.startCol; colIndex <= range.endCol; colIndex++) {
            String columnName = CoordinateFactory.convertIndexToColumnLabel(colIndex);
            TableColumn<ObservableList<CellWrapper>, CellWrapper> column = new TableColumn<>(columnName);

            final int col = colIndex - range.startCol;

            column.setCellValueFactory(cellData -> {
                ObservableList<CellWrapper> row = cellData.getValue();
                if (col < row.size()) { // Ensure we don't go out of bounds
                    CellWrapper cellWrapper = row.get(col);
                    return new ReadOnlyObjectWrapper<>(cellWrapper);
                }
                return null;
            });

            // Configure cell factory
            SheetPopupUtils.configureCellFactoryForPopup(column);

            // Disable sorting on the columns in the pop-up
            column.setSortable(false);

            filteredTableView.getColumns().add(column);
        }



        filteredTableView.setItems(FXCollections.observableArrayList(filteredData));

        // Create a new Stage (window) to display the filtered TableView
        Stage popupStage = new Stage();
        popupStage.setTitle("Filtered Data");
        popupStage.initModality(Modality.APPLICATION_MODAL);

        // Add a close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popupStage.close());

        VBox vbox = new VBox(filteredTableView, closeButton);
        Scene scene = new Scene(vbox);

        popupStage.setScene(scene);
        popupStage.show();
    }


    public static void displayVersionInPopup(Map<String, Object> sheetData) {
        Platform.runLater(() -> {
            int versionNumber = ((Number) sheetData.get("currentVersion")).intValue();
            Stage versionStage = new Stage();
            versionStage.setTitle("Version " + versionNumber);

            TableView<ObservableList<CellWrapper>> versionTableView = new TableView<>();
            versionTableView.setEditable(false);
            int maxRows = ((Double) sheetData.get("maxRows")).intValue();
            int maxColumns = ((Double) sheetData.get("maxColumns")).intValue();
            Map<String, Map<String, String>> cellDataVersion = (Map<String, Map<String, String>>) sheetData.get("cellData");

            // Add row number column
            TableColumn<ObservableList<CellWrapper>, Number> rowNumberCol = new TableColumn<>("#");
            rowNumberCol.setCellValueFactory(cellData -> {
                int index = versionTableView.getItems().indexOf(cellData.getValue());
                return new ReadOnlyObjectWrapper<>(index + 1);
            });
            rowNumberCol.setSortable(false);
            rowNumberCol.setPrefWidth(50);
            versionTableView.getColumns().add(rowNumberCol);

            // Create columns for the sheet data
            for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                String columnName = CoordinateFactory.convertIndexToColumnLabel(colIndex);
                TableColumn<ObservableList<CellWrapper>, CellWrapper> column = new TableColumn<>(columnName);

                final int col = colIndex;
                column.setCellValueFactory(cellData -> {
                    ObservableList<CellWrapper> row = cellData.getValue();
                    CellWrapper cellWrapper = row.get(col);
                    return new ReadOnlyObjectWrapper<>(cellWrapper);
                });
                versionTableView.getColumns().add(column);
            }

            // Populate rows
            ObservableList<ObservableList<CellWrapper>> data = FXCollections.observableArrayList();
            for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
                ObservableList<CellWrapper> rowData = FXCollections.observableArrayList();
                for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                    String key = rowIndex + "," + colIndex;
                    Map<String, String> cellValues = cellDataVersion.get(key);
                    String originalValue = cellValues.get("originalValue");
                    String effectiveValue = cellValues.get("effectiveValue");
                    if (effectiveValue != null && (effectiveValue.equalsIgnoreCase("true") || effectiveValue.equalsIgnoreCase("false"))) {
                        effectiveValue = effectiveValue.toUpperCase();
                    }
                    int version = Integer.parseInt(cellValues.get("version"));
                    CellWrapper cellWrapper = new CellWrapper(originalValue, effectiveValue, version, rowIndex, colIndex);
                    rowData.add(cellWrapper);
                }
                data.add(rowData);
            }
            versionTableView.setItems(data);

            Scene versionScene = new Scene(new VBox(versionTableView));
            versionStage.setScene(versionScene);
            versionStage.show();
        });
    }

    public static void displayDynamicAnalysisInPopup(Map<Double, Map<String, Map<String, String>>> analysisData,
                                                     DynamicAnalysisParameters parameters) {
        Platform.runLater(() -> {
            // Create a new TableView for the analysis data
            TableView<ObservableList<CellWrapper>> analysisTableView = new TableView<>();
            analysisTableView.setEditable(false);

            // Add a row number column
            TableColumn<ObservableList<CellWrapper>, Number> rowNumberCol = new TableColumn<>("#");
            rowNumberCol.setCellValueFactory(cellData -> {
                int index = analysisTableView.getItems().indexOf(cellData.getValue());
                return new ReadOnlyObjectWrapper<>(index + 1);
            });
            rowNumberCol.setSortable(false);
            rowNumberCol.setPrefWidth(50);
            analysisTableView.getColumns().add(rowNumberCol);

            // Dynamically create columns based on maxColumns from parameters
            for (int colIndex = 0; colIndex < parameters.getMaxColumns(); colIndex++) {
                String columnName = CoordinateFactory.convertIndexToColumnLabel(colIndex);
                TableColumn<ObservableList<CellWrapper>, CellWrapper> column = new TableColumn<>(columnName);
                final int col = colIndex;
                column.setCellValueFactory(cellData -> {
                    ObservableList<CellWrapper> row = cellData.getValue();
                    CellWrapper cellWrapper = row.get(col);
                    return new ReadOnlyObjectWrapper<>(cellWrapper);
                });
                configureCellFactoryForPopup(column); // Apply cell factory
                column.setSortable(false);
                analysisTableView.getColumns().add(column);
            }

            // Create a slider to control the analysis steps
            Slider analysisSlider = new Slider(parameters.getMinValue(), parameters.getMaxValue(), parameters.getMinValue());
            analysisSlider.setShowTickLabels(true);
            analysisSlider.setShowTickMarks(true);
            analysisSlider.setMajorTickUnit(parameters.getStepSize());
            analysisSlider.setMinorTickCount(0);
            analysisSlider.setSnapToTicks(true);

            // Display the current slider value
            Label sliderValueLabel = new Label(String.format("Cell ID: %s, Current Value: %.2f", parameters.getCellId(), parameters.getMinValue()));

            // Initialize the table with data for the minimum slider value
            double initialSliderValue = parameters.getMinValue();
            Map<String, Map<String, String>> initialCellData = analysisData.get(initialSliderValue);
            if (initialCellData != null) {
                ObservableList<ObservableList<CellWrapper>> initialData = FXCollections.observableArrayList();
                for (int rowIndex = 0; rowIndex < parameters.getMaxRows(); rowIndex++) {
                    ObservableList<CellWrapper> rowData = FXCollections.observableArrayList();
                    for (int colIndex = 0; colIndex < parameters.getMaxColumns(); colIndex++) {
                        String key = rowIndex + "," + colIndex;
                        Map<String, String> cellValues = initialCellData.getOrDefault(key, Map.of());
                        String effectiveValue = cellValues.get("effectiveValue");
                        effectiveValue = effectiveValue != null && (effectiveValue.equalsIgnoreCase("true") || effectiveValue.equalsIgnoreCase("false"))
                                ? effectiveValue.toUpperCase()
                                : effectiveValue;
                        CellWrapper cellWrapper = new CellWrapper(null, effectiveValue, 0, rowIndex, colIndex);
                        rowData.add(cellWrapper);
                    }
                    initialData.add(rowData);
                }
                analysisTableView.setItems(initialData); // Set initial data for the TableView
            }

            // Set the listener to update the table view based on slider value
            analysisSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double selectedValue = newVal.doubleValue();
                sliderValueLabel.setText(String.format("Cell ID: %s, Current Value: %.2f", parameters.getCellId(), selectedValue));

                Map<String, Map<String, String>> cellData = analysisData.get(selectedValue);
                if (cellData != null) {
                    ObservableList<ObservableList<CellWrapper>> data = FXCollections.observableArrayList();
                    for (int rowIndex = 0; rowIndex < parameters.getMaxRows(); rowIndex++) {
                        ObservableList<CellWrapper> rowData = FXCollections.observableArrayList();
                        for (int colIndex = 0; colIndex < parameters.getMaxColumns(); colIndex++) {
                            String key = rowIndex + "," + colIndex;
                            Map<String, String> cellValues = cellData.getOrDefault(key, Map.of());
                            String effectiveValue = cellValues.get("effectiveValue");
                            effectiveValue = effectiveValue != null && (effectiveValue.equalsIgnoreCase("true") || effectiveValue.equalsIgnoreCase("false"))
                                    ? effectiveValue.toUpperCase()
                                    : effectiveValue;
                            CellWrapper cellWrapper = new CellWrapper(null, effectiveValue, 0, rowIndex, colIndex);
                            rowData.add(cellWrapper);
                        }
                        data.add(rowData);
                    }
                    analysisTableView.setItems(data); // Set new data for the TableView
                    analysisTableView.refresh();
                }
            });

            // Create a new stage for the analysis popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Dynamic Analysis");
            popupStage.initModality(Modality.APPLICATION_MODAL);

            // Close button for the popup
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> popupStage.close());

            // Layout the components in a VBox
            VBox vbox = new VBox(10, sliderValueLabel, analysisSlider, analysisTableView, closeButton);
            Scene scene = new Scene(vbox);
            popupStage.setScene(scene);
            popupStage.show();
        });
    }




}
