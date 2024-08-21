package com.sheetcell.engine.expression.api;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public interface Expression {
    EffectiveValue eval(SheetReadActions sheet);
    CellType getFunctionResultType();
}
