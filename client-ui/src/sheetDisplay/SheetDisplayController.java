package sheetDisplay;

import com.google.gson.Gson;
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

    private final Gson gson = new Gson();
    private Stage stage;
    boolean isSheetLoaded = false;
    String UserName;
    @FXML
    private void initialize() {
        System.out.println("SheetDisplayController initialized");
        if (spreadsheetGridController != null) {
            System.out.println("SheetController is not null");
            spreadsheetGridController.setSheetDisplayController(this);
        }


//        versionSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null && !newValue.isEmpty()) {
//                // Parse the selected version number
//                int selectedVersion = ParsingUtils.extractNewVersionSheet(newValue);
//                int currentVersion = engine.getReadOnlySheet() != null ? engine.getReadOnlySheet().getVersion() : 0;
//
//                // Only display the popup if a past version is selected
//                if (selectedVersion < currentVersion && selectedVersion > 0) {
//                    handleVersionSelection(selectedVersion);
//                }
//            }
//        });
//
//        columnWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue.matches("\\d*")) {
//                if (!newValue.isEmpty()) {
//                    int width = Integer.parseInt(newValue);
//                    String selectedColumn = columnChoiceBox.getSelectionModel().getSelectedItem();
//                    int column = CoordinateFactory.getColumnIndexFromLabel(selectedColumn);
//                    engine.setColumnWidth(column, width);
//                    spreadsheetGridController.adjustAllColumnWidth();
//                }
//            } else {
//                columnWidthField.setText(oldValue); // Revert to the old value if input is invalid
//            }
//        });

        System.out.println("SheetDisplayController initialized DONE");
    }

    public void populateTableView(Map<String, Object> sheetData){
        spreadsheetGridController.populateTableView(sheetData);
    }

    public void initializeSheetView(SheetUserData sheetData, String username) {
        System.out.println("SheetDisplayController initializeSheetView");
        // Set the sheet name
        sheetNameField.setText(sheetData.getSheetName());
        UserName = username;
        // Set the username
        usernameLabel.setText("Welcome, " + username);
        // Initialize the version selector
        //initializeVersionSelector(sheetData);

        // Initialize cell styles
        //initializeCellStyling();

        // Load the sheet data into the spreadsheet grid
        loadSheetIntoGrid(sheetData);

        // Mark the sheet as loaded
        isSheetLoaded = true;
        System.out.println("SheetDisplayController initializeSheetView DONE");
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
    public void handleApplyStyle() {
        // Implementation here
    }

    @FXML
    public void handleResetStyle() {
        // Implementation here
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
    public void handleColumnSelection() {
        // Implementation here
    }

    @FXML
    public void handleApplyAlignment() {
        // Implementation here
    }

    @FXML
    public void handleRowSelection() {
        // Implementation here
    }

    @FXML
    public void handleApplyRowHeight() {
        // Implementation here
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

