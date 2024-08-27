package com.sheetcell.engine.sheet.api;

import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.utils.SheetUpdateResult;

public interface SheetUpdateActions {
    SheetUpdateResult setCell(int row, int column, String value);
    SheetUpdateResult deleteCell(int row, int column);
}
