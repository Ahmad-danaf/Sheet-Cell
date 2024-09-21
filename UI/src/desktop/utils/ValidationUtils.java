package desktop.utils;

import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.utils.RangeValidator;
import desktop.utils.parsing.ParsingUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidationUtils {

    public static void validateSortInput(String rangeInput, String columnsInput, int maxRows, int maxColumns) {
        RangeValidator rangeValidator = new RangeValidator(maxRows, maxColumns);

        if (!rangeValidator.isValidRange(rangeInput)) {
            throw new IllegalArgumentException("Invalid range. Please ensure the range is within the sheet bounds and correctly formatted.");
        }

        // Validate columns format
        if (!columnsInput.matches("^[a-zA-Z](,[a-zA-Z])*$")) {
            throw new IllegalArgumentException("Invalid columns format. Expected format: a,b,c");
        }

        // Check if the columns are within the valid range of the sheet
        List<Integer> columns = ParsingUtils.parseColumns(columnsInput);
        Set<Integer> uniqueColumns = new HashSet<>();
        for (int column : columns) {
            if (column < 0 || column >= maxColumns) {
                throw new IllegalArgumentException("Column out of bounds. Please enter valid columns.");
            }
            if (!uniqueColumns.add(column)) {
                throw new IllegalArgumentException("Column " + CoordinateFactory.convertIndexToColumnLabel(column) + " has already been selected. Duplicate selections are not allowed.");
            }
        }
    }

    public static void validateFilterInput(String rangeInput, String columnInput, List<String> filterValues,int maxRows, int maxColumns) {
        RangeValidator rangeValidator = new RangeValidator(maxRows, maxColumns);

        if (!rangeValidator.isValidRange(rangeInput)) {
            throw new IllegalArgumentException("Invalid range. Please ensure the range is within the sheet bounds and correctly formatted.");
        }

        if (!columnInput.matches("[A-Z]")) {
            throw new IllegalArgumentException("Invalid column format. Expected a single letter representing the column.");
        }

        if (filterValues.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one value to filter.");
        }
    }
}
