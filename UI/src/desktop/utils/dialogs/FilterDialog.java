package desktop.utils.dialogs;

import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.utils.RangeValidator;
import desktop.sheet.SheetController;
import desktop.utils.parameters.FilterParameters;
import desktop.utils.parameters.MultiColFilterParameters;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterDialog {

    @FXML
    private SheetController spreadsheetGridController;

    private final int maxRows;
    private final int maxCols;

    public FilterDialog(SheetController spreadsheetGridController, int maxRows, int maxCols) {
        this.spreadsheetGridController = spreadsheetGridController;
        this.maxRows = maxRows;
        this.maxCols = maxCols;
    }

    public Dialog<String> createColumnSelectionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Column for Filtering");

        // Set the button types
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Create the column choice box
        ChoiceBox<String> columnChoiceBox = new ChoiceBox<>();
        columnChoiceBox.setPrefWidth(150);

        // Populate the choice box with column names (A, B, C, etc.)
        for (int i = 0; i < maxCols; i++) {
            columnChoiceBox.getItems().add(CoordinateFactory.convertIndexToColumnLabel(i)); // Assuming getColumnName(i) exists in SheetController
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


    public Dialog<FilterParameters> createFilterDialog(int columnIndex) {
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

    public Dialog<MultiColFilterParameters> createMultiColumnFilterDialog() {
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
        for (int i = 0; i < maxCols; i++) {
            final int colIndex = i;  // Declare as final or effectively final for use in the lambda

            // Get the unique values for the current column
            List<String> uniqueValues = spreadsheetGridController.getUniqueValuesInColumn(colIndex);

            if (!uniqueValues.isEmpty()) {
                ListView<String> valueSelectionList = new ListView<>(FXCollections.observableArrayList(uniqueValues));
                valueSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                valueSelectionList.setPrefHeight(100); // Set a reasonable height for the ListView

                // Label for the column
                Label columnLabel = new Label("Select values for column " + CoordinateFactory.convertIndexToColumnLabel(colIndex));

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
                RangeValidator rangeValidator = new RangeValidator(maxRows,
                        maxCols);
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

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);  // You can set a header if you want, or leave it as null
        alert.setContentText(message);
        alert.showAndWait();
    }

}
