package desktop.body;

import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import desktop.CellRange;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import desktop.sheet.SheetController;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    // Reference to SheetController
    @FXML
    private SheetController spreadsheetGridController;



    @FXML
    private void initialize() {
        engine = new EngineImpl();
        if(spreadsheetGridController != null)
        {
            spreadsheetGridController.setEngine(engine);
            spreadsheetGridController.setBodyController(this);
        }

    }

    public void setSpreadsheetGridController(SheetController spreadsheetGridController) {
        this.spreadsheetGridController = spreadsheetGridController;
        spreadsheetGridController.setEngine(engine);
        spreadsheetGridController.setBodyController(this);
    }



    @FXML
    void handleAddRange(ActionEvent event) {

    }

    @FXML
    void handleDeleteRange(ActionEvent event) {

    }

    @FXML
    void handleFilter(ActionEvent event) {

    }

    @FXML
    private void handleLoadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Spreadsheet File");
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (file != null) {
            try {
                // Load the XML file with the engine
                engine.loadSheet(file.getAbsolutePath());
                filePathField.setText(file.getAbsolutePath());

                // Get the SheetReadActions object
                SheetReadActions sheetData = engine.getReadOnlySheet();

                // Pass the sheet data to SheetController
                spreadsheetGridController.displaySheet(sheetData);

            } catch (Exception e) {
                e.printStackTrace();
                // Show an error dialog or message
            }
        }
    }

    @FXML
    private void handleSort(ActionEvent event) {
        // Display a dialog to collect sorting parameters
        Dialog<SortParameters> dialog = createSortDialog();
        Optional<SortParameters> result = dialog.showAndWait();

        if (result.isPresent()) {
            SortParameters params = result.get();

            try {
                // Parse the range and columns
                CellRange range = parseRange(params.rangeInput);
                List<Integer> sortColumns = parseColumns(params.columnsInput);

                // Trigger sorting in SheetController
                spreadsheetGridController.showSortedData(range, sortColumns);
            } catch (Exception e) {
                showAlert("Invalid input format. Please check your entries.");
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


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    void handleUpdateValue(ActionEvent event) {

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

    private int columnNameToIndex(String columnName) {
        int index = 0;
        for (int i = 0; i < columnName.length(); i++) {
            index *= 26;
            index += columnName.charAt(i) - 'A' + 1;
        }
        return index - 1;
    }


}