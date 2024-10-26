package utils.sheet;

import com.sheetcell.engine.cell.EffectiveValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.cell.CellRange;
import utils.cell.CellWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SheetUtils {

    public static void showSortedDataHalper(CellRange range, List<Integer> sortColumns, ObservableList<ObservableList<CellWrapper>> data) {
        List<ObservableList<CellWrapper>> dataToSort = new ArrayList<>();
        for (int i = range.startRow; i <= range.endRow; i++) {
            ObservableList<CellWrapper> originalRow = data.get(i);
            ObservableList<CellWrapper> rowCopy = FXCollections.observableArrayList();

            for (int j = range.startCol; j <= range.endCol; j++) {
                CellWrapper originalCell = originalRow.get(j);
                CellWrapper cellCopy = new CellWrapper(originalCell.getOriginalValue(),originalCell.getEffectiveValue(),
                        originalCell.getVersion(),originalCell.getOriginalRow(), originalCell.getColumn());
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

        SheetPopupUtils.displaySortedDataInPopup(dataToSort, range);
    }

    public static Object getSortableValue(CellWrapper cellWrapper) {
        if (cellWrapper == null || cellWrapper.getEffectiveValue() == null) {
            return null;
        }
        return cellWrapper.getEffectiveValue();
    }

    public static void showFilteredDataHalper(CellRange range, int filterColumnIndex, List<String> selectedValues,ObservableList<ObservableList<CellWrapper>> data) {
        List<ObservableList<CellWrapper>> filteredData = new ArrayList<>();

        // Loop through each row within the range
        for (int i = range.startRow; i <= range.endRow; i++) {
            ObservableList<CellWrapper> originalRow = data.get(i);
            CellWrapper cellToFilter = originalRow.get(filterColumnIndex);

            if (cellToFilter != null && cellToFilter.getEffectiveValue() != null) {
                String effectiveValue = cellToFilter.getEffectiveValue();
                if (effectiveValue != null) {
                    String cellValue = effectiveValue;

                    // Check if the cell value is one of the selected values
                    if (selectedValues.contains(cellValue)) {
                        // Create a copy of the row with its styles
                        ObservableList<CellWrapper> rowCopy = FXCollections.observableArrayList();
                        for (int j = range.startCol; j <= range.endCol; j++) {
                            CellWrapper originalCell = originalRow.get(j);
                            CellWrapper cellCopy = new CellWrapper(originalCell.getOriginalValue(),originalCell.getEffectiveValue(),
                                    originalCell.getVersion(),originalCell.getOriginalRow(), originalCell.getColumn());
                            cellCopy.setStyle(originalCell.getStyle());
                            rowCopy.add(cellCopy);
                        }
                        filteredData.add(rowCopy);
                    }
                }
            }
        }

        // Display the filtered data in a popup
        SheetPopupUtils.displayFilteredDataInPopup(filteredData, range);
    }
}
