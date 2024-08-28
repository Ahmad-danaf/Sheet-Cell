package com.sheetcell.engine.sheet;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.expression.api.*;
import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.sheet.api.SheetUpdateActions;
import com.sheetcell.engine.utils.CellGraphManager;
import com.sheetcell.engine.utils.SheetUpdateResult;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Sheet implements SheetReadActions, SheetUpdateActions, Serializable {
    private static final long serialVersionUID = 1L;


    private String name;
    private int version;
    private int maxRows;
    private int MaxColumns;
    private Map<Coordinate, Cell> activeCells;
    private int rowHeight;
    private int columnWidth;
    private int CellChangeCount;

    // Constructor
    public Sheet(String name, int MaxRows, int MaxColumns,int rowHeight, int columnWidth) {
        this.name = name;
        this.maxRows = MaxRows;
        this.MaxColumns = MaxColumns;
        this.version = 1; // Start at version 1
        this.activeCells = new HashMap<>();
        this.rowHeight = rowHeight;
        this.columnWidth = columnWidth;
        this.CellChangeCount = 0;
    }

    // Getters
    @Override
    public String getSheetName() {
        return name;
    }

    @Override
    public int getVersion() { return version; }

    @Override
    public int getMaxRows() {
        return maxRows;
    }

    @Override
    public int getMaxColumns() {
        return MaxColumns;
    }

    @Override
    public int getRowHeight() {
        return rowHeight;
    }

    @Override
    public int getColumnWidth() {
        return columnWidth;
    }

    public Map<Coordinate, Cell> getActiveCells() {
        return activeCells;
    }

    @Override
    public int getCellChangeCount() {
        return CellChangeCount;
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

    public void setCellChangeCount(int cellChangeCount) {
        this.CellChangeCount = cellChangeCount;
    }

    @Override
    public SheetUpdateResult setCell(int row, int column, String value) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);

        Sheet newSheetVersion = copySheet();
        newSheetVersion.resetCellChangeCount();

        // Get the old cell that is about to be replaced (if it exists)
        Cell oldCell = newSheetVersion.activeCells.get(coordinate);

        // Backup the old cell's dependencies and influenced cells
        Map<Cell, Set<Cell>> oldDependenciesBackup = new HashMap<>();
        Map<Cell, Set<Cell>> oldInfluencedCellsBackup = new HashMap<>();

        if (oldCell != null) {
            for (Cell cell : newSheetVersion.activeCells.values()) {
                if (cell.getDependencies().contains(oldCell)) {
                    oldDependenciesBackup.put(cell, new HashSet<>(cell.getDependencies()));
                    cell.removeDependency(oldCell);
                }
                if (cell.getInfluencedCells().contains(oldCell)) {
                    oldInfluencedCellsBackup.put(cell, new HashSet<>(cell.getInfluencedCells()));
                    cell.getInfluencedCells().remove(oldCell);
                }
            }
        }

        Cell newCell = new Cell(row, column, value, newSheetVersion.getVersion() +1 , newSheetVersion);
        newSheetVersion.activeCells.put(coordinate, newCell);
        Map<Coordinate, Cell> newActiveSheetVersion=newSheetVersion.getActiveCells();
        try{
            newCell.setOriginalValue(value);
            for (Cell cell : newActiveSheetVersion.values()) {
                newSheetVersion.updateDependencies(cell, newActiveSheetVersion);
            }

            // Topologically sort cells and recalculate effective values
            List<Cell> sortedCells = CellGraphManager.topologicalSort(newActiveSheetVersion);

            for (Cell cell : sortedCells) {
                boolean updated = cell.calculateEffectiveValue();
                if (updated) {
                    cell.setVersion(newSheetVersion.getVersion()+1);
                    newSheetVersion.incrementCellChangeCount();

                }
            }
            newSheetVersion.incrementVersion();
            return new SheetUpdateResult(newSheetVersion, null);
        }
        catch (Exception e) {
            // Restore the old cell's dependencies and influenced cells
            if (oldCell != null) {
                for (Map.Entry<Cell, Set<Cell>> entry : oldDependenciesBackup.entrySet()) {
                    Cell cell = entry.getKey();
                    cell.getDependencies().clear();
                    cell.getDependencies().addAll(entry.getValue());
                }
                for (Map.Entry<Cell, Set<Cell>> entry : oldInfluencedCellsBackup.entrySet()) {
                    Cell cell = entry.getKey();
                    cell.getInfluencedCells().clear();
                    cell.getInfluencedCells().addAll(entry.getValue());
                }
            }
            return new SheetUpdateResult(this, e.getMessage());
        }
    }

    @Override
    public SheetUpdateResult deleteCell(int row, int column) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);

        // Create a new version of the sheet
        Sheet newSheetVersion = copySheet();
        newSheetVersion.resetCellChangeCount();
        newSheetVersion.setCellChangeCount(1);
        Cell cellToDelete = newSheetVersion.activeCells.remove(coordinate);
        if (cellToDelete == null) {
            return new SheetUpdateResult(this, "The cell at " + CoordinateFactory.convertIndexToCellCord(row, column) + " is already empty or does not exist. No action was taken.", true);
        }
        Map<Coordinate, Cell> newActiveSheetVersion = newSheetVersion.getActiveCells();
        // Remove the deleted cell from the dependencies of other cells
        for (Cell cell : newActiveSheetVersion.values()) {
            cell.removeDependency(cellToDelete);
            cell.getInfluencedCells().remove(cellToDelete);
        }
        try {
            // Recalculate dependencies and effective values
            for (Cell cell : newActiveSheetVersion.values()) {
                newSheetVersion.updateDependencies(cell, newActiveSheetVersion);
            }

            // Topologically sort cells and recalculate effective values
            List<Cell> sortedCells = CellGraphManager.topologicalSort(newActiveSheetVersion);

            for (Cell cell : sortedCells) {
                boolean updated = cell.calculateEffectiveValue();
                if (updated) {
                    cell.setVersion(newSheetVersion.getVersion());
                    newSheetVersion.incrementCellChangeCount();
                }
            }

            newSheetVersion.incrementVersion();
            return new SheetUpdateResult(newSheetVersion, null);
        } catch (Exception e) {
            // Return the current sheet with the error message
            return new SheetUpdateResult(this, e.getMessage());
        }
    }



    private void updateDependencies(Cell cell, Map<Coordinate, Cell> activeCells) {
        // Clear existing dependencies and influenced cells
        for (Cell dependency : cell.getDependencies()) {
            dependency.removeInfluencedCell(cell);
        }
        cell.getDependencies().clear();

        // Parse the cell's formula to detect dependencies
        Expression expression = FunctionParser.parseExpression(cell.getOriginalValue());

        // Recursively evaluate expressions to detect all dependencies
        findAndRegisterDependencies(expression, cell, activeCells);

        // Update influenced cells
        for (Cell dependency : cell.getDependencies()) {
            dependency.addInfluencedCell(cell);
        }
    }

    private void findAndRegisterDependencies(Expression expression, Cell callingCell, Map<Coordinate, Cell> activeCells) {
        if (expression instanceof ReferenceExpression) {
            ReferenceExpression refExpr = (ReferenceExpression) expression;
            Coordinate refCoord = refExpr.getCoordinate();
            Cell referencedCell = activeCells.get(refCoord);

            if (referencedCell != null) {
                callingCell.addDependency(referencedCell);
                referencedCell.addInfluencedCell(callingCell);
            }
        } else if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpr = (UnaryExpression) expression;
            findAndRegisterDependencies(unaryExpr.getArgument(), callingCell, activeCells);
        } else if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpr = (BinaryExpression) expression;
            findAndRegisterDependencies(binaryExpr.getLeft(), callingCell, activeCells);
            findAndRegisterDependencies(binaryExpr.getRight(), callingCell, activeCells);
        } else if (expression instanceof TernaryExpression) {
            TernaryExpression ternaryExpr = (TernaryExpression) expression;
            findAndRegisterDependencies(ternaryExpr.getFirst(), callingCell, activeCells);
            findAndRegisterDependencies(ternaryExpr.getSecond(), callingCell, activeCells);
            findAndRegisterDependencies(ternaryExpr.getThird(), callingCell, activeCells);
        }
        // NOTE: DONT FORGET other expression types if necessary,
        // or leave them as is if they don't need special handling.
        // No need to handle IdentityExpression or literals since they don't have dependencies

    }

