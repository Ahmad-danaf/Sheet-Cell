package com.sheetcell.engine;

import java.util.HashMap;
import java.util.Map;

public class Sheet {
    private String name;
    private int version;
    private int rows;
    private int columns;
    private Map<Coordinate, Cell> cells;

    // Constructor
    public Sheet(String name, int rows, int columns) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.version = 1; // Start at version 1
        this.cells = new HashMap<>();
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Cell getCell(int row, int column) {
        return cells.get(CoordinateFactory.createCoordinate(row, column));
    }

    // Set or Update a Cell
    public void setCell(int row, int column, String originalValue) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
        Cell cell = cells.get(coordinate);
        if (cell == null) {
            cell = new Cell(row, column, originalValue);
            cells.put(coordinate, cell);
        } else {
            cell.setOriginalValue(originalValue);
        }
        recalculateDependencies(cell);
        incrementVersion();
    }

    // Recalculate dependencies (simplified version)
    private void recalculateDependencies(Cell cell) {
        // going to implement the logic to update all cells that depend on the changed cell.
        for (Cell dependent : cell.getInfluencedCells()) {
            dependent.setOriginalValue(dependent.getOriginalValue()); // Re-evaluate the dependent cell
        }
    }

    // Increment the version of the sheet
    private void incrementVersion() {
        this.version++;
    }

    // Display the sheet (simple text output)
    public void displaySheet() {
        System.out.println("Sheet: " + name + " (Version: " + version + ")");
        System.out.println("Rows: " + rows + ", Columns: " + columns);

        // Display column headers
        System.out.print("    "); // Offset for row numbers
        for (char col = 'A'; col < 'A' + columns; col++) {
            System.out.print("  " + col + " ");
        }
        System.out.println();

        // Display each row
        for (int row = 1; row <= rows; row++) {
            System.out.printf("%02d  ", row); // Row numbers with two digits
            for (char col = 'A'; col < 'A' + columns; col++) {
                Coordinate cord = new Coordinate(row, col);
                Cell cell = cells.get(cord);
                if (cell != null) {
                    System.out.print(String.format("%-10s", cell.getEvaluatedValue()));
                } else {
                    System.out.print("          "); // Empty cell space
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    // print a specific cell's details
    public void displayCellDetails(int row, int column) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
        Cell cell = getCell(row, column);
        if (cell != null) {
            System.out.println("Cell " + coordinate + ":");
            System.out.println("Original Value: " + cell.getOriginalValue());
            System.out.println("Effective Value: " + cell.getEvaluatedValue());
            System.out.println("Version: " + cell.getVersion());
            System.out.println("Dependencies: " + cell.getDependencies().size());
            System.out.println("Influenced Cells: " + cell.getInfluencedCells().size());
        } else {
            System.out.println("Cell " + coordinate + " is empty.");
        }
    }
}
