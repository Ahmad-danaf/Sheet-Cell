package com.sheetcell.engine.expression.api;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public interface Expression {
    EffectiveValue eval(SheetReadActions sheet, Cell callingCell);
    CellType getFunctionResultType();
}
