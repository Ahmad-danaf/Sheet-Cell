package com.sheetcell.engine.expression.impl.bool;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.api.UnaryExpression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class NotExpression implements UnaryExpression {
    private final Expression argument;

    public NotExpression(Expression argument) {
        this.argument = argument;
    }

    @Override
    public Expression getArgument() {
        return argument;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        EffectiveValue argumentResult = argument.eval(sheet, callingCell);

        // Check for invalid input
        if (Expression.isInvalidBoolean(argumentResult)) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN");
        }

        Boolean value = argumentResult.castValueTo(Boolean.class);
        if (value == null) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN");
        }

        boolean result = !value;
        return new EffectiveValue(CellType.BOOLEAN, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }

}
