package desktop.utils.sheet;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import desktop.utils.cell.CellRange;
import desktop.utils.cell.CellWrapper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SheetUtils {

    public static Object getSortableValue(CellWrapper cellWrapper) {
        if (cellWrapper == null || cellWrapper.getCell() == null) {
            return null;
        }
        EffectiveValue effectiveValue = cellWrapper.getCell().getEffectiveValue();
        if (effectiveValue != null) {
            return effectiveValue.getValue();
        }
        return null;
    }

    // Utility method to get alignment based on selected RadioMenuItem
    public static Pos getAlignmentFromToggle(Toggle selectedToggle, RadioMenuItem leftAlign, RadioMenuItem centerAlign, RadioMenuItem rightAlign) {
        if (selectedToggle == leftAlign) {
            return Pos.CENTER_LEFT;
        } else if (selectedToggle == rightAlign) {
            return Pos.CENTER_RIGHT;
        } else {
            return Pos.CENTER; // Default to center alignment
        }
    }

    // Utility method to determine wrap/clip setting based on selected toggle
    public static boolean isWrapTextSelected(Toggle selectedToggle, RadioMenuItem wrapText) {
        return selectedToggle == wrapText;
    }


    // Method to create and return a fully configured context menu with alignment and wrap options
    public static ContextMenu createColumnContextMenu(ToggleGroup alignmentGroup, ToggleGroup wrapGroup,
                                                      RadioMenuItem leftAlign, RadioMenuItem centerAlign,
                                                      RadioMenuItem rightAlign, RadioMenuItem wrapText,
                                                      RadioMenuItem clipText) {

        // Create the Alignment Menu
        Menu alignmentMenu = createAlignmentMenu(alignmentGroup, leftAlign, centerAlign, rightAlign);

        // Create the Wrap/Clip Menu
        Menu wrapMenu = createWrapMenu(wrapGroup, wrapText, clipText);

        // Create and configure the context menu
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(alignmentMenu, wrapMenu);

        return contextMenu;
    }

    // Method to create the Alignment Menu
    public static Menu createAlignmentMenu(ToggleGroup alignmentGroup, RadioMenuItem leftAlign,
                                           RadioMenuItem centerAlign, RadioMenuItem rightAlign) {
        Menu alignmentMenu = new Menu("Alignment");

        leftAlign.setToggleGroup(alignmentGroup);
        centerAlign.setToggleGroup(alignmentGroup);
        rightAlign.setToggleGroup(alignmentGroup);

        centerAlign.setSelected(true); // Default alignment to center

        alignmentMenu.getItems().addAll(leftAlign, centerAlign, rightAlign);
        return alignmentMenu;
    }

    // Method to create the Wrap/Clip Menu
    public static Menu createWrapMenu(ToggleGroup wrapGroup, RadioMenuItem wrapText, RadioMenuItem clipText) {
        Menu wrapMenu = new Menu("Text Handling");

        wrapText.setToggleGroup(wrapGroup);
        clipText.setToggleGroup(wrapGroup);

        wrapText.setSelected(true); // Default to wrap

        wrapMenu.getItems().addAll(wrapText, clipText);
        return wrapMenu;
    }

    public static Pos getCurrentAlignment(ToggleGroup alignmentGroup) {
        RadioMenuItem selected = (RadioMenuItem) alignmentGroup.getSelectedToggle();
        if (selected != null) {
            switch (selected.getText()) {
                case "Left":
                    return Pos.CENTER_LEFT;
                case "Right":
                    return Pos.CENTER_RIGHT;
                default:
                    return Pos.CENTER;
            }
        }
        return Pos.CENTER;
    }

    public static void showSortedDataHalper(CellRange range, List<Integer> sortColumns, ObservableList<ObservableList<CellWrapper>> data) {
            List<ObservableList<CellWrapper>> dataToSort = new ArrayList<>();
            for (int i = range.startRow; i <= range.endRow; i++) {
                ObservableList<CellWrapper> originalRow = data.get(i);
                ObservableList<CellWrapper> rowCopy = FXCollections.observableArrayList();

                for (int j = range.startCol; j <= range.endCol; j++) {
                    CellWrapper originalCell = originalRow.get(j);
                    CellWrapper cellCopy = new CellWrapper(originalCell.getCell(), originalCell.getOriginalRow(), originalCell.getColumn());
                    cellCopy.setStyle(originalCell.getStyle());
                    rowCopy.add(cellCopy);
                }
                dataToSort.add(rowCopy);
            }

            Collections.sort(dataToSort, new Comparator<ObservableList<CellWrapper>>() {
                @Override
                public int compare(ObservableList<CellWrapper> row1, ObservableList<CellWrapper> row2) {
                    for (int colIndex : sortColumns) {
                        if (colIndex < range.startCol || colIndex > range.endCol) {
                            continue;
                        }

                        Object value1 = SheetUtils.getSortableValue(row1.get(colIndex - range.startCol));
                        Object value2 = SheetUtils.getSortableValue(row2.get(colIndex - range.startCol));

                        if (!(value1 instanceof Number) || !(value2 instanceof Number)) {
                            continue;
                        }

                        int cmp = Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
                        if (cmp != 0) {
                            return cmp;
                        }
                    }
                    return 0;
                }
            });

            SheetDisplayHelper.displaySortedDataInPopup(dataToSort, range);
    }

    public static void showFilteredDataHalper(CellRange range, int filterColumnIndex, List<String> selectedValues,ObservableList<ObservableList<CellWrapper>> data) {
            List<ObservableList<CellWrapper>> filteredData = new ArrayList<>();

            // Loop through each row within the range
            for (int i = range.startRow; i <= range.endRow; i++) {
                ObservableList<CellWrapper> originalRow = data.get(i);
                CellWrapper cellToFilter = originalRow.get(filterColumnIndex);

                if (cellToFilter != null && cellToFilter.getCell() != null) {
                    EffectiveValue effectiveValue = cellToFilter.getCell().getEffectiveValue();
                    if (effectiveValue != null) {
                        String cellValue = effectiveValue.getValue().toString();

                        // Check if the cell value is one of the selected values
                        if (selectedValues.contains(cellValue)) {
                            // Create a copy of the row with its styles
                            ObservableList<CellWrapper> rowCopy = FXCollections.observableArrayList();
                            for (int j = range.startCol; j <= range.endCol; j++) {
                                CellWrapper originalCell = originalRow.get(j);
                                CellWrapper cellCopy = new CellWrapper(originalCell.getCell(), originalCell.getOriginalRow(), originalCell.getColumn());
                                cellCopy.setStyle(originalCell.getStyle());
                                rowCopy.add(cellCopy);
                            }
                            filteredData.add(rowCopy);
                        }
                    }
                }
            }

            // Display the filtered data in a popup
            SheetDisplayHelper.displayFilteredDataInPopup(filteredData, range);
    }


}
