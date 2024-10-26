package sheetDisplay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.utils.ColumnRowPropertyManager;
import dashboard.DashboardController;
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
import org.jetbrains.annotations.NotNull;
import sheetDisplay.sheet.SheetController;
import utils.UIHelper;
import utils.ValidationUtils;
import utils.cell.CellRange;
import utils.color.ColorUtils;
import utils.dialogs.FilterDialog;
import utils.dialogs.SortDialog;
import utils.http.HttpClientUtil;
import utils.http.RequestUtils;
import utils.parameters.FilterParameters;
import utils.parameters.SortParameters;
import utils.parsing.ParsingUtils;
import okhttp3.*;
import utils.sheet.SheetPopupUtils;

import java.io.IOException;
import java.util.*;

import static utils.UIHelper.showError;

public class SheetDisplayController {


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
    private ColumnRowPropertyManager columnRowPropertyManager;


    private final Gson gson = new Gson();
    private Stage stage;
    boolean isSheetLoaded = false;
    String UserName;
    int currentVersion;
    int latestVersion;
    @FXML
    private void initialize() {
        if (spreadsheetGridController != null) {
            spreadsheetGridController.setSheetDisplayController(this);
        }


        versionSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Parse the selected version number
                int selectedVersion = ParsingUtils.parseVersion(newValue);
                //int currentVersion = engine.getReadOnlySheet() != null ? engine.getReadOnlySheet().getVersion() : 0;

                // Only display the popup if a past version is selected
                if (selectedVersion != currentVersion && selectedVersion > 0) {
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
                    //engine.setColumnWidth(column, width);
                    spreadsheetGridController.adjustAllColumnWidth();
                }
            } else {
                columnWidthField.setText(oldValue); // Revert to the old value if input is invalid
            }
        });

    }

    public void populateTableView(Map<String, Object> sheetData){
        spreadsheetGridController.displaySheet(sheetData);
        initializeVersionSelector(sheetData);
    }

    public void setColumnRowPropertyManager(ColumnRowPropertyManager columnRowPropertyManager,int maxColumn,int maxRows) {
        this.columnRowPropertyManager = columnRowPropertyManager;
        populateColumnChoiceBox(maxColumn);
        populateRowChoiceBox(maxRows);
    }

    private void populateColumnChoiceBox(int maxColumns) {
        // Clear existing choices
        if (columnChoiceBox != null && columnChoiceBox.getItems() != null) {
            columnChoiceBox.getItems().clear();
        }

        for (int i = 0; i < maxColumns; i++) {
            columnChoiceBox.getItems().add("Column " + CoordinateFactory.convertIndexToColumnLabel(i));
        }

        // Reset visibility
        columnControls.setVisible(false);
    }

    private void populateRowChoiceBox(int maxRows) {
        if (rowChoiceBox != null && rowChoiceBox.getItems() != null) {
            rowChoiceBox.getItems().clear();
        }
        for (int i = 0; i < maxRows; i++) {
            rowChoiceBox.getItems().add(i + 1);  // Display row number starting from 1
        }
        rowControls.setVisible(false);  // Hide the controls until a row is selected
    }

    public void initializeSheetView(SheetUserData sheetData, String username) {
        System.out.println("SheetDisplayController initializeSheetView");
        // Set the sheet name
        sheetNameField.setText(sheetData.getSheetName());
        UserName = username;
        // Set the username
        usernameLabel.setText("Welcome, " + username);

        // Initialize cell styles
        //initializeCellStyling();

        // Load the sheet data into the spreadsheet grid
        loadSheetIntoGrid(sheetData);

        // Mark the sheet as loaded
        isSheetLoaded = true;
        System.out.println("SheetDisplayController initializeSheetView DONE");
    }

    public void initializeVersionSelector(Map<String, Object> sheetData){
        // Extract sheet versions (List of Map)
        List<Map<String, Object>> versions = (List<Map<String, Object>>) sheetData.get("versions");
        Map<Integer,Integer> sheetVersions = new HashMap<>();
        for (Map<String, Object> versionEntry : versions) {
            int version = ((Number) versionEntry.get("version")).intValue();
            int cellChanges = ((Number) versionEntry.get("cellChanges")).intValue();
            sheetVersions.put(version,cellChanges);
        }

        // Extract current version
        int currentVersion = ((Number) sheetData.get("currentVersion")).intValue();
        this.currentVersion = currentVersion;
        populateVersionSelector(sheetVersions, currentVersion);
    }

    private void populateVersionSelector(Map<Integer, Integer> sheetVersions, int currentVersion) {
        // Clear existing choices
        if (versionSelector != null && versionSelector.getItems() != null) {
            versionSelector.getItems().clear();
        }
        int tempMaxVersion = currentVersion;
        String currentVersionString = "Version " + currentVersion + " (" + sheetVersions.get(currentVersion) + " changes)";
        versionSelector.getItems().add(currentVersionString);
        TreeMap<Integer, Integer> sortedVersions = new TreeMap<>(sheetVersions);

        // Add the past versions in order
        for (Map.Entry<Integer, Integer> entry : sortedVersions.entrySet()) {
            int version = entry.getKey();
            if (version > tempMaxVersion) {
                tempMaxVersion = version;
            }
            if (version != currentVersion) {
                versionSelector.getItems().add("Version " + version + " (" + entry.getValue() + " changes)");
            }
        }
        versionSelector.setValue(currentVersionString);
        this.latestVersion = tempMaxVersion;
    }

    private void loadSheetIntoGrid(SheetUserData sheetData) {
        // Assuming SheetUserData has a method to get the sheet's data
        // Pass the data to the spreadsheetGridController
       // spreadsheetGridController.loadSheetData(sheetData);
    }

    public void setSpreadsheetGridController(SheetController spreadsheetGridController) {
        this.spreadsheetGridController = spreadsheetGridController;
        spreadsheetGridController.setSheetDisplayController(this);
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            // Load the dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent dashboardParent = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.initializeDashboard(UserName);
            // Get the current stage (window) using the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the scene to the dashboard and show it
            stage.setScene(new Scene(dashboardParent));
            stage.show();

        } catch (IOException e) {
            // Handle any exceptions, such as file not found or FXML loading errors
            e.printStackTrace();
            showError("Error", "Failed to load the dashboard. Please try again.");
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
    public void handleUpdateValue() {
        // Implementation here
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

    @FXML
    private void handleVersionSelection(int selectedVersion) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to view past versions.");
            return;
        }
        if(selectedVersion==latestVersion && currentVersion!=latestVersion){
            RequestUtils.fetchSheetDataFromServer(sheetNameField.getText(), this);
            currentVersion=latestVersion;
        }
        if (selectedVersion > 0 && selectedVersion != currentVersion){
            RequestUtils.displayVersionSheetData(sheetNameField.getText(), selectedVersion);
        }
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
                ValidationUtils.validateSortInput(rangeInput, columnsInput, columnRowPropertyManager.getMaxRow(), columnRowPropertyManager.getMaxColumn());
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
    private void handleFilter(ActionEvent event) {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to apply filters.");
            return;
        }
        FilterDialog filterDialogMaker = new FilterDialog(this.spreadsheetGridController,
                columnRowPropertyManager.getMaxRow(), columnRowPropertyManager.getMaxColumn());
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
                    ValidationUtils.validateFilterInput(rangeInput, selectedColumn, selectedValues, columnRowPropertyManager.getMaxRow(), columnRowPropertyManager.getMaxColumn());
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
    private void handleColumnSelection(ActionEvent event) {
        String selectedColumn = columnChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedColumn != null && !selectedColumn.isEmpty()) {
            int column = CoordinateFactory.getColumnIndexFromLabel(selectedColumn);
            columnControls.setVisible(true);
            columnWidthField.setText(String.valueOf(columnRowPropertyManager.getColumnProperties(column).getWidth()));
            alignmentChoiceBox.setValue(columnRowPropertyManager.getColumnProperties(column).getAlignment());
        } else {
            columnControls.setVisible(false);
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
                //engine.setColumnAlignment(column, alignment);
                spreadsheetGridController.adjustColumnAlignment(column);
            } catch (Exception e) {
                showError("Error Applying Alignment", e.getMessage());
            }
        }

    }

    @FXML
    private void handleRowSelection(ActionEvent event) {
        Integer selectedRow = rowChoiceBox.getValue();
        if (selectedRow != null) {
            rowControls.setVisible(true);
            rowHeightField.setText(String.valueOf(columnRowPropertyManager.getRowProperties(selectedRow - 1).getHeight()));
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
                //engine.setRowHeight(selectedRow - 1, newRowHeight);
                spreadsheetGridController.adjustRowHeight(selectedRow - 1, newRowHeight);
            } else {
                showError("Invalid Input", "Please enter a valid row height.");
            }
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Row height must be a number.");
        }
    }



    @FXML
    public void handleAddRange() {
        if (!ValidationUtils.canUpdateSpreadsheet(isSheetLoaded, currentVersion, latestVersion)) {
            return; // Early exit if validation fails
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

            if (!rangeName.isEmpty() && !rangeDefinition.isEmpty()) {
                // Send range data to the server
                RequestUtils.addRange(sheetNameField.getText(), rangeName, rangeDefinition);
            } else {
                showError("Input Error", "Range name or definition cannot be empty.");
            }
        }
    }


    @FXML
    public void handleDeleteRange() {
        // Check if the user is allowed to update (only on the latest version)
        if (!ValidationUtils.canUpdateSpreadsheet(isSheetLoaded, currentVersion, latestVersion)) {
            return; // Early exit if validation fails
        }

        // Fetch all ranges from the server
        Request request = new Request.Builder()
                .url("http://localhost:8080/webapp/getRanges?sheetName=" + sheetNameField.getText()) // Adjust the URL as needed
                .get()
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to retrieve ranges: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Set<String> ranges = gson.fromJson(responseBody, new TypeToken<Set<String>>() {}.getType());

                    Platform.runLater(() -> {
                        // If no ranges are available, show an alert
                        if (ranges.isEmpty()) {
                            UIHelper.showAlert("No ranges defined", "There are no ranges currently defined to delete.");
                            return;
                        }

                        // Show the ChoiceDialog to select a range to delete
                        List<String> rangeList = new ArrayList<>(ranges);
                        ChoiceDialog<String> deleteDialog = new ChoiceDialog<>(rangeList.isEmpty() ? null : rangeList.get(0), rangeList);
                        deleteDialog.setTitle("Delete Range");
                        deleteDialog.setHeaderText("Select a Range to Delete");
                        deleteDialog.setContentText("Choose a range:");

                        Optional<String> result = deleteDialog.showAndWait();

                        result.ifPresent(rangeName -> {
                            // Send a request to delete the selected range
                            RequestUtils.deleteRange(sheetNameField.getText(), rangeName);
                        });
                    });
                } else {
                    Platform.runLater(() -> showError("Failed to retrieve ranges: Server error"));
                }
                response.close(); // Ensure the response is closed to prevent leaks
            }
        });
    }


    @FXML
    public void handleViewRange() {
        if (!isSheetLoaded) {
            UIHelper.showAlert("No sheet loaded", "Please load a spreadsheet file to view ranges.");
            return;
        }
        if (currentVersion <= 0) {
            UIHelper.showAlert("Invalid version", "The current version is invalid.");
            return;
        }

        // Fetch available ranges from the server for the selected sheet and current version
        Request request = new Request.Builder()
                .url("http://localhost:8080/webapp/getSheetRanges?sheetName=" + sheetNameField.getText()
                        + "&currentVersion=" + currentVersion)
                .get()
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to retrieve ranges: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response as a Map of range names to lists of Coordinate objects
                    String responseBody = response.body().string();
                    Map<String, List<Coordinate>> ranges = gson.fromJson(responseBody, new TypeToken<Map<String, List<Coordinate>>>(){}.getType());

                    Platform.runLater(() -> {
                        if (ranges.isEmpty()) {
                            UIHelper.showAlert("No ranges defined", "There are no ranges currently defined to view.");
                            return;
                        }
                        // Create a Choice Dialog to select a range
                        ChoiceDialog<String> dialog = new ChoiceDialog<>(ranges.keySet().iterator().next(), ranges.keySet());
                        dialog.setTitle("View Range");
                        dialog.setHeaderText("Select a range to view:");
                        dialog.setContentText("Available Ranges:");

                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(rangeName -> {
                            // Get the list of Coordinate objects for the selected range
                            List<Coordinate> rangeCoordinates = ranges.get(rangeName);
                            spreadsheetGridController.highlightRange(new HashSet<>(rangeCoordinates));
                        });
                    });
                } else {
                    Platform.runLater(() -> showError("Failed to retrieve ranges: Server error"));
                }
                response.close();
            }

        });
    }








}

