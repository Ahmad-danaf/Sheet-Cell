package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.XMLSheetProcessor;

import java.util.Map;
import java.util.HashMap;

public class EngineImpl implements Engine {
    private Sheet currentSheet;
    private Map<Integer, Sheet> sheetVersions;
    private XMLSheetProcessor xmlSheetProcessor;


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
    public void saveSheet(String filePath) {
        // Save the current sheet to a file
    }

    @Override
    public void setCellValue(String cellId, String value) {
        // vlaidate cellId...(later)

        int[] coords= CoordinateFactory.convertCellIdToIndex(cellId);
        int row= coords[0];
        int col= coords[1];

        Sheet tempSheet=currentSheet.setCell(row, col, value);


        // if(tempSheet != currentSheet){
        if(tempSheet.getVersion() != currentSheet.getVersion()){
            this.sheetVersions.put(tempSheet.getVersion(), tempSheet);
            currentSheet= tempSheet;
        }
        else {
            throw new IllegalArgumentException("Cell not updated");
        }
    }

    @Override
    public SheetReadActions getReadOnlySheet() {
        return currentSheet;
    }



    @Override
    public Cell getCell(String cellId) {
        // Retrieves a cell by its ID
        return null;
    }
}
