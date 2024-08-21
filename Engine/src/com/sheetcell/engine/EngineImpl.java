package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.sheet.Sheet;

public class EngineImpl implements Engine {
    private Sheet sheet;

    public EngineImpl(String name, int rows, int columns) {
        this.sheet = new Sheet(name, rows, columns);
    }

    @Override
    public void loadSheet(String filePath) {
        // Load sheet data from an XML file
    }

    @Override
    public void saveSheet(String filePath) {
        // Save the current sheet to a file
    }

    @Override
    public void setCellValue(String cellId, String value) {
        // Sets the value of a cell
    }

    @Override
    public void displaySheet() {
        // Displays the current sheet
    }

    @Override
    public Cell getCell(String cellId) {
        // Retrieves a cell by its ID
        return null;
    }
}
