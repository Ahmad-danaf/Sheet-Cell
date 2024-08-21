package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class IdentityExpression implements Expression {

    private final Object value;
    private final CellType type;

    public IdentityExpression(Object value, CellType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        return new EffectiveValue(type, value);
    }

    @Override
    public CellType getFunctionResultType() {
        return type;
    }
}
