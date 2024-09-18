package desktop.sheet;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.cell.CellType;
import desktop.CellRange;
import desktop.CellWrapper;
import desktop.body.BodyController;
import javafx.application.Platform;
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
                Object value = effectiveValue != null ? effectiveValue.getValue() : ""; //"" or null?

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
        String precedentStyle = "-fx-background-color: lightgreen;";
        for (Cell precedent : precedents) {
            highlightCell(precedent, precedentStyle);
        }

        // Highlight dependents in light green
        String dependentStyle = "-fx-background-color: lightblue;";
        for (Cell dependent : dependents) {
            highlightCell(dependent, dependentStyle);
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
            adjustAllColumnWidth(sheetData.getColumnWidth());
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
        });
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
                if (cellWrapper == null) {
                    // If there's no cell wrapper, treat it as empty
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // Clear any previous styles
                } else {
                    String fullStyle = cellWrapper.getStyle() + cellWrapper.getHighlightStyle();

                    // Initialize style variables
                    String backgroundStyle = "";
                    String textStyle = "";

                    if (fullStyle != null && !fullStyle.isEmpty()) {
                        // Split the style string into individual styles
                        System.out.println(fullStyle);
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

                    // Apply background style to the TableCell (applies regardless of cell content)
                    setStyle(backgroundStyle + textStyle);

                    // Handle empty or non-empty cells separately
                    if (empty || cellWrapper.getCell() == null) {
                        // Empty cell: Clear text and graphic
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Non-empty cell: Handle value display
                        EffectiveValue effectiveValue = cellWrapper.getCell().getEffectiveValue();
                        Object value = effectiveValue != null ? effectiveValue.getValue() : null;
                        String displayText = value != null ? value.toString() : "";

                        // Handle boolean values
                        if (effectiveValue != null && effectiveValue.getCellType() == CellType.BOOLEAN) {
                            displayText = displayText.toUpperCase();
                        }

                        // Set alignment and display the value
                        setAlignment(alignment);

                        if (wrapText) {
                            // Use Text node and apply text style
                            Text text = new Text(displayText);
                            text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                            text.setStyle(textStyle);
                            setGraphic(text);
                            setText(null); // Clear any text
                        } else {
                            // Use setText for normal text display
                            setText(displayText);
                            setGraphic(null); // Clear any graphics
                        }
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
        spreadsheetTableView.refresh();
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
            List<ObservableList<CellWrapper>> dataToSort = new ArrayList<>();
            for (int i = range.startRow; i <= range.endRow; i++) {
                ObservableList<CellWrapper> originalRow = data.get(i);
                ObservableList<CellWrapper> rowCopy = FXCollections.observableArrayList();

                for (int j = range.startCol; j <= range.endCol; j++) {
                    CellWrapper originalCell = originalRow.get(j);
                    CellWrapper cellCopy = new CellWrapper(originalCell.getCell(), originalCell.getOriginalRow(), originalCell.getColumn());
                    cellCopy.setStyle(originalCell.getStyle());
                    rowCopy.add(cellCopy);
                }
                dataToSort.add(rowCopy);
            }

            Collections.sort(dataToSort, new Comparator<ObservableList<CellWrapper>>() {
                @Override
                public int compare(ObservableList<CellWrapper> row1, ObservableList<CellWrapper> row2) {
                    for (int colIndex : sortColumns) {
                        if (colIndex < range.startCol || colIndex > range.endCol) {
                            continue;
                        }

                        Object value1 = getSortableValue(row1.get(colIndex - range.startCol));
                        Object value2 = getSortableValue(row2.get(colIndex - range.startCol));

                        if (!(value1 instanceof Number) || !(value2 instanceof Number)) {
                            continue;
                        }

                        int cmp = Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
                        if (cmp != 0) {
                            return cmp;
                        }
                    }
                    return 0;
                }
            });

            displaySortedDataInPopup(dataToSort, range);
        });
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

    public void adjustRowHeight(double newHeight) {
        Platform.runLater(() -> {
            // Set the fixed height for each row in the table view
            spreadsheetTableView.setFixedCellSize(newHeight);

            // Force the table to re-layout and apply the new row height
            spreadsheetTableView.refresh();
        });
    }

    public void adjustColumnWidth(int columnIndex, int weight) {
        Platform.runLater(() -> {
            TableColumn<ObservableList<CellWrapper>, CellWrapper> column = (TableColumn<ObservableList<CellWrapper>, CellWrapper>) spreadsheetTableView.getColumns().get(columnIndex);
            column.setPrefWidth(weight);
            spreadsheetTableView.refresh();
        });
    }

    //adjust column width for all columns exepect column 0(the column that have row numbers)
    public void adjustAllColumnWidth(int weight) {
        Platform.runLater(() -> {
            for (int i = 1; i < spreadsheetTableView.getColumns().size(); i++) {
                TableColumn<ObservableList<CellWrapper>, CellWrapper> column = (TableColumn<ObservableList<CellWrapper>, CellWrapper>) spreadsheetTableView.getColumns().get(i);
                column.setPrefWidth(weight);
            }
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
                    System.out.println("Row out of bounds: " + row);
                    continue; // Skip if row is out of bounds
                }

                ObservableList<CellWrapper> rowData = spreadsheetTableView.getItems().get(row);

                // Ensure CellWrapper exists for the given column
                if (col >= rowData.size()) {
                    System.out.println("Column out of bounds: " + col);
                    continue; // Skip if column is out of bounds
                }

                CellWrapper cellWrapper = rowData.get(col);

                // Handle empty cells (if cellWrapper.getCell() is null)
                if (cellWrapper.getCell() == null) {
                    System.out.println("Empty cell at: " + row + "," + col);
                    cellWrapper.setHighlightStyle("-fx-background-color: #FFD699;"); // Highlight empty cell
                } else {
                    System.out.println("Highlighting non-empty cell at: " + row + "," + col);
                    cellWrapper.setHighlightStyle("-fx-background-color: #FFD699;");  // Highlight non-empty cell
                }
                System.out.println("CellWrapper: " + cellWrapper.getHighlightStyle() + " at row " + row + " col " + col);

            }

            // Refresh the table to show the updated highlights
            spreadsheetTableView.refresh();
        });
    }


    public void displayVersionInPopup(SheetReadActions versionSheet, int versionNumber) {
        Stage versionStage = new Stage();
        versionStage.setTitle("Version " + versionNumber);

        TableView<ObservableList<CellWrapper>> versionTableView = new TableView<>();
        versionTableView.setEditable(false);

        // Add columns and populate rows as in displaySheet method
        // No styling, raw content only
        Platform.runLater(() -> {
            int maxRows = versionSheet.getMaxRows();
            int maxColumns = versionSheet.getMaxColumns();

            // Create columns
            for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                String columnName = getColumnName(colIndex);
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
                    Cell cell = versionSheet.getCell(rowIndex, colIndex);
                    CellWrapper cellWrapper = new CellWrapper(cell, rowIndex, colIndex);
                    rowData.add(cellWrapper);
                }
                data.add(rowData);
            }
            versionTableView.setItems(data);
        });

        Scene versionScene = new Scene(new VBox(versionTableView));
        versionStage.setScene(versionScene);
        versionStage.show();
    }


}
