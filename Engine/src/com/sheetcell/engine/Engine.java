package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;

public interface Engine {
    void loadSheet(String filePath); // Loads sheet data from an XML file
    void saveSheet(String filePath); // Saves the current sheet to a file
    void setCellValue(String cellId, String value); // Sets the value of a cell
    void displaySheet(); // Displays the current sheet
    Cell getCell(String cellId); // Retrieves a cell by its ID
}
