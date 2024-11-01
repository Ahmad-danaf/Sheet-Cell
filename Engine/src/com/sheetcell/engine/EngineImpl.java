package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.*;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


public class EngineImpl implements Engine, Serializable {
    private static final long serialVersionUID = 3556326708908783837L;


    private Sheet currentSheet;
    private Map<Integer, Sheet> sheetVersions;
    private final RangeValidator rangeValidator;
    ColumnRowPropertyManager columnRowPropertyManager;


    public EngineImpl() {
        this.sheetVersions = new HashMap<>();
        currentSheet= null;
        this.rangeValidator = new RangeValidator(0, 0);
        columnRowPropertyManager = new ColumnRowPropertyManager();
    }

    @Override
    public void loadSheet(String filePath) throws Exception {
        XMLSheetProcessor xmlSheetProcessor = new XMLSheetProcessor();
        xmlSheetProcessor.processSheetFile(filePath);
        this.currentSheet = xmlSheetProcessor.getCurrentSheet();
        this.sheetVersions.clear();
        this.currentSheet.setOnload(false);
        this.sheetVersions.put(currentSheet.getVersion(), currentSheet);
        this.rangeValidator.setMaxRows(currentSheet.getMaxRows());
        this.rangeValidator.setMaxCols(currentSheet.getMaxColumns());
        this.columnRowPropertyManager.clearAllProperties();
        this.columnRowPropertyManager.initAllProperties(currentSheet.getMaxRows(),currentSheet.getMaxColumns(), currentSheet.getRowHeight(), currentSheet.getColumnWidth());
    }

    @Override
    public void loadSheetFromContentXML(String fileContent, String username) throws Exception {
        XMLSheetProcessor xmlSheetProcessor = new XMLSheetProcessor();
        xmlSheetProcessor.processSheetContent(fileContent);
        this.currentSheet = xmlSheetProcessor.getCurrentSheet();
        this.sheetVersions.clear();
        this.currentSheet.setOnload(false);
        this.sheetVersions.put(currentSheet.getVersion(), currentSheet);
        this.rangeValidator.setMaxRows(currentSheet.getMaxRows());
        this.rangeValidator.setMaxCols(currentSheet.getMaxColumns());
        this.columnRowPropertyManager.clearAllProperties();
        this.columnRowPropertyManager.initAllProperties(currentSheet.getMaxRows(),currentSheet.getMaxColumns(), currentSheet.getRowHeight(), currentSheet.getColumnWidth());
        this.currentSheet.assignUsernameToAllCells(username);
    }


