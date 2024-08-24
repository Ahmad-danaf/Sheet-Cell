package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public interface Engine {
    void loadSheet(String filePath) throws Exception; // Loads sheet data from an XML file
    void saveSheet(String filePath); // Saves the current sheet to a file
    void setCellValue(String cellId, String value); // Sets the value of a cell
    SheetReadActions displaySheet(); // Displays the current sheet
    Cell getCell(String cellId); // Retrieves a cell by its ID
}
