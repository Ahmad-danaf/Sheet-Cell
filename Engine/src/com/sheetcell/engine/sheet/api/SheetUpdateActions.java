package com.sheetcell.engine.sheet.api;

import com.sheetcell.engine.sheet.Sheet;

public interface SheetUpdateActions {
    Sheet updateCellValueAndCalculate(int row, int column, String value);
}