    @Override
    public void saveSheet(String filePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this); // Serialize the entire EngineImpl object
        } catch (Exception e) {
            throw new IOException("Failed to save the sheet to the specified path: " + filePath, e);
        }
    }

    public static EngineImpl loadState(String filePath) {
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            EngineImpl engine = (EngineImpl) in.readObject(); // Deserialize the EngineImpl object
            return engine;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    public SheetUpdateResult setCellValue(String cellId, String value) {
        doesCellIdVaild(cellId);

        int[] coords = CoordinateFactory.convertCellIdToIndex(cellId);
        int row = coords[0];
        int col = coords[1];

        SheetUpdateResult result;

        if (value.isEmpty()) {
            // Delete the cell if the value is an empty string
            doesCellIdVaildAndExist(cellId);
            result = currentSheet.deleteCell(row, col);
        } else {
            // Set the cell value if it's not empty
            result = currentSheet.setCell(row, col, value);
        }

        if (!result.hasError()) {
            Sheet tempSheet = result.getSheet();
            this.sheetVersions.put(tempSheet.getVersion(), tempSheet);
            currentSheet = tempSheet;
        }
        return result;
    }

    @Override
    public SheetUpdateResult setCellValue(String cellId, String value,String username) {
        doesCellIdVaild(cellId);

        int[] coords = CoordinateFactory.convertCellIdToIndex(cellId);
        int row = coords[0];
        int col = coords[1];

        SheetUpdateResult result;

        if (value.isEmpty()) {
            // Delete the cell if the value is an empty string
            doesCellIdVaildAndExist(cellId);
            result = currentSheet.deleteCell(row, col,username);
        } else {
            // Set the cell value if it's not empty
            result = currentSheet.setCell(row, col, value,username);
        }

        if (!result.hasError()) {
            Sheet tempSheet = result.getSheet();
            this.sheetVersions.put(tempSheet.getVersion(), tempSheet);
            currentSheet = tempSheet;
        }
        return result;
    }


    @Override
    public SheetReadActions getReadOnlySheet() {
        return currentSheet;
    }

    @Override
    public Map<Integer, Integer> getSheetVersions() {
        Map<Integer, Integer> versionDetails = new HashMap<>();
        for (Map.Entry<Integer, Sheet> entry : sheetVersions.entrySet()) {
            int version = entry.getKey();
            int changedCells = entry.getValue().getCellChangeCount();
            versionDetails.put(version, changedCells);
        }
        return versionDetails;
    }

    @Override
    public SheetReadActions getSheetVersion(int version) {
        if (!sheetVersions.containsKey(version)) {
            throw new IllegalArgumentException("Invalid version number: " + version + ". Please enter a valid version.");
        }
        return sheetVersions.get(version);

    }

    @Override
    public Cell getCell(String cellId) {
        if(cellId == null){
            throw new IllegalArgumentException("Invalid cell identifier format: '" + cellId + "'. Please ensure you are using the correct format (e.g., 'A1').");
        }
        if (cellId.isEmpty()) {
            throw new IllegalArgumentException("Invalid cell identifier format: '" + cellId + "'. Please ensure you are using the correct format (e.g., 'A1').");
        }
        if (currentSheet == null) {
            throw new IllegalArgumentException("The sheet is empty. Please load a sheet before accessing cells.");
        }
        doesCellIdVaildAndExist(cellId);
        return currentSheet.getCell(CoordinateFactory.convertCellIdToIndex(cellId)[0], CoordinateFactory.convertCellIdToIndex(cellId)[1]);
    }

    @Override
    public void doesCellIdVaildAndExist(String cellId) {
        try {
            CoordinateFactory.validateCellIdFormat(cellId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cell identifier format: '" + cellId + "'. Please ensure you are using the correct format (e.g., 'A1').");
        }

        int[] coords = CoordinateFactory.convertCellIdToIndex(cellId);
        int row = coords[0];
        int col = coords[1];

        if (row >= currentSheet.getMaxRows() || col >= currentSheet.getMaxColumns()) {
            throw new IllegalArgumentException("The cell '" + cellId + "' is out of bounds. The sheet has a maximum of "
                    + currentSheet.getMaxRows() + " rows and " + currentSheet.getMaxColumns() + " columns.");
        }
        if(row < 0 || col < 0){
            throw new IllegalArgumentException("Invalid cell identifier format: '" + cellId + "'. Please ensure you are using the correct format (e.g., 'A1').");
        }

        Cell cell = currentSheet.getCell(row, col);
        if (cell == null) {
            throw new IllegalArgumentException("The cell '" + cellId + "' is empty. Please check the cell identifier and try again.");
        }
    }

    @Override
    public void doesCellIdVaild(String cellId) {
        try {
            CoordinateFactory.validateCellIdFormat(cellId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cell identifier format: '" + cellId + "'. Please ensure you are using the correct format (e.g., 'A1').");
        }
        int row = CoordinateFactory.convertCellIdToIndex(cellId)[0];
        int col = CoordinateFactory.convertCellIdToIndex(cellId)[1];
        if(row < 0 || col < 0){
            throw new IllegalArgumentException("Invalid cell identifier format: '" + cellId + "'. Please ensure you are using the correct format (e.g., 'A1').");
        }
        if (row >= currentSheet.getMaxRows() || col >= currentSheet.getMaxColumns()) {
            throw new IllegalArgumentException("The cell '" + cellId + "' is out of bounds. The sheet has a maximum of "
                    + currentSheet.getMaxRows() + " rows and " + currentSheet.getMaxColumns() + " columns.");
        }
    }


    @Override
    public void addRange(String rangeName, String rangeDefinition) {
            if (rangeName == null || rangeName.isEmpty()) {
                throw new IllegalArgumentException("Invalid range name");
            }
            if (rangeDefinition == null || rangeDefinition.isEmpty()) {
                throw new IllegalArgumentException("Invalid range definition");
            }
            if (currentSheet == null) {
                throw new IllegalArgumentException("The sheet is empty. Please load a sheet before adding ranges.");
            }
            if (!rangeValidator.isValidRange(rangeDefinition)) {
                throw new IllegalArgumentException("Invalid range definition");
            }

            Coordinate[] coordinates = rangeValidator.parseRange(rangeDefinition);
            Coordinate from = coordinates[0];
            Coordinate to = coordinates[1];

            // Add the range to the current sheet
            currentSheet.addRange(rangeName, from, to);

    }

    @Override
    public Set<String> getAllRanges() {
        if (currentSheet == null) {
            throw new IllegalArgumentException("The sheet is empty. Please load a sheet before accessing ranges.");
        }
        return currentSheet.getRanges();
    }

    @Override
    public void deleteRange(String rangeName) {
        if (currentSheet == null) {
            throw new IllegalArgumentException("The sheet is empty. Please load a sheet before deleting ranges.");
        }
        currentSheet.deleteRange(rangeName);
    }

    @Override
    public Set<Coordinate> getRangeCoordinates(String rangeName) {
        if (currentSheet == null) {
            throw new IllegalArgumentException("The sheet is empty. Please load a sheet before accessing ranges.");
        }
        return currentSheet.getRangeCoordinates(rangeName);
    }

    @Override
    public Set<Coordinate> getDependenciesForCell(int row, int col) {
        doesCellIdVaild(CoordinateFactory.convertIndexToCellCord(row, col));
        return currentSheet.getDependenciesForCell(row, col);
    }

    @Override
    public Set<Coordinate> getInfluencedForCell(int row, int col) {
        doesCellIdVaild(CoordinateFactory.convertIndexToCellCord(row, col));
        return currentSheet.getInfluencedForCell(row, col);
    }

    @Override
    public void setColumnProperties(Integer column, String alignment, int width) {
        if (column < 0 || column >= currentSheet.getMaxColumns()) {
            throw new IllegalArgumentException("Invalid column number: " + column + ". Please enter a valid column number.");
        }
        columnRowPropertyManager.setColumnProperties(column, alignment, width);
    }

    @Override
    public void setColumnAlignment(Integer column, String alignment) {
        if (column < 0 || column >= currentSheet.getMaxColumns()) {
            throw new IllegalArgumentException("Invalid column number: " + column + ". Please enter a valid column number.");
        }
        columnRowPropertyManager.setColumnAlignment(column, alignment);
    }

    @Override
    public void setColumnWidth(Integer column, int width) {
        if (column < 0 || column >= currentSheet.getMaxColumns()) {
            throw new IllegalArgumentException("Invalid column number: " + column + ". Please enter a valid column number.");
        }
        columnRowPropertyManager.setColumnWidth(column, width);
    }

    @Override
    public ColumnProperties getColumnProperties(Integer column) {
        if (column < 0 || column >= currentSheet.getMaxColumns()) {
            throw new IllegalArgumentException("Invalid column number: " + column + ". Please enter a valid column number.");
        }
        return columnRowPropertyManager.getColumnProperties(column);
    }

    @Override
    public void setRowProperties(Integer row, int height) {
        if (row < 0 || row >= currentSheet.getMaxRows()) {
            throw new IllegalArgumentException("Invalid row number: " + row + ". Please enter a valid row number.");
        }
        columnRowPropertyManager.setRowProperties(row, height);
    }

    @Override
    public void setRowHeight(Integer row, int height) {
        if (row < 0 || row >= currentSheet.getMaxRows()) {
            throw new IllegalArgumentException("Invalid row number: " + row + ". Please enter a valid row number.");
        }
        columnRowPropertyManager.setRowHeight(row, height);
    }

    @Override
    public RowProperties getRowProperties(Integer row) {
        if (row < 0 || row >= currentSheet.getMaxRows()) {
            throw new IllegalArgumentException("Invalid row number: " + row + ". Please enter a valid row number.");
        }
        return columnRowPropertyManager.getRowProperties(row);
    }

    @Override
    public DynamicAnalysisResult performDynamicAnalysis(int row, int column, double minValue, double maxValue, double step) {
        DynamicAnalysisResult analysisResult = new DynamicAnalysisResult(currentSheet.getMaxRows(), currentSheet.getMaxColumns());
        Coordinate targetCoord = CoordinateFactory.createCoordinate(row, column);
        Cell originalCell = currentSheet.getCell(row, column);

        // Ensure the cell is numeric and not a formula
        if (originalCell == null || !originalCell.isNumeric() || originalCell.isFormula()) {
            throw new IllegalArgumentException("Selected cell must be a numeric cell and not a formula.");
        }
        // Iterate over the range, updating the cell's value temporarily
        Sheet tempSheet = currentSheet.copySheetForDynamicAnalysis();
        if (tempSheet == null) {
            throw new IllegalArgumentException("Failed to create a temporary sheet for dynamic analysis. Please try again.");
        }
        for (double value = minValue; value <= maxValue; value += step) {
            // Create a temporary sheet snapshot for analysis
            SheetReadActions res=tempSheet.setCellForDynamicAnalysis(row, column, Double.toString(value));

            // Generate a ReadOnlySheetActions view and store it in the result map
            analysisResult.addResult(value, res);
        }

        return analysisResult;
    }
}
