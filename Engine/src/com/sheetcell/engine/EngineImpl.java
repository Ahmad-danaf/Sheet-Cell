package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.SheetUpdateResult;
import com.sheetcell.engine.utils.XMLSheetProcessor;

import java.io.*;
import java.util.Map;
import java.util.HashMap;


public class EngineImpl implements Engine, Serializable {
    private static final long serialVersionUID = 3556326708908783837L;


    private Sheet currentSheet;
    private Map<Integer, Sheet> sheetVersions;


    public EngineImpl() {
        this.sheetVersions = new HashMap<>();
        currentSheet= null;
    }

    @Override
    public void loadSheet(String filePath) throws Exception {
        XMLSheetProcessor xmlSheetProcessor = new XMLSheetProcessor();
        xmlSheetProcessor.processSheetFile(filePath);
        this.currentSheet = xmlSheetProcessor.getCurrentSheet();
        this.sheetVersions.clear();
        this.sheetVersions.put(currentSheet.getVersion(), currentSheet);
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
        // Retrieves a cell by its ID
        return null;
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
}
