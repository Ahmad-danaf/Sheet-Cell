package sheetDisplay;

import com.google.gson.Gson;
import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.SheetUpdateResult;
import data.SheetUserData;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sheetDisplay.sheet.SheetController;
import utils.UIHelper;
import utils.ValidationUtils;
import utils.cell.CellRange;
import utils.color.ColorUtils;
import utils.dialogs.FilterDialog;
import utils.dialogs.SortDialog;
import utils.parameters.FilterParameters;
import utils.parameters.GraphParameters;
import utils.parameters.MultiColFilterParameters;
import utils.parameters.SortParameters;
import utils.parsing.ParsingUtils;
import utils.sheet.GraphGenerator;
import utils.sheet.SheetDisplayHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SheetDisplayController {

    private Engine engine;


    // Top section components

    @FXML
    private Label usernameLabel;
    @FXML
    private TextField sheetNameField;
    @FXML
    private TextField selectedCellId;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private ColorPicker textColorPicker;
    @FXML
    private TextField originalCellValue;
    @FXML
    private TextField lastUpdateCellVersion;
    @FXML
    private ChoiceBox<String> versionSelector;
    // Reference to SheetController
    @FXML
    private SheetController spreadsheetGridController;
    @FXML
    private ChoiceBox<String> columnChoiceBox;
    @FXML
    private VBox columnControls;
    @FXML
    private ChoiceBox<String> alignmentChoiceBox;
    @FXML
    private TextField columnWidthField;
    @FXML
    private ChoiceBox<Integer> rowChoiceBox;
    @FXML
    private TextField rowHeightField;
    @FXML
    private VBox rowControls;

    private final Gson gson = new Gson();
    private Stage stage;
    boolean isSheetLoaded = false;

    @FXML
    private void initialize() {
        engine = new EngineImpl();
        if (spreadsheetGridController != null) {
            spreadsheetGridController.setEngine(engine);
            spreadsheetGridController.setSheetDisplayController(this);
        }


        versionSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Parse the selected version number
                int selectedVersion = ParsingUtils.extractNewVersionSheet(newValue);
                int currentVersion = engine.getReadOnlySheet() != null ? engine.getReadOnlySheet().getVersion() : 0;

                // Only display the popup if a past version is selected
                if (selectedVersion < currentVersion && selectedVersion > 0) {
                    handleVersionSelection(selectedVersion);
                }
            }
        });

        columnWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*")) {
                if (!newValue.isEmpty()) {
                    int width = Integer.parseInt(newValue);
                    String selectedColumn = columnChoiceBox.getSelectionModel().getSelectedItem();
                    int column = CoordinateFactory.getColumnIndexFromLabel(selectedColumn);
                    engine.setColumnWidth(column, width);
                    spreadsheetGridController.adjustAllColumnWidth();
                }
            } else {
                columnWidthField.setText(oldValue); // Revert to the old value if input is invalid
            }
        });


    }

    public void initializeSheetView(SheetUserData sheetData) {
        // Set the sheet name
        sheetNameField.setText(sheetData.getSheetName());

        // Initialize the version selector
        //initializeVersionSelector(sheetData);

        // Initialize cell styles
        //initializeCellStyling();

        // Load the sheet data into the spreadsheet grid
        loadSheetIntoGrid(sheetData);

        // Mark the sheet as loaded
        isSheetLoaded = true;
    }

    private void loadSheetIntoGrid(SheetUserData sheetData) {
        // Assuming SheetUserData has a method to get the sheet's data
        // Pass the data to the spreadsheetGridController
       // spreadsheetGridController.loadSheetData(sheetData);
    }

    public void setSpreadsheetGridController(SheetController spreadsheetGridController) {
        this.spreadsheetGridController = spreadsheetGridController;
        spreadsheetGridController.setEngine(engine);
        spreadsheetGridController.setSheetDisplayController(this);
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            // Load the dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent dashboardParent = loader.load();

            // Get the current stage (window) using the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the scene to the dashboard and show it
            stage.setScene(new Scene(dashboardParent));
            stage.show();

        } catch (IOException e) {
            // Handle any exceptions, such as file not found or FXML loading errors
            e.printStackTrace();
            UIHelper.showError("Error", "Failed to load the dashboard. Please try again.");
        }
    }



    @FXML
    private void handleColumnSelection(ActionEvent event) {
        String selectedColumn = columnChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedColumn != null && !selectedColumn.isEmpty()) {
            int column = CoordinateFactory.getColumnIndexFromLabel(selectedColumn);
            columnControls.setVisible(true);
            columnWidthField.setText(String.valueOf(engine.getColumnProperties(column).getWidth()));
            alignmentChoiceBox.setValue(engine.getColumnProperties(column).getAlignment());
        } else {
            columnControls.setVisible(false);
        }
    }

    @FXML
    private void handleRowSelection(ActionEvent event) {
        Integer selectedRow = rowChoiceBox.getValue();
        if (selectedRow != null) {
            rowControls.setVisible(true);
            rowHeightField.setText(String.valueOf(engine.getRowProperties(selectedRow - 1).getHeight()));
        } else {
            rowControls.setVisible(false);
        }
    }

    @FXML
    private void handleApplyRowHeight(ActionEvent event) {
        // Get the selected row and entered row height
        Integer selectedRow = rowChoiceBox.getValue();
        String heightText = rowHeightField.getText();

        try {
            int newRowHeight = Integer.parseInt(heightText);

            // Apply the new row height
            if (selectedRow != null && newRowHeight > 0) {
                engine.setRowHeight(selectedRow - 1, newRowHeight);
                spreadsheetGridController.adjustRowHeight(selectedRow - 1, newRowHeight);
            } else {
                UIHelper.showError("Invalid Input", "Please enter a valid row height.");
            }
        } catch (NumberFormatException e) {
            UIHelper.showError("Invalid Input", "Row height must be a number.");
        }
    }


    private void populateColumnChoiceBox() {
        // Clear existing choices
        if (columnChoiceBox != null && columnChoiceBox.getItems() != null) {
            columnChoiceBox.getItems().clear();
        }

        int maxColumns = engine.getReadOnlySheet().getMaxColumns();
        for (int i = 0; i < maxColumns; i++) {
            columnChoiceBox.getItems().add("Column " + CoordinateFactory.convertIndexToColumnLabel(i));
        }

        // Reset visibility
        columnControls.setVisible(false);
    }

    private void populateRowChoiceBox() {
        if (rowChoiceBox != null && rowChoiceBox.getItems() != null) {
            rowChoiceBox.getItems().clear();
        }
        int maxRows = engine.getReadOnlySheet().getMaxRows();
        for (int i = 0; i < maxRows; i++) {
            rowChoiceBox.getItems().add(i + 1);  // Display row number starting from 1
        }
        rowControls.setVisible(false);  // Hide the controls until a row is selected
    }


    @FXML
    private void handleSort(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to sort data.");
            return;
        }
        // Display a dialog to collect sorting parameters
        SortDialog sortDialogMaker = new SortDialog();
        Dialog<SortParameters> dialog = sortDialogMaker.createSortDialog();
        Optional<SortParameters> result = dialog.showAndWait();

        if (result.isPresent()) {
            SortParameters params = result.get();
            String rangeInput = params.getRangeInput().trim().toUpperCase();
            String columnsInput = params.getColumnsInput().trim().toUpperCase();

            try {
                ValidationUtils.validateSortInput(rangeInput, columnsInput, engine.getReadOnlySheet().getMaxRows(), engine.getReadOnlySheet().getMaxColumns());
                // Parse the range and columns
                CellRange range = ParsingUtils.parseRange(rangeInput);
                List<Integer> sortColumns = ParsingUtils.parseColumns(columnsInput);

                // Trigger sorting in SheetController
                spreadsheetGridController.showSortedData(range, sortColumns);
            } catch (Exception e) {
                UIHelper.showError("Invalid input format.", e.getMessage());

            }
        }
    }

    @FXML
    void handleUpdateValue(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to update cell values.");
            return;
        }
        String cellAddress = selectedCellId.getText();
        String newValue = originalCellValue.getText();
        if (cellAddress == null || cellAddress.isEmpty()) {
            UIHelper.showAlert("No cell selected", "Please select a cell to update.");
            return;
        }
        if (newValue == null) {
            newValue = "";
        }
        int row = CoordinateFactory.getRowIndex(cellAddress);
        int column = CoordinateFactory.getColumnIndex(cellAddress);

        try {
            engine.doesCellIdVaild(cellAddress);
            SheetUpdateResult result = engine.setCellValue(cellAddress, newValue);
            if (result.isNoActionNeeded()) {
                UIHelper.showAlert(result.getErrorMessage());
                return;
            } else if (result.hasError()) {
                UIHelper.showError("Update failed: ", result.getErrorMessage());
                return;
            }
            spreadsheetGridController.refreshSpreadsheet();
            Cell changedCell = engine.getReadOnlySheet().getCell(row, column);
            if (changedCell != null) {
                int newVersion = changedCell.getVersion();
                lastUpdateCellVersion.setText(String.valueOf(newVersion));
            }
            int newVersionSheet = engine.getReadOnlySheet().getVersion();
            Platform.runLater(() -> {
                versionSelector.getItems().add("Version " + newVersionSheet + " Cells Changed: " + engine.getReadOnlySheet().getCellChangeCount());
                versionSelector.getSelectionModel().selectLast();
            });
            spreadsheetGridController.reselectCell(row, column);
        } catch (Exception e) {
            UIHelper.showAlert("Error updating cell", e.getMessage());
        }
    }

    @FXML
    private void handleApplyStyle(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to apply cell styles.");
            return;
        }
        String cellAddress = selectedCellId.getText();
        if (cellAddress != null && !cellAddress.isEmpty()) {
            int row = CoordinateFactory.getRowIndex(cellAddress);
            int column = CoordinateFactory.getColumnIndex(cellAddress);
            String backgroundColor = ColorUtils.colorToHex(backgroundColorPicker.getValue());
            String textColor = ColorUtils.colorToHex(textColorPicker.getValue());
            String style = "-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + "; -fx-fill: " + textColor + ";";

            spreadsheetGridController.applyCellStyle(row, column, style);
        }
    }

    @FXML
    private void handleVersionSelection(int selectedVersion) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to view past versions.");
            return;
        }
        try {
            // Fetch the sheet for the selected version
            SheetReadActions versionSheet = engine.getSheetVersion(selectedVersion);

            // Pass the versioned sheet to the SheetController to display in a popup
            SheetDisplayHelper.displayVersionInPopup(versionSheet, selectedVersion);

        } catch (Exception e) {
            UIHelper.showError("Error displaying version", e.getMessage());
        }
    }

    @FXML
    private void handleResetStyle(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to reset cell styles.");
            return;
        }
        String cellAddress = selectedCellId.getText();
        if (cellAddress != null && !cellAddress.isEmpty()) {
            int row = CoordinateFactory.getRowIndex(cellAddress);
            int column = CoordinateFactory.getColumnIndex(cellAddress);
            spreadsheetGridController.resetCellStyle(row, column);
        }
    }

    public void updateSelectedCell(String cellAddress, String originalValue, String versionString) {
        selectedCellId.setText(cellAddress);
        originalCellValue.setText(originalValue);
        lastUpdateCellVersion.setText(versionString);
    }

    public void clearSelectedCell() {
        selectedCellId.clear();
        originalCellValue.clear();
        lastUpdateCellVersion.clear();
    }

    @FXML
    private void handleAddRange(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to define a range.");
            return;
        }
        // Create a dialog to input range name and definition
        TextInputDialog rangeDialog = new TextInputDialog();
        rangeDialog.setTitle("Add New Range");
        rangeDialog.setHeaderText("Define a New Range");
        rangeDialog.setContentText("Enter range name (unique):");

        // Create input fields for range definition
        TextField rangeNameField = new TextField();
        rangeNameField.setPromptText("Unique Range Name");

        TextField rangeDefinitionField = new TextField();
        rangeDefinitionField.setPromptText("Range Definition (e.g., A1..A4)");

        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(
                new Label("Range Name:"), rangeNameField,
                new Label("Range Definition:"), rangeDefinitionField
        );
        rangeDialog.getDialogPane().setContent(dialogContent);

        Optional<String> result = rangeDialog.showAndWait();

        if (result.isPresent()) {
            String rangeName = rangeNameField.getText().trim();
            String rangeDefinition = rangeDefinitionField.getText().trim();

            try {
                engine.addRange(rangeName, rangeDefinition); // Add range using engine method
                UIHelper.showAlert("Success", "Range added successfully.");
            } catch (Exception e) {
                UIHelper.showError("Error Adding Range", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteRange(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to delete a range.");
            return;
        }
        Set<String> ranges = engine.getAllRanges();
        List<String> rangeList = new ArrayList<>(ranges);
        ChoiceDialog<String> deleteDialog = new ChoiceDialog<>(rangeList.isEmpty() ? null : rangeList.get(0), rangeList);
        deleteDialog.setTitle("Delete Range");
        deleteDialog.setHeaderText("Select a Range to Delete");
        deleteDialog.setContentText("Choose a range:");

        Optional<String> result = deleteDialog.showAndWait();

        if (result.isPresent()) {
            String rangeName = result.get();
            try {
                engine.deleteRange(rangeName);
                UIHelper.showAlert("Success", "Range deleted successfully.");
            } catch (Exception e) {
                UIHelper.showError("Error Deleting Range", e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewRange(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to view ranges.");
            return;
        }
        // Fetch all ranges from the engine
        Set<String> ranges = engine.getAllRanges();

        if (ranges.isEmpty()) {
            UIHelper.showAlert("No ranges defined", "There are no ranges currently defined to view.");
            return;
        }

        // Create a Choice Dialog to select a range
        ChoiceDialog<String> dialog = new ChoiceDialog<>(ranges.iterator().next(), ranges);
        dialog.setTitle("View Range");
        dialog.setHeaderText("Select a range to view:");
        dialog.setContentText("Available Ranges:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(rangeName -> {
            try {
                // Get the set of coordinates for the selected range
                Set<Coordinate> rangeCoordinates = engine.getRangeCoordinates(rangeName);

                // Highlight the cells within this range
                spreadsheetGridController.highlightRange(rangeCoordinates);

            } catch (Exception e) {
                UIHelper.showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to apply filters.");
            return;
        }
        FilterDialog filterDialogMaker = new FilterDialog(this.spreadsheetGridController,
                engine.getReadOnlySheet().getMaxRows(), engine.getReadOnlySheet().getMaxColumns());
        // Open the column selection dialog
        Dialog<String> columnDialog = filterDialogMaker.createColumnSelectionDialog();
        Optional<String> result = columnDialog.showAndWait();

        if (result.isPresent()) {
            String selectedColumn = result.get();
            int filterColumnIndex = CoordinateFactory.convertColumnLabelToIndex(selectedColumn.trim().toUpperCase());

            // Proceed to open the filter dialog with the selected column
            Dialog<FilterParameters> filterDialog = filterDialogMaker.createFilterDialog(filterColumnIndex);
            Optional<FilterParameters> filterResult = filterDialog.showAndWait();

            if (filterResult.isPresent()) {
                FilterParameters params = filterResult.get();
                String rangeInput = params.getRangeInput().trim().toUpperCase();
                List<String> selectedValues = params.getFilterValues();

                try {
                    ValidationUtils.validateFilterInput(rangeInput, selectedColumn, selectedValues, engine.getReadOnlySheet().getMaxRows(), engine.getReadOnlySheet().getMaxColumns());
                    // Parse the range and column
                    CellRange range = ParsingUtils.parseRange(rangeInput);

                    // Trigger filtering in SheetController
                    spreadsheetGridController.showFilteredData(range, filterColumnIndex, selectedValues);
                } catch (Exception e) {
                    UIHelper.showError("Invalid input format.", e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleMultiColumnFilter(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to apply multi-column filters.");
            return;
        }
        // Open the dialog to select columns and filter values
        FilterDialog filterDialogMaker = new FilterDialog(this.spreadsheetGridController,
                engine.getReadOnlySheet().getMaxRows(), engine.getReadOnlySheet().getMaxColumns());
        Dialog<MultiColFilterParameters> dialog = filterDialogMaker.createMultiColumnFilterDialog();
        Optional<MultiColFilterParameters> result = dialog.showAndWait();

        if (result.isPresent()) {
            MultiColFilterParameters filterParams = result.get();



            // Pass the filter parameters and range to the SheetController
            spreadsheetGridController.applyMultiColumnFilter(filterParams.getRangeInput(), filterParams.getFilterCriteria());
        }
    }

    @FXML
    private void handleApplyAlignment(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to apply column alignment.");
            return;
        }
        String selectedColumn = columnChoiceBox.getSelectionModel().getSelectedItem();
        int column = CoordinateFactory.getColumnIndexFromLabel(selectedColumn);
        String alignment = alignmentChoiceBox.getValue();
        if (alignment != null && !alignment.isEmpty()) {
            try {
                engine.setColumnAlignment(column, alignment);
                spreadsheetGridController.adjustColumnAlignment(column);
            } catch (Exception e) {
                UIHelper.showError("Error Applying Alignment", e.getMessage());
            }
        }



    }

    @FXML
    private void handleCreateGraph(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to generate a graph.");
            return;
        }
        Dialog<GraphParameters> dialog = GraphGenerator.createGraphConfigDialog();
        Optional<GraphParameters> result = dialog.showAndWait();

        if (result.isPresent()) {
            GraphParameters params = result.get();
            // Pass the graph parameters to generate the graph
            spreadsheetGridController.generateGraph(params);
        }
    }

}

