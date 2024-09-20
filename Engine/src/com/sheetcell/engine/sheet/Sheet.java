package com.sheetcell.engine.sheet;

import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.expression.api.*;
import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.sheet.api.SheetUpdateActions;
import com.sheetcell.engine.utils.CellGraphManager;
import com.sheetcell.engine.utils.RangeFactory;
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
    private Set<String> activeRanges;
    private int rowHeight;
    private int columnWidth;
    private int CellChangeCount;
    RangeFactory rangeFactory;
    private Map<Coordinate, Set<Coordinate>> dependenciesMap;
    private Map<Coordinate, Set<Coordinate>> influencedMap;

    // Constructor
    public Sheet(String name, int MaxRows, int MaxColumns,int rowHeight, int columnWidth) {
        this.name = name;
        this.maxRows = MaxRows;
        this.MaxColumns = MaxColumns;
        this.version = 1; // Start at version 1
        this.activeCells = new HashMap<>();
        this.activeRanges = new HashSet<>();
        this.rowHeight = rowHeight;
        this.columnWidth = columnWidth;
        this.CellChangeCount = 0;
        rangeFactory = new RangeFactory();
        dependenciesMap = new HashMap<>();
        influencedMap = new HashMap<>();
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

    public Set<Coordinate> getDependencies(Coordinate coord) {
        return dependenciesMap.getOrDefault(coord, Collections.emptySet());
    }

    public Set<Coordinate> getInfluenced(Coordinate coord) {
        return influencedMap.getOrDefault(coord, Collections.emptySet());
    }

    public Set<Coordinate> getDependenciesForCell(int row, int column) {
        Coordinate coord = CoordinateFactory.createCoordinate(row, column);
        return getDependencies(coord);
    }

    public Set<Coordinate> getInfluencedForCell(int row, int column) {
        Coordinate coord = CoordinateFactory.createCoordinate(row, column);
        return getInfluenced(coord);
    }

    public void addDependency(Coordinate callingCoord, Coordinate referencedCoord) {
        dependenciesMap.computeIfAbsent(callingCoord, k -> new HashSet<>()).add(referencedCoord);
        influencedMap.computeIfAbsent(referencedCoord, k -> new HashSet<>()).add(callingCoord);
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
        Cell doesOldCellExist = activeCells.get(coordinate);
        if (doesOldCellExist!=null && doesOldCellExist.getOriginalValue()!= null &&
                doesOldCellExist.getOriginalValue().equals(value)){
            return new SheetUpdateResult(this,
                    "The cell at " + CoordinateFactory.convertIndexToCellCord(row, column) +
                            " already has the value " + value + ". No action was taken.", true);
        }
        Set<String> backupActiveRangesSet = new HashSet<>(activeRanges);
        activeRanges.clear();
        Map<Coordinate, Set<Coordinate>> dependenciesBackup = new HashMap<>();
        Map<Coordinate, Set<Coordinate>> influencedBackup = new HashMap<>();
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : dependenciesMap.entrySet()) {
            dependenciesBackup.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : influencedMap.entrySet()) {
            influencedBackup.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        dependenciesBackup.clear();
        influencedBackup.clear();

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
            activeRanges.clear();
            activeRanges.addAll(backupActiveRangesSet);
            dependenciesMap.clear();
            dependenciesMap.putAll(dependenciesBackup);

            influencedMap.clear();
            influencedMap.putAll(influencedBackup);
            return new SheetUpdateResult(this, e.getMessage());
        }
    }

    @Override
    public SheetUpdateResult deleteCell(int row, int column) {
        Coordinate coordinate = CoordinateFactory.createCoordinate(row, column);
        if (!activeCells.containsKey(coordinate)) {
            return new SheetUpdateResult(this, "The cell at " + CoordinateFactory.convertIndexToCellCord(row, column) + " is already empty or does not exist. No action was taken.", true);
        }
        Set<String> backupActiveRangesSet = new HashSet<>(activeRanges);
        activeRanges.clear();
        Map<Coordinate, Set<Coordinate>> dependenciesBackup = new HashMap<>();
        Map<Coordinate, Set<Coordinate>> influencedBackup = new HashMap<>();
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : dependenciesMap.entrySet()) {
            dependenciesBackup.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : influencedMap.entrySet()) {
            influencedBackup.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        dependenciesBackup.clear();
        influencedBackup.clear();
        // Create a new version of the sheet
        Sheet newSheetVersion = copySheet();
        newSheetVersion.resetCellChangeCount();
        newSheetVersion.setCellChangeCount(1);
        Cell cellToDelete = newSheetVersion.activeCells.remove(coordinate);
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
            activeCells.clear();
            activeRanges.addAll(backupActiveRangesSet);
            dependenciesMap.clear();
            dependenciesMap.putAll(dependenciesBackup);

            influencedMap.clear();
            influencedMap.putAll(influencedBackup);
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
            addDependency(callingCell.getCoordinate(), refCoord);
            if (referencedCell != null) {
                callingCell.addDependency(referencedCell);
                referencedCell.addInfluencedCell(callingCell);
            }
        } else if (expression instanceof RangeExpression) {
            RangeExpression rangeExpr = (RangeExpression) expression;
            String rangeName = rangeExpr.getRange();

            // Get the coordinates of the range
            Set<Coordinate> rangeCoordinates = rangeFactory.getRange(rangeName);


            // Add each cell in the range as a dependency
            for (Coordinate coord : rangeCoordinates) {
                Cell rangeCell = activeCells.get(coord);
                addDependency(callingCell.getCoordinate(), coord);
                if (rangeCell != null) {
                    callingCell.addDependency(rangeCell);
                    rangeCell.addInfluencedCell(callingCell);
                }
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

    public void addRange(String rangeName, Coordinate from, Coordinate to) {
        if (!isCoordinateWithinBounds(from) || !isCoordinateWithinBounds(to)) {
            throw new IllegalArgumentException("Error in"+ rangeName +": Range coordinates are outside the boundaries of the sheet.");
        }
        rangeFactory.addRange(rangeName, from, to);
    }

    private boolean isCoordinateWithinBounds(Coordinate coord) {
        int maxRows = getMaxRows(); // Method or variable that provides the maximum number of rows in the sheet
        int maxColumns = getMaxColumns(); // Method or variable that provides the maximum number of columns in the sheet

        return coord.getRow() >= 0 && coord.getRow() < maxRows &&
                coord.getColumn() >= 0 && coord.getColumn() < maxColumns;
    }

    @Override
    public Set<Coordinate> getRangeCoordinates(String rangeName) {
        return rangeFactory.getRange(rangeName);
    }

    public void deleteRange(String rangeName) {
        if(isRangeUsed(rangeName)){
            throw new IllegalArgumentException("The range '"+rangeName+"' is in use and cannot be deleted.");
        }
        rangeFactory.deleteRange(rangeName);
        activeRanges.remove(rangeName);
    }

    public boolean isRangeUsed(String rangeName) {
        return activeRanges.contains(rangeName);
    }

    public void markRangeAsUsed(String rangeName) {
        activeRanges.add(rangeName);
    }

    public void markRangeAsUnused(String rangeName) {
        activeRanges.remove(rangeName);
    }

    public Set<String> getRanges() {
        return rangeFactory.getAllRangeNames();
    }

    public RangeFactory getRangeFactory() {
        return rangeFactory;
    }

    public void setRowHeight(int height) {
        this.rowHeight = height;
    }

    public void setColumnWidth(int width) {
        this.columnWidth = width;
    }
}
