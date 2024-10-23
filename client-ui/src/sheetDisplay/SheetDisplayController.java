package sheetDisplay;

import com.google.gson.Gson;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.ColumnRowPropertyManager;
import dashboard.DashboardController;
import data.SheetUserData;
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
import utils.color.ColorUtils;
import utils.parsing.ParsingUtils;

import java.io.IOException;
import java.util.*;

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
                    //engine.setColumnWidth(column, width);
                    spreadsheetGridController.adjustAllColumnWidth();
                }
            } else {
                columnWidthField.setText(oldValue); // Revert to the old value if input is invalid
            }
        });

    }

    public void populateTableView(Map<String, Object> sheetData){
        spreadsheetGridController.populateTableView(sheetData);
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
        initPopulateVersionSelector(sheetVersions, currentVersion);
    }

    private void initPopulateVersionSelector(Map<Integer, Integer> sheetVersions, int currentVersion) {
        // Clear existing choices
        if (versionSelector != null && versionSelector.getItems() != null) {
            versionSelector.getItems().clear();
        }
        String currentVersionString = "Version " + currentVersion + " (" + sheetVersions.get(currentVersion) + " changes)";
        versionSelector.getItems().add(currentVersionString);
        // Add the past versions
        for (Map.Entry<Integer, Integer> entry : sheetVersions.entrySet()) {
            int version = entry.getKey();
            if (version < currentVersion) {
                versionSelector.getItems().add("Version " + version + " (" + entry.getValue() + " changes)");
            }
        }
        versionSelector.setValue(currentVersionString);
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
            UIHelper.showError("Error", "Failed to load the dashboard. Please try again.");
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
    }

    @FXML
    public void handleSort() {
        // Implementation here
    }

    @FXML
    public void handleFilter() {
        // Implementation here
    }

    @FXML
    public void handleMultiColumnFilter() {
        // Implementation here
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
                UIHelper.showError("Error Applying Alignment", e.getMessage());
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
                UIHelper.showError("Invalid Input", "Please enter a valid row height.");
            }
        } catch (NumberFormatException e) {
            UIHelper.showError("Invalid Input", "Row height must be a number.");
        }
    }



    @FXML
    public void handleAddRange() {
        // Implementation here
    }

    @FXML
    public void handleDeleteRange() {
        // Implementation here
    }

    @FXML
    public void handleViewRange() {
        // Implementation here
    }







}

