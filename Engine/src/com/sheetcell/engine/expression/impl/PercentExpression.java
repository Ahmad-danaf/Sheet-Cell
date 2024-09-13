package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.BinaryExpression;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class PercentExpression implements BinaryExpression {
    private final Expression part;
    private final Expression whole;

    public PercentExpression(Expression part, Expression whole) {
        this.part = part;
        this.whole = whole;
    }

    @Override
    public Expression getLeft() {
        return part;
    }

    @Override
    public Expression getRight() {
        return whole;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Evaluate both expressions
        EffectiveValue partValue = part.eval(sheet, callingCell);
        EffectiveValue wholeValue = whole.eval(sheet, callingCell);

        // Check for numeric types
        Double partNumeric = partValue.castValueTo(Double.class);
        Double wholeNumeric = wholeValue.castValueTo(Double.class);

        if (partNumeric == null || wholeNumeric == null) {
            return new EffectiveValue(CellType.NUMERIC, Double.NaN); // Return NaN if invalid
        }

        double result = (partNumeric * wholeNumeric) / 100.0;

        return new EffectiveValue(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}

