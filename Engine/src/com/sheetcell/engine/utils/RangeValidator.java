package com.sheetcell.engine.utils;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangeValidator {

    // Pattern to match the range format (e.g., "A1..A4", "A3..D3", "A3..D4")
    private static final Pattern RANGE_PATTERN = Pattern.compile("([A-Z]+)(\\d+)\\.\\.([A-Z]+)(\\d+)");

    private int maxRows;
    private int maxCols;

    public RangeValidator(int maxRows, int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
    public void setMaxCols(int maxCols) {
        this.maxCols = maxCols;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getMaxCols() {
        return maxCols;
    }

    public boolean isValidRange(String rangeDefinition) {
        // Normalize input
        rangeDefinition = rangeDefinition.toUpperCase();

        Matcher matcher = RANGE_PATTERN.matcher(rangeDefinition);
        if (!matcher.matches()) {
            return false; // Invalid format
        }

        // Extract the components of the range
        String col1 = matcher.group(1);
        int row1 = Integer.parseInt(matcher.group(2));
        String col2 = matcher.group(3);
        int row2 = Integer.parseInt(matcher.group(4));

        // Validate column and row bounds
        int col1Index = columnToIndex(col1);
        int col2Index = columnToIndex(col2);

        return (row1 >= 1 && row1 <= maxRows &&
                row2 >= 1 && row2 <= maxRows &&
                col1Index >= 0 && col1Index < maxCols &&
                col2Index >= 0 && col2Index < maxCols &&
                row1 <= row2 &&
                col1Index <= col2Index);
    }

    public Coordinate[] parseRange(String rangeDefinition) {
        // Normalize input
        rangeDefinition = rangeDefinition.toUpperCase();

        Matcher matcher = RANGE_PATTERN.matcher(rangeDefinition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range format");
        }

        // Extract the components of the range
        String col1 = matcher.group(1);
        int row1 = Integer.parseInt(matcher.group(2));
        String col2 = matcher.group(3);
        int row2 = Integer.parseInt(matcher.group(4));

        // Convert to 0-based indices
        int col1Index = columnToIndex(col1);
        int col2Index = columnToIndex(col2);
        int row1Index = row1 - 1;
        int row2Index = row2 - 1;

        // Create and return coordinates
        Coordinate start = CoordinateFactory.createCoordinate(row1Index, col1Index);
        Coordinate end = CoordinateFactory.createCoordinate(row2Index, col2Index);

        return new Coordinate[]{start, end};
    }

    private int columnToIndex(String column) {
        int index = 0;
        for (char c : column.toCharArray()) {
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Convert to 0-based index
    }

    public static void main(String[] args) {
        // Example usage
        RangeValidator validator = new RangeValidator(10, 10); // Assume 100 rows and 26 columns (A-Z)

        String testRange1 = "A1..A4"; // Single column range
        String testRange2 = "A3..D3"; // Single row range
        String testRange3 = "A3..D4"; // 2D range
        String testRange4 = "a1..a6"; // Single column range

        if (validator.isValidRange(testRange1)) {
            Coordinate[] coords = validator.parseRange(testRange1);
            System.out.println("Start: " + coords[0] + ", End: " + coords[1]);
        }

        if (validator.isValidRange(testRange2)) {
            Coordinate[] coords = validator.parseRange(testRange2);
            System.out.println("Start: " + coords[0] + ", End: " + coords[1]);
        }

        if (validator.isValidRange(testRange3)) {
            Coordinate[] coords = validator.parseRange(testRange3);
            System.out.println("Start: " + coords[0] + ", End: " + coords[1]);
        }
        if (validator.isValidRange(testRange4)) {
            Coordinate[] coords = validator.parseRange(testRange4);
            System.out.println("Start: " + coords[0] + ", End: " + coords[1]);
        }
        else {
            System.out.println(testRange4 + " is not a valid range");
        }
    }
}
