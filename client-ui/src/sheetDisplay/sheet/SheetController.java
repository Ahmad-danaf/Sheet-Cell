package sheetDisplay.sheet;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.utils.ColumnProperties;
import com.sheetcell.engine.utils.ColumnRowPropertyManager;
import com.sheetcell.engine.utils.RowProperties;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import sheetDisplay.SheetDisplayController;
import utils.cell.CellWrapper;

import java.util.*;

public class SheetController {

    @FXML private SheetDisplayController sheetDisplayController;
    @FXML
    private ScrollPane gridScrollPane;
    @FXML
    private TableView<ObservableList<CellWrapper>> spreadsheetTableView;
    private ObservableList<ObservableList<CellWrapper>> originalData;
    private ColumnRowPropertyManager columnRowPropertyManager = new ColumnRowPropertyManager();
    private Map<Coordinate, Set<Coordinate>> dependenciesMap;
    private Map<Coordinate, Set<Coordinate>> influencedMap;

    public void setSheetDisplayController(SheetDisplayController sheetDisplayController) {
        this.sheetDisplayController = sheetDisplayController;
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
                String effectiveValue = "";
                String originalValue = "";
                int version = 0;
                if (cellWrapper != null) {
                    effectiveValue = cellWrapper.getEffectiveValue();
                    originalValue= cellWrapper.getOriginalValue() != null ? cellWrapper.getOriginalValue() : "";
                    version = cellWrapper.getVersion();
                }
                Object value = effectiveValue; //"" or null?

                String versionString = version!=0 ? "Version: " + String.valueOf(version) : "";

                // Notify sheetDisplayController about the selected cell
                if (sheetDisplayController != null) {
                    sheetDisplayController.updateSelectedCell(cellAddress, originalValue, versionString);
                }


                highlightPrecedentsAndDependents(row,dataColumnIndex);
            } else {
                // No cell selected; clear selection in sheetDisplayController
                if (sheetDisplayController != null) {
                    sheetDisplayController.clearSelectedCell();
                }

                // Clear highlights
                //clearHighlights();
                spreadsheetTableView.refresh();
            }
        });
    }

    private void highlightPrecedentsAndDependents(int row, int column) {
        // Clear previous highlights
        clearHighlights();
        Coordinate selectedCoordinate = new Coordinate(row, column);

        Set<Coordinate> precedents = dependenciesMap.getOrDefault(selectedCoordinate, new HashSet<>());
        Set<Coordinate> dependents = influencedMap.getOrDefault(selectedCoordinate, new HashSet<>());

        // Highlight precedents in light green
        String precedentStyle = "-fx-background-color: lightgreen;";
        for (Coordinate precedent : precedents) {
            highlightCell(precedent.getRow(),precedent.getColumn(), precedentStyle);
        }

        // Highlight dependents in light blue
        String dependentStyle = "-fx-background-color: lightblue;";
        for (Coordinate dependent : dependents) {
            highlightCell(dependent.getRow(),dependent.getColumn(), dependentStyle);
        }

        // Refresh the table to apply styles
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

    private void highlightCell(int row,int column, String style) {
        ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
        CellWrapper cellWrapper = rowData.get(column);
        cellWrapper.setHighlightStyle(style);
    }


    public void populateColumnRowPropertyManager(Map<String, Object> sheetData){
        int maxColumns = ((Double) sheetData.get("maxColumns")).intValue();
        int maxRows = ((Double) sheetData.get("maxRows")).intValue();
        Map<String, Map<String, Object>> columnProperties = (Map<String, Map<String, Object>>) sheetData.get("columnProperties");
        Map<String, Double> rowProperties = (Map<String, Double>) sheetData.get("rowProperties");
        for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
            Map<String, Object> columnProperty = columnProperties.get(String.valueOf(colIndex));
            String alignment = (String) columnProperty.get("alignment");
            int width = ((Double) columnProperty.get("width")).intValue();
            columnRowPropertyManager.setColumnProperties(colIndex, alignment, width);
        }
        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
            String key = String.valueOf(rowIndex);
            Double height = rowProperties.get(key);
            columnRowPropertyManager.setRowProperties(rowIndex, height.intValue());
        }
        sheetDisplayController.setColumnRowPropertyManager(columnRowPropertyManager,maxColumns,maxRows);
    }

    public void populateDependenciesAndInfluenced(Map<String, Object> sheetData) {
        // Retrieve dependencies and influenced data as Map<String, List<String>> from sheetData
        Map<String, List<String>> dependenciesData = (Map<String, List<String>>) sheetData.get("dependenciesMap");
        Map<String, List<String>> influencedData = (Map<String, List<String>>) sheetData.get("influencedMap");

        this.dependenciesMap = new HashMap<>();
        this.influencedMap = new HashMap<>();

        // Populate dependenciesMap
        for (Map.Entry<String, List<String>> entry : dependenciesData.entrySet()) {
            // Convert key from string to Coordinate
            String[] keyCoords = entry.getKey().split(",");
            int keyRow = Integer.parseInt(keyCoords[0]);
            int keyColumn = Integer.parseInt(keyCoords[1]);
            Coordinate keyCoordinate = new Coordinate(keyRow, keyColumn);

            // Convert each value in the list from string to Coordinate and add to Set<Coordinate>
            Set<Coordinate> dependencies = new HashSet<>();
            for (String dep : entry.getValue()) {
                String[] depCoords = dep.split(",");
                int depRow = Integer.parseInt(depCoords[0]);
                int depColumn = Integer.parseInt(depCoords[1]);
                dependencies.add(new Coordinate(depRow, depColumn));
            }

            // Add to the dependenciesMap
            this.dependenciesMap.put(keyCoordinate, dependencies);
        }

        // Populate influencedMap
        for (Map.Entry<String, List<String>> entry : influencedData.entrySet()) {
            // Convert key from string to Coordinate
            String[] keyCoords = entry.getKey().split(",");
            int keyRow = Integer.parseInt(keyCoords[0]);
            int keyColumn = Integer.parseInt(keyCoords[1]);
            Coordinate keyCoordinate = new Coordinate(keyRow, keyColumn);

            // Convert each value in the list from string to Coordinate and add to Set<Coordinate>
            Set<Coordinate> influences = new HashSet<>();
            for (String inf : entry.getValue()) {
                String[] infCoords = inf.split(",");
                int infRow = Integer.parseInt(infCoords[0]);
                int infColumn = Integer.parseInt(infCoords[1]);
                influences.add(new Coordinate(infRow, infColumn));
            }

            // Add to the influencedMap
            this.influencedMap.put(keyCoordinate, influences);
        }
    }


    public void populateTableView(Map<String, Object> sheetData) {
        int maxRows = ((Double) sheetData.get("maxRows")).intValue();
        int maxColumns = ((Double) sheetData.get("maxColumns")).intValue();
        Map<String, Map<String, String>> cellData = (Map<String, Map<String, String>>) sheetData.get("cellData");

        // Clear the existing data in the TableView
        this.clearTableView();

        displaySheet(sheetData);
    }

    public void displaySheet(Map<String, Object> sheetData) {
        int maxRows = ((Double) sheetData.get("maxRows")).intValue();
        int maxColumns = ((Double) sheetData.get("maxColumns")).intValue();
        populateColumnRowPropertyManager(sheetData);
        populateDependenciesAndInfluenced(sheetData);
        Map<String, Map<String, String>> cellData = (Map<String, Map<String, String>>) sheetData.get("cellData");
        Platform.runLater(() -> {

            // Clear existing data
            spreadsheetTableView.getColumns().clear();
            spreadsheetTableView.getItems().clear();


            // Add row number column
            addRowNumberColumn();

            // Create data columns dynamically
            createColumns(maxColumns);

            // Populate rows
            populateRows(maxRows, maxColumns, cellData);
            adjustAllColumnWidth();
            adjustAllRowHeight();
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

    private void populateRows(int maxRows, int maxColumns, Map<String, Map<String, String>> cellData) {
        ObservableList<ObservableList<CellWrapper>> data = FXCollections.observableArrayList();

        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
            ObservableList<CellWrapper> rowData = FXCollections.observableArrayList();
            for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                String key = rowIndex + "," + colIndex;
                Map<String, String> cellValues = cellData.get(key);
                String originalValue = cellValues.get("originalValue");
                String effectiveValue = cellValues.get("effectiveValue");
                if(effectiveValue!=null &&(effectiveValue.equalsIgnoreCase("true") || effectiveValue.equalsIgnoreCase("false"))){
                    effectiveValue = effectiveValue.toUpperCase();
                }

                int version = Integer.parseInt(cellValues.get("version"));
                CellWrapper cellWrapper = new CellWrapper(originalValue, effectiveValue, version, rowIndex, colIndex);
                rowData.add(cellWrapper);
            }
            data.add(rowData);
        }

        // Store original data
        originalData = FXCollections.observableArrayList(data);

        spreadsheetTableView.setItems(data);
    }

    public void clearTableView() {
        spreadsheetTableView.getItems().clear();
    }

    public List<String> getUniqueValuesInColumn(int columnIndex) {
       List<String> uniqueValues = new ArrayList<>();
//        for (ObservableList<CellWrapper> row : spreadsheetTableView.getItems()) {
//            CellWrapper cellWrapper = row.get(columnIndex);
//            if (cellWrapper != null) {
//                String value = cellWrapper.getEffectiveValue();
//                if (!uniqueValues.contains(value)) {
//                    uniqueValues.add(value);
//                }
//            }
//        }
        return uniqueValues;
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
                    String displayText = cellWrapper.getEffectiveValue();
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

                    ColumnProperties properties = columnRowPropertyManager.getColumnProperties(cellWrapper.getColumn());
                    RowProperties rowProperties = columnRowPropertyManager.getRowProperties(cellWrapper.getOriginalRow());
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
                ColumnProperties properties = columnRowPropertyManager.getColumnProperties(cellWrapper.getColumn());
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

    public void adjustAllColumnWidth() {
        Platform.runLater(() -> {
            int weight = 50;

            for (int i = 1; i < spreadsheetTableView.getColumns().size(); i++) {
                weight = columnRowPropertyManager.getColumnProperties(i-1).getWidth();
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
                int height = columnRowPropertyManager.getRowProperties(i).getHeight();
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

    public void reselectCell(int row, int column) {
        // Adjust column index to account for row number column
        int adjustedColumn = column + 1;
        spreadsheetTableView.getSelectionModel().clearSelection();
        spreadsheetTableView.getSelectionModel().select(row, spreadsheetTableView.getColumns().get(adjustedColumn));
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

    public void resetCellStyle(int row, int column) {
        Platform.runLater(() -> {
            int adjustedColumn = column;
            ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
            CellWrapper cellWrapper = rowData.get(adjustedColumn);
            cellWrapper.setStyle("");
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

                cellWrapper.setHighlightStyle("-fx-background-color: #FFD699;");  // Highlight non-empty cell

            }

            // Refresh the table to show the updated highlights
            spreadsheetTableView.refresh();
        });
    }

}
