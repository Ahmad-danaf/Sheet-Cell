package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class TimesExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public TimesExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        // Evaluate the expressions
        EffectiveValue leftValue = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);

        // Attempt to cast the left and right values to Double
        Double leftNumeric = leftValue.castValueTo(Double.class);
        Double rightNumeric = rightValue.castValueTo(Double.class);

        // If either value is null, it means the cast failed (not a numeric type)
        if (leftNumeric == null || rightNumeric == null) {
            throw new IllegalArgumentException("Error: One of the arguments provided to the TIMES function is not numeric. " +
                    "Please ensure that both arguments are valid numeric values.");
        }

        // Perform the multiplication
        double result = leftNumeric * rightNumeric;

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}

