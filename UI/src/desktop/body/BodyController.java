package desktop.body;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.RangeValidator;
import desktop.CellRange;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    private Map<Integer, Integer> columnHeights = new HashMap<>();



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
        for (int column : columns) {
            if (column < 0 || column >= maxColumns) {
                throw new IllegalArgumentException("Column out of bounds. Please enter valid columns.");
            }
        }
    }


    @FXML
    void handleUpdateValue(ActionEvent event) {
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
            engine.setCellValue(cellAddress, newValue);
            spreadsheetGridController.refreshSpreadsheet();
            int newVersion = engine.getReadOnlySheet().getCell(row, column).getVersion();
            lastUpdateCellVersion.setText(String.valueOf(newVersion));

            spreadsheetGridController.reselectCell(row, column);
            showAlert("Cell Updated", "Cell value updated successfully.");
        } catch (Exception e) {
            showAlert("Error updating cell", e.getMessage());
        }
    }

    @FXML
    private void handleApplyStyle(ActionEvent event) {
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
    private void handleResetStyle(ActionEvent event) {
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
    void handleFilter(ActionEvent event) {

    }



    // Handle applying column alignment
    @FXML
    private void handleApplyAlignment(ActionEvent event) {

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
