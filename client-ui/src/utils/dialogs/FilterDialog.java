package utils.dialogs;
import sheetDisplay.sheet.SheetController;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.utils.RangeValidator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import utils.parameters.FilterParameters;
import utils.parameters.MultiColFilterParameters;

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



    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);  // You can set a header if you want, or leave it as null
        alert.setContentText(message);
        alert.showAndWait();
    }

}
