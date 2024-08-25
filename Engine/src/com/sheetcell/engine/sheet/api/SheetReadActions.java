package com.sheetcell.engine.sheet.api;

import com.sheetcell.engine.cell.Cell;

public interface SheetReadActions {
    int getVersion();
    Cell getCell(int row, int column);
    String getOriginalValue(int row, int column);
    int getMaxRows();
    int getMaxColumns();
    int getRowHeight();
    int getColumnWidth();
    String getSheetName();
}
