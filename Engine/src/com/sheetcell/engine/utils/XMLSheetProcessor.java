package com.sheetcell.engine.utils;


import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.expression.api.*;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.expression.parser.FunctionParser;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.schema.generatedFiles.*;


import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class XMLSheetProcessor {

    private static final int MAX_SHEET_ROWS = 50;
    private static final int MIN_SHEET_ROWS = 1;
    private static final int MAX_SHEET_COLUMNS = 20;
    private static final int MIN_SHEET_COLUMNS = 1;

    private Sheet currentSheet;

    public void processSheetFile(String xmlFilePath) throws JAXBException {
        if (!xmlFilePath.toLowerCase().endsWith(".xml")) {
            throw new JAXBException(xmlFilePath + " is not a valid XML file");
        }
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists()) {
            throw new JAXBException(xmlFile.getPath() + " does not exist");
        }
        STLSheet importedSheet = parseXMLToSTLSheet(xmlFile);
        mapSTLSheetToSheet(importedSheet);
    }


    private STLSheet parseXMLToSTLSheet(File xmlFile) throws JAXBException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (STLSheet) jaxbUnmarshaller.unmarshal(xmlFile);
        } catch (JAXBException e) {
            throw new JAXBException("Error parsing XML file: " + xmlFile.getPath(), e);
        }
    }

    private void mapSTLSheetToSheet(STLSheet stlSheet) {
        String sheetName = stlSheet.getName();
        int sheetColumns = stlSheet.getSTLLayout().getColumns();
        int sheetRows = stlSheet.getSTLLayout().getRows();
        ensureValidSheetDimensions(sheetRows, sheetColumns);

        int columnUnitWidth = stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();
        int rowUnitHeight = stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        currentSheet = new Sheet(sheetName, sheetRows, sheetColumns, rowUnitHeight, columnUnitWidth);
        fillSheetWithRanges(stlSheet);
        fillSheetWithCells(stlSheet, sheetRows, sheetColumns);
    }

    private void ensureValidSheetDimensions(int sheetRows, int sheetColumns) {
        if (sheetRows > MAX_SHEET_ROWS || sheetRows < MIN_SHEET_ROWS) {
            throw new IllegalArgumentException("Sheet rows must be between " + MIN_SHEET_ROWS + " and " + MAX_SHEET_ROWS);
        }
        if (sheetColumns > MAX_SHEET_COLUMNS || sheetColumns < MIN_SHEET_COLUMNS) {
            throw new IllegalArgumentException("Sheet columns must be between " + MIN_SHEET_COLUMNS + " and " + MAX_SHEET_COLUMNS);
        }
    }

    private void fillSheetWithCells(STLSheet stlSheet, int sheetRows, int sheetColumns) {
        for (STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            int cellColumn = CoordinateFactory.convertColumnLabelToIndex(stlCell.getColumn());
            int cellRow = stlCell.getRow() - 1;
            validateCellPosition(cellRow, cellColumn, sheetRows, sheetColumns);

            String value = stlCell.getSTLOriginalValue().trim();
            currentSheet.setOriginalValueDuringLoad(cellRow, cellColumn, value);
        }

        // Establish dependencies without calculating EffectiveValues
        Map<Coordinate, Cell> activeCells = currentSheet.getActiveCells();
        for (Cell cell : activeCells.values()) {
            updateDependencies(cell, activeCells);
        }

        // Topologically sort cells and calculate EffectiveValues
        List<Cell> sortedCells = CellGraphManager.topologicalSort(activeCells);

        for (Cell cell : sortedCells) {
            cell.calculateEffectiveValue();
        }
        currentSheet.setCellChangeCount(activeCells.size());
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
        }else if (expression instanceof RangeExpression) {
            RangeExpression rangeExpr = (RangeExpression) expression;
            String rangeName = rangeExpr.getRange();

            // Get the coordinates of the range
            Set<Coordinate> rangeCoordinates = RangeFactory.getRange(rangeName);

            if (rangeCoordinates.isEmpty()) {
                throw new IllegalArgumentException("Error: The specified range '" + rangeName + "' does not exist.");
            }
            // Add each cell in the range as a dependency
            for (Coordinate coord : rangeCoordinates) {
                Cell referencedCell = activeCells.get(coord);
                if (referencedCell != null) {
                    callingCell.addDependency(referencedCell);
                    referencedCell.addInfluencedCell(callingCell);
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


    private void validateCellPosition(int cellRow, int cellColumn, int totalRows, int totalColumns) {
        boolean isRowInvalid = cellRow < (MIN_SHEET_ROWS - 1) || cellRow >= totalRows;
        boolean isColumnInvalid = cellColumn < (MIN_SHEET_COLUMNS - 1) || cellColumn >= totalColumns;

        if (isRowInvalid || isColumnInvalid) {
            throw new IllegalArgumentException("Cell at (" + (cellRow + 1) + ", " + CoordinateFactory.convertIndexToColumnLabel(cellColumn) + ") is outside the valid range. "
                    + "Valid rows: " + MIN_SHEET_ROWS + "-" + totalRows + ", "
                    + "Valid columns: " + MIN_SHEET_COLUMNS + "-" + totalColumns);
        }
    }

    public Sheet getCurrentSheet() {
        return this.currentSheet;
    }

    private void fillSheetWithRanges(STLSheet stlSheet) {
        if (stlSheet.getSTLRanges() != null) {
            for (STLRange stlRange : stlSheet.getSTLRanges().getSTLRange()) {
                String rangeName = stlRange.getName();
                String from = stlRange.getSTLBoundaries().getFrom();
                String to = stlRange.getSTLBoundaries().getTo();

                // Convert "from" and "to" to coordinates
                int[] fromCoordinates = CoordinateFactory.convertCellIdToIndex(from);
                int[] toCoordinates = CoordinateFactory.convertCellIdToIndex(to);

                Coordinate fromCoord = CoordinateFactory.createCoordinate(fromCoordinates[0], fromCoordinates[1]);
                Coordinate toCoord = CoordinateFactory.createCoordinate(toCoordinates[0], toCoordinates[1]);

                // Add or update the range in the sheet
                currentSheet.addRange(rangeName, fromCoord, toCoord);
            }
        }
    }

}
