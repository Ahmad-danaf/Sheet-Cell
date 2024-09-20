package desktop.body;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.RangeValidator;
import com.sheetcell.engine.utils.SheetUpdateResult;
import desktop.utils.CellRange;
import desktop.utils.FilterParameters;
import desktop.utils.MultiColFilterParameters;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import desktop.sheet.SheetController;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class BodyController {

    private Engine engine;

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private BorderPane mainPane;
    // Top section components
    @FXML
    private Button loadFileButton;
    @FXML
    private TextField filePathField;
    @FXML
    private TextField selectedCellId;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private ColorPicker textColorPicker;
    @FXML
    private TextField originalCellValue;
    @FXML
    private Button updateValueButton;
    @FXML
    private TextField lastUpdateCellVersion;
    @FXML
    private ChoiceBox<String> versionSelector;

    // Left section components
    @FXML
    private Button sortButton;
    @FXML
    private Button filterButton;
    @FXML
    private Button addRangeButton;
    @FXML
    private Button deleteRangeButton;
    @FXML
    private Button ViewRanges;
    @FXML
    private TextField rowHeightField;
    @FXML
    private TextField columnWidthField;
    @FXML
    private ChoiceBox<String> alignmentChoiceBox;

    // Reference to SheetController
    @FXML
    private SheetController spreadsheetGridController;
    @FXML
    private ChoiceBox<String> columnChoiceBox;
    @FXML
    private Slider columnHeightSlider;
    @FXML
    private Label columnHeightLabel;
    @FXML
    private HBox columnHeightControls;

    boolean isSheetLoaded = false;



    @FXML
    private void initialize() {
        engine = new EngineImpl();
        if(spreadsheetGridController != null)
        {
            spreadsheetGridController.setEngine(engine);
            spreadsheetGridController.setBodyController(this);
        }

        columnHeightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            columnHeightLabel.setText(String.valueOf(newValue.intValue()));
            adjustColumnHeight();
        });

        versionSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Parse the selected version number
                int selectedVersion = extractNewVersionSheet(newValue);
                int currentVersion = engine.getReadOnlySheet()!=null ? engine.getReadOnlySheet().getVersion() : 0;

                // Only display the popup if a past version is selected
                if (selectedVersion < currentVersion && selectedVersion > 0) {
                    handleVersionSelection(selectedVersion);
                }
            }
        });


    }

    @FXML
    private void handleColumnSelection(ActionEvent event) {
        String selectedColumn = columnChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedColumn != null && !selectedColumn.isEmpty()) {
            columnHeightControls.setVisible(true);
        } else {
            columnHeightControls.setVisible(false);
        }
    }
    private void adjustColumnHeight() {

    }
    public void setSpreadsheetGridController(SheetController spreadsheetGridController) {
        this.spreadsheetGridController = spreadsheetGridController;
        spreadsheetGridController.setEngine(engine);
        spreadsheetGridController.setBodyController(this);
    }

    @FXML
    private void handleLoadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Spreadsheet File");
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());

        if (file != null) {
            // Create a new Task for loading the file
            Task<Void> loadFileTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        // Simulate a loading delay (1-2 seconds) to show progress
                        updateProgress(0, 100);
                        Thread.sleep(500);  // Simulating initial delay
                        updateProgress(50, 100);

                        // Load the XML file with the engine
                        engine.loadSheet(file.getAbsolutePath());
                        updateProgress(100, 100);

                        // Pass the sheet data to the SheetController
                        SheetReadActions sheetData = engine.getReadOnlySheet();
                        spreadsheetGridController.displaySheet(sheetData);
                        isSheetLoaded = true;
                        Platform.runLater(() -> {
                            versionSelector.getItems().clear();
                            versionSelector.getItems().add("Version 1"+ " Cells Changed: "+engine.getReadOnlySheet().getCellChangeCount());
                            versionSelector.getSelectionModel().select(0); // Select the first version
                        });

                        // Update the file path field on the UI thread
                        Platform.runLater(() -> filePathField.setText(file.getAbsolutePath()));
                        populateColumnChoiceBox();

                    } catch (Exception e) {
                        // If there's an error, show an alert on the UI thread
                        Platform.runLater(() -> showAlert(e.getMessage()));
                        throw e;  // Re-throw to handle in the task's failed handler
                    }
                    return null;
                }
            };

            // Show a progress indicator while the task is running
            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(loadFileTask.progressProperty());

            // Create a dialog or progress bar indicator in your UI to show progress
            Stage progressStage = new Stage();
            VBox progressRoot = new VBox(progressBar);
            Scene progressScene = new Scene(progressRoot);
            progressStage.setScene(progressScene);
            progressStage.setTitle("Loading File...");
            progressStage.setAlwaysOnTop(true);
            progressStage.show();

            // Handle task completion
            loadFileTask.setOnSucceeded(workerStateEvent -> {
                progressStage.close();  // Close progress dialog when loading is done
            });

            loadFileTask.setOnFailed(workerStateEvent -> {
                progressStage.close();  // Close progress dialog if loading fails
                //showAlert("Failed to load the file. Please check the file format and try again.");
            });

            // Start the task in a new thread
            new Thread(loadFileTask).start();
        }
    }

    private void populateColumnChoiceBox() {
        // Clear existing choices
        columnChoiceBox.getItems().clear();

        int maxColumns = engine.getReadOnlySheet().getMaxColumns();
        for (int i = 0; i < maxColumns; i++) {
            columnChoiceBox.getItems().add("Column " + CoordinateFactory.convertIndexToColumnLabel(i));
        }

        // Reset slider visibility
        columnHeightControls.setVisible(false);
    }


    @FXML
    private void handleSort(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to sort data.");
            return;
        }
        // Display a dialog to collect sorting parameters
        Dialog<SortParameters> dialog = createSortDialog();
        Optional<SortParameters> result = dialog.showAndWait();

        if (result.isPresent()) {
            SortParameters params = result.get();
            String rangeInput=params.rangeInput.trim().toUpperCase();
            String columnsInput=params.columnsInput.trim().toUpperCase();

            try {
                validateSortInput(rangeInput, columnsInput);
                // Parse the range and columns
                CellRange range = parseRange(rangeInput);
                List<Integer> sortColumns = parseColumns(columnsInput);

                // Trigger sorting in SheetController
                spreadsheetGridController.showSortedData(range, sortColumns);
            } catch (Exception e) {
                showError("Invalid input format.", e.getMessage());

            }
        }
    }

    // Helper method to create the sorting dialog
    private Dialog<SortParameters> createSortDialog() {
        Dialog<SortParameters> dialog = new Dialog<>();
        dialog.setTitle("Sort Data");

        // Set the button types
        ButtonType sortButtonType = new ButtonType("Sort", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sortButtonType, ButtonType.CANCEL);

        // Create the inputs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField rangeField = new TextField();
        rangeField.setPromptText("e.g., A3..V9");

        TextField columnsField = new TextField();
        columnsField.setPromptText("e.g., B,D,A");

        grid.add(new Label("Sort Range:"), 0, 0);
        grid.add(rangeField, 1, 0);
        grid.add(new Label("Sort Columns:"), 0, 1);
        grid.add(columnsField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to SortParameters when the sort button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sortButtonType) {
                return new SortParameters(rangeField.getText(), columnsField.getText());
            }
            return null;
        });

        return dialog;
    }

    // Class to hold the sort parameters
    private static class SortParameters {
        String rangeInput;
        String columnsInput;

        SortParameters(String rangeInput, String columnsInput) {
            this.rangeInput = rangeInput;
            this.columnsInput = columnsInput;
        }
    }


    private CellRange parseRange(String rangeInput) {
        String[] parts = rangeInput.split("\\.\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid range format.");
        }
        String startCell = parts[0];
        String endCell = parts[1];

        int startRow = getRowIndex(startCell);
        int startCol = getColumnIndex(startCell);
        int endRow = getRowIndex(endCell);
        int endCol = getColumnIndex(endCell);

        return new CellRange(startRow, startCol, endRow, endCol);
    }

    private List<Integer> parseColumns(String columnsInput) {
        String[] columnLetters = columnsInput.split(",");
        List<Integer> columns = new ArrayList<>();
        for (String colLetter : columnLetters) {
            colLetter = colLetter.trim();
            int colIndex = columnNameToIndex(colLetter);
            columns.add(colIndex);
        }
        return columns;
    }

    private void validateSortInput(String rangeInput, String columnsInput) {
        RangeValidator rangeValidator = new RangeValidator(engine.getReadOnlySheet().getMaxRows(),
                engine.getReadOnlySheet().getMaxColumns());

        if (!rangeValidator.isValidRange(rangeInput)) {
            throw new IllegalArgumentException("Invalid range. Please ensure the range is within the sheet bounds and correctly formatted.");
        }

        // Validate columns format
        if (!columnsInput.matches("^[a-zA-Z](,[a-zA-Z])*$")) {
            throw new IllegalArgumentException("Invalid columns format. Expected format: a,b,c");
        }

        // Check if the columns are within the valid range of the sheet
        List<Integer> columns = parseColumns(columnsInput);
        int maxColumns = engine.getReadOnlySheet().getMaxColumns();
        Set<Integer> uniqueColumns = new HashSet<>();
        for (int column : columns) {
            if (column < 0 || column >= maxColumns) {
                throw new IllegalArgumentException("Column out of bounds. Please enter valid columns.");
            }
            if (!uniqueColumns.add(column)) {
                throw new IllegalArgumentException("Column " + CoordinateFactory.convertIndexToColumnLabel(column) + " has already been selected. Duplicate selections are not allowed.");
            }
        }
    }


    @FXML
    void handleUpdateValue(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to update cell values.");
            return;
        }
        String cellAddress = selectedCellId.getText();
        String newValue = originalCellValue.getText();
        if (cellAddress == null || cellAddress.isEmpty()) {
            showAlert("No cell selected", "Please select a cell to update.");
            return;
        }
        if (newValue == null) {
            newValue = "";
        }
        int row = getRowIndex(cellAddress);
        int column = getColumnIndex(cellAddress);

        try{
            engine.doesCellIdVaild(cellAddress);
            SheetUpdateResult result = engine.setCellValue(cellAddress, newValue);
            if (result.isNoActionNeeded()) {
                showAlert(result.getErrorMessage());
                return;
            } else if (result.hasError()) {
                showError("Update failed: ",result.getErrorMessage());
                return;
            } else {
                System.out.println("Cell " + cellAddress + " updated successfully.");
            }
            spreadsheetGridController.refreshSpreadsheet();
            Cell changedCell = engine.getReadOnlySheet().getCell(row, column);
            if (changedCell != null) {
                int newVersion = changedCell.getVersion();
                lastUpdateCellVersion.setText(String.valueOf(newVersion));
            }
            int newVersionSheet = engine.getReadOnlySheet().getVersion();
            Platform.runLater(() -> {
                versionSelector.getItems().add("Version " + newVersionSheet + " Cells Changed: "+engine.getReadOnlySheet().getCellChangeCount());
                versionSelector.getSelectionModel().selectLast();
            });
            spreadsheetGridController.reselectCell(row, column);
            showAlert("Cell Updated", "Cell value updated successfully.");
        } catch (Exception e) {
            showAlert("Error updating cell", e.getMessage());
        }
    }

    @FXML
    private void handleApplyStyle(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to apply cell styles.");
            return;
        }
        String cellAddress = selectedCellId.getText();
        if (cellAddress != null && !cellAddress.isEmpty()) {
            int row = getRowIndex(cellAddress);
            int column = getColumnIndex(cellAddress);
            String backgroundColor = colorToHex(backgroundColorPicker.getValue());
            String textColor = colorToHex(textColorPicker.getValue());
            String style = "-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + "; -fx-fill: " + textColor + ";";

            spreadsheetGridController.applyCellStyle(row, column, style);
        }
    }

    @FXML
    private void handleVersionSelection(int selectedVersion) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to view past versions.");
            return;
        }
        try {
            // Fetch the sheet for the selected version
            SheetReadActions versionSheet = engine.getSheetVersion(selectedVersion);

            // Pass the versioned sheet to the SheetController to display in a popup
            spreadsheetGridController.displayVersionInPopup(versionSheet, selectedVersion);
            
        } catch (Exception e) {
            showError("Error displaying version", e.getMessage());
        }
    }



    public static int extractNewVersionSheet(String input) {
        // Use split method to break the string at "Version " and " Cells Changed:"
        String[] parts = input.split("Version | Cells Changed:");

        // The newVersionSheet will be in the second part of the split array
        return Integer.parseInt(parts[1].trim());
    }



    @FXML
    private void handleResetStyle(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to reset cell styles.");
            return;
        }
        String cellAddress = selectedCellId.getText();
        if (cellAddress != null && !cellAddress.isEmpty()) {
            int row = getRowIndex(cellAddress);
            int column = getColumnIndex(cellAddress);
            spreadsheetGridController.resetCellStyle(row, column);
        }
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed()*255),
                (int)(color.getGreen()*255),
                (int)(color.getBlue()*255));
    }

    private int getRowIndex(String cellAddress) {
        // Extract row index from cell address (e.g., "A1" -> 0)
        String rowPart = cellAddress.replaceAll("[^\\d]", "");
        return Integer.parseInt(rowPart) - 1;
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


    private int getColumnIndex(String cellAddress) {
        // Extract column index from cell address (e.g., "A1" -> 0)
        String colPart = cellAddress.replaceAll("\\d", "");
        return columnNameToIndex(colPart);
    }

    private int getColumnIndexFromLabel(String columnLabel) {
        // Remove the "Column " prefix
        String columnName = columnLabel.replace("Column ", "").trim();
        return columnNameToIndex(columnName);
    }


    private int columnNameToIndex(String columnName) {
        int index = 0;
        for (int i = 0; i < columnName.length(); i++) {
            index *= 26;
            index += columnName.charAt(i) - 'A' + 1;
        }
        return index - 1;
    }


    @FXML
    private void handleAddRange(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to define a range.");
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
                showAlert("Success", "Range added successfully.");
            } catch (Exception e) {
                showError("Error Adding Range", e.getMessage());
            }
        }
    }


    @FXML
    private void handleDeleteRange(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to delete a range.");
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
                showAlert("Success", "Range deleted successfully.");
            } catch (Exception e) {
                showError("Error Deleting Range", e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewRange(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to view ranges.");
            return;
        }
        // Fetch all ranges from the engine
        Set<String> ranges = engine.getAllRanges();

        if (ranges.isEmpty()) {
            showAlert("No ranges defined", "There are no ranges currently defined to view.");
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
                showError("Error", e.getMessage());
            }
        });
    }



    @FXML
    private void handleFilter(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to apply filters.");
            return;
        }
        // Open the column selection dialog
        Dialog<String> columnDialog = createColumnSelectionDialog();
        Optional<String> result = columnDialog.showAndWait();

        if (result.isPresent()) {
            String selectedColumn = result.get();
            int filterColumnIndex = columnNameToIndex(selectedColumn.trim().toUpperCase());

            // Proceed to open the filter dialog with the selected column
            Dialog<FilterParameters> filterDialog = createFilterDialog(filterColumnIndex);
            Optional<FilterParameters> filterResult = filterDialog.showAndWait();

            if (filterResult.isPresent()) {
                FilterParameters params = filterResult.get();
                String rangeInput = params.getRangeInput().trim().toUpperCase();
                List<String> selectedValues = params.getFilterValues();

                try {
                    validateFilterInput(rangeInput, selectedColumn, selectedValues);
                    // Parse the range and column
                    CellRange range = parseRange(rangeInput);

                    // Trigger filtering in SheetController
                    spreadsheetGridController.showFilteredData(range, filterColumnIndex, selectedValues);
                } catch (Exception e) {
                    showError("Invalid input format.", e.getMessage());
                }
            }
        }
    }


    private Dialog<FilterParameters> createFilterDialog(int columnIndex) {
        Dialog<FilterParameters> dialog = new Dialog<>();
        dialog.setTitle("Filter Data");

        // Set the button types
        ButtonType filterButtonType = new ButtonType("Filter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(filterButtonType, ButtonType.CANCEL);

        // Create the range input field and value choice box
        TextField rangeField = new TextField();
        rangeField.setPromptText("e.g., A3..V9");

        // Get unique values from the selected column
        List<String> uniqueValues = spreadsheetGridController.getUniqueValuesInColumn(columnIndex);

        ListView<String> valueChoiceBox = new ListView<>();
        valueChoiceBox.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        valueChoiceBox.setItems(FXCollections.observableArrayList(uniqueValues));

        Label instructionLabel = new Label("Hold down “Control” or “Command” on a Mac to select more than one.");
        instructionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray; -fx-padding: 5px;");

        // Layout for the main dialog
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.add(instructionLabel, 0, 0, 2, 1); // Add instruction label at the top
        mainGrid.add(new Label("Filter Range:"), 0, 1);
        mainGrid.add(rangeField, 1, 1);
        GridPane selectionGrid = new GridPane();
        selectionGrid.setHgap(10);
        selectionGrid.setVgap(10);
        selectionGrid.add(new Label("Select Values:"), 0, 0);
        selectionGrid.add(valueChoiceBox, 0, 1);
        mainGrid.add(selectionGrid, 0, 2, 2, 1); // Span both columns for the selection grid
        dialog.getDialogPane().setContent(mainGrid);

        // Convert the result to FilterParameters when the user clicks 'Filter'
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == filterButtonType) {
                return new FilterParameters(rangeField.getText(), valueChoiceBox.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        return dialog;
    }


    private Dialog<String> createColumnSelectionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Column for Filtering");

        // Set the button types
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Create the column choice box
        ChoiceBox<String> columnChoiceBox = new ChoiceBox<>();
        columnChoiceBox.setPrefWidth(150);

        // Get the total number of columns (you can retrieve this dynamically)
        int maxColumns = engine.getReadOnlySheet().getMaxColumns(); // Method to get the number of columns in the sheet

        // Populate the choice box with column names (A, B, C, etc.)
        for (int i = 0; i < maxColumns; i++) {
            columnChoiceBox.getItems().add(spreadsheetGridController.getColumnName(i)); // Assuming getColumnName(i) exists in SheetController
        }

        // Set the first column as default
        if (!columnChoiceBox.getItems().isEmpty()) {
            columnChoiceBox.setValue(columnChoiceBox.getItems().get(0)); // Select the first item by default
        }

        // Layout for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Select Column:"), 0, 0);
        grid.add(columnChoiceBox, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to the selected column name when the user clicks 'Select'
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return columnChoiceBox.getValue();
            }
            return null;
        });

        return dialog;
    }



    private void validateFilterInput(String rangeInput, String columnInput, List<String> filterValues) {
        RangeValidator rangeValidator = new RangeValidator(engine.getReadOnlySheet().getMaxRows(),
                engine.getReadOnlySheet().getMaxColumns());

        if (!rangeValidator.isValidRange(rangeInput)) {
            throw new IllegalArgumentException("Invalid range. Please ensure the range is within the sheet bounds and correctly formatted.");
        }

        if (!columnInput.matches("[A-Z]")) {
            throw new IllegalArgumentException("Invalid column format. Expected a single letter representing the column.");
        }

        if (filterValues.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one value to filter.");
        }
    }

    @FXML
    private void handleMultiColumnFilter(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to apply multi-column filters.");
            return;
        }
        // Open the dialog to select columns and filter values
        Dialog<MultiColFilterParameters> dialog = createMultiColumnFilterDialog();
        Optional<MultiColFilterParameters> result = dialog.showAndWait();

        if (result.isPresent()) {
            MultiColFilterParameters filterParams = result.get();

            // Print the selected range and filter criteria
            System.out.println("Selected range: " + filterParams.getRangeInput());
            System.out.println("Filter criteria: " + filterParams.getFilterCriteria());

            // Pass the filter parameters and range to the SheetController
            spreadsheetGridController.applyMultiColumnFilter(filterParams.getRangeInput(), filterParams.getFilterCriteria());
        }
    }


    private Dialog<MultiColFilterParameters> createMultiColumnFilterDialog() {
        Dialog<MultiColFilterParameters> dialog = new Dialog<>();
        dialog.setTitle("Multi-Column Filter");

        // Set the dialog buttons
        ButtonType filterButtonType = new ButtonType("Filter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(filterButtonType, ButtonType.CANCEL);

        // Create a layout for column and value selection
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        // ScrollPane to handle long content
        ScrollPane scrollPane = new ScrollPane(dialogContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);  // Set max height to control the window size
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Map to hold selected column index and corresponding selected values
        Map<Integer, List<String>> filterCriteria = new HashMap<>();

        // Add a TextField to ask for the filter range (e.g., A3..V9)
        Label rangeLabel = new Label("Enter range to filter (e.g., A3..V9):");
        TextField rangeInput = new TextField();
        rangeInput.setPromptText("Enter range");

        dialogContent.getChildren().addAll(rangeLabel, rangeInput);

        // Instruction for multi-select
        Label instructionLabel = new Label("Hold down “Control”, or “Command” on a Mac, to select more than one.");
        instructionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        dialogContent.getChildren().add(instructionLabel);

        // Loop through each column and create a ListView for selecting values in that column
        for (int i = 0; i < engine.getReadOnlySheet().getMaxColumns(); i++) {
            final int colIndex = i;  // Declare as final or effectively final for use in the lambda

            // Get the unique values for the current column
            List<String> uniqueValues = spreadsheetGridController.getUniqueValuesInColumn(colIndex);

            if (!uniqueValues.isEmpty()) {
                ListView<String> valueSelectionList = new ListView<>(FXCollections.observableArrayList(uniqueValues));
                valueSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                valueSelectionList.setPrefHeight(100); // Set a reasonable height for the ListView

                // Label for the column
                Label columnLabel = new Label("Select values for column " + spreadsheetGridController.getColumnName(colIndex));

                // Add the column label and value selection list to the dialog
                VBox columnFilterSection = new VBox(5, columnLabel, valueSelectionList);
                dialogContent.getChildren().add(columnFilterSection);

                // Update filter criteria whenever the user selects/deselects values in the ListView
                valueSelectionList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) change -> {
                    // Get the selected values for this column
                    List<String> selectedValues = new ArrayList<>(valueSelectionList.getSelectionModel().getSelectedItems());
                    if (!selectedValues.isEmpty()) {
                        // Add the selected values for this column to the filter criteria
                        filterCriteria.put(colIndex, selectedValues);
                    } else {
                        // If no values are selected, remove this column from the filter criteria
                        filterCriteria.remove(colIndex);
                    }
                });
            }
        }

        dialog.getDialogPane().setContent(scrollPane);

        // When the user clicks "Filter", return the selected columns, values, and range
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == filterButtonType) {
                String range = rangeInput.getText().trim();
                RangeValidator rangeValidator = new RangeValidator(engine.getReadOnlySheet().getMaxRows(),
                        engine.getReadOnlySheet().getMaxColumns());
                if (!rangeValidator.isValidRange(range)) {
                    showError("Invalid range", "Please enter a valid range (e.g., A3..V9)");
                    return null;
                }
                return new MultiColFilterParameters(range, filterCriteria);
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleApplyAlignment(ActionEvent event) {
        if (!isSheetLoaded) {
            showAlert("No sheet loaded", "Please load a spreadsheet file to apply column alignment.");
            return;
        }

    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);  // You can set a header if you want, or leave it as null
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Choose the type of alert
        alert.setTitle("Info");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
