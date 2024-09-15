package desktop.sheet;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.cell.CellType;
import desktop.CellRange;
import desktop.CellWrapper;
import desktop.body.BodyController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TablePosition;


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
                String cellAddress = getColumnName(dataColumnIndex) + (row + 1);

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
                Object value = effectiveValue != null ? effectiveValue.getValue() : null;

                String versionString = version!=0 ? "Version: " + String.valueOf(version) : "";

                // Notify BodyController about the selected cell
                if (bodyController != null) {
                    bodyController.updateSelectedCell(cellAddress, originalValue, versionString);
                }

                // Highlight precedents and dependents
                highlightPrecedentsAndDependents(cell);
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


    private void highlightPrecedentsAndDependents(Cell selectedCell) {
        // Clear previous highlights
        clearHighlights();

        // Get precedents and dependents
        List<Cell> precedents = selectedCell!= null ? selectedCell.getInfluencedCells() : new ArrayList<>();
        List<Cell> dependents = selectedCell!= null ? selectedCell.getDependencies() : new ArrayList<>();

        // Highlight precedents in light blue
        String precedentStyle = "-fx-background-color: lightblue;";
        for (Cell precedent : precedents) {
            highlightCell(precedent, precedentStyle);
        }

        // Highlight dependents in light green
        String dependentStyle = "-fx-background-color: lightgreen;";
        for (Cell dependent : dependents) {
            highlightCell(dependent, dependentStyle);
        }

        // Refresh the table to apply styles
        spreadsheetTableView.refresh();
    }

    private void highlightCell(Cell cell, String style) {
        int row = cell.getCoordinate().getRow();
        int column = cell.getCoordinate().getColumn();

        // Get the cell wrapper and apply style
        ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
        CellWrapper cellWrapper = rowData.get(column);
        cellWrapper.setHighlightStyle(style);
    }



    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }


    private void setupSpreadsheetTableView() {
        // Configure the table view columns, cell factories, etc.
        // Implement resizing, wrapping, alignment as per requirements
    }

    public void loadSpreadsheetData() {
        // Use engine to load data
        // Populate spreadsheetTableView with data
    }

    public void refreshSpreadsheet() {
        // Refresh data in spreadsheetTableView
    }

    public void displaySheet(SheetReadActions sheetData) {
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
    }


    private void createColumns(int maxColumns) {
        for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
            String columnName = getColumnName(colIndex);
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
            configureCellFactory(column, Pos.CENTER, true);

            // Allow users to adjust column width
            column.setResizable(true);

            // Allow users to set alignment and wrapping/clipping
            addColumnContextMenu(column);

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

    public void resetSort() {
        if (originalData != null) {
            // Restore the original data
            spreadsheetTableView.setItems(FXCollections.observableArrayList(originalData));
            spreadsheetTableView.refresh();
        }
    }


    public void sortRange(CellRange range, List<Integer> sortColumns) {
        // Extract the sublist of rows to sort
        ObservableList<ObservableList<CellWrapper>> data = spreadsheetTableView.getItems();
        List<ObservableList<CellWrapper>> subList = data.subList(range.startRow, range.endRow + 1);

        // Sort the sublist
        Collections.sort(subList, new Comparator<ObservableList<CellWrapper>>() {
            @Override
            public int compare(ObservableList<CellWrapper> row1, ObservableList<CellWrapper> row2) {
                for (int colIndex : sortColumns) {
                    // Ensure colIndex is within the range
                    if (colIndex < range.startCol || colIndex > range.endCol) {
                        continue;
                    }

                    Object value1 = getSortableValue(row1.get(colIndex));
                    Object value2 = getSortableValue(row2.get(colIndex));

                    // Only sort numerical values
                    if (!(value1 instanceof Number) || !(value2 instanceof Number)) {
                        continue;
                    }

                    int cmp = Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                // Stable sort: preserve original order if all compared values are equal
                return 0;
            }
        });

        spreadsheetTableView.refresh();
    }

    private Object getSortableValue(CellWrapper cellWrapper) {
        if (cellWrapper == null || cellWrapper.getCell() == null) {
            return null;
        }
        EffectiveValue effectiveValue = cellWrapper.getCell().getEffectiveValue();
        if (effectiveValue != null) {
            return effectiveValue.getValue();
        }
        return null;
    }


    private String getColumnName(int colIndex) {
        // Convert column index to column name (e.g., 0 -> "A", 1 -> "B", ...)
        StringBuilder columnName = new StringBuilder();
        int tempIndex = colIndex;
        while (tempIndex >= 0) {
            columnName.insert(0, (char) ('A' + (tempIndex % 26)));
            tempIndex = (tempIndex / 26) - 1;
        }
        return columnName.toString();
    }

    private void configureCellFactory(TableColumn<ObservableList<CellWrapper>, CellWrapper> column, Pos alignment, boolean wrapText) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CellWrapper cellWrapper, boolean empty) {
                super.updateItem(cellWrapper, empty);
                if (empty || cellWrapper == null || cellWrapper.getCell() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    EffectiveValue effectiveValue = cellWrapper.getCell().getEffectiveValue();
                    Object value = effectiveValue != null ? effectiveValue.getValue() : null;
                    String displayText = value != null ? value.toString() : "";

                    // Handle boolean values
                    if (effectiveValue != null && effectiveValue.getCellType() == CellType.BOOLEAN) {
                        displayText = displayText.toUpperCase();
                    }

                    // Set alignment
                    setAlignment(alignment);

                    // Combine cell style and highlight style
                    String fullStyle = cellWrapper.getStyle() + cellWrapper.getHighlightStyle();

                    // Initialize style variables
                    String backgroundStyle = "";
                    String textStyle = "";

                    if (fullStyle != null && !fullStyle.isEmpty()) {
                        // Split the style string into individual styles
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

                    // Apply background style to the TableCell
                    setStyle(backgroundStyle);

                    if (wrapText) {
                        // Use Text node and apply text style
                        Text text = new Text(displayText);
                        text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                        text.setStyle(textStyle);
                        setGraphic(text);
                        setText(null); // Clear any text
                    } else {
                        // Use setText and apply text style to the cell
                        setText(displayText);
                        setGraphic(null); // Clear any graphics
                        setStyle(backgroundStyle + textStyle); // Apply both styles to the cell
                    }
                }
            }
        });
    }


    private void clearHighlights() {
        for (ObservableList<CellWrapper> rowData : spreadsheetTableView.getItems()) {
            for (CellWrapper cellWrapper : rowData) {
                cellWrapper.setHighlightStyle("");
            }
        }
    }




    // Context menu for column settings
    private void addColumnContextMenu(TableColumn<ObservableList<CellWrapper>, CellWrapper> column) {
        ContextMenu contextMenu = new ContextMenu();

        // Alignment menu
        Menu alignmentMenu = new Menu("Alignment");
        ToggleGroup alignmentGroup = new ToggleGroup();

        RadioMenuItem leftAlign = new RadioMenuItem("Left");
        leftAlign.setToggleGroup(alignmentGroup);
        RadioMenuItem centerAlign = new RadioMenuItem("Center");
        centerAlign.setToggleGroup(alignmentGroup);
        RadioMenuItem rightAlign = new RadioMenuItem("Right");
        rightAlign.setToggleGroup(alignmentGroup);
        centerAlign.setSelected(true); // Default alignment

        alignmentMenu.getItems().addAll(leftAlign, centerAlign, rightAlign);

        // Wrap/Clip menu
        Menu wrapMenu = new Menu("Text Handling");
        ToggleGroup wrapGroup = new ToggleGroup();

        RadioMenuItem wrapText = new RadioMenuItem("Wrap");
        wrapText.setToggleGroup(wrapGroup);
        RadioMenuItem clipText = new RadioMenuItem("Clip");
        clipText.setToggleGroup(wrapGroup);
        wrapText.setSelected(true); // Default to wrap

        wrapMenu.getItems().addAll(wrapText, clipText);

        // Event handlers
        alignmentGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                Pos alignment = Pos.CENTER;
                if (newToggle == leftAlign) {
                    alignment = Pos.CENTER_LEFT;
                } else if (newToggle == rightAlign) {
                    alignment = Pos.CENTER_RIGHT;
                }
                // Update cell factory with new alignment
                configureCellFactory(column, alignment, wrapText.isSelected());
                spreadsheetTableView.refresh();
            }
        });

        wrapGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                boolean wrap = newToggle == wrapText;
                // Update cell factory with new wrap setting
                configureCellFactory(column, getCurrentAlignment(alignmentGroup), wrap);
                spreadsheetTableView.refresh();
            }
        });

        contextMenu.getItems().addAll(alignmentMenu, wrapMenu);

        // Set context menu on column header
        column.setContextMenu(contextMenu);
    }

    private Pos getCurrentAlignment(ToggleGroup alignmentGroup) {
        RadioMenuItem selected = (RadioMenuItem) alignmentGroup.getSelectedToggle();
        if (selected != null) {
            switch (selected.getText()) {
                case "Left":
                    return Pos.CENTER_LEFT;
                case "Right":
                    return Pos.CENTER_RIGHT;
                default:
                    return Pos.CENTER;
            }
        }
        return Pos.CENTER;
    }

    public void applyCellStyle(int row, int column, String style) {
        // Since the data columns start from index 1 (after the row number column), adjust the column index
        int adjustedColumn = column;
        ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
        CellWrapper cellWrapper = rowData.get(adjustedColumn);
        cellWrapper.setStyle(style);
        spreadsheetTableView.refresh();
    }

    public void showSortedData(CellRange range, List<Integer> sortColumns) {
        // Extract the data to sort
        ObservableList<ObservableList<CellWrapper>> data = spreadsheetTableView.getItems();

        // Create a deep copy of the data within the specified range
        List<ObservableList<CellWrapper>> dataToSort = new ArrayList<>();
        for (int i = range.startRow; i <= range.endRow; i++) {
            ObservableList<CellWrapper> originalRow = data.get(i);
            ObservableList<CellWrapper> rowCopy = FXCollections.observableArrayList();

            for (int j = range.startCol; j <= range.endCol; j++) {
                CellWrapper originalCell = originalRow.get(j);
                // Create a copy of CellWrapper
                CellWrapper cellCopy = new CellWrapper(originalCell.getCell(), originalCell.getOriginalRow(), originalCell.getColumn());
                cellCopy.setStyle(originalCell.getStyle());
                rowCopy.add(cellCopy);
            }

            dataToSort.add(rowCopy);
        }

        // Sort the data
        Collections.sort(dataToSort, new Comparator<ObservableList<CellWrapper>>() {
            @Override
            public int compare(ObservableList<CellWrapper> row1, ObservableList<CellWrapper> row2) {
                for (int colIndex : sortColumns) {
                    // Ensure colIndex is within the range
                    if (colIndex < range.startCol || colIndex > range.endCol) {
                        continue;
                    }

                    Object value1 = getSortableValue(row1.get(colIndex - range.startCol));
                    Object value2 = getSortableValue(row2.get(colIndex - range.startCol));

                    // Only sort numerical values
                    if (!(value1 instanceof Number) || !(value2 instanceof Number)) {
                        continue;
                    }

                    int cmp = Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                // Stable sort: preserve original order if all compared values are equal
                return 0;
            }
        });

        // Display the sorted data in a pop-up window
        displaySortedDataInPopup(dataToSort, range);
    }

    private void displaySortedDataInPopup(List<ObservableList<CellWrapper>> sortedData, CellRange range) {
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
            String columnName = getColumnName(colIndex);
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


    private void configureCellFactoryForPopup(TableColumn<ObservableList<CellWrapper>, CellWrapper> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CellWrapper cellWrapper, boolean empty) {
                super.updateItem(cellWrapper, empty);
                if (empty || cellWrapper == null || cellWrapper.getCell() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    EffectiveValue effectiveValue = cellWrapper.getCell().getEffectiveValue();
                    Object value = effectiveValue != null ? effectiveValue.getValue() : null;
                    String displayText = value != null ? value.toString() : "";

                    // Handle boolean values
                    if (effectiveValue != null && effectiveValue.getCellType() == CellType.BOOLEAN) {
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

    public void resetCellStyle(int row, int column) {
        int adjustedColumn = column;
        ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);
        CellWrapper cellWrapper = rowData.get(adjustedColumn);
        cellWrapper.setStyle("");
        spreadsheetTableView.refresh();
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
        rowNumberCol.setPrefWidth(50); // Adjust width as needed
        spreadsheetTableView.getColumns().add(0, rowNumberCol);
    }




    // Additional methods as needed
}
