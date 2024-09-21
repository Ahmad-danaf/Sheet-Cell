package desktop.utils.dialogs;

import desktop.utils.parameters.SortParameters;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class SortDialog {

    public Dialog<SortParameters> createSortDialog() {
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
}
