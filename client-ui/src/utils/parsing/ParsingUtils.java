package utils.parsing;

import com.sheetcell.engine.coordinate.CoordinateFactory;
import utils.cell.CellRange;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {

    public static int parseVersion(String versionString) {
        // Define the regular expression pattern to match the version
        String pattern = "Version\\s+(\\d+)\\s+\\(\\d+\\s+changes\\)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(versionString);

        if (matcher.find()) {
            // Extract and return the version as an integer
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1; // Return -1 if no match is found
        }
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

    public static void main(String[] args) {
        String versionString = "Version 2 (45 changes)";
        int version = parseVersion(versionString);
        System.out.println(version); // Output: 2
    }

}