//    @Override
//    public Sheet updateCellValueAndCalculate(int row, int column, String value) {
//        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
//
//        Sheet newSheetVersion = copySheet();
//        Cell newCell = new Cell(row, column, value, newSheetVersion.getVersion() +1 , newSheetVersion);
//        newSheetVersion.activeCells.put(coordinate, newCell);
//
//        try {
//            List<Cell> cellsThatHaveChanged =
//                    newSheetVersion
//                            .orderCellsForCalculation()
//                            .stream()
//                            .filter(Cell::calculateEffectiveValue)
//                            .collect(Collectors.toList());
//
//            // successful calculation. update sheet and relevant cells version
//            // int newVersion = newSheetVersion.increaseVersion();
//            // cellsThatHaveChanged.forEach(cell -> cell.updateVersion(newVersion));
//
//            return newSheetVersion;
//        } catch (Exception e) {
//            // deal with the runtime error that was discovered as part of invocation
//            return this;
//        }
//    }

    // Sets the original value of a cell during the XML loading process without calculating effective values.
    public void setOriginalValueDuringLoad(int row, int column, String originalValue) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
        Cell newCell = new Cell(row, column, originalValue, this.version, this);
        this.activeCells.put(coordinate, newCell);
    }

    public void incrementCellChangeCount() {
        this.CellChangeCount++;
    }

    public void resetCellChangeCount() {
        this.CellChangeCount = 0;
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
        // 2. implement copy constructor for CellImpl and SheetImp
        // how about serialization:
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
