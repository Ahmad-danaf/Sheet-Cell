package sheetDisplay.sheet;
import com.sheetcell.engine.Engine;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.ColumnProperties;
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


               // highlightPrecedentsAndDependents(row,dataColumnIndex);
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
            //adjustAllColumnWidth();
            //adjustAllRowHeight();
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
                    //EffectiveValue effectiveValue = cellWrapper.getCell() != null ? cellWrapper.getCell().getEffectiveValue() : null;
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
                    //for now create a mock properties object
                    ColumnProperties properties = new ColumnProperties("center", 100);
                    RowProperties rowProperties = new RowProperties(50);
                   // ColumnProperties properties = engine.getColumnProperties(cellWrapper.getColumn());
                    //RowProperties rowProperties = engine.getRowProperties(cellWrapper.getOriginalRow());
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
                //create a mock properties object
                ColumnProperties properties = new ColumnProperties("center", 100);
               // ColumnProperties properties = engine.getColumnProperties(cellWrapper.getColumn());
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

}
