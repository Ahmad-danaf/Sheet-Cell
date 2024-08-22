package com.sheetcell.engine.sheet;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.sheet.api.SheetUpdateActions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sheet implements SheetReadActions, SheetUpdateActions {
    private String name;
    private int version;
    private int maxRows;
    private int MaxColumns;
    private Map<Coordinate, Cell> activeCells;
    private int rowHeight;
    private int columnWidth;

    // Constructor
    public Sheet(String name, int MaxRows, int MaxColumns,int rowHeight, int columnWidth) {
        this.name = name;
        this.maxRows = MaxRows;
        this.MaxColumns = MaxColumns;
        this.version = 1; // Start at version 1
        this.activeCells = new HashMap<>();
        this.rowHeight = rowHeight;
        this.columnWidth = columnWidth;
    }

    // Getters
    public String getName() {
        return name;
    }

    @Override
    public int getVersion() { return version; }

    public int getMaxRows() {
        return maxRows;
    }

    public int getMaxColumns() {
        return MaxColumns;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    @Override
    public Cell getCell(int row, int column) {
        return activeCells.get(CoordinateFactory.createCoordinate(row, column));
    }

    @Override
    public String getOriginalValue(int row, int column) {
        Cell cell = activeCells.get(CoordinateFactory.createCoordinate(row, column));
        if (cell == null) {
            return "";
        }
        else {
            return cell.getOriginalValue();
        }
    }

    @Override
    public Sheet updateCellValueAndCalculate(int row, int column, String value) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);

        Sheet newSheetVersion = copySheet();
        Cell newCell = new Cell(row, column, value, newSheetVersion.getVersion() + 1, newSheetVersion);
        newSheetVersion.activeCells.put(coordinate, newCell);

        try {
            List<Cell> cellsThatHaveChanged =
                    newSheetVersion
                            .orderCellsForCalculation()
                            .stream()
                            .filter(Cell::calculateEffectiveValue)
                            .collect(Collectors.toList());

            // successful calculation. update sheet and relevant cells version
            // int newVersion = newSheetVersion.increaseVersion();
            // cellsThatHaveChanged.forEach(cell -> cell.updateVersion(newVersion));

            return newSheetVersion;
        } catch (Exception e) {
            // deal with the runtime error that was discovered as part of invocation
            return this;
        }
    }

    // Increment the version of the sheet
    private void incrementVersion() {
        this.version++;
    }

    private List<Cell> orderCellsForCalculation() {
        // data structure 1 0 1: Topological sort...
        // build graph from the cells. each cell is a node. each cell that has ref(s) constitutes an edge
        // handle case of circular dependencies -> should fail
        return null;
    }

    private Sheet copySheet() {
        // lots of options here:
        // 1. implement clone all the way (yac... !)
        // 2. implement copy constructor for CellImpl and SheetImpl

        // 3. how about serialization ?
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (Sheet) ois.readObject();
        } catch (Exception e) {
            // deal with the runtime error that was discovered as part of invocation
            return this;
        }
    }
}
