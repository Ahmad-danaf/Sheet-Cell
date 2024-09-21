package com.sheetcell.engine.coordinate;

import java.util.HashMap;
import java.util.Map;

public class CoordinateFactory {
    private static Map<String, Coordinate> cachedCoordinates = new HashMap<>();

    public static Coordinate createCoordinate(int row, int column) {

        String key = row + ":" + column;
        if (cachedCoordinates.containsKey(key)) {
            return cachedCoordinates.get(key);
        }

        Coordinate coordinate = new Coordinate(row, column);
        cachedCoordinates.put(key, coordinate);

        return coordinate;
    }

    public static Coordinate from(String cellId) {
        try {
            cellId = cellId.toUpperCase();
            int[] parts = convertCellIdToIndex(cellId);
            return createCoordinate(parts[0], parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Converts a column label (for example: "A") to a 0-based column index(for example: 0)
    public static int convertColumnLabelToIndex(String colLabel) {
        colLabel = colLabel.toUpperCase(); // Ensure the column label is uppercase
        int colIndex = 0;

        // Process each character in the column label
        for (int i = 0; i < colLabel.length(); i++) {
            char currentChar = colLabel.charAt(i);
            // Calculate the 1-based index for the current character
            int charValue = currentChar - 'A' + 1;
            // Update the column index
            colIndex = colIndex * 26 + charValue;
        }

        // Convert to 0-based index
        return colIndex - 1;
    }

    // Converts a 0-based column index to a column label (for example: 30 -> "AD")
    public static String convertIndexToColumnLabel(int colIndex) {
        StringBuilder colLabel = new StringBuilder();

        while (colIndex >= 0) {
            int remainder = colIndex % 26;
            colLabel.insert(0, (char) ('A' + remainder));
            colIndex = (colIndex / 26) - 1;
        }

        return colLabel.toString();
    }

    // Converts 0-based row and column indices to cell Cord (for example: (7, 30) -> "AD7").
    public static String convertIndexToCellCord(int rowIndex, int columnIndex) {
        String columnLabel = convertIndexToColumnLabel(columnIndex);
        int rowLabel = rowIndex + 1;
        return columnLabel + rowLabel;
    }

    // Converts a cell ID (for example:, "AD7") into a 0-based row and column index
    public static int[] convertCellIdToIndex(String cellId) {
        cellId = cellId.toUpperCase(); // Convert the cell ID to uppercase to ensure consistency
        validateCellIdFormat(cellId);

        String columnLabel = extractColumnLabel(cellId);
        String rowLabel = extractRowLabel(cellId);

        int rowIndex = Integer.parseInt(rowLabel) - 1;  // Convert 1-based row label to 0-based index
        int columnIndex = convertColumnLabelToIndex(columnLabel);

        return new int[]{rowIndex, columnIndex};
    }

    // Validates that the cell ID is in the correct format (for example: "AD7")
    public static void validateCellIdFormat(String cellId) {
        cellId=cellId.toUpperCase();
        if (!cellId.matches("^[A-Z]+\\d+$")) {
            throw new IllegalArgumentException("Invalid cell ID format: " + cellId);
        }
    }

    // Extracts the column label from a cell ID (for example: "AD7" -> "AA")
    public static String extractColumnLabel(String cellId) {
        cellId=cellId.toUpperCase();
        int i = 0;
        while (i < cellId.length() && Character.isLetter(cellId.charAt(i))) {
            i++;
        }
        return cellId.substring(0, i);
    }

    // Extracts the row label from a cell ID (for example: "AD7" -> "7")
    public static String extractRowLabel(String cellId) {
        cellId=cellId.toUpperCase();
        int i = 0;
        while (i < cellId.length() && Character.isLetter(cellId.charAt(i))) {
            i++;
        }
        return cellId.substring(i);
    }

    // Convert cell address like "A1" to a column index (zero-based)
    public static int getColumnIndex(String cellAddress) {
        String colPart = cellAddress.replaceAll("\\d", "");  // Extract letters
        return CoordinateFactory.convertColumnLabelToIndex(colPart);
    }

    // Convert column label "Column A" to index (zero-based)
    public static int getColumnIndexFromLabel(String columnLabel) {
        // Remove the "Column " prefix
        String columnName = columnLabel.replace("Column ", "").trim();
        return CoordinateFactory.convertColumnLabelToIndex(columnName);
    }

    // Convert cell address like "A1" to a row index (zero-based)
    public static int getRowIndex(String cellAddress) {
        String rowPart = cellAddress.replaceAll("[^\\d]", "");  // Extract digits
        return Integer.parseInt(rowPart) - 1;  // Return zero-based index
    }

    //*******************************tests**********************************//
    public static void main(String[] args) {
        testFromCaseInsensitiveCellId();
        testCreateCoordinate();
        testFromValidCellId();
        testFromInvalidCellId();
        testConvertColumnLabelToIndex();
        testConvertIndexToColumnLabel();
        testConvertIndexToCellCord();
        testConvertCellIdToIndex();
        testGetColumnIndexFromLabel();
    }

    private static void testFromCaseInsensitiveCellId() {
        Coordinate coordUpper = CoordinateFactory.from("A13");
        Coordinate coordLower = CoordinateFactory.from("a13");
        if (coordUpper.equals(coordLower)) {
            System.out.println("testFromCaseInsensitiveCellId PASSED");
        } else {
            System.out.println("testFromCaseInsensitiveCellId FAILED");
        }
    }

    private static void testCreateCoordinate() {
        Coordinate coord1 = CoordinateFactory.createCoordinate(1, 1);
        Coordinate coord2 = CoordinateFactory.createCoordinate(1, 1);

        if (coord1 == coord2) {
            System.out.println("testCreateCoordinate PASSED");
        } else {
            System.out.println("testCreateCoordinate FAILED");
        }
    }

    private static void testFromValidCellId() {
        Coordinate coord = CoordinateFactory.from("B2");
        if (coord != null && coord.getRow() == 1 && coord.getColumn() == 1) {
            System.out.println("testFromValidCellId PASSED");
        } else {
            System.out.println("testFromValidCellId FAILED");
        }
    }

    private static void testFromInvalidCellId() {
        try {
            Coordinate coord = CoordinateFactory.from("Invalid");
            if (coord == null) {
                System.out.println("testFromInvalidCellId PASSED");
            } else {
                System.out.println("testFromInvalidCellId FAILED");
            }
        } catch (IllegalArgumentException e) {
            // Expected exception, test passes if we catch it
            System.out.println("testFromInvalidCellId PASSED");
        } catch (Exception e) {
            // Any other exception means test failed
            System.out.println("testFromInvalidCellId FAILED");
            e.printStackTrace();
        }
    }

    private static void testConvertColumnLabelToIndex() {
        if (CoordinateFactory.convertColumnLabelToIndex("A") == 0 &&
                CoordinateFactory.convertColumnLabelToIndex("Z") == 25 &&
                CoordinateFactory.convertColumnLabelToIndex("AA") == 26 &&
                CoordinateFactory.convertColumnLabelToIndex("AB") == 27) {
            System.out.println("testConvertColumnLabelToIndex PASSED");
        } else {
            System.out.println("testConvertColumnLabelToIndex FAILED");
        }
    }

    private static void testConvertIndexToColumnLabel() {
        if ("A".equals(CoordinateFactory.convertIndexToColumnLabel(0)) &&
                "Z".equals(CoordinateFactory.convertIndexToColumnLabel(25)) &&
                "AA".equals(CoordinateFactory.convertIndexToColumnLabel(26)) &&
                "AB".equals(CoordinateFactory.convertIndexToColumnLabel(27))) {
            System.out.println("testConvertIndexToColumnLabel PASSED");
        } else {
            System.out.println("testConvertIndexToColumnLabel FAILED");
        }
    }

    private static void testConvertIndexToCellCord() {
        if ("A1".equals(CoordinateFactory.convertIndexToCellCord(0, 0)) &&
                "B2".equals(CoordinateFactory.convertIndexToCellCord(1, 1)) &&
                "AA3".equals(CoordinateFactory.convertIndexToCellCord(2, 26))) {
            System.out.println("testConvertIndexToCellCord PASSED");
        } else {
            System.out.println("testConvertIndexToCellCord FAILED");
        }
    }

    private static void testConvertCellIdToIndex() {
        int[] indices = CoordinateFactory.convertCellIdToIndex("B2");
        if (indices.length == 2 && indices[0] == 1 && indices[1] == 1) {
            System.out.println("testConvertCellIdToIndex PASSED");
        } else {
            System.out.println("testConvertCellIdToIndex FAILED");
        }
    }
    public static void testGetColumnIndexFromLabel() {
        if (CoordinateFactory.getColumnIndexFromLabel("Column A") == 0 &&
                CoordinateFactory.getColumnIndexFromLabel("Column Z") == 25 &&
                CoordinateFactory.getColumnIndexFromLabel("Column AA") == 26 &&
                CoordinateFactory.getColumnIndexFromLabel("Column AB") == 27) {
            System.out.println("testGetColumnIndexFromLabel PASSED");
        } else {
            System.out.println("testGetColumnIndexFromLabel FAILED");
        }
    }
}
