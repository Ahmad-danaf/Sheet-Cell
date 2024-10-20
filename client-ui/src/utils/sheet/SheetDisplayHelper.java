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
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.cell.CellRange;
import utils.cell.CellWrapper;

import java.util.List;

public class SheetDisplayHelper {

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
            SheetDisplayHelper.configureCellFactoryForPopup(column);

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

    public static void configureCellFactoryForPopup(TableColumn<ObservableList<CellWrapper>, CellWrapper> column) {
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

    public static void displayVersionInPopup(SheetReadActions versionSheet, int versionNumber) {
        Stage versionStage = new Stage();
        versionStage.setTitle("Version " + versionNumber);

        TableView<ObservableList<CellWrapper>> versionTableView = new TableView<>();
        versionTableView.setEditable(false);

        // Add columns and populate rows as in displaySheet method
        // No styling, raw content only
        Platform.runLater(() -> {
            int maxRows = versionSheet.getMaxRows();
            int maxColumns = versionSheet.getMaxColumns();

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
