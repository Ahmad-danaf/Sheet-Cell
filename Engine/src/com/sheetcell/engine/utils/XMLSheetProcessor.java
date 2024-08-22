package com.sheetcell.engine.utils;


import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.Sheet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.schema.generatedFiles.*;


import java.io.File;


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

            String value = stlCell.getSTLOriginalValue();
            currentSheet.updateCellValueAndCalculate(cellRow, cellColumn, value);
        }
    }

    private void validateCellPosition(int cellRow, int cellColumn, int totalRows, int totalColumns) {
        boolean isRowInvalid = cellRow < (MIN_SHEET_ROWS - 1) || cellRow >= totalRows;
        boolean isColumnInvalid = cellColumn < (MIN_SHEET_COLUMNS - 1) || cellColumn >= totalColumns;

        if (isRowInvalid || isColumnInvalid) {
            throw new IllegalArgumentException("Cell at (" + cellRow + ", " + cellColumn + ") is outside the valid range. "
                    + "Valid rows: " + MIN_SHEET_ROWS  + "-" + totalRows + ", "
                    + "Valid columns: " + MIN_SHEET_COLUMNS + "-" + totalColumns);
        }
    }

    public Sheet getCurrentSheet() {
        return this.currentSheet;
    }
}
