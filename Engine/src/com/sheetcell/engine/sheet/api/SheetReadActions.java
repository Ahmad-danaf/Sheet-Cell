package com.sheetcell.engine.sheet.api;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.coordinate.Coordinate;

import java.util.Map;
import java.util.Set;

public interface SheetReadActions {
    public int getCellChangeCount();
    int getVersion();
    Cell getCell(int row, int column);
    String getOriginalValue(int row, int column);
    int getMaxRows();
    int getMaxColumns();
    int getRowHeight();
    int getColumnWidth();
    String getSheetName();
    Set<Coordinate> getRangeCoordinates(String rangeName);
    void markRangeAsUsed(String rangeName);
    boolean isOnLoad();
    boolean isRangeExists(String rangeName);
     Map<Coordinate, Set<Coordinate>> getDependenciesMap();
     Map<Coordinate, Set<Coordinate>> getInfluencedMap();

}
