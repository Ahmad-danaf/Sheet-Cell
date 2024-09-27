package desktop.sheet;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.utils.ColumnProperties;
import com.sheetcell.engine.utils.RangeValidator;
import com.sheetcell.engine.utils.RowProperties;
import desktop.utils.parameters.GraphParameters;
import desktop.utils.sheet.GraphGenerator;
import desktop.utils.sheet.SheetDisplayHelper;
import desktop.utils.sheet.SheetUtils;
import desktop.utils.cell.CellRange;
import desktop.utils.cell.CellWrapper;
import desktop.body.BodyController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;


import java.util.*;

public class SheetController {

    private Engine engine;
    @FXML private BodyController bodyController;
    @FXML
    private ScrollPane gridScrollPane;
    @FXML
    private TableView<ObservableList<CellWrapper>> spreadsheetTableView;
    private ObservableList<ObservableList<CellWrapper>> originalData;
    private SheetReadActions sheetData;

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @FXML
    private void initialize() {
        // Enable cell selection
        spreadsheetTableView.getSelectionModel().setCellSelectionEnabled(true);
        spreadsheetTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Add listener to focused cell property
        spreadsheetTableView.getFocusModel().focusedCellProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getRow() >= 0 && newVal.getColumn() >= 0) {
                int row = newVal.getRow();
                int column = newVal.getColumn();

                // Ignore the row number column
                if (column == 0) {
                    return;
                }

                // Adjust column index to account for row number column
                int dataColumnIndex = column - 1;

                // Get cell address
                String cellAddress = CoordinateFactory.convertIndexToColumnLabel(dataColumnIndex) + (row + 1);

                // Get the selected cell
                ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
                CellWrapper cellWrapper = rowData.get(dataColumnIndex);

                // Get original cell value
                Cell cell = cellWrapper.getCell();
                EffectiveValue effectiveValue = null;
                String originalValue ="";
                int version = 0;
                if (cell != null) {
                    effectiveValue = cell.getEffectiveValue();
                    originalValue= cell.getOriginalValue() != null ? cell.getOriginalValue().toString() : "";
                    version = cell.getVersion();
                }
                Object value = effectiveValue != null ? effectiveValue.getValue() : ""; //"" or null?

                String versionString = version!=0 ? "Version: " + String.valueOf(version) : "";

                // Notify BodyController about the selected cell
                if (bodyController != null) {
                    bodyController.updateSelectedCell(cellAddress, originalValue, versionString);
                }


                highlightPrecedentsAndDependents(row,dataColumnIndex);
            } else {
                // No cell selected; clear selection in BodyController
                if (bodyController != null) {
                    bodyController.clearSelectedCell();
                }

                // Clear highlights
                clearHighlights();
                spreadsheetTableView.refresh();
            }
        });
    }
    private void highlightPrecedentsAndDependents(int row, int column) {
        // Clear previous highlights
        clearHighlights();
        Cell selectedCell = sheetData.getCell(row, column);
        // Get precedents and dependents
        List<Cell> precedents = selectedCell!= null ? selectedCell.getInfluencedCells() : new ArrayList<>();
        List<Cell> dependents = selectedCell!= null ? selectedCell.getDependencies() : new ArrayList<>();

        // Highlight precedents in light green
        String precedentStyle = "-fx-background-color: lightgreen;";
        for (Cell precedent : precedents) {
            highlightCell(precedent, precedentStyle);
        }

        // Highlight dependents in light blue
        String dependentStyle = "-fx-background-color: lightblue;";
        for (Cell dependent : dependents) {
            highlightCell(dependent, dependentStyle);
        }
        // highlight even empty cells
        Set <Coordinate> emptydependents = engine.getDependenciesForCell(row, column);
        for (Coordinate emptydependent : emptydependents) {
            highlightEmptyCell(emptydependent.getRow(),emptydependent.getColumn(), dependentStyle);
        }
        Set<Coordinate> emptyprecedents = engine.getInfluencedForCell(row, column);
        for (Coordinate emptyprecedent : emptyprecedents) {
            highlightEmptyCell(emptyprecedent.getRow(),emptyprecedent.getColumn(), precedentStyle);
        }

        // Refresh the table to apply styles
        spreadsheetTableView.refresh();
    }

    private void highlightCell(Cell cell, String style) {
        if (cell == null) {
            return;
        }
        int row = cell.getCoordinate().getRow();
        int column = cell.getCoordinate().getColumn();

        // Get the cell wrapper and apply style
        ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
        CellWrapper cellWrapper = rowData.get(column);
        cellWrapper.setHighlightStyle(style);
    }

    private void highlightEmptyCell(int row,int column, String style) {
        // Get the cell wrapper and apply style
        ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
        CellWrapper cellWrapper = rowData.get(column);
        cellWrapper.setHighlightStyle(style);
    }

    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }

    public void refreshSpreadsheet() {
        // Clear and reload data
        sheetData= engine.getReadOnlySheet();
        displaySheet(sheetData);
    }

    public void reselectCell(int row, int column) {
        // Adjust column index to account for row number column
        int adjustedColumn = column + 1;
        spreadsheetTableView.getSelectionModel().clearSelection();
        spreadsheetTableView.getSelectionModel().select(row, spreadsheetTableView.getColumns().get(adjustedColumn));
    }

    public void displaySheet(SheetReadActions sheetData) {
        Platform.runLater(() -> {
            this.sheetData = sheetData;

            // Clear existing data
            spreadsheetTableView.getColumns().clear();
            spreadsheetTableView.getItems().clear();

            // Get sheet dimensions
            int maxRows = sheetData.getMaxRows();
            int maxColumns = sheetData.getMaxColumns();

            // Add row number column
            addRowNumberColumn();

            // Create data columns dynamically
            createColumns(maxColumns);

            // Populate rows
            populateRows(maxRows, maxColumns);
            adjustAllColumnWidth();
            adjustAllRowHeight();
        });
    }

    private void createColumns(int maxColumns) {
        for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
            String columnName = CoordinateFactory.convertIndexToColumnLabel(colIndex);
            TableColumn<ObservableList<CellWrapper>, CellWrapper> column = new TableColumn<>(columnName);
            final int col = colIndex;

            column.setCellValueFactory(cellData -> {
                ObservableList<CellWrapper> row = cellData.getValue();
                CellWrapper cellWrapper = row.get(col);
                return new ReadOnlyObjectWrapper<>(cellWrapper);
            });

            // Disable default sorting
            column.setSortable(false);

            // Configure cell factory with default alignment and wrapping
            configureCellFactory(column);

            // Allow users to adjust column width
            column.setResizable(false);

            // Add column to the table
            spreadsheetTableView.getColumns().add(column);
        }
    }

    private void populateRows(int maxRows, int maxColumns) {
        ObservableList<ObservableList<CellWrapper>> data = FXCollections.observableArrayList();

        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
            ObservableList<CellWrapper> rowData = FXCollections.observableArrayList();
            for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                Cell cell = sheetData.getCell(rowIndex, colIndex);
                CellWrapper cellWrapper = new CellWrapper(cell, rowIndex, colIndex);
                rowData.add(cellWrapper);
            }
            data.add(rowData);
        }

        // Store original data
        originalData = FXCollections.observableArrayList(data);

        spreadsheetTableView.setItems(data);
    }

    private void configureCellFactory(TableColumn<ObservableList<CellWrapper>, CellWrapper> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CellWrapper cellWrapper, boolean empty) {
                super.updateItem(cellWrapper, empty);

                // Ensure that the size and styles are applied regardless of cell being empty or not
                if (cellWrapper == null || empty) {
                    // Handle empty cell styling and size
                    applySizeAndStyles(cellWrapper);
                    setText(null);
                    setGraphic(null);
                } else {
                    // Apply size and styles to non-empty cell
                    applySizeAndStyles(cellWrapper);

                    // Get and display the value inside the cell
                    EffectiveValue effectiveValue = cellWrapper.getCell() != null ? cellWrapper.getCell().getEffectiveValue() : null;
                    String displayText = getDisplayText(effectiveValue);
                    // Apply alignment and padding
                    applyAlignment(cellWrapper);
                    setPadding(new Insets(0));  // Ensure there's no padding to push the text
                    setGraphic(null);  // Ensure no graphic is interfering
                    setText(displayText);
                    String currentStyle = getStyle();  // Get the existing style
                    String newStyle = cellWrapper.getStyle() + "-fx-text-overrun: ellipsis;";
                    setStyle(currentStyle + newStyle);
                }
            }

            // Helper method to apply size (width and height) and styles (background and text)
            private void applySizeAndStyles(CellWrapper cellWrapper) {
                if (cellWrapper != null) {
                    // Get column properties for width and height
                    ColumnProperties properties = engine.getColumnProperties(cellWrapper.getColumn());
                    RowProperties rowProperties = engine.getRowProperties(cellWrapper.getOriginalRow());
                    if (properties != null) {
                        int height = rowProperties.getHeight();
                        int width = properties.getWidth();

                        // Set size for all cells (empty or non-empty)
                        setMinHeight(height);
                        setPrefHeight(height);
                        setMaxHeight(height);
                        setMinWidth(width);
                        setPrefWidth(width);
                        setMaxWidth(width);
                    }

                    // Apply styles from CellWrapper
                    String fullStyle = cellWrapper.getStyle() + cellWrapper.getHighlightStyle();
                    applyStyles(fullStyle);
                }
            }

            // Helper method to apply styles
            private void applyStyles(String fullStyle) {
                String backgroundStyle = "";
                String textStyle = "";

                if (fullStyle != null && !fullStyle.isEmpty()) {
                    String[] styles = fullStyle.split(";");
                    for (String style : styles) {
                        style = style.trim();
                        if (style.startsWith("-fx-background-color")) {
                            backgroundStyle += style + ";";
                        } else if (style.startsWith("-fx-text-fill") || style.startsWith("-fx-fill")) {
                            textStyle += style + ";";
                        }
                    }
                }

                setStyle(backgroundStyle + textStyle);  // Apply the combined styles
            }

            // Helper method to return the display text
            private String getDisplayText(EffectiveValue effectiveValue) {
                Object value = effectiveValue != null ? effectiveValue.getValue() : null;
                String displayText = value != null ? value.toString() : "";

                // Handle boolean values
                if (effectiveValue != null && effectiveValue.getCellType() == CellType.BOOLEAN) {
                    displayText = displayText.toUpperCase();
                }
                return displayText;
            }

            // Helper method to apply text wrapping
            private void applyWrappedText(String textContent, TableColumn<ObservableList<CellWrapper>, CellWrapper> column) {
                Text text = new Text(textContent);
                text.wrappingWidthProperty().bind(column.widthProperty().subtract(10)); // Adjust based on column width
                text.setStyle(getStyle());  // Use the cell's existing style
                setGraphic(text);
                setText(null);  // Clear the text property since we're using the graphic
            }

            // Helper method to apply alignment
            private void applyAlignment(CellWrapper cellWrapper) {
                ColumnProperties properties = engine.getColumnProperties(cellWrapper.getColumn());
                if (properties != null) {
                    String alignment = properties.getAlignment();
                    Pos pos = Pos.CENTER; // Default alignment
                    if ("left".equalsIgnoreCase(alignment)) {
                        pos = Pos.CENTER_LEFT;
                    } else if ("right".equalsIgnoreCase(alignment)) {
                        pos = Pos.CENTER_RIGHT;
                    }
                    setAlignment(pos);  // Apply the alignment to the cell
                }
            }
        });
    }

    // Method to adjust row height
    public void adjustRowHeight(int rowIndex, int height) {
        spreadsheetTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ObservableList<CellWrapper> item, boolean empty) {
                super.updateItem(item, empty);

                // Check if the row index matches the selected row
                if (getIndex() == rowIndex) {
                    setMinHeight(height);
                    setPrefHeight(height);
                    setMaxHeight(height);
                }
            }
        });
        spreadsheetTableView.refresh();
    }


    private void clearHighlights() {
        for (ObservableList<CellWrapper> rowData : spreadsheetTableView.getItems()) {
            for (CellWrapper cellWrapper : rowData) {
                cellWrapper.setHighlightStyle("");
            }
        }
        spreadsheetTableView.refresh();
    }

    public void applyCellStyle(int row, int column, String style) {
        Platform.runLater(() -> {
            int adjustedColumn = column;
            ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
            CellWrapper cellWrapper = rowData.get(adjustedColumn);
            cellWrapper.setStyle(style);
            spreadsheetTableView.refresh();
        });
    }

    public void showSortedData(CellRange range, List<Integer> sortColumns) {
        Platform.runLater(() -> {
            ObservableList<ObservableList<CellWrapper>> data = spreadsheetTableView.getItems();
            SheetUtils.showSortedDataHalper(range, sortColumns, data);
    });
    }

    public List<String> getUniqueValuesInColumn(int columnIndex) {
        Set<String> uniqueValues = new HashSet<>();

        ObservableList<ObservableList<CellWrapper>> data = spreadsheetTableView.getItems();

        for (ObservableList<CellWrapper> row : data) {
            if (columnIndex >= 0 && columnIndex < row.size()) {
                CellWrapper cellWrapper = row.get(columnIndex);
                if (cellWrapper != null && cellWrapper.getCell() != null) {
                    EffectiveValue effectiveValue = cellWrapper.getCell().getEffectiveValue();
                    if (effectiveValue != null) {
                        String value = effectiveValue.getValue().toString();
                        if (value != null && !value.isEmpty() && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                            value=value.toUpperCase();
                        }
                        uniqueValues.add(value);
                    }
                }
            }
        }
        return new ArrayList<>(uniqueValues);
    }

    public void showFilteredData(CellRange range, int filterColumnIndex, List<String> selectedValues) {
        Platform.runLater(() -> {
            ObservableList<ObservableList<CellWrapper>> data = spreadsheetTableView.getItems();
            SheetUtils.showFilteredDataHalper(range, filterColumnIndex, selectedValues, data);
        });
    }

    public void resetCellStyle(int row, int column) {
        Platform.runLater(() -> {
            int adjustedColumn = column;
            ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
            CellWrapper cellWrapper = rowData.get(adjustedColumn);
            cellWrapper.setStyle("");
            spreadsheetTableView.refresh();
        });
    }
    private void addRowNumberColumn() {
        TableColumn<ObservableList<CellWrapper>, Number> rowNumberCol = new TableColumn<>("#");
        rowNumberCol.setCellValueFactory(cellData -> {
            int index = spreadsheetTableView.getItems().indexOf(cellData.getValue());
            // Since we need row numbers to remain sequential regardless of sorting, we can use the item's original row index
            ObservableList<CellWrapper> row = cellData.getValue();
            if (!row.isEmpty()) {
                int originalRowIndex = row.get(0).getOriginalRow();
                return new ReadOnlyObjectWrapper<>(originalRowIndex + 1);
            }
            return new ReadOnlyObjectWrapper<>(index + 1);
        });
        rowNumberCol.setSortable(false);
        rowNumberCol.setPrefWidth(50);
        spreadsheetTableView.getColumns().add(0, rowNumberCol);
    }

    //adjust column width for all columns exepect column 0(the column that have row numbers)
    public void adjustAllColumnWidth() {
        Platform.runLater(() -> {
            int weight = 50;

            for (int i = 1; i < spreadsheetTableView.getColumns().size(); i++) {
                weight=engine.getColumnProperties(i-1).getWidth();
                TableColumn<ObservableList<CellWrapper>, CellWrapper> column = (TableColumn<ObservableList<CellWrapper>, CellWrapper>) spreadsheetTableView.getColumns().get(i);
                column.setPrefWidth(weight);
                column.setMinWidth(weight);
                column.setMinWidth(weight);
                adjustColumnWidthByLabel(CoordinateFactory.convertIndexToColumnLabel(i-1), weight);
            }
            spreadsheetTableView.refresh();
        });
    }

    public void adjustColumnWidthByLabel(String columnLabel, int newWidth) {
        Platform.runLater(() -> {
            for (TableColumn<ObservableList<CellWrapper>, ?> column : spreadsheetTableView.getColumns()) {
                // Check if the column label matches the provided label
                if (column.getText().trim().equals(columnLabel)) {
                    // Set the width for the matching column
                    column.setPrefWidth(newWidth);
                    column.setMinWidth(newWidth);
                    column.setMaxWidth(newWidth);
                    break;
                }
            }
            // Refresh the table view to apply the updated width
            spreadsheetTableView.refresh();
        });
    }


    //adjust all row height
    public void adjustAllRowHeight() {
        Platform.runLater(() -> {
            for (int i = 0; i < spreadsheetTableView.getItems().size(); i++) {
                int height = engine.getRowProperties(i).getHeight();
                adjustRowHeight(i, height);
            }
    });
    }

    //adjust alignment for single column
    public void adjustColumnAlignment(int columnIndex) {
        Platform.runLater(() -> {
            TableColumn<ObservableList<CellWrapper>, CellWrapper> column = (TableColumn<ObservableList<CellWrapper>, CellWrapper>) spreadsheetTableView.getColumns().get(columnIndex + 1);
            configureCellFactory(column);
            spreadsheetTableView.refresh();
        });
    }

    public void highlightRange(Set<Coordinate> rangeCoordinates) {
        Platform.runLater(() -> {
            // Clear any previous highlights
            clearHighlights();

            // Loop through each coordinate and apply a highlight style
            for (Coordinate coord : rangeCoordinates) {
                int row = coord.getRow();
                int col = coord.getColumn();

                // Ensure rowData exists for the given row
                if (row >= spreadsheetTableView.getItems().size()) {
                    continue; // Skip if row is out of bounds
                }

                ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);

                // Ensure CellWrapper exists for the given column
                if (col >= rowData.size()) {
                    continue; // Skip if column is out of bounds
                }

                CellWrapper cellWrapper = rowData.get(col);

                // Handle empty cells (if cellWrapper.getCell() is null)
                if (cellWrapper.getCell() == null) {
                    cellWrapper.setHighlightStyle("-fx-background-color: #FFD699;"); // Highlight empty cell
                } else {
                    cellWrapper.setHighlightStyle("-fx-background-color: #FFD699;");  // Highlight non-empty cell
                }

            }

            // Refresh the table to show the updated highlights
            spreadsheetTableView.refresh();
        });
    }

    public void applyMultiColumnFilter(String range, Map<Integer, List<String>> filterCriteria) {
        RangeValidator rangeValidator = new RangeValidator(engine.getReadOnlySheet().getMaxRows(), engine.getReadOnlySheet().getMaxColumns());
        Coordinate[] rangeCoords = rangeValidator.parseRange(range);

        int startRow = rangeCoords[0].getRow();
        int startCol = rangeCoords[0].getColumn();
        int endRow = rangeCoords[1].getRow();
        int endCol = rangeCoords[1].getColumn();

        Set<Integer> matchingRowIndices = new HashSet<>();

        for (Map.Entry<Integer, List<String>> entry : filterCriteria.entrySet()) {
            int colIndex = entry.getKey();
            List<String> selectedValues = entry.getValue();


            for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
                ObservableList<CellWrapper> row = spreadsheetTableView.getItems().get(rowIndex);
                CellWrapper cellWrapper = row.get(colIndex);

                if (cellWrapper != null && cellWrapper.getCell() != null) {
                    EffectiveValue chosenValue = cellWrapper.getCell().getEffectiveValue();
                    String cellValue = chosenValue != null ? chosenValue.getValue().toString() : "";

                    if ("true".equalsIgnoreCase(cellValue) || "false".equalsIgnoreCase(cellValue)) {
                        cellValue = cellValue.toUpperCase();
                    }

                    if (selectedValues.contains(cellValue)) {
                        matchingRowIndices.add(rowIndex);
                    }
                }
            }
        }

        ObservableList<ObservableList<CellWrapper>> filteredData = FXCollections.observableArrayList();
        for (int rowIndex : matchingRowIndices) {
            ObservableList<CellWrapper> row = spreadsheetTableView.getItems().get(rowIndex);
            filteredData.add(row);
        }

        CellRange cellRange = new CellRange(startRow, startCol, endRow, endCol);
        SheetDisplayHelper.displayFilteredDataInPopup(filteredData, cellRange);
    }

    public void generateGraph(GraphParameters params) {
        GraphGenerator graphGenerator = new GraphGenerator(spreadsheetTableView,
                engine.getReadOnlySheet().getMaxRows(), engine.getReadOnlySheet().getMaxColumns());
        graphGenerator.generateGraph(params);
    }

}
