package desktop.utils.parsing;

import com.sheetcell.engine.coordinate.CoordinateFactory;
import desktop.utils.cell.CellRange;

import java.util.ArrayList;
import java.util.List;

public class ParsingUtils {

    public static int extractNewVersionSheet(String input) {
        // Use split method to break the string at "Version " and " Cells Changed:"
        String[] parts = input.split("Version | Cells Changed:");

        // The newVersionSheet will be in the second part of the split array
        return Integer.parseInt(parts[1].trim());
    }

    // Parse comma-separated column letters into list of integers (column indices)
    public static List<Integer> parseColumns(String columnsInput) {
        String[] columnLetters = columnsInput.split(",");
        List<Integer> columns = new ArrayList<>();
        for (String colLetter : columnLetters) {
            colLetter = colLetter.trim();
            int colIndex = CoordinateFactory.convertColumnLabelToIndex(colLetter);
            columns.add(colIndex);
        }
        return columns;
    }

    // Parse cell range (e.g., "A1..B4") into a CellRange object
    public static CellRange parseRange(String rangeInput) {
        String[] parts = rangeInput.split("\\.\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid range format.");
        }
        String startCell = parts[0];
        String endCell = parts[1];

        int startRow = CoordinateFactory.getRowIndex(startCell);
        int startCol = CoordinateFactory.getColumnIndex(startCell);
        int endRow = CoordinateFactory.getRowIndex(endCell);
        int endCol = CoordinateFactory.getColumnIndex(endCell);

        return new CellRange(startRow, startCol, endRow, endCol);
    }

}
