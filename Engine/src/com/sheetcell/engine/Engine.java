package com.sheetcell.engine;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.utils.ColumnProperties;
import com.sheetcell.engine.utils.SheetUpdateResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {
    void loadSheet(String filePath) throws Exception; // Loads sheet data from an XML file
    void saveSheet(String filePath) throws IOException; // Saves the current sheet to a file
    SheetUpdateResult setCellValue(String cellId, String value); // Sets the value of a cell
    SheetReadActions getReadOnlySheet(); // Displays the current sheet
    Cell getCell(String cellId); // Retrieves a cell by its ID
    Map<Integer, Integer> getSheetVersions();
    SheetReadActions getSheetVersion(int version);
    void doesCellIdVaildAndExist(String cellId);
    void doesCellIdVaild(String cellId);
    void addRange(String rangeName, String rangeDefinition);
    Set<String> getAllRanges();
    void deleteRange(String rangeName);
    Set<Coordinate> getRangeCoordinates(String rangeName);
    Set<Coordinate> getDependenciesForCell(int row, int col);
    Set<Coordinate> getInfluencedForCell(int row, int col);
    void setColumnProperties(Integer column, String alignment, int height, int width);
    void setColumnAlignment(Integer column, String alignment);
    void setColumnHeight(Integer column, int height);
    void setColumnWidth(Integer column, int width);
    ColumnProperties getColumnProperties(Integer column);

}
