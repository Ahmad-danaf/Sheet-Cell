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
    private Sheet currentSheet;
    private Map<Integer, Sheet> sheetVersions;
    private transient XMLSheetProcessor xmlSheetProcessor; // Not serialized


    public EngineImpl() {
        this.sheetVersions = new HashMap<>();
        this.xmlSheetProcessor = new XMLSheetProcessor();
        currentSheet= null;
    }

    @Override
    public void loadSheet(String filePath) throws Exception {
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
            engine.xmlSheetProcessor = new XMLSheetProcessor(); // Re-initialize the transient field
            return engine;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setCellValue(String cellId, String value) {
        // vlaidate cellId...(later)

        int[] coords= CoordinateFactory.convertCellIdToIndex(cellId);
        int row= coords[0];
        int col= coords[1];
        SheetUpdateResult result = currentSheet.setCell(row, col, value);

        // if(tempSheet != currentSheet){
        if(!result.hasError()){
            Sheet tempSheet= result.getSheet();
            this.sheetVersions.put(tempSheet.getVersion(), tempSheet);
            currentSheet= tempSheet;
        }
        else {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
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
}
